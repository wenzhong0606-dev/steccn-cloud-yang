package com.steccn.wms.controller.feign;

import com.steccn.common.dto.TupleDTO;
import com.steccn.common.vo.R;
import com.steccn.wms.service.WmsTransferSlipService;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

/**
 * 调拨单明细
 *
 * @author Mongknife
 * @date 2022/12/7
 */
@RestController
@RequestMapping("/feign/wms/transfer/slip")
@RequiredArgsConstructor
@Slf4j
public class WmsTransferSlipFeignController {

    private final WmsTransferSlipService wmsTransferSlipService;

    @PostMapping(value = "/line/warehouse")
    @ApiOperation("根据线边仓获取领用量")
    R<BigDecimal> slipLineWarehouse(@RequestBody TupleDTO<LocalDate, Set<Integer>> tupleDTO) {
        return R.success(wmsTransferSlipService.slipLineWarehouse(tupleDTO.getField1(), tupleDTO.getField2()));
    }
}
