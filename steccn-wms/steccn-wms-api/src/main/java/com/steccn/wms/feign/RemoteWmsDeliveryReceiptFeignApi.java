package com.steccn.wms.feign;

import com.steccn.common.constant.FeignConstant;
import com.steccn.common.dto.TripleDTO;
import com.steccn.common.vo.R;
import com.steccn.wms.dto.WmsDeliveryReceiptClientNumDTO;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@FeignClient(contextId = "RemoteWmsDeliveryReceiptFeignApi", value = FeignConstant.STECCN_WMS_SERVICE_NAME)
public interface RemoteWmsDeliveryReceiptFeignApi {

    @ApiOperation("获取客户指定日期范围内的出库汇总")
    @PostMapping("/feign/wms/delivery/receipt/client/num")
    R<List<WmsDeliveryReceiptClientNumDTO>> clientNum(@RequestBody TripleDTO<Set<Long>, LocalDate, LocalDate> dto);

}
