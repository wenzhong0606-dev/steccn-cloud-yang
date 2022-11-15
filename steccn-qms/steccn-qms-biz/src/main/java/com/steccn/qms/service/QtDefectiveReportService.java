package com.steccn.qms.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.qms.dto.QtChartParamDTO;
import com.steccn.qms.vo.*;

import java.util.List;

/**
 * @author yequan
 * @description 缺陷项报表控制器
 * @createDate 2022-11-12 14:35:09
 */
public interface QtDefectiveReportService {

    /**
     * 深加工缺陷类别分布-柱状图
     *
     * @param dto dto
     * @return
     */
    QtChartBarVO deepDefectiveCategoryBar(QtChartParamDTO dto);

    /**
     * 根据条件获取缺陷扁平化数据
     *
     * @param dto dto
     * @return 分组数据
     */
    List<QtCheckTaskDetailReportVO> defectiveDataFlat(QtChartParamDTO dto);

    /**
     * 深加工缺陷类别分布-饼图
     * @param dto dto
     * @return vo
     */
    QtChartPieVO deepDefectiveCategoryPie(QtChartParamDTO dto);

    /**
     * 深加工缺陷类别分布-明细
     * @param dto 分页查询条件
     * @return 分页数据
     */
    Page<DeepDefectiveCategoryDetailVO> deepDefectiveCategoryDetail(QtChartParamDTO dto);

    /**
     * 深加工缺陷项分布
     * @param dto
     * @return
     */
    QtChartBarVO deepDefectiveItemBar(QtChartParamDTO dto);

    /**
     * 深加工基片缺陷分布
     * @param dto
     * @return
     */
    QtChartBarVO substrateItemBar(QtChartParamDTO dto);

    /**
     * 深加工缺陷项分布-饼图
     * @param dto
     * @return
     */
    QtChartPieVO deepDefectiveItemPie(QtChartParamDTO dto);

    /**
     * 基片缺陷项分布-饼图
     * @param dto
     * @return
     */
    QtChartPieVO substrateItemPie(QtChartParamDTO dto);

    /**
     * 基片缺陷明细
     * @param dto
     * @return
     */
    Page<DeepDefectiveCategoryDetailVO> substrateItemDetail(QtChartParamDTO dto);

    /**
     * 基片缺陷项趋势-折线图
     * @param dto
     * @return
     */
    QtLineChartBarVO substrateItemLine(QtChartParamDTO dto);

    /**
     * 深加工缺陷项趋势-折线图
     * @param dto
     * @return
     */
    QtLineChartBarVO deepItemLine(QtChartParamDTO dto);

    Page<DeepDefectiveCategoryDetailVO> deepDefectiveItemDetail(QtChartParamDTO dto);

    /**
     * 深加工类别趋势明细
     * @param dto
     * @return
     */
    Page<DeepDefectiveCategoryDetailVO> deepCategoryTrendDetail(QtChartParamDTO dto);

    /**
     * 深加工缺陷项趋势-折线图-明细
     * @param dto
     * @return
     */
    Page<DeepDefectiveCategoryDetailVO> deepItemTrendDetail(QtChartParamDTO dto);

    /**
     * 基片缺陷项趋势-折线图-明细
     * @param dto
     * @return
     */
    Page<DeepDefectiveCategoryDetailVO> substrateItemTrendDetail(QtChartParamDTO dto);

    /**
     * 深加工缺陷类别趋势-折线图
     * @param dto 筛选条件
     * @return vo
     */
    QtLineChartBarVO deepDefectiveCategoryLine(QtChartParamDTO dto);
}
