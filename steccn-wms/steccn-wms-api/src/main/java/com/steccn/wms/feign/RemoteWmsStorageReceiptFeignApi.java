package com.steccn.wms.feign;


import com.steccn.common.constant.FeignConstant;
import com.steccn.common.dto.TripleDTO;
import com.steccn.common.dto.TupleDTO;
import com.steccn.common.vo.R;
import com.steccn.pms.dto.RawMaterialReportDTO;
import com.steccn.wms.dto.WmsDeliveryReceiptDTO;
import com.steccn.wms.dto.WmsWeighbridgeInfoDTO;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.time.LocalDate;
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
    R<Map<String, BigDecimal>> purchaseQuantity(@RequestBody RawMaterialReportDTO dto);

    @ApiOperation("根据时间获取类型为生产领料和生产退料的物料数量-用于碳核算")
    @PostMapping("/feign/wms/storage/receipt/produce/consume/quantity")
    R<Map<LocalDate, BigDecimal>> produceConsumeQuantity(@RequestBody TripleDTO<Long, LocalDate, LocalDate> dto);

    @ApiOperation("地磅信息保存")
    @PostMapping("/feign/wms/storage/receipt/weighbridge/save")
    R<Boolean> weighbridgeSave(@RequestBody WmsWeighbridgeInfoDTO dto);


    @ApiOperation("原片采购入库数")
    @PostMapping("/feign/wms/storage/receipt/purchase/inventory/raw/glass")
    R<BigDecimal> purchaseInventoryRawGlass(@RequestBody TupleDTO<LocalDateTime,LocalDateTime> tupleDTO);
}
