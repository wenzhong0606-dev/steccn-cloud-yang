package com.steccn.qms.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 基片缺陷全量数据表
 *
 * @author AndrewLuo
 * @date 2022/11/10
 */
@Data
public class QtChartSubDetailDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 班次时间
     */
    private String workOrderTime;

    /**
     * 班次时间的date类型
     */
    private LocalDate workOrderDate;

    /**
     * 班次
     */
    private String workOrder;

    /**
     * 生产车间id
     */
    private Integer productWorkShopId;

    /**
     * 生产车间名
     */
    private String productWorkShopName;

    /**
     * 产线id
     */
    private Integer productLineId;

    /**
     * 产线名
     */
    private String productLineName;

    /**
     * 损耗原因
     */
    private String reason;

    /**
     * 损耗重量
     */
    private BigDecimal lossWeight;

    /**
     * 损耗重量字符串
     */
    private String lossWeightStr;

    /**
     * 拉引量
     */
    private BigDecimal pullAmount;

    /**
     * 拉引量字符串
     */
    private String pullAmountStr;

    /**
     * 班组id
     */
    private Integer classGroupId;

    /**
     * 班组名字
     */
    private String classGroupName;

}
