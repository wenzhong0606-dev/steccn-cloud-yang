package com.steccn.pms.service;

import com.steccn.common.constant.ResultCode;
import com.steccn.common.dto.QtLineChartBarDTO;
import com.steccn.common.dto.TupleDTO;

import java.time.LocalDate;

/**
 * @author yequan
 * @desc 基片车间生产驾驶舱-统计看板
 * @date 2023/9/11
 */
public interface PmsReportDashboardCtService {

    /**
     * 累计产量总数
     *
     * @param workShopId 车间主键
     * @param start      开始日期
     * @param end        结束日期
     * @param unit       单位对象
     * @return
     */
    QtLineChartBarDTO produceClass(Integer workShopId, LocalDate start, LocalDate end, TupleDTO<String, Integer> unit);

    /**
     * 总成品率
     *
     * @param workShopId 车间主键
     * @param start      开始日期
     * @param end        结束日期
     * @return
     */
    QtLineChartBarDTO yieldRateDate(Integer workShopId, LocalDate start, LocalDate end);
}
