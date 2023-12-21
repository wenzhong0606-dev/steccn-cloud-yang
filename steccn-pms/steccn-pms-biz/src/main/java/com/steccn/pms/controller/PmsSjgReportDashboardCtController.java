package com.steccn.pms.controller;

import com.steccn.common.dto.QtLineChartBarDTO;
import com.steccn.common.dto.TupleDTO;
import com.steccn.common.vo.R;
import com.steccn.pms.service.PmsSjgReportDashboardCtService;
import com.steccn.pms.util.PmsUnitUtil;
import com.steccn.pms.vo.SjgCountReportVO;
import com.steccn.pms.vo.SjgDashboardLineProduceVO;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

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
    public R<List<SjgCountReportVO>> countReport(@RequestParam("workShopId") Integer workShopId, @RequestParam("start") LocalDate start, @RequestParam("end") LocalDate end) {
        return R.success();
    }

}
