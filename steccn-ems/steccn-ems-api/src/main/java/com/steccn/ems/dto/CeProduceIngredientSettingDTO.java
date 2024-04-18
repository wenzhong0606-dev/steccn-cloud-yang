package com.steccn.ems.dto;

import com.steccn.common.valid.InsertGroup;
import com.steccn.common.valid.UpdateGroup;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * @desc 生产原料设置表
 */
@Data
public class CeProduceIngredientSettingDTO implements Serializable {
    /**
     * 主键
     */
    @NotNull(groups = {UpdateGroup.class}, message = "主键不能为空")
    private Integer id;

    /**
     * 物料id
     */
    @NotNull(groups = {UpdateGroup.class, InsertGroup.class}, message = "物料不能为空")
    private Long materialId;

    /**
     * 物料编码
     */
    @NotNull(groups = {UpdateGroup.class, InsertGroup.class}, message = "物料编码不能为空")
    private String materialCode;

    /**
     * 物料名称
     */
    @NotNull(groups = {UpdateGroup.class, InsertGroup.class}, message = "物料名称不能为空")
    private String materialName;

    /**
     * 排放因子
     */
    @NotNull(groups = {UpdateGroup.class, InsertGroup.class}, message = "排放因子不能为空")
    @DecimalMin(groups = {UpdateGroup.class, InsertGroup.class}, value = "0", message = "排放因子应大于0")
    @DecimalMax(groups = {UpdateGroup.class, InsertGroup.class}, value = "99999999999.99999", message = "排放因子应小于99999999999.99999")
    @Digits(groups = {UpdateGroup.class, InsertGroup.class}, integer = 11, fraction = 5, message = "排放因子上限为11位，小数位上限位5位")
    private BigDecimal factorValue;

    /**
     * 纯度
     */
    @NotNull(groups = {UpdateGroup.class, InsertGroup.class}, message = "纯度不能为空")
    @DecimalMin(groups = {UpdateGroup.class, InsertGroup.class}, value = "0", message = "纯度应大于0")
    @DecimalMax(groups = {UpdateGroup.class, InsertGroup.class}, value = "999999999999999.9", message = "纯度应小于999999999999999.9")
    @Digits(groups = {UpdateGroup.class, InsertGroup.class}, integer = 15, fraction = 1, message = "纯度上限为15位，小数位上限位1位")
    private BigDecimal purityValue;

    /**
     * 煅烧比例
     */
    @NotNull(groups = {UpdateGroup.class, InsertGroup.class}, message = "煅烧比例不能为空")
    @DecimalMin(groups = {UpdateGroup.class, InsertGroup.class}, value = "0", message = "煅烧比例应大于0")
    @DecimalMax(groups = {UpdateGroup.class, InsertGroup.class}, value = "999999999999999.9", message = "煅烧比例应小于999999999999999.9")
    @Digits(groups = {UpdateGroup.class, InsertGroup.class}, integer = 15, fraction = 1, message = "煅烧比例上限为15位，小数位上限位1位")
    private BigDecimal calcinationRatio;

    /**
     * 用量统计来源
     */
    @NotNull(groups = {UpdateGroup.class, InsertGroup.class}, message = "用量统计来源不能为空")
    private Integer countSource;

    /**
     * 有效期-开始
     */
    @NotNull(groups = {UpdateGroup.class, InsertGroup.class}, message = "有效期-开始时间不能为空")
    private LocalDate validStart;

    /**
     * 有效期-结束
     */
    @NotNull(groups = {UpdateGroup.class, InsertGroup.class}, message = "有效期-开始时间不能为空")
    private LocalDate validEnd;

    /**
     * 备注
     */
    @Length(groups = {UpdateGroup.class, InsertGroup.class}, max = 50, message = "备注不能超过50个文字")
    private String note;

    private static final long serialVersionUID = 1L;
}