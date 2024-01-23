package com.steccn.pms.dto;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

/**
 * 拉引量报表DTO
 *
 * @author AndrewLuo
 * @date 2024/01/18
 */
@Data
public class PmsPullForceDashboardDTO {
    /**
     * 基片制造部，深加工制造部的组织机构主键
     */
    @NotEmpty(message = "产线不能为空")
    private List<Integer> productLineId;

    /**
     * 开始日期
     */
    @NotNull(message = "开始日期不能为空")
    private LocalDate start;

    /**
     * 结束日期
     */
    @NotNull(message = "结束日期不能为空")
    private LocalDate end;

    /**
     * 粒度
     */
    @NotNull(message = "粒度不能为空")
    private Integer granularity;
}
