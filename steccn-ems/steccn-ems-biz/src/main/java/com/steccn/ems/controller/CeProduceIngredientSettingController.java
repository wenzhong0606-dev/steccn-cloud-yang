package com.steccn.ems.controller;

import cn.hutool.core.lang.Assert;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.common.util.LocalDateUtil;
import com.steccn.common.valid.InsertGroup;
import com.steccn.common.valid.UpdateGroup;
import com.steccn.common.vo.R;
import com.steccn.ems.dto.CeProduceIngredientSettingDTO;
import com.steccn.ems.dto.CeProduceIngredientSettingPageDTO;
import com.steccn.ems.service.CeProduceIngredientSettingService;
import com.steccn.ems.vo.CeProduceIngredientSettingVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * @author yequan
 * @desc 生产原料设置控制器
 */
@Api("生产原料设置")
@RestController
@RequiredArgsConstructor
@RequestMapping("/ems/ce/produce/ingredient/setting")
public class CeProduceIngredientSettingController {

    private final CeProduceIngredientSettingService ceProduceIngredientSettingService;

    @PostMapping("/page")
    @ApiOperation(value = "查询生产原料设置分页列表")
    public R<Page<CeProduceIngredientSettingVO>> page(@RequestBody CeProduceIngredientSettingPageDTO dto) {
        return R.success(ceProduceIngredientSettingService.getPage(dto));
    }

    @DeleteMapping("/{id}")
    @ApiOperation(value = "根据id删除生产原料设置")
    public R<Boolean> delete(@PathVariable("id") Integer id) {
        return R.success(ceProduceIngredientSettingService.removeById(id));
    }

    @GetMapping("/{id}")
    @ApiOperation(value = "根据id查询生产原料设置")
    public R<CeProduceIngredientSettingVO> info(@PathVariable("id") Integer id) {
        return R.success(ceProduceIngredientSettingService.info(id));
    }

    @PostMapping
    @ApiOperation(value = "新增生产原料设置")
    public R<Boolean> add(@RequestBody @Validated(InsertGroup.class) CeProduceIngredientSettingDTO dto) {
        //日期必须符合start小于end
        Assert.isTrue(LocalDateUtil.le(dto.getValidStart(), dto.getValidEnd()), "开始日期必须小于结束日期");
        return R.success(ceProduceIngredientSettingService.add(dto));
    }

    @PutMapping
    @ApiOperation(value = "编辑生产原料设置")
    public R<Boolean> edit(@RequestBody @Validated(UpdateGroup.class) CeProduceIngredientSettingDTO dto) {
        //日期必须符合start小于end
        Assert.isTrue(LocalDateUtil.le(dto.getValidStart(), dto.getValidEnd()), "开始日期必须小于结束日期");
        return R.success(ceProduceIngredientSettingService.edit(dto));
    }
}
