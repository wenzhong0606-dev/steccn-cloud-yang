package com.steccn.authserver.config;

import com.steccn.authserver.constant.JwtAdditionConstant;
import com.steccn.authserver.util.SteccnUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;

import java.util.HashMap;
import java.util.Map;

/**
 * @description: 配置token内容增强器
 */
@Slf4j
public class JwtTokenEnhancer implements TokenEnhancer {

    private StringRedisTemplate redisTemplate;

    public JwtTokenEnhancer(StringRedisTemplate redisTemplate){
        this.redisTemplate = redisTemplate;
    }

    @Override
    public OAuth2AccessToken enhance(OAuth2AccessToken oAuth2AccessToken, OAuth2Authentication oAuth2Authentication) {
        log.info("redisTemplate -> {}", redisTemplate);
        Map<String, Object> info = new HashMap<>();
        //需要增强的内容
        Object principal = oAuth2Authentication.getPrincipal();
        if (principal instanceof SteccnUser) {
            SteccnUser steccnUser = (SteccnUser) principal;
            info.put(JwtAdditionConstant.JWT_ADDITION_USERID, steccnUser.getUserId());
            info.put(JwtAdditionConstant.JWT_ADDITION_NAME, steccnUser.getName());
            info.put("pwStr", redisTemplate.opsForValue().get(steccnUser.getUsername()));
        }
        ((DefaultOAuth2AccessToken) oAuth2AccessToken).setAdditionalInformation(info);
        return oAuth2AccessToken;
    }
}
