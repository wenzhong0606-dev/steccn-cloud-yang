package com.steccn.pms.dto;

import lombok.Data;

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
    private List<Integer> productLineList;

    /**
     * * 开始日期
     */
    private LocalDate start;

    /**
     * * 结束日期
     */
    private LocalDate end;

    /**
     * 粒度
     */
    private Integer granularity;
}

