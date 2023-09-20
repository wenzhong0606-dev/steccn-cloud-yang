package com.steccn.qms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.nacos.common.utils.StringUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.common.constant.FeignConstant;
import com.steccn.common.util.LocalDateUtil;
import com.steccn.platform.dto.SysOfficeParentDTO;
import com.steccn.platform.feign.RemoteOfficeFeign;
import com.steccn.platform.vo.SysOfficeVO;
import com.steccn.qms.constant.QtManagerConstant;
import com.steccn.qms.domain.*;
import com.steccn.qms.dto.OutputQualityDetailDTO;
import com.steccn.qms.dto.QtCheckTaskConditionDTO;
import com.steccn.qms.dto.QtOnlineCheckDefectDTO;
import com.steccn.qms.mapper.QtCheckTaskDetailMapper;
import com.steccn.qms.service.*;
import com.steccn.qms.vo.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author yequan
 * @description 针对表【qt_check_task_detail(巡检任务详细信息表)】的数据库操作Service实现
 * @createDate 2022-09-02 08:54:09
 */
@Service
@RequiredArgsConstructor
public class QtCheckTaskDetailServiceImpl extends ServiceImpl<QtCheckTaskDetailMapper, QtCheckTaskDetail> implements QtCheckTaskDetailService {


    @Autowired
    private QtCheckTaskService qtCheckTaskService;

    @Lazy
    private final QtCheckItemService qtCheckItemService;

    private final QtDefectiveItemService qtDefectiveItemService;
    private final QtItemCategoryService qtItemCategoryService;

    @Override
    public Map<Long, List<QtIQCAcceptabilityCopyVO>> listByTaskId(Set<Long> taskIds) {
        Map<Long, List<QtIQCAcceptabilityCopyVO>> resMap = Maps.newHashMap();
        //1.自定义sql获取动态表格内容

        //修复taskIds为空的情况
        List<QtSubtableVO> list = Lists.newArrayList();
        if (CollUtil.isNotEmpty(taskIds)) {
            list = this.baseMapper.listByTaskId(taskIds);
        }

        //2.处理数据
        if (CollUtil.isNotEmpty(list)) {
            //根据任务分组
            Map<Long, List<QtSubtableVO>> map = list.stream().collect(Collectors.groupingBy(QtSubtableVO::getTaskId));
            for (Map.Entry<Long, List<QtSubtableVO>> entry : map.entrySet()) {
                List<QtIQCAcceptabilityCopyVO> copyVOList = Lists.newArrayList();
                Map<Integer, List<QtSubtableVO>> tableMap = entry.getValue().stream().collect(Collectors.groupingBy(QtSubtableVO::getCopy));
                for (Map.Entry<Integer, List<QtSubtableVO>> copyEntry : tableMap.entrySet()) {
                    QtIQCAcceptabilityCopyVO qtIQCAcceptabilityCopyVO = new QtIQCAcceptabilityCopyVO();
                    BeanUtil.copyProperties(copyEntry.getValue().get(0), qtIQCAcceptabilityCopyVO);
                    qtIQCAcceptabilityCopyVO.setList(copyEntry.getValue());
                    copyVOList.add(qtIQCAcceptabilityCopyVO);
                }
                resMap.put(entry.getKey(), copyVOList);
            }
        }
        //3.返回数据
        return resMap;
    }

    @Override
    public List<QtCheckTaskDetailItemMaterialVO> getByClassTime(LocalDateTime startTime, LocalDateTime endTime) {
        List<QtCheckTaskDetailItemMaterialVO> resList = this.baseMapper.selectAllByClassTime(startTime, endTime);
        if (Objects.isNull(resList)) {
            resList = Lists.newArrayList();

        }
        return resList;
    }


