package com.steccn.wms.controller.feign;


import com.steccn.common.dto.TripleDTO;
import com.steccn.common.dto.TupleDTO;
import com.steccn.common.vo.R;
import com.steccn.pms.dto.RawMaterialReportDTO;
import com.steccn.wms.domain.WmsStorageReceipt;
import com.steccn.wms.domain.WmsStorageReceiptInfo;
import com.steccn.wms.dto.WmsDeliveryReceiptDTO;
import com.steccn.wms.dto.WmsWeighbridgeInfoDTO;
import com.steccn.wms.service.WmsStorageReceiptInfoService;
import com.steccn.wms.service.WmsStorageReceiptService;
import com.steccn.wms.service.WmsWeighbridgeInfoService;
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

    private final WmsWeighbridgeInfoService wmsWeighbridgeInfoService;

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
    @PostMapping("/purchase/quantity")
    public R<Map<String, BigDecimal>> purchaseQuantity(@RequestBody RawMaterialReportDTO dto){
        return R.success(wmsStorageReceiptInfoService.purchaseQuantity(dto));
    }

    @ApiOperation("根据时间获取类型为生产领料和生产退料的物料数量-用于碳核算")
    @PostMapping("/produce/consume/quantity")
    public R<Map<LocalDate, BigDecimal>> produceConsumeQuantity(@RequestBody TripleDTO<Long, LocalDate, LocalDate> dto) {
        return R.success(wmsStorageReceiptInfoService.produceConsumeQuantity(dto.getField1(), dto.getField2(), dto.getField3()));
    }

    @ApiOperation("地磅信息保存")
    @PostMapping("/weighbridge/save")
    public R<Boolean> weighbridgeSave(@RequestBody WmsWeighbridgeInfoDTO dto) {
        return R.success(wmsWeighbridgeInfoService.saveInfo(dto));
    }


    @ApiOperation("原片采购入库数")
    @PostMapping("/purchase/inventory/raw/glass")
    public R<BigDecimal> purchaseInventoryRawGlass(@RequestBody  TupleDTO<LocalDateTime,LocalDateTime> tupleDTO){
        return R.success(wmsStorageReceiptInfoService.purchaseInventoryRawGlass(tupleDTO));
    }
}
