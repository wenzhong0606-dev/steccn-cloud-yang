package com.steccn.oa.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * oa提交
 *
 * @author AndrewLuo
 * @date 2023/04/04
 */
@Data
public class OaWorkFlowSubmitParamDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 签字意见
     */
    private String remark;
    /**
     * 主要数据
     */
    private List<OaWorkFlowNameValueDTO> mainData;
    /**
     * 流程id
     */
    private Integer requestId;
    /**
     * 其他参数
     */
    private Map<String,String> otherParams;
    /**
     * 详情数据
     */
    private List<OaWorkFlowDetailObjDTO> detailData;

    /**
     * 提交人id
     */
    private Long userId;
}
