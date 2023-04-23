package com.steccn.qms.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 损耗日报表统计DTO
 *
 * @author AndrewLuo
 * @date 2023/04/23
 */
@Data
public class QtLossReportDataDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 重量
     */
    private BigDecimal weight;
    /**
     * 损耗率
     */
    private BigDecimal lossRate;
}
