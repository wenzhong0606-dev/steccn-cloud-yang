package com.steccn.sales.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 合同
 * @TableName ks_contract
 */
@TableName(value ="ks_contract")
@Data
public class KsContract implements Serializable {
    /**
     * 
     */
    @TableId
    private Object id;

    /**
     * Main Attachment
     */
    private Object messageMainAttachmentId;

    /**
     * 框架合同
     */
    private Object frameContractId;

    /**
     * 销售组织
     */
    private Object companyId;

    /**
     * 客户
     */
    private Object customerId;

    /**
     * 收款条件
     */
    private Object paymentTermId;

    /**
     * 销售部门
     */
    private Object saleDepartmentId;

    /**
     * 销售员
     */
    private Object saleId;

    /**
     * Created by
     */
    private Object createUid;

    /**
     * Last Updated by
     */
    private Object writeUid;

    /**
     * 单据类型
     */
    private String orderType;

    /**
     * 合同号
     */
    private String name;

    /**
     * 客户合同号
     */
    private String customerCode;

    /**
     * 合同名称
     */
    private String contractName;

    /**
     * 业务类型
     */
    private String businessType;

    /**
     * 交货方式
     */
    private String deliveryMethod;

    /**
     * 交货地点
     */
    private String placeDelivery;

    /**
     * 结算币别
     */
    private String settleCurrency;

    /**
     * 核价方式
     */
    private String checkMethod;

    /**
     * 单据状态
     */
    private String orderStatus;

    /**
     * 备注
     */
    private String note;

    /**
     * 状态
     */
    private String state;

    /**
     * 日期
     */
    private Date contractDate;

    /**
     * 是否含税
     */
    private Boolean isTax;

    /**
     * Created on
     */
    private Date createDate;

    /**
     * Last Updated on
     */
    private Date writeDate;

    /**
     * 审批结果
     */
    private String approveResult;

    /**
     * 请求返回ID
     */
    private Object requestId;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}