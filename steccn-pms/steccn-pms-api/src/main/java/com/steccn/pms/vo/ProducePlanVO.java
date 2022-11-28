package com.steccn.pms.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * @author chenhuan
 * @date 2022/11/28 10:40
 */
@Data
public class ProducePlanVO {
    /**
     * 日期
     */
    private LocalDate planDate;

    private BigDecimal quantity;

    /**
     * 物料id
     */
    private Long materialId;

    private String materialCode;

    private Long id;
}
