package com.steccn.pms.domain;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 生产领用单详情
 * @author yequan
 * @TableName pms_material_requisition_info
 */
@TableName(value ="pms_material_requisition_info",autoResultMap = true)
@Data
public class PmsMaterialRequisitionInfo implements Serializable {
    /**
     * 主键
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 机构id
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<Integer> organization;

    /**
     * 货架id
     */
    private Integer shelfId;

    /**
     * 物料id
     */
    private Long materialId;

    /**
     * 物料编码
     */
    private String materialCode;

    /**
     * 需求时间
     */
    private LocalDate requireDate;

    /**
     * 数量
     */
    private BigDecimal quantity;

    /**
     * 领用数量
     */
    private BigDecimal receiveQuantity;

    /**
     * 关联单据
     */
    private String associatedNo;


    /**
     * 单位id
     */
    private Long unitId;

    /**
     * 生产领料单id
     */
    private Long materialRequisitionId;

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