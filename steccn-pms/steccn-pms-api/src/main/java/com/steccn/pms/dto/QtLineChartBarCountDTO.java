package com.steccn.pms.dto;

import com.steccn.common.dto.QtLineChartBarDTO;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author yequan
 * @desc 加上汇总信息
 * @date 2023/12/19
 */
@Data
public class QtLineChartBarCountDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 图
     */
    private QtLineChartBarDTO qtLineChartBarDTO;

    /**
     * 总数
     */
    private BigDecimal totalQuantity;

    /**
     * 总率
     */
    private BigDecimal totalRate;
}
