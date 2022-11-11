package com.steccn.qms.controller;

import com.steccn.common.vo.R;
import com.steccn.qms.dto.QtChatDepartmentDTO;
import com.steccn.qms.service.QtChartDepartmentService;
import com.steccn.qms.vo.QtDefectChartVO;
import io.swagger.annotations.ApiModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("qms/chart/department")
@ApiModel("部门分析驾驶舱")
@Slf4j
public class QtChartDepartmentController {

    private final QtChartDepartmentService qtChartDepartmentService;

    @PostMapping("/info/jp")
    public R<QtDefectChartVO> getChartDepartmentInfo(@RequestBody QtChatDepartmentDTO dto) {
        return R.success(qtChartDepartmentService.getChartDepartmentInfo(dto));
    }

    @PostMapping("/info/sjg")
    public R<QtDefectChartVO> getChartDepartmentInfoSJG(@RequestBody QtChatDepartmentDTO dto) {
        return R.success(qtChartDepartmentService.getChartDepartmentInfoSJG(dto));
    }

}
