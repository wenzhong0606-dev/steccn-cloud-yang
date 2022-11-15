package com.steccn.qms.controller;

import com.steccn.common.vo.R;
import com.steccn.qms.dto.QtChatTargetValueDTO;
import com.steccn.qms.dto.QtSubOrDeepAcceptabilityDTO;
import com.steccn.qms.service.QtChartTargetValueService;
import com.steccn.qms.service.QtCheckTaskService;
import com.steccn.qms.vo.QtDeepOrSubAcceptabilityDetailVO;
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
 * 目标值controller
 *
 * @author AndrewLuo
 * @date 2022/11/08
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/qms/chart")
@Slf4j
public class QtChartTargetValueController {
    private final QtChartTargetValueService qtChartTargetValueService;
    @PostMapping("/value")
    @ApiOperation(value = "设置值")
    public R<Boolean> pageList(@Valid @RequestBody QtChatTargetValueDTO dto) {
        return R.success(qtChartTargetValueService.add(dto));
    }
}
