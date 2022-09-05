package com.steccn.qms.controller;

import com.steccn.authserver.exception.BusinessException;
import com.steccn.authserver.util.SecurityUtils;
import com.steccn.authserver.util.SteccnUser;
import com.steccn.common.vo.R;
import com.steccn.qms.service.QtItemCategoryService;
import constant.QtManagerConstant;
import dto.QtItemCategorySaveDTO;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import vo.QtItemCategoryVO;

import javax.validation.Valid;
import java.util.List;

/**
 * @desc 质量管理后台配置
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/qms")
@Slf4j
public class QtItemCategoryController {

    private final QtItemCategoryService qtItemCategoryService;

    @ApiOperation("获取种类列表")
    @GetMapping("/category/list")
    public R<List<QtItemCategoryVO>> getCategoryList(@ApiParam("类别 0检验项 1缺陷项") @RequestParam("type") Integer type) {
        SteccnUser user = SecurityUtils.getUser();
        return R.success(qtItemCategoryService.getCategoryList(type));
    }

    @ApiOperation("保存检验项|缺陷项目类别")
    @PostMapping("/category")
    public R<Boolean> saveCategory(@Valid @RequestBody QtItemCategorySaveDTO dto) {
        //1.检验类别类型是否正常
        if (!QtManagerConstant.QtItemCategoryEnum.categoryAll().contains(dto.getType())) {
            //获取枚举元素
            throw new BusinessException("类别参数不正确");
        }
        //2.插入数据库
        return qtItemCategoryService.saveCategory(dto);
    }
}
