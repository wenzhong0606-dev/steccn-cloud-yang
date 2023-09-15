package com.steccn.pms.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 报工记录工时VO
 *
 * @author AndrewLuo
 * @date 2023/09/15
 */
@Data
public class ProcessReportWorkHourVO implements Serializable {
    /**
     * 开始时间
     */
    private LocalDateTime startTime;
    /**
     * 结束时间
     */
    private LocalDateTime endTime;
    /**
     * 产线id
     */
    private Integer productLineId;
    /**
     * 班次时间
     */
    private LocalDate classDate;
    /**
     * 班次id
     */
    private Long classesId;
}
