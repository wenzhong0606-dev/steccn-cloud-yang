package com.steccn.wms.feign;

import com.steccn.common.constant.FeignConstant;

import com.steccn.common.vo.R;
import com.steccn.wms.dto.CaseSiteDTO;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


import java.util.Map;
import java.util.Set;

/**
 * 类说明
 *
 * @author Mongknife
 * @date 2022/12/12
 */
@FeignClient(contextId = "RemoteWmsInventoryFeignApi", value = FeignConstant.STECCN_WMS_SERVICE_NAME)
public interface RemoteWmsInventoryFeignApi {

    /**
     * 根据箱号获取仓库货架信息
     *
     * @param caseNos
     * @return
     */
    @PostMapping("/feign/wms/inventory/stockByCaseNo")
    @ApiOperation("根据产线查询当前正在执行订单信息")
    R<Map<String, CaseSiteDTO>> stockByCaseNo(@RequestBody Set<String> caseNos);
}
