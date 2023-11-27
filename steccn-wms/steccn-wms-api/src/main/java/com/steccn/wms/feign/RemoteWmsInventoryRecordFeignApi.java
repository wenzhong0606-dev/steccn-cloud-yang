package com.steccn.wms.feign;


import com.steccn.common.constant.FeignConstant;
import com.steccn.common.vo.R;
import com.steccn.wms.dto.WmsInventoryRecordDTO;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;


/**
 * 库存流水feign
 * @author 孟乐
 * @date 2023/10/11
 */
@FeignClient(contextId = "RemoteWmsInventoryRecordFeignApi", value = FeignConstant.STECCN_WMS_SERVICE_NAME)
public interface RemoteWmsInventoryRecordFeignApi {

    /**
     * 库存流水记录保存
     * @param wmsInventoryList
     * @return
     */
    @ApiOperation("库存流水记录保存")
    @PostMapping("/save")
   R<Boolean> save(@RequestBody List<WmsInventoryRecordDTO> wmsInventoryList);
}
