package com.steccn.wms.controller;

import cn.hutool.core.lang.Assert;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.common.annotation.SysLog;
import com.steccn.common.valid.InsertGroup;
import com.steccn.common.valid.UpdateGroup;
import com.steccn.common.vo.R;
import com.steccn.wms.dto.WmsDeliveryReceiptDTO;
import com.steccn.wms.dto.WmsDeliveryReceiptPageDTO;
import com.steccn.wms.service.WmsDeliveryReceiptService;
import com.steccn.wms.vo.WmsDeliveryReceiptVO;
import com.steccn.wms.vo.WmsTransferSlipVO;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 出库凭证控制器
 *
 * @author Mongknife
 * @date 2022/12/9
 */
@RestController
@RequestMapping("/wms/delivery/receipt")
@RequiredArgsConstructor
@Slf4j
public class WmsDeliveryReceiptController {

    private final WmsDeliveryReceiptService wmsDeliveryReceiptService;

    @ApiOperation("出库凭证分页列表")
    @PostMapping("/list")
    public R<Page<WmsDeliveryReceiptVO>> pageList(@RequestBody WmsDeliveryReceiptPageDTO dto) {
        return R.success(wmsDeliveryReceiptService.pageList(dto));
    }

    @ApiOperation("新增")
    @PostMapping()
    @SysLog("出库凭证新增")
    public R<Boolean> add(@Validated(InsertGroup.class) @RequestBody WmsDeliveryReceiptDTO dto) {
        return R.success(wmsDeliveryReceiptService.add(dto));
    }

    @ApiOperation("出库凭证编辑")
    @PutMapping
    @SysLog("出库凭证编辑")
    public R<Boolean> edit(@Validated(UpdateGroup.class) @RequestBody WmsDeliveryReceiptDTO dto) {
        return R.success(wmsDeliveryReceiptService.edit(dto));
    }

    @ApiOperation("出库凭证确认")
    @PutMapping("/affirm")
    @SysLog("出库凭证确认")
    public R<Boolean> affirm(@RequestBody WmsDeliveryReceiptDTO dto) {
        Assert.notNull(dto.getId(), "出库凭证id不能为空");
        return R.success(wmsDeliveryReceiptService.affirm(dto.getId()));
    }

    @ApiOperation("出库凭证删除")
    @DeleteMapping()
    @SysLog("出库凭证删除")
    public R<Boolean> delete(@RequestBody WmsDeliveryReceiptDTO dto) {
        Assert.notNull(dto.getId(), "出库凭证id不能为空");
        return R.success(wmsDeliveryReceiptService.delete(dto.getId()));
    }

    @ApiOperation("出库凭证详情")
    @GetMapping()
    public R<WmsDeliveryReceiptVO> get(@ApiParam("调拨单id") @RequestParam("id") Long id){
        return R.success(wmsDeliveryReceiptService.get(id));
    }


}
