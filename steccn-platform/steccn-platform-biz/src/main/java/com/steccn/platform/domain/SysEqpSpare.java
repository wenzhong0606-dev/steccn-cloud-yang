package com.steccn.platform.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Mes备品备件表
 *
 * @TableName sys_eqp_spare
 */
@TableName(value = "sys_eqp_spare")
@Data
public class SysEqpSpare implements Serializable {
    /**
     * 主键id
     */
    @TableId
    private Long id;

    /**
     * 编码
     */
    private String code;

    /**
     * 名称
     */
    private String name;

    /**
     * 规格型号
     */
    private String specifications;

    /**
     * 单位
     */
    private String unit;

    /**
     * 状态（0正常 1删除 2停用）
     */
    private String status;

    /**
     * 创建者
     */
    private String createBy;

    /**
     * 创建时间
     */
    private LocalDateTime createDate;

    /**
     * 更新者
     */
    private String updateBy;

    /**
     * 更新时间
     */
    private LocalDateTime updateDate;

    /**
     * 类型
     */
    private String type;

    /**
     * 业务状态
     */
    private String state;

    /**
     * 批次类型
     */
    private String batchType;

    /**
     * 备注
     */
    private String remarks;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    /**
     * 长度单位
     */
    private Long lengthUnitId;

    /**
     * 长度
     */
    private BigDecimal lengthQuantity;

    /**
     * 高度单位
     */
    private Long highUnitId;

    /**
     * 高度
     */
    private BigDecimal highQuantity;

    /**
     * 宽度单位
     */
    private Long wideUnitId;

    /**
     * 宽度
     */
    private BigDecimal wideQuantity;

    /**
     *厚度单位
     */
    private Long thickUnitId;

    /**
     * 厚度
     */
    private BigDecimal thickQuantity;

    /**
     * 体积
     */
    private BigDecimal volumeQuantity;

    /**
     * 体积单位
     */
    private Long volumeUnitId;

    /**
     * 重量
     */
    private BigDecimal weightQuantity;

    /**
     * 重量单位
     */
    private Long weightUnitId;

    /**
     * 采购类型id
     */
    private Long procurementTypeId;

    /**
     * 单位
     */
    private Long unitId;
}