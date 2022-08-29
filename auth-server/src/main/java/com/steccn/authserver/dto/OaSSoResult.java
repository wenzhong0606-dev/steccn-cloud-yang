package com.steccn.authserver.dto;

import lombok.Data;

/**
 * oa跳过登录页token名
 *
 * @author AndrewLuo
 * @date 2022/08/29
 */
@Data
public class OaSSoResult {
    private String msg;
    private int code;
    private boolean status;
    private String token;
}
