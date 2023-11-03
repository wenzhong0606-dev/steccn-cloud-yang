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
}
