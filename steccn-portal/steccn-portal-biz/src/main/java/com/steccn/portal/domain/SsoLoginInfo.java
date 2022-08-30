package com.steccn.portal.domain;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

/**
 * @desc 单点登录表
 */
@Table("sso_login_info")
@Data
public class SsoLoginInfo {
    @Id
    private Integer id;

    @Column("user_id")
    private Long userId;

    @Column("app_name")
    private String appName;

    @Column("app_user_id")
    private String appUserId;

    @Column("token")
    private String token;

    @Column("token_expire")
    private Long tokenExpire;

    @Column("refresh_token")
    private String refreshToken;

    @Column("login_name")
    private String loginName;

    @Column("login_status")
    private String loginStatus;

    @Column("login_time")
    private LocalDateTime loginTime;

    @Column("create_time")
    private LocalDateTime createTime;

    @Column("update_time")
    private LocalDateTime updateTime;
}
