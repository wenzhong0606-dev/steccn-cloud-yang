package com.steccn.qms.controller;

import com.steccn.common.vo.R;
import com.steccn.qms.dto.QtSubOrDeepAcceptabilityDTO;
import com.steccn.qms.service.QtCheckTaskService;
import com.steccn.qms.vo.QtChartAcceptabilityVO;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
