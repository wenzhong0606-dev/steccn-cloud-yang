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
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
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

    @ApiOperation("根据时间获取调拨单源仓库为主要材料库的指定物料数量")
    @GetMapping("/material/depot/quantity")
    R<Map<String, BigDecimal>> materialDepot(@RequestParam("startTime") LocalDateTime startTime,
                                             @RequestParam("endTime") LocalDateTime endTime,
                                             @RequestParam("materialName") List<String> materialName){
        return R.success(wmsTransferSlipService.materialDepot(startTime,endTime,materialName));
    };
}
