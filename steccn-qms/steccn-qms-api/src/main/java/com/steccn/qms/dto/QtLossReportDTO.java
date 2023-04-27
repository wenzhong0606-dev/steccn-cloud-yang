package com.steccn.qms.dto;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Set;

/**
 * 损耗日报表DTO
 *
 * @author AndrewLuo
 * @date 2023/04/20
 */
@Data
public class QtLossReportDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 产线id
     */
    @NotEmpty(message = "产线id不能为空")
    private Set<Integer> productLineIds;

    /**
     * 报表类型 班次0 日报表1 月报表2
     */
    @NotNull(message = "报表类型不能为空")
    @Min(value = 0,message = "类型只能是0 1 2")
    @Max(value = 2, message = "类型只能是0 1 2")
    private Integer type;

    /**
     * 开始日期
     */
    @NotNull(message = "开始时间不能为空")
    private LocalDate startDate;

    /**
     * 结束日期
     */
    @NotNull(message = "结束时间不能为空")
    private LocalDate endDate;
}
