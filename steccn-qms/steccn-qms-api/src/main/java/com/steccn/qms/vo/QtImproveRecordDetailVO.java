package com.steccn.qms.vo;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@ApiModel("整改明细详情")
public class QtImproveRecordDetailVO {


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
