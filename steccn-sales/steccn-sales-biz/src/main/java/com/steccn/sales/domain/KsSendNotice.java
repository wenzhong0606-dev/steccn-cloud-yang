package com.steccn.sales.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 发货通知单
 * @TableName ks_send_notice
 */
@TableName(value ="ks_send_notice")
@Data
public class KsSendNotice implements Serializable {
    /**
     * 
     */
    @TableId
    private Long id;

    /**
     * Main Attachment
     */
    private Object messageMainAttachmentId;

    /**
     * 销售员
     */
    private Object saleId;

    /**
     * 合同
     */
    private Object contractId;

    /**
     * 合同明细
     */
    private Object contractDetailsId;

    /**
     * 客户
     */
    private Object customerId;

    /**
     * 产品
     */
    private Object productId;

    /**
     * 销售数量(片)
     */
    private Object saleQty;

    /**
     * Created by
     */
    private Object createUid;

    /**
     * Last Updated by
     */
    private Object writeUid;

    /**
     * 发货通知单号
     */
    private String name;

    /**
     * 合同号
     */
    private String contractCode;

    /**
     * 客户合同号
     */
    private String customerCode;

    /**
     * 规格型号
     */
    private String modelSpecs;

    /**
     * 备注
     */
    private String note;

    /**
     * 发货日期
     */
    private Date sendDate;

    /**
     * 面积(m²)
     */
    private Integer priceQty;

    /**
     * 单价(元/m²)
     */
    private Integer unitPrice;

    /**
     * 金额
     */
    private Integer totalAmount;

    /**
     * Created on
     */
    private Date createDate;

    /**
     * Last Updated on
     */
    private Date writeDate;

    /**
     * 状态
     */
    private String state;

    /**
     * 已发货数量(片)
     */
    private Object sendQty;

    /**
     * 客户物料编码
     */
    private String customerProductCode;

    /**
     * 发货状态
     */
    private String sendStatus;

    /**
     * 合同明细数量
     */
    private Object contractDetailsQty;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}