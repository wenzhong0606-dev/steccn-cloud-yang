package com.steccn.pms.vo;


import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PmsCaseNoInfoVO {

    /**
     * 数量
     */
    private BigDecimal quantity;

    /**
     * 物料id
     */
    private Long materialId;


    /**
     * 数量单位
     */
    private Long unitId;



    /**
     * 仓库id
     */
    private Integer warehouseId;

    /**
     * 货架Id
     */
    private Integer shelfId;


    /**
     * 批次号
     */
    private String batchNo;


}
