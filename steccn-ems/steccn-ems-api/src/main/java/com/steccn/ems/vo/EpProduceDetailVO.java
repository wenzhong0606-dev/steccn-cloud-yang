package com.steccn.ems.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * @author chenhuan
 * @date 2022/12/9 14:31
 */
@Data
public class EpProduceDetailVO {

    /**
     * 厚度
     */
    private BigDecimal thickQuantity;

    /**
     * 班次日期
     */
    private LocalDate classDate;

    /**
     * 班次id
     */
    private Integer classId;

    /**
     * 节点id
     */
    private Integer officeId;

    /**
     * 基片拉引量、深加工产量
     */
    private BigDecimal yield;

    private Long materialId;

    private Integer productLineId;
}
