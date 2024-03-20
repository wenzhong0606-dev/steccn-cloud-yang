package com.steccn.websocket.dto;

import cn.hutool.core.date.LocalDateTimeUtil;
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
     * 建立链接时间
     */
    private LocalDateTime linkTime;

}
