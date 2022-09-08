package com.steccn.qms.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 物料关联检验计划表
 * @TableName qt_material_link_check_plan
 */
@TableName(value ="qt_material_link_check_plan")
@Data
public class QtMaterialLinkCheckPlan implements Serializable {
    /**
     * 主键id
     */
    @TableId
    private Integer id;

    /**
     * 关联物料id
     */
    private Long materialId;

    /**
     * 已配检验类型
     */
    private Long checkType;

    /**
     * 已关联检验方案
     */
    private Integer checkPlanId;

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