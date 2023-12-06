package com.steccn.dp.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 电子秤读数VO
 *
 * @author AndrewLuo
 * @date 2023/11/23
 */
@Data
public class DpWeightVO {
    private String tagId;
    private String tagName;
    private BigDecimal value;
    private LocalDate date;
}
