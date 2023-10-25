package com.steccn.qms.domain;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 客诉记录明细表
 * @TableName qms_customer_complain_detail
 */
@TableName(value ="qt_customer_complain_detail")
@Data
public class QtCustomerComplainDetail implements Serializable {
    /**
     * 主键
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;


    /**
     * 客户条码
     */
    private String customerBarcode;

    /**
     * 客户定制箱号
     */
    private String clientCaseNo;

    /**
     * 箱号
     */
    private String caseNo;

    /**
     * 批次号
     */
    private String batchNo;

    /**
     * 生产订单号
     */
    private String produceOrderCode;

    /**
     * 物料主键
     */
    private Long materialId;

    /**
     * 物料名称
     */
    private String materialName;

    /**
     * 物料编码
     */
    private String materialCode;

    /**
     * 发货数量
     */
    private BigDecimal deliverQuantity;

    /**
     * 投诉数量
     */
    private BigDecimal complainQuantity;

    /**
     * 上线数量
     */
    private BigDecimal onlineQuantity;

    /**
     * 不良率
     */
    private BigDecimal defectRate;

    /**
     * 抽检数量
     */
    private BigDecimal spotQuantity;

    /**
     * 抽检率
     */
    private BigDecimal spotRate;

    /**
     * 勘查/确认日期
     */
    private LocalDate surveyDate;

    /**
     * 不良判定依据
     */
    private Long badJudgeBasis;

    /**
     * 勘查/确认详细结果
     */
    private String surveyResult;

    /**
     * 勘查/确认初步结论
     */
    private String surveyConclusion;

    /**
     * 客诉主键
     */
    private Long customerComplainId;

    /**
     * 备注
     */
    private String remarks;

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

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}