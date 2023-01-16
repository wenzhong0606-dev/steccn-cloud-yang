package com.steccn.oa.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * oa工作流详情数据DTO
 *
 * @author AndrewLuo
 * @date 2023/01/16
 */
@Data
public class OaWorkFlowDetailDataListDTO implements Serializable {
    /**
     * 记录顺序
     */
    private String recordOrder;

    /**
     * 表格详情
     */
    private List<OaWorkFlowNameValueDTO> workflowRequestTableFields;
}
