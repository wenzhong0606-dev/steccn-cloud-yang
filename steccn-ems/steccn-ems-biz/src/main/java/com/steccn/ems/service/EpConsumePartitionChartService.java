package com.steccn.ems.service;

import com.steccn.common.vo.EpChartGraphVO;
import com.steccn.common.vo.EpChartGraphXAxisVO;
import com.steccn.ems.dto.EpTableDTO;

import java.math.BigDecimal;

/**
 * 能耗分区图表生成
 *
 * @author yequan
 * @date 2022/12/07
 */
public interface EpConsumePartitionChartService {

    /**
     * 能耗分区对比
     *
     * @param dto dto
     * @return 柱状图
     */
    EpChartGraphVO<EpChartGraphXAxisVO, BigDecimal> consumePartitionContrast(EpTableDTO dto);
}
