package com.steccn.pms.service;

import com.steccn.common.constant.ResultCode;
import com.steccn.common.dto.QtLineChartBarDTO;
import com.steccn.pms.dto.DashboardProducePageDTO;
import com.steccn.pms.vo.DashboardProducePageVO;

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
     * @param unit       单位
     * @param unitLength 精度
     * @return
     */
    BigDecimal consume(Integer workShopId, LocalDate date, String unit, Integer unitLength);


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

    /**
     * 产线耗用数
     * @param workShopId 车间
     * @param date 日期
     * @return 柱状图
     */
    QtLineChartBarDTO lineUseCount(Integer workShopId, LocalDate date);

    /**
     * 产线完工信息
     * @param workShopId 车间
     * @param date 日期
     * @return
     */
    QtLineChartBarDTO lineComplete(Integer workShopId, LocalDate date);

    /**
     * 产线入库信息
     * @param workShopId 车间
     * @param date 日期
     * @return
     */
    QtLineChartBarDTO lineInStorage(Integer workShopId, LocalDate date);

}
