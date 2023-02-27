package com.steccn.qms.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 类说明
 *
 * @author Mongknife
 * @date 2023/2/24
 */
@Data
public class QtReportWarnDTO {
    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;

    /**
     * 粒度:小时、天、周、月、季度、年
     */
    private String granularity;
}
