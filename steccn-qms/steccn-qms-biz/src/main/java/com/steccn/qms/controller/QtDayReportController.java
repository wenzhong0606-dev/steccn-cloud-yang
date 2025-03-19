package com.steccn.qms.controller;

import com.alibaba.excel.util.MapUtils;
import com.alibaba.fastjson.JSON;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.common.constant.FeignConstant;
import com.steccn.common.dto.QtLineChartBarDTO;
import com.steccn.common.vo.R;
import com.steccn.dp.dto.DpBusiMonitorDTO;
import com.steccn.qms.constant.QtManagerConstant;
import com.steccn.qms.dto.QtDayReportOnlineDTO;
import com.steccn.qms.dto.QtLossReportDTO;
import com.steccn.qms.dto.QtReportFirstPatrolDTO;
import com.steccn.qms.service.QtDayReportService;
import com.steccn.qms.vo.*;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * @author yequan
 * @desc 质量日报表-控制器
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/qms/day/report")
@Slf4j
@ApiModel("质量日报表-控制器")
public class QtDayReportController {

    private final QtDayReportService qtDayReportService;

    @PostMapping("/online/man")
    @ApiOperation("质量日报表-在线检人工")
    public R<QtDayReportOnlineVO> onlineMan(@RequestBody @Validated QtDayReportOnlineDTO dto) {
        return R.success(qtDayReportService.online(dto.getDate(), dto.getProductLineIdList(), QtManagerConstant.CONF_QMS_COLLECT_DISABLE_CONTENT));
    }

    @PostMapping("/online")
    @ApiOperation("质量日报表-在线检")
    public R<QtDayReportOnlineVO> online(@RequestBody @Validated QtDayReportOnlineDTO dto) {
        return R.success(qtDayReportService.online(dto.getDate(), dto.getProductLineIdList(), QtManagerConstant.CONF_QMS_COLLECT_ENABLE_CONTENT));
    }

    @PostMapping("/online/man/download")
    @ApiOperation("质量日报表-在线检")
    public void onlineManDownload(@RequestBody @Validated QtDayReportOnlineDTO dto, HttpServletResponse response) throws IOException {
        try {
            qtDayReportService.onlineDownload(dto.getDate(), dto.getProductLineIdList(), QtManagerConstant.CONF_QMS_COLLECT_DISABLE_CONTENT, response);
        } catch (Exception e) {
            response.reset();
            response.setContentType("application/json");
            response.setCharacterEncoding(QtManagerConstant.QT_CHART_EXCEL_ENCODE);
            Map<String, String> map = MapUtils.newHashMap();
            map.put("status", "failure");
            map.put("message", "下载文件失败" + e.getMessage());
            response.getWriter().println(JSON.toJSONString(map));
        }
    }

    @PostMapping("/online/man/download/multi")
    @ApiOperation("质量日报表-在线检")
    public void onlineManMultiDownload(@RequestBody QtDayReportOnlineDTO dto, HttpServletResponse response) throws IOException {
        try {
            qtDayReportService.multiOnlineDownload(dto.getStart(),dto.getEnd(), dto.getProductLineIdList(), QtManagerConstant.CONF_QMS_COLLECT_DISABLE_CONTENT, response);
        } catch (Exception e) {
            response.reset();
            response.setContentType("application/json");
            response.setCharacterEncoding(QtManagerConstant.QT_CHART_EXCEL_ENCODE);
            Map<String, String> map = MapUtils.newHashMap();
            map.put("status", "failure");
            map.put("message", "下载文件失败" + e.getMessage());
            response.getWriter().println(JSON.toJSONString(map));
        }
    }

    @PostMapping("/first")
    @ApiOperation("质量日报表-首检2/巡检4")
    public R<QtDayReportFirstVO> first(@RequestBody @Validated QtReportFirstPatrolDTO dto) {
        return R.success(qtDayReportService.first(dto));
    }

    @PostMapping("/first/download")
    @ApiOperation("质量日报表-首检2/巡检4-下载")
    public void firstDownload(@RequestBody @Validated QtReportFirstPatrolDTO dto, HttpServletResponse response) throws IOException {
        try {
            qtDayReportService.firstDownload(dto,response);
        } catch (Exception e) {
            log.error("{}",e.getLocalizedMessage());
            response.reset();
            response.setContentType("application/json");
            response.setCharacterEncoding(QtManagerConstant.QT_CHART_EXCEL_ENCODE);
            Map<String, String> map = MapUtils.newHashMap();
            map.put("status", "failure");
            map.put("message", "下载文件失败" + e.getMessage());
            response.getWriter().println(JSON.toJSONString(map));
        }
    }

