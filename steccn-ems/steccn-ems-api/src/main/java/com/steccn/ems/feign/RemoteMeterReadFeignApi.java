package com.steccn.ems.feign;

import com.steccn.common.constant.FeignConstant;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @author yequan
 * @desc 能源的耗用数
 */
@FeignClient(contextId = "RemoteMeterReadFeignApi", value = FeignConstant.STECCN_EMS_SERVICE_NAME)
public interface RemoteMeterReadFeignApi {


}
