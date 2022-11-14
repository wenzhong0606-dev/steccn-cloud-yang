package com.steccn.qms.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 配料抽检合格率明细表的下拉详情
 */
@Data
public class QtAccessoryAcceptablityCopyVO implements Serializable {
    /**
     * 样本序号
     */
    private String sampleNum;

    /**
     * 配料付数
     */
    private Integer ingredientNum;

    /**
     *取样时间
     */
    private LocalDateTime samplingTime;

    /**
     * 水份
     */
    private BigDecimal moisture;

    /**
     * 含碱量平均值
     */
    private BigDecimal alkalinityAverage;

    /**
     * 含碱量均方差
     */
    private BigDecimal alkalinityMse;
}
