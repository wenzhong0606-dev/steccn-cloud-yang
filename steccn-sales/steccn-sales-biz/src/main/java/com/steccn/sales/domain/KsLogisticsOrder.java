package com.steccn.sales.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import lombok.Data;

/**
 * 物流单
 * @TableName ks_logistics_order
 */
@TableName(value ="ks_logistics_order")
@Data
public class KsLogisticsOrder implements Serializable {
    /**
     * 
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * Main Attachment
     */
    private Integer messageMainAttachmentId;

    /**
     * 发货通知单
     */
    private Long sendNoticeId;

    /**
     * 客户
     */
    private Integer customerId;

    /**
     * 产品
     */
    private Long productId;

    /**
     * 发货数量(片)
     */
    private BigDecimal saleQty;

    /**
     * 客户收货地址
     */
    private Integer customerDeliveryId;

    /**
     * 预约入厂
     */
    private Integer enterFactoryId;

    /**
     * 物流公司
     */
    private Integer logisticsCompanyId;

    /**
     * Created by
     */
    private Integer createUid;

    /**
     * Last Updated by
     */
    private Integer writeUid;

    /**
     * 物流单号
     */
    private String name;

    /**
     * 客户合同号
     */
    private String customerCode;

    /**
     * 规格型号
     */
    private String modelSpecs;

    /**
     * 收货地址
     */
    private String driverAdds;

    /**
     * 预约时段
     */
    private String timeSpan;

    /**
     * 司机姓名
     */
    private String driverName;

    /**
     * 车牌号
     */
    private String carCode;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 发货日期
     */
    private LocalDate sendDate;

    /**
     * 预约日期
     */
    private LocalDate makeDate;

    /**
     * 签收日期
     */
    private LocalDate signDate;

    /**
     * 面积(m²)
     */
    private BigDecimal priceQty;

    /**
     * Created on
     */
    private LocalDateTime createDate;

    /**
     * Last Updated on
     */
    private LocalDateTime writeDate;

    /**
     * 重量(吨)
     */
    private BigDecimal weight;

    /**
     * 预估运费
     */
    private BigDecimal deliveryFreight;

    /**
     * 签收数量(片)
     */
    private BigDecimal signQty;

    /**
     * 包装类型
     */
    private String packType;

    /**
     * 状态
     */
    private String state;

    /**
     * 签收类型
     */
    private String signType;

    /**
     * 签收面积(m²)
     */
    private BigDecimal signArea;

    /**
     * 件数
     */
    private BigDecimal pieceQty;

    /**
     * 客户物料编码
     */
    private String customerProductCode;

    /**
     * 出库数量(片)
     */
    private BigDecimal outQty;

    /**
     * 单据来源
     */
    private String orderOrigin;

    /**
     * 实际运费(元)
     */
    private BigDecimal realityFreight;

    /**
     * 单价(元/m²)
     */
    private BigDecimal unitPrice;

    /**
     * 金额
     */
    private BigDecimal totalAmount;

    /**
     * 销售员
     */
    private Integer saleId;

    /**
     * 合同号
     */
    private String contractCode;

    /**
     * 联系电话
     */
    private String driverPhone;

    /**
     * 合同明细数量
     */
    private BigDecimal contractDetailsQty;

    /**
     * 出库单号
     */
    private String deliveryCode;

    /**
     * 客户条码
     */
    private String customerBarcode;

    /**
     * 原发货数量(片)
     */
    private BigDecimal oldSaleQty;

    /**
     * 仓库名称
     */
    private String warehouseNames;

    private LocalDateTime allocationDate;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}