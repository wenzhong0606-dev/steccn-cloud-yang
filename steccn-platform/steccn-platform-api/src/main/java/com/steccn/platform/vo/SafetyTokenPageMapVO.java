package com.steccn.platform.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * token page mapVO
 *
 * @author AndrewLuo
 * @date 2023/07/31
 */
@Data
public class SafetyTokenPageMapVO implements Serializable {
    private String path;
    private String name;
    private String title;
    private String resId;
}
