package com.steccn.ems.service;

import com.steccn.common.constant.ResultCode;
import com.steccn.ems.dto.CeCarbonReportDTO;
import com.steccn.ems.vo.CeCarbonSumReportVO;

import java.time.LocalDate;

/**
 * @author yequan
 * @desc 碳排放报表接口
 */
public interface CeCarbonReportService {

    /**
     * 碳排放总量报表
     *
     * @param start       开始日期
     * @param end         结束日期
     * @param granularity 粒度
     * @return
     */
    CeCarbonSumReportVO carbonSumReport(LocalDate start, LocalDate end, Integer granularity);
}
