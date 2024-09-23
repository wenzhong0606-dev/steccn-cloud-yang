package com.steccn.dp.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * value类型DTO
 *
 * @author AndrewLuo
 * @date 2024/09/23
 */
@Data
public class DpValueDTO {
    private BigDecimal value;
    private String tagId;
}
