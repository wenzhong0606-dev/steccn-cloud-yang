package com.steccn.qms.controller;

import com.steccn.common.vo.R;
import com.steccn.qms.domain.QtCheckTask;
import com.steccn.qms.service.QtCheckTaskCopyService;
import com.steccn.qms.vo.QtCheckTaskCopyVO;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/qms/check/taskcopy")
@Slf4j
public class QtCheckTaskCopyController {
private final QtCheckTaskCopyService qtCheckTaskCopyService;
    @GetMapping("/select/sampling")
    @ApiOperation(value = "获取每个样本取样时间的最早时间,返回任务id以及取样时间")
    public R<List<QtCheckTaskCopyVO>> getLocalDateList(){
        return R.success(qtCheckTaskCopyService.getLocalDateList());
    }
}
