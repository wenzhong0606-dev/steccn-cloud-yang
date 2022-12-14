package com.steccn.ems.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.common.vo.BaseSelectVO;
import com.steccn.common.vo.EpChartGraphVO;
import com.steccn.common.vo.EpChartGraphXAxisVO;
import com.steccn.ems.dto.EpTableDTO;
import com.steccn.ems.vo.ConsumeHistoryContrastDetailVO;
import com.steccn.ems.vo.ConsumePartitionUnitClassDetailVO;
import com.steccn.ems.vo.EpConsumeDetailVO;

import java.math.BigDecimal;
import java.util.List;

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

    /**
     * 能耗分区对比-明细
     * @param dto dto
     * @return 分页对象
     */
    EpConsumeDetailVO consumePartitionContrastDetail(EpTableDTO dto);

    /**
     * 能耗历史对比-柱状图
     *
     * @param dto dto
     * @return 柱状图
     */
    EpChartGraphVO<EpChartGraphXAxisVO, BigDecimal> consumeHistoryContrast(EpTableDTO dto);

    /**
     * 能耗历史对比-明细
     * @param dto dto
     * @return 分页列表
     */
    Page<ConsumeHistoryContrastDetailVO> consumeHistoryContrastDetail(EpTableDTO dto);

    /**
     * 厚度下拉选项
     * @param dto dto
     * @return
     */
    List<BaseSelectVO<BigDecimal>> thick(EpTableDTO dto);

    /**
     * 单耗班组对比-柱状图
     * @param dto dto请求条件
     * @return 图表vo
     */
    EpChartGraphVO<EpChartGraphXAxisVO, BigDecimal> consumePartitionUnitClassChart(EpTableDTO dto);

    /**
     * 单耗班组对比-明细
     * @param dto dto
     * @return 分页参数
     */
    Page<ConsumePartitionUnitClassDetailVO> consumePartitionUnitClassDetail(EpTableDTO dto);
}
