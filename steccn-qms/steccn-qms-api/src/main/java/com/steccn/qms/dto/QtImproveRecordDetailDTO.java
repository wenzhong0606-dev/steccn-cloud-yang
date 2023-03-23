package com.steccn.qms.dto;


import io.swagger.annotations.ApiModel;
import lombok.Data;

@Data
@ApiModel("整改记录详情")
public class QtImproveRecordDetailDTO {


    /**
     * id
     */
    private Long id;

    /**
     * 整改主键
     */
    private Long recordId;

    /**
     * 第几步 D1 D2 ...D8
     */
    private String approverStep;

    /**
     * 处理人主键
     */
    private Long approverId;

    /**
     * 处理人名称
     */
    private String approverName;

    /**
     * 结论
     */
    private String approverConclusion;
}
