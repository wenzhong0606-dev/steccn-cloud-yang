package com.steccn.pms.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 领用单明细需求人
 * @TableName pms_material_requisition_demander
 */
@TableName(value ="pms_material_requisition_demander")
@Data
public class PmsMaterialRequisitionDemander implements Serializable {
    /**
     * id
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 生产领用单id
     */
    private Long materialRequisitionId;

    /**
     * 领用单明细id
     */
    private Long requisitionInfoId;

    /**
     * 需求人id
     */
    private Long demanderId;

    /**
     * 需求人名称
     */
    private String demanderName;

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
    private Date createTime;

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
    private Date updateTime;

    /**
     * 是否删除标识：0否 1是
     */
    private Integer delFlag;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}