package com.steccn.ems.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * @desc 化石燃料设置表
 */
@Data
public class CeProduceIngredientSettingVO implements Serializable {
    /**
     * 主键
     */
    private Integer id;

    /**
     * 物料id
     */
    private Long materialId;

    /**
     * 物料编码
     */
    private String materialCode;

    /**
     * 物料名称
     */
    private String materialName;

    /**
     * 排放因子
     */
    private BigDecimal factorValue;

    /**
     * 纯度
     */
    private BigDecimal purityValue;

    /**
     * 煅烧比例
     */
    private BigDecimal calcinationRatio;

    /**
     * 用量统计来源
     */
    private Integer countSource;

    /**
     * 有效期-开始
     */
    private LocalDate validStart;

    /**
     * 有效期-结束
     */
    private LocalDate validEnd;

    /**
     * 备注
     */
    private String note;

    /**
     * 创建人id
     */
    private Long createBy;

    /**
     * 创建人名称
     */
    private String createName;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    private static final long serialVersionUID = 1L;
}