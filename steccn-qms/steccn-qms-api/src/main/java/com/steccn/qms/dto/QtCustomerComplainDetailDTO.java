package com.steccn.qms.dto;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.NonNull;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 客诉记录明细
 */
@ApiModel("客诉记录明细DTO")
@Data
public class QtCustomerComplainDetailDTO implements Serializable {
    /**
     * 主键
     */
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
//    @NotNull(message = "物料主键不能为空")
    private Long materialId;

    /**
     * 物料名称
     */
//    @NotNull(message = "物料名称不能为空")
    private String materialName;

    /**
     * 物料编码
     */
//    @NotNull(message = "物料编码不能为空")
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


}