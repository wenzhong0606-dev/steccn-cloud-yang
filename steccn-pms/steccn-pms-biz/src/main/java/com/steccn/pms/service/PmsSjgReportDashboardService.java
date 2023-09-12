package com.steccn.pms.service;

import com.steccn.common.constant.ResultCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * @author yequan
 * @desc 深加工车间当日看板接口
 * @date 2023/9/12
 */
public interface PmsSjgReportDashboardService {

    /**
     * 获取当日产量
     *
     * @param workShopId 车间
     * @param date       日期
     * @return
     */
    List<BigDecimal> produceNumToday(Integer workShopId, LocalDate date);

    /**
     * 产量合格率
     *
     * @param workShopId 车间
     * @param date       日期
     * @return
     */
    BigDecimal produceNumRate(Integer workShopId, LocalDate date);

    /**
     * 今日入库数（单位/T/片/㎡）
     *
     * @param workShopId 车间
     * @param date       日期
     * @return
     */
    List<BigDecimal> inStorageToday(Integer workShopId, LocalDate date);

    /**
     * 入库合格率
     * @param workShopId 车间
     * @param date       日期
     * @return
     */
    BigDecimal inStorageRate(Integer workShopId, LocalDate date);
}
