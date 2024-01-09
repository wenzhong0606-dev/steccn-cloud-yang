package com.steccn.pms.controller;

import com.alibaba.excel.util.MapUtils;
import com.alibaba.fastjson.JSON;
import com.steccn.common.dto.QtLineChartBarDTO;
import com.steccn.common.dto.TupleDTO;
import com.steccn.common.util.BigDecimalUtil;
import com.steccn.common.util.UnitLengthUtil;
import com.steccn.common.vo.R;
import com.steccn.pms.constant.PmManagerConstant;
import com.steccn.pms.dto.QtLineChartBarCountDTO;
import com.steccn.pms.service.PmsReportDashboardCtService;
import com.steccn.pms.util.PmsUnitUtil;
import com.steccn.pms.vo.JpCountReportDetailVO;
import com.steccn.qms.constant.QtManagerConstant;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * @author yequan
 * @desc 生产看板-统计看板【日期范围】
 * @date 2023/8/28
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/pms/report/dashboard/ct")
@Slf4j
public class PmsReportDashboardCtController {

    private final PmsReportDashboardCtService pmsReportDashboardCtService;

    @GetMapping("/produce/class")
    @ApiOperation("累计产量总数")
    public R<QtLineChartBarCountDTO> produceClass(@RequestParam("workShopId") Integer workShopId, @RequestParam("start") LocalDate start, @RequestParam("end") LocalDate end, @RequestParam("unitType") Integer unitType, @RequestParam(value = "granularity",defaultValue = "7") Integer granularity) {
        TupleDTO<String, Integer> unit = PmsUnitUtil.unit(unitType);
        return R.success(pmsReportDashboardCtService.pmDate(workShopId, start, end, granularity, unit.getField2(), unit.getField1()));
    }

    @GetMapping("/yield/rate/date")
    @ApiOperation("总成品率")
    public R<QtLineChartBarCountDTO> yieldRateDate(@RequestParam("workShopId") Integer workShopId, @RequestParam("start") LocalDate start, @RequestParam("end") LocalDate end, @RequestParam("granularity") Integer granularity) {
        return R.success(pmsReportDashboardCtService.yieldRateDate(workShopId, start, end,granularity));
    }

    @GetMapping("/pm/date")
    @ApiOperation("累计产量总数")
    public R<QtLineChartBarCountDTO> pmDate(@RequestParam("workShopId") Integer workShopId, @RequestParam("start") LocalDate start, @RequestParam("end") LocalDate end, @RequestParam("granularity") Integer granularity) {
        return R.success(pmsReportDashboardCtService.pmDate(workShopId, start, end, granularity, UnitLengthUtil.UNIT_TON_LENGTH, PmManagerConstant.MEASURE_UNIT_TON));
    }

    @GetMapping("/in/storage/date")
    @ApiOperation("累计入库量总数")
    public R<QtLineChartBarCountDTO> inStorageDate(@RequestParam("workShopId") Integer workShopId, @RequestParam("start") LocalDate start, @RequestParam("end") LocalDate end, @RequestParam("granularity") Integer granularity) {
        return R.success(pmsReportDashboardCtService.inStorageDate(workShopId, start, end, granularity, UnitLengthUtil.UNIT_TON_LENGTH, PmManagerConstant.MEASURE_UNIT_TON));
    }

    @GetMapping("/pull/force/date")
    @ApiOperation("累计拉引量总数")
    public R<QtLineChartBarCountDTO> pullForceDate(@RequestParam("workShopId") Integer workShopId, @RequestParam("start") LocalDate start, @RequestParam("end") LocalDate end, @RequestParam("granularity") Integer granularity) {
        return R.success(pmsReportDashboardCtService.pullForceDate(workShopId, start, end, granularity, UnitLengthUtil.UNIT_TON_LENGTH, PmManagerConstant.MEASURE_UNIT_TON));
    }

    @GetMapping("/yield/rate/date/pm")
    @ApiOperation("各产线成品率-完工")
    public R<QtLineChartBarCountDTO> yieldRateDatePm(@RequestParam("workShopId") Integer workShopId, @RequestParam("start") LocalDate start, @RequestParam("end") LocalDate end, @RequestParam("granularity") Integer granularity) {
        return R.success(pmsReportDashboardCtService.yieldRateDatePm(workShopId, start, end, granularity));
    }

    @GetMapping("/yield/rate/date/in/storage")
    @ApiOperation("各产线成品率-入库")
    public R<QtLineChartBarCountDTO> yieldRateDateInStorage(@RequestParam("workShopId") Integer workShopId, @RequestParam("start") LocalDate start, @RequestParam("end") LocalDate end, @RequestParam("granularity") Integer granularity) {
        return R.success(pmsReportDashboardCtService.yieldRateDateInStorage(workShopId, start, end, granularity));
    }

    @GetMapping("/line/date/pm")
    @ApiOperation("各产线总产量")
    public R<QtLineChartBarCountDTO> lineDatePm(@RequestParam("workShopId") Integer workShopId, @RequestParam("start") LocalDate start, @RequestParam("end") LocalDate end, @RequestParam("granularity") Integer granularity) {
        return R.success(pmsReportDashboardCtService.lineDatePm(workShopId, start, end, granularity));
    }

    @GetMapping("/line/date/in/storage")
    @ApiOperation("各产线入库总数")
    public R<QtLineChartBarCountDTO> lineDateInStorage(@RequestParam("workShopId") Integer workShopId, @RequestParam("start") LocalDate start, @RequestParam("end") LocalDate end, @RequestParam("granularity") Integer granularity) {
        return R.success(pmsReportDashboardCtService.lineDateInStorage(workShopId, start, end, granularity));
    }

    @GetMapping("/line/date/pf")
    @ApiOperation("各产线拉引量总数")
    public R<QtLineChartBarCountDTO> lineDatePf(@RequestParam("workShopId") Integer workShopId, @RequestParam("start") LocalDate start, @RequestParam("end") LocalDate end, @RequestParam("granularity") Integer granularity) {
        return R.success(pmsReportDashboardCtService.lineDatePf(workShopId, start, end, granularity));
    }

    @GetMapping("/count/report/detail")
    @ApiOperation("基片统计报表")
    public R<JpCountReportDetailVO> countReportDetail(@RequestParam("workShopId") Integer workShopId, @RequestParam("start") LocalDate start, @RequestParam("end") LocalDate end, @RequestParam("granularity") Integer granularity) throws ExecutionException, InterruptedException {
        return R.success(pmsReportDashboardCtService.countReportDetail(workShopId, start, end, granularity));
    }

    @GetMapping("/count/report/detail/download")
    @ApiOperation("基片统计报表-下载")
    public void countReportDetailDownload(@RequestParam("workShopId") Integer workShopId, @RequestParam("start") LocalDate start, @RequestParam("end") LocalDate end, @RequestParam("granularity") Integer granularity, HttpServletResponse response) throws Exception {
        try {
            pmsReportDashboardCtService.countReportDetailDownload(workShopId, start, end, granularity,response);
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
