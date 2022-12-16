package com.steccn.ems.domain;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * 产品单耗标准
 * @TableName ep_product_unit_consume_standard
 */
@TableName(value ="ep_product_unit_consume_standard", autoResultMap = true)
@Data
public class EpProductUnitConsumeStandard implements Serializable {
    /**
     * 主键id
     */
    @TableId
    private Integer id;

    /**
     * 标准编码
     */
    private String code;

    /**
     * 标准名称
     */
    private String name;

    /**
     * 能源类型
     */
    private Integer energyType;

    /**
     * 标准类型【企业标准、行业标准、部门标准】
     */
    private Long standardType;

    /**
     * 应用部门
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Set<Integer> applyOffice;

    /**
     * 产品类型【基片0、深加工1】
     */
    private Long productType;

    /**
     * 产品厚度
     */
    private Long productThick;

    /**
     * 单耗标准值
     */
    private BigDecimal unitValue;

    /**
     * 计量单位
     */
    private Long measureUnit;

    /**
     * 有效期-开始
     */
    private LocalDate validStart;

    /**
     * 有效期-结束
     */
    private LocalDate validEnd;

    /**
     * 备注
     */
    private String note;

    /**
     * 是否删除 0 否 1是
     */
    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer delFlag;

    /**
     * 是否可见 0 否 1 是
     */
    private Integer showFlag;

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
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}