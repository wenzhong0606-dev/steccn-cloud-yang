package com.steccn.portal.service;

import com.alibaba.fastjson.JSONObject;
import com.steccn.common.vo.R;
import com.steccn.oa.dto.OaLoginParam;
import com.steccn.oa.dto.OaLoginResult;
import com.steccn.oa.feign.OaInfoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Objects;
import java.util.Optional;

/**
 * @desc 登录服务
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LoginService {

    private final OaInfoService oaInfoService;

    /**
     * 登录模块
     *
     * @param username
     * @param password
     * @return
     */
    public R login(String username, String password) {
        OaLoginParam oaLoginParam = new OaLoginParam();
        oaLoginParam.setLoginid(username);
        oaLoginParam.setUserpassword(password);
        R r = oaInfoService.checkLogin(oaLoginParam);
        OaLoginResult oaLoginResult = JSONObject.parseObject((String) r.getData(), OaLoginResult.class);
        if (Objects.nonNull(oaLoginResult) && oaLoginResult.getLoginStatus()) {
            R ssoToken = oaInfoService.getSsoToken(String.valueOf(oaLoginResult.getUserId()));
            log.info("ssoToken -> {}", ssoToken);
        }
        return r;
    }
}
