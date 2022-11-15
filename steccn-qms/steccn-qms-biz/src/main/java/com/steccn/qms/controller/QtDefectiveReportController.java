package com.steccn.qms.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.common.vo.R;
import com.steccn.qms.dto.QtChartParamDTO;
import com.steccn.qms.service.QtDefectiveReportService;
import com.steccn.qms.vo.*;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import javax.validation.Valid;

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

    @PostMapping("/deep/category/line")
    @ApiOperation("深加工缺陷类别趋势-折线图")
    public R<QtLineChartBarVO> deepDefectiveCategoryLine(@Valid @RequestBody QtChartParamDTO dto) {
        return R.success(qtDefectiveReportService.deepDefectiveCategoryLine(dto));
    }

    @PostMapping("/deep/item/bar")
    @ApiOperation("深加工缺陷分布-柱状图")
    public R<QtChartBarVO> deepDefectiveCategoryByInfo(@Valid @RequestBody QtChartParamDTO dto) {
        return R.success(qtDefectiveReportService.deepDefectiveItem(dto));
    }

    @PostMapping("/deep/item/pie")
    @ApiOperation("深加工缺陷分布-饼图")
    public R<QtChartPieVO> deepDefectiveCategoryByInfoCategoryPie(@Valid @RequestBody QtChartParamDTO dto) {
        return R.success(qtDefectiveReportService.deepDefectiveItemPie(dto));
    }

    @PostMapping("/deep/item/detail")
    @ApiOperation("深加工缺陷分布-明细")
    public R<Page<DeepDefectiveCategoryDetailVO>> deepDefectiveCategoryInfoDetail(@Valid @RequestBody QtChartParamDTO dto) {
        return R.success(qtDefectiveReportService.deepDefectiveItemDetail(dto));
    }

    @PostMapping("/substrate/item/bar")
    @ApiOperation("基片缺陷项分布-柱状图")
    public R<QtChartBarVO> deepDefectiveCategoryJPFB(@Valid @RequestBody QtChartParamDTO dto) {
        return R.success(qtDefectiveReportService.substratePlateBar(dto));
    }

    @PostMapping("/substrate/item/pie")
    @ApiOperation("基片缺陷项分布-饼图")
    public R<QtChartPieVO> deepDefectiveCategoryJPie(@Valid @RequestBody QtChartParamDTO dto) {
        return R.success(qtDefectiveReportService.substrateCategoryJPie(dto));
    }

    @PostMapping("/substrate/item/detail")
    @ApiOperation("基片缺陷项分布-明细")
    public R<Page<DeepDefectiveCategoryDetailVO>> substrateDefectiveCategoryDetail(@Valid @RequestBody QtChartParamDTO dto) {
        return R.success(qtDefectiveReportService.substrateDefectiveCategoryDetail(dto));
    }

    @PostMapping("/deep/item/line")
    @ApiOperation("深加工缺陷项趋势-折线图")
    public R<QtLineChartBarVO> deepDefectiveCategoryInfoTrend(@Valid @RequestBody QtChartParamDTO dto) {
        return R.success(qtDefectiveReportService.deepDefectiveCategoryInfoTrend(dto));
    }

    @PostMapping("/substrate/item/line")
    @ApiOperation("基片缺陷项趋势-折线图")
    public R<QtLineChartBarVO> substrateDefectiveCategoryTrend(@Valid @RequestBody QtChartParamDTO dto) {
        return R.success(qtDefectiveReportService.substrateDefectiveCategoryTrend(dto));
    }

    @PostMapping("/deep/item/trend/detail")
    @ApiOperation("深加工缺陷项类别趋势-折线图-明细")
    public R<Page<DeepDefectiveCategoryDetailVO>> deepDefectiveCategoryTrendDetail(@Valid @RequestBody QtChartParamDTO dto) {
        return R.success(qtDefectiveReportService.deepDefectiveCategoryTrendDetail(dto));
    }

    @PostMapping("/deep/item/trend/detail")
    @ApiOperation("深加工缺陷项趋势-折线图-明细")
    public R<Page<DeepDefectiveCategoryDetailVO>> deepDefectiveCategoryInfoTrendDetail(@Valid @RequestBody QtChartParamDTO dto) {
        return R.success(qtDefectiveReportService.deepDefectiveCategoryInfoTrendDetail(dto));
    }

    @PostMapping("/substrate/item/trend/detail")
    @ApiOperation("基片缺陷项趋势-折线图-明细")
    public R<Page<DeepDefectiveCategoryDetailVO>> substrateDefectiveCategoryTrendDetail(@Valid @RequestBody QtChartParamDTO dto) {
        return R.success(qtDefectiveReportService.substrateDefectiveCategoryTrendDetail(dto));
    }
}
