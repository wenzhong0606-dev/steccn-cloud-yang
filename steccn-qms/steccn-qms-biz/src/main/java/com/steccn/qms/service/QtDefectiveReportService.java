package com.steccn.qms.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.qms.dto.QtChartParamDTO;
import com.steccn.qms.vo.DeepDefectiveCategoryDetailVO;
import com.steccn.qms.vo.QtChartBarVO;
import com.steccn.qms.vo.QtChartPieVO;
import com.steccn.qms.vo.QtCheckTaskDetailReportVO;
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
}
