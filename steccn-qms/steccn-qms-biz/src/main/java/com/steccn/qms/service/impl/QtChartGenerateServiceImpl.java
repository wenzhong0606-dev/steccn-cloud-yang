package com.steccn.qms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.steccn.qms.constant.QtManagerConstant;
import com.steccn.qms.domain.QtCheckTask;
import com.steccn.qms.dto.QtChartParamDTO;
import com.steccn.qms.service.QtChartGenerateService;
import com.steccn.qms.service.QtCheckTaskService;
import com.steccn.qms.vo.QtChartBarVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 *
 * 生成图片数据
 * @author AndrewLuo
 * @date 2022/11/10
 */
@Service
@RequiredArgsConstructor
public class QtChartGenerateServiceImpl implements QtChartGenerateService {

    private final QtCheckTaskService qtCheckTaskService;
    /**
     * 生成基片损耗图
     * @param dto
     * @return
     */
    @Override
    public QtChartBarVO generateSubstrateLoss(QtChartParamDTO dto) {
        //转时间戳
        LocalDateTime startTime = dto.getDateStart().atTime(0,0,1);
        LocalDateTime endTime = dto.getDateEnd().atTime(23, 59,59);
        //1、先根据已知条件查询任务表
        LambdaQueryWrapper<QtCheckTask> taskCondition = new LambdaQueryWrapper<QtCheckTask>()
                //任务完成
                .eq(QtCheckTask::getStatus, QtManagerConstant.QtCheckTaskStatusEnum.FINISHED)
                //完成时间范围
                .between(QtCheckTask::getFinishedTime,startTime,endTime)
                //班次
                .eq(dto.getWorkOrderId()!=null,QtCheckTask::getClassesId,dto.getWorkOrderId())
                //车间
                .apply(dto.getProductWorkShopId() != null, "JSON_CONTAINS(product_workshop, JSON_OBJECT('id', {0}))", dto.getProductWorkShopId())
                //产线
                .apply(dto.getProductLineId() != null, "JSON_CONTAINS(product_line, JSON_OBJECT('id', {0}))", dto.getProductLineId());
        List<QtCheckTask> taskList = qtCheckTaskService.list(taskCondition);

        return null;
    }
}
