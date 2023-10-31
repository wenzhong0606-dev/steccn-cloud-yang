package com.steccn.qms.controller;

import com.steccn.common.dto.SteccnTableDataDTO;
import com.steccn.common.vo.R;
import com.steccn.qms.dto.QtDefectReportDTO;
import com.steccn.qms.service.QtDefectReportService;
import com.steccn.qms.vo.*;
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
 * @desc 1.2.6版本不良报表
 * @date 2023/7/11
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/qms/defect/report")
@Slf4j
public class QtDefectReportController {

    private final QtDefectReportService qtDefectReportService;

    @PostMapping("/finish")
    @ApiOperation(value = "成品率报表")
    public R<SteccnTableDataDTO> finishedRate(@Valid @RequestBody QtDefectReportDTO dto) {
        return R.success(qtDefectReportService.finishedRate(dto));
    }

    @PostMapping("/defect")
    @ApiOperation(value = "不良报表")
    public R<QtDefectReportVO> defectList(@Valid @RequestBody QtDefectReportDTO dto) {
        return R.success(qtDefectReportService.defectList(dto));
    }

    @PostMapping("/falling")
    @ApiOperation(value = "落板记录报表")
    public R<List<QtDefectFallingVO>> falling(@Valid @RequestBody QtDefectReportDTO dto) {
        return R.success(qtDefectReportService.falling(dto));
    }

    @PostMapping("/thick")
    @ApiOperation(value = "厚度报表")
    public R<QtCheckItemMultiAvgVO> thickAvg(@Valid @RequestBody QtDefectReportDTO dto) {
        return R.success(qtDefectReportService.thickAvg(dto));
    }

    @PostMapping("/rbw")
    @ApiOperation(value = "毛板宽报表")
    public R<QtCheckItemMultiAvgVO> rbwAvg(@Valid @RequestBody QtDefectReportDTO dto) {
        return R.success(qtDefectReportService.rbwAvg(dto));
    }

    @PostMapping("/roughness")
    @ApiOperation(value = "粗糙度报表")
    public R<QtCheckItemSingleAvgVO> roughness(@Valid @RequestBody QtDefectReportDTO dto) {
        return R.success(qtDefectReportService.roughness(dto));
    }

    @PostMapping("/transmittance")
    @ApiOperation(value = "透光率报表")
    public R<QtCheckItemTransmitVO> transmittance(@Valid @RequestBody QtDefectReportDTO dto) {
        return R.success(qtDefectReportService.transmittance(dto));
    }

    @PostMapping("/micro/bubble")
    @ApiOperation(value = "微气泡报表")
    public R<QtMicroBubbleVO> microBubble(@Valid @RequestBody QtDefectReportDTO dto) {
        return R.success(qtDefectReportService.microBubble(dto));
    }

    @PostMapping("/original/defect")
    @ApiOperation(value = "原片不良报表")
    public R<OriginalDefectVO> originalDefect(@Valid @RequestBody QtDefectReportDTO dto) {
        return R.success(qtDefectReportService.originalDefect(dto));
    }

    @PostMapping("/coating/film")
    @ApiOperation(value = "镀膜不良月报表")
    public R<CoatingFilmVO> coatingFilm(@Valid @RequestBody QtDefectReportDTO dto) {
        return R.success(qtDefectReportService.coatingFilm(dto));
    }

    @PostMapping("/defect/day")
    @ApiOperation(value = "不良日报表")
    public R<DefectDayVO> defectDay(@Valid @RequestBody QtDefectReportDTO dto) {
        return R.success(qtDefectReportService.defectDay(dto));
    }

    @PostMapping("/defect/week")
    @ApiOperation(value = "不良周报表")
    public R<List<DefectCountRowVO>> defectWeek(@Valid @RequestBody QtDefectReportDTO dto) {
        return R.success(qtDefectReportService.defectWeek(dto));
    }

    @PostMapping("/defect/month")
    @ApiOperation(value = "不良月报表")
    public R<List<DefectCountRowVO>> defectMonth(@Valid @RequestBody QtDefectReportDTO dto) {
        return R.success(qtDefectReportService.defectMonth(dto));
    }

}

