package com.steccn.mes.dto;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * 温度点检项id
 *
 * @author AndrewLuo
 * @date 2023/11/13
 */
@Data
public class MesInsTaskTmpParamDTO {
    @NotEmpty(message = "点检项id不能为空")
    private Set<Long> insItemId;

    @NotNull(message = "日期不能为空")
    private LocalDate startDate;

    @NotNull(message = "日期不能为空")
    private LocalDate endDate;

    @NotEmpty(message = "产线code不能为空")
    private Set<String> productLineCode;

    private LocalDateTime startTime;

    private LocalDateTime endTime;
}
