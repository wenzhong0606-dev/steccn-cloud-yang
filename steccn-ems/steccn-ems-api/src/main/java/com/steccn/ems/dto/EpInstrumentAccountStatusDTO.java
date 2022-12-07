package com.steccn.ems.dto;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
public class EpInstrumentAccountStatusDTO implements Serializable {
    /**
     * id
     */
    @NotNull(message = "仪表台账id不能为空")
    private Integer id;

    /**
     * 状态码 0 停用 1 启用
     */
    @NotNull(message = "状态不能为空")
    @Max(value = 1, message = "状态只能是 0停用  1启用")
    @Min(value = 0, message = "状态只能是 0停用  1启用")
    private Integer status;
}
