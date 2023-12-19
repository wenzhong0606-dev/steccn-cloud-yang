package com.steccn.pms.service;

import com.steccn.common.constant.ResultCode;
import com.steccn.common.dto.QtLineChartBarDTO;
import com.steccn.common.dto.TupleDTO;
import com.steccn.pms.dto.QtLineChartBarCountDTO;

import java.time.LocalDate;

/**
 * @author yequan
 * @desc 基片车间生产驾驶舱-统计看板
 * @date 2023/9/11
 */
public interface PmsReportDashboardCtService {


    /**
     * 班次日期总成品率
     *
     * @param workShopId  车间主键
     * @param start       开始日期
     * @param end         结束日期
     * @param granularity 班次，日，周，月，年
     * @return
     */
    QtLineChartBarCountDTO yieldRateDate(Integer workShopId, LocalDate start, LocalDate end, Integer granularity);

    /**
     * 班次日期产量
     *
     * @param workShopId  车间主键
     * @param start       开始日期
     * @param end         结束日期
     * @param granularity 班次，日，周，月，年
     * @param bit         位数
     * @param unit        单位
     * @return
     */
    QtLineChartBarCountDTO pmDate(Integer workShopId, LocalDate start, LocalDate end,Integer granularity, Integer bit , String unit);

    /**
     * 累计入库量总数
     *
     * @param workShopId  车间主键
     * @param start       开始日期
     * @param end         结束日期
     * @param granularity 班次，日，周，月，年
     * @param bit         位数
     * @param unit        单位
     * @return
     */
    QtLineChartBarCountDTO inStorageDate(Integer workShopId, LocalDate start, LocalDate end, Integer granularity, int bit, String unit);

    /**
     * 累计拉引量总数
     *
     * @param workShopId 车间主键
     * @param start      开始日期
     * @param end        结束日期
     * @param granularity 班次，日，周，月，年
     * @param bit        位数
     * @param unit       单位
     * @return
     */
    QtLineChartBarCountDTO pullForceDate(Integer workShopId, LocalDate start, LocalDate end, Integer granularity, int bit, String unit);

    /**
     * 各产线成品率-完工
     *
     * @param workShopId  车间主键
     * @param start       开始日期
     * @param end         结束日期
     * @param granularity 班次，日，周，月，年
     * @return
     */
    QtLineChartBarCountDTO yieldRateDatePm(Integer workShopId, LocalDate start, LocalDate end, Integer granularity);

    /**
     * 各产线成品率-入库
     *
     * @param workShopId  车间主键
     * @param start       开始日期
     * @param end         结束日期
     * @param granularity 班次，日，周，月，年
     * @return
     */
    QtLineChartBarCountDTO yieldRateDateInStorage(Integer workShopId, LocalDate start, LocalDate end, Integer granularity);

    /**
     * 各产线总产量
     *
     * @param workShopId  车间主键
     * @param start       开始日期
     * @param end         结束日期
     * @param granularity 班次，日，周，月，年
     * @return
     */
    QtLineChartBarCountDTO lineDatePm(Integer workShopId, LocalDate start, LocalDate end, Integer granularity);

    /**
     * 各产线入库总数
     *
     * @param workShopId 车间主键
     * @param start      开始日期
     * @param end        结束日期
     * @param granularity   班次，日，周，月，年
     * @return
     */
    QtLineChartBarCountDTO lineDateInStorage(Integer workShopId, LocalDate start, LocalDate end, Integer granularity);

    /**
     * 各产线拉引量总数
     *
     * @param workShopId  车间主键
     * @param start       开始日期
     * @param end         结束日期
     * @param granularity 班次，日，周，月，年
     * @return
     */
    QtLineChartBarCountDTO lineDatePf(Integer workShopId, LocalDate start, LocalDate end, Integer granularity);
}
