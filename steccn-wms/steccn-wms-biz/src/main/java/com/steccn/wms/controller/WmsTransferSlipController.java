package com.steccn.wms.controller;

import cn.hutool.core.lang.Assert;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.common.annotation.SysLog;
import com.steccn.common.valid.InsertGroup;
import com.steccn.common.valid.UpdateGroup;
import com.steccn.common.vo.R;
import com.steccn.wms.dto.WmsTransferSlipBatchDto;
import com.steccn.wms.dto.WmsTransferSlipDTO;
import com.steccn.wms.dto.WmsTransferSlipPageDTO;
import com.steccn.wms.service.WmsTransferSlipService;
import com.steccn.wms.vo.WmsTransferSlipVO;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 调拨单明细
 *
 * @author Mongknife
 * @date 2022/12/7
 */
@RestController
@RequestMapping("/wms/transfer/slip")
@RequiredArgsConstructor
@Slf4j
public class WmsTransferSlipController {

    private final WmsTransferSlipService wmsTransferSlipService;


    @ApiOperation("调拨单新增")
    @PostMapping()
    @SysLog("调拨单新增")
    public R<Boolean> add(@Validated(InsertGroup.class) @RequestBody WmsTransferSlipDTO dto) {
        return R.success(wmsTransferSlipService.add(dto));
    }

    @ApiOperation("调拨单编辑")
    @PutMapping
    @SysLog("调拨单编辑")
    public R<Boolean> edit(@Validated(UpdateGroup.class) @RequestBody WmsTransferSlipDTO dto) {
        return R.success(wmsTransferSlipService.edit(dto));
    }

    @ApiOperation("调拨单确认")
    @PutMapping("/affirm")
    @SysLog("调拨单确认")
    public R<Boolean> affirm(@RequestBody WmsTransferSlipDTO dto) {
        Assert.notNull(dto.getId(), "调拨单id不能为空");
        return R.success(wmsTransferSlipService.affirm(dto.getId()));
    }

    @ApiOperation("调拨单删除")
    @DeleteMapping()
    @SysLog("调拨单删除")
    public R<Boolean> delete(@RequestBody WmsTransferSlipDTO dto) {
        Assert.notNull(dto.getId(), "调拨单id不能为空");
        return R.success(wmsTransferSlipService.delete(dto.getId()));
    }

    @ApiOperation("调拨单详情")
    @GetMapping()
    public R<WmsTransferSlipVO> get(@ApiParam("调拨单id") @RequestParam("id") Long id){
        return R.success(wmsTransferSlipService.get(id));
    }

    @ApiOperation("调拨单分页列表")
    @PostMapping("/list")
    public R<Page<WmsTransferSlipVO>> pageList(@RequestBody WmsTransferSlipPageDTO dto) {
        return R.success(wmsTransferSlipService.pageList(dto));
    }

    @ApiOperation("调拨单详情")
    @GetMapping("/linkErp")
    public R<Boolean> linkErp(@ApiParam("调拨单id") @RequestParam("id") Long id){
        return R.success(wmsTransferSlipService.linkErp(id));
    }

    @ApiOperation("批量调拨玻璃物料")
    @PostMapping("/batch")
    public R<Boolean> batch(@RequestBody WmsTransferSlipBatchDto dto){
        return R.success(wmsTransferSlipService.batch(dto));

    }
}
