package com.steccn.sales.feign;

import com.steccn.common.constant.FeignConstant;
import com.steccn.common.dto.TupleDTO;
import com.steccn.common.vo.R;
import com.steccn.sales.dto.KsLogisticsOrderDTO;
import com.steccn.sales.vo.ResUserVO;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 类说明
 *
 * @author AndrewLuo
 * @date 2024/06/04
 */
@FeignClient(contextId = "RemoteSalesFeignApi", value = FeignConstant.STECCN_SALES_NAME)
public interface RemoteSaleFeignApi {

    /**
     * 根据班次日期 查询当前班次日期的所有报工记录
     * @param dto f1 f2
     * @return
     */
    @PostMapping("/feign/sales/delivery/sum")
    @ApiOperation("根据班次日期 查询当前班次日期的所有报工记录")
    R<BigDecimal> salesDeliverySum(@RequestBody TupleDTO<LocalDate, LocalDate> dto);

    @GetMapping("/feign/sales/user/info")
    @ApiOperation("获取销售用户信息")
    R<ResUserVO> salesUserInfo(@RequestParam("name") String username);


    @PostMapping("/feign/sales/update")
    @ApiOperation("修改物流单信息")
    R<Boolean> update(@RequestBody KsLogisticsOrderDTO dto);

    @PostMapping("/feign/sales/sales/order/car/code")
    @ApiOperation("获取物流单车牌号")
    R<Map<String,String>> salesOrderCarCode (@RequestBody  KsLogisticsOrderDTO dto);

    @PostMapping("/feign/sales/order/company")
    @ApiOperation("获取物流单公司")
    R<String> salesOrderCompany(@RequestBody  KsLogisticsOrderDTO dto);
}
