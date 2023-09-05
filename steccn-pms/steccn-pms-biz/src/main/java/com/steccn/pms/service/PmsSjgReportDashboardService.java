package com.steccn.pms.service;

import com.steccn.common.constant.ResultCode;
import com.steccn.common.dto.QtLineChartBarDTO;

import java.time.LocalDate;

/**
 * @author yequan
 * @desc 深加工车间看板接口
 * @date 2023/9/5
 */
public interface PmsSjgReportDashboardService {

    /**
     * 产线产量
     *
     * @param workShopId 车间id
     * @param date       日期
     * @return
     */
    QtLineChartBarDTO lineProduce(Integer workShopId, LocalDate date);
}
