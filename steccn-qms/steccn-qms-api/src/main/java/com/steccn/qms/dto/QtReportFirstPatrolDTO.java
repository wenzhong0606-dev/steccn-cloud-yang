package com.steccn.qms.dto;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Set;

/**
 * 首检、在线检DTO
 *
 * @author AndrewLuo
 * @date 2023/06/09
 */
@Data
public class QtReportFirstPatrolDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @NotNull(message = "日期不能为空")
    private LocalDate date;

    private LocalDate start;

    private LocalDate end;

    /**
     * 2巡检，4首检
     */
    @NotNull(message = "类型不能为空")
    private Integer checkTaskType;

    @NotEmpty(message = "产线id不能为空")
    private Set<Integer> productLineId;
}
