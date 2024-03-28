package com.steccn.dp.controller.feign;

import com.steccn.common.dto.TupleDTO;
import com.steccn.common.vo.R;
import com.steccn.dp.domain.AdsWmsInventory;
import com.steccn.dp.dto.DpWmsDataDTO;
import com.steccn.dp.service.AdsWmsInventoryService;
import com.steccn.dp.vo.AdsWmsInventoryVO;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/feign/dp/wms/data")
public class RemoteDpWmsDataFeignController {
    private final AdsWmsInventoryService adsWmsInventoryService;


    @ApiOperation("获取期末库存数据")
    @PostMapping("/endingData")
    public R<List<AdsWmsInventoryVO>> endingData(@Valid @RequestBody DpWmsDataDTO dto) {
        return R.success(adsWmsInventoryService.endingData(dto));
    }
}
