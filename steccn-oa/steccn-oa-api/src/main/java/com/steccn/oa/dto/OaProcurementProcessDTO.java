package com.steccn.oa.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 采购流程DTO
 *
 * @author AndrewLuo
 * @date 2023/01/13
 */
@Data
public class OaProcurementProcessDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    private String requestName;
    private String mainData;
    private String detailData;
    private String remark;
    private String otherParams;
    private String workflowId;
    private String requestLevel;

}
