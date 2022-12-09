package com.steccn.wms.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 出库单
 * @TableName wms_delivery_receipt
 */
@TableName(value ="wms_delivery_receipt")
@Data
public class WmsDeliveryReceipt implements Serializable {
    /**
     * 主键
     */
    @TableId
    private Long id;

    /**
     * 编码
     */
    private String code;

    /**
     * 类型
     */
    private Long type;

    /**
     * 状态
     */
    private Integer state;

    /**
     * 仓库id
     */
    private Long warehouseId;

    /**
     * 确认时间
     */
    private LocalDateTime affirmTime;

    /**
     * 确认人
     */
    private String affirmName;

    /**
     * 备注
     */
    private String note;

    /**
     * 创建人id
     */
    private Long createBy;

    /**
     * 创建人名称
     */
    private String createName;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新人id
     */
    private Long updateBy;

    /**
     * 更新人名称
     */
    private String updateName;

    /**
     * 更新人时间
     */
    private LocalDateTime updateTime;

    /**
     * 是否删除标识：0否 1是
     */
    private Integer delFlag;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}