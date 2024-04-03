package com.steccn.pms.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 劳保标准详情DTO
 *
 * @author AndrewLuo
 * @date 2024/04/03
 */
@Data
public class PmsLaborStandardDetailDTO {
    /**
     * 物料id
     */
    @NotNull(message = "物料Id不能为空")
    private Long materialId;

    /**
     * 时间间隔
     */
    @NotNull(message = "时间间隔不能为空")
    private Integer period;

    /**
     * 可领数量
     */
    @NotNull(message = "可领数量不能为空")
    private Long amount;
}
