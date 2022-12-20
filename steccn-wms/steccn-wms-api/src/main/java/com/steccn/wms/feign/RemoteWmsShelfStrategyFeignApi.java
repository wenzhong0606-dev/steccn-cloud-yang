package com.steccn.wms.feign;

import com.steccn.common.constant.FeignConstant;
import com.steccn.common.vo.R;
import com.steccn.wms.vo.WmsMaterialShelfUpVO;
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
 * @date 2022/12/20
 */
@FeignClient(contextId = "RemoteWmsShelfStrategyFeignApi", value = FeignConstant.STECCN_WMS_SERVICE_NAME)
public interface RemoteWmsShelfStrategyFeignApi {

    /**
     * 获取物料配置的货架信息
     *
     * @param materialIds
     * @return
     */
    @PostMapping("/feign/wms/shelfUp/strategy/material/shelf/upInfo")
    @ApiOperation("物料货架信息")
    R<Map<Long, WmsMaterialShelfUpVO>> materialShelfUpInfo(@RequestBody Set<Long> materialIds);
}
