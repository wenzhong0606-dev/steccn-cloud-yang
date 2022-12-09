package com.steccn.qms.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

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

    private LocalDate classTime;

    private Integer classId;
}
