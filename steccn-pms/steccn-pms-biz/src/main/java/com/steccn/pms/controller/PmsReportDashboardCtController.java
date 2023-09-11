package com.steccn.pms.controller;

import com.steccn.common.dto.QtLineChartBarDTO;
import com.steccn.common.vo.R;
import com.steccn.pms.service.PmsReportDashboardCtService;
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
    public R<QtLineChartBarDTO> produceClass(@RequestParam("workShopId") Integer workShopId, @RequestParam("start") LocalDate start, @RequestParam("end") LocalDate end) {
        return R.success(pmsReportDashboardCtService.produceClass(workShopId, start, end));
    }

}
