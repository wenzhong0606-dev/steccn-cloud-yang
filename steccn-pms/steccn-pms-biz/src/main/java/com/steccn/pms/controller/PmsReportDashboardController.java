package com.steccn.pms.controller;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.util.MapUtils;
import com.alibaba.fastjson.JSON;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.common.constant.FeignConstant;
import com.steccn.common.dto.QtLineChartBarDTO;
import com.steccn.common.dto.TripleDTO;
import com.steccn.common.util.LocalDateUtil;
import com.steccn.common.vo.EpChartGraphVO;
import com.steccn.common.vo.R;
import com.steccn.dp.dto.DpPmsOperateDTO;
import com.steccn.oa.constant.OaConstant;
import com.steccn.platform.feign.RemoteSysOauthClientDetailFeignApi;
import com.steccn.pms.dto.JpAnalysisReportDTO;
import com.steccn.pms.vo.JpAnalysisReportVO;
import com.steccn.pms.dto.DashboardProducePageDTO;
import com.steccn.pms.dto.PmsOperateDriverDTO;
import com.steccn.pms.service.PmsReportDashboardService;
import com.steccn.pms.vo.DashboardProducePageVO;
import com.steccn.pms.vo.PmsDeepProcessingNumVO;
import com.steccn.pms.vo.PmsPullForceMeltingNumVO;
import com.steccn.qms.constant.QtManagerConstant;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * @author yequan
 * @desc 生产看板
 * @date 2023/8/28
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/pms/report/dashboard")
@Slf4j
public class PmsReportDashboardController {

    private final PmsReportDashboardService pmsReportDashboardService;

    private final RemoteSysOauthClientDetailFeignApi remoteSysOauthClientDetailFeignApi;

    @GetMapping("/pull/force/today")
    @ApiOperation("今日拉引量（单位/T）")
    public R<BigDecimal> pullForceToday(@RequestParam("workShopId") Integer workShopId, @RequestParam("date") LocalDate date) {
        return R.success(pmsReportDashboardService.pullForceToday(workShopId, date));
    }

    @GetMapping("/pull/force/today/dataTwin")
    @ApiOperation("今日拉引量（单位/T）")
    public R<BigDecimal> pullForceTodayDataTwin(HttpServletRequest request) {
        validRequestHeader(request);
        return R.success(pmsReportDashboardService.pullForceTodayDataTwin());
    }

    @GetMapping("/pull/force/date/range")
    @ApiOperation("日期范围拉引量（单位/T）")
    public R<BigDecimal> pullForceDateRange(@RequestParam("partId") Integer partId, @RequestParam("start") LocalDate start, @RequestParam("end") LocalDate end) {
        return R.success(pmsReportDashboardService.pullForceDateRange(partId, start, end));
    }

    @GetMapping("/pull/melting/today")
    @ApiOperation("熔化率（单位/T）")
    public R<BigDecimal> meltingRate(@RequestParam("workShopId") Integer workShopId, @RequestParam("date") LocalDate date) {
        return R.success(pmsReportDashboardService.meltingRate(workShopId, date));
    }

    @GetMapping("/pull/melting/today/monitor")
    @ApiOperation("熔化率（单位/T）-监控")
    public R<Void> meltingRateMonitor(@RequestParam("date") LocalDate date) {
        pmsReportDashboardService.meltingRateMonitor(date, "JP_RH_RATE");
        return R.success();
    }

    @GetMapping("/produce/num/today")
    @ApiOperation("今日产量（单位/T/片/㎡）")
    public R<List<BigDecimal>> produceNumToday(@RequestParam("workShopId") Integer workShopId, @RequestParam("date") LocalDate date) {
        return R.success(pmsReportDashboardService.produceNumToday(workShopId, date));
    }

    @GetMapping("/produce/num/date/range")
    @ApiOperation("日期范围产量（单位/T/片/㎡）")
    public R<List<BigDecimal>> produceNumDateRange(@RequestParam("partId") Integer partId, @RequestParam("start") LocalDate start, @RequestParam("end") LocalDate end) {
        return R.success(pmsReportDashboardService.produceNumDateRange(partId, start, end));
    }

    @GetMapping("/produce/num/rate")
    @ApiOperation("产量合格率")
    public R<BigDecimal> produceNumRate(@RequestParam("workShopId") Integer workShopId, @RequestParam("date") LocalDate date) {
        return R.success(pmsReportDashboardService.produceNumRate(workShopId, date));
    }

    @GetMapping("/produce/num/rate/date/range")
    @ApiOperation("日期范围产量合格率")
    public R<BigDecimal> produceNumRateDateRange(@RequestParam("partId") Integer partId, @RequestParam("start") LocalDate start, @RequestParam("end") LocalDate end) {
        return R.success(pmsReportDashboardService.produceNumRateDateRange(partId, start, end));
    }

    @GetMapping("/in/storage/today")
    @ApiOperation("今日入库数（单位/T/片/㎡）")
    public R<List<BigDecimal>> inStorageToday(@RequestParam("workShopId") Integer workShopId, @RequestParam("date") LocalDate date) {
        return R.success(pmsReportDashboardService.inStorageToday(workShopId, date));
    }

    @GetMapping("/in/storage/date/range")
    @ApiOperation("日期范围入库数（单位/T/片/㎡）")
    public R<List<BigDecimal>> inStorageDateRange(@RequestParam("partId") Integer partId, @RequestParam("start") LocalDate start, @RequestParam("end") LocalDate end) {
        return R.success(pmsReportDashboardService.inStorageDateRange(partId, start, end));
    }

