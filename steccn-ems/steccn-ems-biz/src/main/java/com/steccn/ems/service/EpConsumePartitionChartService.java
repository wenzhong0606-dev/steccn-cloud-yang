package com.steccn.ems.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.common.constant.ResultCode;
import com.steccn.common.vo.BaseSelectVO;
import com.steccn.common.vo.EpChartGraphVO;
import com.steccn.common.vo.EpChartGraphXAxisVO;
import com.steccn.ems.dto.EpTableDTO;
import com.steccn.ems.dto.EpTotalConsumeDTO;
import com.steccn.ems.vo.*;

import javax.servlet.http.HttpServletResponse;
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
     * 能耗分区对比 -均值，最值统计表格
     * @param dto
     * @return
     */
    ConsumePartitionContrastTableVO consumePartitionContrastTable(EpTableDTO dto);

    /**
     * 能耗分区对比-明细
     * @param dto dto
     * @return 分页对象
     */
    EpConsumeDetailVO consumePartitionContrastDetail(EpTableDTO dto);

    /**
     * 下载能耗分区
     * @param dto
     * @param response
     */
    void downloadConsume(EpTableDTO dto, HttpServletResponse response);

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
     * 下载能耗历史
     * @param dto
     * @param response
     */
    void downloadHistoryConsume(EpTableDTO dto, HttpServletResponse response);

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

    /**
     * 领导驾驶舱-总体能耗趋势(近7日)-折线图
     * @param dto dto
     * @return
     */
    EpChartGraphVO<EpChartGraphXAxisVO, BigDecimal> totalConsumePartitionLine(EpTotalConsumeDTO dto);

    /**
     * 领导驾驶舱-电耗分布(近7日)-饼图
     * @param dto dto
     * @return 饼图
     */
    List<EpEnergyChartPieValueVO<BigDecimal>> totalConsumeEnergyPie(EpTableDTO dto);

    /**
     * 单耗历史对比-柱状图
     * @param dto dto请求
     * @return
     */
    EpChartGraphVO<EpChartGraphXAxisVO, BigDecimal> consumePartitionUnitHistoryChart(EpTableDTO dto);

    /**
     * 单耗历史对比-明细
     * @param dto dto
     * @return 明细分页列表
     */
    Page<ConsumeHistoryContrastDetailVO> consumePartitionUnitHistoryDetail(EpTableDTO dto);

    /**
     * 单耗分区对比-柱状图
     *
     * @param dto
     * @return
     */
    EpChartGraphVO<EpChartGraphXAxisVO, BigDecimal> unitConsumePartitionContrastChart(EpTableDTO dto);

    /**
     * 单耗分区对比-明细
     *
     * @param dto
     * @return
     */
    EpConsumeDetailVO unitConsumePartitionContrastDetail(EpTableDTO dto);

    /**
     * 单耗分区对比-下载
     *
     * @param dto
     * @param response
     */
    void unitContrastDownload(EpTableDTO dto, HttpServletResponse response);

    /**
     * 单耗班组对比-下载
     * @param dto
     * @param response
     */
    void unitClassDownload(EpTableDTO dto, HttpServletResponse response);

    /**
     * 单耗历史对比-下载
     * @param dto
     * @param response
     */
    void unitHistoryDownload(EpTableDTO dto, HttpServletResponse response);
}
