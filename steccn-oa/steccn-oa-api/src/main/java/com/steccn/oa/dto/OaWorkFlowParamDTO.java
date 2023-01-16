package com.steccn.oa.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 接收流程创建参数
 *
 * @author AndrewLuo
 * @date 2023/01/16
 */
@Data
public class OaWorkFlowParamDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 流程名字
     */
    private String requestName;
    /**
     * 主要数据
     */
    private List<OaWorkFlowNameValueDTO> mainData;
    /**
     * 工作流id
     */
    private String workflowId;
    /**
     * 其他参数
     */
    private Map<String,String> otherParams;
    /**
     * 详情数据
     */
    private List<OaWorkFlowDetailObjDTO> detailData;
}
