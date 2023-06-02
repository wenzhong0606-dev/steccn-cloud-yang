package com.steccn.ems.service;

import com.steccn.ems.vo.EpDayReportCountVO;

import java.time.LocalDate;

/**
 * @author yequan
 * @desc 能耗日统计报表
 * @date 2023/6/1
 */
public interface EpDayReportService {
    /**
     * 能耗日统计报表
     * @param month 年月
     * @return 结果
     */
    EpDayReportCountVO count(LocalDate month);
}
