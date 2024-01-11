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

    /**
     * 锅炉报表
     * @param date
     * @return
     */
    SteccnTableDataDTO<Map<String,String>> boilerReport(LocalDate date);

    /**
     * 35kv总开关站运行值班日报表
     * @param date
     * @return
     */
    SteccnTableDataDTO<Map<String,String>> thirtyFiveKvReport(LocalDate date);

    /**
     * 脱硫脱硝报表
     * @param date
     * @return
     */
    SteccnTableDataDTO<Map<String,String>> desReport(LocalDate date);
}
