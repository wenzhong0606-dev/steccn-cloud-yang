package com.steccn.mes.dto;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 缺陷图表DTO
 *
 * @author AndrewLuo
 * @date 2023/10/20
 */
@Data
public class MesDefectChartDTO implements Serializable {
    @NotNull(message = "开始时间不能为空")
    private LocalDateTime startTime;
    @NotNull(message = "结束时间不能为空")
    private LocalDateTime endTime;
    private List<Integer> levels;
}
