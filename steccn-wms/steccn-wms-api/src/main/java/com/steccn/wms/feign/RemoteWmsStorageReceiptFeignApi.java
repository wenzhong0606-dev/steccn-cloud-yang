package com.steccn.wms.feign;


import com.steccn.common.constant.FeignConstant;
import com.steccn.common.vo.R;
import com.steccn.wms.dto.WmsDeliveryReceiptDTO;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

@FeignClient(contextId = "remoteWmsStorageReceiptFeignApi", value = FeignConstant.STECCN_WMS_SERVICE_NAME)
public interface RemoteWmsStorageReceiptFeignApi {

    @ApiOperation("冲销")
    @PostMapping("/feign/wms/storage/receipt/charge/against")
    R<WmsDeliveryReceiptDTO> chargeAgainstByCode(@ApiParam("入库单编码") @RequestParam("code") String code,@RequestParam("userName")String userName);

    @ApiOperation("根据报工集合获取供应商code")
    @PostMapping("/feign/wms/storage/receipt/rw/supplier")
    R<Map<Long, String>> rwLinkSupplier(@RequestBody Set<Long> rwIdSet);

    @ApiOperation("根据时间获取类型为采购入库的指定物料数量")
    @PostMapping("/feign/wms/storage/receipt/purchase/quantity")
    R<Map<String, BigDecimal>> purchaseQuantity(@RequestBody LocalDateTime startTime, LocalDateTime endTime, List<String> materialName);
}
