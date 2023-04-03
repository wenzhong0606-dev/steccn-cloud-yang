package com.steccn.qms.feign;


import com.steccn.common.constant.FeignConstant;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(contextId = "remoteCustomerComplainFeignApi", value = FeignConstant.STECCN_QMS_SERVICE_NAME)
public interface RemoteCustomerComplainFeignApi {



}
