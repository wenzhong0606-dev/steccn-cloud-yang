package com.steccn.wms.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.common.constant.FeignConstant;
import com.steccn.common.constant.SysDictConstant;
import com.steccn.common.dto.SteccnTableColumn;
import com.steccn.common.dto.SteccnTableDataDTO;
import com.steccn.common.dto.TupleDTO;
import com.steccn.common.util.BigDecimalUtil;
import com.steccn.common.util.IntegerUtil;
import com.steccn.common.util.LocalDateUtil;
import com.steccn.dp.dto.DpWmsDataDTO;
import com.steccn.dp.feign.RemoteDpWmsDataFeignApi;
import com.steccn.dp.vo.AdsWmsInventoryVO;
import com.steccn.platform.feign.RemoteDictFeignApi;
import com.steccn.platform.feign.RemoteOfficeFeign;
import com.steccn.platform.feign.RemoteSpareFeignApi;
import com.steccn.platform.feign.RemoteSpareUnitConvertFeignApi;
import com.steccn.platform.vo.EqpSpareVO;
import com.steccn.platform.vo.SysDictVO;
import com.steccn.platform.vo.SysOfficeVO;
import com.steccn.platform.vo.SysSpareUnitConversionVO;
import com.steccn.pms.constant.PmManagerConstant;
import com.steccn.pms.feign.RemoteCompleteStorageFeignApi;
import com.steccn.pms.feign.RemotePmsFeignApi;
import com.steccn.pms.util.UnitConvertUtil;
import com.steccn.wms.domain.WmsDeliveryReceiptInfo;
import com.steccn.wms.domain.WmsStorageReceiptInfo;
import com.steccn.wms.dto.WmsInventoryDaySheetDTO;
import com.steccn.wms.dto.WmsOutInWarehouseInfoDTO;
import com.steccn.wms.dto.WmsReportStatisticalDTO;
import com.steccn.wms.dto.WmsStorageReceiptInfoDTO;
import com.steccn.wms.feign.RemoteWmsInventoryFeignApi;
import com.steccn.wms.service.WmsDeliveryReceiptService;
import com.steccn.wms.service.WmsReportService;
import com.steccn.wms.service.WmsStorageReceiptService;
import com.steccn.wms.vo.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.Lists;
import org.apache.commons.compress.utils.Sets;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static cn.hutool.core.collection.CollUtil.keySet;


@Service
@RequiredArgsConstructor
@Slf4j
public class WmsReportServiceImpl implements WmsReportService {

