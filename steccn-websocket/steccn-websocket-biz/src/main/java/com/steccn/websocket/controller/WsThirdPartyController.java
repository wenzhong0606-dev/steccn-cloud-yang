package com.steccn.websocket.controller;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.authserver.util.SecurityUtils;
import com.steccn.common.constant.FeignConstant;
import com.steccn.common.vo.R;
import com.steccn.oa.constant.OaConstant;
import com.steccn.platform.feign.RemoteMsgFeignApi;
import com.steccn.platform.feign.RemoteSafetyBizFeignApi;
import com.steccn.platform.feign.RemoteSafetyFeignApi;
import com.steccn.platform.feign.RemoteSysOauthClientDetailFeignApi;
import com.steccn.platform.vo.SafetyTokenVO;
import com.steccn.websocket.dto.MsgPushDTO;
import com.steccn.websocket.dto.OnlineUserDTO;
import com.steccn.websocket.service.WebSocketManager;
import com.steccn.websocket.service.WsThirdPartyService;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

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
    private final WsThirdPartyService wsThirdPartyService;
    private final ThreadPoolExecutor poolExecutor = new ThreadPoolExecutor(3, 5, 60L, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(1000), Executors.defaultThreadFactory());
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
        //发送消息
//        poolExecutor.execute(()->{
//            FeignOptional.of(()->remoteSafetyBizFeignApi.sendMsg(),FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
//        });
        return R.success(true);
    }

    @GetMapping("/online")
    public R<Page<OnlineUserDTO>> online(@RequestParam("current") int current, @RequestParam("size") int size) {
        return R.success(wsThirdPartyService.online(current, size));
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
