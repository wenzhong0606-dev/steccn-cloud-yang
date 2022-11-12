package com.steccn.qms.controller;

import com.steccn.common.vo.R;
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

//    /**
//     * 基片成品率概览领导
//     * @param dto
//     * @return
//     */
//    @PostMapping("/sub/overview")
//    public R<QtDefectChartVO> generateSubstrateOverview(@RequestBody QtChartDateRangeDTO dto){
//        return R.success(qtChartGenerateService.generateSubstrateOverview(dto));
//    }
//
//    /**
//     * 基片成品率概览
//     * @return
//     */
//    @GetMapping("/sub/overview")
//    public R<QtDefectChartVO> generateSubstrateOverview(){
//        return R.success(qtChartGenerateService.generateSubstrateOverview(null));
//    }
//
//    /**
//     * 基片成品率趋势
//     * @return
//     */
//    @PostMapping("/sub/yield")
//    public R<QtChartBarVO> generateChatBarVO(@RequestBody QtChartParamDTO dto){
//        return R.success(qtChartGenerateService.generateSubstrateTend(dto));
//    }

    @GetMapping("/passRateOverview")
     public R<PassRateOverViewVO> passRateOverview(@RequestParam Integer type) {
        return R.success(qtChartDepartmentService.passRateOverview(type));
    }

}
