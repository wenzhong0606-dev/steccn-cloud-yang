package com.steccn.platform.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 获取srm
 *
 * @author AndrewLuo
 * @date 2023/08/24
 */
@Data
public class SrmTokenVO implements Serializable {
    private String message;
    private String accessToken;
    private String refreshToken;
    private Integer expiresIn;
}
