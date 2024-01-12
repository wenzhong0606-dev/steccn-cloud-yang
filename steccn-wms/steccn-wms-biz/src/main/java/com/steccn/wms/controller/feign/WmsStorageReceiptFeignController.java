package com.steccn.wms.controller.feign;


import com.steccn.common.vo.R;
import com.steccn.wms.domain.WmsStorageReceipt;
import com.steccn.wms.domain.WmsStorageReceiptInfo;
import com.steccn.wms.dto.WmsDeliveryReceiptDTO;
import com.steccn.wms.service.WmsStorageReceiptInfoService;
import com.steccn.wms.service.WmsStorageReceiptService;
import com.steccn.wms.vo.WmsStorageReceiptVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@Api("入库管理feign")
@RequestMapping("/feign/wms/storage/receipt")
@RequiredArgsConstructor
@Slf4j
public class WmsStorageReceiptFeignController {

    private final WmsStorageReceiptService wmsStorageReceiptService;

    private final WmsStorageReceiptInfoService wmsStorageReceiptInfoService;

    @ApiOperation("冲销")
    @PostMapping("/charge/against")
    public R<WmsDeliveryReceiptDTO> chargeAgainstByCode(@ApiParam("入库单编码") @RequestParam("code") String code, @RequestParam("userName")String userName) {
        return R.success(wmsStorageReceiptService.chargeAgainstByCode(code,userName));
    }

    @ApiOperation("根据报工集合获取供应商code")
    @PostMapping("/rw/supplier")
    R<Map<Long, String>> rwLinkSupplier(@RequestBody Set<Long> rwIdSet) {
        return R.success(wmsStorageReceiptService.rwLinkSupplier(rwIdSet));
    }

    @ApiOperation("根据时间获取类型为采购入库的指定物料数量")
    @GetMapping("/purchase/quantity")
    public R<Map<String, BigDecimal>> purchaseQuantity(@RequestParam("startTime") LocalDateTime startTime,
                                                       @RequestParam("endTime") LocalDateTime endTime,
                                                       @RequestParam("materialName") List<String> materialName){
        return R.success(wmsStorageReceiptInfoService.purchaseQuantity(startTime,endTime,materialName));
    }

}
