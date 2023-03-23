package com.steccn.qms.dto;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
@ApiModel("整改记录-中断")
public class QtImproveRecordSuspendDTO {

    /**
     * 记录id
     */
    @NotNull(message = "记录id不能为空")
    private Long id;


    /**
     * 中断原因
     */
    @NotNull(message = "中断原因不能为空")
    @Size(min = 1, max = 100, message = "中断原因不能超过100字")
    private String reason;
}