    @GetMapping("/in/storage/rate")
    @ApiOperation("入库合格率")
    public R<BigDecimal> inStorageRate(@RequestParam("workShopId") Integer workShopId, @RequestParam("date") LocalDate date) {
        return R.success(pmsReportDashboardService.inStorageRate(workShopId, date));
    }

    @GetMapping("/in/storage/rate/date/range")
    @ApiOperation("日期范围入库合格率")
    public R<BigDecimal> inStorageRateDateRange(@RequestParam("partId") Integer partId, @RequestParam("start") LocalDate start, @RequestParam("end") LocalDate end) {
        return R.success(pmsReportDashboardService.inStorageRateDateRange(partId, start, end));
    }

    @PostMapping("/produce/page")
    @ApiOperation("产线加工信息")
    public R<DashboardProducePageVO> producePage(@RequestBody DashboardProducePageDTO dto) {
        return R.success(pmsReportDashboardService.producePage(dto));
    }

    @GetMapping("/line/pull/force")
    @ApiOperation("产线拉引量")
    public R<QtLineChartBarDTO> linePullForce(@RequestParam("workShopId") Integer workShopId, @RequestParam("date") LocalDate date) {
        return R.success(pmsReportDashboardService.linePullForce(workShopId, date));
    }

    @GetMapping("/line/complete")
    @ApiOperation("产线完工信息")
    public R<QtLineChartBarDTO> lineComplete(@RequestParam("workShopId") Integer workShopId, @RequestParam("date") LocalDate date) {
        return R.success(pmsReportDashboardService.lineComplete(workShopId, date));
    }

    @GetMapping("/line/complete/monitor")
    @ApiOperation("产线完工信息-监控")
    public R<Void> lineCompleteMonitor(@RequestParam("date") LocalDate date) {
        pmsReportDashboardService.lineCompleteMonitor(date, "JP_PRODUCT_PASS_RATE");
        return R.success();
    }

    @GetMapping("/line/in/storage")
    @ApiOperation("产线入库信息")
    public R<QtLineChartBarDTO> lineInStorage(@RequestParam("workShopId") Integer workShopId, @RequestParam("date") LocalDate date) {
        return R.success(pmsReportDashboardService.lineInStorage(workShopId, date));
    }

    @GetMapping("/line/in/storage/monitor")
    @ApiOperation("产线入库信息-监控")
    public R<Void> lineInStorageMonitor(@RequestParam("date") LocalDate date) {
        pmsReportDashboardService.lineInStorageMonitor(date, "JP_IN_STORAGE_RATE");
        return R.success();
    }

    @PostMapping("/line/pull/force/melting")
    @ApiOperation("拉引量/熔化率")
    public R<QtLineChartBarDTO> pullForceMelting(@RequestBody @Valid DpPmsOperateDTO dto) {
        return R.success(pmsReportDashboardService.pullForceMelting(dto));
    }

    @GetMapping("/line/pull/force/melting/num")
    @ApiOperation("拉引量/熔化率/数值")
    public R<PmsPullForceMeltingNumVO> pullForceMeltingNum(@RequestParam("start") LocalDate start,@RequestParam("end")LocalDate end) {
        return R.success(pmsReportDashboardService.pullForceMeltingNum(start,end));
    }

    @PostMapping("/deep/processing/num")
    @ApiOperation("深加工制造部-领用总数/耗用总数/使用总数")
    public R<PmsDeepProcessingNumVO> deepProcessingNum(@Valid @RequestBody PmsOperateDriverDTO dto) {
        return R.success(pmsReportDashboardService.deepProcessingNum(dto));
    }

    @PostMapping("/deep/processing/histogram")
    @ApiOperation("深加工制造部-基片数柱状图")
    public R<EpChartGraphVO<List<String>, TripleDTO<BigDecimal,BigDecimal,BigDecimal>>> deepProcessingHistogram(@Valid @RequestBody PmsOperateDriverDTO dto) {
        return R.success(pmsReportDashboardService.deepProcessingHistogram(dto));
    }

    @PostMapping("/analysis/report")
    @ApiOperation("基片分析报表")
    public R<List<JpAnalysisReportVO>> analysisReport(@Valid @RequestBody JpAnalysisReportDTO dto) {
        Assert.isTrue(LocalDateUtil.le(dto.getStart(), dto.getEnd()), "起始日期不得大于终止日期");
        return R.success(pmsReportDashboardService.analysisReport(dto.getWorkshopId(), dto.getStart(), dto.getEnd()));
    }

    @PostMapping("/analysis/report/download")
    @ApiOperation("基片分析报表-导出")
    public void analysisReportDownload(@Valid @RequestBody JpAnalysisReportDTO dto, HttpServletResponse response) throws IOException {
        try {
            Assert.isTrue(LocalDateUtil.le(dto.getStart(), dto.getEnd()), "起始日期不得大于终止日期");
            pmsReportDashboardService.analysisReportDownload(dto.getWorkshopId(), dto.getStart(), dto.getEnd(), response);
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

    /**
     * 校验请求头信息
     *
     * @param request 请求
     */
    private void validRequestHeader(HttpServletRequest request) {
        List<String> pwdSet = FeignOptional.of(() -> remoteSysOauthClientDetailFeignApi.clientEncodePassword(), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        String clientId = request.getHeader(OaConstant.CLIENT_ID);
        Assert.isTrue(StrUtil.isNotBlank(clientId), "请求头中必须携带client-id");
        Assert.isTrue(pwdSet.contains(clientId), "非法访问，请携带正确的client-id");
    }
}
