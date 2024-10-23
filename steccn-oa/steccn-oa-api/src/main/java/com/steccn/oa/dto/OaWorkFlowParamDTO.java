package com.steccn.oa.dto;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
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
     * 流程标题
     */
    @NotNull(message = "流程标题不能为空")
    private String requestName;
    /**
     * 主要数据
     */
    private List<OaWorkFlowNameValueDTO> mainData;

    private List<JSONObject> jsonList;
    /**
     * 工作流id
     */
    @NotBlank(message = "工作流id不能为空")
    private String workflowId;
    /**
     * 其他参数
     */
    private Map<String,String> otherParams;
    /**
     * 详情数据
     */
    private List<OaWorkFlowDetailObjDTO> detailData;
    /**
     * 用户id
     */
    private Long userId;
}
