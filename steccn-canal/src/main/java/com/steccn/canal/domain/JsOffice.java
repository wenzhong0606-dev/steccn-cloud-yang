package com.steccn.canal.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import javax.persistence.Column;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class JsOffice implements Serializable {
    /**
     * 机构编码
     */
    @Column(name = "office_code")
    private String officeCode;

    /**
     * 父级编号
     */
    @Column(name =  "parent_code")
    private String parentCode;

    /**
     * 所有父级编号
     */
    @Column(name = "parent_codes")
    private String parentCodes;

    /**
     * 本级排序号（升序）
     */
    @Column(name = "tree_sort")
    private Integer treeSort;

    /**
     * 所有级别排序号
     */
    @Column(name = "tree_sorts")
    private String treeSorts;

    /**
     * 是否最末级
     */
    @Column(name = "tree_leaf")
    private String treeLeaf;

    /**
     * 层次级别
     */
    @Column(name = "tree_level")
    private Integer treeLevel;

    /**
     * 全节点名
     */
    @Column(name = "tree_names")
    private String treeNames;

    /**
     * 机构代码
     */
    @Column(name = "view_code")
    private String viewCode;

    /**
     * 机构名称
     */
    @Column(name = "office_name")
    private String officeName;

    /**
     * 机构全称
     */
    @Column(name = "full_name")
    private String fullName;

    /**
     * 机构类型
     */
    @Column(name = "office_type")
    private String officeType;

    /**
     * 负责人
     */
    @Column(name = "leader")
    private String leader;

    /**
     * 办公电话
     */
    @Column(name = "phone")
    private String phone;

    /**
     * 联系地址
     */
    @Column(name = "address")
    private String address;

    /**
     * 邮政编码
     */
    @Column(name = "zip_code")
    private String zipCode;

    /**
     * 电子邮箱
     */
    @Column(name = "email")
    private String email;

    /**
     * 状态（0正常 1删除 2停用）
     */
    @Column(name = "status")
    private String status;

    /**
     * 创建者
     */
    @Column(name = "create_by")
    private String createBy;

    /**
     * 创建时间
     */
    @Column(name = "create_date")
    private LocalDateTime createDate;

    /**
     * 更新者
     */
    @Column(name = "update_by")
    private String updateBy;

    /**
     * 更新时间
     */
    @Column(name = "update_date")
    private LocalDateTime updateDate;

    /**
     * 备注信息
     */
    @Column(name = "remarks")
    private String remarks;
}
