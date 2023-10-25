package com.steccn.wms.controller;

import cn.hutool.core.lang.Assert;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.common.annotation.SysLog;
import com.steccn.common.valid.InsertGroup;
import com.steccn.common.valid.UpdateGroup;
import com.steccn.common.vo.R;
import com.steccn.wms.domain.WmsDeliveryReceipt;
import com.steccn.wms.domain.WmsStorageReceipt;
import com.steccn.wms.dto.WmsDeliveryReceiptDTO;
import com.steccn.wms.dto.WmsDeliveryReceiptFlagInfoDto;
import com.steccn.wms.dto.WmsDeliveryReceiptPageDTO;
import com.steccn.wms.dto.WmsStorageReceiptFlagInfoDTO;
import com.steccn.wms.service.WmsDeliveryReceiptService;
import com.steccn.wms.vo.*;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

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
    public R<WmsDeliveryReceiptVO> get(@ApiParam("出库单id") @RequestParam("id") Long id){
        return R.success(wmsDeliveryReceiptService.get(id));
    }

    @ApiOperation("扫描 根据箱号获取库存信息")
    @GetMapping("/api/getCaseNoByScanInventoryInfo")
    public R<WmsScanInventoryVO> getCaseNoByScanInventoryInfo(@ApiParam("箱号") @RequestParam("caseNo") String caseNo){
        if(StringUtils.isBlank(caseNo)) return R.failure("箱号不可为空");
        return  wmsDeliveryReceiptService.getCaseNoByScanInventoryInfo(caseNo);
    }

    @ApiOperation("扫描出库")
    @PostMapping("/api/scanDeliveryReceipt")
    public R<String> scanDeliveryReceipt(@RequestBody Map<String, Object> paramMap){
        return  wmsDeliveryReceiptService.scanDeliveryReceipt(paramMap);
    }

    @ApiOperation("获取销售发货状态标识")
    @GetMapping("/sell/flag")
    public R<WmsDeliverySellFlagVO> sellFlag(@ApiParam("物流单号") @RequestParam("number") String number){
        return R.success(wmsDeliveryReceiptService.sellFlag(number));
    }


    @ApiOperation("销售出库获取箱号出库单标识相关内容")
    @PostMapping("/delivery/flag/info")
    public R<WmsDeliveryFlagVO> deliveryFlagInfo(@RequestBody WmsDeliveryReceiptFlagInfoDto dto){
        return R.success(wmsDeliveryReceiptService.deliveryFlagInfo(dto));
    }

    @ApiOperation("销售出库通过物流单号获取物料明细")
    @GetMapping("/delivery/sell/material/info")
    public R<WmsDeliverySellInfoVO> deliverySellMaterialInfo(@ApiParam("物流单号") @RequestParam("number")String number){
        return R.success(wmsDeliveryReceiptService.deliverySellMaterialInfo(number));
    }

    @ApiOperation("销售发货")
    @PostMapping("/sell")
    public R<String> sell(@RequestBody WmsDeliveryReceiptDTO wmsDeliveryReceiptDTO){
        String code = wmsDeliveryReceiptService.sell(wmsDeliveryReceiptDTO);
        WmsDeliveryReceipt wmsDeliveryReceipt = wmsDeliveryReceiptService.getOne(new LambdaQueryWrapper<WmsDeliveryReceipt>().eq(WmsDeliveryReceipt::getCode, code), false);
        Assert.notNull(wmsDeliveryReceipt, "出库单为空");
        wmsDeliveryReceiptService.affirm(wmsDeliveryReceipt.getId());
        //同步数据到销售系统
        wmsDeliveryReceiptService.logistics(wmsDeliveryReceipt);
        return R.success(code);
    }

}
