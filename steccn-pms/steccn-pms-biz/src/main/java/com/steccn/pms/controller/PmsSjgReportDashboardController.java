package com.steccn.pms.controller;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.lang.Tuple;
import com.steccn.common.dto.QtLineChartBarDTO;
import com.steccn.common.dto.TupleDTO;
import com.steccn.common.vo.EpChartGraphVO;
import com.steccn.common.vo.EpChartGraphXAxisVO;
import com.steccn.common.vo.R;
import com.steccn.pms.constant.PmManagerConstant;
import com.steccn.pms.service.PmsSjgReportDashboardService;
import com.steccn.pms.vo.SjgDashboardLineProduceVO;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * @author yequan
 * @desc 深加工看板
 * @date 2023/9/5
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/pms/sjg/report/dashboard")
@Slf4j
public class PmsSjgReportDashboardController {

    private final PmsSjgReportDashboardService pmsSjgReportDashboardService;

    @GetMapping("/line/produce")
    @ApiOperation("产线产量")
    public R<QtLineChartBarDTO> lineProduce(@RequestParam("workShopId") Integer workShopId, @RequestParam("start") LocalDate start, @RequestParam("end") LocalDate end, @RequestParam("unitType") Integer unitType) {
        TupleDTO<String, Integer> unit = unit(unitType);
        return R.success(pmsSjgReportDashboardService.lineProduce(workShopId, start, end, unit.getField1(), unit.getField2()));
    }

    @GetMapping("/line/produce/detail")
    @ApiOperation("产线产量表格")
    public R<SjgDashboardLineProduceVO> lineProduceDetail(@RequestParam("workShopId") Integer workShopId, @RequestParam("start") LocalDate start, @RequestParam("end") LocalDate end, @RequestParam("unitType") Integer unitType) {
        TupleDTO<String, Integer> unit = unit(unitType);
        return R.success(pmsSjgReportDashboardService.lineProduceDetail(workShopId, start, end, unit.getField1(), unit.getField2()));
    }

    @GetMapping("/line/produce/classgroup")
    @ApiOperation("产线产量表格")
    public R<QtLineChartBarDTO> lineProduceClassGroup(@RequestParam("workShopId") Integer workShopId, @RequestParam("start") LocalDate start, @RequestParam("end") LocalDate end, @RequestParam("unitType") Integer unitType) {
        TupleDTO<String, Integer> unit = unit(unitType);
        return R.success(pmsSjgReportDashboardService.lineProduceClassGroup(workShopId, start, end, unit.getField1(), unit.getField2()));
    }


    @GetMapping("/line/produce/classgroup/detail")
    @ApiOperation("产线产量表格")
    public R<SjgDashboardLineProduceVO> lineProduceClassGroupDetail(@RequestParam("workShopId") Integer workShopId, @RequestParam("start") LocalDate start, @RequestParam("end") LocalDate end, @RequestParam("unitType") Integer unitType) {
        TupleDTO<String, Integer> unit = unit(unitType);
        return R.success(pmsSjgReportDashboardService.lineProduceClassGroupDetail(workShopId, start, end, unit.getField1(), unit.getField2()));
    }

    @GetMapping("/line/produce/passRate")
    @ApiOperation("产线合格率")
    public R<QtLineChartBarDTO> linePassRate(@RequestParam("workShopId") Integer workShopId, @RequestParam("start") LocalDate start, @RequestParam("end") LocalDate end) {
        return R.success(pmsSjgReportDashboardService.linePassRate(workShopId,start,end));
    }

    @GetMapping("/line/produce/passRate/detail")
    @ApiOperation("产线合格率详情")
    public R<SjgDashboardLineProduceVO> linePassRateDetail(@RequestParam("workShopId") Integer workShopId, @RequestParam("start") LocalDate start, @RequestParam("end") LocalDate end) {
        return R.success(pmsSjgReportDashboardService.linePassRateDetail(workShopId,start,end));
    }

    @GetMapping("/line/produce/classPassrate")
    @ApiOperation("产线班组合格率")
    public R<QtLineChartBarDTO> lineGroupPassRate(@RequestParam("workShopId") Integer workShopId, @RequestParam("start") LocalDate start, @RequestParam("end") LocalDate end) {
        return R.success(pmsSjgReportDashboardService.lineGroupPassRate(workShopId,start,end));
    }

    @GetMapping("/line/produce/classPassrate/detail")
    @ApiOperation("产线班组合格率详情")
    public R<SjgDashboardLineProduceVO> lineGroupPassRateDetail(@RequestParam("workShopId") Integer workShopId, @RequestParam("start") LocalDate start, @RequestParam("end") LocalDate end) {
        return R.success(pmsSjgReportDashboardService.lineGroupPassRateDetail(workShopId,start,end));
    }

    /**
     * 根据单位类型，获取转换单位
     *
     * @param unitType 单位类型
     * @return
     */
    private TupleDTO<String, Integer> unit(Integer unitType) {
        Assert.isTrue(unitType < 3 && unitType >= 0, "单位参数不合法");
        if (unitType == 0) {
            return new TupleDTO(PmManagerConstant.MEASURE_UNIT_PIECE, 0);
        } else if (unitType == 1) {
            return new TupleDTO(PmManagerConstant.MEASURE_UNIT_TON, 1);
        } else {
            return new TupleDTO(PmManagerConstant.MEASURE_UNIT_SM, 0);
        }
    }
}
