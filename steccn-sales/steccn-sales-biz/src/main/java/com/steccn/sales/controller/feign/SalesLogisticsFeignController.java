package com.steccn.sales.controller.feign;

import com.steccn.common.dto.TupleDTO;
import com.steccn.common.vo.R;
import com.steccn.sales.service.KsLogisticsOrderService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.time.LocalDate;
import java.math.BigDecimal;

/**
 * 类说明
 *
 * @author AndrewLuo
 * @date 2024/06/04
 */
@RestController
@Api("完工入库feign")
@RequestMapping("/feign/sales")
@RequiredArgsConstructor
@Slf4j
public class SalesLogisticsFeignController {
    private final KsLogisticsOrderService ksLogisticsOrderService;

    @PostMapping("/delivery/sum")
    @ApiOperation("物流单")
    R<BigDecimal> salesDeliverySum(@RequestBody TupleDTO<LocalDate, LocalDate> dto){
        return R.success(ksLogisticsOrderService.salesDeliverySum(dto));
    }
}
