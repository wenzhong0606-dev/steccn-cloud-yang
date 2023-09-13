package com.steccn.wms.feign;

import com.steccn.common.constant.FeignConstant;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @author yequan
 * @desc
 * @date 2023/9/13
 */
@FeignClient(contextId = "remoteWmsTransferSlipFeignApi", value = FeignConstant.STECCN_WMS_SERVICE_NAME)
public interface RemoteWmsTransferSlipFeignApi {

}
