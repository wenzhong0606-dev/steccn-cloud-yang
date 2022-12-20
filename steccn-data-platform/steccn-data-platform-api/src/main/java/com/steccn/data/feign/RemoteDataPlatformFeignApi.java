package com.steccn.data.feign;

import com.steccn.common.constant.FeignConstant;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @author yequan
 * @desc 数据中台服务
 */
@FeignClient(contextId = "remoteDataPlatformFeignApi", value = FeignConstant.STECCN_DATA_PLATFORM_SERVICE_NAME)
public interface RemoteDataPlatformFeignApi {


}
