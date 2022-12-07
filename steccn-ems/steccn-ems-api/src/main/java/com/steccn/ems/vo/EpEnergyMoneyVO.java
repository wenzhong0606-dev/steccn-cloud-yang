package com.steccn.ems.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 电耗分布VO
 *
 * @author AndrewLuo
 * @date 2022/12/07
 */
@Data
public class EpEnergyMoneyVO implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 班次日期
     */
    private LocalDate classDate;

    /**
     * 分区节点
     */
    private String partitionName;

    /**
     * 时段 峰 谷 平
     */
    private String typeName;

    /**
     * 峰0 谷1 平2
     */
    private Integer type;

    /**
     * 开始时间
     */
    private LocalDateTime start;

    /**
     * 结束时间
     */
    private LocalDateTime end;

    /**
     * 单价数值
     */
    @JsonIgnore
    private BigDecimal price;

    /**
     * 单价字符串
     */
    private String priceStr;

    /**
     * 电耗
     */
    private String consumption;

    /**
     * 电耗数值
     */
    @JsonIgnore
    private BigDecimal consumptionNum;

    /**
     * 电费
     */
    private String fee;

    /**
     * 电费数值
     */
    @JsonIgnore
    private BigDecimal feeNum;

}
