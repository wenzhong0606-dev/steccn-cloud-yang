package com.steccn.oa.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * oa工作流状态vo
 *
 * @author AndrewLuo
 * @date 2023/03/17
 */
@Data
public class OaWorkflowStatusVO implements Serializable {
    /**
     * 流程id
     */
    private Integer requestId;

    /**
     * 状态code 返回数据状态 SUCCESS：成功，PARAM_ERROR：参数错误，NO_PERMISSION：无权限，SYSTEM_INNER_ERROR：程序异常，USER_EXCEPTION：用户异常
     */
    private String code;

    /**
     * 错误信息
     */
    private String errMsg;
}
