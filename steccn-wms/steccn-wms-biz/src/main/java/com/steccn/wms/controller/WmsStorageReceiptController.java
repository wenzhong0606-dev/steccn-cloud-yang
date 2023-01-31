package com.steccn.wms.controller;

import cn.hutool.core.lang.Assert;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.common.annotation.SysLog;
import com.steccn.common.valid.InsertGroup;
import com.steccn.common.valid.UpdateGroup;
import com.steccn.common.vo.R;
import com.steccn.pms.dto.PmExcelDTO;
import com.steccn.pms.vo.ProducePlanImportVO;
import com.steccn.wms.dto.WmsDeliveryReceiptDTO;
import com.steccn.wms.dto.WmsDeliveryReceiptPageDTO;
import com.steccn.wms.dto.WmsStorageReceiptDTO;
import com.steccn.wms.dto.WmsStorageReceiptPageDTO;
import com.steccn.wms.service.WmsStorageReceiptService;
import com.steccn.wms.vo.WmsDeliveryReceiptVO;
import com.steccn.wms.vo.WmsStorageReceiptVO;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

/**
 * 入库凭证控制器
 *
 * @author Mongknife
 * @date 2022/12/9
 */

@RestController
@RequestMapping("/wms/storage/receipt")
@RequiredArgsConstructor
@Slf4j
public class WmsStorageReceiptController {

    private final WmsStorageReceiptService wmsStorageReceiptService;

    @ApiOperation("入库凭证分页列表")
    @PostMapping("/list")
    public R< Page<WmsStorageReceiptVO>> pageList(@RequestBody WmsStorageReceiptPageDTO dto) {
        return R.success(wmsStorageReceiptService.pageList(dto));
    }

    @ApiOperation("新增")
    @PostMapping()
    @SysLog("入库凭证新增")
    public R<Boolean> add(@Validated(InsertGroup.class) @RequestBody WmsStorageReceiptDTO dto) {
        return R.success(wmsStorageReceiptService.add(dto));
    }

    @ApiOperation("入库凭证编辑")
    @PutMapping
    @SysLog("入库凭证编辑")
    public R<Boolean> edit(@Validated(UpdateGroup.class) @RequestBody WmsStorageReceiptDTO dto) {
        return R.success(wmsStorageReceiptService.edit(dto));
    }

    @ApiOperation("入库凭证确认")
    @PutMapping("/affirm")
    @SysLog("入库凭证确认")
    public R<Boolean> affirm(@RequestBody WmsStorageReceiptDTO dto) {
        Assert.notNull(dto.getId(), "入库凭证id不能为空");
        return R.success(wmsStorageReceiptService.affirm(dto.getId()));
    }

    @ApiOperation("入库凭证删除")
    @DeleteMapping()
    @SysLog("入库凭证删除")
    public R<Boolean> delete(@RequestBody WmsStorageReceiptDTO dto) {
        Assert.notNull(dto.getId(), "入库凭证id不能为空");
        return R.success(wmsStorageReceiptService.delete(dto.getId()));
    }

    @ApiOperation("入库凭证详情")
    @GetMapping()
    public R<WmsStorageReceiptVO> get(@ApiParam("入库单id") @RequestParam("id") Long id){
        return R.success(wmsStorageReceiptService.get(id));
    }

    @ApiModelProperty("Excel导入")
    @PostMapping("/excelImportData")
    public R<Boolean> excelImportData(@Validated(InsertGroup.class) PmExcelDTO dto) throws IOException {
        return R.success(wmsStorageReceiptService.excelImportData(dto));
    }

}
