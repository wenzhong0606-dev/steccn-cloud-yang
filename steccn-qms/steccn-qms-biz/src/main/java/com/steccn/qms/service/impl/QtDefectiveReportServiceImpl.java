package com.steccn.qms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.steccn.qms.constant.QtManagerConstant;
import com.steccn.qms.domain.QtCheckTask;
import com.steccn.qms.domain.QtCheckTaskDetail;
import com.steccn.qms.domain.QtDefectiveItem;
import com.steccn.qms.dto.QtChartParamDTO;
import com.steccn.qms.dto.QtCheckTaskConditionDTO;
import com.steccn.qms.dto.QtOnlineCheckDefectDTO;
import com.steccn.qms.service.QtCheckTaskDetailService;
import com.steccn.qms.service.QtCheckTaskService;
import com.steccn.qms.service.QtDefectiveItemService;
import com.steccn.qms.service.QtDefectiveReportService;
import com.steccn.qms.vo.QtChartBarVO;
import com.steccn.qms.vo.QtCheckTaskDetailReportVO;
import com.steccn.qms.vo.QtCheckTaskDetailVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.Lists;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author yequan
 * @description 缺陷日报表
 * @createDate 2022-11-12 08:54:09
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class QtDefectiveReportServiceImpl implements QtDefectiveReportService {

    private final QtCheckTaskService qtCheckTaskService;
    private final QtCheckTaskDetailService qtCheckTaskDetailService;
    private final QtDefectiveItemService qtDefectiveItemService;

    @Override
    public QtChartBarVO deepDefectiveCategory(QtChartParamDTO dto) {
        defectiveDataFlat(dto);
        return null;
    }

    /**
     * 获取缺陷扁平化数据
     *
     * @param dto
     */
    private void defectiveDataFlat(QtChartParamDTO dto) {
        //查询符合条件的检验任务
        QtCheckTaskConditionDTO condition = QtCheckTaskConditionDTO.builder()
                .taskType(QtManagerConstant.QtCheckTaskTypeEnum.ONLINE.getCode())
                .startTime(LocalDateTime.of(dto.getDateStart(), LocalTime.MIN))
                .endTime(LocalDateTime.of(dto.getDateEnd(), LocalTime.MAX))
                .workOrderId(dto.getWorkOrderId())
                .productWorkShopId(dto.getProductWorkShopId())
                .productLineId(dto.getProductLineId())
                .build();
        List<QtCheckTask> qtCheckTasks = qtCheckTaskService.conditionTaskList(condition);
        Assert.notEmpty(qtCheckTasks, "深加工缺陷类别分布暂无数据");
        //获取检验项类别
        Map<Long, QtCheckTask> taskMap = Maps.newHashMap();
        qtCheckTasks.forEach(ele -> taskMap.put(ele.getId(), ele));
        //对符合条件的检验任务进行缺陷项的统计
        Set<Long> taskIdSet = qtCheckTasks.stream().map(QtCheckTask::getId).collect(Collectors.toSet());
        //获取所有的检验任务明细结果
        List<QtCheckTaskDetail> detailList = qtCheckTaskDetailService.list(new LambdaQueryWrapper<QtCheckTaskDetail>().in(QtCheckTaskDetail::getTaskId, taskIdSet));
        //获取所有的缺陷项id集合
        Set<Integer> defectiveIdSet = Sets.newHashSet();
        detailList.forEach(ele -> {
            if (CollUtil.isNotEmpty(ele.getDefectNum())) {
                String numString = JSON.toJSONString(ele.getDefectNum());
                List<QtOnlineCheckDefectDTO> defectNumList = JSON.parseArray(numString, QtOnlineCheckDefectDTO.class);
                for (QtOnlineCheckDefectDTO de : defectNumList) {
                    defectiveIdSet.add(de.getDefectId());
                }
            }
        });
        //获取缺陷项id集合
        Assert.notEmpty(defectiveIdSet, "深加工缺陷项集合为空，暂无报表");
        Map<Integer, Integer> defectiveItemMap = Maps.newHashMap();
        List<QtDefectiveItem> defectiveItemList = qtDefectiveItemService.listByIds(defectiveIdSet);
        Assert.notEmpty(defectiveItemList, "深加工缺陷集合为空，暂无报表");
        defectiveItemList.forEach(ele -> defectiveItemMap.put(ele.getId(), ele.getCategoryId()));
        //遍历缺陷项集合
        List<QtCheckTaskDetailReportVO> flatDefectiveList = Lists.newArrayList();
        detailList.forEach(ele -> {
            //获取检验任务，一定存在，因为明细是根据任务查询出来的
            QtCheckTask qtCheckTask = taskMap.get(ele.getTaskId());
            if (CollUtil.isNotEmpty(ele.getDefectNum())) {
                String numString = JSON.toJSONString(ele.getDefectNum());
                List<QtOnlineCheckDefectDTO> defectNumList = JSON.parseArray(numString, QtOnlineCheckDefectDTO.class);
                defectNumList.forEach(de -> {
                    if (Objects.nonNull(de.getDefectId())) {
                        QtCheckTaskDetailReportVO vo = new QtCheckTaskDetailReportVO();
                        vo.setDefectId(de.getDefectId());
                        vo.setDefectCategory(defectiveItemMap.get(de.getDefectId()));
                        vo.setDefectNum(Objects.nonNull(de.getDefectNum()) ? de.getDefectNum() : 0);
                        vo.setId(ele.getId());
                        vo.setTaskId(ele.getTaskId());
                        vo.setTaskCode(qtCheckTask.getCode());
                        vo.setProductWorkshopId(Objects.nonNull(qtCheckTask.getProductWorkshop()) ? qtCheckTask.getProductWorkshop().getId() : null);
                        vo.setProductLineId(Objects.nonNull(qtCheckTask.getProductLine()) ? qtCheckTask.getProductLine().getId() : null);
                        vo.setClassId(qtCheckTask.getClassesId());
                        flatDefectiveList.add(vo);
                    }
                });
            }
        });
    }
}
