package com.steccn.pms.service;

import com.steccn.common.dto.SteccnTableDataDTO;

import java.time.LocalDate;
import java.util.Map;

/**
 * 数采报表
 *
 * @author AndrewLuo
 * @date 2024/01/10
 */
public interface PmsDataCollectReportService {
    /**
     * 余热汽轮机报表
     * @param date
     * @return
     */
    SteccnTableDataDTO<Map<String,String>> turbineReport(LocalDate date);
}
