package com.steccn.wms.controller.feign;


import com.steccn.common.vo.R;
import com.steccn.wms.domain.WmsCustomerBarcode;
import com.steccn.wms.dto.WmsCustomerBarcodeDTO;
import com.steccn.wms.dto.WmsDeliveryReceiptDTO;
import com.steccn.wms.service.WmsCustomerBarcodeService;
import com.steccn.wms.service.WmsStorageReceiptService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Api("入库管理feign")
@RequestMapping("/feign/wms/customer/barcode")
@RequiredArgsConstructor
@Slf4j
public class WmsCustomerBarcodeFeignController {

    private final WmsCustomerBarcodeService wmsCustomerBarcodeService;

    @ApiOperation("根据箱号获取绑定的客户条码")
    @GetMapping("/caseNo")
    public R<String> getCustomerBarcodeByCaseNO(@ApiParam("箱号") @RequestParam("caseNo") String caseNo) {
        return R.success(wmsCustomerBarcodeService.getCustomerBarcodeByCaseNO(caseNo));
    }

    @ApiOperation("批量更新客户条码")
    @PostMapping("/batch/update")
    public R<Boolean> batchUpdate(@RequestBody List<WmsCustomerBarcodeDTO> list){
        return R.success(wmsCustomerBarcodeService.batchUpdate(list));
    }

    @GetMapping("/list/barcode")
    @ApiOperation("根据客户条码查询箱号")
    public R<List<String>> listByCustomerBarcode(@ApiParam("客户条码") @RequestParam("customerBarcode") String customerBarcode){
        return R.success(wmsCustomerBarcodeService.listByCustomerBarcode(customerBarcode));
    }

}