    @Override
    public List<OutputQualityDetailVO> listDefectiveData(OutputQualityDetailDTO dto) {
        LocalDateTime start = LocalDateUtil.getMorning(dto.getStartDate());
        LocalDateTime end = LocalDateUtil.getMorning(dto.getEndDate().plusDays(1));
        //获取在线检数据
        //查询符合条件的检验任务

        List<QtCheckTask> qtCheckTasks = qtCheckTaskService.list(new LambdaQueryWrapper<QtCheckTask>()
                //任务完成
                .eq(QtCheckTask::getStatus, QtManagerConstant.QtCheckTaskStatusEnum.FINISHED.getCode())
                //完成时间范围
                .ge(QtCheckTask::getFinishedTime, start)
                .le(QtCheckTask::getFinishedTime, end)
                .eq(QtCheckTask::getTaskType, QtManagerConstant.QtCheckTaskTypeEnum.ONLINE.getCode())
                //车间
                .apply(CollUtil.isNotEmpty(dto.getProductWorkshopId()), "JSON_CONTAINS(JSON_ARRAY(" + String.join(",", dto.getProductWorkshopId().stream().map(Object::toString).collect(Collectors.toList())) + "), product_workshop->'$.id')")
                .orderByAsc(QtCheckTask::getFinishedTime));
        Map<Long, QtCheckTask> taskMap = Maps.newHashMap();
        List<QtCheckTaskDetail> detailList = null;
        if (CollUtil.isNotEmpty(qtCheckTasks)) {
            qtCheckTasks.forEach(qtCheckTask -> taskMap.put(qtCheckTask.getId(), qtCheckTask));
            Set<Long> taskIdSet = qtCheckTasks.stream().map(QtCheckTask::getId).collect(Collectors.toSet());
            //获取所有检验任务明细
            detailList = list(new LambdaQueryWrapper<QtCheckTaskDetail>().in(QtCheckTaskDetail::getTaskId, taskIdSet));
        }

        //根据缺陷id查询类别id和类别名称
        Set<Integer> defectIdSet = Sets.newHashSet();

        //遍历在线检缺陷项集合
        List<OutputQualityDetailVO> flatDefectiveList = new ArrayList<>();
        if (CollUtil.isNotEmpty(detailList)) {
            for (QtCheckTaskDetail qtCheckTaskDetail : detailList) {
                QtCheckTask qtCheckTask = taskMap.get(qtCheckTaskDetail.getTaskId());
                if (CollUtil.isNotEmpty(qtCheckTaskDetail.getDefectNum())) {
                    String numString = JSON.toJSONString(qtCheckTaskDetail.getDefectNum());
                    List<QtOnlineCheckDefectDTO> defectNumList = JSON.parseArray(numString, QtOnlineCheckDefectDTO.class);
                    List<QtOnlineCheckDefectDTO> collect = defectNumList.stream().filter(v -> Objects.nonNull(v.getDefectId())).collect(Collectors.toList());
                    for (QtOnlineCheckDefectDTO defect : collect) {
                        if (Objects.nonNull(defect.getDefectId()) && Objects.nonNull(defect.getDefectNum()) && defect.getDefectNum() > 0) {
                            OutputQualityDetailVO vo = new OutputQualityDetailVO();
                            defectIdSet.add(defect.getDefectId());
                            vo.setDefectId(defect.getDefectId());
                            vo.setDefectName(defect.getDefectName());
                            vo.setDefectNum(Objects.nonNull(defect.getDefectNum()) ? defect.getDefectNum() : 0);
                            vo.setTaskId(qtCheckTaskDetail.getTaskId());
                            vo.setClassId(qtCheckTask.getClassesId());
                            vo.setClassGroupId(qtCheckTask.getClassGroupId());
                            vo.setClassGroupName(qtCheckTask.getClassGroupName());
                            vo.setProductWorkshopId(Objects.nonNull(qtCheckTask.getProductWorkshop()) ? qtCheckTask.getProductWorkshop().getId() : null);
                            vo.setProductLineId(Objects.nonNull(qtCheckTask.getProductLine()) ? qtCheckTask.getProductLine().getId() : null);
                            vo.setProductOrder(Objects.nonNull(qtCheckTask.getProductOrder()) ? qtCheckTask.getProductOrder() : null);
                            flatDefectiveList.add(vo);
                        }
                    }
                }
            }
        }
        //根据缺陷id查询类别id和类别名称
        if (CollUtil.isNotEmpty(flatDefectiveList) && CollUtil.isNotEmpty(defectIdSet)) {
            Map<Integer, QtDefectiveItem> defectMap = qtDefectiveItemService.listById(defectIdSet);
            Set<Integer> categoryIdSet = defectMap.values().stream().map(QtDefectiveItem::getCategoryId).collect(Collectors.toSet());
            if (CollUtil.isNotEmpty(categoryIdSet)) {
                Map<Integer, String> categoryMap = qtItemCategoryService.listByIdCh(categoryIdSet);
                flatDefectiveList.forEach(ele -> {
                    QtDefectiveItem qtDefectiveItem = defectMap.get(ele.getDefectId());
                    if (Objects.nonNull(qtDefectiveItem)) {
                        String categoryName = categoryMap.get(qtDefectiveItem.getCategoryId());
                        ele.setDefectCategory(qtDefectiveItem.getCategoryId());
                        ele.setDefectCategoryName(categoryName);
                    }
                });
            }
        }
        return flatDefectiveList;
    }

