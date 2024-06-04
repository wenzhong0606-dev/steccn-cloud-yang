package com.steccn.pms.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 总经理日报表VO
 *
 * @author AndrewLuo
 * @date 2024/05/31
 */
@Data
public class PmsManageReportVO {
    private String department;
    private String indicator;
    private BigDecimal value;
    private BigDecimal relative;
    private BigDecimal difference;
    private BigDecimal differencePercent;
}
