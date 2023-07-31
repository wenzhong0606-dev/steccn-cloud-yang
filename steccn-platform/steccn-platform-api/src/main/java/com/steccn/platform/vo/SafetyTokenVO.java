package com.steccn.platform.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 安环tokenVO
 *
 * @author AndrewLuo
 * @date 2023/07/31
 */
@Data
public class SafetyTokenVO implements Serializable {
    private String token;
}
