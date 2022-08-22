package com.steccn.gateway.controller;

import com.steccn.common.vo.R;
import com.steccn.common.vo.UserBaseInfoVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * 类说明
 *
 * @author Jinglihui
 * @date 2022/8/4
 */
@RestController
@Slf4j
@RequestMapping("/aas")
public class AuthorizationController {

    @GetMapping("/token")
    R<UserBaseInfoVO> getAuthorizationCode(@RegisteredOAuth2AuthorizedClient("gatewayclient") OAuth2AuthorizedClient client) {
       //todo 安全性考虑，统一返回R
        UserBaseInfoVO vo = new UserBaseInfoVO();
        vo.setAccessToken(client.getAccessToken().getTokenValue());
        vo.setExpiresAt(LocalDateTime.ofInstant(client.getAccessToken().getExpiresAt(), ZoneId.systemDefault()));
        vo.setRefreshToken(client.getRefreshToken().getTokenValue());
        return R.success(vo);
    }
}
