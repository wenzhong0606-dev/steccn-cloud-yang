package com.steccn.oa.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * OA工作流DTO
 *
 * @author AndrewLuo
 * @date 2022/11/07
 */
@Data
public class OaToDoWorkFlowDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 第几页
     */
    private Integer pageNo;

    /**
     * 页数
     */
    private Integer pageSize;

    /**
     * 使用的userId
     */
    private String userId;
}
