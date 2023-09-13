package com.steccn.pms.service;

import com.steccn.common.constant.ResultCode;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * @author yequan
 * @desc 深加工车间当日看板接口
 * @date 2023/9/12
 */
public interface PmsSjgReportDashboardService {

    /**
     * 领用数
     *
     * @param workShopId 车间
     * @param date       日期
     * @return
     */
    BigDecimal slip(Integer workShopId, LocalDate date);

    /**
     * 耗用数
     *
     * @param workShopId 车间
     * @param date       日期
     * @return
     */
    BigDecimal consume(Integer workShopId, LocalDate date);
}
