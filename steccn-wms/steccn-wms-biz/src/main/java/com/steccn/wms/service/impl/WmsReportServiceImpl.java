package com.steccn.wms.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.common.constant.FeignConstant;
import com.steccn.common.constant.SysDictConstant;
import com.steccn.common.dto.SteccnTableColumn;
import com.steccn.common.dto.SteccnTableDataDTO;
import com.steccn.common.dto.TupleDTO;
import com.steccn.common.util.BigDecimalUtil;
import com.steccn.common.util.LocalDateUtil;
import com.steccn.dp.dto.DpWmsDataDTO;
import com.steccn.dp.feign.RemoteDpWmsDataFeignApi;
import com.steccn.dp.vo.AdsWmsInventoryVO;
import com.steccn.platform.feign.RemoteDictFeignApi;
import com.steccn.platform.feign.RemoteSpareFeignApi;
import com.steccn.platform.feign.RemoteSpareUnitConvertFeignApi;
import com.steccn.platform.vo.EqpSpareVO;
import com.steccn.platform.vo.SysDictVO;
import com.steccn.platform.vo.SysSpareUnitConversionVO;
import com.steccn.pms.constant.PmManagerConstant;
import com.steccn.pms.util.UnitConvertUtil;
import com.steccn.wms.domain.WmsDeliveryReceiptInfo;
import com.steccn.wms.domain.WmsStorageReceiptInfo;
import com.steccn.wms.dto.WmsReportStatisticalDTO;
import com.steccn.wms.feign.RemoteWmsInventoryFeignApi;
import com.steccn.wms.service.WmsDeliveryReceiptService;
import com.steccn.wms.service.WmsReportService;
import com.steccn.wms.service.WmsStorageReceiptService;
import com.steccn.wms.vo.WmsReportDataVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.Lists;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


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
        upperInterval.setLabel("上区间（"+formattedDate(tupleDTO.getField1())+" ~ "+formattedDate(tupleDTO.getField2())+")");
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
        homeInterval.setLabel("本区间（"+formattedDate(startTime)+" ~ "+formattedDate(endTime)+")");
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
}
