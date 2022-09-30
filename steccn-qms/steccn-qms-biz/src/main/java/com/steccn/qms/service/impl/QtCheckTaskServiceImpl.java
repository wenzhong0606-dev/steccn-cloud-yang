package com.steccn.qms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.steccn.authserver.exception.BusinessException;
import com.steccn.common.constant.MpConstant;
import com.steccn.platform.feign.RemoteOfficeFeign;
import com.steccn.qms.domain.*;
import com.steccn.qms.dto.*;
import com.steccn.qms.mapper.QtMaterialLinkCheckPlanMapper;
import com.steccn.qms.service.*;
import com.steccn.qms.mapper.QtCheckTaskMapper;
import com.steccn.qms.util.CodeGenerateUtil;
import com.steccn.qms.vo.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    /**
     * 新增
     *
     * @param dto
     * @return
     */
    @Override
    public Boolean add(QtAddCheckTaskDTO dto) {
        QtCheckTask qtCheckTask = new QtCheckTask();
        BeanUtils.copyProperties(dto, qtCheckTask);
        //任务类型,原始任务
        qtCheckTask.setType(0);
        //状态待检验
        qtCheckTask.setStatus(0);
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
        }else {
            //取旧的快照
            qtCheckTask.setCheckPlanId(maxSnapshot.getId());
        }
        return save(qtCheckTask);
    }

    /**
     * 任务列表
     *
     * @param dto
     * @return
     */
    @Override
    public List<QtCheckTaskListVO> checkTaskList(QtCheckTaskListDTO dto) {
        List<QtCheckTask> qtCheckTasks = qtCheckTaskMapper.selectByTaskType(dto.getCheckTypeCode(), dto.getStatus());
        List<QtCheckTaskListVO> vos = new ArrayList<>();
        if (qtCheckTasks != null && qtCheckTasks.size() > 0) {
            qtCheckTasks.forEach(tmp -> {
                QtCheckTaskListVO qtCheckTaskListVo = new QtCheckTaskListVO();
                qtCheckTaskListVo.setId(tmp.getId());
                qtCheckTaskListVo.setStatus(tmp.getStatus());
                //返回名字
                qtCheckTaskListVo.setMaterialName(qtMaterialService.getById(tmp.getMaterialId()).getName());
                qtCheckTaskListVo.setWorkshopName(remoteOfficeFeign.getNameById(tmp.getWorkshopId()).getMsg());
                qtCheckTaskListVo.setProductLineName(remoteOfficeFeign.getNameById(tmp.getProductLineId()).getMsg());
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
    public QtCheckTaskDetailVO taskDetail(Long id) {
        QtCheckTask qtCheckTask = getById(id);
        QtCheckTaskListVO qtCheckTaskListVo = new QtCheckTaskListVO();
        if (qtCheckTask != null) {
            qtCheckTaskListVo.setId(qtCheckTask.getId());
            qtCheckTaskListVo.setStatus(qtCheckTask.getStatus());
            //可能有结果
            if (qtCheckTask.getCheckNum() != null) {
                qtCheckTaskListVo.setCheckNum(qtCheckTask.getCheckNum());
            }
            if (qtCheckTask.getGoodNum() != null) {
                qtCheckTaskListVo.setGoodNum(qtCheckTask.getGoodNum());
            }
            if (qtCheckTask.getBadNum() != null) {
                qtCheckTaskListVo.setBadNum(qtCheckTask.getBadNum());
            }
            if (qtCheckTask.getResult() != null) {
                qtCheckTaskListVo.setResult(qtCheckTask.getResult());
            }
            if (qtCheckTask.getNote() != null) {
                qtCheckTaskListVo.setNote(qtCheckTask.getNote());
            }
            //返回名字
            qtCheckTaskListVo.setMaterialName(qtMaterialService.getById(qtCheckTask.getMaterialId()).getName());
            qtCheckTaskListVo.setWorkshopName(remoteOfficeFeign.getNameById(qtCheckTask.getWorkshopId()).getMsg());
            qtCheckTaskListVo.setProductLineName(remoteOfficeFeign.getNameById(qtCheckTask.getProductLineId()).getMsg());
        }
        QtCheckTaskDetailVO qtCheckTaskDetailVo = new QtCheckTaskDetailVO();
        qtCheckTaskDetailVo.setQtCheckTaskListVo(qtCheckTaskListVo);

        //处理样本数
        //选择任务详情
        QueryWrapper<QtCheckTaskDetail> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("DISTINCT copy").eq("task_id", id).orderByAsc("copy");
        List<QtCheckTaskDetail> qtCheckTaskDetails = qtCheckTaskDetailService.list(queryWrapper);
        List<QtCheckTaskDetailCopyVO> copy = Lists.newArrayList();
        if (!qtCheckTaskDetails.isEmpty()) {
            //包装好group和copy
            qtCheckTaskDetails.forEach(tmp -> {
                QtCheckPlanDetailsVO qtCheckPlanDetailsVO = qtCheckPlanService.info(qtCheckTask.getCheckPlanId(), id, tmp.getCopy());
                QtCheckTaskDetailCopyVO qtCheckTaskDetailCopyVO = new QtCheckTaskDetailCopyVO();
                qtCheckTaskDetailCopyVO.setNum(tmp.getCopy());
                qtCheckTaskDetailCopyVO.setCheckGroup(qtCheckPlanDetailsVO.getCheckGroup());
                copy.add(qtCheckTaskDetailCopyVO);
            });
        }
        //details没有值说明没有输入详细值
        else {
            QtCheckPlanDetailsVO qtCheckPlanDetailsVO = qtCheckPlanService.info(qtCheckTask.getCheckPlanId(), id, null);
            QtCheckTaskDetailCopyVO qtCheckTaskDetailCopyVO = new QtCheckTaskDetailCopyVO();
            qtCheckTaskDetailCopyVO.setNum(1);
            qtCheckTaskDetailCopyVO.setCheckGroup(qtCheckPlanDetailsVO.getCheckGroup());
            copy.add(qtCheckTaskDetailCopyVO);
        }
        QtCheckPlanDetailsVO qtCheckPlanDetailsVO = new QtCheckPlanDetailsVO();
        qtCheckPlanDetailsVO.setCopy(copy);
        qtCheckTaskDetailVo.setQtCheckPlanDetailsVO(qtCheckPlanDetailsVO);
        return qtCheckTaskDetailVo;
    }

    /**
     * 修改任务状态
     *
     * @param dto
     * @return
     */
    @Override
    public Boolean updateStatus(QtCheckTaskStatusDTO dto) {
        QtCheckTask qtCheckTask = new QtCheckTask();
        qtCheckTask.setId(dto.getId());
        qtCheckTask.setStatus(dto.getStatus());
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

        //插入值
        if (dto.getValues() != null && dto.getValues().size() > 0) {
            dto.getValues().forEach(valueTmp -> {
                //先删除可能暂存的值
                Map<String, Object> valueCondition = new HashMap<>();
                valueCondition.put("task_id", dto.getId());
                valueCondition.put("copy", valueTmp.getCopy());
                qtCheckTaskDetailService.removeByMap(valueCondition);
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
                                valueModels.add(qtCheckTaskDetail);
                            });
                        }
                    });
                }

            });
            qtCheckTaskDetailService.saveBatch(valueModels);
        }
        QtCheckTaskStatusDTO statusDTO = new QtCheckTaskStatusDTO();
        statusDTO.setId(dto.getId());
        statusDTO.setStatus(dto.getStatus());
        return updateStatus(statusDTO);
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
        return updateById(qtCheckTask);
    }

    /**
     * 复检任务
     *
     * @param id
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long retask(Long id) {
        QtCheckTask res = getById(id);
        QtCheckTask model = new QtCheckTask();
        if (res == null) {
            throw new BusinessException("任务信息不存在");
        }
        BeanUtils.copyProperties(res, model);
        //复制值后置空
        model.setBadNum(null);
        model.setCheckNum(null);
        model.setGoodNum(null);
        model.setResult(null);
        model.setNote(null);
        model.setStatus(0);
        model.setType(1);
        model.setId(null);
        save(model);
        QtRecheckTask recheckModel = new QtRecheckTask();
        recheckModel.setOriginalId(id);
        recheckModel.setRecheckId(model.getId());
        qtRecheckTaskService.save(recheckModel);
        return model.getId();
    }

    @Override
    public List<QtCheckTaskTypeCountVO> countTaskByType() {
        List<QtCheckTaskTypeCountVO> vos = Lists.newArrayList();
        //检验方案编码:来料检1/首检2/在线检3/巡检4/出货检5
        for (int i = 1; i <= 5; i++) {
            QtCheckTaskTypeCountVO vo = new QtCheckTaskTypeCountVO();
            Long count = qtMaterialLinkCheckPlanMapper.CountTaskByTaskType(i);
            vo.setCount(count);
            vo.setTypeCode(i);
            vos.add(vo);
        }
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




