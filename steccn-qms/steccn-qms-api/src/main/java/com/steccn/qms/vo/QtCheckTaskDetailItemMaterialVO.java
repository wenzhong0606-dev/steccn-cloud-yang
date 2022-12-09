package com.steccn.qms.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author chenhuan
 * @date 2022/12/9 9:31
 */
@Data
public class QtCheckTaskDetailItemMaterialVO {
    private String itemName;

    private String checkResult;

    private Long materialId;

    private BigDecimal thickQuantity;

    private LocalDateTime classTime;

    private Integer classId;

    private BigDecimal lengthQuantity;
}
