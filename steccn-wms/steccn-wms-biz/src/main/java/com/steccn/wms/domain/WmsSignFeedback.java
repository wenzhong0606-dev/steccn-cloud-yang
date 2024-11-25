package com.steccn.wms.domain;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import lombok.Data;

/**
 * 
 * @TableName wms_sign_feedback
 */
@TableName(value ="wms_sign_feedback")
@Data
public class WmsSignFeedback implements Serializable {
    /**
     * 
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 物流单号
     */
    private String salesOrder;

    /**
     * 客户
     */
    private String client;



    private String logisticsCompany;


    /**
     * 反馈状态（0:待确认反馈 1：已反馈）
     */
    private Integer state;


    private String carCode;

    private String product;

    /**
     * 发货日期
     */
    private Date sendDate;

    /**
     * 发货数量
     */
    private BigDecimal sendQuantity;

    /**
     * 发货面积
     */
    private BigDecimal priceQuantity;

    /**
     * 签收类型
     */
    private String signatureType;

    /**
     * 签收数量
     */
    private BigDecimal signedQuantity;

    /**
     * 签收面积
     */
    private BigDecimal signedPriceQuantity;

    /**
     * 签收日期
     */
    private LocalDate signedDate;


    /**
     * 异常原因
     */
    private String abnormalCause;

    /**
     * 文件id
     */
    private Long fileId;

    /**
     * 退回原因
     */
    private String sendBackReason;

    /**
     * 创建人id
     */
    @TableField(fill = FieldFill.INSERT)
    private Long createBy;

    /**
     * 创建人名称
     */
    @TableField(fill = FieldFill.INSERT)
    private String createName;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新人id
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updateBy;

    /**
     * 更新人名称
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updateName;

    /**
     * 更新人时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 是否删除标识：0否 1是
     */
    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer delFlag;

    /**
     * 备注
     */
    private String note;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (that == null) {
            return false;
        }
        if (getClass() != that.getClass()) {
            return false;
        }
        WmsSignFeedback other = (WmsSignFeedback) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getSalesOrder() == null ? other.getSalesOrder() == null : this.getSalesOrder().equals(other.getSalesOrder()))
            && (this.getClient() == null ? other.getClient() == null : this.getClient().equals(other.getClient()))
            && (this.getSendDate() == null ? other.getSendDate() == null : this.getSendDate().equals(other.getSendDate()))
            && (this.getSendQuantity() == null ? other.getSendQuantity() == null : this.getSendQuantity().equals(other.getSendQuantity()))
            && (this.getPriceQuantity() == null ? other.getPriceQuantity() == null : this.getPriceQuantity().equals(other.getPriceQuantity()))
            && (this.getSignatureType() == null ? other.getSignatureType() == null : this.getSignatureType().equals(other.getSignatureType()))
            && (this.getSignedQuantity() == null ? other.getSignedQuantity() == null : this.getSignedQuantity().equals(other.getSignedQuantity()))
            && (this.getSignedPriceQuantity() == null ? other.getSignedPriceQuantity() == null : this.getSignedPriceQuantity().equals(other.getSignedPriceQuantity()))
            && (this.getSignedDate() == null ? other.getSignedDate() == null : this.getSignedDate().equals(other.getSignedDate()))
            && (this.getAbnormalCause() == null ? other.getAbnormalCause() == null : this.getAbnormalCause().equals(other.getAbnormalCause()))
            && (this.getFileId() == null ? other.getFileId() == null : this.getFileId().equals(other.getFileId()))
            && (this.getCreateBy() == null ? other.getCreateBy() == null : this.getCreateBy().equals(other.getCreateBy()))
            && (this.getCreateName() == null ? other.getCreateName() == null : this.getCreateName().equals(other.getCreateName()))
            && (this.getCreateTime() == null ? other.getCreateTime() == null : this.getCreateTime().equals(other.getCreateTime()))
            && (this.getUpdateBy() == null ? other.getUpdateBy() == null : this.getUpdateBy().equals(other.getUpdateBy()))
            && (this.getUpdateName() == null ? other.getUpdateName() == null : this.getUpdateName().equals(other.getUpdateName()))
            && (this.getUpdateTime() == null ? other.getUpdateTime() == null : this.getUpdateTime().equals(other.getUpdateTime()))
            && (this.getDelFlag() == null ? other.getDelFlag() == null : this.getDelFlag().equals(other.getDelFlag()))
            && (this.getNote() == null ? other.getNote() == null : this.getNote().equals(other.getNote()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getSalesOrder() == null) ? 0 : getSalesOrder().hashCode());
        result = prime * result + ((getClient() == null) ? 0 : getClient().hashCode());
        result = prime * result + ((getSendDate() == null) ? 0 : getSendDate().hashCode());
        result = prime * result + ((getSendQuantity() == null) ? 0 : getSendQuantity().hashCode());
        result = prime * result + ((getPriceQuantity() == null) ? 0 : getPriceQuantity().hashCode());
        result = prime * result + ((getSignatureType() == null) ? 0 : getSignatureType().hashCode());
        result = prime * result + ((getSignedQuantity() == null) ? 0 : getSignedQuantity().hashCode());
        result = prime * result + ((getSignedPriceQuantity() == null) ? 0 : getSignedPriceQuantity().hashCode());
        result = prime * result + ((getSignedDate() == null) ? 0 : getSignedDate().hashCode());
        result = prime * result + ((getAbnormalCause() == null) ? 0 : getAbnormalCause().hashCode());
        result = prime * result + ((getFileId() == null) ? 0 : getFileId().hashCode());
        result = prime * result + ((getCreateBy() == null) ? 0 : getCreateBy().hashCode());
        result = prime * result + ((getCreateName() == null) ? 0 : getCreateName().hashCode());
        result = prime * result + ((getCreateTime() == null) ? 0 : getCreateTime().hashCode());
        result = prime * result + ((getUpdateBy() == null) ? 0 : getUpdateBy().hashCode());
        result = prime * result + ((getUpdateName() == null) ? 0 : getUpdateName().hashCode());
        result = prime * result + ((getUpdateTime() == null) ? 0 : getUpdateTime().hashCode());
        result = prime * result + ((getDelFlag() == null) ? 0 : getDelFlag().hashCode());
        result = prime * result + ((getNote() == null) ? 0 : getNote().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", salesOrder=").append(salesOrder);
        sb.append(", client=").append(client);
        sb.append(", sendDate=").append(sendDate);
        sb.append(", sendQuantity=").append(sendQuantity);
        sb.append(", priceQuantity=").append(priceQuantity);
        sb.append(", signatureType=").append(signatureType);
        sb.append(", signedQuantity=").append(signedQuantity);
        sb.append(", signedPriceQuantity=").append(signedPriceQuantity);
        sb.append(", signedDate=").append(signedDate);
        sb.append(", abnormalCause=").append(abnormalCause);
        sb.append(", fileId=").append(fileId);
        sb.append(", createBy=").append(createBy);
        sb.append(", createName=").append(createName);
        sb.append(", createTime=").append(createTime);
        sb.append(", updateBy=").append(updateBy);
        sb.append(", updateName=").append(updateName);
        sb.append(", updateTime=").append(updateTime);
        sb.append(", delFlag=").append(delFlag);
        sb.append(", note=").append(note);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}