    @PostMapping("/first/download/multi")
    @ApiOperation("质量日报表-首检2/巡检4-下载")
    public void firstDownloadMulti(@RequestBody QtReportFirstPatrolDTO dto, HttpServletResponse response) throws IOException {
        try {
            qtDayReportService.multiFirstDownload(dto,response);
        } catch (Exception e) {
            log.error("{}",e.getLocalizedMessage());
            response.reset();
            response.setContentType("application/json");
            response.setCharacterEncoding(QtManagerConstant.QT_CHART_EXCEL_ENCODE);
            Map<String, String> map = MapUtils.newHashMap();
            map.put("status", "failure");
            map.put("message", "下载文件失败" + e.getMessage());
            response.getWriter().println(JSON.toJSONString(map));
        }
    }

    @PostMapping("/polling/defect/item")
    @ApiOperation("质量日报表-巡检-缺陷项")
    public R<QtDayPollingFirstVO> pollingDefectItem(@RequestBody @Validated QtReportFirstPatrolDTO dto) {
        return R.success(qtDayReportService.pollingDefectItem(dto));
    }

    @GetMapping("/sampling")
    @ApiOperation("配料抽检日报表")
    public R<QtDaySamplingVO> sampling(@RequestParam("date") LocalDate date) {
        return R.success(qtDayReportService.sampling(date));
    }

    @GetMapping("/sampling/water/rate")
    @ApiOperation("配料含水率")
    public R<QtLineChartBarDTO> samplingWaterRate(@RequestParam("start") LocalDate start, @RequestParam("end") LocalDate end) {
        return R.success(qtDayReportService.samplingWaterRate(start, end));
    }

    @GetMapping("/sampling/water/rate/si")
    @ApiOperation("配料含水率-")
    public R<QtLineChartBarDTO> samplingWaterRateSi(@RequestParam("start") LocalDate start, @RequestParam("end") LocalDate end) {
        return R.success(qtDayReportService.samplingWaterRateSi(start, end));
    }

    @GetMapping("/sampling/water/rate/si/monitor")
    @ApiOperation("配料含水率-")
    public R<Void> samplingWaterRateSiMonitor(@RequestParam("start") LocalDateTime start, @RequestParam("end") LocalDateTime end) {
        qtDayReportService.samplingWaterRateSiMonitor(start, end, "GS_WATER");
        return R.success();
    }

    @GetMapping("/sampling/water/rate/batch")
    @ApiOperation("配料含水率-")
    public R<QtLineChartBarDTO> samplingWaterRateBatch(@RequestParam("start") LocalDate start, @RequestParam("end") LocalDate end) {
        return R.success(qtDayReportService.samplingWaterRateBatch(start, end));
    }

    @GetMapping("/sampling/water/rate/batch/monitor")
    @ApiOperation("配料含水率-")
    public R<Void> samplingWaterRateBatchMonitor(@RequestParam("start") LocalDateTime start, @RequestParam("end") LocalDateTime end) {
        qtDayReportService.samplingWaterRateBatchMonitor(start, end, "PHL_WATER");
        return R.success();
    }

    @GetMapping("/sampling/download")
    @ApiOperation("配料抽检日报表")
    public void samplingDownload(@RequestParam("date") LocalDate date, HttpServletResponse response) throws IOException {
        try {
            qtDayReportService.samplingDownload(date, response);
        } catch (Exception e) {
            response.reset();
            response.setContentType("application/json");
            response.setCharacterEncoding(QtManagerConstant.QT_CHART_EXCEL_ENCODE);
            Map<String, String> map = MapUtils.newHashMap();
            map.put("status", "failure");
            map.put("message", "下载文件失败" + e.getMessage());
            response.getWriter().println(JSON.toJSONString(map));
        }
    }

    @PostMapping("/loss")
    @ApiOperation("损耗日报表")
    public R<QtDayReportLossVO> loss(@RequestBody @Validated QtLossReportDTO dto) {
        return R.success(qtDayReportService.loss(dto));
    }

    @PostMapping("/loss/download")
    @ApiOperation("损耗日报表-下载")
    public void lossDownload(@RequestBody @Validated QtLossReportDTO dto, HttpServletResponse response) throws IOException {
        try {
            qtDayReportService.lossDownload(dto, response);
        } catch (Exception e) {
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
