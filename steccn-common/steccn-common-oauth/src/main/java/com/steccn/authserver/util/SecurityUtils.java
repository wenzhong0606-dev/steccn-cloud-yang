package com.steccn.authserver.util;

import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTUtil;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;

/**
 * 安全工具类
 *
 * @author L.cm
 */
@UtilityClass
@Slf4j
public class SecurityUtils {

    /**
     * 获取Authentication
     */
    public Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    /**
     * 获取用户
     *
     * @param authentication
     * @return SteccnUser
     */
    public SteccnUser getUser(Authentication authentication) {
        if (authentication == null) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof SteccnUser) {
            return (SteccnUser) principal;
        }
        return null;
    }

    /**
     * 获取用户
     */
    public SteccnUser getUser() {
        Authentication authentication = getAuthentication();
        return getUser(authentication);
    }
}