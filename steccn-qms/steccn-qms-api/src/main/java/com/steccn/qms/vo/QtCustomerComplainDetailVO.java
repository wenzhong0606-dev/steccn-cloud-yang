package com.steccn.qms.vo;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 客诉记录明细
 */
@ApiModel("客诉记录明细DTO")
@Data
public class QtCustomerComplainDetailVO implements Serializable {
    /**
     * 主键
     */
    private Long id;

    /**
     * 箱号
     */
    private String caseNo;

    /**
     * 批次号
     */
    private String batchNo;

    /**
     * 物料主键
     */
    private Integer materialId;

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


}