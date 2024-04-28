package com.steccn.ems.service.impl;

import com.steccn.common.util.BigDecimalUtil;
import com.steccn.ems.dto.CeCarbonReportDTO;
import com.steccn.ems.service.CeCarbonReportService;
import com.steccn.ems.service.CeElectricSettingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * @author yequan
 * @desc 碳排放报表接口实现
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CeCarbonReportServiceImpl implements CeCarbonReportService {

    private final CeElectricSettingService ceElectricSettingService;

    @Override
    public CeCarbonReportDTO carbonSumReport(LocalDate start, LocalDate end, Integer granularity) {
        //获取有效期内的
        List<BigDecimal> valueList1 = ceElectricSettingService.carbonSumReportDay(start, end);
        return null;
    }
}
