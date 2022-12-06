package com.steccn.ems.service;

import com.steccn.ems.dto.EpSectionPriceDTO;
import com.steccn.ems.vo.EpEnergyChartPieDisVO;
import com.steccn.ems.vo.EqEnergyChartPieVO;

import java.math.BigDecimal;

/**
 * 峰谷平图表生成
 *
 * @author AndrewLuo
 * @date 2022/12/05
 */
public interface EpEnergyGenerateSectionPriceChart {
    /**
     * 生成峰谷平分布数据
     * @param dto
     * @return
     */
    EpEnergyChartPieDisVO generateDistribution(EpSectionPriceDTO dto);
}
