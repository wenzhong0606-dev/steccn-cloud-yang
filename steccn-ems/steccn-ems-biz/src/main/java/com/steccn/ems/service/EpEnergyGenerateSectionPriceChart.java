package com.steccn.ems.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.ems.dto.EpSectionPriceDTO;
import com.steccn.ems.dto.EpSectionPriceLeaderDTO;
import com.steccn.ems.vo.EpEnergyChartPieDisVO;
import com.steccn.ems.vo.EpEnergyMoneyVO;
import com.steccn.ems.vo.EqEnergyChartPieVO;
import com.steccn.qms.dto.AccessoryAcceptabilityDownDTO;
import lombok.SneakyThrows;

import javax.servlet.http.HttpServletResponse;
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
     * 生成峰谷平图表
     * @param dto
     * @return
     */
    EpEnergyChartPieDisVO generateDistribution(EpSectionPriceDTO dto);

    /**
     * 生成峰谷平图表-领导驾驶舱
     * @param dto
     * @return
     */
    EpEnergyChartPieDisVO generateDistributionLeader(EpSectionPriceLeaderDTO dto);

    /**
     * 峰谷平详情页
     * @param dto
     * @return
     */
    Page<EpEnergyMoneyVO> getDistributionDetails(EpSectionPriceDTO dto);

    /**
     * 下载峰谷平分布
     * @param dto
     * @param response
     */
    void downloadDistribution(EpSectionPriceDTO dto, HttpServletResponse response);
}
