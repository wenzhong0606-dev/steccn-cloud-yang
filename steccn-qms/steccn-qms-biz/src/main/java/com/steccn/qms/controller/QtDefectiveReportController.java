package com.steccn.qms.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.common.dto.QtLineChartBarDTO;
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

import javax.servlet.http.HttpServletResponse;
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
    public R<QtLineChartBarDTO> deepDefectiveCategoryBar(@Valid @RequestBody QtChartParamDTO dto) {
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

    @PostMapping("/deep/category/download")
    @ApiOperation("深加工缺陷类别分布-下载")
    public void deepDefectiveCategoryDownload(@Valid @RequestBody QtChartParamDTO dto, HttpServletResponse response) {
        qtDefectiveReportService.deepDefectiveCategoryDownload(dto,response);
    }

    @PostMapping("/deep/item/bar")
    @ApiOperation("深加工缺陷分布-柱状图")
    public R<QtLineChartBarDTO> deepDefectiveItemBar(@Valid @RequestBody QtChartParamDTO dto) {
        return R.success(qtDefectiveReportService.deepDefectiveItemBar(dto));
    }

    @PostMapping("/deep/item/pie")
    @ApiOperation("深加工缺陷分布-饼图")
    public R<QtChartPieVO> deepDefectiveItemPie(@Valid @RequestBody QtChartParamDTO dto) {
        return R.success(qtDefectiveReportService.deepDefectiveItemPie(dto));
    }

    @PostMapping("/deep/item/detail")
    @ApiOperation("深加工缺陷分布-明细")
    public R<Page<DeepDefectiveCategoryDetailVO>> deepDefectiveItemDetail(@Valid @RequestBody QtChartParamDTO dto) {
        return R.success(qtDefectiveReportService.deepDefectiveItemDetail(dto));
    }

    @PostMapping("/substrate/item/bar")
    @ApiOperation("基片缺陷项分布-柱状图")
    public R<QtLineChartBarDTO> substrateItemBar(@Valid @RequestBody QtChartParamDTO dto) {
        return R.success(qtDefectiveReportService.substrateItemBar(dto));
    }

    @PostMapping("/substrate/item/pie")
    @ApiOperation("基片缺陷项分布-饼图")
    public R<QtChartPieVO> substrateItemPie(@Valid @RequestBody QtChartParamDTO dto) {
        return R.success(qtDefectiveReportService.substrateItemPie(dto));
    }

    @PostMapping("/substrate/item/detail")
    @ApiOperation("基片缺陷项分布-明细")
    public R<Page<DeepDefectiveCategoryDetailVO>> substrateItemDetail(@Valid @RequestBody QtChartParamDTO dto) {
        return R.success(qtDefectiveReportService.substrateItemDetail(dto));
    }

    @PostMapping("/deep/category/line")
    @ApiOperation("深加工缺陷类别趋势-折线图")
    public R<QtLineChartBarDTO> deepDefectiveCategoryLine(@Valid @RequestBody QtChartParamDTO dto) {
        return R.success(qtDefectiveReportService.deepDefectiveCategoryLine(dto));
    }

    @PostMapping("/deep/item/line")
    @ApiOperation("深加工缺陷项趋势-折线图")
    public R<QtLineChartBarDTO> deepItemLine(@Valid @RequestBody QtChartParamDTO dto) {
        return R.success(qtDefectiveReportService.deepItemLine(dto));
    }

    @PostMapping("/substrate/item/line")
    @ApiOperation("基片缺陷项趋势-折线图")
    public R<QtLineChartBarDTO> substrateItemLine(@Valid @RequestBody QtChartParamDTO dto) {
        return R.success(qtDefectiveReportService.substrateItemLine(dto));
    }
}
