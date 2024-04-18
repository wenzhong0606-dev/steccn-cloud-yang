package com.steccn.ems.controller;

import cn.hutool.core.lang.Assert;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.common.util.LocalDateUtil;
import com.steccn.common.valid.InsertGroup;
import com.steccn.common.valid.UpdateGroup;
import com.steccn.common.vo.R;
import com.steccn.ems.dto.CeFossilFuelSettingDTO;
import com.steccn.ems.dto.CeFossilFuelSettingPageDTO;
import com.steccn.ems.service.CeFossilFuelSettingService;
import com.steccn.ems.vo.CeFossilFuelSettingVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * @author yequan
 * @desc 电力设置控制器
 */
@Api("化石燃料设置")
@RestController
@RequiredArgsConstructor
@RequestMapping("/ems/ce/fossil/fuel")
public class CeFossilFuelController {

    private final CeFossilFuelSettingService ceFossilFuelSettingService;

    @PostMapping("/page")
    @ApiOperation(value = "查询化石燃料设置分页列表")
    public R<Page<CeFossilFuelSettingVO>> page(@RequestBody CeFossilFuelSettingPageDTO dto) {
        return R.success(ceFossilFuelSettingService.getPage(dto));
    }

    @DeleteMapping("/{id}")
    @ApiOperation(value = "根据id删除化石燃料设置")
    public R<Boolean> delete(@PathVariable("id") Integer id) {
        return R.success(ceFossilFuelSettingService.removeById(id));
    }

    @GetMapping("/{id}")
    @ApiOperation(value = "根据id查询化石燃料设置")
    public R<CeFossilFuelSettingVO> info(@PathVariable("id") Integer id) {
        return R.success(ceFossilFuelSettingService.info(id));
    }

    @PostMapping
    @ApiOperation(value = "新增化石燃料设置")
    public R<Boolean> add(@RequestBody @Validated(InsertGroup.class) CeFossilFuelSettingDTO dto) {
        //日期必须符合start小于end
        Assert.isTrue(LocalDateUtil.le(dto.getValidStart(), dto.getValidEnd()), "开始日期必须小于结束日期");
        return R.success(ceFossilFuelSettingService.add(dto));
    }

    @PutMapping
    @ApiOperation(value = "编辑化石燃料设置")
    public R<Boolean> edit(@RequestBody @Validated(UpdateGroup.class) CeFossilFuelSettingDTO dto) {
        //日期必须符合start小于end
        Assert.isTrue(LocalDateUtil.le(dto.getValidStart(), dto.getValidEnd()), "开始日期必须小于结束日期");
        return R.success(ceFossilFuelSettingService.edit(dto));
    }
}
