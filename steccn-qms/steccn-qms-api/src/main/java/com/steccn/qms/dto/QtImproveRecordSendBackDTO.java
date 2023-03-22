package com.steccn.qms.dto;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
@ApiModel("整改记录退回DTO")
public class QtImproveRecordSendBackDTO {

    /**
     * 记录id
     */
    @NotNull(message = "记录唯一标识不能为空")
    private Long id;

    /**
     * 退回原因
     */
    @NotNull(message = "退回原因不能为空")
    @Size(min = 1, max = 100, message = "退回原因字数不能大于100")
    private String reason;
}
