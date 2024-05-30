package com.steccn.mes.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

import lombok.Data;

/**
 * 设备表

 * @TableName mes_equipment
 */
@TableName(value ="mes_equipment")
@Data
public class MesEquipment implements Serializable {
    /**
     * id
     */
    @TableId
    private Long id;

    /**
     * 设备编码
     */
    private String equipmentCode;

    /**
     * 设备名称
     */
    private String equipmentName;

    /**
     * 节点上级编码
     */
    private String parentCode;

    /**
     * 节点所有上级编码（快速检索下级节点）
     */
    private String parentCodes;

    /**
     * 当前层级排序号（decimal类型）
     */
    private Integer treeSort;

    /**
     * 树节点的完整排序号，10位数字组成（快速整树排序）
     */
    private String treeSorts;

    /**
     * 是否是末级，是否叶子节点（0：否，1：是，char类型）
     */
    private String treeLeaf;

    /**
     * 节点的全名称（用“/”分隔，快速获取当前节点完整路径）
     */
    private String treeNames;

    /**
     * 节点层次级别（从0开始，decimal类型，快速分级查询，根据层级缩进）
     */
    private Integer treeLevel;

    /**
     * 节点状态（0：正常，1：删除，2：停用）
     */
    private String status;

    /**
     * 创建者用户编码
     */
    private String createBy;

    /**
     * 数据创建时间
     */
    private Date createDate;

    /**
     * 更新者用户编码
     */
    private String updateBy;

    /**
     * 数据更新时间
     */
    private Date updateDate;

    /**
     * 备注信息
     */
    private String remarks;

    /**
     * 分类信息关联字段
     */
    private String classification;

    /**
     * 部门
     */
    private String department;

    /**
     * 是否特种(0:是 1，否）
     */
    private String isSpecial;

    /**
     * 购置时间
     */
    private Date purchaseTime;

    /**
     * 生产厂家
     */
    private String manufacturer;

    /**
     * 型号
     */
    private String model;

    /**
     * 资产编码
     */
    private String propertyCode;

    /**
     * 是否是设备（0：是 1：否）
     */
    private String isEqp;

    /**
     * 位号
     */
    private String bitNumber;

    /**
     * 设备状态（0：运行，1：告警，2：待机，3：关机）
     */
    private String state;

    /**
     * 编码时是否跳过当前层级（0：是 ，1：否）
     */
    private String isSkip;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @TableField(exist = false)
    private List<MesEquipment> children;
}