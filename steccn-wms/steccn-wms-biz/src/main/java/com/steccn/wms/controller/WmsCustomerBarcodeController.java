package com.steccn.wms.controller;


import com.steccn.common.annotation.SysLog;
import com.steccn.common.valid.InsertGroup;
import com.steccn.common.vo.R;
import com.steccn.wms.domain.WmsCustomerBarcode;
import com.steccn.wms.dto.WmsCustomerBarcodeDTO;
import com.steccn.wms.dto.WmsDeliveryReceiptDTO;
import com.steccn.wms.service.WmsCustomerBarcodeService;
import com.steccn.wms.vo.WmsCustomerBarcodeVO;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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

    @ApiOperation("根据客户条码获取绑定的箱号以及生产批次号")
    @GetMapping("/caseNo")
    public R<WmsCustomerBarcodeVO> getCaseNoCustomerBarcode(@ApiParam("客户条码") @RequestParam("customerBarcode") String customerBarcode) {
        return R.success(wmsCustomerBarcodeService.CustomerBarcodeByCaseNO(customerBarcode));
    }

    @ApiOperation("根据箱号获取绑定的客户条码")
    @GetMapping()
    public R<String> getCustomerBarcodeByCaseNO(@ApiParam("箱号") @RequestParam("caseNo") String caseNo) {
        return R.success(wmsCustomerBarcodeService.getCustomerBarcodeByCaseNO(caseNo));
    }
}
