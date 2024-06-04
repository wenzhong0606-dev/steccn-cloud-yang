package com.steccn.sales.feign;

import com.steccn.common.constant.FeignConstant;
import com.steccn.common.dto.TupleDTO;
import com.steccn.common.vo.R;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

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
}
