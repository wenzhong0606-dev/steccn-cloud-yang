package com.steccn.pms.dto;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * 甘特图DTO
 *
 * @author AndrewLuo
 * @date 2023/11/02
 */
@Data
public class PmsProduceOrderGanttChartDTO {
    @NotNull(message = "开始时间不能为空")
    private LocalDate startTime;

    @NotNull(message = "结束时间不能为空")
    private LocalDate endTime;

    @NotNull(message = "类型不能为空")
    @Min(value = 0)
    @Max(value = 2)
    //0表示全部 1表示深加工 2表示基片
    private Integer type;
}
