package com.steccn.qms.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 检验方案启用停用DTO
 *
 * @author AndrewLuo
 * @date 2022/09/13
 */
@Data
public class QtCheckPlanStausDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 方案类型
     */
    @NotNull
    private Integer status;

    /**
     * 方案id
     */
    @NotNull
    private Integer id;
}
