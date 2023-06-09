package com.steccn.pms.vo;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@ApiModel("报工工时信息")
@Data
public class PmsReportWorkHourVO {

    /**
     * 主键
     */
    private Long id;

    /**
     * 原因（字典：工时原因）
     */
    private Long causeId;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;

    /**
     * 工时
     */
    private BigDecimal manHour;

    /**
     * 工时单位（字典时间单位）
     */
    private Long unitId;

    /**
     * 备注
     */
    private String note;

    /**
     * 工序报工id
     */
    private Long reportWorkId;
}
