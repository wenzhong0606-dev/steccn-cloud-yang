package com.steccn.sales.controller.feign;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.common.dto.TupleDTO;
import com.steccn.common.vo.R;
import com.steccn.sales.dto.KsLogisticsOrderDTO;
import com.steccn.sales.service.KsLogisticsOrderService;
import com.steccn.sales.service.ResUsersService;
import com.steccn.sales.vo.ResUserVO;
import com.steccn.wms.dto.WmsSignFeedbackDTO;
import com.steccn.wms.vo.WmsSignFeedbackVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDate;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;

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
    R<ResUserVO> salesUserInfo(@RequestParam("name") String username){
        return R.success(resUsersService.userInfo(username));
    }

    @PostMapping("/update")
    @ApiOperation("修改物流单信息")
    R<Boolean> update(@RequestBody KsLogisticsOrderDTO dto){
        return R.success(ksLogisticsOrderService.updateOrderByOrderCode(dto));
    }


    @PostMapping("/sales/order/car/code")
    @ApiOperation("获取物流单车牌号")
    R<Map<String,String>> salesOrderCarCode (@RequestBody  KsLogisticsOrderDTO dto){
        return R.success(ksLogisticsOrderService.salesOrderCarCode(dto.getSalesOrders()));
    }

    @PostMapping("/sales/order/inWms")
    @ApiOperation("设置wms系统中已经存在物流单")
    R<Boolean> setInWmsColumn (@RequestBody  KsLogisticsOrderDTO dto){
        return R.success(ksLogisticsOrderService.setInWmsColumn(dto.getSalesOrders()));
    }


    @PostMapping("/order/company")
    @ApiOperation("获取物流单公司")
    R<String> salesOrderCompany(@RequestBody  KsLogisticsOrderDTO dto){
        return R.success(ksLogisticsOrderService.salesOrderCompany(dto.getName()));
    }

    @PostMapping("/order/company/id")
    @ApiOperation("获取物流单公司")
    R<Integer> salesOrderCompanyId(@RequestBody  KsLogisticsOrderDTO dto){
        return R.success(ksLogisticsOrderService.salesOrderCompanyId(dto.getName()));
    }

    @PostMapping("/order/logistics/info")
    @ApiOperation("获取物流单详情")
    R<Page<WmsSignFeedbackVO>> signFeedback(@RequestBody WmsSignFeedbackDTO dto){
        return R.success(ksLogisticsOrderService.getSignFeedback(dto));
    }

}
