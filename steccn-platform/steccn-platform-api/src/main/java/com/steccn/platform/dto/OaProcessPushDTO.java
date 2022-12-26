package com.steccn.platform.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 接收OA新流程推送接口信息
 *
 * @author AndrewLuo
 * @date 2022/12/26
 */
@Data
public class OaProcessPushDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String username;

    private String processId;

    private String status;

    private String title;

    private String content;
}
