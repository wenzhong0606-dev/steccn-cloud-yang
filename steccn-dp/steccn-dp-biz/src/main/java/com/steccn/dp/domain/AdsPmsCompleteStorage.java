package com.steccn.dp.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 各车间产线每日入库统计
 * @TableName ads_pms_complete_storage
 */
@TableName(value ="ads_pms_complete_storage")
@Data
public class AdsPmsCompleteStorage implements Serializable {
    /**
     * 车间id
     */
    @TableId
    private Long workshopId;

    /**
     * 车间
     */
    private String workshopName;

    /**
     * 产线id
     */
    private Integer productLineId;

    /**
     * 产线
     */
    private String productLineName;

    /**
     * 班组id
     */
    private Long classGroupId;

    /**
     * 班组
     */
    private String classGroupName;

    /**
     * 班次id
     */
    private Long classesId;

    /**
     * 班次
     */
    private String classesName;

    /**
     * 数量单位
     */
    private Long unitId;

    /**
     * 统计日期
     */
    private LocalDate dt;

    /**
     * 
     */
    private Long materialId;

    /**
     * 
     */
    private String materialName;

    /**
     * 
     */
    private String materialCode;

    /**
     * 最近1天入库数
     */
    private BigDecimal quantity1d;

    /**
     * 最近7天入库数
     */
    private BigDecimal quantity7d;

    /**
     * 最近1月入库数
     */
    private BigDecimal quantity1m;

    /**
     * 最近1季度入库数
     */
    private BigDecimal quantity3m;

    /**
     * 最近1年入库数
     */
    private BigDecimal quantity1y;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}