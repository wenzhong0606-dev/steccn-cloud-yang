package com.steccn.gateway.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    Object getAuthorizationCode(@RegisteredOAuth2AuthorizedClient("gatewayclient") OAuth2AuthorizedClient client) {
       //todo 安全性考虑，统一返回R
        return client;
    }
}
