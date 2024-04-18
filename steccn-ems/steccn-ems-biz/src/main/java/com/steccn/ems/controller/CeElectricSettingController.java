package com.steccn.ems.controller;

import cn.hutool.core.lang.Assert;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.common.dto.BasePageDTO;
import com.steccn.common.util.LocalDateUtil;
import com.steccn.common.valid.InsertGroup;
import com.steccn.common.valid.UpdateGroup;
import com.steccn.common.vo.R;
import com.steccn.ems.dto.CeElectricSettingDTO;
import com.steccn.ems.service.CeElectricSettingService;
import com.steccn.ems.vo.CeElectricSettingVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * @author yequan
 * @desc 电力设置控制器
 */
@Api("电力设置")
@RestController
@RequiredArgsConstructor
@RequestMapping("/ems/ce/electric/setting")
public class CeElectricSettingController {

    private final CeElectricSettingService ceElectricSettingService;

    @PostMapping("/page")
    @ApiOperation(value = "查询电力设置分页列表")
    public R<Page<CeElectricSettingVO>> page(@RequestBody BasePageDTO dto) {
        return R.success(ceElectricSettingService.getPage(dto));
    }

    @DeleteMapping("/{id}")
    @ApiOperation(value = "根据id删除电力设置")
    public R<Boolean> delete(@PathVariable("id") Integer id) {
        return R.success(ceElectricSettingService.removeById(id));
    }

    @GetMapping("/{id}")
    @ApiOperation(value = "根据id查询电力设置")
    public R<CeElectricSettingVO> info(@PathVariable("id") Integer id) {
        return R.success(ceElectricSettingService.info(id));
    }

    @PostMapping
    @ApiOperation(value = "新增电力设置")
    public R<Boolean> add(@RequestBody @Validated(InsertGroup.class) CeElectricSettingDTO dto) {
        //日期必须符合start小于end
        Assert.isTrue(LocalDateUtil.le(dto.getValidStart(), dto.getValidEnd()), "开始日期必须小于结束日期");
        return R.success(ceElectricSettingService.add(dto));
    }

    @PutMapping
    @ApiOperation(value = "编辑电力设置")
    public R<Boolean> edit(@RequestBody @Validated(UpdateGroup.class) CeElectricSettingDTO dto) {
        //日期必须符合start小于end
        Assert.isTrue(LocalDateUtil.le(dto.getValidStart(), dto.getValidEnd()), "开始日期必须小于结束日期");
        return R.success(ceElectricSettingService.edit(dto));
    }
}
