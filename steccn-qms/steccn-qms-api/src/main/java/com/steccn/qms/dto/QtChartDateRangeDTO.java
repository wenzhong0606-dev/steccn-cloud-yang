package com.steccn.qms.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * 时间范围
 *
 * @author AndrewLuo
 * @date 2022/11/11
 */
@Data
public class QtChartDateRangeDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    @NotNull(message = "日期不能为空")
    private LocalDate startTime;
    @NotNull(message = "日期不能为空")
    private LocalDate endTime;
}
