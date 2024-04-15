package com.steccn.pms.domain;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import lombok.Data;

/**
 * 劳保记录表
 * @TableName pms_labor_protection_record
 */
@TableName(value ="pms_labor_protection_record")
@Data
public class PmsLaborProtectionRecord implements Serializable {
    /**
     * 主键id
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 部门编码
     */
    private String depCode;

    /**
     * 岗位编码
     */
    private String postCode;

    /**
     * 用户名
     */
    private String loginCode;

    /**
     * 用户姓名
     */
    private String loginName;

    /**
     * 入职时间 
     */
    private LocalDate startTime;

    /**
     * 物料id
     */
    private Long materialId;

    /**
     * 领用时间
     */
    private LocalDateTime receiptTime;

    /**
     * 领用数量 
     */
    private BigDecimal receiptNum;

    /**
     * 领用单编码
     */
    private String receiptCode;

    /**
     * 领用仓库
     */
    private String receiptWarehouse;

    /**
     * 领用人登录名
     */
    private String receiptPersonCode;

    /**
     * 领用人姓名 
     */
    private String receiptPersonName;

    /**
     * 领用单id
     */
    private Integer receiptId;

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
     * 更新人时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

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
     * 是否删除标识：0否 1是
     */
    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer delFlag;

    /**
     * 备注
     */
    private String note;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}