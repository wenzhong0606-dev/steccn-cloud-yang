package com.steccn.ems.service.impl;

import com.steccn.authserver.util.FeignOptional;
import com.steccn.common.constant.FeignConstant;
import com.steccn.common.dto.TripleDTO;
import com.steccn.common.util.BigDecimalUtil;
import com.steccn.ems.dto.CeCarbonReportDTO;
import com.steccn.ems.service.CeCarbonReportService;
import com.steccn.ems.service.CeElectricSettingService;
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
    private final RemoteWmsStorageReceiptFeignApi remoteWmsStorageReceiptFeignApi;

    @Override
    public CeCarbonReportDTO carbonSumReport(LocalDate start, LocalDate end, Integer granularity) {
        //获取有效期内的
        Map<LocalDate,BigDecimal> valueList1 = ceElectricSettingService.carbonSumReportDay(start, end);
        log.info("valueList1 -> {}",valueList1);
        Map<LocalDate, BigDecimal> valueMap1 = FeignOptional.of(() -> remoteWmsStorageReceiptFeignApi.produceConsumeQuantity(new TripleDTO<>(1554387196106801152L, start, end)), FeignConstant.STECCN_WMS_SERVICE_NAME).get();
        log.info("valueMap1 -> {}",valueMap1);


        return null;
    }
}
