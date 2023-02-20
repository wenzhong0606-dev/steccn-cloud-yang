package com.steccn.pms.dto;

import lombok.Data;

import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 生料DTO
 *
 * @author AndrewLuo
 * @date 2023/02/17
 */
@Data
public class PmsRecipeRawDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 物料id
     */
    @NotNull(message = "物料id不能为空")
    private Long materialId;

    /**
     * 物料编码
     */
    @NotNull(message = "物料编码不能为空")
    private String materialCode;

    /**
     * 物料名称
     */
    @NotNull(message = "物料名称不能为空")
    private String materialName;

    /**
     * 干基用量
     */
    @NotNull(message = "干基用量不能为空")
    @DecimalMin(value="0",message="干基用量应大于等于0")
    @DecimalMax(value="999999999999.9999",message="数量大小应小于999999999999.9999")
    @Digits(integer = 12,fraction = 4,message = "整数位上限为12位，小数位上限位4位")
    private BigDecimal dryBasisQuantity;

    /**
     * 干基单位
     */
    private Long dryBasisUnit;

    /**
     * 湿基用量
     */
    @NotNull(message = "干基用量不能为空")
    @DecimalMin(value="0",message="干基用量应大于等于0")
    @DecimalMax(value="999999999999.9999",message="数量大小应小于999999999999.9999")
    @Digits(integer = 12,fraction = 4,message = "整数位上限为12位，小数位上限位4位")
    private BigDecimal wetBasisQuantity;

    /**
     * 湿基单位
     */
    private Long wetBasisUnit;

    /**
     * 湿基含水率
     */
    @NotNull(message = "湿基含水率不能为空")
    @DecimalMin(value="0",message="湿基含水率应大于等于0")
    @DecimalMax(value="100",message="数量大小应小于100")
    @Digits(integer = 3,fraction = 4,message = "整数位上限为3位，小数位上限位4位")
    private BigDecimal wetBasisWaterRate;

}
