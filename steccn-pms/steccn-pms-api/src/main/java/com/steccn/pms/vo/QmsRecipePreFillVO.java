package com.steccn.pms.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 预填配方单VO
 *
 * @author AndrewLuo
 * @date 2023/05/23
 */
@Data
public class QmsRecipePreFillVO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 版本号
     */
    private String version;

    /**
     * 干基值
     */
    private BigDecimal dryBasisQuantity;

    /**
     * 湿基值
     */
    private BigDecimal wetBasisQuantity;
}
