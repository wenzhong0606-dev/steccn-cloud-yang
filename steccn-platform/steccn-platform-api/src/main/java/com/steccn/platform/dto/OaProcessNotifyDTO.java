package com.steccn.platform.dto;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * OA流程信息审批返回DTO
 *
 * @author AndrewLuo
 * @date 2022/12/26
 */
@Data
public class OaProcessNotifyDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 状态标记
     */
    private Boolean success;

    /**
     * 类型01-退回 02-归档 03-审批 04-其他
     */
    private String type;

    /**
     * 创建时间
     */
    private LocalDateTime creationTime;

    /**
     * 创建时间字符串
     */
    private String creationTimeStr;

    private OaProcessDataDTO data;
}
