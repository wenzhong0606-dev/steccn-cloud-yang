package com.steccn.ems.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.ems.dto.EpSectionPriceDTO;
import com.steccn.ems.vo.EpEnergyChartPieDisVO;
import com.steccn.ems.vo.EpEnergyMoneyVO;
import com.steccn.ems.vo.EqEnergyChartPieVO;

import java.math.BigDecimal;
import java.util.List;

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

    /**
     * 峰谷平详情页
     * @param dto
     * @return
     */
    Page<EpEnergyMoneyVO> getDetails(EpSectionPriceDTO dto);
}
