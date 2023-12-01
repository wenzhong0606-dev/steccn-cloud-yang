package com.steccn.dp.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 日期-值VO
 *
 * @author AndrewLuo
 * @date 2023/12/01
 */
@Data
public class DpValueDateVO {
    private LocalDate day;
    private BigDecimal value;
}
