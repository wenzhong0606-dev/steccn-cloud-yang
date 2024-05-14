package com.steccn.ems.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 深加工消耗统计分析
 *
 * @author AndrewLuo
 * @date 2024/05/13
 */
@Data
public class DeepProcessConsumeReportVO {
    private String category;
    private BigDecimal one;
    private BigDecimal two;
    private BigDecimal three;
    private BigDecimal count;
}
