package com.steccn.pms.domain;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 物料清单表
 * @TableName pm_bill_of_material
 */
@TableName(value ="pm_bill_of_material")
@Data
public class PmBillOfMaterial implements Serializable {
    /**
     * 表主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 清单编码
     */
    private String code;

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
     * 清单状态：1有效0无效
     */
    private Integer status;

    /**
     * 标准数量
     */
    private Object quantity;

    /**
     * 标准数量单位ID
     */
    private Long unitId;

    /**
     * 版本
     */
    private String version;

    /**
     * 是否是当前版本：1是0否
     */
    private Integer isCurrentVersion;

    /**
     * 有效开始时间
     */
    private LocalDateTime validBeginTime;

    /**
     * 有效结止时间
     */
    private LocalDateTime validEndTime;

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
    @TableLogic
    private Integer delFlag;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}