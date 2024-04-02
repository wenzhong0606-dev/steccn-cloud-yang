package com.steccn.pms.service;

import com.steccn.common.dto.SteccnTableDataDTO;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
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
     * 余热汽轮机报表-导出
     *
     * @param date
     * @param response
     */
    void turbineReportDownload(LocalDate date, HttpServletResponse response) throws IOException;

    /**
     * 锅炉报表
     * @param date
     * @return
     */
    SteccnTableDataDTO<Map<String,String>> boilerReport(LocalDate date);

    /**
     * 锅炉报表-导出
     * @param date
     * @param response
     */
    void boilerReportDownload(LocalDate date, HttpServletResponse response) throws IOException;

    /**
     * 35kv总开关站运行值班日报表
     * @param date
     * @return
     */
    SteccnTableDataDTO<Map<String,String>> thirtyFiveKvReport(LocalDate date);

    /**
     * 35kv总开关站运行值班日报表 - 导出
     * @param date 日期
     * @param response 响应
     */
    void thirtyFiveKvReportDownload(LocalDate date, HttpServletResponse response) throws IOException;

    /**
     * 脱硫脱硝报表
     * @param date
     * @return
     */
    SteccnTableDataDTO<Map<String,String>> desReport(LocalDate date);

    /**
     * 脱硫脱硝报表
     * @param date
     * @param response
     */
    void desReportDownload(LocalDate date, HttpServletResponse response) throws IOException;

    /**
     * 熔化日志1报表
     * @param date
     * @return
     */
    SteccnTableDataDTO<Map<String,String>> melting1(LocalDate date);

    /**
     * 熔化日志2报表
     * @param date
     * @return
     */
    SteccnTableDataDTO<Map<String,String>> melting2(LocalDate date);

    /**
     * 退火窑日志
     * @param date
     * @return
     */
    SteccnTableDataDTO<Map<String,String>> annealing(LocalDate date,Integer type);

    /**
     * 成型作业日志(1-4)线
     * @param date
     * @return
     */
    SteccnTableDataDTO<Map<String,String>> molding1(LocalDate date);

    /**
     * 成型作业日志(5-8)线
     * @param date
     * @return
     */
    SteccnTableDataDTO<Map<String,String>> molding2(LocalDate date);

    /**
     * 空压站
     * @param date
     * @return
     */
    SteccnTableDataDTO<Map<String,String>> acStation(LocalDate date);

    /**
     * 空压站-导出
     *
     * @param date 日期
     * @param response 响应
     */
    void acStationDownload(LocalDate date, HttpServletResponse response) throws IOException;

    /**
     * 水泵房
     * @param date
     * @return
     */
    SteccnTableDataDTO<Map<String,String>> waterPump(LocalDate date);

    /**
     * 水泵房 - 导出
     * @param date
     * @param response
     */
    void waterPumpDownload(LocalDate date, HttpServletResponse response) throws IOException;

}
