package com.steccn.sales.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * Product Variant
 * @TableName product_product
 */
@TableName(value ="product_product")
@Data
public class ProductProduct implements Serializable {
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
     * Product Template
     */
    private Object productTmplId;

    /**
     * Created by
     */
    private Object createUid;

    /**
     * Last Updated by
     */
    private Object writeUid;

    /**
     * Internal Reference
     */
    private String defaultCode;

    /**
     * Barcode
     */
    private String barcode;

    /**
     * Combination Indices
     */
    private String combinationIndices;

    /**
     * Volume
     */
    private Integer volume;

    /**
     * Weight
     */
    private Integer weight;

    /**
     * Active
     */
    private Boolean active;

    /**
     * Can Variant Image 1024 be zoomed
     */
    @TableField("can_image_variant_1024_be_zoomed")
    private Boolean canImageVariant1024BeZoomed;

    /**
     * Created on
     */
    private Date createDate;

    /**
     * Last Updated on
     */
    private Date writeDate;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}