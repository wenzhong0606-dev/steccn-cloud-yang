package com.steccn.platform.controller;

import com.setccn.platfrom.vo.SysDictVO;
import com.steccn.common.vo.R;
import com.steccn.platform.service.SysDictService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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
}
