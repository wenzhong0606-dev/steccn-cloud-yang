package com.steccn.platform.controller;

import cn.hutool.core.lang.Assert;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.common.dto.ValidList;
import com.steccn.common.vo.R;
import com.steccn.platform.dto.SysProductionCapacityAddDTO;
import com.steccn.platform.dto.SysProductionCapacityInfoDTO;
import com.steccn.platform.dto.SysProductionCapacityPageDTO;
import com.steccn.platform.service.SysProductionCapacityService;
import com.steccn.platform.vo.SysProductionCapacityInfoVO;
import com.steccn.platform.vo.SysProductionCapacityVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import java.util.List;

/**
 * @author yequan
 * @desc 标准产能
 * @date 2023/12/20
 */
@Api("标准产能")
@RestController
@RequiredArgsConstructor
@RequestMapping("/platform/sys/production/capacity")
public class SysProductionCapacityController {

    private final SysProductionCapacityService sysProductionCapacityService;

    @ApiOperation("标准产能列表")
    @PostMapping("/page")
    public R<Page<SysProductionCapacityVO>> capacityPage(@Valid @RequestBody SysProductionCapacityPageDTO dto) {
        return R.success(sysProductionCapacityService.capacityPage(dto));
    }

    @ApiOperation("标准产能新增")
    @PostMapping
    public R<Boolean> addCapacity(@Valid @RequestBody SysProductionCapacityAddDTO dto) {
        return R.success(sysProductionCapacityService.addCapacity(dto));
    }

    @ApiOperation("标准产能删除")
    @DeleteMapping("/{id}")
    public R<Boolean> deleteCapacity(@PathVariable("id") Integer id) {
        return R.success(sysProductionCapacityService.deleteCapacity(id));
    }

    @ApiOperation("标准产能查询")
    @GetMapping("/{id}")
    public R<List<SysProductionCapacityInfoVO>> info(@PathVariable("id") Integer id) {
        return R.success(sysProductionCapacityService.infoCapacity(id));
    }

    @ApiOperation("标准产能明细更新")
    @PutMapping
    public R<Boolean> updateCapacity(@Valid @RequestBody ValidList<SysProductionCapacityInfoDTO> dto) {
        Assert.notEmpty(dto.getList(), "明细集合不能为空");
        return R.success(sysProductionCapacityService.updateCapacity(dto.getList()));
    }
}
