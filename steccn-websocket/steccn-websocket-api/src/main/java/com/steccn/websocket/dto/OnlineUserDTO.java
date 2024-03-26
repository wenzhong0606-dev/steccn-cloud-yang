package com.steccn.websocket.dto;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class OnlineUserDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户名
     */
    private Long userId;

    /**
     * 真实名称
     */
    private String realName;

    /**
     * 头像
     */
    private String headImage;

    /**
     * 建立链接时间
     */
    private LocalDateTime linkTime;

}
