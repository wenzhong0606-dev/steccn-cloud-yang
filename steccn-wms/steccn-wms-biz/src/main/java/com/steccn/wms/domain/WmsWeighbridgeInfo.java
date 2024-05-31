package com.steccn.wms.domain;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;
import lombok.Data;

/**
 * 地磅信息
 * @TableName wms_weighbridge_info
 */
@TableName(value ="wms_weighbridge_info")
@Data
public class WmsWeighbridgeInfo implements Serializable {
    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 物料id
     */
    private Long materialId;

    /**
     * 物料名称
     */
    private String materialName;

    /**
     * 物料编码
     */
    private String materialCode;

    /**
     * 流程单号
     */
    private String flowCode;

    /**
     * 车牌号
     */
    private String licensePlate;

    /**
     * 司机名称
     */
    private String driverName;

    /**
     * 联系方式
     */
    private String driverPhone;

    /**
     * 供应商id
     */
    private String companyId;

    /**
     * 供应商名称
     */
    private String companyName;

    /**
     * 毛重
     */
    private BigDecimal grossWeight;

    /**
     * 皮重
     */
    private BigDecimal tareWeight;

    /**
     * 净重
     */
    private BigDecimal netWeight;

    /**
     * 扣重
     */
    private BigDecimal deductWeight;

    /**
     * 完成时间
     */
    private LocalDateTime finishTime;

    /**
     * 是否生成入库单 0否 1是
     */
    private Integer isGenerateList;

    /**
     * 入库单号
     */
    private String receiptCode;

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

    /**
     * 备注
     */
    private String note;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}