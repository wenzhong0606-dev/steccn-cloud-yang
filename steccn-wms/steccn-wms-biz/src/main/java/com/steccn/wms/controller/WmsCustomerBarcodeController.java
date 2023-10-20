package com.steccn.wms.controller;


import com.steccn.common.annotation.SysLog;
import com.steccn.common.valid.InsertGroup;
import com.steccn.common.vo.R;
import com.steccn.wms.domain.WmsCustomerBarcode;
import com.steccn.wms.dto.WmsCustomerBarcodeDTO;
import com.steccn.wms.dto.WmsDeliveryReceiptDTO;
import com.steccn.wms.service.WmsCustomerBarcodeService;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 客户条码绑定
 *
 * @author Mongknife
 * @date 2023/10/20
 */
@RestController
@RequestMapping("/wms/customer/barcode")
@RequiredArgsConstructor
@Slf4j
public class WmsCustomerBarcodeController {

    private final WmsCustomerBarcodeService wmsCustomerBarcodeService;


    @ApiOperation("客户条码绑定")
    @PostMapping()
    @SysLog("客户条码绑定")
    public R<Boolean> add(@Validated(InsertGroup.class) @RequestBody WmsCustomerBarcodeDTO dto) {
        return R.success(wmsCustomerBarcodeService.add(dto));
    }

    @ApiOperation("批量更新客户条码")
    @PostMapping("/batch/update")
    public R<Boolean> batchUpdate(@RequestBody List<WmsCustomerBarcodeDTO> list){
        return R.success(wmsCustomerBarcodeService.batchUpdate(list));
    }
}
