package com.steccn.platform.controller;

import cn.hutool.core.collection.CollUtil;
import com.google.common.collect.Maps;
import com.steccn.platform.vo.SysDictVO;
import com.steccn.common.vo.R;
import com.steccn.platform.service.SysDictService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * @desc 数据字典
 */
@Api("数据字典控制器")
@RestController
@RequiredArgsConstructor
@RequestMapping("/platform/sys/dict")
public class SysDictController {

    private final SysDictService sysDictService;

    @GetMapping
    @ApiOperation("数据字典")
    public R<List<SysDictVO>> list(@ApiParam("类型编码") @RequestParam("type") String type) {
        return R.success(sysDictService.sysList(type));
    }

    @GetMapping("/filter")
    @ApiOperation("数据字典-过滤版")
    public R<List<SysDictVO>> listFilter(@ApiParam("类型编码") @RequestParam("type") String type, @ApiParam("名称过滤") @RequestParam(value = "name",required = false) String name) {
        return R.success(sysDictService.sysListFilter(type, name));
    }
}
