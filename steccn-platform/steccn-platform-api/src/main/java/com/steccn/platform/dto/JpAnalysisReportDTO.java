package com.steccn.platform.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * @author yequan
 * @desc 基片分析报表-请求dto
 */
@Data
public class JpAnalysisReportDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 车间主键
     */
    @NotNull(message = "组织机构不能为空")
    private Integer workshopId;

    /**
     * 区间开始日期
     */
    @NotNull(message = "开始日期不能为空")
    private LocalDate start;

    /**
     * 区间结束日期
     */
    @NotNull(message = "结束日期不能为空")
    private LocalDate end;
}


