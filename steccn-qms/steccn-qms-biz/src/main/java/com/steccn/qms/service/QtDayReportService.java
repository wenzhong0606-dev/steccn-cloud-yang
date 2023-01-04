package com.steccn.qms.service;

import com.steccn.qms.vo.QtDayReportFirstVO;
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

    /**
     * 质量日报表 - 巡检
     * @param date  日期
     * @param productLineId  产线id
     * @return
     */
    QtDayReportOnlineVO polling(LocalDate date, Integer productLineId);

    /**
     * 质量日报表-首检
     * @param date 指定日期
     * @param productLineId 产线id
     * @return
     */
    QtDayReportFirstVO first(LocalDate date,Integer productLineId);
}
