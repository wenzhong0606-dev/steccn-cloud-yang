package com.steccn.qms.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.lang.Assert;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.nacos.common.utils.StringUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import com.google.common.collect.Lists;
import com.nimbusds.jose.util.IntegerUtils;
import com.steccn.authserver.exception.BusinessException;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.authserver.util.SecurityUtils;
import com.steccn.common.constant.MpConstant;
import com.steccn.common.util.BigDecimalUtil;
import com.steccn.common.util.LocalDateUtil;
import com.steccn.common.vo.R;
import com.steccn.platform.dto.SysOfficeParentDTO;
import com.steccn.platform.feign.RemoteOfficeFeign;
import com.steccn.platform.vo.SysOfficeVO;
import com.steccn.pms.constant.PmManagerConstant;
import com.steccn.pms.dto.PmsWorkOrderCraftStationDTO;
import com.steccn.pms.feign.RemotePmsFeignApi;
import com.steccn.pms.vo.PmsProduceOrderVO;
import com.steccn.qms.constant.QtManagerConstant;
import com.steccn.qms.domain.*;
import com.steccn.qms.dto.*;
import com.steccn.qms.mapper.QtCheckTaskCopyMapper;
import com.steccn.qms.mapper.QtDefectiveItemMapper;
import com.steccn.qms.mapper.QtMaterialLinkCheckPlanMapper;
import com.steccn.qms.service.*;
import com.steccn.qms.mapper.QtCheckTaskMapper;
import com.steccn.qms.util.CodeGenerateUtil;
import com.steccn.qms.util.ShardingTimeUtil;
import com.steccn.qms.vo.*;
import io.swagger.models.auth.In;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * @author AndrewLuo
 * @description 针对表【qt_check_task(巡检任务表)】的数据库操作Service实现
 * @createDate 2022-09-02 08:54:09
 */
