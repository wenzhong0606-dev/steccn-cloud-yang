package com.steccn.qms.dto;


import io.swagger.annotations.ApiModel;
import lombok.Data;

@Data
@ApiModel("整改记录详情")
public class QtImproveRecordDetailDTO {

    /**
     * 处理人主键
     */
    private Integer approverId;

    /**
     * 处理人名称
     */
    private String approverName;

    /**
     * 结论
     */
    private String approverConclusion;
}
