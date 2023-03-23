package com.steccn.authserver.util;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTUtil;
import com.google.common.collect.Lists;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;

import java.util.List;

/**
 * 安全工具类
 *
 * @author L.cm
 */
@UtilityClass
@Slf4j
public class SecurityUtils {

    @Autowired
    @Lazy
    private SessionRegistry sessionRegistry;

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

    public List<SteccnUser> getAllOnlineUser(){
        List<Object> allPrincipals = sessionRegistry.getAllPrincipals();
        List<SteccnUser> users = Lists.newArrayList();
        if(CollUtil.isEmpty(allPrincipals)){
            return users;
        }else {
            allPrincipals.forEach(tmp -> {
                if(tmp instanceof SteccnUser){
                    users.add((SteccnUser) tmp);
                }
            });
        }
        return users;
    }
}