    @Override
    public List<OutputQualityDetailVO> listPollingDate(OutputQualityDetailDTO dto) {
        LocalDateTime start = LocalDateUtil.getMorning(dto.getStartDate());
        LocalDateTime end = LocalDateUtil.getMorning(dto.getEndDate().plusDays(1));
        //获取巡检数据
        List<QtCheckTask> patrolCheckTasks = qtCheckTaskService.list(new LambdaQueryWrapper<QtCheckTask>()
                //任务完成
                .eq(QtCheckTask::getStatus, QtManagerConstant.QtCheckTaskStatusEnum.FINISHED.getCode())
                //完成时间范围
                .ge(QtCheckTask::getFinishedTime, start)
                .le(QtCheckTask::getFinishedTime, end)
                .eq(QtCheckTask::getTaskType, QtManagerConstant.QtCheckTaskTypeEnum.PATROL.getCode())
                //车间
                .apply(CollUtil.isNotEmpty(dto.getProductWorkshopId()), "JSON_CONTAINS(JSON_ARRAY(" + String.join(",", dto.getProductWorkshopId().stream().map(Object::toString).collect(Collectors.toList())) + "), product_workshop->'$.id')")
                .orderByAsc(QtCheckTask::getFinishedTime));
        Map<Long, QtCheckTask> taskMap = Maps.newHashMap();
        List<OutputQualityDetailVO> flatDefectiveList = new ArrayList<>();
        if (CollUtil.isNotEmpty(patrolCheckTasks)) {
            // 查询检验项为 ”厚度平均值“的数据
            List<QtCheckItem> items = qtCheckItemService.list(new LambdaQueryWrapper<QtCheckItem>().eq(QtCheckItem::getName, QtManagerConstant.AVERAGE_THICKNESS));
            patrolCheckTasks.forEach(qtCheckTask -> taskMap.put(qtCheckTask.getId(), qtCheckTask));
            Set<Long> taskIdSet = patrolCheckTasks.stream().map(QtCheckTask::getId).collect(Collectors.toSet());
            Set<Integer> itemIds = items.stream().map(QtCheckItem::getId).collect(Collectors.toSet());
            if (CollUtil.isNotEmpty(taskIdSet) && CollUtil.isNotEmpty(itemIds)) {
                List<QtCheckTaskDetail> list = list(new LambdaQueryWrapper<QtCheckTaskDetail>().in(QtCheckTaskDetail::getTaskId, taskIdSet).in(QtCheckTaskDetail::getItemId, itemIds));
                if (CollUtil.isNotEmpty(list)) {
                    for (QtCheckTaskDetail detail : list) {
                        QtCheckTask qtCheckTask = taskMap.get(detail.getTaskId());
                        OutputQualityDetailVO vo = new OutputQualityDetailVO();
                        vo.setCheckItemId(detail.getItemId());
                        vo.setCheckResult(detail.getCheckResult());
                        vo.setTaskId(qtCheckTask.getId());
                        vo.setClassId(qtCheckTask.getClassesId());
                        vo.setClassGroupId(qtCheckTask.getClassGroupId());
                        vo.setClassGroupName(qtCheckTask.getClassGroupName());
                        vo.setProductWorkshopId(Objects.nonNull(qtCheckTask.getProductWorkshop()) ? qtCheckTask.getProductWorkshop().getId() : null);
                        vo.setProductLineId(Objects.nonNull(qtCheckTask.getProductLine()) ? qtCheckTask.getProductLine().getId() : null);
                        vo.setProductOrder(Objects.nonNull(qtCheckTask.getProductOrder()) ? qtCheckTask.getProductOrder() : null);
                        flatDefectiveList.add(vo);
                    }
                }
            }
        }
        return flatDefectiveList;
    }
}




