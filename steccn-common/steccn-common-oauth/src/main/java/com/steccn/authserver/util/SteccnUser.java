package com.steccn.authserver.util;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.io.Serializable;
import java.util.Collection;

/**
 * @desc 凯盛节能用户信息
 */
public class SteccnUser extends User implements Serializable {

    /**
     * 用户id
     */
    @Getter
    private Long userId;

    /**
     * 用户名称
     */
    @Getter
    private String name;

    /**
     * 构造函数
     *
     * @param userId
     * @param name
     * @param username
     * @param password
     * @param authorities
     */
    public SteccnUser(Long userId, String name, String username, String password, Collection<? extends GrantedAuthority> authorities) {
        super(username, password, authorities);
        this.userId = userId;
        this.name = name;
    }
}
