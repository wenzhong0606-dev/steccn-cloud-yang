package com.steccn.qms.controller;

import com.steccn.common.vo.R;
import com.steccn.qms.dto.QtChartDateRangeDTO;
import com.steccn.qms.dto.QtChatTargetValueDTO;
import com.steccn.qms.service.QtChartGenerateService;
import com.steccn.qms.vo.QtDefectChartVO;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 驾驶舱图表
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/qms/chart")
@Slf4j
public class QtChartController {

//    private final QtCheckTaskService qtCheckTaskService;
//
//    //TODO:深加工/基片巡检合格率趋势
//    @PostMapping("/subordeep/acceptability")
//    @ApiOperation(value = "深加工/基片巡检合格率趋势")
//    public R<QtChartAcceptabilityVO> getSubOrDeepProcess(@RequestBody QtSubOrDeepAcceptabilityDTO dto){
//        return R.success(qtCheckTaskService.getSubOrDeepProcess(dto));
//    }
    /**
     * 生成chartService
     */
    private final QtChartGenerateService qtChartGenerateService;

    @GetMapping("/sub/overview")
    @ApiOperation(value = "概览")
    public R<QtDefectChartVO> generateOverview() {
        return R.success(qtChartGenerateService.generateSubstrateOverview(null));
    }

    @PostMapping("/sub/overview")
    @ApiOperation(value = "概览")
    public R<QtDefectChartVO> generateOverview(@RequestBody QtChartDateRangeDTO dto) {
        return R.success(qtChartGenerateService.generateSubstrateOverview(dto));
    }

}
