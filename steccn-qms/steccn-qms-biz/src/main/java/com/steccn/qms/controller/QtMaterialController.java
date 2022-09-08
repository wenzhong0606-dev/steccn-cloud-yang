package com.steccn.qms.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.common.vo.R;
import com.steccn.qms.domain.QtMaterial;
import com.steccn.qms.dto.QtCheckItemPageDTO;
import com.steccn.qms.dto.QtMaterialLinkCheckPlanDTO;
import com.steccn.qms.dto.QtMaterialPageDTO;
import com.steccn.qms.service.QtMaterialLinkCheckPlanService;
import com.steccn.qms.service.QtMaterialService;
import com.steccn.qms.vo.QtCheckItemListVO;
import com.steccn.qms.vo.QtMaterialPageVO;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

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
    private final QtMaterialService qtMaterialService;

    @PostMapping
    @ApiOperation(value = "物料配置")
    public R<Boolean> edit(@RequestBody QtMaterialLinkCheckPlanDTO dto) {
        return R.success(qtMaterialService.edit(dto));
    }

    @PostMapping("/list")
    @ApiOperation(value = "物料配置分页")
    public R<Page<QtMaterialPageVO>> pageList(@Valid @RequestBody QtMaterialPageDTO dto) {
        return R.success(qtMaterialService.pageList(dto));
    }

    @GetMapping("/{id}")
    @ApiOperation(value = "物料配置回显")
    public R<QtMaterial> info(@PathVariable("id") Long id) {
        return R.success(qtMaterialService.info(id));
    }
}
