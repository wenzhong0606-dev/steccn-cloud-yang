package com.steccn.pms.vo;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 劳保详情标准VO
 *
 * @author AndrewLuo
 * @date 2024/04/03
 */
@Data
public class PmsLaborStandardDetailVO {
    /**
     * 物料id
     */
    private Long materialId;

    /**
     * 物料名称
     */
    private String materialName;

    /**
     * 时间间隔
     */
    private Integer period;

    /**
     * 可领数量
     */
    private Long amount;
}
