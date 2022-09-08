package com.steccn.qms.controller;

import com.steccn.common.vo.R;
import com.steccn.qms.dto.QtMaterialLinkCheckPlanDTO;
import com.steccn.qms.service.QtMaterialLinkCheckPlanService;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 物料配置controller
 *
 * @author AndrewLuo
 * @date 2022/09/08
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/qms/material")
@Slf4j
public class QtMaterialController {
    private final QtMaterialLinkCheckPlanService qtMaterialLinkCheckPlanService;

    @PostMapping
    @ApiOperation(value = "物料配置")
    public R<Boolean> add(@RequestBody QtMaterialLinkCheckPlanDTO dto) {
        return R.success(qtMaterialLinkCheckPlanService.add(dto));
    }
}
