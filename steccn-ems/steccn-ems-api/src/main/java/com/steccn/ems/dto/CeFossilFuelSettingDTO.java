package com.steccn.ems.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * @desc 化石燃料设置表
 */
@Data
public class CeFossilFuelSettingDTO implements Serializable {
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
     * 低位发热量
     */
    private BigDecimal lowerHeatingValue;

    /**
     * 单位热值含碳量
     */
    private BigDecimal tceValue;

    /**
     * 碳氧化率
     */
    private BigDecimal coRatio;

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

    private static final long serialVersionUID = 1L;
}