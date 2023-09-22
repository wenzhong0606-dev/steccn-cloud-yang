package com.steccn.wms.feign;

import com.steccn.common.constant.FeignConstant;
import com.steccn.common.dto.TupleDTO;
import com.steccn.common.vo.R;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

/**
 * @author yequan
 * @desc
 * @date 2023/9/13
 */
@FeignClient(contextId = "remoteWmsTransferSlipFeignApi", value = FeignConstant.STECCN_WMS_SERVICE_NAME)
public interface RemoteWmsTransferSlipFeignApi {

    /**
     * 根据线边仓获取领用量
     *
     * @param tupleDTO dto
     * @return
     */
    @PostMapping(value = "/feign/wms/transfer/slip/line/warehouse")
    @ApiOperation("根据线边仓获取领用量")
    R<BigDecimal> slipLineWarehouse(@RequestBody TupleDTO<LocalDate, Set<Integer>> tupleDTO);

}
