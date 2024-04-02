package com.steccn.pms.controller;

import com.alibaba.excel.util.MapUtils;
import com.alibaba.fastjson.JSON;
import com.steccn.common.dto.SteccnTableDataDTO;
import com.steccn.common.vo.R;
import com.steccn.pms.dto.PmsDayReportDTO;
import com.steccn.pms.service.PmsDataCollectReportService;
import com.steccn.pms.vo.SubstrateDayReportVO;
import com.steccn.qms.constant.QtManagerConstant;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Map;

/**
 * 数采报表控制器
 *
 * @author AndrewLuo
 * @date 2024/01/10
 */
@RestController
@RequestMapping("/pms/dataCollect/report")
@RequiredArgsConstructor
@Slf4j
public class PmsDataCollectReportController {
    private final PmsDataCollectReportService pmsDataCollectReportService;

    @GetMapping("/turbine")
    @ApiOperation("余热汽轮机报表")
    public R<SteccnTableDataDTO> turbine(@RequestParam("date") LocalDate date) {
        return R.success(pmsDataCollectReportService.turbineReport(date));
    }

    @GetMapping("/turbine/download")
    @ApiOperation("余热汽轮机报表")
    public void turbineDownload(@RequestParam("date") LocalDate date, HttpServletResponse response) throws IOException {
        try {
            pmsDataCollectReportService.turbineReportDownload(date, response);
        } catch (Exception e) {
            log.error("{}", e.getLocalizedMessage());
            response.reset();
            response.setContentType("application/json");
            response.setCharacterEncoding(QtManagerConstant.QT_CHART_EXCEL_ENCODE);
            Map<String, String> map = MapUtils.newHashMap();
            map.put("status", "failure");
            map.put("message", "下载文件失败" + e.getMessage());
            response.getWriter().println(JSON.toJSONString(map));
        }
    }

    @GetMapping("/boiler")
    @ApiOperation("余热锅炉报表")
    public R<SteccnTableDataDTO> boiler(@RequestParam("date") LocalDate date) {
        return R.success(pmsDataCollectReportService.boilerReport(date));
    }

    @GetMapping("/boiler/download")
    @ApiOperation("余热锅炉报表")
    public void boilerDownload(@RequestParam("date") LocalDate date, HttpServletResponse response) throws IOException {
        try {
            pmsDataCollectReportService.boilerReportDownload(date, response);
        } catch (Exception e) {
            log.error("{}", e.getLocalizedMessage());
            response.reset();
            response.setContentType("application/json");
            response.setCharacterEncoding(QtManagerConstant.QT_CHART_EXCEL_ENCODE);
            Map<String, String> map = MapUtils.newHashMap();
            map.put("status", "failure");
            map.put("message", "下载文件失败" + e.getMessage());
            response.getWriter().println(JSON.toJSONString(map));
        }
    }

    @GetMapping("/thirtyFiveKv")
    @ApiOperation("35kv总开关报表")
    public R<SteccnTableDataDTO> thirtyFiveKv(@RequestParam("date") LocalDate date) {
        return R.success(pmsDataCollectReportService.thirtyFiveKvReport(date));
    }

    @GetMapping("/thirtyFiveKv/download")
    @ApiOperation("35kv总开关报表-导出")
    public void thirtyFiveKvDownload(@RequestParam("date") LocalDate date, HttpServletResponse response) throws IOException {
        try {
            pmsDataCollectReportService.thirtyFiveKvReportDownload(date, response);
        } catch (Exception e) {
            log.error("{}", e.getLocalizedMessage());
            response.reset();
            response.setContentType("application/json");
            response.setCharacterEncoding(QtManagerConstant.QT_CHART_EXCEL_ENCODE);
            Map<String, String> map = MapUtils.newHashMap();
            map.put("status", "failure");
            map.put("message", "下载文件失败" + e.getMessage());
            response.getWriter().println(JSON.toJSONString(map));
        }
    }


    @GetMapping("/des")
    @ApiOperation("脱硫脱硝报表")
    public R<SteccnTableDataDTO> des(@RequestParam("date") LocalDate date) {
        return R.success(pmsDataCollectReportService.desReport(date));
    }

    @GetMapping("/melting1")
    @ApiOperation("1200t/d熔化日志1")
    public R<SteccnTableDataDTO> melting1(@RequestParam("date") LocalDate date) {
        return R.success(pmsDataCollectReportService.melting1(date));
    }

    @GetMapping("/melting2")
    @ApiOperation("1200t/d熔化日志2")
    public R<SteccnTableDataDTO> melting2(@RequestParam("date") LocalDate date) {
        return R.success(pmsDataCollectReportService.melting2(date));
    }

    @GetMapping("/annealing")
    @ApiOperation("退火窑日志")
    public R<SteccnTableDataDTO> annealing(@RequestParam("date") LocalDate date,@RequestParam("type") Integer type) {
        return R.success(pmsDataCollectReportService.annealing(date,type));
    }

    @GetMapping("/molding1")
    @ApiOperation("1200t/d成型作业日志(1-4)")
    public R<SteccnTableDataDTO> molding1(@RequestParam("date") LocalDate date) {
        return R.success(pmsDataCollectReportService.molding1(date));
    }

    @GetMapping("/molding2")
    @ApiOperation("1200t/d成型作业日志(5-8)")
    public R<SteccnTableDataDTO> molding2(@RequestParam("date") LocalDate date) {
        return R.success(pmsDataCollectReportService.molding2(date));
    }

    @GetMapping("/acStation")
    @ApiOperation("空压站")
    public R<SteccnTableDataDTO> acStation(@RequestParam("date") LocalDate date) {
        return R.success(pmsDataCollectReportService.acStation(date));
    }

    @GetMapping("/acStation/download")
    @ApiOperation("空压站")
    public void acStationDownload(@RequestParam("date") LocalDate date, HttpServletResponse response) throws IOException {
        try {
            pmsDataCollectReportService.acStationDownload(date, response);
        } catch (Exception e) {
            log.error("{}", e.getLocalizedMessage());
            response.reset();
            response.setContentType("application/json");
            response.setCharacterEncoding(QtManagerConstant.QT_CHART_EXCEL_ENCODE);
            Map<String, String> map = MapUtils.newHashMap();
            map.put("status", "failure");
            map.put("message", "下载文件失败" + e.getMessage());
            response.getWriter().println(JSON.toJSONString(map));
        }
    }

    @GetMapping("/waterPump")
    @ApiOperation("水泵房")
    public R<SteccnTableDataDTO> waterPump(@RequestParam("date") LocalDate date) {
        return R.success(pmsDataCollectReportService.waterPump(date));
    }

    @GetMapping("/waterPump/download")
    @ApiOperation("水泵房-导出")
    public void waterPumpDownload(@RequestParam("date") LocalDate date, HttpServletResponse response) throws IOException {
        try {
            pmsDataCollectReportService.waterPumpDownload(date, response);
        } catch (Exception e) {
            log.error("{}", e.getLocalizedMessage());
            response.reset();
            response.setContentType("application/json");
            response.setCharacterEncoding(QtManagerConstant.QT_CHART_EXCEL_ENCODE);
            Map<String, String> map = MapUtils.newHashMap();
            map.put("status", "failure");
            map.put("message", "下载文件失败" + e.getMessage());
            response.getWriter().println(JSON.toJSONString(map));
        }
    }

}
