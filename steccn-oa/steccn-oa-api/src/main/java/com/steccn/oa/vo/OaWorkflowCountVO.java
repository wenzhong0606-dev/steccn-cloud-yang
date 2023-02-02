package com.steccn.oa.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * oa流程数量VO
 *
 * @author AndrewLuo
 * @date 2023/02/01
 */
@Data
public class OaWorkflowCountVO implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 我的待办
     */
    private Integer todoCount;

    /**
     * 我提交的
     */
    private Integer submitCount;

    /**
     * 我已处理的
     */
    private Integer finishCount;

    /**
     * 我已驳回的
     */
    private Integer rejectCount;
}
