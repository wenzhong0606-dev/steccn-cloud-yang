package com.steccn.qms.controller;

import com.steccn.common.valid.InsertGroup;
import com.steccn.common.vo.R;
import com.steccn.platform.vo.SysOfficeVO;
import com.steccn.qms.dto.QtCheckTaskDtDTO;
import com.steccn.qms.service.QtCheckTaskDtService;
import com.steccn.qms.vo.QtCheckTaskDtVO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 数字孪生控制器
 *
 * @author AndrewLuo
 * @date 2023/05/16
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("qms/check/dt")
@ApiModel("基片成品率图表相关")
@Slf4j
public class QtCheckTaskDtController {
    private final QtCheckTaskDtService qtCheckTaskDtService;

    @GetMapping("/office")
    @ApiOperation("获取所有基片车间信息")
    public R<List<SysOfficeVO>> getOfficeList() {
        return R.success(qtCheckTaskDtService.getOfficeList());
    }

    @PostMapping("/data")
    @ApiOperation("获取检验项信息")
    public R<Map<String, List<QtCheckTaskDtVO>>> getDtData(@Validated @RequestBody QtCheckTaskDtDTO dto) {
        return R.success(qtCheckTaskDtService.getDtData(dto));
    }
}
