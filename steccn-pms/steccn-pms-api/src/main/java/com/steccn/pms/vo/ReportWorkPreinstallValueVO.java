package com.steccn.pms.vo;

import io.swagger.annotations.ApiOperation;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@ApiOperation("报工预填值")
public class ReportWorkPreinstallValueVO {

    /**
     * 班次
     */
    private String classOrder;
    /**
     * 班组
     */
    private String classGroup;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;
}
