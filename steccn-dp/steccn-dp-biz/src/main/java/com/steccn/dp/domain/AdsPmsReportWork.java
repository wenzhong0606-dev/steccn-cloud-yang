package com.steccn.dp.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 各车间产线工序每日报工统计
 * @TableName ads_pms_report_work
 */
@TableName(value ="ads_pms_report_work")
@Data
public class AdsPmsReportWork implements Serializable {
    /**
     * 统计日期
     */
    @TableId
    private LocalDate dt;

    /**
     * 车间id
     */
    private Long workshopId;

    /**
     * 产线id
     */
    private Integer productLineId;

    /**
     * 班组id
     */
    private Long classGroupId;

    /**
     * 班次id
     */
    private Long classesId;

    /**
     * 工序id
     */
    private Long processId;

    /**
     * 物料id
     */
    private Long materialId;

    /**
     * 车间
     */
    private String workshopName;

    /**
     * 产线
     */
    private String productLineName;

    /**
     * 班组
     */
    private String classGroupName;

    /**
     * 班次
     */
    private String classesName;

    /**
     * 工序id
     */
    private String processCode;

    /**
     * 工序id
     */
    private String processName;

    /**
     * 物料code
     */
    private String materialCode;

    /**
     * 物料name
     */
    private String materialName;

    /**
     * 最近1天报工合格数
     */
    private BigDecimal processQuantity1d;

    /**
     * 最近1天报工不合格数
     */
    private BigDecimal badnessQuantity1d;

    /**
     * 最近1天报工总数
     */
    private BigDecimal allQuantity1d;

    /**
     * 最近7天报工合格数
     */
    private BigDecimal processQuantity7d;

    /**
     * 最近7天报工不合格数
     */
    private BigDecimal badnessQuantity7d;

    /**
     * 最近7天报工总数
     */
    private BigDecimal allQuantity7d;

    /**
     * 最近1月报工合格数
     */
    private BigDecimal processQuantity1m;

    /**
     * 最近1月报工不合格数
     */
    private BigDecimal badnessQuantity1m;

    /**
     * 最近1月报工总数
     */
    private BigDecimal allQuantity1m;

    /**
     * 最近1个季度报工合格数
     */
    private BigDecimal processQuantity3m;

    /**
     * 最近1个季度报工不合格数
     */
    private BigDecimal badnessQuantity3m;

    /**
     * 最近1个季度报工总数
     */
    private BigDecimal allQuantity3m;

    /**
     * 最近1年报工合格数
     */
    private BigDecimal processQuantity1y;

    /**
     * 最近1年报工不合格数
     */
    private BigDecimal badnessQuantity1y;

    /**
     * 最近1年报工总数
     */
    private BigDecimal allQuantity1y;

    /**
     * 单位id
     */
    private Long unitId;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}