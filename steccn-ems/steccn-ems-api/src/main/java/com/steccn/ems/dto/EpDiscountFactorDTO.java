package com.steccn.ems.dto;

import com.steccn.common.valid.InsertGroup;
import com.steccn.common.valid.UpdateGroup;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * @author yequan
 * @desc 折标系数-新增编辑对象
 */
@Data
public class EpDiscountFactorDTO implements Serializable {

    private final static long serialVersionUID = 1L;

    /**
     * 主键 - 编辑时使用
     */
    @NotNull(groups = {UpdateGroup.class}, message = "主键不能为空")
    private Integer id;

    /**
     * 系数名称
     */
    @NotEmpty(groups = {UpdateGroup.class, InsertGroup.class}, message = "系数名称不能为空")
    @Length(max = 50, min = 1, message = "系数名称不能超过50个文字")
    private String name;

    /**
     * 系数类型
     */
    @NotNull(groups = {UpdateGroup.class, InsertGroup.class}, message = "系数类型不能为空")
    private Long type;

    /**
     * 能源类型
     */
    private Integer energyType;

    /**
     * 系数值
     */
    @NotNull(groups = {UpdateGroup.class, InsertGroup.class}, message = "系数值不能为空")
    @DecimalMin(groups = {UpdateGroup.class, InsertGroup.class}, value = "0", message = "系数值应大于0")
    @DecimalMax(groups = {UpdateGroup.class, InsertGroup.class}, value = "999999999999.9999", message = "系数值应小于999999999999.9999")
    @Digits(groups = {UpdateGroup.class, InsertGroup.class}, integer = 12, fraction = 4, message = "整数位上限为12位，小数位上限位4位")
    private BigDecimal value;

    /**
     * 计量单位
     */
    private Long valueUnit;

    /**
     * 有效期
     */
    @NotNull(groups = {UpdateGroup.class, InsertGroup.class}, message = "有效期-开始时间不能为空")
    private LocalDate validStart;

    /**
     * 有效期
     */
    @NotNull(groups = {UpdateGroup.class, InsertGroup.class}, message = "有效期-结束时间不能为空")
    private LocalDate validEnd;

    /**
     * 备注
     */
    @Length(max = 100, message = "备注不能超过50个文字")
    private String note;
}
