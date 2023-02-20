package com.steccn.pms.dto;

import lombok.Data;

import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 配方版本DTO
 *
 * @author AndrewLuo
 * @date 2023/02/17
 */
@Data
public class PmsRecipeDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 版本号
     */
    private String version;

    /**
     * 生料列表
     */
    private List<PmsRecipeRawDTO> rawList;

    /**
     * 备注
     */
    private String note;

    /**
     * 熟料占比
     */
    @NotNull(message = "熟料占比不能为空")
    @DecimalMin(value="0",message="熟料占比率应大于等于0")
    @DecimalMax(value="100",message="熟料占比应小于100")
    @Digits(integer = 3,fraction = 4,message = "整数位上限为3位，小数位上限位4位")
    private BigDecimal clinkerRate;

    /**
     * 熟料用量
     */
    private BigDecimal clinkerQuantity;

    /**
     * 配合料含水率
     */
    @NotNull(message = "配合料含水率不能为空")
    @DecimalMin(value="0",message="配合料含水率应大于等于0")
    @DecimalMax(value="100",message="配合料含水率应小于100")
    @Digits(integer = 3,fraction = 4,message = "整数位上限为3位，小数位上限位4位")
    private BigDecimal batchWaterRate;

    /**
     * 加水量
     */
    private BigDecimal batchWaterQuantity;


    /**
     * 上偏差
     */
    @NotNull(message = "上偏差不能为空")
    @DecimalMin(value="0",message="上偏差应大于等于0")
    @DecimalMax(value="999999999999.9999",message="上偏差应小于999999999999.9999")
    @Digits(integer = 12,fraction = 4,message = "整数位上限为12位，小数位上限位4位")
    private BigDecimal batchWaterUpper;

    /**
     * 下偏差
     */
    @NotNull(message = "下偏差不能为空")
    @DecimalMin(value="0",message="下偏差应大于等于0")
    @DecimalMax(value="999999999999.9999",message="下偏差应小于999999999999.9999")
    @Digits(integer = 12,fraction = 4,message = "整数位上限为12位，小数位上限位4位")
    private BigDecimal batchWaterLower;
}
