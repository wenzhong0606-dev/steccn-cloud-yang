package com.steccn.qms.controller;

import com.steccn.common.vo.R;
import com.steccn.qms.service.QtBackendService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vo.QtItemCategoryVO;

import java.util.List;

/**
 * @desc 质量管理后台配置
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/qms")
public class QtBackendController {

    private final QtBackendService qtBackendService;

    @ApiOperation("获取种类列表")
    @GetMapping("/category/list")
    public R<List<QtItemCategoryVO>> getCategoryList(@ApiParam("类别 0检验项 1缺陷项") @RequestParam("type") Integer type) {
        return R.success(qtBackendService.getCategoryList(type));
    }
}