@Service
@RequiredArgsConstructor
public class QtCheckTaskServiceImpl extends ServiceImpl<QtCheckTaskMapper, QtCheckTask>
        implements QtCheckTaskService {

    private final QtCheckTaskMapper qtCheckTaskMapper;

    private final RemoteOfficeFeign remoteOfficeFeign;

    private final QtMaterialService qtMaterialService;

    private final QtCheckPlanService qtCheckPlanService;

    private final QtCheckTaskDetailService qtCheckTaskDetailService;

    private final QtRecheckTaskService qtRecheckTaskService;

    private final QtMaterialLinkCheckPlanMapper qtMaterialLinkCheckPlanMapper;

    private final QtCheckTaskCopyMapper qtCheckTaskCopyMapper;

    private final QtDefectiveItemMapper qtDefectiveItemMapper;

    private final RemotePmsFeignApi remotePmsFeignApi;

    /**
     * 新增
     *
     * @param dto
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long add(QtAddCheckTaskDTO dto) {
        QtCheckTask qtCheckTask = new QtCheckTask();
        BeanUtils.copyProperties(dto, qtCheckTask);
        //如果新建检验类型为在线检，缺陷记录时段不允许跨班次;即8:00、20:00两个时间点
        if (dto.getTaskType().equals(QtManagerConstant.QtCheckTaskTypeEnum.ONLINE.getCode())) {
            if (dto.getDefectStartTime() != null && dto.getDefectEndTime() != null) {
                if (dto.getDefectStartTime().getHour() > QtManagerConstant.beforeHour
                        && dto.getDefectEndTime().getHour() > QtManagerConstant.afterHour) {
                    throw new BusinessException("缺陷记录时段不允许跨班次");
                } else if (dto.getDefectStartTime().getHour() < QtManagerConstant.afterHour && dto.getDefectEndTime().getHour() < QtManagerConstant.beforeHour) {
                    qtCheckTask.setClassesId(QtManagerConstant.MORNINGWORK);//早班
                } else {
                    qtCheckTask.setClassesId(QtManagerConstant.NIGHTWORK);//晚班
                }
            }
            Assert.notNull("缺陷记录时段不能为空");
        }

        //状态待检验
        qtCheckTask.setStatus(0);
        if (dto.getOriginalId() != null) {
            //复检任务
            qtCheckTask.setType(1);
        } else {
            //正常任务
            qtCheckTask.setType(0);
        }
        //获取最大的自增编码
        qtCheckTask.setCode(CodeGenerateUtil.getNextCode(CodeGenerateUtil.PREFIX_JYRW, CodeGenerateUtil.NUM_BIT_8, currentMaxCode()));
        /**
         * 判断是否需要创建检验方案快照【方案+分组+检验项+缺陷项】
         *      plan1 v1 -> T1 需要创建快照，plan1' v1
         *               -> T2 去查询最新版本是v1 不需要创建快照
         *      plan2 v2 -> T3 去查询最新版本是v2 需要创建快照 plan2' v2
         *               -> T4 去查询最新版本是v2 不需要创建快照
         */
        //获取检验方案id
        Integer checkPlanId = qtCheckTask.getCheckPlanId();
        //判断检验方案是否存在
        QtCheckPlan qtCheckPlan = qtCheckPlanService.getById(checkPlanId);
        if (Objects.isNull(qtCheckPlan)) {
            throw new BusinessException("检验方案不存在");
        }
        //获取最大版本号的快照数据
        QtCheckPlan maxSnapshot = qtCheckPlanService.getOne(new LambdaQueryWrapper<QtCheckPlan>()
                .eq(QtCheckPlan::getOriginalId, checkPlanId)
                .eq(QtCheckPlan::getShowFlag, MpConstant.NO)
                .orderByDesc(QtCheckPlan::getVersion)
                .last(MpConstant.STR_LIMIT_1));
        //如果第一次进来没有快照，或者快照版本已比当前版本低则重建快照，绑定最新的数据
        if (Objects.isNull(maxSnapshot) || maxSnapshot.getVersion() < qtCheckPlan.getVersion()) {
            QtCheckPlan planSnapshot = qtCheckPlanService.planSnapshot(checkPlanId);
            qtCheckTask.setCheckPlanId(planSnapshot.getId());
        } else {
            //取旧的快照
            qtCheckTask.setCheckPlanId(maxSnapshot.getId());
        }
        //复检任务需要置空
        if (dto.getOriginalId() != null) {
            qtCheckTask.setFinishedTime(null);
            qtCheckTask.setFinishedName(null);
            qtCheckTask.setFinishedBy(null);
            qtCheckTask.setCancelTime(null);
            qtCheckTask.setCancelBy(null);
            qtCheckTask.setCancelName(null);
            qtCheckTask.setStartTime(null);
            qtCheckTask.setStartBy(null);
            qtCheckTask.setStartName(null);
        }
        save(qtCheckTask);
        //如果有原始任务id,则表示为复检任务
        if (dto.getOriginalId() != null) {
            //复检任务
            QtCheckTask originalTask = getById(dto.getOriginalId());
            QtRecheckTask recheckModel = new QtRecheckTask();
            recheckModel.setOriginalId(dto.getOriginalId());
            recheckModel.setRecheckId(qtCheckTask.getId());
            recheckModel.setOriginalCode(originalTask.getCode());
            qtRecheckTaskService.save(recheckModel);
        }
        return qtCheckTask.getId();
    }

    /**
     * 任务列表
     *
     * @param dto
     * @return
     */
    @Override
    public List<QtCheckTaskListVO> checkTaskList(QtCheckTaskListDTO dto) {
        Assert.notNull(dto.getCheckTypeCode(), "检验类型不能为空");
        Assert.notNull(dto.getStatus(), "任务状态不能为空");
        List<QtCheckTask> qtCheckTasks = qtCheckTaskMapper.selectByTaskType(dto.getCheckTypeCode(), dto.getStatus());
        List<QtCheckTaskListVO> vos = new ArrayList<>();
        if (qtCheckTasks != null && qtCheckTasks.size() > 0) {
            qtCheckTasks.forEach(tmp -> {
                QtCheckTaskListVO qtCheckTaskListVo = new QtCheckTaskListVO();
                qtCheckTaskListVo.setId(tmp.getId());
                qtCheckTaskListVo.setStatus(tmp.getStatus());
                //返回名字
                qtCheckTaskListVo.setMaterialName(qtMaterialService.getById(tmp.getMaterialId()).getName());
                qtCheckTaskListVo.setProductWorkshopName(remoteOfficeFeign.getNameById(tmp.getProductWorkshop().getId()).getMsg());
                qtCheckTaskListVo.setProductLineName(remoteOfficeFeign.getNameById(tmp.getProductLine().getId()).getMsg());
                vos.add(qtCheckTaskListVo);
            });
        }
        return vos;
    }

    /**
     * 任务详情
     *
     * @param id
     * @return
     */
    @Override
    public QtCheckTaskDetailVO taskDetail(Long id, Boolean isPc) {
        QtCheckTask qtCheckTask = getById(id);
        QtCheckTaskListVO qtCheckTaskListVo = new QtCheckTaskListVO();
        QtCheckPlanDetailsVO qtCheckPlanDetailsVO = new QtCheckPlanDetailsVO();
        if (qtCheckTask != null) {
            //json名字
            qtCheckTaskListVo.setMaterialName(qtMaterialService.getById(qtCheckTask.getMaterialId()).getName());
            //计划id
            qtCheckTaskListVo.setCheckPlanName(qtCheckPlanService.getById(qtCheckTask.getCheckPlanId()).getName());
            //
            if (qtCheckTask.getMaterialWorkshop() != null) {
                qtCheckTask.setMaterialWorkshopName(qtCheckTask.getMaterialWorkshop().getName());
            }
            if (qtCheckTask.getSupplier() != null) {
                qtCheckTask.setSupplierName(qtCheckTask.getSupplier().getName());
            }
            if (qtCheckTask.getCustomer() != null) {
                qtCheckTask.setCustomerName(qtCheckTask.getCustomer().getName());
            }
            if (qtCheckTask.getProductWorkshop() != null) {
                qtCheckTask.setProductWorkshopName(qtCheckTask.getProductWorkshop().getName());
            }
            if (qtCheckTask.getProductLine() != null) {
                qtCheckTask.setProductLineName(qtCheckTask.getProductLine().getName());
            }
            //如果是暂存状态，则返回检验中
            if (qtCheckTask.getStatus().equals(QtManagerConstant.QtCheckTaskStatusEnum.TEMPORARY.getCode())) {
                qtCheckTask.setStatus(QtManagerConstant.QtCheckTaskStatusEnum.CHECKING.getCode());
            }
            BeanUtils.copyProperties(qtCheckTask, qtCheckTaskListVo);
            //如果为复检任务则查询复检任务
            if (qtCheckTask.getType() == 1) {
                QtRecheckTask recheckTask = qtRecheckTaskService.getOne(new LambdaQueryWrapper<QtRecheckTask>().eq(QtRecheckTask::getRecheckId, id));
                qtCheckTaskListVo.setOriginalTaskCode(recheckTask.getOriginalCode());
            }
        }
        QtCheckTaskDetailVO qtCheckTaskDetailVo = new QtCheckTaskDetailVO();
        qtCheckTaskDetailVo.setQtCheckTaskListVo(qtCheckTaskListVo);

        //处理样本数
        //选择任务详情
        QueryWrapper<QtCheckTaskDetail> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("DISTINCT copy").eq("task_id", id).orderByAsc("copy");
        List<QtCheckTaskDetail> qtCheckTaskDetails = qtCheckTaskDetailService.list(queryWrapper);
        //手机端copy
        List<QtCheckTaskDetailCopyVO> copy = Lists.newArrayList();
        if (!qtCheckTaskDetails.isEmpty()) {
            //包装好group和copy
            for (QtCheckTaskDetail tmp : qtCheckTaskDetails) {
                QtCheckPlanDetailsVO qtCheckPlanDetailsVOTmp = qtCheckPlanService.info(qtCheckTask.getCheckPlanId(), id, tmp.getCopy());
                //如果有值则表示在线检，跳出循环即可
                if (CollUtil.isNotEmpty(qtCheckPlanDetailsVOTmp.getOnlineItem())) {
                    qtCheckPlanDetailsVO.setOnlineItem(qtCheckPlanDetailsVOTmp.getOnlineItem());
                    break;
                }
                //查看是否有details
                QtCheckTaskCopy qtCheckTaskCopy = qtCheckTaskCopyMapper.selectOne(new LambdaQueryWrapper<QtCheckTaskCopy>()
                        .eq(QtCheckTaskCopy::getTaskId, id)
                        .eq(QtCheckTaskCopy::getCopy, tmp.getCopy()));
                QtCheckTaskDetailCopyVO qtCheckTaskDetailCopyVO = new QtCheckTaskDetailCopyVO();
                qtCheckTaskDetailCopyVO.setNum(tmp.getCopy());
                qtCheckTaskDetailCopyVO.setCheckGroup(qtCheckPlanDetailsVOTmp.getCheckGroup());
                //放入copyDetails
                QtCheckTaskCopyDetailsDTO copyDetailsVO = new QtCheckTaskCopyDetailsDTO();
                if (qtCheckTaskCopy != null) {
                    BeanUtils.copyProperties(qtCheckTaskCopy, copyDetailsVO);
                }
                qtCheckTaskDetailCopyVO.setCopyDetails(copyDetailsVO);
                copy.add(qtCheckTaskDetailCopyVO);

            }

        }
        //details没有值说明没有输入详细值
        else {
            qtCheckPlanDetailsVO = qtCheckPlanService.info(qtCheckTask.getCheckPlanId(), id, null);
            QtCheckTaskDetailCopyVO qtCheckTaskDetailCopyVO = new QtCheckTaskDetailCopyVO();
            qtCheckTaskDetailCopyVO.setNum(1);
            qtCheckTaskDetailCopyVO.setCheckGroup(qtCheckPlanDetailsVO.getCheckGroup());
            copy.add(qtCheckTaskDetailCopyVO);
        }

        //pc端对数据进行处理
        if (isPc) {
            //copy没有值是在线检
            if (copy.size() > 0) {
                //先拿到checkGroup
                List<QtCheckPlanGroupVO> checkGroup = copy.get(0).getCheckGroup();
                //如果没有group则说明是在线检
                if (checkGroup != null) {
                    //对每个item进行赋值
                    checkGroup.forEach(groupTmp -> {
                        groupTmp.getItems().forEach(itemTmp -> {
                            Map<String, String> result = new HashMap<>();
                            Set<String> unQualifiedSet = new HashSet<>();
                            //对每一层copy进行遍历
                            for (QtCheckTaskDetailCopyVO copyTmp : copy) {
                                String num = String.valueOf(copyTmp.getNum());
                                //需要对每一个copy下的分组和项进行校验，匹配则放入值
                                copyTmp.getCheckGroup().forEach(copyGroupTmp -> {
                                    if (copyGroupTmp.getId().equals(groupTmp.getId())) {
                                        copyGroupTmp.getItems().forEach(itemGroupTmp -> {
                                            if (itemGroupTmp.getItemId().equals(itemTmp.getItemId())) {
                                                if (StringUtils.isNotBlank(itemGroupTmp.getCheckResult())) {
                                                    result.put("checkResult" + num, itemGroupTmp.getCheckResult());
                                                }
                                                if (itemGroupTmp.getDefectId() != null) {
                                                    result.put("defectId" + num, itemGroupTmp.getCheckResult());
                                                    result.put("defectName" + num, itemGroupTmp.getDefectName());
                                                }
                                                if (itemGroupTmp.getIsQualified() != null && itemGroupTmp.getIsQualified().equals(0)) {
                                                    unQualifiedSet.add("checkResult" + num);
                                                }
                                            }
                                        });
                                    }
                                });
                            }
                            itemTmp.setResult(result);
                            List<String> unQualifiedList = new ArrayList<>();
                            unQualifiedList.addAll(unQualifiedSet);
                            itemTmp.setIsQualifiedList(unQualifiedList);
                        });
                    });
                    qtCheckTaskDetailVo.setCheckGroup(checkGroup);
                } else {
                    qtCheckTaskDetailVo.setQtCheckPlanDetailsVO(qtCheckPlanDetailsVO);
                }
            } else {
                qtCheckTaskDetailVo.setQtCheckPlanDetailsVO(qtCheckPlanDetailsVO);
            }
        } else {
            qtCheckPlanDetailsVO.setCopy(copy);
            qtCheckTaskDetailVo.setQtCheckPlanDetailsVO(qtCheckPlanDetailsVO);
        }
        return qtCheckTaskDetailVo;
    }

    /**
     * 修改任务状态
     *
     * @param dto
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateStatus(QtCheckTaskStatusDTO dto) {
        QtCheckTask qtCheckTask = new QtCheckTask();
        QtCheckTask oldModel = getById(dto.getId());
        qtCheckTask.setId(dto.getId());
        qtCheckTask.setStatus(dto.getStatus());
        //开始任务
        if (dto.getStatus().equals(QtManagerConstant.QtCheckTaskStatusEnum.CHECKING.getCode())) {
            qtCheckTask.setStartBy(SecurityUtils.getUser().getUserId());
            qtCheckTask.setStartName(SecurityUtils.getUser().getName());
            qtCheckTask.setStartTime(LocalDateTime.now());
        }
        //取消任务
        if (dto.getStatus().equals(QtManagerConstant.QtCheckTaskStatusEnum.CANCELED.getCode())) {
            //不是待检验状态则无法修改
            if (!oldModel.getStatus().equals(QtManagerConstant.QtCheckTaskStatusEnum.WAITED.getCode())) {
                throw new BusinessException("只有待检验状态才能取消");
            }
            qtCheckTask.setCancelBy(SecurityUtils.getUser().getUserId());
            qtCheckTask.setCancelName(SecurityUtils.getUser().getName());
            qtCheckTask.setCancelTime(LocalDateTime.now());
        }
        //完成任务
        if (dto.getStatus().equals(QtManagerConstant.QtCheckTaskStatusEnum.FINISHED.getCode())) {
            qtCheckTask.setFinishedBy(SecurityUtils.getUser().getUserId());
            //修改完成任务显示拼音的bug
            //qtCheckTask.setFinishedName(SecurityUtils.getUser().getUsername());
            qtCheckTask.setFinishedName(SecurityUtils.getUser().getName());
            qtCheckTask.setFinishedTime(LocalDateTime.now());
        }
        return updateById(qtCheckTask);
    }

    /**
     * 添加值
     *
     * @param dto
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean itemValue(QtCheckTaskSubmitDTO dto) {
        List<QtCheckTaskDetail> valueModels = new ArrayList<>();
        QtCheckTask task = getById(dto.getId());
        //先删除可能暂存的值
        Map<String, Object> valueCondition = new HashMap<>();
        valueCondition.put("task_id", dto.getId());
        qtCheckTaskDetailService.removeByMap(valueCondition);
        //插入值
        if (dto.getValues() != null && dto.getValues().size() > 0) {
            dto.getValues().forEach(valueTmp -> {
                //value下的list
                if (valueTmp.getList() != null && valueTmp.getList().size() > 0) {
                    valueTmp.getList().forEach(listTmp -> {
                        //list下checkGroup
                        if (listTmp.getCheckGroup() != null && listTmp.getCheckGroup().size() > 0) {
                            listTmp.getCheckGroup().forEach(checkGroupTmp -> {
                                Map<String, Object> condition = new HashMap<>();
                                QtCheckTaskDetail qtCheckTaskDetail = new QtCheckTaskDetail();
                                qtCheckTaskDetail.setCopy(valueTmp.getCopy());
                                condition.put("copy", valueTmp.getCopy());
                                qtCheckTaskDetail.setCheckResult(checkGroupTmp.getCheckResult());
                                if (checkGroupTmp.getDefectId() != null) {
                                    qtCheckTaskDetail.setDefectId(checkGroupTmp.getDefectId());
                                }
                                qtCheckTaskDetail.setTaskId(dto.getId());
                                condition.put("task_id", dto.getId());
                                qtCheckTaskDetail.setItemId(checkGroupTmp.getItemId());
                                condition.put("item_id", checkGroupTmp.getItemId());
                                qtCheckTaskDetail.setGroupId(listTmp.getGroupId());
                                condition.put("group_id", listTmp.getGroupId());
                                //需要先删除之前保存的值
                                qtCheckTaskDetailService.removeByMap(condition);
                                qtCheckTaskDetail.setIsQualified(checkGroupTmp.getIsQualified());
                                valueModels.add(qtCheckTaskDetail);
                            });
                        }
                    });
                }
                //操作样本详情 当样本类型为来料检、化验抽检、配料抽检时，有样本模块
                if ((task.getTaskType().equals(QtManagerConstant.QtCheckTaskTypeEnum.INCOMING.getCode()) || task.getTaskType().equals(QtManagerConstant.QtCheckTaskTypeEnum.CHEMICAL.getCode()) || task.getTaskType().equals(QtManagerConstant.QtCheckTaskTypeEnum.MIXED.getCode())) && !Objects.isNull(valueTmp.getCopyDetails())) {
                    QtCheckTaskCopy copyDetailModel = new QtCheckTaskCopy();
                    Map<String, Object> deleteCondition = new HashMap<>();
                    deleteCondition.put("copy", valueTmp.getCopy());
                    copyDetailModel.setCopy(valueTmp.getCopy());
                    deleteCondition.put("task_id", dto.getId());
                    copyDetailModel.setTaskId(dto.getId());
                    BeanUtils.copyProperties(valueTmp.getCopyDetails(), copyDetailModel);
                    //先删除冗余数据
                    qtCheckTaskCopyMapper.deleteByMap(deleteCondition);
                    qtCheckTaskCopyMapper.insert(copyDetailModel);
                }
            });
            qtCheckTaskDetailService.saveBatch(valueModels);
        }
        //插入在线检数据
        else if (task.getTaskType().equals(QtManagerConstant.QtCheckTaskTypeEnum.ONLINE.getCode()) && dto.getOnlineItem() != null) {
            List<QtCheckTaskDetail> model = Lists.newArrayList();
            dto.getOnlineItem().forEach(tmp -> {
                QtCheckTaskDetail detail = new QtCheckTaskDetail();
                detail.setItemId(tmp.getItemId());
                detail.setGroupId(tmp.getGroupId());
                detail.setCopy(0);
                detail.setTaskId(dto.getId());
                List<QtOnlineCheckDefectDTO> defectList = new ArrayList<>();
                if (CollUtil.isNotEmpty(tmp.getCategory())) {
                    tmp.getCategory().forEach(category -> {
                        category.getDefect().forEach(categoryTmp -> {
                            defectList.add(categoryTmp);
                        });
                    });
                }
                detail.setDefectNum(defectList);
                model.add(detail);
            });
            qtCheckTaskDetailService.saveBatch(model);
        }
        QtCheckTaskStatusDTO statusDTO = new QtCheckTaskStatusDTO();
        statusDTO.setId(dto.getId());
        statusDTO.setStatus(dto.getStatus());
        return updateStatus(statusDTO);
    }

    /**
     * pc端添加值
     *
     * @param dto
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean pcItemValue(QtPcCheckTaskSubmitDTO dto) {
        if (CollUtil.isEmpty(dto.getCheckGroup()) && dto.getOnlineItem() == null) {
            throw new BusinessException("传的值为空");
        }
        List<QtCheckTaskDetail> valueModels = new ArrayList<>();
        QtCheckTask tasKDetail = getById(dto.getId());
        //先删除可能暂存的值
        Map<String, Object> valueCondition = new HashMap<>();
        valueCondition.put("task_id", dto.getId());
        qtCheckTaskDetailService.removeByMap(valueCondition);
        //处理样本概况数据
        //样本概况在来料捡,化验抽捡,配料抽检时显示
        if (tasKDetail.getTaskType().equals(QtManagerConstant.QtCheckTaskTypeEnum.ONLINE.getCode()) || tasKDetail.getTaskType().equals(QtManagerConstant.QtCheckTaskTypeEnum.INCOMING.getCode()) || tasKDetail.getTaskType().equals(QtManagerConstant.QtCheckTaskTypeEnum.CHEMICAL.getCode())) {
            if (CollUtil.isNotEmpty(dto.getSampleInfoList())) {
                QtCheckTaskCopy copyDetailModel = new QtCheckTaskCopy();
                Map<String, Object> deleteCondition = new HashMap<>();
                dto.getSampleInfoList().forEach(sampleInfo -> {
                    if (StringUtils.isNotBlank(sampleInfo.getSampleNum()) && sampleInfo.getSampleNum().equals(true)) {
                        for (Map.Entry<String, String> entry : sampleInfo.getResult().entrySet()) {


                        }

                    }
                });
            }
        }
        //处理数据
        if (CollUtil.isNotEmpty(dto.getCheckGroup())) {
            dto.getCheckGroup().forEach(group -> {
                if (CollUtil.isNotEmpty(group.getItems())) {
                    group.getItems().forEach(item -> {
                        //遍历Map
                        for (Map.Entry<String, String> entry : item.getResult().entrySet()) {
                            String key = entry.getKey();
                            //如果map值等于checkResult
                            if (key.substring(0, key.length() - 1).equals(QtManagerConstant.QT_CHECK_TASK_FIELD_CHECK_RESULT)) {
                                QtCheckTaskDetail qtCheckTaskDetail = new QtCheckTaskDetail();
                                //detail设置Result
                                qtCheckTaskDetail.setCheckResult(item.getResult().get(key));
                                //如果是在线检则设置copy为0
                                if (tasKDetail.getTaskType().equals(QtManagerConstant.QtCheckTaskTypeEnum.ONLINE.getCode())) {
                                    qtCheckTaskDetail.setCopy(0);
                                } else {
                                    //否则设置copy的值为末尾
                                    Integer copy = Integer.valueOf(key.substring(key.length() - 1));
                                    qtCheckTaskDetail.setCopy(copy);
                                }
                                //处理defect
                                String defectId = QtManagerConstant.QT_CHECK_TASK_FIELD_CHECK_DEFECT + key.substring(key.length() - 1);
                                if (item.getResult().get(defectId) != null) {
                                    qtCheckTaskDetail.setDefectId(Integer.valueOf(item.getResult().get(defectId)));
                                }
                                qtCheckTaskDetail.setTaskId(dto.getId());
                                qtCheckTaskDetail.setItemId(item.getItemId());
                                qtCheckTaskDetail.setGroupId(group.getId());
                                //在该list中的数据是不合格数据的值
                                if (CollUtil.isNotEmpty(item.getIsQualifiedList()) && item.getIsQualifiedList().contains(key)) {
                                    qtCheckTaskDetail.setIsQualified(0);
                                } else {
                                    qtCheckTaskDetail.setIsQualified(1);
                                }
                                valueModels.add(qtCheckTaskDetail);
                            }
                        }
                    });

                } else {
                    throw new BusinessException("检验项数据为空");
                }
            });
            qtCheckTaskDetailService.saveBatch(valueModels);
        }
        //插入在线检数据
        if (tasKDetail.getTaskType().equals(QtManagerConstant.QtCheckTaskTypeEnum.ONLINE.getCode()) && dto.getOnlineItem() != null) {
            List<QtCheckTaskDetail> model = Lists.newArrayList();
            dto.getOnlineItem().forEach(tmp -> {
                QtCheckTaskDetail detail = new QtCheckTaskDetail();
                detail.setTaskId(dto.getId());
                detail.setItemId(tmp.getItemId());
                detail.setGroupId(tmp.getGroupId());
                detail.setCopy(0);
                List<QtOnlineCheckDefectDTO> defectList = new ArrayList<>();
                if (CollUtil.isNotEmpty(tmp.getCategory())) {
                    tmp.getCategory().forEach(category -> {
                        category.getDefect().forEach(categoryTmp -> {
                            defectList.add(categoryTmp);
                        });
                    });
                }
                detail.setDefectNum(defectList);
                model.add(detail);
            });
            qtCheckTaskDetailService.saveBatch(model);
        }
        //pc可能会存任务信息
        QtCheckTask taskModel = new QtCheckTask();
        if (dto.getBadNum() != null) {
            taskModel.setBadNum(dto.getBadNum());
        }
        if (dto.getGoodNum() != null) {
            taskModel.setGoodNum(dto.getGoodNum());
        }
        if (dto.getCheckNum() != null) {
            taskModel.setCheckNum(dto.getCheckNum());
        }
        if (StringUtils.isNotBlank(dto.getNote())) {
            taskModel.setNote(dto.getNote());
        }
        if (StringUtils.isNotBlank(dto.getResult())) {
            taskModel.setResult(dto.getResult());
        }
        if (dto.getDefectNum() != null) {
            taskModel.setDefectNum(dto.getDefectNum());
        }
        taskModel.setId(dto.getId());
        taskModel.setStatus(dto.getStatus());
        if (dto.getStatus().equals(QtManagerConstant.QtCheckTaskStatusEnum.FINISHED.getCode())) {
            taskModel.setFinishedBy(SecurityUtils.getUser().getUserId());
            taskModel.setFinishedName(SecurityUtils.getUser().getName());
            taskModel.setFinishedTime(LocalDateTime.now());
        }
        return updateById(taskModel);
    }

    /**
     * 完成任务
     *
     * @param dto
     * @return
     */
    @Override
    public Boolean finished(QtCheckResultDTO dto) {
        QtCheckTask qtCheckTask = new QtCheckTask();
        BeanUtils.copyProperties(dto, qtCheckTask);
        qtCheckTask.setStatus(2);
        qtCheckTask.setFinishedBy(SecurityUtils.getUser().getUserId());
        qtCheckTask.setFinishedName(SecurityUtils.getUser().getName());
        qtCheckTask.setFinishedTime(LocalDateTime.now());
        return updateById(qtCheckTask);
    }

    /**
     * 复检任务
     *
     * @param dto
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long retask(QtReTaskDTO dto) {
        if (dto.getId() == null) {
            throw new BusinessException("任务信息不存在");
        }
        QtReTaskDTO qtReTaskDTO = new QtReTaskDTO();
        qtReTaskDTO.setOriginalId(dto.getId());
        //qtReTaskDTO.setCheckPlanId(dto.getCheckPlanId());
        Long taskId = 0L;
        QtAddCheckTaskDTO neworiginalId = new QtAddCheckTaskDTO();

        if (qtReTaskDTO != null) {
            BeanUtils.copyProperties(qtReTaskDTO, neworiginalId);
        }
        add(neworiginalId);
        return taskId;
    }

    @Override
    public List<QtCheckTaskTypeCountVO> countTaskByType() {
        List<QtCheckTaskTypeCountVO> vos = Lists.newArrayList();
        //检验方案编码:来料检1/首检2/在线检3/巡检4/出货检5/化验抽检6/配料抽检7
        for (int i = 1; i <= 7; i++) {
            QtCheckTaskTypeCountVO vo = new QtCheckTaskTypeCountVO();
            Long count = count(new LambdaQueryWrapper<QtCheckTask>().eq(QtCheckTask::getStatus, 0).eq(QtCheckTask::getTaskType, i));
            vo.setCount(count);
            vo.setTypeCode(i);
            vos.add(vo);
        }
        return vos;
    }

    @Override
    public Page<QtCheckTaskListVO> pageList(QtCheckTaskListDTO dto) {
        //如果是进行中则默认是暂存和进行中状态
        if (dto.getStatus() != null && dto.getStatus().equals(QtManagerConstant.QtCheckTaskStatusEnum.CHECKING.getCode())) {
            dto.setStatus(null);
            List<Integer> statusList = new ArrayList<>();
            statusList.add(QtManagerConstant.QtCheckTaskStatusEnum.CHECKING.getCode());
            statusList.add(QtManagerConstant.QtCheckTaskStatusEnum.TEMPORARY.getCode());
            dto.setStatusList(statusList);
        }
        //如果传了进行中,则包括暂存状态
        if (CollUtil.isNotEmpty(dto.getStatusList())) {
            if (dto.getStatusList().contains(QtManagerConstant.QtCheckTaskStatusEnum.CHECKING.getCode())) {
                dto.getStatusList().add(QtManagerConstant.QtCheckTaskStatusEnum.TEMPORARY.getCode());
            }
        }
        //1、配置分页信息
        MPJLambdaWrapper<QtCheckTask> condition = new MPJLambdaWrapper<QtCheckTask>()
                .selectAll(QtCheckTask.class)
                //关联表映射
                .selectAs(QtMaterial::getName, QtCheckTaskListVO::getMaterialName)
                .selectAs(QtRecheckTask::getOriginalCode, QtCheckTaskListVO::getOriginalTaskCode)
                //json映射
                .selectAs(QtCheckTask::getMaterialWorkshopName, QtCheckTaskListVO::getMaterialWorkshopName)
                .selectAs(QtCheckTask::getProductWorkshopName, QtCheckTaskListVO::getProductWorkshopName)
                .selectAs(QtCheckTask::getProductLineName, QtCheckTaskListVO::getProductLineName)
                .selectAs(QtCheckTask::getSupplierName, QtCheckTaskListVO::getSupplierName)
                .selectAs(QtCheckTask::getCustomerName, QtCheckTaskListVO::getCustomerName)
                //关联
                .leftJoin(QtMaterial.class, QtMaterial::getId, QtCheckTask::getMaterialId)
                .leftJoin(QtRecheckTask.class, QtRecheckTask::getRecheckId, QtCheckTask::getId)
                //查询
                //物料名称或者编码
                .and((dto.getMaterialNameOrCode() != null && !dto.getMaterialNameOrCode().equals("")), wrapper -> wrapper.like(QtMaterial::getName, dto.getMaterialNameOrCode()).or().like(QtMaterial::getCode, dto.getMaterialNameOrCode()))
                //状态
                .in(CollUtil.isNotEmpty(dto.getStatusList()), QtCheckTask::getStatus, dto.getStatusList())
                //状态
                .eq(dto.getStatus() != null, QtCheckTask::getStatus, dto.getStatus())
                //供应商
                .apply(dto.getSupplierId() != null, "JSON_CONTAINS(supplier, JSON_OBJECT('id', {0}))", dto.getSupplierId())
                //到货时间大于等于
                .between(dto.getArrivalTimeStart() != null && dto.getArrivalTimeEnd() != null, QtCheckTask::getArrivalTime, dto.getArrivalTimeStart(), dto.getArrivalTimeEnd())
                //物料使用车间
                .apply(dto.getMaterialWorkshopId() != null, "JSON_CONTAINS(material_workshop, JSON_OBJECT('id', {0}))", dto.getMaterialWorkshopId())
                //检验结果
                .eq(StringUtils.isNotBlank(dto.getResult()), QtCheckTask::getResult, dto.getResult())
                //生产订单号
                .like(StringUtils.isNotBlank(dto.getProductOrder()), QtCheckTask::getProductOrder, dto.getProductOrder())
                //生产车间
                .apply(dto.getProductWorkshopId() != null, "JSON_CONTAINS(product_workshop, JSON_OBJECT('id', {0}))", dto.getProductWorkshopId())
                //产线
                .apply(dto.getProductLineId() != null, "JSON_CONTAINS(product_line, JSON_OBJECT('id', {0}))", dto.getProductLineId())
                //生产批次
                .like(StringUtils.isNotBlank(dto.getProductBatch()), QtCheckTask::getProductBatch, dto.getProductBatch())
                //原始任务单号
                .like(StringUtils.isNotBlank(dto.getOriginalTaskCode()), QtRecheckTask::getOriginalCode, dto.getOriginalTaskCode())
                //发货日期
                .between(dto.getDeliverTimeStart() != null && dto.getArrivalTimeEnd() != null, QtCheckTask::getDeliverTime, dto.getDeliverTimeStart(), dto.getArrivalTimeEnd())
                //客户
                .apply(dto.getCustomerId() != null, "JSON_CONTAINS(customer, JSON_OBJECT('id', {0}))", dto.getCustomerId())
                //创建时间
                .between(dto.getCreateTimeStart() != null && dto.getCreateTimeEnd() != null, QtCheckTask::getCreateTime, dto.getCreateTimeStart(), dto.getCreateTimeEnd())
                //创建人
                .eq(dto.getCreateBy() != null, QtCheckTask::getCreateBy, dto.getCreateBy())
                //检验类型
                .eq(dto.getCheckTypeCode() != null, QtCheckTask::getTaskType, dto.getCheckTypeCode());
        //2.解析排序规则F
        if (QtManagerConstant.QT_CHECK_TASK_SORT_FIELD_CODE.equals(dto.getSortField())) {
            condition.orderBy(true, dto.getAscFlag(), QtCheckTask::getCode);
        } else if (QtManagerConstant.QT_CHECK_TASK_SORT_FIELD_STATUS.equals(dto.getSortField())) {
            if (dto.getAscFlag()) {
                condition.last("ORDER BY FIELD(t.status,0,1,4,2,3)");
            } else {
                condition.last("ORDER BY FIELD(t.status,3,2,4,1,0)");
            }
        } else if (QtManagerConstant.QT_CHECK_TASK_SORT_FIELD_MATERIAL_NAME.equals(dto.getSortField())) {
            if (dto.getAscFlag()) {
                condition.last("ORDER BY CONVERT(materialName using gbk) ASC");
            } else {
                condition.last("ORDER BY CONVERT(materialName using gbk) DESC");
            }
        } else if (QtManagerConstant.QT_CHECK_TASK_SORT_FIELD_ARRIVAL_TIME.equals(dto.getSortField())) {
            condition.orderBy(true, dto.getAscFlag(), QtCheckTask::getArrivalTime);
        } else if (QtManagerConstant.QT_CHECK_TASK_SORT_FIELD_SUPPLIER_NAME.equals(dto.getSortField())) {
            if (dto.getAscFlag()) {
                condition.last("ORDER BY CONVERT(t.supplier ->> '$.name' using gbk) ASC");
            } else {
                condition.last("ORDER BY CONVERT(t.supplier ->> '$.name' using gbk) DESC");
            }
        } else if (QtManagerConstant.QT_CHECK_TASK_SORT_FIELD_MATERIAL_WORKSHOP_NAME.equals(dto.getSortField())) {
            if (dto.getAscFlag()) {
                condition.last("ORDER BY CONVERT(t.material_workshop ->> '$.name' using gbk) ASC");
            } else {
                condition.last("ORDER BY CONVERT(t.material_workshop ->> '$.name' using gbk) DESC");
            }
        } else if (QtManagerConstant.QT_CHECK_TASK_SORT_FIELD_PRODUCT_ORDER.equals(dto.getSortField())) {
            condition.orderBy(true, dto.getAscFlag(), QtCheckTask::getProductOrder);
        } else if (QtManagerConstant.QT_CHECK_TASK_SORT_FIELD_PRODUCT_WORKSHOP_NAME.equals(dto.getSortField())) {
            if (dto.getAscFlag()) {
                condition.last("ORDER BY CONVERT(t.product_workshop ->> '$.name' using gbk) ASC");
            } else {
                condition.last("ORDER BY CONVERT(t.product_workshop ->> '$.name' using gbk) DESC");
            }
        } else if (QtManagerConstant.QT_CHECK_TASK_SORT_FIELD_PRODUCT_LINE_NAME.equals(dto.getSortField())) {
            if (dto.getAscFlag()) {
                condition.last("ORDER BY CONVERT(t.product_line ->> '$.name' using gbk) ASC");
            } else {
                condition.last("ORDER BY CONVERT(t.product_line ->> '$.name' using gbk) DESC");
            }
        } else if (QtManagerConstant.QT_CHECK_TASK_SORT_FIELD_PRODUCT_BATCH.equals(dto.getSortField())) {
            if (dto.getAscFlag()) {
                condition.last("ORDER BY CONVERT(t.product_batch using gbk) ASC");
            } else {
                condition.last("ORDER BY CONVERT(t.product_batch using gbk) DESC");
            }
        } else if (QtManagerConstant.QT_CHECK_TASK_SORT_FIELD_DEFECT_START_TIME.equals(dto.getSortField())) {
            condition.orderBy(true, dto.getAscFlag(), QtCheckTask::getDefectStartTime);
        } else if (QtManagerConstant.QT_CHECK_TASK_SORT_FIELD_DEFECT_ORIGINAL_CODE.equals(dto.getSortField())) {
            condition.orderBy(true, dto.getAscFlag(), QtRecheckTask::getOriginalCode);
        } else if (QtManagerConstant.QT_CHECK_TASK_SORT_FIELD_DEFECT_DELIVER_ORDER.equals(dto.getSortField())) {
            condition.orderBy(true, dto.getAscFlag(), QtCheckTask::getDeliverOrder);
        } else if (QtManagerConstant.QT_CHECK_TASK_SORT_FIELD_DEFECT_DELIVER_TIME.equals(dto.getSortField())) {
            condition.orderBy(true, dto.getAscFlag(), QtCheckTask::getDeliverTime);
        } else if (QtManagerConstant.QT_CHECK_TASK_SORT_FIELD_CUSTOMER_NAME.equals(dto.getSortField())) {
            if (dto.getAscFlag()) {
                condition.last("ORDER BY CONVERT(t.customer ->> '$.name' using gbk) ASC");
            } else {
                condition.last("ORDER BY CONVERT(t.customer ->> '$.name' using gbk) DESC");
            }
        } else if (QtManagerConstant.QT_CHECK_TASK_SORT_FIELD_DEFECT_DELIVER_NUM.equals(dto.getSortField())) {
            condition.orderBy(true, dto.getAscFlag(), QtCheckTask::getDeliverNum);
        } else if (QtManagerConstant.QT_CHECK_TASK_SORT_FIELD_DEFECT_RESULT.equals(dto.getSortField())) {
            condition.orderBy(true, dto.getAscFlag(), QtCheckTask::getResult);
        } else if (QtManagerConstant.QT_CHECK_TASK_SORT_FIELD_CREATE_NAME.equals(dto.getSortField())) {
            if (dto.getAscFlag()) {
                condition.last("ORDER BY CONVERT(t.create_name using gbk) ASC");
            } else {
                condition.last("ORDER BY CONVERT(t.create_name using gbk) DESC");
            }
        } else if (QtManagerConstant.QT_CHECK_TASK_SORT_FIELD_CREATE_TIME.equals(dto.getSortField())) {
            condition.orderBy(true, dto.getAscFlag(), QtCheckTask::getCreateTime);
        } else {
            //默认按照创建时间降序
            condition.orderByDesc(true, QtCheckTask::getCreateTime);
        }
        IPage<QtCheckTaskListVO> iPage = qtCheckTaskMapper.selectJoinPage(new Page<>(dto.getCurrent(), dto.getSize()), QtCheckTaskListVO.class, condition);
        //3、处理返回数据
        if (CollUtil.isNotEmpty(iPage.getRecords())) {
            for (QtCheckTaskListVO vo : iPage.getRecords()) {
                //若为暂存状态，则是检验中
                if (vo.getStatus() == 4) {
                    vo.setStatus(1);
                }
                if (vo.getMaterialWorkshopName() == null || ("null").equals(vo.getMaterialWorkshopName())) {
                    vo.setMaterialWorkshopName("");
                }
                QtCheckPlan planNameTmp = qtCheckPlanService.getById(vo.getCheckPlanId());
                if (planNameTmp != null) {
                    vo.setCheckPlanName(planNameTmp.getName());
                }
                log.debug("vo:" + vo);
            }
        }
        Page page = new Page<>(dto.getCurrent(), dto.getSize());
        BeanUtils.copyProperties(iPage, page);
        return page;
    }

//TODO

    /**
     * 根据生产订单号查询产线，批次,车间等信息
     *
     * @param code
     * @return
     */
    @Override
    public QtProduceOrderLineVO productOrderInfo(String code) {
        QtProduceOrderLineVO qtProduceOrderLineVO = new QtProduceOrderLineVO();
        R<PmsProduceOrderVO> pmsProduceOrderVO = remotePmsFeignApi.code(code);

        if (pmsProduceOrderVO.getData() != null) {
            SysOfficeParentDTO sysOfficeParentDTO = new SysOfficeParentDTO();
            sysOfficeParentDTO.setId(pmsProduceOrderVO.getData().getProductionLineId());
            sysOfficeParentDTO.setOfficeType(PmManagerConstant.STATE_9);
            //根据产线查询父级车间
            SysOfficeVO sysOfficeVO = FeignOptional.of(remoteOfficeFeign.parent(Integer.valueOf(PmManagerConstant.STATE_9))).get();
            qtProduceOrderLineVO.setWorkshopId(Integer.valueOf(PmManagerConstant.STATE_8));
            qtProduceOrderLineVO.setWorkshopName(sysOfficeVO.getOfficeName());
        }

        return qtProduceOrderLineVO;
    }

    /**
     * 根据产线查询生产订单号,批次等信息
     *
     * @param id
     * @return
     */
    @Override
    public QtProduceOrderLineVO selectProductOrder(Integer id) {

        QtProduceOrderLineVO qtProduceOrderLineVO = new QtProduceOrderLineVO();
        R<PmsProduceOrderVO> pmsProduceOrderVO = remotePmsFeignApi.listByProduceLineId(id);
        BeanUtils.copyProperties(pmsProduceOrderVO, qtProduceOrderLineVO);

        return qtProduceOrderLineVO;
    }

    @Override
    public Page<QtCheckProduceTaskVO> pageProductOrder(QtCheckaProductTaskListDTO dto) {
        if (dto.getProductOrder() == null) {
            throw new BusinessException("生产订单号不能为空");
        }
        List checkTypeCodeList = new ArrayList<>();
        //把检验类型为在线检,巡检,首检,化验抽检四种检验类型放入检查集合中
        checkTypeCodeList.add(QtManagerConstant.QtCheckTaskTypeEnum.ONLINE.getCode());
        checkTypeCodeList.add(QtManagerConstant.QtCheckTaskTypeEnum.CHEMICAL.getCode());
        checkTypeCodeList.add(QtManagerConstant.QtCheckTaskTypeEnum.FIRST.getCode());
        checkTypeCodeList.add(QtManagerConstant.QtCheckTaskTypeEnum.PATROL.getCode());
        MPJLambdaWrapper<QtCheckTask> productOrder = new MPJLambdaWrapper<QtCheckTask>()
                .selectAll(QtCheckTask.class)
                //关联复检表
                .leftJoin(QtRecheckTask.class, QtRecheckTask::getRecheckId, QtCheckTask::getId)
                .selectAs(QtRecheckTask::getOriginalCode, QtCheckTaskListVO::getOriginalTaskCode)
                .eq(QtCheckTask::getProductOrder, dto.getProductOrder())
                .in(QtCheckTask::getTaskType, checkTypeCodeList);
        if (QtManagerConstant.QT_CHECK_TASK_SORT_FIELD_CODE.equals(dto.getSortField())) {
            productOrder.orderBy(true, dto.getAscFlag(), QtCheckTask::getCode);
        } else if (QtManagerConstant.QT_CHECK_TASK_SORT_FIELD_STATUS.equals(dto.getSortField())) {
            if (dto.getAscFlag()) {
                productOrder.last("ORDER BY FIELD(t.status,0,1,4,2,3)");
            } else {
                productOrder.last("ORDER BY FIELD(t.status,3,2,4,1,0)");
            }
        } else if (QtManagerConstant.QT_CHECK_TASK_SORT_FIELD_MATERIAL_NAME.equals(dto.getSortField())) {
            if (dto.getAscFlag()) {
                productOrder.last("ORDER BY CONVERT(materialName using gbk) ASC");
            } else {
                productOrder.last("ORDER BY CONVERT(materialName using gbk) DESC");
            }
        } else if (QtManagerConstant.QT_CHECK_TASK_SORT_FIELD_ARRIVAL_TIME.equals(dto.getSortField())) {
            productOrder.orderBy(true, dto.getAscFlag(), QtCheckTask::getArrivalTime);
        } else if (QtManagerConstant.QT_CHECK_TASK_SORT_FIELD_SUPPLIER_NAME.equals(dto.getSortField())) {
            if (dto.getAscFlag()) {
                productOrder.last("ORDER BY CONVERT(t.supplier ->> '$.name' using gbk) ASC");
            } else {
                productOrder.last("ORDER BY CONVERT(t.supplier ->> '$.name' using gbk) DESC");
            }
        } else if (QtManagerConstant.QT_CHECK_TASK_SORT_FIELD_PRODUCT_ORDER.equals(dto.getSortField())) {
            productOrder.orderBy(true, dto.getAscFlag(), QtCheckTask::getProductOrder);
        } else if (QtManagerConstant.QT_CHECK_TASK_SORT_FIELD_PRODUCT_BATCH.equals(dto.getSortField())) {
            if (dto.getAscFlag()) {
                productOrder.last("ORDER BY CONVERT(t.product_batch using gbk) ASC");
            } else {
                productOrder.last("ORDER BY CONVERT(t.product_batch using gbk) DESC");
            }
        } else if (QtManagerConstant.QT_CHECK_TASK_SORT_FIELD_DEFECT_START_TIME.equals(dto.getSortField())) {
            productOrder.orderBy(true, dto.getAscFlag(), QtCheckTask::getDefectStartTime);
        } else if (QtManagerConstant.QT_CHECK_TASK_SORT_FIELD_DEFECT_ORIGINAL_CODE.equals(dto.getSortField())) {
            productOrder.orderBy(true, dto.getAscFlag(), QtRecheckTask::getOriginalCode);
        } else if (QtManagerConstant.QT_CHECK_TASK_SORT_FIELD_CREATE_TIME.equals(dto.getSortField())) {
            productOrder.orderBy(true, dto.getAscFlag(), QtCheckTask::getCreateTime);
        } else {
            //默认按照创建时间降序
            productOrder.orderByDesc(true, QtCheckTask::getCreateTime);
        }
        //配置分页对象
        Page page = new Page<>(dto.getCurrent(), dto.getSize());
        //查询分页数据
        IPage<QtCheckTask> resPage = page(page, productOrder);
        //拷贝page信息
        BeanUtils.copyProperties(resPage, page);
        //处理返回数据
        Page produceTaskpage = new Page<>(dto.getCurrent(), dto.getSize());
        List<QtCheckProduceTaskVO> resRecords = Lists.newArrayList();
        if (CollUtil.isNotEmpty(resPage.getRecords())) {
            for (QtCheckTask qtCheckTask : resPage.getRecords()) {
                QtCheckProduceTaskVO vo = new QtCheckProduceTaskVO();
                BeanUtils.copyProperties(qtCheckTask, vo);
                resRecords.add(vo);
            }
        }
        //设置数据
        produceTaskpage.setRecords(resRecords);
        return produceTaskpage;
    }

    /**
     * 深加工/基片巡检合格率趋势明细
     *
     * @param dto
     * @return
     */
    @Override
    public List<QtDeepOrSubAcceptabilityDetailVO> getQtDeepOrSubAcceptabilityDetailVO(QtSubOrDeepAcceptabilityDTO dto) {
        //先根据开始时间和结束时间查出这段时间内所有完成的符合条件的检验项
        LambdaQueryWrapper<QtCheckTask> condition = new QueryWrapper<QtCheckTask>().select("any_value(type) type , any_value(material_id) material_id , " +
                        "any_value(material_workshop) material_workshop , any_value(product_line) product_line , any_value(product_order) product_order , " +
                        "any_value(product_batch) product_batch , any_value(check_plan_id) check_plan_id , any_value(status) status , any_value(check_num) check_num , " +
                        "any_value(good_num) good_num , any_value(bad_num) bad_num , any_value(result) result , any_value(del_flag) del_flag , " +
                        "any_value(supplier) supplier , any_value(customer) customer , any_value(deliver_order) deliver_order , any_value(task_type) task_type ," +
                        "any_value(deliver_num) deliver_num , any_value(product_workshop) product_workshop , any_value(classes_id) classes_id").lambda()
                //车间
                .apply(Objects.nonNull(dto.getProductWorkShopId()), "JSON_CONTAINS(product_workshop, JSON_OBJECT('id', {0}))", dto.getProductWorkShopId())
                //产线
                .apply(Objects.nonNull(dto.getProductLineId()), "JSON_CONTAINS(product_line, JSON_OBJECT('id', {0}))", dto.getProductLineId())
                //物料
                .and(Objects.nonNull(dto.getMaterialId()), wrapper -> wrapper.eq(QtCheckTask::getMaterialId, dto.getMaterialId()))
                //客户
                .apply(Objects.nonNull(dto.getCustomer()), "JSON_CONTAINS(customer, JSON_OBJECT('id', {0}))", dto.getCustomer());


        //判断是深加工车间还是基片车间
        if (dto.getName().equals(QtManagerConstant.DEEP_OFFICE_NAME)) {
            condition.apply(Objects.nonNull(dto.getProductWorkShopId()), "JSON_CONTAINS(product_workshop, JSON_OBJECT('name', {0}))", QtManagerConstant.DEEP_OFFICE_NAME_ONE)
                    .or()
                    .apply(Objects.nonNull(dto.getProductWorkShopId()), "JSON_CONTAINS(product_workshop, JSON_OBJECT('name', {0}))", QtManagerConstant.DEEP_OFFICE_NAME_TWO)
                    .or()
                    .apply(Objects.nonNull(dto.getProductWorkShopId()), "JSON_CONTAINS(product_workshop, JSON_OBJECT('name', {0}))", QtManagerConstant.DEEP_OFFICE_NAME_THREE);
        }
        if (dto.getName().equals(QtManagerConstant.SUB_OFFICE_NAME)) {
            condition.apply(Objects.nonNull(dto.getProductWorkShopId()), "JSON_CONTAINS(product_workshop, JSON_OBJECT('name', {0}))", QtManagerConstant.SUB_OFFICE_NAME_ONE)
                    .or()
                    .apply(Objects.nonNull(dto.getProductWorkShopId()), "JSON_CONTAINS(product_workshop, JSON_OBJECT('name', {0}))", QtManagerConstant.SUB_OFFICE_NAME_TWO);
        }

        //判断是否筛选白班夜班
        if (dto.getWorkingOrder().equals(QtManagerConstant.QtClassesEnum.DAY.getCode())){
        condition.eq(QtCheckTask::getClassesId,QtManagerConstant.MORNINGWORK);
        }else if (dto.getWorkingOrder().equals(QtManagerConstant.NIGHTWORK)){
            condition.eq(QtCheckTask::getClassesId,QtManagerConstant.NIGHTWORK);
        }


        //根据完成时间筛选
        condition.ge(QtCheckTask::getFinishedTime, dto.getDateStart())
                .le(QtCheckTask::getFinishedTime, dto.getDateEnd())
                .groupBy(QtCheckTask::getClassesId);

        List<QtCheckTask> qtCheckTasks = list(condition);
        Assert.notEmpty(qtCheckTasks,"暂无数据");

        //构建返回数据集合
        List<QtDeepOrSubAcceptabilityDetailVO> detailVOList = Lists.newArrayList();
        qtCheckTasks.forEach(qtCheckTask -> {
            QtDeepOrSubAcceptabilityDetailVO vo = new QtDeepOrSubAcceptabilityDetailVO();
            BeanUtils.copyProperties(qtCheckTask, vo);
            BigDecimal checkNum = new BigDecimal(qtCheckTask.getCheckNum());
            BigDecimal goodNum = new BigDecimal(qtCheckTask.getGoodNum());
            vo.setAcceptability(BigDecimalUtil.divide(goodNum,checkNum,BigDecimalUtil.SCALE_2));
            vo.setFinishedTime(LocalDateUtil.format(qtCheckTask.getFinishedTime(), DatePattern.NORM_DATETIME_MINUTE_PATTERN));
            vo.setClassesDate(LocalDateUtil.format(qtCheckTask.getCreateTime(), DatePattern.NORM_DATE_PATTERN));
            detailVOList.add(vo);
        });

        return detailVOList;
    }

    /**
     * 深加工或基片合格率图表
     *
     * @param dto
     * @return
     */
    @Override
    public QtChartBarVO getQtChartDeepOrSubBarVO(QtSubOrDeepAcceptabilityDTO dto) {
        //先根据开始时间和结束时间查出这段时间内所有完成的符合条件的检验项
        LambdaQueryWrapper<QtCheckTask> condition = new QueryWrapper<QtCheckTask>().select("any_value(type) type , any_value(material_id) material_id , " +
                        "any_value(material_workshop) material_workshop , any_value(product_line) product_line , any_value(product_order) product_order , " +
                        "any_value(product_batch) product_batch , any_value(check_plan_id) check_plan_id , any_value(status) status , any_value(check_num) check_num , " +
                        "any_value(good_num) good_num , any_value(bad_num) bad_num , any_value(result) result , any_value(del_flag) del_flag , " +
                        "any_value(supplier) supplier , any_value(customer) customer , any_value(deliver_order) deliver_order , any_value(task_type) task_type ," +
                        "any_value(deliver_num) deliver_num , any_value(product_workshop) product_workshop , any_value(classes_id) classes_id").lambda()
                //车间
                .apply(Objects.nonNull(dto.getProductWorkShopId()), "JSON_CONTAINS(product_workshop, JSON_OBJECT('id', {0}))", dto.getProductWorkShopId())
                //产线
                .apply(Objects.nonNull(dto.getProductLineId()), "JSON_CONTAINS(product_line, JSON_OBJECT('id', {0}))", dto.getProductLineId())

                //物料
                .and(Objects.nonNull(dto.getMaterialId()), wrapper -> wrapper.eq(QtCheckTask::getMaterialId, dto.getMaterialId()))
                //客户
                .apply(Objects.nonNull(dto.getCustomer()), "JSON_CONTAINS(customer, JSON_OBJECT('id', {0}))", dto.getCustomer());

        //判断是深加工车间还是基片车间
        if (dto.getName().equals(QtManagerConstant.DEEP_OFFICE_NAME)) {
            condition.apply(Objects.nonNull(dto.getProductWorkShopId()), "JSON_CONTAINS(product_workshop, JSON_OBJECT('name', {0}))", QtManagerConstant.DEEP_OFFICE_NAME_ONE)
                    .or()
                    .apply(Objects.nonNull(dto.getProductWorkShopId()), "JSON_CONTAINS(product_workshop, JSON_OBJECT('name', {0}))", QtManagerConstant.DEEP_OFFICE_NAME_TWO)
                    .or()
                    .apply(Objects.nonNull(dto.getProductWorkShopId()), "JSON_CONTAINS(product_workshop, JSON_OBJECT('name', {0}))", QtManagerConstant.DEEP_OFFICE_NAME_THREE);
        }
        if (dto.getName().equals(QtManagerConstant.SUB_OFFICE_NAME)) {
            condition.apply(Objects.nonNull(dto.getProductWorkShopId()), "JSON_CONTAINS(product_workshop, JSON_OBJECT('name', {0}))", QtManagerConstant.SUB_OFFICE_NAME_ONE)
                    .or()
                    .apply(Objects.nonNull(dto.getProductWorkShopId()), "JSON_CONTAINS(product_workshop, JSON_OBJECT('name', {0}))", QtManagerConstant.SUB_OFFICE_NAME_TWO);
        }

        //创建返回数据的对象
        QtChartBarVO qtChartBarVO = new QtChartBarVO();
        LocalDateTime startTimeOfDayStr = LocalDateUtil.getStartTimeOfDayStr(dto.getDateStart());
        LocalDateTime endTimeOfDayStr = LocalDateUtil.getEndTimeOfDayStr(dto.getDateEnd());
        String start = LocalDateUtil.format(startTimeOfDayStr, "yyyy-MM-dd HH:mm:ss");
        String end = LocalDateUtil.format(endTimeOfDayStr, "yyyy-MM-dd HH:mm:ss");

        List<String> dateList = Lists.newArrayList();
        //加上初始时间
        String startDay = LocalDateUtil.localDateFormat(dto.getDateStart(), "yyyy-MM-dd");
        dateList.add(startDay);
        List<String> cutDate = ShardingTimeUtil.cutDate(QtManagerConstant.QtSPCGranularityEnum.DAY.getEname(), start, end);
        cutDate.stream().forEach(p -> {
            dateList.add(p);
        });

        //设置分片时间数据
        qtChartBarVO.setXAis(dateList);

        QtChartBarDecimalDataVO qtChartBarDecimalDataVO = new QtChartBarDecimalDataVO();
        //设置BigDecimal集合储存每天平均合格率
        List<BigDecimal> dayRate = Lists.newArrayList();

        //设置集合储存每天的白班的的合格率
        List<BigDecimal> morningData = Lists.newArrayList();

        //设置集合每天的夜班的合格率
        List<BigDecimal> nightData = Lists.newArrayList();

        //设置柱状图柱子
        QtChartBarSeriesVO qtChartBarSeriesMorningVO = new QtChartBarSeriesVO();
        QtChartBarSeriesVO qtChartBarSeriesNightVO = new QtChartBarSeriesVO();

        dateList.forEach(d -> {
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate date2 = LocalDate.parse(d, fmt);
            LocalDateTime startTimeOfDay = LocalDateUtil.getStartTimeOfDayStr(date2);
            LocalDateTime endTimeOfDay = LocalDateUtil.getEndTimeOfDayStr(date2);
            //去数据库中查询在这一天内的符合条件的完成的检验任务
            List<QtCheckTask> qtCheckTasks = this.list(condition
                    //筛选完成的
                    .eq(QtCheckTask::getStatus, QtManagerConstant.QtCheckTaskStatusEnum.FINISHED.getCode())
                    .ge(QtCheckTask::getFinishedTime, startTimeOfDay)
                    .le(QtCheckTask::getFinishedTime, endTimeOfDay).groupBy(QtCheckTask::getClassesId));
            //获取检验任务每天合格率的平均值
            int checksum = qtCheckTasks.stream().mapToInt(QtCheckTask::getCheckNum).sum();
            int goodsum = qtCheckTasks.stream().mapToInt(QtCheckTask::getGoodNum).sum();
            BigDecimal checkBig = BigDecimal.valueOf((int) checksum);
            BigDecimal goodBig = BigDecimal.valueOf((int) goodsum);
            BigDecimal result = BigDecimalUtil.divide(goodBig, checkBig);
            dayRate.add(result);

            //获取白班的合格率
            qtCheckTasks.stream().filter(f -> f.getClassesId()
                    .equals(QtManagerConstant.MORNINGWORK)).forEach(q -> {
                BigDecimal qCheckBig = BigDecimal.valueOf((int) q.getCheckNum());
                BigDecimal qGoodBig = BigDecimal.valueOf((int) q.getGoodNum());
                BigDecimal morningRate = BigDecimalUtil.divide(qGoodBig, qCheckBig);
                morningData.add(morningRate);
            });
            qtChartBarSeriesMorningVO.setName(QtManagerConstant.MORNINGWORKNAME);
            qtChartBarSeriesMorningVO.setData(morningData);

            //设置夜班合格率
            qtCheckTasks.stream().filter(f -> f.getClassesId()
                    .equals(QtManagerConstant.NIGHTWORK)).forEach(q -> {
                BigDecimal qCheckBig = BigDecimal.valueOf((int) q.getCheckNum());
                BigDecimal qGoodBig = BigDecimal.valueOf((int) q.getGoodNum());
                BigDecimal morningRate = BigDecimalUtil.divide(qGoodBig, qCheckBig);
                nightData.add(morningRate);
            });
            qtChartBarSeriesNightVO.setName(QtManagerConstant.NIGHTWORKNAME);
            qtChartBarSeriesNightVO.setData(nightData);

        });
        qtChartBarDecimalDataVO.setData(dayRate);
        List<QtChartBarSeriesVO> qtChartBarSeriesVOS = new ArrayList<>();
        if (Objects.isNull(dto.getWorkingOrder())) {
            Collections.addAll(qtChartBarSeriesVOS, qtChartBarSeriesMorningVO, qtChartBarSeriesNightVO);
        } else if (dto.getWorkingOrder().equals(QtManagerConstant.MORNINGWORK)) {
            qtChartBarSeriesVOS.add(qtChartBarSeriesMorningVO);
        } else if (dto.getWorkingOrder().equals(QtManagerConstant.NIGHTWORK)) {
            qtChartBarSeriesVOS.add(qtChartBarSeriesNightVO);
        }
        //设置柱状图数据
        qtChartBarVO.setSeries(qtChartBarSeriesVOS);
        //设置折线图数据
        qtChartBarVO.setMarkLine(qtChartBarDecimalDataVO);

        return qtChartBarVO;
    }

    @Override
    public List<QtCheckTask> conditionTaskList(QtCheckTaskConditionDTO dto) {
        List<Integer> classIds = Lists.newArrayList();
        List<Integer> badnums = Lists.newArrayList();
        QtChartBarSeriesVO<String> bar1 = new QtChartBarSeriesVO<>();
        List<String> bar1Data = Lists.newArrayList();
        bar1.setData(bar1Data);

        QtChartBarSeriesVO<String> bar2 = new QtChartBarSeriesVO<>();
        List<String> bar2Data = Lists.newArrayList();
        bar2.setData(bar2Data);
        if (dto.getType() == QtManagerConstant.QtChartType.HISTOGRAM.getCode()){
        if (dto.getWorkOrderId()!=null) {
            //查询时间范围为当天的8点到晚上八点
            LocalDateTime start = LocalDateTime.of(dto.getStartTime().toLocalDate(), LocalTime.of(8, 0, 0));
            LocalDateTime end = LocalDateTime.of(dto.getEndTime().toLocalDate(), LocalTime.of(20, 0, 0));
            List<QtCheckTask> qtCheckTasks = list(new LambdaQueryWrapper<QtCheckTask>().eq(QtCheckTask::getClassesId, dto.getWorkOrderId())
                    .ge(QtCheckTask::getFinishedTime, start)
                    .le(QtCheckTask::getFinishedTime, end)
                    .groupBy(QtCheckTask::getClassesId));
            for (QtCheckTask tem : qtCheckTasks) {
                Integer classesId = tem.getClassesId();
                Integer badNum = tem.getBadNum();
                classIds.add(classesId);
                badnums.add(badNum);


            }
            //柱状图
            if (dto.getWorkOrderId()==QtManagerConstant.QtChartType.HISTOGRAM.getCode()){
                bar1.setName(QtManagerConstant.QtChartType.HISTOGRAM.getName());
            }
            //饼状图
            else  {
                bar2.setName(QtManagerConstant.QtChartType.PIECHART.getName());
            }
            /*qtCheckTasks.stream().filter(v -> v.getClassesId().equals(dto.getWorkOrderId()));{

            }*/

        }


        }

        return null;
        //根据已知条件查询任务表
       /* LambdaQueryWrapper<QtCheckTask> taskCondition = new LambdaQueryWrapper<QtCheckTask>()
                //任务完成
                .eq(QtCheckTask::getStatus, QtManagerConstant.QtCheckTaskStatusEnum.FINISHED.getCode())
                //完成时间范围
                .between(Objects.nonNull(dto.getStartTime()) && Objects.nonNull(dto.getEndTime()), QtCheckTask::getFinishedTime, dto.getStartTime(), dto.getEndTime())
                //班次
                .eq(Objects.nonNull(dto.getWorkOrderId()), QtCheckTask::getClassesId, dto.getWorkOrderId())
                //只要巡检
                .eq(Objects.nonNull(dto.getTaskType()), QtCheckTask::getTaskType, dto.getTaskType())
                //车间
                .apply(Objects.nonNull(dto.getProductWorkShopId()), "JSON_CONTAINS(product_workshop, JSON_OBJECT('id', {0}))", dto.getProductWorkShopId())
                //产线
                .apply(Objects.nonNull(dto.getProductLineId()), "JSON_CONTAINS(product_line, JSON_OBJECT('id', {0}))", dto.getProductLineId())
                .orderByAsc(QtCheckTask::getFinishedTime);*/
        //return list(taskCondition);
    }

    @Override
    public QtChartDefectVO getPictureJPFB(QtChartParamDTO dto) {
        return null;
    }


    /**
     * 配料抽检合格率的明细
     *
     * @param dto
     * @return
     */
    @Override
    public List<QtAccessoryAcceptabilityDetailVO> getQtAccessoryAcceptabilityDetailVO(QtAccessoryAcceptabilityDTO dto) {
        //查询时间范围为当天的8点到晚上八点
        LocalDateTime start = LocalDateTime.of(dto.getDateTime(), LocalTime.of(8, 0, 0));
        LocalDateTime end = LocalDateTime.of(dto.getDateTime(), LocalTime.of(20, 0, 0));

        //根据条件查询
        LambdaQueryWrapper<QtCheckTask> condition = new LambdaQueryWrapper<QtCheckTask>()
                .eq(QtCheckTask::getTaskType, QtManagerConstant.QtCheckTaskTypeEnum.MIXED.getCode())
                .eq(QtCheckTask::getStatus,QtManagerConstant.QtCheckTaskStatusEnum.FINISHED.getCode())
                .ge(QtCheckTask::getFinishedTime, start)
                .le(QtCheckTask::getFinishedTime, end);
        List<QtCheckTask> qtCheckTasks = list(condition);

        //创建返回数据的类
      List<QtAccessoryAcceptabilityDetailVO> vos= Lists.newArrayList();
        qtCheckTasks.forEach(q->{
            QtAccessoryAcceptabilityDetailVO vo = new QtAccessoryAcceptabilityDetailVO();
            BeanUtils.copyProperties(q,vo);

            vo.setFinishedTime(LocalDateUtil.format(q.getFinishedTime(), "yyyy-MM-dd HH:mm"));
            vos.add(vo);
        });

        return vos;
    }

    private Long currentMaxCode() {
        //1.定义返回值
        Long currentMax = 0L;
        //2.获取系统最大code
        QtCheckTask maxCodeModel = getOne(new LambdaQueryWrapper<QtCheckTask>().orderByDesc(QtCheckTask::getCode).last(MpConstant.STR_LIMIT_1));
        //3.如果最大code不为空
        if (Objects.nonNull(maxCodeModel)) {
            try {
                //4.解析数值部分
                String substringVal = maxCodeModel.getCode().substring(CodeGenerateUtil.PREFIX_JYRW.length());
                //赋值
                long l = Long.parseLong(substringVal);
                currentMax = l;
            } catch (Exception e) {
                log.error(e.getLocalizedMessage());
            }
        }
        return currentMax;
    }


}




