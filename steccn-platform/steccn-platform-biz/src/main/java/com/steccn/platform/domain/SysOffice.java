package com.steccn.platform.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * Mes组织机构表
 * @TableName sys_office
 */
@TableName(value ="sys_office")
@Data
public class SysOffice implements Serializable {
    /**
     * 主键id
     */
    @TableId
    private Integer id;
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
    private LocalDateTime createDate;

    /**
     * 更新者
     */
    private String updateBy;

    /**
     * 更新时间
     */
    private LocalDateTime updateDate;

    /**
     * 备注信息
     */
    private String remarks;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}