package com.steccn.pms.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

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
}
