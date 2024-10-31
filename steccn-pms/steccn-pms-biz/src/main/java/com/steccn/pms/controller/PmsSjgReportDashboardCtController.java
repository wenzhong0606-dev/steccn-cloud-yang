package com.steccn.pms.controller;

import cn.hutool.core.lang.Assert;
import com.alibaba.excel.util.MapUtils;
import com.alibaba.fastjson.JSON;
import com.steccn.common.dto.QtLineChartBarDTO;
import com.steccn.common.dto.TupleDTO;
import com.steccn.common.vo.R;
import com.steccn.pms.service.PmsSjgReportDashboardCtService;
import com.steccn.pms.util.PmsUnitUtil;
import com.steccn.pms.vo.SjgCountReportStopVO;
import com.steccn.pms.vo.SjgCountReportVO;
import com.steccn.pms.vo.SjgDashboardLineProduceVO;
import com.steccn.pms.vo.SjgMonthCountReportVO;
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
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * @author yequan
 * @desc 深加工看板
 * @date 2023/9/5
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/pms/sjg/report/dashboard")
@Slf4j
public class PmsSjgReportDashboardCtController {

    private final PmsSjgReportDashboardCtService pmsSjgReportDashboardService;

    @GetMapping("/line/produce")
    @ApiOperation("产线产量")
    public R<QtLineChartBarDTO> lineProduce(@RequestParam("workShopId") Integer workShopId, @RequestParam("start") LocalDate start, @RequestParam("end") LocalDate end, @RequestParam("unitType") Integer unitType) {
        TupleDTO<String, Integer> unit = PmsUnitUtil.unit(unitType);
        return R.success(pmsSjgReportDashboardService.lineProduce(workShopId, start, end, unit.getField1(), unit.getField2()));
    }

    @GetMapping("/line/produce/detail")
    @ApiOperation("产线产量表格")
    public R<SjgDashboardLineProduceVO> lineProduceDetail(@RequestParam("workShopId") Integer workShopId, @RequestParam("start") LocalDate start, @RequestParam("end") LocalDate end, @RequestParam("unitType") Integer unitType) {
        TupleDTO<String, Integer> unit = PmsUnitUtil.unit(unitType);
        return R.success(pmsSjgReportDashboardService.lineProduceDetail(workShopId, start, end, unit.getField1(), unit.getField2()));
    }

    @GetMapping("/line/produce/classgroup")
    @ApiOperation("产线班组产量")
    public R<QtLineChartBarDTO> lineProduceClassGroup(@RequestParam("workShopId") Integer workShopId, @RequestParam("start") LocalDate start, @RequestParam("end") LocalDate end, @RequestParam("unitType") Integer unitType) {
        TupleDTO<String, Integer> unit = PmsUnitUtil.unit(unitType);
        return R.success(pmsSjgReportDashboardService.lineProduceClassGroup(workShopId, start, end, unit.getField1(), unit.getField2()));
    }


    @GetMapping("/line/produce/classgroup/detail")
    @ApiOperation("产线班组产量表格")
    public R<SjgDashboardLineProduceVO> lineProduceClassGroupDetail(@RequestParam("workShopId") Integer workShopId, @RequestParam("start") LocalDate start, @RequestParam("end") LocalDate end, @RequestParam("unitType") Integer unitType) {
        TupleDTO<String, Integer> unit = PmsUnitUtil.unit(unitType);
        return R.success(pmsSjgReportDashboardService.lineProduceClassGroupDetail(workShopId, start, end, unit.getField1(), unit.getField2()));
    }

    @GetMapping("/line/produce/passRate")
    @ApiOperation("产线合格率")
    public R<QtLineChartBarDTO> linePassRate(@RequestParam("workShopId") Integer workShopId, @RequestParam("start") LocalDate start, @RequestParam("end") LocalDate end) {
        return R.success(pmsSjgReportDashboardService.linePassRate(workShopId, start, end));
    }

    @GetMapping("/line/produce/passRate/monitor")
    @ApiOperation("产线合格率-监控")
    public R<Void> linePassRateMonitor(@RequestParam("start") LocalDateTime start, @RequestParam("end") LocalDateTime end, @RequestParam("granularity") Integer granularity) {
        //获取粒度
        QtManagerConstant.QtGranularityEnum granularityEnum = QtManagerConstant.QtGranularityEnum.getByCode(granularity);
        Assert.notNull(granularityEnum, "粒度不存在");
        pmsSjgReportDashboardService.linePassRateMonitor(start, end, "DEEP_REPORT_WORK_FINISH_NUM", granularityEnum);
        return R.success();
    }

    @GetMapping("/line/produce/passRate/detail")
    @ApiOperation("产线合格率详情")
    public R<SjgDashboardLineProduceVO> linePassRateDetail(@RequestParam("workShopId") Integer workShopId, @RequestParam("start") LocalDate start, @RequestParam("end") LocalDate end) {
        return R.success(pmsSjgReportDashboardService.linePassRateDetail(workShopId, start, end));
    }

    @GetMapping("/line/produce/classPassrate")
    @ApiOperation("产线班组合格率")
    public R<QtLineChartBarDTO> lineGroupPassRate(@RequestParam("workShopId") Integer workShopId, @RequestParam("start") LocalDate start, @RequestParam("end") LocalDate end) {
        return R.success(pmsSjgReportDashboardService.lineGroupPassRate(workShopId, start, end));
    }

    @GetMapping("/line/produce/classPassrate/detail")
    @ApiOperation("产线班组合格率详情")
    public R<SjgDashboardLineProduceVO> lineGroupPassRateDetail(@RequestParam("workShopId") Integer workShopId, @RequestParam("start") LocalDate start, @RequestParam("end") LocalDate end) {
        return R.success(pmsSjgReportDashboardService.lineGroupPassRateDetail(workShopId, start, end));
    }

    @GetMapping("/count/report")
    @ApiOperation("深加工统计报表")
    public R<SjgCountReportVO> countReport(@RequestParam("workShopId") Integer workShopId, @RequestParam("start") LocalDate start, @RequestParam("end") LocalDate end) {
        return R.success(pmsSjgReportDashboardService.countReport(workShopId,start,end));
    }

    @GetMapping("/count/report/stop")
    @ApiOperation("深加工统计报表-停机信息")
    public R<List<SjgCountReportStopVO>> countReportStop(@RequestParam("lineId") Integer lineId, @RequestParam("start") LocalDate start, @RequestParam("end") LocalDate end) {
        return R.success(pmsSjgReportDashboardService.countReportStop(lineId, start, end));
    }

    @GetMapping("/month/count/report")
    @ApiOperation("深加工月度统计报表")
    public R<SjgMonthCountReportVO> monthReport(@RequestParam("type") Integer type, @RequestParam("officeId") Integer officeId, @RequestParam("start") LocalDate start, @RequestParam("end") LocalDate end) {
        return R.success(pmsSjgReportDashboardService.monthReport(type, officeId, start, end));
    }

    @GetMapping("/month/count/report/download")
    @ApiOperation("深加工月度统计报表-下载")
    public void monthReportDownload(@RequestParam("type") Integer type,
                                    @RequestParam("officeId") Integer officeId,
                                    @RequestParam("start") LocalDate start,
                                    @RequestParam("end") LocalDate end,
                                    HttpServletResponse response) throws IOException {
        try {
            pmsSjgReportDashboardService.monthReportDownload(type, officeId, start, end, response);
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
