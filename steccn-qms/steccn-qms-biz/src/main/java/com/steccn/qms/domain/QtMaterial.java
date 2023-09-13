package com.steccn.qms.domain;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.steccn.qms.dto.QtMaterialLinkCheckPlanTypeDTO;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 物料表
 * @TableName qt_material
 */
@TableName(value ="qt_material" ,autoResultMap = true)
@Data
public class QtMaterial implements Serializable {
    /**
     * 主键
     */
    @TableId
    @NotNull
    private Long id;

    /**
     * 物料编码
     */
    private String code;

    /**
     * 物料名称
     */
    private String name;

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

    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<QtMaterialLinkCheckPlanTypeDTO> type;

    /**
     * 启用1 停用0
     */
    private Integer status;

    /**
     * 长度
     */
    private BigDecimal lengthQuantity;

    /**
     * 长度单位
     */
    private Long lengthUnitId;


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
     * 单位
     */
    private String unit;

    /**
     * 单位
     */
    private Long unitId;

    /**
     * 采购类型id
     */
    private Long procurementTypeId;

    /**
     * 业务状态
     */
    private String state;

    /**
     * 批次类型
     */
    private String batchType;
}