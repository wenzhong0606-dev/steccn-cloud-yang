package com.steccn.pms.controller;

import com.steccn.common.annotation.SysLog;
import com.steccn.common.dto.SteccnTableDataDTO;
import com.steccn.common.vo.R;
import com.steccn.platform.vo.EqpSpareVO;
import com.steccn.pms.dto.PmsLaborStandardDTO;
import com.steccn.pms.service.PmsLaborProtectionStandardService;
import com.steccn.pms.vo.PmsLaborStandardVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

/**
 * 劳保控制器
 *
 * @author AndrewLuo
 * @date 2024/04/03
 */
@Api("劳保控制器")
@RestController
@RequiredArgsConstructor
@RequestMapping("/pms/labor")
public class PmsLaborController {
    private final PmsLaborProtectionStandardService pmsLaborProtectionStandardService;

    @PostMapping("/standard")
    @ApiOperation(value = "新建劳保标准")
    @SysLog("新建劳保标准")
    public R<Boolean> add(@Validated @RequestBody PmsLaborStandardDTO dto) {
        return R.success(pmsLaborProtectionStandardService.add(dto));
    }

    @PutMapping("/standard")
    @ApiOperation(value = "修改劳保标准")
    @SysLog("修改劳保标准")
    public R<Boolean> updateData(@Validated @RequestBody PmsLaborStandardDTO dto) {
        return R.success(pmsLaborProtectionStandardService.updateData(dto));
    }

    @GetMapping("/standard/{id}")
    @ApiOperation(value = "劳保标准详情")
    public R<PmsLaborStandardVO> info(@PathVariable("id")Integer id) {
        return R.success(pmsLaborProtectionStandardService.info(id));
    }

    @GetMapping("/standard/material")
    @ApiOperation(value = "获取物料下拉框")
    public R<List<EqpSpareVO>> material() {
        return R.success(pmsLaborProtectionStandardService.getAvailSpare());
    }

    @PostMapping("/standard/delete")
    @ApiOperation(value = "删除劳保标准")
    @SysLog("删除劳保标准")
    public R<Boolean> batchDeleteById(@RequestBody PmsLaborStandardDTO dto) {
        return R.success(pmsLaborProtectionStandardService.batchDeleteById(dto));
    }

    @GetMapping("/standard/list")
    @ApiOperation(value = "劳保标准列表")
    public R<SteccnTableDataDTO> listInfo() {
        return R.success(pmsLaborProtectionStandardService.listInfo());
    }
}
