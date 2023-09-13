package com.steccn.pms.controller;

import com.steccn.pms.service.PmsSjgReportDashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author yequan
 * @desc 深加工当日看板
 * @date 2023/9/12
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/pms/sjg/report/dashboard")
@Slf4j
public class PmsSjgReportDashboardController {

    private final PmsSjgReportDashboardService pmsSjgReportDashboardService;

}
