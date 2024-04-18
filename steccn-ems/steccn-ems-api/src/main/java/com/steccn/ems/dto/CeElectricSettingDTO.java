package com.steccn.ems.dto;

import com.steccn.common.valid.InsertGroup;
import com.steccn.common.valid.UpdateGroup;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * @author yequan
 * @desc 电力设置数据传输对象
 */
@Data
public class CeElectricSettingDTO implements Serializable {

    /**
     * 主键
     */
    @NotNull(groups = {UpdateGroup.class}, message = "主键不能为空")
    private Integer id;

    /**
     * 电力名称
     */
    @NotEmpty(groups = {UpdateGroup.class, InsertGroup.class}, message = "电力名称不能为空")
    @Length(max = 30, min = 1, message = "电力名称不能超过30个文字")
    private String name;

    /**
     * 排放因子
     */
    @NotNull(groups = {UpdateGroup.class, InsertGroup.class}, message = "排放因子不能为空")
    @DecimalMin(groups = {UpdateGroup.class, InsertGroup.class}, value = "0", message = "排放因子应大于0")
    @DecimalMax(groups = {UpdateGroup.class, InsertGroup.class}, value = "999999999999.9999", message = "排放因子应小于999999999999.9999")
    @Digits(groups = {UpdateGroup.class, InsertGroup.class}, integer = 12, fraction = 4, message = "排放因子上限为12位，小数位上限位4位")
    private BigDecimal factorValue;

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
