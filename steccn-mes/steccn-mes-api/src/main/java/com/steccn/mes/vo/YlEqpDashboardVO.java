package com.steccn.mes.vo;

import com.steccn.common.dto.QtLineChartBarDTO;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 原料驾驶舱VO
 *
 * @author AndrewLuo
 * @date 2024/01/12
 */
@Data
public class YlEqpDashboardVO {
    private QtLineChartBarDTO eqpDashBoard;
    private Integer eqpAllNum;
    private BigDecimal eqpRunningPercent;
    private Integer insNum;
    private BigDecimal insFinishedPercent;
}
