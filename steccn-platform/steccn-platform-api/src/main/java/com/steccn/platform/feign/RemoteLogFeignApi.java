package com.steccn.platform.feign;

import com.steccn.common.constant.FeignConstant;
import com.steccn.common.vo.R;
import com.steccn.platform.dto.SysLogDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @author chenhuan
 * @date 2022/10/20 10:21
 */
@FeignClient(contextId = "remoteLogFeignApi", value = FeignConstant.STECCN_PLATFORM_SERVICE_NAME)
public interface RemoteLogFeignApi {

    @PostMapping("/feign/platform/sys/log")
    R<Boolean> add(@RequestBody SysLogDTO sysLogDTO);
}
