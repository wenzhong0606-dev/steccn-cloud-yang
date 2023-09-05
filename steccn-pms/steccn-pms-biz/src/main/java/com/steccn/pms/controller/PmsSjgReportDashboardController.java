package com.steccn.pms.controller;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.lang.Tuple;
import com.steccn.common.dto.QtLineChartBarDTO;
import com.steccn.common.dto.TupleDTO;
import com.steccn.common.vo.R;
import com.steccn.pms.constant.PmManagerConstant;
import com.steccn.pms.service.PmsSjgReportDashboardService;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
    public R<QtLineChartBarDTO> lineProduce(@RequestParam("workShopId") Integer workShopId, @RequestParam("date") LocalDate date, @RequestParam("unitType") Integer unitType) {
        TupleDTO<String, Integer> unit = unit(unitType);
        return R.success(pmsSjgReportDashboardService.lineProduce(workShopId, date, unit.getField1(), unit.getField2()));
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
