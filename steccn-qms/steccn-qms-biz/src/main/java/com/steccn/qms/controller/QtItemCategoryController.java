package com.steccn.qms.controller;

import com.steccn.authserver.exception.BusinessException;
import com.steccn.authserver.util.SecurityUtils;
import com.steccn.authserver.util.SteccnUser;
import com.steccn.common.vo.R;
import com.steccn.qms.constant.QtManagerConstant;
import com.steccn.qms.dto.QtItemCategorySaveDTO;
import com.steccn.qms.service.QtItemCategoryService;
import com.steccn.qms.vo.QtItemCategoryVO;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * @desc 检验项，缺陷项类别管理
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/qms/category")
@Slf4j
public class QtItemCategoryController {

    private final QtItemCategoryService qtItemCategoryService;

    @ApiOperation("获取种类列表")
    @GetMapping("/list")
    public R<List<QtItemCategoryVO>> getCategoryList(@ApiParam("类别 0检验项 1缺陷项") @RequestParam("type") Integer type) {
        SteccnUser user = SecurityUtils.getUser();
        return R.success(qtItemCategoryService.getCategoryList(type));
    }

    @ApiOperation("保存检验项|缺陷项类别")
    @PostMapping
    public R<Boolean> saveCategory(@Valid @RequestBody QtItemCategorySaveDTO dto) {
        //1.检验类别类型是否正常
        if (!QtManagerConstant.QtItemCategoryEnum.categoryAll().contains(dto.getType())) {
            throw new BusinessException("类别参数不正确");
        }
        //2.插入数据库
        return R.success(qtItemCategoryService.saveCategory(dto));
    }

    @ApiOperation("检验项|缺陷项类别详情")
    @GetMapping("/{id}")
    public R<QtItemCategoryVO> info(@PathVariable("id") Integer id) {
        return R.success(qtItemCategoryService.info(id));
    }

    @ApiOperation("检验项|缺陷项类别删除")
    @DeleteMapping("/{id}")
    public R<Boolean> delete(@PathVariable("id") Integer id) {
        return R.success(qtItemCategoryService.delete(id));
    }
}
