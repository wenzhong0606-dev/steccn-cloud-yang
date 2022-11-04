package com.steccn.pms.controller;

import com.steccn.common.vo.R;
import com.steccn.pms.dto.SubstrateDayReportDTO;
import com.steccn.pms.service.ReportService;
import com.steccn.pms.vo.SubstrateDayReportVO;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * @author yequan
 * @desc 报表控制器
 */
@RestController
@RequestMapping("/pms/report")
@RequiredArgsConstructor
@Slf4j
public class ReportController {

    private final ReportService reportService;

    @PostMapping("/sub")
    @ApiOperation("基片日报表")
    public R<SubstrateDayReportVO> substrateDayReport(@RequestBody SubstrateDayReportDTO dto) {
        return R.success(reportService.substrateDayReport(dto));
    }
}
