package com.steccn.sales.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 物流公司
 * @TableName ks_logistics_company
 */
@TableName(value ="ks_logistics_company")
@Data
public class KsLogisticsCompany implements Serializable {
    /**
     * 
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Integer id;

    /**
     * Main Attachment
     */
    private Object messageMainAttachmentId;

    /**
     * Created by
     */
    private Object createUid;

    /**
     * Last Updated by
     */
    private Object writeUid;

    /**
     * 名称
     */
    private String name;

    /**
     * 编码
     */
    private String code;

    /**
     * 联系电话
     */
    private String telephone;

    /**
     * 联系人
     */
    private String contacts;

    /**
     * Created on
     */
    private Date createDate;

    /**
     * Last Updated on
     */
    private Date writeDate;

    /**
     * 权重(%)
     */
    private Object ratio;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}