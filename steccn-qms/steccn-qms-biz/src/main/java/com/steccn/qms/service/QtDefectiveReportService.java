package com.steccn.qms.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.common.dto.QtLineChartBarDTO;
import com.steccn.qms.dto.QtChartParamDTO;
import com.steccn.qms.vo.*;

import javax.servlet.http.HttpServletResponse;
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
    QtLineChartBarDTO deepDefectiveCategoryBar(QtChartParamDTO dto);

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
     * 深加工缺陷类别分布-下载
     *
     * @param dto
     * @param response
     */
    void deepDefectiveCategoryDownload(QtChartParamDTO dto, HttpServletResponse response, Integer type);

    /**
     * 深加工缺陷项分布
     * @param dto
     * @return
     */
    QtLineChartBarDTO deepDefectiveItemBar(QtChartParamDTO dto);

    /**
     * 深加工基片缺陷分布
     * @param dto
     * @return
     */
    QtLineChartBarDTO substrateItemBar(QtChartParamDTO dto);

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
    QtLineChartBarDTO substrateItemLine(QtChartParamDTO dto);

    /**
     * 深加工缺陷项趋势-折线图
     * @param dto
     * @return
     */
    QtLineChartBarDTO deepItemLine(QtChartParamDTO dto);

    Page<DeepDefectiveCategoryDetailVO> deepDefectiveItemDetail(QtChartParamDTO dto);

    /**
     * 深加工缺陷类别趋势-折线图
     * @param dto 筛选条件
     * @return vo
     */
    QtLineChartBarDTO deepDefectiveCategoryLine(QtChartParamDTO dto);

    /**
     * 机器采集的缺陷集合
     * @param dto dto
     * @return list
     */
    List<QtCheckTaskDetailReportVO> defectCollect(QtChartParamDTO dto);
}
