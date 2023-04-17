package com.steccn.qms.dto;


import com.steccn.qms.dto.QtCarbonCopyManDTO;
import io.swagger.annotations.ApiModel;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@ApiModel("整改记录-提交审批")
public class QtImproveRecordApproveDTO {

    /**
     * 记录id
     */
    @NotNull(message = "记录id不能为空")
    private Long id;


    /**
     * 处理结论
     */
    private String approverConclusion;


    /**
     * d7抄送人
     */
    private List<QtCarbonCopyManDTO> carbonCopyManList;

    /**
     * oa流程跳转内容
     */
    private String infoUrl;
}
