package com.steccn.qms.vo;

import com.steccn.qms.dto.QtBaseIdNameDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 深加工合格率趋势明细表
 */
@Data
public class QtDeepOrSubAcceptabilityDetailVO implements Serializable {

    /**
     * 检验任务单号
     */
    private String code;

    /**
     * 平均 合格率
     */
    private BigDecimal acceptability;

    /**
     * 检验结果
     */
    private String result;

    /**
     * 检验完成时间
     */
    private String finishedTime;

    /**
     * 班次日期
     */
    private LocalDate classesDate;

    /**
     * 班次
     */
    private Integer classId;

    @ApiModelProperty("班次名称")
    private String className;

    /**
     * 生产车间id
     */
    private Integer productWorkshopId;
    /**
     * 生产车间名称
     */
    private String productWorkshopName;

    /**
     * 生产产线id
     */
    private Integer productLineId;

    /**
     * 生产产线名称
     */
    private String productLineName;

    /**
     *物料id
     */
    private Integer materialWorkshopId;

    /**
     * 物料名称
     */
    private String materialWorkshopName;


    @ApiModelProperty("物料名称")
    private String materialName;

    /**
     * 客户
     */
    private Integer customerId;

    /**
     * 客户名称
     */
    private String customerName;

    /**
     * 检验任务详情
     */
    private List<QtSubOrDeepFPYTaskDetailsVO> taskDetails;

}
