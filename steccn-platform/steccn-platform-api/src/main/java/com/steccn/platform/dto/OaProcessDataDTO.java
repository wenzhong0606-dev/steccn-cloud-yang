package com.steccn.platform.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * oa流程审批返回通知DTO
 *
 * @author AndrewLuo
 * @date 2022/12/26
 */
@Data
public class OaProcessDataDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 工作流id
     */
    private String workflowId;

    /**
     * 工作流名称
     */
    private String workflowName;

    /**
     * 流程请求id
     */
    private String requestId;

    /**
     * 流程请求名称
     */
    private String requestName;

    /**
     * 创建人id
     */
    private String creatorId;

    /**
     * 创建人名称
     */
    private String creatorName;

    /**
     * 创建人工号
     */
    private String creatorWorkCode;

    /**
     * 部门id
     */
    private String departmentId;

    /**
     * 部门名称
     */
    private String departmentName;

    /**
     * 部门编码
     */
    private String departmentCode;

    /**
     * 审批意见id
     */
    private String approvalComments;

    /**
     * 备注
     */
    private String remark;

    /**
     * 附件列表
     */
    private String appendixList;
}
