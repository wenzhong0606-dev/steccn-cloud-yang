package com.steccn.qms.controller;

import com.steccn.common.vo.R;
import com.steccn.qms.dto.QtDefectReportDTO;
import com.steccn.qms.service.QtDefectReportService;
import com.steccn.qms.vo.QtDefectFallingVO;
import com.steccn.qms.vo.QtDefectReportVO;
import com.steccn.qms.vo.QtThickAvgVO;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    @PostMapping("/defect")
    @ApiOperation(value = "不良报表")
    public R<QtDefectReportVO> defectList(@RequestBody QtDefectReportDTO dto) {
        return R.success(qtDefectReportService.defectList(dto));
    }

    @PostMapping("/falling")
    @ApiOperation(value = "落板记录报表")
    public R<List<QtDefectFallingVO>> falling(@RequestBody QtDefectReportDTO dto) {
        return R.success(qtDefectReportService.falling(dto));
    }

    @PostMapping("/thick")
    @ApiOperation(value = "厚度报表")
    public R<QtThickAvgVO> thickAvg(@RequestBody QtDefectReportDTO dto) {
        return R.success(qtDefectReportService.thickAvg(dto));
    }


}

