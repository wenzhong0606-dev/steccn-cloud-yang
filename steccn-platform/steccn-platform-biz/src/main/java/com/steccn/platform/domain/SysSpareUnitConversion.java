package com.steccn.platform.domain;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 备件单位换算
 * @author yequan
 * @TableName sys_spare_unit_conversion
 */
@TableName(value ="sys_spare_unit_conversion")
@Data
public class SysSpareUnitConversion implements Serializable {
    /**
     * 主键
     */
    @TableId
    private Long id;

    /**
     * 备件id
     */
    private Long spareId;

    /**
     * 标准数量
     */
    private BigDecimal quantity;

    /**
     * 标准单位
     */
    private Long unitId;

    /**
     * 换算数量
     */
    private BigDecimal conversionQunantity;

    /**
     * 换算单位
     */
    private Long conversionUintId;

    /**
     * 创建人id
     */
    @TableField(fill = FieldFill.INSERT)
    private Long createBy;

    /**
     * 创建人名称
     */
    @TableField(fill = FieldFill.INSERT)
    private String createName;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新人id
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updateBy;

    /**
     * 更新人名称
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updateName;

    /**
     * 更新人时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 是否删除标识：0否 1是
     */
    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer delFlag;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}