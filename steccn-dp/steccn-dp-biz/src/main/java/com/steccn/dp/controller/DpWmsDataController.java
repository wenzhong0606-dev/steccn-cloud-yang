package com.steccn.dp.controller;


import com.steccn.common.dto.TupleDTO;
import com.steccn.common.vo.R;
import com.steccn.dp.domain.AdsWmsInventory;
import com.steccn.dp.dto.DpWmsDataDTO;
import com.steccn.dp.service.AdsWmsInventoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.math.BigDecimal;

@Api("仓储数据")
@RestController
@RequiredArgsConstructor
@RequestMapping("/dp/wms/data")
public class DpWmsDataController {
    private final AdsWmsInventoryService adsWmsInventoryService;

    @PostMapping("/openning")
    @ApiOperation(value = "期初-片/平方米")
    public R<TupleDTO<BigDecimal,BigDecimal>> openning(@Valid @RequestBody DpWmsDataDTO dto) {
        return R.success(adsWmsInventoryService.openning(dto));
    }


    @PostMapping("/ending")
    @ApiOperation(value = "期末-片/平方米")
    public R<TupleDTO<BigDecimal,BigDecimal>> ending(@Valid @RequestBody DpWmsDataDTO dto) {
        return R.success(adsWmsInventoryService.ending(dto));
    }
}
