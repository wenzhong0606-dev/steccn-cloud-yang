package com.steccn.pms.dto;

import lombok.Data;

import javax.validation.constraints.NotEmpty;

/**
 * 生产月计划查询DTO
 *
 * @author AndrewLuo
 * @date 2024/04/28
 */
@Data
public class PmsProducePlanQueryDTO {
    @NotEmpty
    private String monthDate;
    private String materialCode;
    private String materialName;
    private Integer productProperty;
}
