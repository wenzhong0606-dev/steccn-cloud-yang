package com.steccn.pms.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author chenhuan
 * @date 2022/11/8 18:51
 */
@Data
public class CompleteStorageCountVO {

    private Long classesId;
    private Integer productionLineId;
    private BigDecimal quantity;
}
