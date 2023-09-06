package com.steccn.pms.service;

import com.steccn.common.constant.ResultCode;
import com.steccn.common.dto.QtLineChartBarDTO;
import com.steccn.common.vo.EpChartGraphVO;
import com.steccn.common.vo.EpChartGraphXAxisVO;
import com.steccn.pms.vo.SjgDashboardLineProduceVO;

import java.math.BigDecimal;
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
     * @param unit       单位
     * @param bit        位数
     * @return
     */
    QtLineChartBarDTO lineProduce(Integer workShopId, LocalDate date, String unit, Integer bit);

    /**
     * 产线产量-表格
     *
     * @param workShopId 车间id
     * @param date       日期
     * @param unit       单位
     * @param bit        位数
     * @return
     */
    SjgDashboardLineProduceVO lineProduceDetail(Integer workShopId, LocalDate date, String unit, Integer bit);

    /**
     * 产线产量-表格
     *
     * @param workShopId 车间id
     * @param date       日期
     * @param unit       单位
     * @param bit        位数
     * @return
     */
    EpChartGraphVO<EpChartGraphXAxisVO, BigDecimal> lineProduceClassGroup(Integer workShopId, LocalDate date, String unit, Integer bit);
}
