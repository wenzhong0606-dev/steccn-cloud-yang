package com.steccn.wms.controller;


import com.steccn.common.vo.R;
import com.steccn.wms.service.WmsInventoryRecordService;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 仓库
 *
 * @author 孟乐
 * @date 2023年11月20日
 */
@RestController
@RequestMapping("/wms/report")
@RequiredArgsConstructor
@Slf4j
public class WmsReportController {

    private final WmsInventoryRecordService wmsInventoryRecordService;
    @GetMapping("/opening/inventory")
    @ApiOperation("获取期初库存")
    public R<BigDecimal>  openingInventory(@RequestParam("warehouseId") Long warehouseId, @RequestParam("date")LocalDate date){
        return R.success(wmsInventoryRecordService.openingInventory(warehouseId,date));
    }


    @GetMapping("/ending/inventory")
    @ApiOperation("获取期末库存")
    public R<BigDecimal>  endingInventory(@RequestParam("warehouseId") Long warehouseId, @RequestParam("date")LocalDate date){
        return R.success(wmsInventoryRecordService.endingInventory(warehouseId,date));
    }
}
