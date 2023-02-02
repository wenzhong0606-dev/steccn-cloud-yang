package com.steccn.oa.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 待办列表VO
 *
 * @author AndrewLuo
 * @date 2022/11/07
 */
@Data
public class OaToDoWorkflowVO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 流程标题
     */
    private String requestName;

    /**
     * 接收时间
     */
    private String receiveTime;

    /**
     * 操作时间
     */
    private String operateTime;

    /**
     * 是否已读 0未读 1已读
     */
    private Integer isRead;

    /**
     * 创建人姓名
     */
    private String creatorName;

    /**
     * 时间差
     */
    private String timePeriod;

    /**
     * 时间差单位
     */
    private String timePeriodUnit;
}
