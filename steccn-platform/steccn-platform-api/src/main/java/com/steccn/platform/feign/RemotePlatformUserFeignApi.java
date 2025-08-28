package com.steccn.platform.feign;

import com.steccn.common.constant.FeignConstant;
import com.steccn.common.vo.R;
import com.steccn.platform.vo.PlatformUserVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 类说明
 *
 * @author AndrewLuo
 * @date 2025/08/06
 */
@FeignClient(contextId = "remotePlatformUserFeignApi", value = FeignConstant.STECCN_PLATFORM_SERVICE_NAME)
public interface RemotePlatformUserFeignApi {
    @GetMapping("/feign/platform/user/userName")
    R<PlatformUserVO> userInfoByUserName(@RequestParam("userName") String userName);

    @GetMapping("/feign/platform/user/compareMac")
    R<Boolean> compareMac(@RequestParam("userId") Long userId,@RequestParam("macAddress") String macAddress);
}
