package com.steccn.websocket.feign;

import com.steccn.common.constant.FeignConstant;
import com.steccn.common.vo.R;
import com.steccn.websocket.dto.MsgPushDTO;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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

    /**
     * 推送消息
     * @param msgPushDTO
     * @return
     */
    @PostMapping("/feign/websocket/push/msg")
    R<MsgPushDTO> pushMsg(@RequestBody MsgPushDTO msgPushDTO);

    /**
     * 给指定人推送刷新消息
     * @param msgPushDTO
     * @return
     */
    @PostMapping("/feign/websocket/push/refresh")
    R<MsgPushDTO> pushRefreshMsg(@RequestBody MsgPushDTO msgPushDTO);
}
