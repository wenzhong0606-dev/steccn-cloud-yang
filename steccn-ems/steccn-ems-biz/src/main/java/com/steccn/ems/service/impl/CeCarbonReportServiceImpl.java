package com.steccn.ems.service.impl;

import com.steccn.authserver.util.FeignOptional;
import com.steccn.common.constant.FeignConstant;
import com.steccn.common.dto.TripleDTO;
import com.steccn.common.util.BigDecimalUtil;
import com.steccn.ems.domain.CeProduceIngredientSetting;
import com.steccn.ems.dto.CeCarbonReportDTO;
import com.steccn.ems.service.CeCarbonReportService;
import com.steccn.ems.service.CeElectricSettingService;
import com.steccn.ems.service.CeProduceIngredientSettingService;
import com.steccn.wms.feign.RemoteWmsStorageReceiptFeignApi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * @author yequan
 * @desc 碳排放报表接口实现
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CeCarbonReportServiceImpl implements CeCarbonReportService {

    private final CeElectricSettingService ceElectricSettingService;
    private final CeProduceIngredientSettingService ceProduceIngredientSettingService;

    @Override
    public CeCarbonReportDTO carbonSumReport(LocalDate start, LocalDate end, Integer granularity) {
        //获取有效期内的
        Map<LocalDate,BigDecimal> valueMap = ceElectricSettingService.carbonSumReportDay(start, end);
        log.info("valueMap -> {}",valueMap);

        List<Map<LocalDate,BigDecimal>> valueList2 = ceProduceIngredientSettingService.carbonSumReportDay(start, end);
        log.info("valueList2 -> {}",valueList2);

        return null;
    }
}
