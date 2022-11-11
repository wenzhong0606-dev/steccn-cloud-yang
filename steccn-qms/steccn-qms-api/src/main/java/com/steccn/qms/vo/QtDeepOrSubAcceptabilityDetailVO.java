package com.steccn.qms.vo;

import com.steccn.qms.dto.QtBaseIdNameDTO;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

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
     * 合格率
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
    private String classesDate;

    /**
     * 班次
     */
    private Integer classId;

    /**
     * 生产车间
     */
    private QtBaseIdNameDTO productWorkshop;

    /**
     * 生产产线
     */
    private QtBaseIdNameDTO productLine;

    /**
     * 物料
     */
    private QtBaseIdNameDTO materialWorkshop;

    /**
     * 客户
     */
    private QtBaseIdNameDTO customer;

}
