package com.steccn.qms.service;

import com.steccn.qms.vo.QtDayReportOnlineVO;

import java.time.LocalDate;

/**
 * @author yequan
 * @desc 质量日报表-接口
 */
public interface QtDayReportService {

    /**
     * 质量日报表-在线检
     * @param date 指定日期
     * @param productLineId 产线id
     * @return vo
     */
    QtDayReportOnlineVO online(LocalDate date, Integer productLineId);
}
