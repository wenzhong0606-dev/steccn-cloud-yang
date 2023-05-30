package com.steccn.pms.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.common.annotation.SysLog;
import com.steccn.common.dto.BasePageDTO;
import com.steccn.common.vo.R;
import com.steccn.pms.dto.PmsRecipeDTO;
import com.steccn.pms.dto.PmsWetBasisWaterRateDTO;
import com.steccn.pms.service.PmsRecipeService;
import com.steccn.pms.vo.PmsRecipeVO;
import com.steccn.pms.vo.PmsRecipeVerHisVO;
import com.steccn.pms.vo.QmsRecipePreFillVO;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 配料管理控制器
 *
 * @author AndrewLuo
 * @date 2023/02/16
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/pms/recipe")
@Slf4j
public class PmsRecipeController {
    private final PmsRecipeService pmsRecipeService;

    @ApiOperation(value = "根据版本id获取版本详情")
    @GetMapping("/{id}")
    @SysLog("根据版本id获取版本详情")
    @PreAuthorize("hasAuthority('pms:recipe:view')")
    public R<PmsRecipeVO> getById(@PathVariable("id") Long id) {
        return R.success(pmsRecipeService.getPmsRecipeVO(id));
    }

    @ApiOperation(value = "获取版本分页信息")
    @PostMapping("/page")
    @PreAuthorize("hasAuthority('pms:recipe:view')")
    public R<Page<PmsRecipeVerHisVO>> getRecipePage(@RequestBody BasePageDTO dto) {
        return R.success(pmsRecipeService.pageList(dto));
    }

    @ApiOperation(value = "更新用量")
    @PreAuthorize("hasAuthority('pms:recipe:amount')")
    @SysLog("更新用量")
    @PostMapping("/amount")
    public R<Boolean> amount(@RequestBody PmsRecipeDTO dto) {
        return R.success(pmsRecipeService.publishQuantity(dto));
    }

    @ApiOperation(value = "更新配方含水率")
    @PreAuthorize("hasAuthority('pms:recipe:rate')")
    @SysLog("更新配方含水率")
    @PostMapping("/rate")
    public R<Boolean> rate(@RequestBody PmsWetBasisWaterRateDTO dto) {
        return R.success(pmsRecipeService.publishRate(dto));
    }

    @ApiOperation(value = "获取预填值")
    @GetMapping("/prefill")
    public R<QmsRecipePreFillVO> rate() {
        return R.success(pmsRecipeService.qmsPreFill());
    }

    @GetMapping("/refresh")
    public R<Boolean> refreshClerkQuantity() {
        return R.success(pmsRecipeService.refreshClerkQuantity());
    }
}
