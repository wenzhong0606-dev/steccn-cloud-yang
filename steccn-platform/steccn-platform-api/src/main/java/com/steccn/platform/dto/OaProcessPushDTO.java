package com.steccn.platform.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
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

    /**
     * 用户工号
     */
    private String username;

    /**
     * 流程id
     */
    private String processId;

    /**
     * 流程状态
     */
    private String status;

    /**
     * 流程标题
     */
    private String title;

    /**
     * 流程内容
     */
    private String content;

    /**
     * 唯一值
     */
    @NotNull(message = "唯一标识不能为空")
    private String uniqueValue;
}
