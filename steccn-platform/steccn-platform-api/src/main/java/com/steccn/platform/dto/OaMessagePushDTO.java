package com.steccn.platform.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * oa消息推送接口
 *
 * @author AndrewLuo
 * @date 2022/12/26
 */
@Data
public class OaMessagePushDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 用户工号
     */
    private String username;

    /**
     * 消息编号
     */
    private String processId;

    /**
     * 消息类型
     */
    private String status;

    /**
     * 标题
     */
    private String title;

    /**
     * 内容
     */
    private String content;

    /**
     * 分类
     */
    private String category;
}
