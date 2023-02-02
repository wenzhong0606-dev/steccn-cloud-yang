package com.steccn.websocket.feign;

import com.steccn.common.constant.FeignConstant;
import com.steccn.common.vo.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author yequan
 * @date 2023/2/2 9:19
 */
@FeignClient(contextId = "remoteWsFeignApi", value = FeignConstant.STECCN_WEBSOCKET_SERVICE_NAME)
public interface RemoteWsFeignApi {

    /**
     * 推送oa数据
     *
     * @param userId 用户id
     * @return
     */
    @GetMapping("/feign/websocket/push/oa")
    R<String> pushOa(@RequestParam("userId") Long userId);
}
