package com.steccn.pms.service;


import com.steccn.common.constant.ResultCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * @author yequan
 * @desc 生产看板服务
 * @date 2023/8/28
 */
public interface PmsReportDashboardService {

    /**
     * 今日拉引量(单位/T)
     *
     * @param workShopId 车间id
     * @param date       班次日期
     * @return
     */
    BigDecimal pullForceToday(Integer workShopId, LocalDate date);

    /**
     * 今日产量（单位/T/片/㎡）
     *
     * @param workShopId 车间id
     * @param date       班次日期
     * @return
     */
    List<BigDecimal> produceNumToday(Integer workShopId, LocalDate date);

    /**
     * 产量合格率
     *
     * @param workShopId 车间id
     * @param date       班次日期
     * @return
     */
    BigDecimal produceNumRate(Integer workShopId, LocalDate date);

    /**
     * 今日入库数（单位/T/片/㎡）
     * @param workShopId 车间id
     * @param date       班次日期
     * @return
     */
    List<BigDecimal> inStorageToday(Integer workShopId, LocalDate date);

    /**
     * 入库合格率
     *
     * @param workShopId 车间id
     * @param date       班次日期
     * @return
     */
    BigDecimal inStorageRate(Integer workShopId, LocalDate date);
}
