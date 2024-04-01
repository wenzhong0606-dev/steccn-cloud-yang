package com.steccn.wms.service.impl;

import com.steccn.authserver.util.FeignOptional;
import com.steccn.common.constant.FeignConstant;
import com.steccn.common.dto.SteccnTableColumn;
import com.steccn.common.dto.SteccnTableDataDTO;
import com.steccn.common.dto.TupleDTO;
import com.steccn.common.util.LocalDateUtil;
import com.steccn.dp.dto.DpWmsDataDTO;
import com.steccn.dp.feign.RemoteDpWmsDataFeignApi;
import com.steccn.dp.vo.AdsWmsInventoryVO;
import com.steccn.wms.domain.WmsDeliveryReceiptInfo;
import com.steccn.wms.domain.WmsStorageReceiptInfo;
import com.steccn.wms.dto.WmsReportStatisticalDTO;
import com.steccn.wms.feign.RemoteWmsInventoryFeignApi;
import com.steccn.wms.service.WmsDeliveryReceiptService;
import com.steccn.wms.service.WmsReportService;
import com.steccn.wms.service.WmsStorageReceiptService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.Lists;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;


@Service
@RequiredArgsConstructor
@Slf4j
public class WmsReportServiceImpl implements WmsReportService {

    private final WmsDeliveryReceiptService wmsDeliveryReceiptService;
    private final WmsStorageReceiptService wmsStorageReceiptService;
    private final RemoteDpWmsDataFeignApi remoteDpWmsDataFeignApi;
    @Override
    public SteccnTableDataDTO statisticalData(WmsReportStatisticalDTO dto) {
        //定义表头数据
        SteccnTableDataDTO steccnTableDataDTO = new SteccnTableDataDTO();

        List<SteccnTableColumn> columns = Lists.newArrayList();
        SteccnTableColumn category = new SteccnTableColumn();
        category.setProp("category");
        category.setLabel("类别");
        columns.add(category);
        SteccnTableColumn category1 = new SteccnTableColumn();
        category.setProp("category1");
        category.setLabel("类别");
        columns.add(category1);

        //本区间时间(班次时间)
        LocalDateTime startTime = LocalDateUtil.getMorning(dto.getStartDate());
        LocalDateTime endTime = LocalDateUtil.getMorning(dto.getEndDate()).plusDays(1);
        //上区间时间(班次时间)
        TupleDTO<LocalDateTime, LocalDateTime> tupleDTO = LocalDateUtil.intervalTime(startTime, endTime);

        //获取上区间的入库数据
        List<WmsDeliveryReceiptInfo> upDeliveryList =  wmsDeliveryReceiptService.intervalData(startTime,endTime);
        //获取上区间的出库数据
        List<WmsStorageReceiptInfo> upStorageList = wmsStorageReceiptService.intervalData(startTime,endTime);
        //获取上区间相关物料的期末库存
        DpWmsDataDTO dpWmsDataDTO = new DpWmsDataDTO();
        dpWmsDataDTO.setDate(tupleDTO.getField2().toLocalDate().plusDays(1));
        List<AdsWmsInventoryVO> upEndingList = FeignOptional.of(() -> remoteDpWmsDataFeignApi.endingData(dpWmsDataDTO), FeignConstant.STECCN_DP_SERVICE_NAME).get();

        //获取本区间的入库数据
        List<WmsDeliveryReceiptInfo> deliveryList =  wmsDeliveryReceiptService.intervalData(tupleDTO.getField1(),tupleDTO.getField2());
        //获取本区间的出库数据
        List<WmsStorageReceiptInfo> storageList = wmsStorageReceiptService.intervalData(tupleDTO.getField1(),tupleDTO.getField2());
        //获取本区间相关物料的期末库存
        DpWmsDataDTO dpDTO = new DpWmsDataDTO();
        dpDTO.setDate(dto.getEndDate().plusDays(1));
        List<AdsWmsInventoryVO> enddingList = FeignOptional.of(() -> remoteDpWmsDataFeignApi.endingData(dpDTO), FeignConstant.STECCN_DP_SERVICE_NAME).get();
        //自制原片的
        return null;
    }
}
