package com.steccn.mes.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Data;

/**
 * 分类管理
 * @TableName mes_eqp_classification
 */
@TableName(value ="mes_eqp_classification")
@Data
public class MesEqpClassification implements Serializable {
    /**
     * ID
     */
    @TableId
    private String id;

    /**
     * 公司code
     */
    private String companyCode;

    /**
     * 分类编码
     */
    private String classificationCode;

    /**
     * 名称
     */
    private String name;

    /**
     * 分类类型（0组织分类1物料分类2点检分类3设备分类4点检方法5维保分类）
     */
    private String classifyType;

    /**
     * 分类状态（0启动 1停用）
     */
    private String classifyStatus;

    /**
     * 父级编号
     */
    private String parentCode;

    /**
     * 所有父级编号
     */
    private String parentCodes;

    /**
     * 本级排序号（升序）
     */
    private Integer treeSort;

    /**
     * 所有级别排序号
     */
    private String treeSorts;

    /**
     * 是否最末级
     */
    private String treeLeaf;

    /**
     * 层次级别
     */
    private Integer treeLevel;

    /**
     * 全节点名
     */
    private String treeNames;

    /**
     * 备注
     */
    private String remarks;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}