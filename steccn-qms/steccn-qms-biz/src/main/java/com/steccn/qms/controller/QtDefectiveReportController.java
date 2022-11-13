package com.steccn.qms.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.common.vo.R;
import com.steccn.qms.dto.QtChartParamDTO;
import com.steccn.qms.service.QtDefectiveReportService;
import com.steccn.qms.vo.DeepDefectiveCategoryDetailVO;
import com.steccn.qms.vo.QtChartBarVO;
import com.steccn.qms.vo.QtChartPieVO;
import com.steccn.qms.vo.QtCheckTaskDetailReportVO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

/**
 * @author yequan
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/qms/defective/report")
@Slf4j
@ApiModel("缺陷项报表控制器")
public class QtDefectiveReportController {

    private final QtDefectiveReportService qtDefectiveReportService;

    @PostMapping("/deep/category/bar")
    @ApiOperation("深加工缺陷类别分布-柱状图")
    public R<QtChartBarVO> deepDefectiveCategoryBar(@Valid @RequestBody QtChartParamDTO dto) {
        return R.success(qtDefectiveReportService.deepDefectiveCategoryBar(dto));
    }

    @PostMapping("/deep/category/pie")
    @ApiOperation("深加工缺陷类别分布-饼图")
    public R<QtChartPieVO> deepDefectiveCategoryPie(@Valid @RequestBody QtChartParamDTO dto) {
        return R.success(qtDefectiveReportService.deepDefectiveCategoryPie(dto));
    }

    @PostMapping("/deep/category/detail")
    @ApiOperation("深加工缺陷类别分布-明细")
    public R<Page<DeepDefectiveCategoryDetailVO>> deepDefectiveCategoryDetail(@Valid @RequestBody QtChartParamDTO dto) {
        return R.success(qtDefectiveReportService.deepDefectiveCategoryDetail(dto));
    }

    /**
     * 深加工缺陷项分布
     * @param dto
     * @return
     */
    @PostMapping("/deep/category/info")
    @ApiOperation("深加工缺陷分布")
    public R<QtChartBarVO> deepDefectiveCategoryByInfo(@Valid @RequestBody QtChartParamDTO dto) {
        return R.success(qtDefectiveReportService.deepDefectiveCategoryByInfo(dto));
    }

    /**
     * 深加工缺陷项分布-饼图
     * @param dto
     * @return
     */
    @PostMapping("/deep/category/info/pie")
    @ApiOperation("深加工缺陷分布")
    public R<QtChartPieVO> deepDefectiveCategoryByInfoCategoryPie(@Valid @RequestBody QtChartParamDTO dto) {
        return R.success(qtDefectiveReportService.deepDefectiveCategoryByInfoCategoryPie(dto));
    }

    /**
     * 基片缺陷项分布-柱状图
     * @param dto
     * @return
     */
    @PostMapping("/deep/category/jpqx")
    @ApiOperation("基片缺陷项分布")
    public R<QtChartBarVO> deepDefectiveCategoryJPFB(@Valid @RequestBody QtChartParamDTO dto) {
        return R.success(qtDefectiveReportService.deepBasePlateCategory(dto));
    }

    /**
     * 基片缺陷项分布-饼图
     * @param dto
     * @return
     */
    @PostMapping("/deep/category/jpqx/pie")
    @ApiOperation("基片缺陷项分布")
    public R<QtChartPieVO> deepDefectiveCategoryJPie(@Valid @RequestBody QtChartParamDTO dto) {
        return R.success(qtDefectiveReportService.deepDefectiveCategoryJPie(dto));
    }

    /**
     * 深加工缺陷项类别趋势
     * @param dto
     * @return
     */
    @PostMapping("/deep/category/picture/info")
    @ApiOperation("深加工缺陷分布")
    public R<QtChartBarVO> deepDefectiveCategorySJG(@Valid @RequestBody QtChartParamDTO dto) {
        return R.success(qtDefectiveReportService.deepDefectiveCategory(dto));
    }

    /**
     * 深加工缺陷项趋势
     * @param dto
     * @return
     */
    @PostMapping("/deep/category/picture/sjg")
    @ApiOperation("深加工缺陷分布")
    public R<QtChartBarVO> deepDefectiveCategoryFB(@Valid @RequestBody QtChartParamDTO dto) {
        return R.success(qtDefectiveReportService.deepDefectiveCategory(dto));
    }
}
