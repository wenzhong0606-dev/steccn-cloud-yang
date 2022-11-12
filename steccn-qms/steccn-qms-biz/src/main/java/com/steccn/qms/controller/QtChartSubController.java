package com.steccn.qms.controller;

import com.steccn.common.vo.R;
import com.steccn.qms.dto.QtChartDateRangeDTO;
import com.steccn.qms.dto.QtChartParamDTO;
import com.steccn.qms.service.QtChartGenerateService;
import com.steccn.qms.vo.QtChartBarVO;
import com.steccn.qms.vo.QtDefectChartVO;
import io.swagger.annotations.ApiModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 基片成品率图表相关controller
 *
 * @author AndrewLuo
 * @date 2022/11/12
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("qms/chart/department")
@ApiModel("基片成品率图表相关")
@Slf4j
public class QtChartSubController {
    private final QtChartGenerateService qtChartGenerateService;

    /**
     * 基片成品率概览领导驾驶舱
     * @param dto
     * @return
     */
    @PostMapping("/sub/overview")
    public R<QtDefectChartVO> generateSubstrateOverview(@RequestBody QtChartDateRangeDTO dto){
        return R.success(qtChartGenerateService.generateSubstrateOverview(dto));
    }

    /**
     * 基片成品率概览
     * @return
     */
    @GetMapping("/sub/overview")
    public R<QtDefectChartVO> generateSubstrateOverview(){
        return R.success(qtChartGenerateService.generateSubstrateOverview(null));
    }

    /**
     * 基片成品率趋势
     * @return
     */
    @PostMapping("/sub/yield")
    public R generateChatBarVO(@RequestBody QtChartParamDTO dto){
        if(dto.getType().equals(0)){
            return R.success(qtChartGenerateService.generateSubstrateBarTend(dto));
        }else {
            return R.success(qtChartGenerateService.generateSubstratePieTend(dto));
        }
    }
}
