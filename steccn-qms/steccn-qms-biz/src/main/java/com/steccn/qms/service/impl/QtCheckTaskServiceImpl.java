package com.steccn.qms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.alibaba.nacos.common.utils.CollectionUtils;
import com.alibaba.nacos.common.utils.StringUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.steccn.authserver.exception.BusinessException;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.authserver.util.SecurityUtils;
import com.steccn.common.constant.MpConstant;
import com.steccn.common.dto.*;
import com.steccn.common.util.BigDecimalUtil;
import com.steccn.common.util.LocalDateUtil;
import com.steccn.platform.util.LogicalPageUtil;
import com.steccn.common.util.StackGraphUtil;
import com.steccn.common.vo.R;
import com.steccn.platform.dto.SysOfficeDTO;
import com.steccn.platform.dto.SysOfficeParentDTO;
import com.steccn.platform.dto.SysOfficeTypeKeyDTO;
import com.steccn.platform.feign.RemoteOfficeFeign;
import com.steccn.platform.feign.RemoteSpareFeignApi;
import com.steccn.platform.vo.SysOfficeVO;
import com.steccn.pms.constant.PmManagerConstant;
import com.steccn.pms.feign.RemotePmsFeignApi;
import com.steccn.pms.vo.PmsProduceOrderVO;
import com.steccn.pms.vo.PmsWorkOrderBatchVO;
import com.steccn.qms.constant.QtManagerConstant;
import com.steccn.qms.domain.*;
import com.steccn.qms.dto.*;
import com.steccn.qms.mapper.*;
import com.steccn.qms.service.*;
import com.steccn.qms.util.CodeGenerateUtil;
import com.steccn.qms.util.ShardTimeUtil;
import com.steccn.qms.vo.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author AndrewLuo
 * @description 针对表【qt_check_task(巡检任务表)】的数据库操作Service实现
 * @createDate 2022-09-02 08:54:09
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class QtCheckTaskServiceImpl extends ServiceImpl<QtCheckTaskMapper, QtCheckTask>
        implements QtCheckTaskService {

    private final QtCheckTaskMapper qtCheckTaskMapper;

    private final RemoteOfficeFeign remoteOfficeFeign;

    private final QtMaterialService qtMaterialService;

    private final QtCheckPlanService qtCheckPlanService;

    private final QtCheckTaskDetailService qtCheckTaskDetailService;

    private final QtRecheckTaskService qtRecheckTaskService;

    private final QtCheckTaskCopyMapper qtCheckTaskCopyMapper;

    private final RemotePmsFeignApi remotePmsFeignApi;

    private final QtCheckTaskDestoryInfoService qtCheckTaskDestoryInfoService;

    private final QtCheckTaskCopyService qtCheckTaskCopyService;

    private final QtCheckTaskDestoryInfoMapper qtCheckTaskDestoryInfoMapper;
    
    private final RemoteSpareFeignApi remoteSpareFeignApi;

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
                    qtCheckTask.setClassesId(QtManagerConstant.QtClassesEnum.DAY.getCode());//早班
                } else {
                    qtCheckTask.setClassesId(QtManagerConstant.QtClassesEnum.NIGHT.getCode());//晚班
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
         *               -> 分组，缺陷项和检验项变化会造成方法版本更新
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
        //fix bug 检验方案为快照，无法创建任务
        if (MpConstant.NO.equals(qtCheckPlan.getShowFlag())) {
            throw new BusinessException("检验方案是快照，不可以用于创建任务");
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
            qtCheckTask.setClassesId(null);
            qtCheckTask.setClassTime(null);
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
     * @param isPc
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
                //放入落板记录模块数据setQtCheckTaskDestoryInfoVOList
                List<QtCheckTaskDestoryInfo> qtCheckTaskDestoryInfoList = qtCheckTaskDestoryInfoMapper.selectList(new LambdaQueryWrapper<QtCheckTaskDestoryInfo>().eq(QtCheckTaskDestoryInfo::getTaskId, id));
                List<QtCheckTaskDestoryInfoVO> qtCheckTaskDestoryInfoVOList = BeanUtil.copyToList(qtCheckTaskDestoryInfoList, QtCheckTaskDestoryInfoVO.class);
                qtCheckTaskDetailCopyVO.setQtCheckTaskDestoryInfo(qtCheckTaskDestoryInfoVOList);

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
                    if (qtCheckTask.getStation() != null) {
                        qtCheckTask.setStationId(qtCheckTask.getStationId());
                        qtCheckTask.setStationName(qtCheckTask.getStationName());
                        if(qtCheckTask.getStationId() != null){
                            qtCheckTaskListVo.setStationId(Math.toIntExact(qtCheckTask.getStationId()));
                        }
                        qtCheckTaskListVo.setStationName(qtCheckTask.getStation().getName());
                        qtCheckTaskListVo.setStation(qtCheckTask.getStation());
                    }
                    //设置样本概况模块数据
                    String[] strings = {"sampleNum","licensePlate","analyst","analystTime","analystName","ingredientNum","samplingTime"};
                    List<QtCheckTaskCopy> qtCheckTaskCopies = qtCheckTaskCopyService.list(new LambdaQueryWrapper<QtCheckTaskCopy>().eq(QtCheckTaskCopy::getTaskId, id));
                    List<QtCheckTaskSimpleDTO> qtCheckTaskSimpleDTOS = Lists.newArrayList();
                    if (!qtCheckTaskCopies.isEmpty()) {
                        QtCheckTaskSimpleDTO qtCheckTaskSimpleDTO = new QtCheckTaskSimpleDTO();
                        for (String s: strings) {
                            qtCheckTaskSimpleDTO = new QtCheckTaskSimpleDTO();
                            qtCheckTaskSimpleDTO.setId(s);
                            Map<String, Object> simpleMap = new HashMap<>();

                            for (QtCheckTaskCopy qtCheckTaskCopy : qtCheckTaskCopies) {
                                //qtCheckTaskSimpleDTO.setResult();
                                Map<String, Map<String, Object>> result = new HashMap<>();
                                if (QtManagerConstant.SAMPLENUM.equals(s)){
                                    simpleMap.put("checkResult" + qtCheckTaskCopy.getCopy(), qtCheckTaskCopy.getSampleNum());
                                } else if (QtManagerConstant.LICENSEPLATE.equals(s)) {
                                    simpleMap.put("checkResult" + qtCheckTaskCopy.getCopy(), qtCheckTaskCopy.getLicensePlate());
                                } /*else if ("analyst".equals(s)) {
                                    simpleMap.put("checkResult" + qtCheckTaskCopy.getCopy(), qtCheckTaskCopy.getAnalyst());
                                } */else if (QtManagerConstant.ANALYSTIME.equals(s)) {
                                    simpleMap.put("checkResult" + qtCheckTaskCopy.getCopy(), qtCheckTaskCopy.getAnalystTime());
                                } else if (QtManagerConstant.ANALYSTNAME.equals(s)) {
                                    simpleMap.put("checkResult" + qtCheckTaskCopy.getCopy(), qtCheckTaskCopy.getAnalystName());
                                } else if (QtManagerConstant.INGREDIENTNUM.equals(s)) {
                                    simpleMap.put("checkResult" + qtCheckTaskCopy.getCopy(), qtCheckTaskCopy.getIngredientNum());
                                } else if (QtManagerConstant.SAMPLINGTIME.equals(s)) {
                                    simpleMap.put("checkResult" + qtCheckTaskCopy.getCopy(), qtCheckTaskCopy.getSamplingTime());
                                }
                            }
                            qtCheckTaskSimpleDTO.setResult(simpleMap);
                            qtCheckTaskSimpleDTOS.add(qtCheckTaskSimpleDTO);

                        }
                        qtCheckTaskDetailVo.setSampleInfoList(qtCheckTaskSimpleDTOS);
                    }
                    //样本概况模块返回数据设置完成
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
        QtCheckTaskDetailCopyVO qtCheckTaskDetailCopyVO = new QtCheckTaskDetailCopyVO();
        QtCheckTaskDestoryInfoVO qtCheckTaskDestoryInfoVO = new QtCheckTaskDestoryInfoVO();
        List<QtCheckTaskDestoryInfoVO> qtCheckTaskDestoryInfoVOList = Lists.newArrayList();
        //放入落板记录模块数据setQtCheckTaskDestoryInfoVOList
        List<QtCheckTaskDestoryInfo> qtCheckTaskDestoryInfoList = qtCheckTaskDestoryInfoMapper.selectList(new LambdaQueryWrapper<QtCheckTaskDestoryInfo>().eq(QtCheckTaskDestoryInfo::getTaskId, id));
        for (QtCheckTaskDestoryInfo qtCheckTaskDestoryInfo : qtCheckTaskDestoryInfoList) {
            BeanUtils.copyProperties(qtCheckTaskDestoryInfo, qtCheckTaskDestoryInfoVO);
            qtCheckTaskDestoryInfoVOList.add(qtCheckTaskDestoryInfoVO);
        }
        qtCheckTaskDetailVo.setQtCheckTaskDestoryInfo(qtCheckTaskDestoryInfoVOList);
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
            //计算班次时间
            QtTaskClassCalculate taskClassCalculate = this.calculateDateTime(qtCheckTask.getFinishedTime());
            qtCheckTask.setClassesId(taskClassCalculate.getClassType());
            qtCheckTask.setClassTime(taskClassCalculate.getClassDate());
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
        //根据任务id删除落板记录表的暂存的数据
        qtCheckTaskDestoryInfoService.removeByMap(valueCondition);
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
        //组装落板记录详情数据
        if (CollUtil.isNotEmpty(dto.getQtCheckTaskDestoryInfo())) {
            QtCheckTaskDetail qtCheckTaskDetail = new QtCheckTaskDetail();
            List<QtCheckTaskDestoryInfo> qtCheckTaskDestoryInfoList = Lists.newArrayList();
            dto.getQtCheckTaskDestoryInfo().forEach(destoryInfoVO -> {
                if (dto.getId() != null) {
                    QtCheckTaskDestoryInfoDTO qtCheckTaskDestoryInfoDTO = new QtCheckTaskDestoryInfoDTO();
                    QtCheckTaskDestoryInfo qtCheckTaskDestoryInfo = new QtCheckTaskDestoryInfo();
                    if (qtCheckTaskDetailService.getById(dto.getId()) != null) {
                        qtCheckTaskDestoryInfoDTO.setCopy(qtCheckTaskDetailService.getById(dto.getId()).getCopy());
                    }
                    qtCheckTaskDestoryInfoDTO.setTaskId(dto.getId());
                    qtCheckTaskDestoryInfoDTO.setDestoryDetail(destoryInfoVO.getDestoryDetail());
                    qtCheckTaskDestoryInfoDTO.setDestoryReason(destoryInfoVO.getDestoryReason());
                    qtCheckTaskDestoryInfoDTO.setDelFlag(0);
                    qtCheckTaskDestoryInfoDTO.setDestoryInfoRemark(dto.getDestoryInfoRemark());
                    qtCheckTaskDestoryInfoDTO.setCreateBy(SecurityUtils.getUser().getUserId());
                    qtCheckTaskDestoryInfoDTO.setDestoryStartTime(destoryInfoVO.getDestoryStartTime());
                    qtCheckTaskDestoryInfoDTO.setDestoryEndTime(destoryInfoVO.getDestoryEndTime());
                    qtCheckTaskDestoryInfoDTO.setCreateName(SecurityUtils.getUser().getName());
                    if (qtCheckTaskCopyService.getById(dto.getId()) != null) {
                        qtCheckTaskDestoryInfoDTO.setSampleNum(qtCheckTaskCopyService.getById(dto.getId()).getSampleNum());
                    }
                    BeanUtils.copyProperties(qtCheckTaskDestoryInfoDTO, qtCheckTaskDestoryInfo);
                    qtCheckTaskDestoryInfoList.add(qtCheckTaskDestoryInfo);
                }
            });
            qtCheckTaskDestoryInfoService.saveBatch(qtCheckTaskDestoryInfoList);
        }
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
                List<QtCheckTaskCopy> qtCheckTaskCopies = Lists.newArrayList();
                deleteCondition.put("task_id", dto.getId());
                //先删除可能暂存的值
                qtCheckTaskCopyMapper.deleteByMap(deleteCondition);
                List<QtCheckTaskCopy> qtCheckTaskCopies2 = Lists.newArrayList();

                Map<String, QtCheckTaskCopy> objectMap = new HashMap<>();

                dto.getSampleInfoList().forEach(sampleInfo -> {
                    //遍历Map
                    for (Map.Entry<String, String> entry : sampleInfo.getResult().entrySet()) {

                        QtCheckTaskCopy qtCheckTaskCopy = new QtCheckTaskCopy();
                        if (Objects.nonNull(objectMap.get(entry.getKey()))) {
                            if (QtManagerConstant.SAMPLENUM.equals(sampleInfo.getId())) {
                                objectMap.get(entry.getKey()).setSampleNum(entry.getValue());
                            } else if (QtManagerConstant.LICENSEPLATE.equals(sampleInfo.getId())) {
                                objectMap.get(entry.getKey()).setLicensePlate(entry.getValue());
                            }else if (QtManagerConstant.ANALYST.equals(sampleInfo.getId())) {
                                if (StringUtils.isNotBlank(entry.getKey())) {
                                    objectMap.get(entry.getKey()).setAnalyst(Integer.valueOf(entry.getValue()));
                                }
                            }else if (QtManagerConstant.ANALYSTNAME.equals(sampleInfo.getId())) {
                                objectMap.get(entry.getKey()).setAnalystName(entry.getValue());
                            }else if (QtManagerConstant.SAMPLINGTIME.equals(sampleInfo.getId())) {
                                if (StringUtils.isNotBlank(entry.getKey())) {
                                    objectMap.get(entry.getKey()).setSamplingTime(LocalDateTime.parse(entry.getValue(),DatePattern.NORM_DATETIME_FORMATTER));
                                }
                            }else if (QtManagerConstant.ANALYSTIME.equals(sampleInfo.getId())) {
                                if (StringUtils.isNotBlank(entry.getKey())) {
                                    objectMap.get(entry.getKey()).setAnalystTime(LocalDateTime.parse(entry.getValue(),DatePattern.NORM_DATETIME_FORMATTER));
                                }
                            }else if (QtManagerConstant.INGREDIENTNUM.equals(sampleInfo.getId())) {
                                if (StringUtils.isNotBlank(entry.getKey())) {
                                    objectMap.get(entry.getKey()).setIngredientNum(Integer.valueOf(entry.getValue()));
                                }
                            }
                            objectMap.get(entry.getKey()).setLicensePlate(entry.getValue());
                        } else {
                            if (QtManagerConstant.SAMPLENUM.equals(sampleInfo.getId())) {
                                qtCheckTaskCopy.setSampleNum(entry.getValue());
                            }else if (QtManagerConstant.LICENSEPLATE.equals(sampleInfo.getId())) {
                                qtCheckTaskCopy.setLicensePlate(entry.getValue());
                            }else if (QtManagerConstant.ANALYST.equals(sampleInfo.getId())) {
                                if (entry.getKey() != null) {
                                    qtCheckTaskCopy.setAnalyst(Integer.valueOf(entry.getValue()));
                                }
                            }else if (QtManagerConstant.ANALYSTNAME.equals(sampleInfo.getId())) {
                                qtCheckTaskCopy.setAnalystName(entry.getValue());
                            }else if (QtManagerConstant.SAMPLINGTIME.equals(sampleInfo.getId())) {
                                if (StringUtils.isNotBlank(entry.getKey())) {
                                    qtCheckTaskCopy.setSamplingTime(LocalDateTime.parse(entry.getKey()));
                                }
                            }else if (QtManagerConstant.ANALYSTIME.equals(sampleInfo.getId())) {
                                if (StringUtils.isNotBlank(entry.getKey())) {
                                    qtCheckTaskCopy.setAnalystTime(LocalDateTime.parse(entry.getValue()));
                                }
                            }else if (QtManagerConstant.INGREDIENTNUM.equals(sampleInfo.getId())){
                                if (StringUtils.isNotBlank(entry.getKey())) {
                                    qtCheckTaskCopy.setIngredientNum(Integer.valueOf(entry.getKey()));
                                }
                            }
                            objectMap.put(entry.getKey(),qtCheckTaskCopy);
                        }
                    }
                });
                objectMap.forEach((key,qtCheckTaskCopyInfo)->{
                    QtCheckTaskCopy qtCheckTaskCopy1 = new QtCheckTaskCopy();
                    BeanUtils.copyProperties(qtCheckTaskCopyInfo,qtCheckTaskCopy1);
                    qtCheckTaskCopy1.setTaskId(dto.getId());
                    //不是在线检，设置copy值
                    if(key.length() != 0) {
                        Integer copy = Integer.valueOf(key.substring(key.length() - 1));
                        qtCheckTaskCopy1.setCopy(copy);
                    }
                    qtCheckTaskCopies2.add(qtCheckTaskCopy1);
                });
                qtCheckTaskCopyService.saveBatch(qtCheckTaskCopies2);
            }
        }
        //往样本概况表设置数据完成

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
        //组装落板记录详情数据
        if (CollUtil.isNotEmpty(dto.getQtCheckTaskDestoryInfo())) {
            List<QtCheckTaskDestoryInfo> qtCheckTaskDestoryInfoList = Lists.newArrayList();
            QtCheckTaskDetail qtCheckTaskDetail = new QtCheckTaskDetail();
            dto.getQtCheckTaskDestoryInfo().forEach(destoryInfoVO -> {
                QtCheckTaskDestoryInfoDTO qtCheckTaskDestoryInfoDTO = new QtCheckTaskDestoryInfoDTO();
                QtCheckTaskDestoryInfo qtCheckTaskDestoryInfo = new QtCheckTaskDestoryInfo();
                if (dto.getId() != null) {
                    qtCheckTaskDestoryInfoDTO.setCopy(qtCheckTaskDetailService.getById(dto.getId()).getCopy());
                    qtCheckTaskDestoryInfoDTO.setTaskId(dto.getId());
                    qtCheckTaskDestoryInfoDTO.setDestoryDetail(destoryInfoVO.getDestoryDetail());
                    qtCheckTaskDestoryInfoDTO.setDestoryReason(destoryInfoVO.getDestoryReason());
                    qtCheckTaskDestoryInfoDTO.setDelFlag(0);
                    qtCheckTaskDestoryInfoDTO.setDestoryInfoRemark(destoryInfoVO.getDestoryInfoRemark());
                    qtCheckTaskDestoryInfoDTO.setCreateBy(SecurityUtils.getUser().getUserId());
                    qtCheckTaskDestoryInfoDTO.setDestoryStartTime(destoryInfoVO.getDestoryStartTime());
                    qtCheckTaskDestoryInfoDTO.setDestoryEndTime(destoryInfoVO.getDestoryEndTime());
                    qtCheckTaskDestoryInfoDTO.setCreateName(SecurityUtils.getUser().getName());
                    if (qtCheckTaskCopyService.getById(dto.getId()) != null) {
                        qtCheckTaskDestoryInfoDTO.setSampleNum(qtCheckTaskCopyService.getById(dto.getId()).getSampleNum());
                    }

                }
                qtCheckTaskDestoryInfoDTO.setDestoryInfoRemark(dto.getDestoryInfoRemark());
                BeanUtils.copyProperties(qtCheckTaskDestoryInfoDTO, qtCheckTaskDestoryInfo);
                qtCheckTaskDestoryInfoList.add(qtCheckTaskDestoryInfo);
            });
            qtCheckTaskDestoryInfoService.saveBatch(qtCheckTaskDestoryInfoList);
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
            //计算班次时间
            QtTaskClassCalculate taskClassCalculate = this.calculateDateTime(taskModel.getFinishedTime());
            taskModel.setClassesId(taskClassCalculate.getClassType());
            taskModel.setClassTime(taskClassCalculate.getClassDate());
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
        //计算班次时间
        QtTaskClassCalculate taskClassCalculate = this.calculateDateTime(qtCheckTask.getFinishedTime());
        qtCheckTask.setClassesId(taskClassCalculate.getClassType());
        qtCheckTask.setClassTime(taskClassCalculate.getClassDate());
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
            //如果根据生产订单号能查询到当前订单信息，则根据订单表中的id查询批次信息
            R<PmsWorkOrderBatchVO> pmsWorkOrderBatchVO = remotePmsFeignApi.getByOrderId(pmsProduceOrderVO.getData().getId());
            String batchCode = "";
            if (pmsWorkOrderBatchVO.getData() != null){
                batchCode = pmsWorkOrderBatchVO.getData().getCode();
            }
            qtProduceOrderLineVO.setBatchNo(batchCode);
            sysOfficeParentDTO.setId(pmsProduceOrderVO.getData().getProductionLineId());
            sysOfficeParentDTO.setOfficeType(PmManagerConstant.STATE_9);
            Integer productionLineId = pmsProduceOrderVO.getData().getProductionLineId();

            BeanUtils.copyProperties(pmsProduceOrderVO.getData(),qtProduceOrderLineVO);
            if (productionLineId != null){
                //根据产线查询父级车间
                SysOfficeVO sysOfficeVO = FeignOptional.of(remoteOfficeFeign.parent(productionLineId)).get();
                qtProduceOrderLineVO.setWorkshopId(sysOfficeVO.getParentId());
                qtProduceOrderLineVO.setWorkshopName(sysOfficeVO.getOfficeName());
            }
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
        PmsProduceOrderVO pmsProduceOrderVO = remotePmsFeignApi.listByProduceLineId(id).getData();
        Assert.notNull(pmsProduceOrderVO,"该条产线下面没有正在执行中的订单");
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
     * 深加工/基片巡检合格率趋势明细分页表
     *
     * @param dto
     * @return
     */
    @Override
    public Page<QtDeepOrSubAcceptabilityDetailVO> getQtDeepOrSubAcceptabilityDetailVO(String keyword, QtSubOrDeepAcceptabilityDTO dto) {
        Page<QtDeepOrSubAcceptabilityDetailVO> page = new Page<>(dto.getCurrent(), dto.getSize());
        dto.setDateTimeStart(LocalDateTime.of(dto.getDateStart(), LocalTime.MIN));
        dto.setDateTimeEnd(LocalDateTime.of(dto.getDateEnd(), LocalTime.MAX));
        //根据条件获取数据
        List<QtCheckTaskReportVO> rateDataList = rateDataList(keyword, dto);
        rateDataList = rateDataList.stream().filter(v -> Objects.nonNull(v.getClassesId()) &&
                Objects.nonNull(v.getClassTime()) &&
                Objects.nonNull(v.getProductWorkshopId()) &&
                Objects.nonNull(v.getProductLineId()) &&
                Objects.nonNull(v.getMaterialId()) &&
                Objects.nonNull(v.getCustomerId())).collect(Collectors.toList());
        if (CollUtil.isEmpty(rateDataList)) {
            page.setTotal(0L);
            page.setRecords(Lists.newArrayList());
            return page;
        }
        //车间，产线，物料名称，客户
        Set<Integer> officeIdSet = Sets.newHashSet();
        Set<Long> materialIdSet = Sets.newHashSet();
        rateDataList.forEach(ele -> {
            officeIdSet.add(ele.getProductWorkshopId());
            officeIdSet.add(ele.getProductLineId());
            officeIdSet.add(ele.getCustomerId());
            materialIdSet.add(ele.getMaterialId());
        });
        Map<Integer, String> officeIdNameMap = FeignOptional.of(remoteOfficeFeign.listByIdCh(officeIdSet)).get();
        Map<Long, String> materialIdNameMap = FeignOptional.of(remoteSpareFeignApi.listByIdCh(materialIdSet)).get();

        Map<LocalDate, List<QtCheckTaskReportVO>> classDateMap = rateDataList.stream().collect(Collectors.groupingBy(v -> v.getClassTime().toLocalDate()));
        //定义返回的集合
        List<QtDeepOrSubAcceptabilityDetailVO> resList = Lists.newArrayList();

        for (Map.Entry<LocalDate, List<QtCheckTaskReportVO>> classDateEntry : classDateMap.entrySet()) {
            Map<Integer, List<QtCheckTaskReportVO>> classMap = classDateEntry.getValue().stream().collect(Collectors.groupingBy(QtCheckTaskReportVO::getClassesId));
            for (Map.Entry<Integer, List<QtCheckTaskReportVO>> classEntry : classMap.entrySet()) {
                Map<Integer, List<QtCheckTaskReportVO>> produceWorkshopMap = classEntry.getValue().stream().collect(Collectors.groupingBy(QtCheckTaskReportVO::getProductWorkshopId));
                for (Map.Entry<Integer, List<QtCheckTaskReportVO>> pwsEntry : produceWorkshopMap.entrySet()) {
                    Map<Integer, List<QtCheckTaskReportVO>> lineMap = pwsEntry.getValue().stream().collect(Collectors.groupingBy(QtCheckTaskReportVO::getProductLineId));
                    for (Map.Entry<Integer, List<QtCheckTaskReportVO>> lineEntry : lineMap.entrySet()) {
                        Map<Long, List<QtCheckTaskReportVO>> materialMap = lineEntry.getValue().stream().collect(Collectors.groupingBy(QtCheckTaskReportVO::getMaterialId));
                        for (Map.Entry<Long, List<QtCheckTaskReportVO>> materialEntry : materialMap.entrySet()) {
                            Map<Integer, List<QtCheckTaskReportVO>> customerMap = materialEntry.getValue().stream().collect(Collectors.groupingBy(QtCheckTaskReportVO::getCustomerId));
                            for (Map.Entry<Integer, List<QtCheckTaskReportVO>> customerEntry : customerMap.entrySet()) {
                                QtDeepOrSubAcceptabilityDetailVO resVO = new QtDeepOrSubAcceptabilityDetailVO();
                                resVO.setClassesDate(classDateEntry.getKey());
                                QtManagerConstant.QtClassesEnum classEnum = QtManagerConstant.QtClassesEnum.code(classEntry.getKey());
                                resVO.setClassName(Objects.nonNull(classEnum) ? classEnum.getName() : Strings.EMPTY);
                                resVO.setProductWorkshopName(officeIdNameMap.getOrDefault(pwsEntry.getKey(), Strings.EMPTY));
                                resVO.setProductLineName(officeIdNameMap.getOrDefault(lineEntry.getKey(), Strings.EMPTY));
                                resVO.setMaterialName(materialIdNameMap.getOrDefault(materialEntry.getKey(), Strings.EMPTY));
                                resVO.setCustomerName(officeIdNameMap.getOrDefault(customerEntry.getKey(), Strings.EMPTY));
                                List<QtSubOrDeepFPYTaskDetailsVO> taskDetails = Lists.newArrayList();
                                List<BigDecimal> rateList = Lists.newArrayList();
                                customerEntry.getValue().forEach(ele -> {
                                    QtSubOrDeepFPYTaskDetailsVO detail = new QtSubOrDeepFPYTaskDetailsVO();
                                    detail.setCode(ele.getCode());
                                    detail.setDetailAcceptability(BigDecimalUtil.divide(BigDecimal.valueOf(ele.getGoodNum()).multiply(BigDecimalUtil.HUNDRED), BigDecimal.valueOf(ele.getCheckNum())));
                                    detail.setFinishedTime(ele.getFinishedTime());
                                    detail.setCode(ele.getCode());
                                    detail.setResult(ele.getResult());
                                    detail.setTaskId(ele.getTaskId());
                                    rateList.add(detail.getDetailAcceptability());
                                    taskDetails.add(detail);
                                });
                                BigDecimal reduce = rateList.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
                                BigDecimal divide = BigDecimalUtil.divide(reduce, BigDecimal.valueOf(rateList.size()), 2);
                                resVO.setAcceptability(divide);
                                resVO.setTaskDetails(taskDetails);
                                resList.add(resVO);
                            }
                        }
                    }
                }
            }
        }
        //分页
        Page<QtDeepOrSubAcceptabilityDetailVO> page1 = LogicalPageUtil.page(resList, dto.getCurrent(), dto.getSize());
        return page1;
    }

    private LambdaQueryWrapper<QtCheckTask> getQtDeepOrSubAcceptabilityWrapper(QtSubOrDeepAcceptabilityDTO dto) {
        //先根据开始时间和结束时间查出这段时间内所有完成的符合条件的检验项
         LambdaQueryWrapper<QtCheckTask> condition = new QueryWrapper<QtCheckTask>().select("any_value(type) type , any_value(material_id) material_id , " +
                        "any_value(material_workshop) material_workshop , any_value(product_line) product_line , any_value(product_order) product_order , " +
                        "any_value(product_batch) product_batch , any_value(check_plan_id) check_plan_id , any_value(status) status , sum(check_num) check_num , " +
                        "sum(good_num) good_num , sum(bad_num) bad_num , any_value(result) result , any_value(del_flag) del_flag , " +
                        "any_value(supplier) supplier , any_value(customer) customer , any_value(deliver_order) deliver_order , any_value(task_type) task_type ," +
                        "any_value(deliver_num) deliver_num , any_value(product_workshop) product_workshop , any_value(classes_id) classes_id , any_value ( code ) code , " +
                         "finished_time").lambda()
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
        if (QtManagerConstant.QtClassesEnum.DAY.getCode().equals(dto.getWorkingOrder())) {
            condition.eq(QtCheckTask::getClassesId, QtManagerConstant.QtClassesEnum.DAY);
        } else if (QtManagerConstant.QtClassesEnum.NIGHT.equals(dto.getWorkingOrder())) {
            condition.eq(QtCheckTask::getClassesId, QtManagerConstant.QtClassesEnum.NIGHT);
        }


        //根据完成时间筛选
        List<SFunction<QtCheckTask,?>> functionList = new ArrayList<>();

        functionList.add(QtCheckTask::getFinishedTime);
        if (QtManagerConstant.QtChartCompareEnum.WORK.getCode()==dto.getCompare()){
            functionList.add(QtCheckTask::getClassesId);
        }else if (QtManagerConstant.QtChartCompareEnum.WORKSHOP.getCode()==dto.getCompare()){
            functionList.add(QtCheckTask::getProductWorkshopId);
        }else if (QtManagerConstant.QtChartCompareEnum.PRODUCTLINE.getCode()==dto.getCompare()){
            functionList.add(QtCheckTask::getProductLineId);
        }


        condition.ge(QtCheckTask::getFinishedTime, dto.getDateStart())
                .le(QtCheckTask::getFinishedTime, dto.getDateEnd())
                .groupBy(functionList);
        //2.解析排序规则
        if ((QtManagerConstant.QT_CHECK_TASK_SORT_FIELD_CLASS_ID.equals(dto.getSortField()))) {
            if (dto.getAscFlag()) {
                //使用last把sql用于排序的中文函数代码语句拼接到最后
                condition.last("ORDER BY CONVERT(class_id using gbk) ASC");
            } else {
                condition.last("ORDER BY CONVERT(class_id using gbk) DESC");
            }
        } else if (QtManagerConstant.QT_CHECK_TASK_SORT_FIELD_PRODUCT_WORKSHOP_NAME.equals(dto.getSortField())) {
            condition.orderBy(true, !dto.getAscFlag(), QtCheckTask::getProductWorkshopName);
        } else if (QtManagerConstant.QT_CHECK_TASK_SORT_FIELD_PRODUCT_LINE_NAME.equals(dto.getSortField())) {
            condition.orderBy(true, !dto.getAscFlag(), QtCheckTask::getProductLine);
        } else if (QtManagerConstant.QT_CHECK_TASK_SORT_FIELD_MATERIAL_NAME.equals(dto.getSortField())) {
            condition.orderBy(true, !dto.getAscFlag(), QtCheckTask::getMaterialId);
        } else if (QtManagerConstant.QT_CHECK_TASK_SORT_FIELD_CUSTOMER_NAME.equals(dto.getSortField())) {
            condition.orderBy(true, !dto.getAscFlag(), QtCheckTask::getCustomerName);
        } else {
            condition.orderBy(true, !dto.getAscFlag(), QtCheckTask::getFinishedTime);
        }
        return condition;
    }

    /**
     * 深加工或基片合格率图表
     *
     * @param dto
     * @return
     */
    @Override
    public QtLineChartBiBarDTO getQtChartDeepOrSubBarVO(String keyword, QtSubOrDeepAcceptabilityDTO dto) {
        dto.setDateTimeStart(LocalDateTime.of(dto.getDateStart(), LocalTime.MIN));
        dto.setDateTimeEnd(LocalDateTime.of(dto.getDateEnd(), LocalTime.MAX));
        List<QtCheckTaskReportVO> flatDataList  = rateDataList(keyword, dto);
        Assert.notEmpty(flatDataList, "数据为空，暂无报表");
        //定义数据集合
        List<BiStackGraphDTO<String, BigDecimal>> stackGraphDTOList = Lists.newArrayList();

        //将时间范围根据粒度分片
        List<LocalDateTime> cutDate = ShardTimeUtil.cutDateRange(QtManagerConstant.QtGranularityEnum.DAY.getType()
                , LocalDateTime.of(dto.getDateStart(), LocalTime.MIN)
                , LocalDateTime.of(dto.getDateEnd(), LocalTime.MAX));
        Assert.notEmpty(cutDate, "时间范围根据粒度分片为空");

        //考虑对比
        TupleDTO<Map<Integer, List<QtCheckTaskReportVO>>, Map<Integer, String>> mapMapTupleDTO = commonGroup(dto.getCompare(), flatDataList);
        //获取对比数据集和对比维度
        Map<Integer, List<QtCheckTaskReportVO>> contrastDataMap = mapMapTupleDTO.getField1();
        Map<Integer, String> idNameMap = mapMapTupleDTO.getField2();

        for (int i = 1; i < cutDate.size(); i++) {
            LocalDateTime start = cutDate.get(i - 1);
            LocalDateTime end = cutDate.get(i);
            //获取时间范围的集合
            List<QtCheckTaskReportVO> conditionDataList = flatDataList.stream().filter(v -> {
                //数据范围应该是大于等于开始时间  小于结束时间
                if ((start.isBefore(v.getFinishedTime()) || start.isEqual(v.getFinishedTime())) && end.isAfter(v.getFinishedTime())) {
                    return Boolean.TRUE;
                }
                return Boolean.FALSE;
            }).collect(Collectors.toList());
            BiStackGraphDTO<String, BigDecimal> biStackGraphDTO = new BiStackGraphDTO<>();
            biStackGraphDTO.setXAxis(LocalDateTimeUtil.format(start, DatePattern.NORM_DATE_PATTERN));
            biStackGraphDTO.setData(Lists.newArrayList());

            //默认集合
            List<QtCheckTaskReportVO> defaultQtCheckTaskList = Lists.newArrayList();
            //对比的结果
            for (Map.Entry<Integer, String> entry : idNameMap.entrySet()) {
                //获取对比数据
                List<QtCheckTaskReportVO> contrastData = contrastDataMap.getOrDefault(entry.getKey(), defaultQtCheckTaskList);
                Set<Long> conditionIdList = conditionDataList.stream().map(QtCheckTaskReportVO::getId).collect(Collectors.toSet());
                contrastData = contrastData.stream().filter(v -> conditionIdList.contains(v.getId())).collect(Collectors.toList());
                TupleDTO<String, BigDecimal> tupleDTO = getTupleDTO(contrastData, entry.getValue());
                biStackGraphDTO.getData().add(Lists.newArrayList(tupleDTO));
            }
            stackGraphDTOList.add(biStackGraphDTO);
        }
        //自定义转换工具
        QtLineChartBiBarDTO qtLineChartBiBarDTO = StackGraphUtil.biConvertData(stackGraphDTOList, Boolean.FALSE);
        return qtLineChartBiBarDTO;
    }

    /**
     * 拷贝数据
     *
     * @param sourceData 原始数据
     * @return
     */
    private List<QtCheckTaskReportVO> copyQtCheckTask(List<QtCheckTask> sourceData) {
        List<QtCheckTaskReportVO> resList = Lists.newArrayList();
        sourceData.forEach(ele -> {
            QtCheckTaskReportVO vo = new QtCheckTaskReportVO();
            BeanUtils.copyProperties(ele, vo);
            if(Objects.nonNull(vo.getProductLine())){
                vo.setProductLineId(ele.getProductLine().getId());
            }
            if(Objects.nonNull(ele.getProductWorkshop())){
                vo.setProductWorkshopId(ele.getProductWorkshop().getId());
            }
            if(Objects.nonNull(ele.getMaterialWorkshop())){
                vo.setMaterialWorkshopId(ele.getMaterialWorkshop().getId());
            }
            if(Objects.nonNull(ele.getSupplier())){
                vo.setSupplierId(ele.getSupplier().getId());
            }
            if(Objects.nonNull(ele.getCustomer())){
                vo.setCustomerId(ele.getCustomer().getId());
            }
            vo.setTaskId(ele.getId());
            resList.add(vo);
        });
        return resList;
    }

    /**
     * 公共分组
     *
     * @param compare      对比
     * @param flatDataList 数据集合
     */
    private TupleDTO<Map<Integer, List<QtCheckTaskReportVO>>, Map<Integer, String>> commonGroup(Integer compare, List<QtCheckTaskReportVO> flatDataList) {
        Map<Integer, List<QtCheckTaskReportVO>> targetMap;
        Map<Integer, String> idNameMap;
        if (QtManagerConstant.QtChartCompareEnum.WORKSHOP.getCode().equals(compare)) {
            targetMap = flatDataList.stream().filter(v -> Objects.nonNull(v.getProductWorkshopId())).collect(Collectors.groupingBy(QtCheckTaskReportVO::getProductWorkshopId));
            Set<Integer> workshopIdSet = flatDataList.stream().filter(v -> Objects.nonNull(v.getProductWorkshopId())).map(QtCheckTaskReportVO::getProductWorkshopId).collect(Collectors.toSet());
            idNameMap = FeignOptional.of(remoteOfficeFeign.listByIdCh(workshopIdSet)).get();
        } else if (QtManagerConstant.QtChartCompareEnum.PRODUCTLINE.getCode().equals(compare)) {
            targetMap = flatDataList.stream().filter(v -> Objects.nonNull(v.getProductLineId())).collect(Collectors.groupingBy(QtCheckTaskReportVO::getProductLineId));
            Set<Integer> lineIdSet = flatDataList.stream().filter(v -> Objects.nonNull(v.getProductLineId())).map(QtCheckTaskReportVO::getProductLineId).collect(Collectors.toSet());
            idNameMap = FeignOptional.of(remoteOfficeFeign.listByIdCh(lineIdSet)).get();
        } else if (QtManagerConstant.QtChartCompareEnum.MATERIALWORKSHOP.getCode().equals(compare)) {
            targetMap = flatDataList.stream().filter(v -> Objects.nonNull(v.getMaterialWorkshopId())).collect(Collectors.groupingBy(QtCheckTaskReportVO::getMaterialWorkshopId));
            Set<Integer> idSet = flatDataList.stream().filter(v -> Objects.nonNull(v.getMaterialWorkshopId())).map(QtCheckTaskReportVO::getMaterialWorkshopId).collect(Collectors.toSet());
            idNameMap = FeignOptional.of(remoteOfficeFeign.listByIdCh(idSet)).get();
        }else {
            targetMap = flatDataList.stream().collect(Collectors.groupingBy(QtCheckTaskReportVO::getClassesId));
            idNameMap = Maps.newHashMap();
            idNameMap.put(QtManagerConstant.QtClassesEnum.DAY.getCode(), QtManagerConstant.QtClassesEnum.DAY.getName());
            idNameMap.put(QtManagerConstant.QtClassesEnum.NIGHT.getCode(), QtManagerConstant.QtClassesEnum.NIGHT.getName());
        }
        return new TupleDTO<>(targetMap, idNameMap);
    }
    /**
     * 求和，返回二元组数据
     *
     * @param tDataList 原始数据
     * @param key       元素显示
     * @return
     */
    private TupleDTO<String, BigDecimal> getTupleDTO(List<QtCheckTaskReportVO> tDataList, String key) {
        int checkSum = tDataList.stream().filter(v -> Objects.nonNull(v.getCheckNum())).mapToInt(QtCheckTaskReportVO::getCheckNum).sum();
        int goodSum = tDataList.stream().filter(v -> Objects.nonNull(v.getGoodNum())).mapToInt(QtCheckTaskReportVO::getGoodNum).sum();
        BigDecimal divide = BigDecimalUtil.divide(BigDecimal.valueOf(goodSum), BigDecimal.valueOf(checkSum), BigDecimalUtil.SCALE_4);
        return new TupleDTO<>(key, BigDecimalUtil.multiply(divide,BigDecimal.valueOf(100)));
    }


    /**
     * 获取合格率基础数据集合
     * @param keyword 关键字，深加工||基片
     * @param dto dto
     * @return list
     */
    private List<QtCheckTaskReportVO> rateDataList(String keyword, QtSubOrDeepAcceptabilityDTO dto) {
        List<QtCheckTaskReportVO> resList = Lists.newArrayList();
        Set<Integer> produceLineSet = null;
        if(StrUtil.isNotBlank(keyword)){
            SysOfficeDTO sysOfficeDTO = new SysOfficeDTO();
            sysOfficeDTO.setOfficeName(keyword);
            sysOfficeDTO.setOfficeType(QtManagerConstant.OFFICE_PRODUCE_LINE_TYPE);
            List<SysOfficeVO> sysOfficeVOList = FeignOptional.of(remoteOfficeFeign.getByTypeAndLine(sysOfficeDTO)).get();
            Assert.notEmpty(sysOfficeVOList, "产线集合不能为空");
            produceLineSet = sysOfficeVOList.stream().map(SysOfficeVO::getId).collect(Collectors.toSet());
        }
        List<QtCheckTask> qtCheckTasks = this.baseMapper.rateDataList(produceLineSet, dto);
        if(CollUtil.isNotEmpty(qtCheckTasks)){
            resList = copyQtCheckTask(qtCheckTasks);
            if(QtManagerConstant.QtCheckTaskTypeEnum.INCOMING.getCode().equals(dto.getTaskType())){
                //获取时间
                Set<Long> taskIdSet = resList.stream().map(QtCheckTaskReportVO::getId).collect(Collectors.toSet());
                //任务id集合
                List<QtCheckTaskCopy> copyList = qtCheckTaskCopyService.list(new QueryWrapper<QtCheckTaskCopy>()
                        .select("task_id , min(sampling_time) as sampling_time")
                        .lambda()
                        .in(QtCheckTaskCopy::getTaskId, taskIdSet)
                        .groupBy(QtCheckTaskCopy::getTaskId));
                Map<Long,LocalDateTime> copyMap = Maps.newHashMap();
                if(CollUtil.isNotEmpty(copyList)){
                    for (QtCheckTaskCopy qtCheckTaskCopy : copyList) {
                        copyMap.put(qtCheckTaskCopy.getTaskId(),qtCheckTaskCopy.getSamplingTime());
                    }
                }
                resList.forEach(ele -> ele.setSamplingTime(copyMap.get(ele.getId())));
            }
        }
        return resList;
    }

    @Override
    public List<QtCheckTask> conditionTaskList(QtCheckTaskConditionDTO dto) {
        //根据已知条件查询任务表
        LambdaQueryWrapper<QtCheckTask> taskCondition = new LambdaQueryWrapper<QtCheckTask>()
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
                .orderByAsc(QtCheckTask::getFinishedTime);
        return list(taskCondition);
    }

    /**
     * 配料抽检合格率的明细分页
     *
     * @param dto
     * @return
     */
    @Override
    public Page<QtAccessoryAcceptabilityDetailVO> getQtAccessoryAcceptabilityDetailVO(QtAccessoryAcceptabilityDetailDTO dto) {
        //查询时间范围为当天的8点到晚上八点
        LocalDateTime start = LocalDateTime.of(dto.getDateTime(), LocalTime.of(8, 0, 0));
        LocalDateTime end = LocalDateTime.of(dto.getDateTime(), LocalTime.of(20, 0, 0));
//        LambdaQueryWrapper<QtCheckTask> condition = accessoryAcceptabilityQtCheckTasks(dto, start, end);


        //3.定义分页对象
        Page page = new Page<>(dto.getCurrent(), dto.getSize());

        if (CollectionUtils.isEmpty(dto.getWorkshopIdList())) {
            return page;
        }
        //4.查询分页数据
        IPage<QtCheckTask> resPage = page(page, new LambdaQueryWrapper<QtCheckTask>()
                .ge(QtCheckTask::getFinishedTime, start)
                .le(QtCheckTask::getFinishedTime, end)
                .eq(QtCheckTask::getTaskType,QtManagerConstant.QtCheckTaskTypeEnum.MIXED.getCode()));

        //5.拷贝page信息
        BeanUtils.copyProperties(resPage, page);
        if (CollectionUtils.isEmpty(resPage.getRecords())) {
            return page;
        }
        //6.拷贝返回的数据
        List<QtCheckTask> qtCheckTasks = BeanUtil.copyToList(resPage.getRecords(), QtCheckTask.class);
        qtCheckTasks = qtCheckTasks.stream().filter(qtCheckTask -> dto.getWorkshopIdList().contains(qtCheckTask.getMaterialWorkshop().getId())).collect(Collectors.toList());
        Set<Long> taskIds = qtCheckTasks.stream().map(QtCheckTask::getId).collect(Collectors.toSet());
        Map<Long,List<QtIQCAcceptabilityCopyVO>> taskInfoMap = qtCheckTaskDetailService.listByTaskId(taskIds);
        List<QtIQCAcceptabilityCopyVO> orCopyList =Lists.newArrayList();

        //创建返回数据的类
        List<QtAccessoryAcceptabilityDetailVO> vos = Lists.newArrayList();
        qtCheckTasks.forEach(q -> {
            QtAccessoryAcceptabilityDetailVO vo = new QtAccessoryAcceptabilityDetailVO();

            BigDecimal qCheckBig = BigDecimal.valueOf(Objects.isNull(q.getCheckNum())?0:q.getCheckNum());
            BigDecimal qGoodBig = BigDecimal.valueOf(Objects.isNull(q.getGoodNum())?0:q.getGoodNum());
            BigDecimal rate = BigDecimalUtil.divide(qGoodBig, qCheckBig,4);
            BeanUtils.copyProperties(q, vo);
//            List<QtCheckTaskCopy> qtCheckTaskCopies = qtCheckTaskCopyService.list(new LambdaQueryWrapper<QtCheckTaskCopy>()
//                    .eq(QtCheckTaskCopy::getTaskId, q.getId()));
//            List<QtAccessoryAcceptablityCopyVO> qtAccessoryAcceptablityCopyVOS = Lists.newArrayList();
//            qtCheckTaskCopies.forEach(qtCheckTaskCopy -> {
//                QtAccessoryAcceptablityCopyVO copyVO = new QtAccessoryAcceptablityCopyVO();
//                BeanUtils.copyProperties(qtCheckTaskCopy, copyVO);
//                qtAccessoryAcceptablityCopyVOS.add(copyVO);
//            });
            List<QtIQCAcceptabilityCopyVO> orDefault = taskInfoMap.getOrDefault(q.getId(), orCopyList);
            vo.setAccessoryDetail(orDefault);
            vo.setAcceptability(rate);
            vo.setFinishedTime(LocalDateUtil.format(q.getFinishedTime(), DatePattern.NORM_DATETIME_MINUTE_PATTERN));
            vos.add(vo);
        });
        page.setTotal(vos.size());
        return page.setRecords(vos);
    }

    /**
     * 配料抽检合格率柱状图
     *
     * @param dto
     * @return
     */
    @Override
    public QtChartBarVO getAccessoryChartDefectVO(QtAccessoryAcceptabilityDTO dto) {
        //查询时间范围为当天的8点到晚上八点
        LocalDateTime start = LocalDateTime.of(dto.getDateTime(), LocalTime.of(8, 0, 0));
        LocalDateTime end = LocalDateTime.of(dto.getDateTime(), LocalTime.of(20, 0, 0));

        //根据条件查询
        LambdaQueryWrapper<QtCheckTask> wrapper = accessoryAcceptabilityQtCheckTasks(dto, start, end);
        List<QtCheckTask> qtCheckTasks = list(wrapper);
        //创建返回数据对象
        QtChartBarVO qtChartBarVO = new QtChartBarVO();

        //遍历筛选出来的检验任务
        List<QtCheckTask> subOneCheckTasks = qtCheckTasks.stream().filter(s -> s.getProductWorkshopName().equals(QtManagerConstant.SUB_OFFICE_NAME_ONE))
                .collect(Collectors.toList());
        List<QtCheckTask> subTwoCheckTasks = qtCheckTasks.stream().filter(s -> s.getProductWorkshopName().equals(QtManagerConstant.SUB_OFFICE_NAME_TWO))
                .collect(Collectors.toList());

        //把集合容量大的作为x坐标
        List<String> count = Lists.newArrayList();
        List<QtChartBarSeriesVO> qtChartBarSeriesVOS = Lists.newArrayList();
        if (subOneCheckTasks.size() >= subTwoCheckTasks.size()) {
            for (int i = 1; i <= subOneCheckTasks.size(); i++) {
                List<BigDecimal> resutOne = Lists.newArrayList();
                QtChartBarSeriesVO qtOneChartBarSeriesVO = new QtChartBarSeriesVO();
                QtChartBarSeriesVO qtTwoChartBarSeriesVO = new QtChartBarSeriesVO();
                qtOneChartBarSeriesVO.setName(QtManagerConstant.SUB_OFFICE_NAME_ONE);
                QtCheckTask qtCheckTaskOne = subOneCheckTasks.get(i - 1);
                BigDecimal bigDecimalCheck1 = new BigDecimal(qtCheckTaskOne.getGoodNum());
                BigDecimal bigDecimalCheck2 = new BigDecimal(qtCheckTaskOne.getCheckNum());
                resutOne.add(BigDecimalUtil.divide(bigDecimalCheck1, bigDecimalCheck2, BigDecimalUtil.SCALE_2));
                qtOneChartBarSeriesVO.setData(resutOne);

                List<BigDecimal> resutTwo = Lists.newArrayList();
                if (i <= subTwoCheckTasks.size()) {
                    QtCheckTask qtCheckTaskTwo = subTwoCheckTasks.get(i - 1);
                    BigDecimal bigDecimal3 = new BigDecimal(qtCheckTaskTwo.getGoodNum());
                    BigDecimal bigDecimal4 = new BigDecimal(qtCheckTaskTwo.getCheckNum());
                    resutTwo.add(BigDecimalUtil.divide(bigDecimal3, bigDecimal4, BigDecimalUtil.SCALE_2));

                    qtTwoChartBarSeriesVO.setName(QtManagerConstant.SUB_OFFICE_NAME_TWO);
                    qtTwoChartBarSeriesVO.setData(resutTwo);
                }
                Collections.addAll(qtChartBarSeriesVOS, qtOneChartBarSeriesVO, qtTwoChartBarSeriesVO);
                count.add(i + "");
            }
        } else {
            for (int i = 1; i <= subTwoCheckTasks.size(); i++) {
                List<BigDecimal> resutOne = Lists.newArrayList();
                QtChartBarSeriesVO qtOneChartBarSeriesVO = new QtChartBarSeriesVO();
                QtChartBarSeriesVO qtTwoChartBarSeriesVO = new QtChartBarSeriesVO();
                if (i <= subOneCheckTasks.size()) {
                    qtOneChartBarSeriesVO.setName(QtManagerConstant.SUB_OFFICE_NAME_ONE);
                    QtCheckTask qtCheckTaskOne = subOneCheckTasks.get(i - 1);
                    BigDecimal bigDecimalCheck1 = new BigDecimal(qtCheckTaskOne.getGoodNum());
                    BigDecimal bigDecimalCheck2 = new BigDecimal(qtCheckTaskOne.getCheckNum());
                    resutOne.add(BigDecimalUtil.divide(bigDecimalCheck1, bigDecimalCheck2, BigDecimalUtil.SCALE_2));
                    qtOneChartBarSeriesVO.setData(resutOne);
                }
                List<BigDecimal> resutTwo = Lists.newArrayList();
                QtCheckTask qtCheckTaskTwo = subTwoCheckTasks.get(i - 1);
                BigDecimal bigDecimal3 = new BigDecimal(qtCheckTaskTwo.getGoodNum());
                BigDecimal bigDecimal4 = new BigDecimal(qtCheckTaskTwo.getCheckNum());
                resutTwo.add(BigDecimalUtil.divide(bigDecimal3, bigDecimal4, BigDecimalUtil.SCALE_2));
                qtTwoChartBarSeriesVO.setName(QtManagerConstant.SUB_OFFICE_NAME_TWO);
                qtTwoChartBarSeriesVO.setData(resutTwo);

                Collections.addAll(qtChartBarSeriesVOS, qtOneChartBarSeriesVO, qtTwoChartBarSeriesVO);
                count.add(i + "");
            }
        }
        qtChartBarVO.setXAis(count);
        qtChartBarVO.setSeries(qtChartBarSeriesVOS);
        return qtChartBarVO;
    }

    private LambdaQueryWrapper<QtCheckTask> accessoryAcceptabilityQtCheckTasks(QtAccessoryAcceptabilityDTO dto, LocalDateTime start, LocalDateTime end) {
        List<String> type = Lists.newArrayList();
        type.add(QtManagerConstant.OFFICE_WORKSHOP_TYPE);
        SysOfficeTypeKeyDTO sysOfficeTypeKeyDTO = new SysOfficeTypeKeyDTO();
        sysOfficeTypeKeyDTO.setKey(QtManagerConstant.SUB_OFFICE_NAME);
        sysOfficeTypeKeyDTO.setType(type);
        List<SysOfficeVO> sysOfficeVOS = FeignOptional.of(remoteOfficeFeign.getListByTypeAndKey(sysOfficeTypeKeyDTO)).get();
        List<String> officeNames = sysOfficeVOS.stream().map(SysOfficeVO::getOfficeName).collect(Collectors.toList());


        //根据条件查询
        LambdaQueryWrapper<QtCheckTask> condition = new QueryWrapper<QtCheckTask>().select("any_value(code) code , any_value(result) result , " +
                        "any_value(finished_time) finished_time , any_value(product_workshop) product_workshop , " +
                        "any_value(material_workshop) material_workshop").lambda()
                .in(QtCheckTask::getMaterialWorkshopName, officeNames)
                .eq(QtCheckTask::getTaskType, QtManagerConstant.QtCheckTaskTypeEnum.MIXED.getCode())
                .eq(QtCheckTask::getStatus, QtManagerConstant.QtCheckTaskStatusEnum.FINISHED.getCode())
                .ge(QtCheckTask::getFinishedTime, start)
                .le(QtCheckTask::getFinishedTime, end);
        //如果选了物料车间id名称就加上物料车间条件
        if (Objects.nonNull(dto.getMaterialWorkshopId())) {
            condition.apply("JSON_CONTAINS(material_workshop, JSON_OBJECT('id', {0}))", dto.getMaterialWorkshopId());
        }
        condition.groupBy(QtCheckTask::getMaterialWorkshop)
                .orderByAsc(QtCheckTask::getFinishedTime);
        //获取符合条件的任务集合
        return condition;
    }


    /**
     * 基片化验抽检合格率趋势明细分页
     *
     * @param dto
     * @return
     */
    @Override
    public Page<QtCheckTaskReportVO> getQtSubLaboratoryAcceptabilityDetailVO(QtSubOrDeepAcceptabilityDTO dto) {
        dto.setDateTimeStart(LocalDateTime.of(dto.getDateStart(), LocalTime.MIN));
        dto.setDateTimeEnd(LocalDateTime.of(dto.getDateEnd(), LocalTime.MAX));
        List<QtCheckTaskReportVO> flatDataList  = rateDataList("基片", dto);
        Assert.notEmpty(flatDataList, "数据为空，暂无报表");
        //筛选车间
        if(dto.getProductLineId()!=null){
            flatDataList = flatDataList.stream().filter(s->s.getProductLineId().equals(dto.getProductLineId())).collect(Collectors.toList());
        }
        //筛选filters
        if(dto.getFilters()!=null){
            flatDataList = flatDataList.stream().filter(s->dto.getFilters().contains(s.getProductLineName())).collect(Collectors.toList());
        }
        //计算每条任务的合格率
        flatDataList.forEach(tmp->{
            BigDecimal result = BigDecimalUtil.divide(new BigDecimal(tmp.getGoodNum()),new BigDecimal(tmp.getCheckNum()));
            tmp.setPassRate(new DecimalFormat("0.00%").format(result));
        });

        //根据检验完成时间升序排列
        flatDataList = flatDataList.stream().sorted(Comparator.comparing(QtCheckTaskReportVO::getFinishedTime)).collect(Collectors.toList());

        Set<Long> taskIds = flatDataList.stream().map(QtCheckTaskReportVO::getId).collect(Collectors.toSet());
        Map<Long,List<QtIQCAcceptabilityCopyVO>> taskInfoMap = qtCheckTaskDetailService.listByTaskId(taskIds);

        //分页
//        int pageSize = dto.getSize();
//        int current = dto.getCurrent() - 1;
//        flatDataList = flatDataList.stream().skip(current * pageSize)
//                .limit(dto.getSize())
//                .collect(Collectors.toList());
//        Page<QtCheckTaskReportVO> page = new Page<>(dto.getCurrent(), dto.getSize());
//        page.setRecords(flatDataList);
//        page.setTotal(flatDataList.size());
        Page<QtCheckTaskReportVO> page = LogicalPageUtil.page(flatDataList, dto.getCurrent(), dto.getSize());
        page.getRecords().forEach(flatData ->{
            flatData.setList(taskInfoMap.get(flatData.getId()));
        });
        return page;
    }

    @Override
    public Page<QtIQCAcceptabilityDetailVO> getQtIQCAcceptabilityDetailVO(QtSubOrDeepAcceptabilityDTO dto) {
        //1.处理时间数据
        if (Objects.nonNull(dto.getDateStart())) {
            dto.setDateTimeStart(LocalDateTime.of(dto.getDateStart(), LocalTime.of(8, 0, 0)));
        }
        if (Objects.nonNull(dto.getDateEnd())) {
            dto.setDateTimeStart(LocalDateTime.of(dto.getDateEnd().plusDays(1), LocalTime.of(8, 0, 0)));
        }
        if (Objects.nonNull(dto.getSamplingTime())) {
            dto.setSamplingStartTime(LocalDateTime.of(dto.getSamplingTime(), LocalTime.MIN));
            dto.setSamplingEndTime(LocalDateTime.of(dto.getSamplingTime(), LocalTime.MAX));
        }
        //2.自定义sql查询数据
        Page<QtIQCAcceptabilityDetailVO> resPage = this.baseMapper.pageList(new Page<>(dto.getCurrent(), dto.getSize()), dto);
        //3.处理数据
        Set<Long> taskIds = Sets.newHashSet();
        Set<Long> materialIds = Sets.newHashSet();
        List<QtIQCAcceptabilityDetailVO> resList = resPage.getRecords();
        resList.forEach(task -> {
                    //处理时间
//            task.setFinishedTime(LocalDate.from(task.getFinishedTime()));
//            task.setSamplingTime(LocalDateTime.from(task.get));
                    taskIds.add(task.getId());
                    materialIds.add(task.getMaterialId());
                    //计算合格
                    BigDecimal checkNum = new BigDecimal(task.getCheckNum());
                    BigDecimal goodNum = new BigDecimal(task.getGoodNum());
                    task.setAcceptability(BigDecimalUtil.divide(goodNum, checkNum, BigDecimalUtil.SCALE_2));
                }
        );
        List<QtIQCAcceptabilityDetailVO> voList = Lists.newArrayList();
        if (CollUtil.isNotEmpty(resList)) {
            List<QtIQCAcceptabilityDetailVO> filterList = resList;
            //根据对比类型过滤数据
            if (Objects.nonNull(dto.getCompare())) {
                filterList = resList.stream().filter(predicate(dto.getCompare(), dto.getCompareContent(), resList)).collect(Collectors.toList());
            }
            //获取物料数据
            Map<Long, String> materialMap = FeignOptional.of(remoteSpareFeignApi.listByIdCh(materialIds)).get();
            //获取详情数据
            Map<Long,List<QtIQCAcceptabilityCopyVO>> taskInfoMap = qtCheckTaskDetailService.listByTaskId(taskIds);
            List<QtIQCAcceptabilityCopyVO> orCopyList =Lists.newArrayList();
            //处理详情数据
            filterList.forEach(task -> {
                List<QtIQCAcceptabilityCopyVO> orDefault = taskInfoMap.getOrDefault(task.getId(), orCopyList);
                task.setDetail(orDefault);
                task.setMaterialName(materialMap.get(task.getMaterialId()));
            });
            //根据规则排序
            voList = filterList.stream().sorted(Comparator.comparing(QtIQCAcceptabilityDetailVO::getMaterialName,Comparator.nullsLast(String::compareTo))
                    .thenComparing(QtIQCAcceptabilityDetailVO::getFinishedTime,Comparator.nullsLast(LocalDate::compareTo))
                    .thenComparing(QtIQCAcceptabilityDetailVO::getSamplingTime,Comparator.nullsLast(LocalDate::compareTo))
                    .thenComparing(Comparator.nullsLast(Comparator.comparing(x -> x.getMaterialWorkshop().getName())))
                    .thenComparing(Comparator.nullsLast(Comparator.comparing(x -> x.getSupplier().getName()))))
                    .collect(Collectors.toList());
        }
        return resPage.setRecords(voList);
    }

//    /**
//     * 来料检合格率明细分页
//     *
//     * @param dto
//     * @return
//     */
//    @Override
//    public Page<QtIQCAcceptabilityDetailVO> getQtIQCAcceptabilityDetailVO(QtIQCAcceptabilityDTO dto) {
//        MPJLambdaWrapper<QtCheckTask> condition = getQtIQCCondition(dto);
//        //3.定义分页对象
//        Page page = new Page<>(dto.getCurrent(), dto.getSize());
//        //4.查询分页数据
//        IPage<QtCheckTask> resPage = page(page, condition);
//        //5.拷贝page信息
//        BeanUtils.copyProperties(resPage, page);
//        //6.拷贝返回的数据
//        List<QtIQCAcceptabilityDetailVO> qtIQCAcceptabilityDetailVOS = Lists.newArrayList();
//        List<QtCheckTask> qtCheckTasks = BeanUtil.copyToList(resPage.getRecords(), QtCheckTask.class);
//
//        qtCheckTasks.forEach(q -> {
//
//            QtIQCAcceptabilityDetailVO qtIQCAcceptabilityDetailVO = new QtIQCAcceptabilityDetailVO();
//            List<QtIQCAcceptabilityCopyVO> qtIQCAcceptabilityCopyVOS = Lists.newArrayList();
//            List<QtCheckTaskCopy> checkTaskCopies = qtCheckTaskCopyService.list(new LambdaQueryWrapper<QtCheckTaskCopy>().eq(QtCheckTaskCopy::getTaskId, q.getId()));
//            checkTaskCopies.forEach(checkTaskCopy -> {
//                QtIQCAcceptabilityCopyVO qtIQCAcceptabilityCopyVO = new QtIQCAcceptabilityCopyVO();
//                BeanUtils.copyProperties(checkTaskCopy, qtIQCAcceptabilityCopyVO);
//                qtIQCAcceptabilityCopyVOS.add(qtIQCAcceptabilityCopyVO);
//            });
//            qtIQCAcceptabilityDetailVO.setDetail(qtIQCAcceptabilityCopyVOS);
//            BeanUtils.copyProperties(q, qtIQCAcceptabilityDetailVO);
//            BigDecimal checkNum = new BigDecimal(q.getCheckNum());
//            BigDecimal goodNum = new BigDecimal(q.getGoodNum());
//            BigDecimal result = BigDecimalUtil.divide(goodNum, checkNum, BigDecimalUtil.SCALE_2);
//            qtIQCAcceptabilityDetailVO.setAcceptability(result);
//            qtIQCAcceptabilityDetailVOS.add(qtIQCAcceptabilityDetailVO);
//        });
//        page.setRecords(qtIQCAcceptabilityDetailVOS);
//        page.setTotal(qtIQCAcceptabilityDetailVOS.size());
//        return page;
//    }

//        MPJLambdaWrapper<QtCheckTask> condition = getQtIQCCondition(dto);
//        //3.定义分页对象
//        Page page = new Page<>(dto.getCurrent(), dto.getSize());
//        //4.查询分页数据
//        IPage<QtCheckTask> resPage = page(page, condition);
//        //5.拷贝page信息
//        BeanUtils.copyProperties(resPage, page);
//        //6.拷贝返回的数据
//        List<QtIQCAcceptabilityDetailVO> qtIQCAcceptabilityDetailVOS = Lists.newArrayList();
//        List<QtCheckTask> qtCheckTasks = BeanUtil.copyToList(resPage.getRecords(), QtCheckTask.class);
//        qtCheckTasks.forEach(q -> {
//            QtIQCAcceptabilityDetailVO qtIQCAcceptabilityDetailVO = new QtIQCAcceptabilityDetailVO();
//            List<QtIQCAcceptabilityCopyVO> qtIQCAcceptabilityCopyVOS = Lists.newArrayList();
//            List<QtCheckTaskCopy> checkTaskCopies = qtCheckTaskCopyService.list(new LambdaQueryWrapper<QtCheckTaskCopy>().eq(QtCheckTaskCopy::getTaskId, q.getId()));
//            checkTaskCopies.forEach(checkTaskCopy -> {
//                QtIQCAcceptabilityCopyVO qtIQCAcceptabilityCopyVO = new QtIQCAcceptabilityCopyVO();
//                BeanUtils.copyProperties(checkTaskCopy, qtIQCAcceptabilityCopyVO);
//                qtIQCAcceptabilityCopyVOS.add(qtIQCAcceptabilityCopyVO);
//            });
//            qtIQCAcceptabilityDetailVO.setDetail(qtIQCAcceptabilityCopyVOS);
//            BeanUtils.copyProperties(q, qtIQCAcceptabilityDetailVO);
//            BigDecimal checkNum = new BigDecimal(q.getCheckNum());
//            BigDecimal goodNum = new BigDecimal(q.getGoodNum());
//            BigDecimal result = BigDecimalUtil.divide(goodNum, checkNum, BigDecimalUtil.SCALE_2);
//            qtIQCAcceptabilityDetailVO.setAcceptability(result);
//            qtIQCAcceptabilityDetailVOS.add(qtIQCAcceptabilityDetailVO);
//        });
//        page.setRecords(qtIQCAcceptabilityDetailVOS);
//        return page;
//    }

//    private MPJLambdaWrapper<QtCheckTask> getQtIQCCondition(QtSubOrDeepAcceptabilityDTO dto) {
//        //1、配置分页信息
//        MPJLambdaWrapper<QtCheckTask> condition = new MPJLambdaWrapper<QtCheckTask>()
//                .selectAll(QtCheckTask.class)
//                //关联表映射
//                .selectAs(QtMaterial::getName, QtCheckTaskListVO::getMaterialName)
//                .selectAs(QtCheckTaskCopy::getTaskId, QtCheckTaskListVO::getId)
//                //json映射
//                .selectAs(QtCheckTask::getMaterialWorkshopName, QtCheckTaskListVO::getMaterialWorkshopName)
//                .selectAs(QtCheckTask::getProductWorkshopName, QtCheckTaskListVO::getProductWorkshopName)
//                .selectAs(QtCheckTask::getProductLineName, QtCheckTaskListVO::getProductLineName)
//                .selectAs(QtCheckTask::getSupplierName, QtCheckTaskListVO::getSupplierName)
//                .selectAs(QtCheckTask::getCustomerName, QtCheckTaskListVO::getCustomerName)
//                //关联
//                .leftJoin(QtCheckTaskCopy.class, QtCheckTaskCopy::getTaskId, QtCheckTask::getId)
//                .leftJoin(QtMaterial.class, QtMaterial::getId, QtCheckTask::getMaterialId)
//                //查询
//                .and(Objects.nonNull(dto.getSamplingTime()), wrapper -> wrapper.eq(QtCheckTaskCopy::getSamplingTime, dto.getSamplingTime()))
//                .and(Objects.nonNull(dto.getMaterialId()), wrapper -> wrapper.eq(QtCheckTask::getMaterialId, dto.getMaterialId()))
//                .and(Objects.nonNull(dto.getMaterialWorkshopId()), wrapper -> wrapper.eq(QtCheckTask::getMaterialWorkshopId, dto.getMaterialWorkshopId()))
//                .and(Objects.nonNull(dto.getSupplierId()), wrapper -> wrapper.eq(QtCheckTask::getSupplierId, dto.getSupplierId()));
//
//        if (QtManagerConstant.QtChartCompareEnum.MATERIALWORKSHOP.getCode().equals(dto.getCompare())){
//            condition.groupBy(QtCheckTask::getMaterialId);
//        }else
//        if (QtManagerConstant.QtChartCompareEnum.SAMPLINGTIME.getCode().equals(dto.getCompare())){
//            condition.groupBy(QtCheckTaskCopy::getSamplingTime);
//        }
//        condition.groupBy(QtCheckTask::getSupplierId)
//                .gt(QtCheckTask::getFinishedTime,LocalDateTime.of(dto.getDateTime(),LocalTime.of(8,0,0)))
//                .le(QtCheckTask::getFinishedTime,LocalDateTime.of(dto.getDateTime().plusDays(1),LocalTime.of(8,0,0)));
//       //2.解析排序规则
//        if (QtManagerConstant.QT_CHECK_TASK_SORT_FIELD_MATERIAL_NAME.equals(dto.getSortField())) {
//            if (dto.getAscFlag()) {
//                condition.last("ORDER BY CONVERT(name using gbk) ASC");
//            } else {
//                condition.last("ORDER BY CONVERT(name using gbk) DESC");
//            }
//        } else if (QtManagerConstant.QT_CHECK_TASK_SORT_FIELD_FINISHED_TIME.equals(dto.getSortField())) {
//            condition.orderBy(true, !dto.getAscFlag(), QtCheckTask::getFinishedTime);
//        } else if (QtManagerConstant.QT_CHECK_TASK_COPY__SORT_FIELD.equals(dto.getSortField())) {
//            condition.orderBy(true, dto.getAscFlag(), QtCheckTaskCopy::getSamplingTime);
//        } else if (QtManagerConstant.QT_CHECK_TASK_SORT_FIELD_MATERIAL_WORKSHOP_NAME.equals(dto.getSortField())) {
//            condition.orderBy(true, dto.getAscFlag(), QtCheckTask::getMaterialWorkshopName);
//        } else {
//            //默认按创建时间降序排列
//            condition.orderBy(true, false, QtCheckTask::getSupplierName);
//        }
//        return condition;
//    }

    /**
     * 来料检合格率柱状图表
     *
     * @param dto
     * @return
     */
    @Override
    public QtLineChartBiBarDTO accessoryChart(QtSubOrDeepAcceptabilityDTO dto) {
        dto.setDateTimeStart(LocalDateTime.of(dto.getDateStart(), LocalTime.MIN));
        dto.setDateTimeEnd(LocalDateTime.of(dto.getDateEnd(), LocalTime.MAX));
        List<QtCheckTaskReportVO> flatDataList  = rateDataList(null, dto);
        Assert.notEmpty(flatDataList, "数据为空，暂无报表");

        //定义数据集合
        List<BiStackGraphDTO<String, BigDecimal>> stackGraphDTOList = Lists.newArrayList();

        flatDataList = flatDataList.stream().sorted(Comparator.comparing(QtCheckTaskReportVO::getFinishedTime).reversed()).collect(Collectors.toList());
        LinkedHashMap<LocalDateTime, List<QtCheckTaskReportVO>> linkedMap = flatDataList.stream().collect(Collectors.groupingBy(QtCheckTaskReportVO::getFinishedTime, LinkedHashMap::new, Collectors.toList()));

        //考虑对比
        Map<Integer, List<QtCheckTaskReportVO>> contrastDataMap = Maps.newHashMap();
        Map<Integer, String> idNameMap = Maps.newHashMap();
        if(Objects.nonNull(dto.getCompare())){
             TupleDTO<Map<Integer, List<QtCheckTaskReportVO>>, Map<Integer, String>> mapMapTupleDTO = commonGroup(dto.getCompare(), flatDataList);
             //获取对比数据集和对比维度
             contrastDataMap = mapMapTupleDTO.getField1();
             idNameMap = mapMapTupleDTO.getField2();
             if(CollUtil.isEmpty(idNameMap)) return null;
        }
        //次数
        int idx = 1;
        for (Map.Entry<LocalDateTime, List<QtCheckTaskReportVO>> entry : linkedMap.entrySet()) {
            //获取时间范围的集合
            List<QtCheckTaskReportVO> conditionDataList = flatDataList.stream().filter(v -> {
                //数据范围应该是大于等于开始时间  小于结束时间
                if (entry.getKey().isEqual(v.getFinishedTime())) {
                    return Boolean.TRUE;
                }
                return Boolean.FALSE;
            }).collect(Collectors.toList());

            BiStackGraphDTO<String, BigDecimal> biStackGraphDTO = new BiStackGraphDTO<>();
            biStackGraphDTO.setXAxis("第" + idx + "抽检");
            biStackGraphDTO.setData(Lists.newArrayList());

            //默认集合
            List<QtCheckTaskReportVO> defaultQtCheckTaskList = Lists.newArrayList();
            //对比的结果
            if(Objects.nonNull(dto.getCompare())){
                for (Map.Entry<Integer, String> entry1 : idNameMap.entrySet()) {
                    //获取对比数据
                    List<QtCheckTaskReportVO> contrastData = contrastDataMap.getOrDefault(entry1.getKey(), defaultQtCheckTaskList);
                    Set<Long> conditionIdList = conditionDataList.stream().map(QtCheckTaskReportVO::getId).collect(Collectors.toSet());
                    contrastData = contrastData.stream().filter(v -> conditionIdList.contains(v.getId())).collect(Collectors.toList());
                    TupleDTO<String, BigDecimal> tupleDTO = getTupleDTO(contrastData, entry1.getValue());
                    biStackGraphDTO.getData().add(Lists.newArrayList(tupleDTO));
                }
            }else{
                TupleDTO<String, BigDecimal> tupleDTO = getTupleDTO(entry.getValue(), "第" + idx + "抽检");
                biStackGraphDTO.getData().add(Lists.newArrayList(tupleDTO));
            }
            stackGraphDTOList.add(biStackGraphDTO);
            idx++;
        }
        //自定义转换工具
        QtLineChartBiBarDTO qtLineChartBiBarDTO = StackGraphUtil.biConvertData(stackGraphDTOList, Boolean.FALSE);
        return qtLineChartBiBarDTO;
    }

    @Override
    public QtLineChartBiBarDTO getQtIQCAcceptabilityChart(QtSubOrDeepAcceptabilityDTO dto) {
        dto.setDateTimeStart(LocalDateTime.of(dto.getDateStart(), LocalTime.MIN));
        dto.setDateTimeEnd(LocalDateTime.of(dto.getDateEnd(), LocalTime.MAX));
        List<QtCheckTaskReportVO> flatDataList = rateDataList(null, dto);
        Assert.notEmpty(flatDataList, "数据为空，暂无报表");

        //定义数据集合
        List<BiStackGraphDTO<String, BigDecimal>> stackGraphDTOList = Lists.newArrayList();

        //获取物料id
        Set<Long> materialIdSet = flatDataList.stream().map(QtCheckTaskReportVO::getMaterialId).collect(Collectors.toSet());
        Map<Long, String> materialIdNameMap = FeignOptional.of(remoteSpareFeignApi.listByIdCh(materialIdSet)).get();

        flatDataList = flatDataList.stream().sorted(Comparator.comparing(QtCheckTaskReportVO::getFinishedTime).reversed()).collect(Collectors.toList());
        LinkedHashMap<Long, List<QtCheckTaskReportVO>> linkedMap = flatDataList.stream().collect(Collectors.groupingBy(QtCheckTaskReportVO::getMaterialId, LinkedHashMap::new, Collectors.toList()));

        //考虑对比
        Map<String, List<QtCheckTaskReportVO>> contrastDataMap = Maps.newHashMap();
        Map<String, String> idNameMap = Maps.newHashMap();
        if(Objects.nonNull(dto.getCompare())){
            TupleDTO<Map<String, List<QtCheckTaskReportVO>>, Map<String, String>> mapMapTupleDTO = rateGroup(dto.getCompare(), flatDataList);
            //获取对比数据集和对比维度
            contrastDataMap = mapMapTupleDTO.getField1();
            idNameMap = mapMapTupleDTO.getField2();
            if(CollUtil.isEmpty(idNameMap)) return null;
        }
        //根据物料id去分组
        for (Map.Entry<Long, List<QtCheckTaskReportVO>> entry : linkedMap.entrySet()) {
            BiStackGraphDTO<String, BigDecimal> biStackGraphDTO = new BiStackGraphDTO<>();
            biStackGraphDTO.setXAxis(materialIdNameMap.getOrDefault(entry.getKey(), Strings.EMPTY));
            biStackGraphDTO.setData(Lists.newArrayList());
            //默认集合
            List<QtCheckTaskReportVO> defaultQtCheckTaskList = Lists.newArrayList();
            //对比的结果
            if(Objects.nonNull(dto.getCompare())){
                for (Map.Entry<String, String> entry1 : idNameMap.entrySet()) {
                    //获取对比数据
                    List<QtCheckTaskReportVO> contrastData = contrastDataMap.getOrDefault(entry1.getKey(), defaultQtCheckTaskList);
                    Set<Long> conditionIdList = entry.getValue().stream().map(QtCheckTaskReportVO::getId).collect(Collectors.toSet());
                    contrastData = contrastData.stream().filter(v -> conditionIdList.contains(v.getId())).collect(Collectors.toList());
                    TupleDTO<String, BigDecimal> tupleDTO = getTupleDTO(contrastData, entry1.getValue());
                    biStackGraphDTO.getData().add(Lists.newArrayList(tupleDTO));
                }
            }else{
                TupleDTO<String, BigDecimal> tupleDTO = getTupleDTO(entry.getValue(), materialIdNameMap.getOrDefault(entry.getKey(), Strings.EMPTY));
                biStackGraphDTO.getData().add(Lists.newArrayList(tupleDTO));
            }
            stackGraphDTOList.add(biStackGraphDTO);
        }
        //自定义转换工具
        QtLineChartBiBarDTO qtLineChartBiBarDTO = StackGraphUtil.biConvertData(stackGraphDTOList, Boolean.FALSE);
        return qtLineChartBiBarDTO;
    }

    /**
     * 合格率分组
     *
     * @param compare      对比
     * @param flatDataList 数据集合
     */
    private TupleDTO<Map<String, List<QtCheckTaskReportVO>>, Map<String, String>> rateGroup(Integer compare, List<QtCheckTaskReportVO> flatDataList) {
        Map<String, List<QtCheckTaskReportVO>> targetMap;
        Map<String, String> idNameMap = Maps.newHashMap();
        if (QtManagerConstant.QtChartCompareEnum.SAMPLINGTIME.getCode().equals(compare)) {
            targetMap = flatDataList.stream().filter(v -> Objects.nonNull(v.getSamplingTime())).collect(Collectors.groupingBy(x -> String.valueOf(x.getSamplingTime().toLocalDate())));
        } else if (QtManagerConstant.QtChartCompareEnum.MATERIALWORKSHOP.getCode().equals(compare)) {
            targetMap = flatDataList.stream().filter(v -> Objects.nonNull(v.getMaterialWorkshopId())).collect(Collectors.groupingBy(v -> String.valueOf(v.getMaterialWorkshopId())));
        } else {
            targetMap = flatDataList.stream().filter(v -> Objects.nonNull(v.getSupplierId())).collect(Collectors.groupingBy(v -> String.valueOf(v.getSupplierId())));
        }
        for (Map.Entry<String, List<QtCheckTaskReportVO>> e : targetMap.entrySet()) {
            idNameMap.put(e.getKey(),e.getKey());
        }
        return new TupleDTO<>(targetMap, idNameMap);
    }


    /**
     * 基片化验抽检合格率趋势查询条件
     *
     * @param dto
     * @return
     */
    private LambdaQueryWrapper<QtCheckTask> getQtCheckTaskSubLaboratoryCondition(QtSubLaboratoryAcceptabilityDTO dto) {
        //定义一个接口,查询所有的符合条件的车间和产线
        //①先调用下拉选项接口,查询所有的机构
        //②根据关键字过滤想要的机构
        List<String> type = Lists.newArrayList();
        type.add(QtManagerConstant.OFFICE_WORKSHOP_TYPE);
        SysOfficeTypeKeyDTO sysOfficeTypeKeyDTO = new SysOfficeTypeKeyDTO();
        sysOfficeTypeKeyDTO.setKey(QtManagerConstant.SUB_OFFICE_NAME);
        sysOfficeTypeKeyDTO.setType(type);
        R<List<SysOfficeVO>> listByTypeAndKey = remoteOfficeFeign.getListByTypeAndKey(sysOfficeTypeKeyDTO);
        List<String> officeNames = listByTypeAndKey.getData().stream().map(SysOfficeVO::getOfficeName).collect(Collectors.toList());

        //根据条件查询
        LambdaQueryWrapper<QtCheckTask> condition = new QueryWrapper<QtCheckTask>().select("any_value(type) type , any_value(material_id) material_id , " +
                        "any_value(material_workshop) material_workshop , any_value(product_line) product_line , any_value(product_order) product_order , " +
                        "any_value(product_batch) product_batch , any_value(check_plan_id) check_plan_id , any_value(status) status , any_value(check_num) check_num , " +
                        "any_value(good_num) good_num , any_value(bad_num) bad_num , any_value(result) result , any_value(del_flag) del_flag , " +
                        "any_value(supplier) supplier , any_value(customer) customer , any_value(deliver_order) deliver_order , any_value(task_type) task_type ," +
                        "any_value(deliver_num) deliver_num , any_value(product_workshop) product_workshop , any_value(classes_id) classes_id")
                .lambda()
                //找出所有基片车间的数据
                .in(QtCheckTask::getMaterialWorkshopName, officeNames)
                //任务类型为化验检
                .eq(QtCheckTask::getTaskType, QtManagerConstant.QtCheckTaskTypeEnum.CHEMICAL.getCode())
                //任务状态为完成的
                .eq(QtCheckTask::getStatus, QtManagerConstant.QtCheckTaskStatusEnum.FINISHED.getCode());

        //如果基片生产车间id不为空根据车间id筛选
        if (Objects.nonNull(dto.getProductWorkshopId())) {
            condition.eq(QtCheckTask::getProductWorkshopId, dto.getProductWorkshopId());
        }

        //按生产车间分组
        condition
                //时间范围
                .ge(QtCheckTask::getFinishedTime, dto.getStartTime())
                .lt(QtCheckTask::getFinishedTime, dto.getEndTime())
                .groupBy(QtCheckTask::getProductWorkshopId)
                .orderByAsc(QtCheckTask::getFinishedTime);

        return condition;
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

    public QtTaskClassCalculate calculateDateTime(LocalDateTime time) {
        QtTaskClassCalculate result = new QtTaskClassCalculate();
        //早班开始时间
        LocalDateTime zaoStartTime = time;
        zaoStartTime = zaoStartTime.withHour(8).withMinute(0).withSecond(0);
        //早班结束时间
        LocalDateTime zaoEndTime = time;
        zaoEndTime = zaoEndTime.withHour(20).withMinute(0).withSecond(0);

        //在早班开始之前
        if (time.isBefore(zaoStartTime)) {
            result.setClassType(1);
            result.setClassDate(zaoStartTime.minusDays(1).withHour(0).withMinute(1));
            return result;
        }
        //在早班的范围内
        if (time.isAfter(zaoStartTime) && time.isBefore(zaoEndTime)) {
            result.setClassType(0);
            result.setClassDate(zaoStartTime.withHour(0).withMinute(1));
            return result;
        }
        //在早班之后
        if (time.isAfter(zaoEndTime)) {
            result.setClassType(1);
            result.setClassDate(zaoStartTime.withHour(0).withMinute(1));
            return result;
        }
        return result;
    }

    private static Predicate<QtIQCAcceptabilityDetailVO> predicate(Integer compare, List<String> filterList, List<QtIQCAcceptabilityDetailVO> list) {
        Predicate<QtIQCAcceptabilityDetailVO> predicate = new Predicate<QtIQCAcceptabilityDetailVO>() {
            @Override
            public boolean test(QtIQCAcceptabilityDetailVO o) {
                List<QtIQCAcceptabilityDetailVO> resList = null;
                if (QtManagerConstant.QtChartCompareEnum.WORKSHOP.equals(compare)) {
                    resList = list.stream().filter(v -> filterList.contains(v.getProductWorkshop().getName())).collect(Collectors.toList());
                } else {
                    resList = list;
                }
                return Boolean.TRUE;
            }
        };
        return predicate;
    }

}




