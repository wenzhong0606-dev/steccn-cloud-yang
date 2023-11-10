package com.steccn.wms.controller.feign;


import com.steccn.common.vo.R;
import com.steccn.wms.domain.WmsStorageReceipt;
import com.steccn.wms.dto.WmsDeliveryReceiptDTO;
import com.steccn.wms.service.WmsStorageReceiptService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Set;

@RestController
@Api("入库管理feign")
@RequestMapping("/feign/wms/storage/receipt")
@RequiredArgsConstructor
@Slf4j
public class WmsStorageReceiptFeignController {

    private final WmsStorageReceiptService wmsStorageReceiptService;

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

}
