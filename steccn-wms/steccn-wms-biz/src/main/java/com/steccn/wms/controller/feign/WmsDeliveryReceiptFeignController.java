package com.steccn.wms.controller.feign;

import com.steccn.common.dto.TripleDTO;
import com.steccn.common.vo.R;
import com.steccn.wms.dto.WmsDeliveryReceiptClientNumDTO;
import com.steccn.wms.service.WmsDeliveryReceiptService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@RestController
@Api("出库管理feign")
@RequestMapping("/feign/wms/delivery/receipt")
@RequiredArgsConstructor
@Slf4j
public class WmsDeliveryReceiptFeignController {

    private final WmsDeliveryReceiptService wmsDeliveryReceiptService;

    @ApiOperation("获取客户指定日期范围内的出库汇总")
    @PostMapping("/client/num")
    public R<List<WmsDeliveryReceiptClientNumDTO>> clientNum(@RequestBody TripleDTO<Set<Long>, LocalDate, LocalDate> dto) {
        return R.success(wmsDeliveryReceiptService.clientNum(dto.getField1(), dto.getField2(), dto.getField3()));
    }
}
