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
     * 班次日期总成品率
     *
     * @param workShopId 车间主键
     * @param start      开始日期
     * @param end        结束日期
     * @return
     */
    QtLineChartBarDTO yieldRateDate(Integer workShopId, LocalDate start, LocalDate end);

    /**
     * 班次日期产量
     *
     * @param workShopId 车间主键
     * @param start      开始日期
     * @param end        结束日期
     * @param bit        位数
     * @param unit       单位
     * @return
     */
    QtLineChartBarDTO pmDate(Integer workShopId, LocalDate start, LocalDate end, Integer bit , String unit);

    /**
     * 累计入库量总数
     *
     * @param workShopId 车间主键
     * @param start      开始日期
     * @param end        结束日期
     * @param bit        位数
     * @param unit       单位
     * @return
     */
    QtLineChartBarDTO inStorageDate(Integer workShopId, LocalDate start, LocalDate end, int bit, String unit);

    /**
     * 累计拉引量总数
     *
     * @param workShopId 车间主键
     * @param start      开始日期
     * @param end        结束日期
     * @param bit        位数
     * @param unit       单位
     * @return
     */
    QtLineChartBarDTO pullForceDate(Integer workShopId, LocalDate start, LocalDate end, int bit, String unit);

    /**
     * 各产线成品率-完工
     *
     * @param workShopId 车间主键
     * @param start      开始日期
     * @param end        结束日期
     * @param dateType   日期，班次
     * @return
     */
    QtLineChartBarDTO yieldRateDatePm(Integer workShopId, LocalDate start, LocalDate end, Integer dateType);

    /**
     * 各产线成品率-入库
     *
     * @param workShopId 车间主键
     * @param start      开始日期
     * @param end        结束日期
     * @param dateType   日期，班次
     * @return
     */
    QtLineChartBarDTO yieldRateDateInStorage(Integer workShopId, LocalDate start, LocalDate end, Integer dateType);

    /**
     * 各产线总产量
     *
     * @param workShopId 车间主键
     * @param start      开始日期
     * @param end        结束日期
     * @param dateType   日期，班次
     * @return
     */
    QtLineChartBarDTO lineDatePm(Integer workShopId, LocalDate start, LocalDate end, Integer dateType);

    /**
     * 各产线入库总数
     *
     * @param workShopId 车间主键
     * @param start      开始日期
     * @param end        结束日期
     * @param dateType   日期，班次
     * @return
     */
    QtLineChartBarDTO lineDateInStorage(Integer workShopId, LocalDate start, LocalDate end, Integer dateType);

    /**
     * 各产线拉引量总数
     *
     * @param workShopId 车间主键
     * @param start      开始日期
     * @param end        结束日期
     * @param dateType   日期，班次
     * @return
     */
    QtLineChartBarDTO lineDatePf(Integer workShopId, LocalDate start, LocalDate end, Integer dateType);
}
