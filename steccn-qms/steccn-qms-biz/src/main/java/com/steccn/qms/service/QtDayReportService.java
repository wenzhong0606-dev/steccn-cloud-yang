package com.steccn.qms.service;

import com.steccn.common.constant.ResultCode;
import com.steccn.qms.dto.QtLossReportDTO;
import com.steccn.qms.dto.QtReportFirstPatrolDTO;
import com.steccn.qms.vo.*;

import java.time.LocalDate;
import java.util.List;

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
    QtDayReportOnlineVO online(LocalDate date, List<Integer> productLineId);


    /**
     * 质量日报表-首检2/巡检4
     * @param dto
     * @return
     */
    QtDayReportFirstVO first(QtReportFirstPatrolDTO dto);


    /**
     * 质量日报表-巡检-缺陷项
     * @param dto
     * @return
     */
    QtDayPollingFirstVO pollingDefectItem(QtReportFirstPatrolDTO dto);

    /**
     * 配料抽检日报表
     * @param date 日期
     * @return 报表
     */
    QtDaySamplingVO sampling(LocalDate date);

    /**
     * 基片损耗日报表
     * @param dto
     * @return
     */
    QtDayReportLossVO loss(QtLossReportDTO dto);
}
