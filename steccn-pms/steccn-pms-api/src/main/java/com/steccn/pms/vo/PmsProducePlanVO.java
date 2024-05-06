package com.steccn.pms.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * @author chenhuan
 * @date 2022/11/3 15:44
 */
@Data
public class PmsProducePlanVO {

    private String materialCode;

    private String materialName;

    private Long materialId;

    private LocalDate planDate;

    private BigDecimal quantity;

    private Long id;

    private String position;

    private Integer customerId;

    private String customerCode;

    private String customerName;

    private Integer type;

    private Integer productProperty;

    /**
     * 创建时间
     */
    private LocalDateTime updateTime;
}
