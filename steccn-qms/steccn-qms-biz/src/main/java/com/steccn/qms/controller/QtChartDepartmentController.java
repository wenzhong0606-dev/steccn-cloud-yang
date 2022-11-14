package com.steccn.qms.controller;

import com.steccn.common.vo.R;
import com.steccn.qms.constant.QtManagerConstant;
import com.steccn.qms.dto.QtChartDateRangeDTO;
import com.steccn.qms.dto.QtChartParamDTO;
import com.steccn.qms.dto.QtChatDepartmentDTO;
import com.steccn.qms.service.QtChartDepartmentService;
import com.steccn.qms.service.QtChartGenerateService;
import com.steccn.qms.vo.QtChartBarVO;
import com.steccn.qms.vo.QtChartDefectVO;
import com.steccn.qms.vo.QtDefectChartVO;
import com.steccn.qms.vo.PassRateOverViewVO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("qms/chart/department")
@ApiModel("部门分析驾驶舱")
@Slf4j
public class QtChartDepartmentController {

    private final QtChartDepartmentService qtChartDepartmentService;

//    private final QtChartGenerateService qtChartGenerateService;

    @PostMapping("/info/jp")
    public R<QtDefectChartVO> getChartDepartmentInfo(@RequestBody QtChatDepartmentDTO dto) {
        return R.success(qtChartDepartmentService.getChartDepartmentInfo(dto));
    }

    @PostMapping("/info/sjg")
    public R<QtDefectChartVO> getChartDepartmentInfoSJG(@RequestBody QtChatDepartmentDTO dto) {
        return R.success(qtChartDepartmentService.getChartDepartmentInfoSJG(dto));
    }

   /* @PostMapping("/picture/jpfb")
    @ApiOperation(value = "基片缺陷分布柱状图和饼状图")
    public R<QtChartDefectVO> getPictureJPFB(QtChartParamDTO dto){
        return R.success(qtChartDepartmentService.getPictureJPFB(dto));
    }*/

    @ApiOperation("来料检卡片")
    @GetMapping("/llJhg")
     public R<PassRateOverViewVO> llJhg() {
        return R.success(qtChartDepartmentService.passRateOverview(QtManagerConstant.QtChatTargetValue.LLJHG.getCode()));
    }

    @ApiOperation("基片巡检卡片")
    @GetMapping("/jpxJhg")
    public R<PassRateOverViewVO> jpxJhg() {
        return R.success(qtChartDepartmentService.passRateOverview(QtManagerConstant.QtChatTargetValue.JPXJHG.getCode()));
    }

    @ApiOperation("深加工巡检卡片")
    @GetMapping("/sjgJhg")
    public R<PassRateOverViewVO> sjgJhg() {
        return R.success(qtChartDepartmentService.passRateOverview(QtManagerConstant.QtChatTargetValue.SJGXJHG.getCode()));
    }

}
