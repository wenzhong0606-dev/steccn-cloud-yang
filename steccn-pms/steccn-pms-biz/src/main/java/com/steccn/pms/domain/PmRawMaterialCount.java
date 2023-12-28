package com.steccn.pms.domain;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import lombok.Data;

/**
 * 原料配料统计表
 * @TableName pm_raw_material_count
 */
@TableName(value ="pm_raw_material_count")
@Data
public class PmRawMaterialCount implements Serializable {
    /**
     * 主键id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 电子称编码
     */
    private String tagId;

    /**
     * 物料编码
     */
    private String materialCode;

    /**
     * 值
     */
    private BigDecimal value;

    /**
     * 日期
     */
    private LocalDate classDate;

    /**
     * 物料id
     */
    private Long materialId;

    /**
     * 出库单id
     */
    private Long deliveryId;

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
    private Integer delFlag;

    /**
     * 备注
     */
    private String note;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}