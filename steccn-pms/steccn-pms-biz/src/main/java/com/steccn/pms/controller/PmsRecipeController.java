package com.steccn.pms.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.common.dto.BasePageDTO;
import com.steccn.common.vo.R;
import com.steccn.pms.service.PmsRecipeService;
import com.steccn.pms.vo.PmsRecipeVO;
import com.steccn.pms.vo.PmsRecipeVerHisVO;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    public R<PmsRecipeVO> getById(@PathVariable("id") Long id) {
        return R.success(pmsRecipeService.getPmsRecipeVO(id));
    }

    @ApiOperation(value = "获取版本分页信息")
    @PostMapping("/page")
    public R<Page<PmsRecipeVerHisVO>> getRecipePage(@RequestBody BasePageDTO dto) {
        return R.success(pmsRecipeService.pageList(dto));
    }
}
