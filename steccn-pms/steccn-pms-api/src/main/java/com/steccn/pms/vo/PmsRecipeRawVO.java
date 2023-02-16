package com.steccn.pms.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 生料VO
 *
 * @author AndrewLuo
 * @date 2023/02/16
 */
@Data
public class PmsRecipeRawVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 物料id
     */
    private Long materialId;

    /**
     * 物料编码
     */
    private String materialCode;

    /**
     * 物料名称
     */
    private String materialName;

    /**
     * 干基用量
     */
    private BigDecimal dryBasisQuantity;

    /**
     * 干基单位
     */
    @JsonIgnore
    private Long dryBasisUnit;

    /**
     * 湿基用量
     */
    private BigDecimal wetBasisQuantity;

    /**
     * 湿基单位
     */
    @JsonIgnore
    private Long wetBasisUnit;

    /**
     * 湿基含水率
     */
    private BigDecimal wetBasisWaterRate;
}
