package com.steccn.sales.controller.feign;

import com.steccn.common.dto.TupleDTO;
import com.steccn.common.vo.R;
import com.steccn.sales.service.KsLogisticsOrderService;
import com.steccn.sales.service.ResUsersService;
import com.steccn.sales.vo.ResUserVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

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
    private final ResUsersService resUsersService;

    @PostMapping("/delivery/sum")
    @ApiOperation("物流单")
    R<BigDecimal> salesDeliverySum(@RequestBody TupleDTO<LocalDate, LocalDate> dto){
        return R.success(ksLogisticsOrderService.salesDeliverySum(dto));
    }

    @GetMapping("/user/info")
    @ApiOperation("获取销售用户信息")
    R<ResUserVO> salesUserInfo(@RequestParam String username){
        return R.success(resUsersService.userInfo(username));
    }
}
