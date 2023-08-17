package com.steccn.websocket.controller;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.common.constant.FeignConstant;
import com.steccn.common.vo.R;
import com.steccn.oa.constant.OaConstant;
import com.steccn.platform.feign.RemoteSysOauthClientDetailFeignApi;
import com.steccn.websocket.dto.MsgPushDTO;
import com.steccn.websocket.service.WebSocketManager;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 第三方webSocket
 *
 * @author AndrewLuo
 * @date 2023/08/10
 */
@RestController
@AllArgsConstructor
@RequestMapping("/websocket")
public class WsThirdPartyController {

    private final ObjectMapper objectMapper;
    private final RemoteSysOauthClientDetailFeignApi remoteSysOauthClientDetailFeignApi;
    /**
     * 第三方推oa刷新消息
     * @return
     */
    @SneakyThrows
    @GetMapping("/thirdparty/push")
    public R<Boolean> pushMsg(HttpServletRequest request) {
        validRequestHeader(request);
        MsgPushDTO msgPushDTO = new MsgPushDTO();
        msgPushDTO.setType(OaConstant.NO);
        String msg = objectMapper.writeValueAsString(msgPushDTO);
        WebSocketManager.sendMessageToOnlineUser(msg);
        return R.success(true);
    }

    /**
     * 校验请求头信息
     *
     * @param request 请求
     */
    private void validRequestHeader(HttpServletRequest request) {
        List<String> pwdSet = FeignOptional.of(() -> remoteSysOauthClientDetailFeignApi.clientEncodePassword(), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        String clientId = request.getHeader(OaConstant.CLIENT_ID);
        Assert.isTrue(StrUtil.isNotBlank(clientId), "请求头中必须携带client-id");
        Assert.isTrue(pwdSet.contains(clientId), "非法访问，请携带正确的client-id");
    }
}
