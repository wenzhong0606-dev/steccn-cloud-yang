package com.steccn.qms.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class QtSpcReportDownloadVO implements Serializable {

    /**
     * 时间分片开始时间
     */
    private LocalDateTime startTime;

    /**
     * 时间分片结束时间
     */
    private LocalDateTime endTime;

    /**
     * 检验项名称
     */
    private String  checkItemName;

    /**
     * 计量单位
     */
    private String measureUnit;

    /**
     * 检验结果平均值
     */
    private BigDecimal checkItemResultAverage;

    /**
     * 标准值
     */
    private BigDecimal standardValue;

    /**
     * 上限
     */
    private BigDecimal upLimit;

    /**
     * 下限
     */
    private BigDecimal downLimit;
}
