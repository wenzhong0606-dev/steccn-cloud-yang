package com.steccn.ems.controller;

import cn.hutool.core.lang.Assert;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.common.util.LocalDateUtil;
import com.steccn.common.valid.InsertGroup;
import com.steccn.common.valid.UpdateGroup;
import com.steccn.common.vo.R;
import com.steccn.ems.dto.EpDiscountFactorDTO;
import com.steccn.ems.dto.EpDiscountFactorPageDTO;
import com.steccn.ems.service.EpDiscountFactorService;
import com.steccn.ems.vo.EpDiscountFactorVO;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * @author yequan
 * @desc 折标系数
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/ems/discount/factor")
@Slf4j
public class EpDiscountFactorController {

    private final EpDiscountFactorService epDiscountFactorService;

    @PostMapping("/page")
    @ApiOperation(value = "查询折标系数分页列表")
    public R<Page<EpDiscountFactorVO>> page(@RequestBody EpDiscountFactorPageDTO dto) {
        return R.success(epDiscountFactorService.getPage(dto));
    }

    @DeleteMapping("/{id}")
    @ApiOperation(value = "根据id删除折标系数")
    public R<Boolean> delete(@PathVariable("id") Integer id) {
        return R.success(epDiscountFactorService.removeById(id));
    }

    @GetMapping("/{id}")
    @ApiOperation(value = "根据id查询折标系数")
    public R<EpDiscountFactorVO> info(@PathVariable("id") Integer id) {
        return R.success(epDiscountFactorService.info(id));
    }

    @PostMapping
    @ApiOperation(value = "新增折标系数")
    public R<Boolean> add(@RequestBody @Validated(InsertGroup.class) EpDiscountFactorDTO dto) {
        //日期必须符合start小于end
        Assert.isTrue(LocalDateUtil.le(dto.getValidStart(), dto.getValidEnd()), "开始日期必须小于结束日期");
        return R.success(epDiscountFactorService.add(dto));
    }

    @PutMapping
    @ApiOperation(value = "编辑折标系数")
    public R<Boolean> edit(@RequestBody @Validated(UpdateGroup.class) EpDiscountFactorDTO dto) {
        //日期必须符合start小于end
        Assert.isTrue(LocalDateUtil.le(dto.getValidStart(), dto.getValidEnd()), "开始日期必须小于结束日期");
        return R.success(epDiscountFactorService.edit(dto));
    }
}