    private final WmsDeliveryReceiptService wmsDeliveryReceiptService;
    private final WmsStorageReceiptService wmsStorageReceiptService;
    private final RemoteDpWmsDataFeignApi remoteDpWmsDataFeignApi;
    private final RemoteSpareFeignApi remoteSpareFeignApi;
    private final RemoteSpareUnitConvertFeignApi remoteSpareUnitConvertFeignApi;
    private final RemoteDictFeignApi remoteDictFeignApi;
    private final RemoteCompleteStorageFeignApi remoteCompleteStorageFeignApi;
    private final RemoteOfficeFeign remoteOfficeFeign;
    @Override
    public SteccnTableDataDTO statisticalData(WmsReportStatisticalDTO dto) {

        //本区间时间(班次时间)
        LocalDateTime startTime = LocalDateUtil.getMorning(dto.getStartDate());
        LocalDateTime endTime = LocalDateUtil.getMorning(dto.getEndDate()).plusDays(1);
        //上区间时间(班次时间)
        TupleDTO<LocalDateTime, LocalDateTime> tupleDTO = LocalDateUtil.intervalTime(startTime, endTime);
        //定义表头数据
        SteccnTableDataDTO steccnTableDataDTO = new SteccnTableDataDTO();

        List<SteccnTableColumn> columns = Lists.newArrayList();
        SteccnTableColumn category = new SteccnTableColumn();
        category.setProp("category");
        category.setLabel("类别");
        columns.add(category);
        SteccnTableColumn category1 = new SteccnTableColumn();
        category1.setProp("category1");
        category1.setLabel("类别");
        columns.add(category1);
        SteccnTableColumn upperInterval = new SteccnTableColumn();
        upperInterval.setProp("upperInterval");
        upperInterval.setLabel("上区间（"+formattedDate(tupleDTO.getField1())+" ~ "+formattedDate(tupleDTO.getField2().minusDays(1))+")");
        List<SteccnTableColumn> upChildrenColumn = Lists.newArrayList();
        SteccnTableColumn upSteccnTableColumn = new SteccnTableColumn();
        upSteccnTableColumn.setLabel("入库(㎡)");
        upSteccnTableColumn.setProp("warehousing");
        upChildrenColumn.add(upSteccnTableColumn);
        SteccnTableColumn upColumn1 = new SteccnTableColumn();
        upColumn1.setProp("outbound");
        upColumn1.setLabel("出库(㎡)");
        upChildrenColumn.add(upColumn1);
        SteccnTableColumn upColumn2 = new SteccnTableColumn();
        upColumn2.setProp("inventory");
        upColumn2.setLabel("库存(㎡)");
        upChildrenColumn.add(upColumn2);
        upperInterval.setChildren(upChildrenColumn);
        columns.add(upperInterval);
        SteccnTableColumn homeInterval = new SteccnTableColumn();
        homeInterval.setProp("homeInterval");
        homeInterval.setLabel("本区间（"+formattedDate(startTime)+" ~ "+formattedDate(endTime.minusDays(1))+")");
        List<SteccnTableColumn> childrenColumn = Lists.newArrayList();
        SteccnTableColumn steccnTableColumn = new SteccnTableColumn();
        steccnTableColumn.setLabel("入库(㎡)");
        steccnTableColumn.setProp("warehousing1");
        childrenColumn.add(steccnTableColumn);
        SteccnTableColumn column1 = new SteccnTableColumn();
        column1.setProp("outbound1");
        column1.setLabel("出库(㎡)");
        childrenColumn.add(column1);
        SteccnTableColumn column2 = new SteccnTableColumn();
        column2.setProp("inventory1");
        column2.setLabel("库存(㎡)");
        childrenColumn.add(column2);
        homeInterval.setChildren(childrenColumn);
        columns.add(homeInterval);
        SteccnTableColumn growthRate = new SteccnTableColumn();
        growthRate.setProp("growthRate");
        growthRate.setLabel("增长率");
        columns.add(growthRate);
        steccnTableDataDTO.setColumns(columns);
        List<WmsReportDataVO> tableList = Lists.newArrayList();
        // 自制原片：编码以“P1.Z.P”开头的物料
        WmsReportDataVO homemadeRaw = new WmsReportDataVO();
        homemadeRaw.setCategory("原片");
        homemadeRaw.setCategory1("自制原片");
        TupleDTO<String, Integer> homemadeRawMaterial = new TupleDTO<>();
        homemadeRawMaterial.setField1("P1.Z.P");
        WmsReportDataVO homemadeRawValue = getWmsReportDataVO(homemadeRaw, homemadeRawMaterial, tupleDTO, startTime, endTime, dto.getEndDate());
        tableList.add(homemadeRawValue);

        //外购原片：编码以“P1.W.P”开头的物料
        WmsReportDataVO rawPurchased = new WmsReportDataVO();
        rawPurchased.setCategory("原片");
        rawPurchased.setCategory1("外购原片");
        TupleDTO<String, Integer> rawMaterialsPurchased = new TupleDTO<>();
        rawMaterialsPurchased.setField1("P1.W.P");
        WmsReportDataVO rawPurchasedValue = getWmsReportDataVO(rawPurchased, rawMaterialsPurchased, tupleDTO, startTime, endTime, dto.getEndDate());
        tableList.add(rawPurchasedValue);

        //镀膜无印：编码以“P1.Z.S”开头，且产品属性为“镀膜无印”的物料(0 镀膜无印,1双膜无印,2无膜无印,3无膜丝印)
        WmsReportDataVO coatingWithoutPrinting = new WmsReportDataVO();
        TupleDTO<String, Integer> coatingWithoutPrintingMaterial = new TupleDTO<>();
        coatingWithoutPrintingMaterial.setField1("P1.Z.S");
        coatingWithoutPrintingMaterial.setField2(0);
        coatingWithoutPrinting.setCategory("深加工成品");
        coatingWithoutPrinting.setCategory1("镀膜无印");
        WmsReportDataVO coatingWithoutPrintingValue = getWmsReportDataVO(coatingWithoutPrinting, coatingWithoutPrintingMaterial, tupleDTO, startTime, endTime, dto.getEndDate());
        tableList.add(coatingWithoutPrintingValue);

        //双膜无印：编码以“P1.Z.S”开头，且产品属性为“双膜无印”的物料
        TupleDTO<String, Integer> dualModeNoPrintingMaterial = new TupleDTO<>();
        dualModeNoPrintingMaterial.setField1("P1.Z.S");
        dualModeNoPrintingMaterial.setField2(1);
        WmsReportDataVO dualModeNoPrinting = new WmsReportDataVO();
        dualModeNoPrinting.setCategory("深加工成品");
        dualModeNoPrinting.setCategory1("双膜无印");
        WmsReportDataVO dualModeNoPrintingValue = getWmsReportDataVO(dualModeNoPrinting, dualModeNoPrintingMaterial, tupleDTO, startTime, endTime, dto.getEndDate());
        tableList.add(dualModeNoPrintingValue);

        //无膜无印：编码以“P1.Z.S”开头，且产品属性为“无膜无印”的物料
        WmsReportDataVO noFilmNoPrinting = new WmsReportDataVO();
        noFilmNoPrinting.setCategory("深加工成品");
        noFilmNoPrinting.setCategory1("无膜无印");
        TupleDTO<String, Integer> noFilmNoPrintingMaterial = new TupleDTO<>();
        noFilmNoPrintingMaterial.setField1("P1.Z.S");
        noFilmNoPrintingMaterial.setField2(2);
        WmsReportDataVO noFilmNoPrintingValue = getWmsReportDataVO(noFilmNoPrinting, noFilmNoPrintingMaterial, tupleDTO, startTime, endTime, dto.getEndDate());
        tableList.add(noFilmNoPrintingValue);

        //无膜丝印：编码以“P1.Z.S”开头，且产品属性为“无膜丝印”的物料
        TupleDTO<String, Integer> screenPrintingWithoutFilmMaterial = new TupleDTO<>();
        screenPrintingWithoutFilmMaterial.setField1("P1.Z.S");
        screenPrintingWithoutFilmMaterial.setField2(3);
        WmsReportDataVO screenPrintingWithoutFilm = new WmsReportDataVO();
        screenPrintingWithoutFilm.setCategory("深加工成品");
        screenPrintingWithoutFilm.setCategory1("无膜丝印");
        WmsReportDataVO screenPrintingWithoutFilmValue = getWmsReportDataVO(screenPrintingWithoutFilm, screenPrintingWithoutFilmMaterial, tupleDTO, startTime, endTime, dto.getEndDate());
        tableList.add(screenPrintingWithoutFilmValue);

        //其他：编码以“P1.Z.S”开头，且产品属性为空值的物料
        TupleDTO<String, Integer> elseMaterial = new TupleDTO<>();
        elseMaterial.setField1("P1.Z.S");
        WmsReportDataVO elseVO = new WmsReportDataVO();
        elseVO.setCategory("深加工成品");
        elseVO.setCategory1("其他");
        WmsReportDataVO elseVOValue = getWmsReportDataVO(elseVO, elseMaterial, tupleDTO, startTime, endTime, dto.getEndDate());
        tableList.add(elseVOValue);
        if (CollUtil.isNotEmpty(tableList)){
            Map<String, List<WmsReportDataVO>> dataMap = tableList.stream().collect(Collectors.groupingBy(WmsReportDataVO::getCategory));
            List<WmsReportDataVO> originalList  = dataMap.get("原片");
            BigDecimal  upInventoryQuantity = originalList.stream()
                    .map(WmsReportDataVO::getInventory)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);;
            BigDecimal  deliveryQuantity = originalList.stream()
                    .map(WmsReportDataVO::getOutbound1)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);;
            BigDecimal  inventoryQuantity = originalList.stream()
                    .map(WmsReportDataVO::getInventory1)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);; ;
            BigDecimal upDeliveryQuantity = originalList.stream()
                    .map(WmsReportDataVO::getOutbound)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            tableList.forEach( wmsReportDataVO -> {
                if (("原片".equals(wmsReportDataVO.getCategory()))){
                    wmsReportDataVO.setOutbound(upDeliveryQuantity);
                    wmsReportDataVO.setOutbound1(deliveryQuantity);
                    wmsReportDataVO.setInventory(upInventoryQuantity);
                    wmsReportDataVO.setInventory1(inventoryQuantity);
                    BigDecimal multiply = BigDecimalUtil.multiply(
                            BigDecimalUtil.divide(
                                    BigDecimalUtil.subtract(inventoryQuantity, upInventoryQuantity),
                                    upInventoryQuantity
                            ),
                            new BigDecimal("100") // 使用BigDecimal对象表示100%
                    );
                    DecimalFormat decimalFormat = new DecimalFormat("0.00%");
                    wmsReportDataVO.setGrowthRate(decimalFormat.format(multiply));
                }
            });
        }
        steccnTableDataDTO.setTableList(tableList);
        return steccnTableDataDTO;
    }

    @Override
    public SteccnTableDataDTO outInWarehouseInfo(WmsOutInWarehouseInfoDTO dto) {
        SteccnTableDataDTO steccnTableDataDTO = new SteccnTableDataDTO();
        LocalDateTime  morningStartTime = LocalDateUtil.getMorning(dto.getDate());
        LocalDateTime  nightStartTime = LocalDateUtil.getNight(dto.getDate());
        LocalDateTime  nightEndTime = LocalDateUtil.getMorning(dto.getDate()).plusDays(1);
        //出库数据
        List<WmsDeliveryReceiptInfoVO> wmsDeliveryReceiptInfoVOList = wmsDeliveryReceiptService.intervalDataVO(morningStartTime,nightEndTime);
        //入库数据
        List<WmsStorageReceiptInfoVO> wmsStorageReceiptInfoVOList = wmsStorageReceiptService.intervalDataVO(morningStartTime, nightEndTime);

        //P1.Z.P ——自制原片
        List<WmsOutInWarehouseInfoVO> voList = Lists.newArrayList();
        TupleDTO<String, Integer> materialDTO = new TupleDTO<>();
        materialDTO.setField1("P1.Z.P");
        List<WmsOutInWarehouseInfoVO> inOutInfoDataVO = getInOutInfoDataVO(wmsDeliveryReceiptInfoVOList, wmsStorageReceiptInfoVOList, "自制/原片", materialDTO, morningStartTime, nightStartTime, nightEndTime,0);
        //P1.W.P ——外购原片
        TupleDTO<String, Integer> materialDTO1 = new TupleDTO<>();
        materialDTO1.setField1("P1.W.P");
        List<WmsOutInWarehouseInfoVO> inOutInfoDataVO1 = getInOutInfoDataVO(wmsDeliveryReceiptInfoVOList, wmsStorageReceiptInfoVOList, "外购/原片", materialDTO1, morningStartTime, nightStartTime, nightEndTime,1);
        //P1.Z.S ——自制加工
        TupleDTO<String, Integer> materialDTO2 = new TupleDTO<>();
        materialDTO2.setField1("P1.Z.S");
        List<WmsOutInWarehouseInfoVO> inOutInfoDataVO2 = getInOutInfoDataVO(wmsDeliveryReceiptInfoVOList, wmsStorageReceiptInfoVOList, "自制/加工", materialDTO2, morningStartTime, nightStartTime, nightEndTime,2);
        //P1.W.S ——外购加工
        TupleDTO<String, Integer> materialDTO3 = new TupleDTO<>();
        materialDTO3.setField1("P1.W.S");
        List<WmsOutInWarehouseInfoVO> inOutInfoDataVO3 = getInOutInfoDataVO(wmsDeliveryReceiptInfoVOList, wmsStorageReceiptInfoVOList, "外购/加工", materialDTO3, morningStartTime, nightStartTime, nightEndTime,3);
        voList.addAll(inOutInfoDataVO);
        voList.addAll(inOutInfoDataVO2);
        voList.addAll(inOutInfoDataVO1);
        voList.addAll(inOutInfoDataVO3);
        //合计
        if (CollUtil.isNotEmpty(voList)){
            //分类合计3
            //P1_Z 自制 P1_W 外购
            Map<String, List<WmsOutInWarehouseInfoVO>> p1ZMap = voList.stream().filter(info -> info.getMaterialCode() != null && info.getMaterialCode().startsWith("P1.Z") && info.getTotalFlag() == 0).collect(Collectors.groupingBy(WmsOutInWarehouseInfoVO::getUnit));
            p1ZMap.forEach((k,v)->{
                WmsOutInWarehouseInfoVO wmsOutInWarehouseInfoVO = new WmsOutInWarehouseInfoVO();
                wmsOutInWarehouseInfoVO.setType("合计（自制）");
                wmsOutInWarehouseInfoVO.setType1(3);
                wmsOutInWarehouseInfoVO.setTotalFlag(1);
                wmsOutInWarehouseInfoVO.setSortFlag1(0);
                v.forEach(info->{
                    wmsOutInWarehouseInfoVO.setMorningShiftStorage(BigDecimalUtil.add(wmsOutInWarehouseInfoVO.getMorningShiftStorage(),info.getMorningShiftStorage()));
                    wmsOutInWarehouseInfoVO.setMorningShiftOutbound(BigDecimalUtil.add(wmsOutInWarehouseInfoVO.getMorningShiftOutbound(),info.getMorningShiftOutbound()));
                    wmsOutInWarehouseInfoVO.setNightShiftStorage(BigDecimalUtil.add(wmsOutInWarehouseInfoVO.getNightShiftStorage(),info.getNightShiftStorage()));
                    wmsOutInWarehouseInfoVO.setNightShiftOutbound(BigDecimalUtil.add(wmsOutInWarehouseInfoVO.getNightShiftOutbound(),info.getNightShiftOutbound()));
                    wmsOutInWarehouseInfoVO.setTotalInboundQuantity(BigDecimalUtil.add(wmsOutInWarehouseInfoVO.getTotalInboundQuantity(),info.getTotalInboundQuantity()));
                    wmsOutInWarehouseInfoVO.setTotalOutboundQuantity(BigDecimalUtil.add(wmsOutInWarehouseInfoVO.getTotalOutboundQuantity(),info.getTotalOutboundQuantity()));
                });
                voList.add(wmsOutInWarehouseInfoVO);
            });

            Map<String, List<WmsOutInWarehouseInfoVO>> p1WMap = voList.stream().filter(info -> info.getMaterialCode() != null && info.getMaterialCode().startsWith("P1.W") && info.getTotalFlag() == 0).collect(Collectors.groupingBy(WmsOutInWarehouseInfoVO::getUnit));
            p1WMap.forEach((k,v)->{
                WmsOutInWarehouseInfoVO wmsOutInWarehouseInfoVO = new WmsOutInWarehouseInfoVO();
                wmsOutInWarehouseInfoVO.setType("合计（外购）");
                wmsOutInWarehouseInfoVO.setType1(3);
                wmsOutInWarehouseInfoVO.setTotalFlag(1);
                wmsOutInWarehouseInfoVO.setSortFlag1(1);
                v.forEach(info->{
                    wmsOutInWarehouseInfoVO.setMorningShiftStorage(BigDecimalUtil.add(wmsOutInWarehouseInfoVO.getMorningShiftStorage(),info.getMorningShiftStorage()));
                    wmsOutInWarehouseInfoVO.setMorningShiftOutbound(BigDecimalUtil.add(wmsOutInWarehouseInfoVO.getMorningShiftOutbound(),info.getMorningShiftOutbound()));
                    wmsOutInWarehouseInfoVO.setNightShiftStorage(BigDecimalUtil.add(wmsOutInWarehouseInfoVO.getNightShiftStorage(),info.getNightShiftStorage()));
                    wmsOutInWarehouseInfoVO.setNightShiftOutbound(BigDecimalUtil.add(wmsOutInWarehouseInfoVO.getNightShiftOutbound(),info.getNightShiftOutbound()));
                    wmsOutInWarehouseInfoVO.setTotalInboundQuantity(BigDecimalUtil.add(wmsOutInWarehouseInfoVO.getTotalInboundQuantity(),info.getTotalInboundQuantity()));
                    wmsOutInWarehouseInfoVO.setTotalOutboundQuantity(BigDecimalUtil.add(wmsOutInWarehouseInfoVO.getTotalOutboundQuantity(),info.getTotalOutboundQuantity()));
                });
                voList.add(wmsOutInWarehouseInfoVO);
            });
            //总合计4
            Map<String, List<WmsOutInWarehouseInfoVO>> collect = voList.stream().filter(info -> info.getTotalFlag() == 0).collect(Collectors.groupingBy(WmsOutInWarehouseInfoVO::getUnit));
            collect.forEach((k,v)->{
                WmsOutInWarehouseInfoVO wmsOutInWarehouseInfoVO = new WmsOutInWarehouseInfoVO();
                wmsOutInWarehouseInfoVO.setType("合计");
                wmsOutInWarehouseInfoVO.setType1(4);
                wmsOutInWarehouseInfoVO.setTotalFlag(1);
                wmsOutInWarehouseInfoVO.setUnit(k);
                v.forEach(info->{
                    wmsOutInWarehouseInfoVO.setMorningShiftStorage(BigDecimalUtil.add(wmsOutInWarehouseInfoVO.getMorningShiftStorage(),info.getMorningShiftStorage()));
                    wmsOutInWarehouseInfoVO.setMorningShiftOutbound(BigDecimalUtil.add(wmsOutInWarehouseInfoVO.getMorningShiftOutbound(),info.getMorningShiftOutbound()));
                    wmsOutInWarehouseInfoVO.setNightShiftStorage(BigDecimalUtil.add(wmsOutInWarehouseInfoVO.getNightShiftStorage(),info.getNightShiftStorage()));
                    wmsOutInWarehouseInfoVO.setNightShiftOutbound(BigDecimalUtil.add(wmsOutInWarehouseInfoVO.getNightShiftOutbound(),info.getNightShiftOutbound()));
                    wmsOutInWarehouseInfoVO.setTotalInboundQuantity(BigDecimalUtil.add(wmsOutInWarehouseInfoVO.getTotalInboundQuantity(),info.getTotalInboundQuantity()));
                    wmsOutInWarehouseInfoVO.setTotalOutboundQuantity(BigDecimalUtil.add(wmsOutInWarehouseInfoVO.getTotalOutboundQuantity(),info.getTotalOutboundQuantity()));
                });
                voList.add(wmsOutInWarehouseInfoVO);
            });
        }
        //排序
        List<WmsOutInWarehouseInfoVO> collect = voList.stream()
                .sorted(Comparator.comparing(WmsOutInWarehouseInfoVO::getSortFlag, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(WmsOutInWarehouseInfoVO::getSortFlag1, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(WmsOutInWarehouseInfoVO::getThick, Comparator.nullsLast(Comparator.naturalOrder())))
                .collect(Collectors.toList());
        steccnTableDataDTO.setTableList(collect);
        return steccnTableDataDTO;




    }

    @Override
    public List<WmsInventoryDaySheetVO> daySheetData(WmsInventoryDaySheetDTO dto) {
        //获取数据
        List<WmsInventoryDaySheetVO>  daySheetDataList =  wmsStorageReceiptService.daySheetData(dto);
        List<WmsInventoryDaySheetVO> resList = Lists.newArrayList();
        if (CollUtil.isNotEmpty(daySheetDataList)){
            //根据入库单单号找到对应的生产订单的产线名称
            Set<String> codeSet  = Sets.newHashSet();
            Set<Long> typeIds = Sets.newHashSet();
            Set<Integer> officeIdSet = Sets.newHashSet();
            daySheetDataList.forEach( wmsInventoryDaySheetVO -> {
                codeSet.add(wmsInventoryDaySheetVO.getCode());
                typeIds.add(wmsInventoryDaySheetVO.getType());
                officeIdSet.add(wmsInventoryDaySheetVO.getWarehouseId());
            });
            Map<String,String> lineNameMap =  FeignOptional.of(()->remoteCompleteStorageFeignApi.storageReceiptCodeGetLineName(codeSet),FeignConstant.STECCN_PMS_SERVICE_NAME).get();
            //获取产线名称
            Map<Long, String> typeNameMap = FeignOptional.of(() -> remoteDictFeignApi.listByIdCh(typeIds), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            Map<Integer, SysOfficeVO> officeMap = FeignOptional.of(() -> remoteOfficeFeign.listById(officeIdSet), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            daySheetDataList.forEach( wmsInventoryDaySheetVO -> wmsInventoryDaySheetVO.setProductionLineName(lineNameMap.get(wmsInventoryDaySheetVO.getCode())));
            //根据产线处理数据
            Map<String, List<WmsInventoryDaySheetVO>> lineMap = daySheetDataList.stream()
                    .collect(Collectors.groupingBy(vo -> Optional.ofNullable(vo.getProductionLineName()).orElse("null")));
            lineMap.forEach((k,v)->{
                //根据仓库处理数据
                Map<Integer, List<WmsInventoryDaySheetVO>> warehouseMap = v.stream().collect(Collectors.groupingBy(WmsInventoryDaySheetVO::getWarehouseId));
                warehouseMap.forEach((warehouseKey,warehouseValue)->{
                    //根据类型处理数据
                    Map<Long, List<WmsInventoryDaySheetVO>> typeMap = warehouseValue.stream().collect(Collectors.groupingBy(WmsInventoryDaySheetVO::getType));
                    typeMap.forEach((typeKey,typeValue)->{
                        //根据物料处理数据
                        Map<String, List<WmsInventoryDaySheetVO>> materialNameMap = warehouseValue.stream().collect(Collectors.groupingBy(WmsInventoryDaySheetVO::getMaterialName));
                        materialNameMap.forEach((materialKey,materialValue)->{
                            //根据状态处理数据
                            Map<String, List<WmsInventoryDaySheetVO>> statusMap = warehouseValue.stream().collect(Collectors.groupingBy(WmsInventoryDaySheetVO::getStatus));
                            statusMap.forEach((statusKey,statusValue)->{
                                //根据班次处理数据
                                Map<String, List<WmsInventoryDaySheetVO>> classesMap = warehouseValue.stream().collect(Collectors.groupingBy(WmsInventoryDaySheetVO::getClasses));
                                classesMap.forEach((classesKey,classesValue)->{
                                    WmsInventoryDaySheetVO wmsInventoryDaySheetVO = new WmsInventoryDaySheetVO();
                                    WmsInventoryDaySheetVO daySheetVO = classesValue.get(0);
                                    wmsInventoryDaySheetVO.setProductionLineName(daySheetVO.getProductionLineName());
                                    wmsInventoryDaySheetVO.setWarehouseName(officeMap.get(daySheetVO.getWarehouseId()).getOfficeName());
                                    wmsInventoryDaySheetVO.setTypeName(typeNameMap.get(daySheetVO.getType()));
                                    wmsInventoryDaySheetVO.setClasses(daySheetVO.getClasses());
                                    wmsInventoryDaySheetVO.setMaterialName(daySheetVO.getMaterialName());
                                    wmsInventoryDaySheetVO.setStatus(daySheetVO.getStatus());
                                    BigDecimal totalQuantity = BigDecimal.ZERO;
                                    List<String> caseNoList = Lists.newArrayList();
                                    Integer caseNum = null;
                                    for (WmsInventoryDaySheetVO inventoryDaySheetVO : classesValue) {
                                        totalQuantity =BigDecimalUtil.add(totalQuantity,inventoryDaySheetVO.getTotalQuantity());
                                        caseNum = IntegerUtil.add(caseNum,inventoryDaySheetVO.getCaseNum());
                                        List<String> boxNumbers = Arrays.stream(inventoryDaySheetVO.getCaseNo().split(",\\s*"))
                                                .map(String::trim)
                                                .collect(Collectors.toList());
                                        caseNoList.addAll(boxNumbers);
                                    }
                                    wmsInventoryDaySheetVO.setCaseNum(caseNum);
                                    wmsInventoryDaySheetVO.setTotalQuantity(totalQuantity);
                                    wmsInventoryDaySheetVO.setCaseNoList(caseNoList);
                                    resList.add(wmsInventoryDaySheetVO);
                                });
                            });
                        });
                    });
                });
            });
            //
            if (Objects.nonNull(dto.getProductionLineName())){
              return resList.stream()
                        .filter(vo -> !dto.getProductionLineName().equals(vo.getMaterialName()))
                        .collect(Collectors.toList());
            }
        }
        return resList;
    }

    @Override
    public List<WmsInventoryDaySheetCaseVO> daySheetCaseList(WmsInventoryDaySheetDTO dto) {
      return wmsStorageReceiptService.daySheetCaseList(dto);
    }


    /**
     * 获取指定格式日期
     * @param dateTime
     * @return
     */
    private String formattedDate(LocalDateTime dateTime){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd");
        return dateTime.format(formatter);
    }

    /**
     * 获取数据
     * @param wmsReportDataVO
     * @param material
     * @param tupleDTO
     * @param startTime
     * @param endTime
     * @return
     */
    private WmsReportDataVO getWmsReportDataVO(WmsReportDataVO wmsReportDataVO, TupleDTO<String, Integer> material, TupleDTO<LocalDateTime, LocalDateTime> tupleDTO,LocalDateTime startTime,LocalDateTime endTime,LocalDate endDate){
        List<EqpSpareVO> homemadeRawMaterialList = FeignOptional.of(() -> remoteSpareFeignApi.listByProperty(material), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        Set<Long> homemadeRawMaterialIds = homemadeRawMaterialList.stream().map(EqpSpareVO::getId).collect(Collectors.toSet());
        List<WmsDeliveryReceiptInfo> upHomemadeRawDeliveryList = wmsDeliveryReceiptService.intervalData(tupleDTO.getField1(), tupleDTO.getField2(), homemadeRawMaterialIds);
        List<WmsDeliveryReceiptInfo> homemadeRawDeliveryList = wmsDeliveryReceiptService.intervalData(startTime, endTime, homemadeRawMaterialIds);

        List<WmsStorageReceiptInfo> upHomemadeRawStorageList = wmsStorageReceiptService.intervalData(tupleDTO.getField1(), tupleDTO.getField2(), homemadeRawMaterialIds);
        List<WmsDeliveryReceiptInfo> homemadeRawStorageList = wmsDeliveryReceiptService.intervalData(startTime, endTime, homemadeRawMaterialIds);

        //单位换算
        SysDictVO unitPiece = FeignOptional.of(() -> remoteDictFeignApi.getByCode(PmManagerConstant.MEASURE_UNIT_PIECE, SysDictConstant.TYPE_MEASURE_UNIT), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        SysDictVO unitSm = FeignOptional.of(() -> remoteDictFeignApi.getByCode(PmManagerConstant.MEASURE_UNIT_SM, SysDictConstant.TYPE_MEASURE_UNIT), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        Map<Long, List<SysSpareUnitConversionVO>> materialConvertMap = FeignOptional.of(() -> remoteSpareUnitConvertFeignApi.convert(homemadeRawMaterialIds), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        // 上区间入库
        upHomemadeRawDeliveryList.forEach(delivery -> {
            wmsReportDataVO.setWarehousing(BigDecimalUtil.add(wmsReportDataVO.getWarehousing(), UnitConvertUtil.convertMaterial(materialConvertMap.get(delivery.getMaterialId()), unitPiece.getId(), unitSm.getId(), delivery.getQuantity(), 4)));
        });
        // 本区间入库
        homemadeRawDeliveryList.forEach( delivery->{
            wmsReportDataVO.setWarehousing1(BigDecimalUtil.add(wmsReportDataVO.getWarehousing1(), UnitConvertUtil.convertMaterial(materialConvertMap.get(delivery.getMaterialId()), unitPiece.getId(), unitSm.getId(), delivery.getQuantity(), 4)));
        });
        // 上区间出库
        upHomemadeRawStorageList.forEach( storageInfo->{
            wmsReportDataVO.setOutbound(BigDecimalUtil.add(wmsReportDataVO.getOutbound(), UnitConvertUtil.convertMaterial(materialConvertMap.get(storageInfo.getMaterialId()), unitPiece.getId(), unitSm.getId(), storageInfo.getQuantity(), 4)));
        });
        // 本区间出库
        homemadeRawStorageList.forEach(storageInfo ->{
            wmsReportDataVO.setOutbound1(BigDecimalUtil.add(wmsReportDataVO.getOutbound1(), UnitConvertUtil.convertMaterial(materialConvertMap.get(storageInfo.getMaterialId()), unitPiece.getId(), unitSm.getId(), storageInfo.getQuantity(), 4)));
        });
        //上区间-库存
        DpWmsDataDTO dpWmsDataDTO = new DpWmsDataDTO();
        dpWmsDataDTO.setDate(tupleDTO.getField2().toLocalDate().plusDays(1));
        dpWmsDataDTO.setMaterialIds(homemadeRawMaterialIds);
        List<AdsWmsInventoryVO> upHomemadeRawEndingList = FeignOptional.of(() -> remoteDpWmsDataFeignApi.endingData(dpWmsDataDTO), FeignConstant.STECCN_DP_SERVICE_NAME).get();
        upHomemadeRawEndingList.forEach( adsWmsInventoryVO -> {
            wmsReportDataVO.setInventory(BigDecimalUtil.add(wmsReportDataVO.getInventory(),UnitConvertUtil.convertMaterial(materialConvertMap.get(adsWmsInventoryVO.getMaterialId()), unitPiece.getId(), unitSm.getId(), adsWmsInventoryVO.getTotalQuantity1(), 4)));
        });
        //本区间-库存
        DpWmsDataDTO dpDTO = new DpWmsDataDTO();
        dpDTO.setDate(endDate.plusDays(1));
        dpDTO.setMaterialIds(homemadeRawMaterialIds);
        List<AdsWmsInventoryVO> homemadeRawEnddingList = FeignOptional.of(() -> remoteDpWmsDataFeignApi.endingData(dpDTO), FeignConstant.STECCN_DP_SERVICE_NAME).get();
        homemadeRawEnddingList.forEach( adsWmsInventoryVO -> {
            wmsReportDataVO.setInventory1(BigDecimalUtil.add(wmsReportDataVO.getInventory1(),UnitConvertUtil.convertMaterial(materialConvertMap.get(adsWmsInventoryVO.getMaterialId()), unitPiece.getId(), unitSm.getId(), adsWmsInventoryVO.getTotalQuantity1(), 4)));
        });
        //增长率(本区间库存 - 上区间库存) / 上区间库存 * 100%
        if (wmsReportDataVO.getInventory().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal multiply = BigDecimalUtil.multiply(
                    BigDecimalUtil.divide(
                            BigDecimalUtil.subtract(wmsReportDataVO.getInventory1(), wmsReportDataVO.getInventory()),
                            wmsReportDataVO.getInventory()
                    ),
                    new BigDecimal("100") // 使用BigDecimal对象表示100%
            );
            DecimalFormat decimalFormat = new DecimalFormat("0.00%");
            wmsReportDataVO.setGrowthRate(decimalFormat.format(multiply));
        }
        return wmsReportDataVO;
    }

    private List<WmsOutInWarehouseInfoVO>  getInOutInfoDataVO( List<WmsDeliveryReceiptInfoVO> wmsDeliveryReceiptInfoVOList,
                                                               List<WmsStorageReceiptInfoVO> wmsStorageReceiptInfoVOList,
                                                               String type,
                                                               TupleDTO<String, Integer> materialDTO,
                                                               LocalDateTime  morningStartTime,
                                                               LocalDateTime  nightStartTime,
                                                               LocalDateTime  nightEndTime,
                                                               Integer sortFlag){
        List<WmsOutInWarehouseInfoVO> voList = Lists.newArrayList();
        List<EqpSpareVO> materialList = FeignOptional.of(() -> remoteSpareFeignApi.listByProperty(materialDTO), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        Map<Long, List<EqpSpareVO>> materialMap = materialList.stream().collect(Collectors.groupingBy(EqpSpareVO::getId));
        Set<Long> materialIds = materialList.stream().map(EqpSpareVO::getId).collect(Collectors.toSet());

        //白班出库数据
        Map<Long, List<WmsDeliveryReceiptInfoVO>> morningDeliveryMap = wmsDeliveryReceiptInfoVOList.stream()
                .filter(delivery ->
                        materialIds.contains(delivery.getMaterialId()) &&
                                (!delivery.getAffirmTime().isBefore(morningStartTime) && !delivery.getAffirmTime().isAfter(nightStartTime))
                )
                .collect(Collectors.groupingBy(WmsDeliveryReceiptInfoVO::getMaterialId));
        //夜班出库数据
        Map<Long, List<WmsDeliveryReceiptInfoVO>> nightDeliveryMap = wmsDeliveryReceiptInfoVOList.stream()
                .filter(delivery ->
                        materialIds.contains(delivery.getMaterialId()) &&
                                (!delivery.getAffirmTime().isBefore(nightStartTime) && !delivery.getAffirmTime().isAfter(nightEndTime))
                )
                .collect(Collectors.groupingBy(WmsDeliveryReceiptInfoVO::getMaterialId));
        //白班入库数据
        Map<Long, List<WmsStorageReceiptInfoVO>> morningStorageMap = wmsStorageReceiptInfoVOList.stream().filter(storage ->
                        materialIds.contains(storage.getMaterialId()) && (!storage.getAffirmTime().isBefore(morningStartTime) && !storage.getAffirmTime().isAfter(nightStartTime))
                )
                .collect(Collectors.groupingBy(WmsStorageReceiptInfoVO::getMaterialId));
        //夜班入库数据
        Map<Long, List<WmsStorageReceiptInfoVO>> nightStorageMap = wmsStorageReceiptInfoVOList.stream().filter(storage ->
                        materialIds.contains(storage.getMaterialId()) && (!storage.getAffirmTime().isBefore(nightStartTime) && !storage.getAffirmTime().isAfter(nightEndTime))
                )
                .collect(Collectors.groupingBy(WmsStorageReceiptInfoVO::getMaterialId));

        //单位换算
        SysDictVO unitPiece = FeignOptional.of(() -> remoteDictFeignApi.getByCode(PmManagerConstant.MEASURE_UNIT_PIECE, SysDictConstant.TYPE_MEASURE_UNIT), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        SysDictVO unitSm = FeignOptional.of(() -> remoteDictFeignApi.getByCode(PmManagerConstant.MEASURE_UNIT_SM, SysDictConstant.TYPE_MEASURE_UNIT), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        Map<Long, List<SysSpareUnitConversionVO>> materialConvertMap = FeignOptional.of(() -> remoteSpareUnitConvertFeignApi.convert(materialIds), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        materialList.forEach(material ->{
            List<EqpSpareVO> eqpSpareVOS = materialMap.get(material.getId());
            WmsOutInWarehouseInfoVO wmsOutInWarehouseInfoVO = new WmsOutInWarehouseInfoVO();
            wmsOutInWarehouseInfoVO.setType(type);
            wmsOutInWarehouseInfoVO.setUnit(PmManagerConstant.MEASURE_UNIT_SM);
            wmsOutInWarehouseInfoVO.setThick(eqpSpareVOS.get(0).getThickQuantity());
            wmsOutInWarehouseInfoVO.setWidth(eqpSpareVOS.get(0).getWeightQuantity());
            wmsOutInWarehouseInfoVO.setLength(eqpSpareVOS.get(0).getLengthQuantity());
            wmsOutInWarehouseInfoVO.setMaterialCode(material.getCode());
            wmsOutInWarehouseInfoVO.setSortFlag(sortFlag);

            WmsOutInWarehouseInfoVO wmsOutInWarehouseInfoPieceVO = new WmsOutInWarehouseInfoVO();
            wmsOutInWarehouseInfoPieceVO.setType(type);
            wmsOutInWarehouseInfoPieceVO.setUnit(PmManagerConstant.MEASURE_UNIT_PIECE);
            wmsOutInWarehouseInfoPieceVO.setThick(eqpSpareVOS.get(0).getThickQuantity());
            wmsOutInWarehouseInfoPieceVO.setWidth(eqpSpareVOS.get(0).getWeightQuantity());
            wmsOutInWarehouseInfoPieceVO.setLength(eqpSpareVOS.get(0).getLengthQuantity());
            wmsOutInWarehouseInfoPieceVO.setMaterialCode(material.getCode());
            wmsOutInWarehouseInfoPieceVO.setSortFlag(sortFlag);

            // 白班出库
            List<WmsDeliveryReceiptInfoVO> morningDeliverInfos = morningDeliveryMap.get(material.getId());
            if (CollUtil.isNotEmpty(morningDeliverInfos)) {
                morningDeliverInfos.forEach(delivery -> {
                    wmsOutInWarehouseInfoVO.setMorningShiftOutbound(BigDecimalUtil.add(wmsOutInWarehouseInfoVO.getMorningShiftOutbound(), UnitConvertUtil.convertMaterial(materialConvertMap.get(delivery.getMaterialId()), unitPiece.getId(), unitSm.getId(), delivery.getQuantity(), 4)));
                    wmsOutInWarehouseInfoPieceVO.setMorningShiftOutbound(BigDecimalUtil.add(wmsOutInWarehouseInfoPieceVO.getMorningShiftOutbound(), delivery.getQuantity()));
                });
            }
            // 夜班出库
            List<WmsDeliveryReceiptInfoVO> nightDeliveryInfos = nightDeliveryMap.get(material.getId());
            if (CollUtil.isNotEmpty(nightDeliveryInfos)) {
                nightDeliveryInfos.forEach(delivery -> {
                    wmsOutInWarehouseInfoVO.setNightShiftOutbound(BigDecimalUtil.add(wmsOutInWarehouseInfoVO.getNightShiftOutbound(), UnitConvertUtil.convertMaterial(materialConvertMap.get(delivery.getMaterialId()), unitPiece.getId(), unitSm.getId(), delivery.getQuantity(), 4)));
                    wmsOutInWarehouseInfoPieceVO.setNightShiftOutbound(BigDecimalUtil.add(wmsOutInWarehouseInfoPieceVO.getNightShiftOutbound(), delivery.getQuantity()));
                });
            }
            // 白班入库
            List<WmsStorageReceiptInfoVO> morningStorageInfos = morningStorageMap.get(material.getId());
            if (CollUtil.isNotEmpty(morningStorageInfos)) {
                morningStorageInfos.forEach(storageInfo -> {

                    wmsOutInWarehouseInfoVO.setMorningShiftStorage(BigDecimalUtil.add(wmsOutInWarehouseInfoVO.getMorningShiftStorage(), UnitConvertUtil.convertMaterial(materialConvertMap.get(storageInfo.getMaterialId()), unitPiece.getId(), unitSm.getId(), storageInfo.getQuantity(), 4)));
                    wmsOutInWarehouseInfoPieceVO.setMorningShiftStorage(BigDecimalUtil.add(wmsOutInWarehouseInfoPieceVO.getMorningShiftStorage(), storageInfo.getQuantity()));
                });
            }
            // 夜班入库
            List<WmsStorageReceiptInfoVO> nightStorageInfos = nightStorageMap.get(material.getType());
            if (CollUtil.isNotEmpty(nightStorageInfos)) {
                nightStorageInfos.forEach(storageInfo -> {
                    wmsOutInWarehouseInfoVO.setNightShiftStorage(BigDecimalUtil.add(wmsOutInWarehouseInfoVO.getNightShiftStorage(), UnitConvertUtil.convertMaterial(materialConvertMap.get(storageInfo.getMaterialId()), unitPiece.getId(), unitSm.getId(), storageInfo.getQuantity(), 4)));
                    wmsOutInWarehouseInfoPieceVO.setNightShiftStorage(BigDecimalUtil.add(wmsOutInWarehouseInfoPieceVO.getNightShiftStorage(), storageInfo.getQuantity()));
                });
            }
            //入库总数
            wmsOutInWarehouseInfoVO.setTotalInboundQuantity(BigDecimalUtil.add(wmsOutInWarehouseInfoVO.getMorningShiftStorage(),wmsOutInWarehouseInfoVO.getNightShiftStorage()));
            //出库总数
            wmsOutInWarehouseInfoVO.setTotalOutboundQuantity(BigDecimalUtil.add(wmsOutInWarehouseInfoVO.getMorningShiftOutbound(),wmsOutInWarehouseInfoVO.getNightShiftOutbound()));
            if (Objects.nonNull(wmsOutInWarehouseInfoVO.getMorningShiftOutbound()) || Objects.nonNull(wmsOutInWarehouseInfoVO.getMorningShiftStorage())
                    || Objects.nonNull(wmsOutInWarehouseInfoVO.getNightShiftOutbound()) || Objects.nonNull(wmsOutInWarehouseInfoVO.getNightShiftStorage())) {
                voList.add(wmsOutInWarehouseInfoVO);
                voList.add(wmsOutInWarehouseInfoPieceVO);
            }
        });
        //合计
        if (CollUtil.isNotEmpty(voList)){
            //厚度合计
            Map<BigDecimal, List<WmsOutInWarehouseInfoVO>> collect = voList.stream().collect(Collectors.groupingBy(WmsOutInWarehouseInfoVO::getThick));
            collect.forEach((k,v)->{
                Map<String, List<WmsOutInWarehouseInfoVO>> unitMap = v.stream().collect(Collectors.groupingBy(WmsOutInWarehouseInfoVO::getUnit));
                unitMap.forEach((unitKey,unitValue)->{
                    WmsOutInWarehouseInfoVO wmsOutInWarehouseInfoVO = new WmsOutInWarehouseInfoVO();
                    wmsOutInWarehouseInfoVO.setType(type);
                    wmsOutInWarehouseInfoVO.setType1(1);
                    wmsOutInWarehouseInfoVO.setTotalFlag(1);
                    wmsOutInWarehouseInfoVO.setUnit(unitKey);
                    wmsOutInWarehouseInfoVO.setThick(k);
                    wmsOutInWarehouseInfoVO.setSortFlag(sortFlag);
                    unitValue.forEach(info->{
                        wmsOutInWarehouseInfoVO.setMorningShiftStorage(BigDecimalUtil.add(wmsOutInWarehouseInfoVO.getMorningShiftStorage(),info.getMorningShiftStorage()));
                        wmsOutInWarehouseInfoVO.setMorningShiftOutbound(BigDecimalUtil.add(wmsOutInWarehouseInfoVO.getMorningShiftOutbound(),info.getMorningShiftOutbound()));
                        wmsOutInWarehouseInfoVO.setNightShiftStorage(BigDecimalUtil.add(wmsOutInWarehouseInfoVO.getNightShiftStorage(),info.getNightShiftStorage()));
                        wmsOutInWarehouseInfoVO.setNightShiftOutbound(BigDecimalUtil.add(wmsOutInWarehouseInfoVO.getNightShiftOutbound(),info.getNightShiftOutbound()));
                        wmsOutInWarehouseInfoVO.setTotalInboundQuantity(BigDecimalUtil.add(wmsOutInWarehouseInfoVO.getTotalInboundQuantity(),info.getTotalInboundQuantity()));
                        wmsOutInWarehouseInfoVO.setTotalOutboundQuantity(BigDecimalUtil.add(wmsOutInWarehouseInfoVO.getTotalOutboundQuantity(),info.getTotalOutboundQuantity()));
                    });
                    voList.add(wmsOutInWarehouseInfoVO);
                });
            });
            //小分类合并
            Map<String, List<WmsOutInWarehouseInfoVO>> map = voList.stream().filter(e ->e.getTotalFlag() == 0).collect(Collectors.toList()).stream().collect(Collectors.groupingBy(WmsOutInWarehouseInfoVO::getUnit));
            map.forEach((k,v)->{
                WmsOutInWarehouseInfoVO wmsOutInWarehouseInfoVO = new WmsOutInWarehouseInfoVO();
                wmsOutInWarehouseInfoVO.setType(type);
                wmsOutInWarehouseInfoVO.setType1(2);
                wmsOutInWarehouseInfoVO.setTotalFlag(1);
                wmsOutInWarehouseInfoVO.setUnit(k);
                wmsOutInWarehouseInfoVO.setSortFlag(sortFlag);
                v.forEach(info->{
                    wmsOutInWarehouseInfoVO.setMorningShiftStorage(BigDecimalUtil.add(wmsOutInWarehouseInfoVO.getMorningShiftStorage(),info.getMorningShiftStorage()));
                    wmsOutInWarehouseInfoVO.setMorningShiftOutbound(BigDecimalUtil.add(wmsOutInWarehouseInfoVO.getMorningShiftOutbound(),info.getMorningShiftOutbound()));
                    wmsOutInWarehouseInfoVO.setNightShiftStorage(BigDecimalUtil.add(wmsOutInWarehouseInfoVO.getNightShiftStorage(),info.getNightShiftStorage()));
                    wmsOutInWarehouseInfoVO.setNightShiftOutbound(BigDecimalUtil.add(wmsOutInWarehouseInfoVO.getNightShiftOutbound(),info.getNightShiftOutbound()));
                    wmsOutInWarehouseInfoVO.setTotalInboundQuantity(BigDecimalUtil.add(wmsOutInWarehouseInfoVO.getTotalInboundQuantity(),info.getTotalInboundQuantity()));
                    wmsOutInWarehouseInfoVO.setTotalOutboundQuantity(BigDecimalUtil.add(wmsOutInWarehouseInfoVO.getTotalOutboundQuantity(),info.getTotalOutboundQuantity()));
                });
                voList.add(wmsOutInWarehouseInfoVO);
            });
        }
        return voList;
    }
}
