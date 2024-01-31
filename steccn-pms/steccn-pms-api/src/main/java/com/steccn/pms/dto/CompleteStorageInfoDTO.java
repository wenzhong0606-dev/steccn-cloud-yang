package com.steccn.pms.dto;


import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CompleteStorageInfoDTO {


    /**
     * 物料id
     */
    private Long materialId;

    /* *
     * 物料名称
     */
    private String materialName;


    /**
     * 数量
     */
    private BigDecimal quantity;

    private LocalDateTime enterTime;


    /**
     * 入库时间
     */
    private LocalDateTime  time;

    /**
     * 粒度
     */
    private String inStorageTime;

    /**
     * 产线id
     */
    private Integer productLineId;
}
