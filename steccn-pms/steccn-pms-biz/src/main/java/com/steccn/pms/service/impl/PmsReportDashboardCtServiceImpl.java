package com.steccn.pms.service.impl;

import com.steccn.common.dto.QtLineChartBarDTO;
import com.steccn.pms.service.PmsReportDashboardCtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

/**
 * @author yequan
 * @desc 基片车间生产驾驶舱-统计看板服务实现
 * @date 2023/9/11
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PmsReportDashboardCtServiceImpl implements PmsReportDashboardCtService {

    @Override
    public QtLineChartBarDTO produceClass(Integer workShopId, LocalDate start, LocalDate end) {

        return null;
    }
}
