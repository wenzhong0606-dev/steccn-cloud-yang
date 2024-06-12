package com.steccn.wms.domain;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;
import lombok.Data;

/**
 * 盘点扫描明细
 * @TableName wms_check_order_scan_info
 */
@TableName(value ="wms_check_order_scan_info")
@Data
public class WmsCheckOrderScanInfo implements Serializable {
    /**
     * 主键
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 物料id
     */
    private Long materialId;

    /**
     * 物料code
     */
    private String materialCode;

    /**
     * 物料名称
     */
    private String materialName;

    /**
     * 数量
     */
    private BigDecimal quantity;

    /**
     * 单位id
     */
    private Long unitId;

    /**
     * 盘点人id
     */
    private Long checkBy;

    /**
     * 盘点人名称
     */
    private String checkName;

    /**
     * 盘点时间
     */
    private LocalDateTime checkTime;

    /**
     * 备注
     */
    private String note;

    /**
     * 批次
     */
    private String batch;

    /**
     * 箱号
     */
    private String caseNo;

    /**
     * 仓库
     */
    private Integer warehouse;

    /**
     * 货架
     */
    private Integer shelf;

    /**
     * 盘点单id
     */
    private Long checkOrderId;
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