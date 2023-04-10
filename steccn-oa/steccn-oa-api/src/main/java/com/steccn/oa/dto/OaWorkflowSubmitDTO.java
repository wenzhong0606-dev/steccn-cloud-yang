package com.steccn.oa.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * oa提交流程DTO
 *
 * @author AndrewLuo
 * @date 2023/04/04
 */
@Data
public class OaWorkflowSubmitDTO implements Serializable {
    private String mainData;
    private String detailData;
    private String remark;
    private String otherParams;
    private Integer requestId;
}
