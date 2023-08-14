package com.steccn.mes.domain;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;
import lombok.Data;

/**
 * 客户详情表
 * @TableName js_sys_customer_detail
 */
@TableName(value ="js_sys_customer_detail")
@Data
public class JsSysCustomerDetail implements Serializable {
    /**
     * 主键id
     */
    @TableId
    private Integer id;

    /**
     * 客户code
     */
    private String officeCode;

    /**
     * 单据状态
     */
    private String customerStatus;

    /**
     * 禁用状态
     */
    private String forbidStatus;

    /**
     * 使用组织
     */
    private String orgName;

    /**
     * 审核人
     */
    private String approverName;

    /**
     * 审核日期
     */
    private LocalDateTime approveDate;

    /**
     * 客户分组
     */
    private String groupName;

    /**
     * 注册地址
     */
    private String registerAddress;

    /**
     * 纳税登记号
     */
    private String taxRegisterCode;

    /**
     * 发票抬头
     */
    private String invoiceTitle;

    /**
     * 开户银行
     */
    private String invoiceBankName;

    /**
     * 统一社会信用代码
     */
    private String socialCreCode;

    /**
     * 银行账号
     */
    private String invoiceBankAccount;

    /**
     * 开票联系电话
     */
    private String invoiceTel;

    /**
     * 开票通讯地址
     */
    private String invoiceAddress;

    /**
     * 联系人
     */
    private Object contactList;

    /**
     * 联系人名称
     */
    private String contactName;

    /**
     * 修改人
     */
    private String updateBy;

    /**
     * 修改时间
     */
    private LocalDateTime updateDate;

    /**
     * 创建人
     */
    private String createBy;

    /**
     * 创建时间
     */
    private LocalDateTime createDate;

    /**
     * 逻辑删除
     */
    @TableLogic
    private Integer delFlag;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}