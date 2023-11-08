package com.steccn.pms.dto;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

/**
 * @author yequan
 * @desc 日报表公共请求dto
 * @date 2023/11/2
 */
@Data
public class DayReportCommonDTO implements Serializable {

    private final static long serialVersionUID = 1L;

    /**
     * * 产线集合
     */
    @NotEmpty(message = "产线不能为空")
    private List<Integer> productLineList;

    /**
     * * 开始日期
     */
    @NotNull(message = "开始日期不能为空")
    private LocalDate start;

    /**
     * * 结束日期
     */
    @NotNull(message = "结束日期不能为空")
    private LocalDate end;

    /**
     * 粒度
     */
    private Integer granularity;
}

