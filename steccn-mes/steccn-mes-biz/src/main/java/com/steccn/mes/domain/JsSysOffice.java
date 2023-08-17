package com.steccn.mes.domain;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;
import lombok.Data;

/**
 * 组织机构表
 * @TableName js_sys_office
 */
@TableName(value ="js_sys_office")
@Data
public class JsSysOffice implements Serializable {
    /**
     * 机构编码
     */
    private String officeCode;

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
     * 机构代码
     */
    private String viewCode;

    /**
     * 机构名称    CUCC 公司表SUBCOMPANY_NAME
     */
    private String officeName;

    /**
     * 机构全称   CUCC 公司表SUBCOMPANY_DESC
     */
    private String fullName;

    /**
     * 机构类型
     */
    private String officeType;

    /**
     * 负责人
     */
    private String leader;

    /**
     * 办公电话
     */
    private String phone;

    /**
     * 联系地址
     */
    private String address;

    /**
     * 邮政编码
     */
    private String zipCode;

    /**
     * 电子邮箱
     */
    private String email;

    /**
     * 状态（0正常 1删除 2停用）
     */
    private String status;

    /**
     * 创建者
     */
    private String createBy;

    /**
     * 创建时间
     */
//    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createDate;

    /**
     * 更新者
     */
    private String updateBy;

    /**
     * 更新时间
     */
//    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateDate;

    /**
     * 备注信息
     */
    private String remarks;

    /**
     * 租户代码
     */
    private String corpCode;

    /**
     * 租户名称
     */
    private String corpName;

    /**
     * 扩展 String 1
     */
    private String extendS1;

    /**
     * 扩展 String 2
     */
    private String extendS2;

    /**
     * 扩展 String 3
     */
    private String extendS3;

    /**
     * 扩展 String 4
     */
    private String extendS4;

    /**
     * 扩展 String 5
     */
    private String extendS5;

    /**
     * 扩展 String 6
     */
    private String extendS6;

    /**
     * 扩展 String 7
     */
    private String extendS7;

    /**
     * 扩展 String 8
     */
    private String extendS8;

    /**
     * 扩展 Integer 1
     */
    private Long extendI1;

    /**
     * 扩展 Integer 2
     */
    private Long extendI2;

    /**
     * 扩展 Integer 3
     */
    private Long extendI3;

    /**
     * 扩展 Integer 4
     */
    private Long extendI4;

    /**
     * 扩展 Float 1
     */
    private BigDecimal extendF1;

    /**
     * 扩展 Float 2
     */
    private BigDecimal extendF2;

    /**
     * 扩展 Float 3
     */
    private BigDecimal extendF3;

    /**
     * 扩展 Float 4
     */
    private BigDecimal extendF4;

    /**
     * 扩展 Date 1
     */
    private Date extendD1;

    /**
     * 扩展 Date 2
     */
    private Date extendD2;

    /**
     * 扩展 Date 3
     */
    private Date extendD3;

    /**
     * 扩展 Date 4
     */
    private Date extendD4;

    /**
     * CUCC 公司表主键ID
     */
    private String employeeId;

    /**
     * CUCC 公司表
     */
    private Long supsubcomId;

    /**
     * CUCC 公司表URL
     */
    private String url;

    /**
     * CUCC 公司表
     */
    private BigDecimal showorder;

    /**
     * CUCC 公司表 
     */
    private String canceled;

    /**
     * CUCC 公司表 
     */
    private String subcompanyCode;

    /**
     * CUCC 公司表 
     */
    private String outkey;

    /**
     * CUCC 公司表 
     */
    private Long budgetAtuomoveOrder;

    /**
     * CUCC 公司表 
     */
    private String ecologyPinyinSearch;

    /**
     * CUCC 公司表 
     */
    private BigDecimal limitusers;

    /**
     * CUCC 公司表 
     */
    private BigDecimal tlevel;

    /**
     * CUCC 公司表
     */
    private String companyId;

    /**
     * CUCC 公司表
     */
    private BigDecimal ecologyId;

    /**
     * CUCC 部门表
     */
    private String organizationId;

    /**
     * CUCC 部门表
     */
    private String parentid;

    /**
     * CUCC 部门表
     */
    private String orgType;

    /**
     * CUCC 部门表
     */
    private String remark;

    /**
     * CUCC 部门表
     */
    private Long sort;

    /**
     * CUCC 部门表
     */
    private String enable;

    /**
     * CUCC 部门表
     */
    private String isDeleted;

    /**
     * CUCC 部门表
     */
    private Long orgTlevel;

    /**
     * CUCC 部门表
     */
    private String subcompanyId;

    /**
     * CUCC 部门表
     */
    private String orgEcologyId;

    /**
     * 扩展 JSON
     */
    private String extendJson;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}