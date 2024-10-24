package com.steccn.pms.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.authserver.util.SteccnTableAssertUtil;
import com.steccn.common.constant.FeignConstant;
import com.steccn.common.constant.SysDictConstant;
import com.steccn.common.dto.QtLineChartBarDTO;
import com.steccn.common.dto.StackGraphDTO;
import com.steccn.common.dto.TupleDTO;
import com.steccn.common.util.*;
import com.steccn.dp.dto.DpBusiMonitorDTO;
import com.steccn.dp.feign.RemoteBusiMonitorFeignApi;
import com.steccn.platform.constant.SysMsgConstant;
import com.steccn.platform.constant.SysOfficeConstant;
import com.steccn.platform.dto.SysMsgSendDTO;
import com.steccn.platform.dto.SysOfficeParentDTO;
import com.steccn.platform.feign.*;
import com.steccn.platform.vo.SysDictVO;
import com.steccn.platform.vo.SysOfficeVO;
import com.steccn.platform.vo.SysSpareUnitConversionVO;
import com.steccn.platform.vo.SysStationWarehouseVO;
import com.steccn.pms.constant.PmManagerConstant;
import com.steccn.pms.domain.PmsPullForce;
import com.steccn.pms.service.PmsOfficeManager;
import com.steccn.pms.service.PmsReportWorkService;
import com.steccn.pms.service.PmsSjgReportDashboardService;
import com.steccn.pms.util.UnitConvertUtil;
import com.steccn.websocket.constant.WebsocketConstant;
import com.steccn.wms.feign.RemoteWmsTransferSlipFeignApi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.Lists;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author yequan
 * @desc 深加工车间当日看板服务
 * @date 2023/9/12
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class PmsSjgReportDashboardServiceImpl implements PmsSjgReportDashboardService {

    private final RemoteOfficeFeign remoteOfficeFeign;
    private final RemoteStationWarehouseFeignApi stationWarehouseFeignApi;
    private final RemoteWmsTransferSlipFeignApi wmsTransferSlipFeignApi;
    private final PmsReportWorkService pmsReportWorkService;
    private final RemoteSpareUnitConvertFeignApi spareUnitConvertFeignApi;
    private final RemoteDictFeignApi remoteDictFeignApi;
    private final PmsOfficeManager pmsOfficeManager;
    private final RemoteBusiMonitorFeignApi remoteBusiMonitorFeignApi;
    private final RemoteMsgFeignApi remoteMsgFeignApi;

    @Override
    public BigDecimal slip(Integer workShopId, LocalDate date) {
        Set<Integer> officeIdSet = Sets.newHashSet(workShopId);
        //根据车间获取产线
        SysOfficeParentDTO lineParentDto = new SysOfficeParentDTO();
        lineParentDto.setId(workShopId);
        lineParentDto.setOfficeType(SysOfficeConstant.PRODUCT_LINE);
        List<SysOfficeVO> lineList = FeignOptional.of(() -> remoteOfficeFeign.listByParentId(lineParentDto), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        if (CollUtil.isNotEmpty(lineList)) {
            Set<Integer> lineIdSet = lineList.stream().map(SysOfficeVO::getId).collect(Collectors.toSet());
            officeIdSet.addAll(lineIdSet);
            //根据产线获取工位
            SysOfficeParentDTO stationParentDto = new SysOfficeParentDTO();
            stationParentDto.setParentIds(lineIdSet);
            stationParentDto.setOfficeType(SysOfficeConstant.STATION);
            List<SysOfficeVO> stationList = FeignOptional.of(() -> remoteOfficeFeign.listByParentIdAndType(stationParentDto), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            Set<Integer> stationIdSet = stationList.stream().map(SysOfficeVO::getId).collect(Collectors.toSet());
            officeIdSet.addAll(stationIdSet);
        }
        Map<Integer, List<SysStationWarehouseVO>> lineWarehouseMap = FeignOptional.of(() -> stationWarehouseFeignApi.listByStationIds(officeIdSet), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        SteccnTableAssertUtil.notEmpty(lineWarehouseMap, "车间暂未绑定线边仓");
        //获取线边仓货架
        Set<Integer> shelfSet = Sets.newHashSet();
        lineWarehouseMap.forEach((k, v) -> {
            v.forEach(ele -> {
                shelfSet.add(ele.getShelfId());
            });
        });
        //根据线边仓去调拨单中获取今日的原片数量之和
        BigDecimal sum = FeignOptional.of(() -> wmsTransferSlipFeignApi.slipLineWarehouse(new TupleDTO<>(date, shelfSet)), FeignConstant.STECCN_WMS_SERVICE_NAME).get();
        return BigDecimalUtil.setScale(sum, UnitLengthUtil.UNIT_PIECE_LENGTH);
    }

    @Override
    public BigDecimal consume(Integer workShopId, LocalDate date, String unit, Integer unitLength) {
        //获取产线id
        List<SysOfficeVO> wsLineList = pmsOfficeManager.getWsLineList(workShopId);
        SteccnTableAssertUtil.notEmpty(wsLineList, "车间下暂无产线");
        List<Integer> lineIdList = wsLineList.stream().map(SysOfficeVO::getId).collect(Collectors.toList());
        Map<Integer, BigDecimal> consumeMap = pmsReportWorkService.getDateRangeUseCountUnit(lineIdList, date, date, unit);
        BigDecimal sum = BigDecimalUtil.sum(consumeMap.values());
        return BigDecimalUtil.setScale(sum, unitLength);
    }

    @Override
    public List<BigDecimal> produceNumToday(Integer workShopId, LocalDate date) {
        SysOfficeVO wsOffice = FeignOptional.of(() -> remoteOfficeFeign.sysOfficeInfo(workShopId), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        Assert.notNull(wsOffice, "车间不能为空");
        List<SysOfficeVO> wsLineList = pmsOfficeManager.getWsLineList(wsOffice.getId());
        Assert.notEmpty(wsLineList, "车间下暂无产线");
        List<Integer> lineIdList = GetValueUtil.collectionGet(wsLineList, x -> x.getId());
        int type = wsOffice.getOfficeName().indexOf("基片") != -1 ? 0 : 1;
        //根据车间区分深加工还是基片
        //①基片产量：所有报工加工数之和
        //②深加工产量:磨边工位报工加工数之和
        Map<Long, Map<Integer, BigDecimal>> todayPm = pmsReportWorkService.getTodayPm(lineIdList, date, type);
        //获取物料转换单位
        Map<Long, List<SysSpareUnitConversionVO>> convertMap = FeignOptional.of(() -> spareUnitConvertFeignApi.convert(todayPm.keySet()), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        //获取/T/片/㎡
        SysDictVO sysDict1 = FeignOptional.of(() -> remoteDictFeignApi.getByCode(PmManagerConstant.MEASURE_UNIT_PIECE, SysDictConstant.TYPE_MEASURE_UNIT), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        SysDictVO sysDict2 = FeignOptional.of(() -> remoteDictFeignApi.getByCode(PmManagerConstant.MEASURE_UNIT_TON, SysDictConstant.TYPE_MEASURE_UNIT), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        SysDictVO sysDict3 = FeignOptional.of(() -> remoteDictFeignApi.getByCode(PmManagerConstant.MEASURE_UNIT_SM, SysDictConstant.TYPE_MEASURE_UNIT), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        Assert.notNull(sysDict1, "换算单位1不存在");
        Assert.notNull(sysDict2, "换算单位2不存在");
        Assert.notNull(sysDict3, "换算单位3不存在");
        BigDecimal sum1 = null;
        BigDecimal sum2 = null;
        BigDecimal sum3 = null;
        //先换算后求和
        for (Map.Entry<Long, Map<Integer, BigDecimal>> entry : todayPm.entrySet()) {
            BigDecimal value = BigDecimalUtil.sum(entry.getValue().values());
            BigDecimal convert1 = UnitConvertUtil.convert(convertMap, entry.getKey(), sysDict1.getId(), sysDict2.getId(), value);
            BigDecimal convert2 = UnitConvertUtil.convert(convertMap, entry.getKey(), sysDict1.getId(), sysDict3.getId(), value);
            sum1 = BigDecimalUtil.add(sum1, value);
            sum2 = BigDecimalUtil.add(sum2, convert1);
            sum3 = BigDecimalUtil.add(sum3, convert2);
        }
        return Arrays.asList(BigDecimalUtil.setScale(sum2, UnitLengthUtil.UNIT_TON_LENGTH), BigDecimalUtil.setScale(sum1, UnitLengthUtil.UNIT_PIECE_LENGTH), BigDecimalUtil.setScale(sum3, UnitLengthUtil.UNIT_SQUARE_METER_LENGTH));
    }

    @Override
    public BigDecimal produceNumRate(Integer workShopId, LocalDate date) {
        BigDecimal num = produceNumToday(workShopId, date).get(0);
        BigDecimal consume = consume(workShopId, date, PmManagerConstant.MEASURE_UNIT_TON, UnitLengthUtil.UNIT_TON_LENGTH);
        return BigDecimalUtil.percent(num, consume);
    }

    @Override
    public List<BigDecimal> inStorageToday(Integer workShopId, LocalDate date) {
        SysOfficeVO wsOffice = FeignOptional.of(() -> remoteOfficeFeign.sysOfficeInfo(workShopId), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        Assert.notNull(wsOffice, "车间不能为空");
        List<SysOfficeVO> wsLineList = pmsOfficeManager.getWsLineList(wsOffice.getId());
        Assert.notEmpty(wsLineList, "车间下暂无产线");
        List<Integer> lineIdList = GetValueUtil.collectionGet(wsLineList, x -> x.getId());
        Map<Long, Map<Integer, BigDecimal>> inStorageMap = pmsReportWorkService.getTodayInStorage(lineIdList, date);
        //获取物料转换单位
        Map<Long, List<SysSpareUnitConversionVO>> convertMap = FeignOptional.of(() -> spareUnitConvertFeignApi.convert(inStorageMap.keySet()), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        //获取/T/片/㎡
        SysDictVO sysDict1 = FeignOptional.of(() -> remoteDictFeignApi.getByCode(PmManagerConstant.MEASURE_UNIT_PIECE, SysDictConstant.TYPE_MEASURE_UNIT), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        SysDictVO sysDict2 = FeignOptional.of(() -> remoteDictFeignApi.getByCode(PmManagerConstant.MEASURE_UNIT_TON, SysDictConstant.TYPE_MEASURE_UNIT), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        SysDictVO sysDict3 = FeignOptional.of(() -> remoteDictFeignApi.getByCode(PmManagerConstant.MEASURE_UNIT_SM, SysDictConstant.TYPE_MEASURE_UNIT), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        Assert.notNull(sysDict1, "换算单位1不存在");
        Assert.notNull(sysDict2, "换算单位2不存在");
        Assert.notNull(sysDict3, "换算单位3不存在");
        BigDecimal sum1 = null;
        BigDecimal sum2 = null;
        BigDecimal sum3 = null;
        //先换算后求和
        for (Map.Entry<Long, Map<Integer, BigDecimal>> entry : inStorageMap.entrySet()) {
            BigDecimal value = BigDecimalUtil.sum(entry.getValue().values());
            BigDecimal convert1 = UnitConvertUtil.convert(convertMap, entry.getKey(), sysDict1.getId(), sysDict2.getId(), value);
            BigDecimal convert2 = UnitConvertUtil.convert(convertMap, entry.getKey(), sysDict1.getId(), sysDict3.getId(), value);
            sum1 = BigDecimalUtil.add(sum1, value);
            sum2 = BigDecimalUtil.add(sum2, convert1);
            sum3 = BigDecimalUtil.add(sum3, convert2);
        }
        return Arrays.asList(BigDecimalUtil.setScale(sum2, UnitLengthUtil.UNIT_TON_LENGTH), BigDecimalUtil.setScale(sum1, UnitLengthUtil.UNIT_PIECE_LENGTH), BigDecimalUtil.setScale(sum3, UnitLengthUtil.UNIT_SQUARE_METER_LENGTH));
    }

    @Override
    public BigDecimal inStorageRate(Integer workShopId, LocalDate date) {
        BigDecimal num = inStorageToday(workShopId, date).get(0);
        BigDecimal consume = consume(workShopId, date, PmManagerConstant.MEASURE_UNIT_TON, UnitLengthUtil.UNIT_TON_LENGTH);
        return BigDecimalUtil.percent(num, consume, 2);
    }

    @Override
    public QtLineChartBarDTO lineUseCount(Integer workShopId, LocalDate date) {
        Map<Integer, SysOfficeVO> sysOfficeMap = FeignOptional.of(() -> remoteOfficeFeign.listById(Sets.newHashSet(workShopId)), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        Assert.notEmpty(sysOfficeMap, "车间不能为空");
        Map<Integer, SysOfficeVO> wsLineMap = getWsLineMap(sysOfficeMap.get(workShopId));
        Assert.notEmpty(wsLineMap, "车间下暂无产线");
        //获取班次耗用量
        Map<String, BigDecimal> pfMap = pmsReportWorkService.lineClassUseCount(wsLineMap.keySet(), date, date, PmManagerConstant.MEASURE_UNIT_TON);
        SteccnTableAssertUtil.notEmpty(pfMap, "耗用量不存在");
        //获取早晚班
        SysDictVO morningDict = FeignOptional.of(() -> remoteDictFeignApi.getByCode(PmManagerConstant.NUM_0, PmManagerConstant.SYS_SHIFT), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        SysDictVO nightDict = FeignOptional.of(() -> remoteDictFeignApi.getByCode(PmManagerConstant.NUM_1, PmManagerConstant.SYS_SHIFT), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        Assert.notNull(morningDict, "早班数据字典不存在");
        Assert.notNull(nightDict, "晚班数据字典不存在");
        List<StackGraphDTO<String, BigDecimal>> dataList = Lists.newArrayList();
        List<PmsPullForce> list = Lists.newArrayList();
        //对line排序
        List<SysOfficeVO> sortLineList = wsLineMap.values().stream().sorted(Comparator.comparing(SysOfficeVO::getTreeSort)).collect(Collectors.toList());
        Map<Integer, List<PmsPullForce>> lineMap = list.stream().collect(Collectors.groupingBy(PmsPullForce::getProductLineId));
        Map<Integer, List<PmsPullForce>> objMap = Maps.newLinkedHashMap();
        sortLineList.forEach(ele -> objMap.put(ele.getId(), lineMap.getOrDefault(ele.getId(), Lists.newArrayList())));

        for (SysOfficeVO line : sortLineList) {
            StackGraphDTO<String, BigDecimal> stackGraphDTO = new StackGraphDTO<>();
            stackGraphDTO.setXAxis(line.getOfficeName());
            List<TupleDTO<String, BigDecimal>> datList = Lists.newArrayList();
            BigDecimal morning = pfMap.get(line.getId() + "_" + morningDict.getId());
            BigDecimal night = pfMap.get(line.getId() + "_" + nightDict.getId());
            datList.add(new TupleDTO<>("产线总耗用量", BigDecimalUtil.add(morning, night)));
            datList.add(new TupleDTO<>("白班耗用量", morning));
            datList.add(new TupleDTO<>("夜班耗用量", night));
            stackGraphDTO.setData(datList);
            dataList.add(stackGraphDTO);
        }
        return StackGraphUtil.convertData(dataList, Boolean.TRUE);
    }

    @Override
    public QtLineChartBarDTO lineComplete(Integer workShopId, LocalDate date) {
        Map<Integer, SysOfficeVO> sysOfficeMap = FeignOptional.of(() -> remoteOfficeFeign.listById(Sets.newHashSet(workShopId)), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        Assert.notEmpty(sysOfficeMap, "车间不能为空");
        Map<Integer, SysOfficeVO> wsLineMap = getWsLineMap(sysOfficeMap.get(workShopId));
        Assert.notEmpty(wsLineMap, "车间下暂无产线");
        //基片深加工
        SysOfficeVO wsOffice = sysOfficeMap.get(workShopId);
        int type = wsOffice.getOfficeName().indexOf("基片") != -1 ? 0 : 1;

        //获取班次产量
        Map<String, BigDecimal> productNumMap = pmsReportWorkService.lineClassPm(wsLineMap.keySet(), date, date, type, PmManagerConstant.MEASURE_UNIT_TON);
        SteccnTableAssertUtil.notEmpty(productNumMap, "暂无数据");
        Map<String, BigDecimal> pfMap = pmsReportWorkService.lineClassUseCount(wsLineMap.keySet(), date, date, PmManagerConstant.MEASURE_UNIT_TON);
        //获取早晚班
        SysDictVO morningDict = FeignOptional.of(() -> remoteDictFeignApi.getByCode(PmManagerConstant.NUM_0, PmManagerConstant.SYS_SHIFT), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        SysDictVO nightDict = FeignOptional.of(() -> remoteDictFeignApi.getByCode(PmManagerConstant.NUM_1, PmManagerConstant.SYS_SHIFT), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        Assert.notNull(morningDict, "早班数据字典不存在");
        Assert.notNull(nightDict, "晚班数据字典不存在");
        List<StackGraphDTO<String, BigDecimal>> dataList = Lists.newArrayList();
        List<SysOfficeVO> sortLineList = wsLineMap.values().stream().sorted(Comparator.comparing(SysOfficeVO::getTreeSort)).collect(Collectors.toList());
        //根据产量map获取合格率
        Map<Integer, List<BigDecimal>> valueMap = Maps.newHashMap();
        sortLineList.forEach(x -> {
            Integer ele = x.getId();
            BigDecimal morning = productNumMap.get(ele + "_" + morningDict.getId());
            BigDecimal night = productNumMap.get(ele + "_" + nightDict.getId());
            BigDecimal sum = BigDecimalUtil.add(morning, night);
            MapUtil.compute(valueMap, ele, sum);
            MapUtil.compute(valueMap, ele, morning);
            MapUtil.compute(valueMap, ele, night);
            BigDecimal pfMorning = pfMap.get(ele + "_" + morningDict.getId());
            BigDecimal pfNight = pfMap.get(ele + "_" + nightDict.getId());
            BigDecimal pfSum = BigDecimalUtil.add(pfMorning, pfNight);
            MapUtil.compute(valueMap, ele, BigDecimalUtil.percent(sum, pfSum, 2));
            MapUtil.compute(valueMap, ele, BigDecimalUtil.percent(morning, pfMorning, 2));
            MapUtil.compute(valueMap, ele, BigDecimalUtil.percent(night, pfNight, 2));
        });
        sortLineList.forEach(ele -> {
            String officeName = wsLineMap.get(ele.getId()).getOfficeName();
            StackGraphDTO<String, BigDecimal> stackGraphDTO = new StackGraphDTO<>();
            stackGraphDTO.setXAxis(officeName);
            List<TupleDTO<String, BigDecimal>> datList = Lists.newArrayList();
            datList.add(new TupleDTO<>("产线总产量", valueMap.get(ele.getId()).get(0)));
            datList.add(new TupleDTO<>("白班总产量", valueMap.get(ele.getId()).get(1)));
            datList.add(new TupleDTO<>("夜班总产量", valueMap.get(ele.getId()).get(2)));
            datList.add(new TupleDTO<>("产线产量合格率", valueMap.get(ele.getId()).get(3)));
            datList.add(new TupleDTO<>("白班产量合格率", valueMap.get(ele.getId()).get(4)));
            datList.add(new TupleDTO<>("夜班产量合格率", valueMap.get(ele.getId()).get(5)));
            stackGraphDTO.setData(datList);
            dataList.add(stackGraphDTO);
        });
        return StackGraphUtil.convertData(dataList, Boolean.TRUE);
    }

    @Override
    public void lineCompleteMonitor(LocalDate date, String alarmCode) {
        //根据告警code获取
        List<DpBusiMonitorDTO> passRateList = FeignOptional.of(() -> remoteBusiMonitorFeignApi.getBusiMonitorAlarm(alarmCode), FeignConstant.STECCN_DP_SERVICE_NAME).get();
        if(CollUtil.isEmpty(passRateList)) {
            return ;
        }
        //获取深加工一车间,深加工二车间（东线）,深加工三车间（西线）
        Map<String, SysOfficeVO> sysOfficeMap = FeignOptional.of(() -> remoteOfficeFeign.listByCodeSet(Sets.newHashSet("STEC002", "STEC003", "STEC015")), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        Assert.notEmpty(sysOfficeMap, "车间不能为空");
        //获取两个车间的所有产线
        Map<Integer, SysOfficeVO> wsLineMap = Maps.newHashMap();
        for (Map.Entry<String, SysOfficeVO> wsOffice : sysOfficeMap.entrySet()) {
            Map<Integer, SysOfficeVO> tmpLineMap = getWsLineMap(wsOffice.getValue());
            wsLineMap.putAll(tmpLineMap);
            Assert.notEmpty(wsLineMap, "车间下暂无产线");
        }
        int type = 1 ;
        //获取班次产量
        Map<String, BigDecimal> productNumMap = pmsReportWorkService.lineClassPm(wsLineMap.keySet(), date, date, type, PmManagerConstant.MEASURE_UNIT_TON);
        SteccnTableAssertUtil.notEmpty(productNumMap, "暂无数据");
        Map<String, BigDecimal> pfMap = pmsReportWorkService.lineClassUseCount(wsLineMap.keySet(), date, date, PmManagerConstant.MEASURE_UNIT_TON);
        //获取早晚班
        SysDictVO morningDict = FeignOptional.of(() -> remoteDictFeignApi.getByCode(PmManagerConstant.NUM_0, PmManagerConstant.SYS_SHIFT), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        SysDictVO nightDict = FeignOptional.of(() -> remoteDictFeignApi.getByCode(PmManagerConstant.NUM_1, PmManagerConstant.SYS_SHIFT), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        Assert.notNull(morningDict, "早班数据字典不存在");
        Assert.notNull(nightDict, "晚班数据字典不存在");
        List<SysOfficeVO> sortLineList = wsLineMap.values().stream().sorted(Comparator.comparing(SysOfficeVO::getTreeSort)).collect(Collectors.toList());
        //根据产量map获取合格率
        Map<Integer,BigDecimal> valueMap = Maps.newHashMap();
        sortLineList.forEach(x -> {
            Integer ele = x.getId();
            BigDecimal morning = productNumMap.get(ele + "_" + morningDict.getId());
            BigDecimal night = productNumMap.get(ele + "_" + nightDict.getId());
            BigDecimal sum = BigDecimalUtil.add(morning, night);
            BigDecimal pfMorning = pfMap.get(ele + "_" + morningDict.getId());
            BigDecimal pfNight = pfMap.get(ele + "_" + nightDict.getId());
            BigDecimal pfSum = BigDecimalUtil.add(pfMorning, pfNight);
            valueMap.put(ele, BigDecimalUtil.percent(sum, pfSum, 2));
        });
        //获取三个车间的合格率
        sysOfficeMap.forEach((k,v) ->{
            BigDecimal wsPm = produceNumRate(v.getId(), date);
            valueMap.put(v.getId(),wsPm);
        });
        for (DpBusiMonitorDTO monitor : passRateList) {
            if(CollUtil.isNotEmpty(monitor.getMonitorRange())){
                StringBuilder contentSb = new StringBuilder();
                BigDecimal upValue = monitor.getUpValue();
                BigDecimal lowValue = monitor.getLowValue();
                Map<Integer, SysOfficeVO> officeIdMap = FeignOptional.of(() -> remoteOfficeFeign.listById(Sets.newHashSet(monitor.getMonitorRange())), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
                for (SysOfficeVO office : officeIdMap.values()) {
                    //第一种情况，是车间集合
                    //第二种情况，是产线集合告警
                    //第三种情况，是车间+产线告警
                    BigDecimal currentPmRate = valueMap.get(office.getId());
                    //高于上限
                    String template = SysMsgConstant.MsgTypeEnum.PRODUCT_PASS_RATE_ALARM.getTemplate();
                    if (Objects.nonNull(currentPmRate) && Objects.nonNull(upValue) && BigDecimalUtil.gt(currentPmRate, upValue)) {
                        String ts1 = String.format(template, LocalDateUtil.format(date, "yyyy-MM-dd"),office.getOfficeName(), BigDecimalUtil.format(currentPmRate), "高", upValue);
                        contentSb.append(ts1).append("\n");
                    } else if (Objects.nonNull(currentPmRate) && Objects.nonNull(lowValue) && BigDecimalUtil.lt(currentPmRate, lowValue)) {
                        String ts3 = String.format(template, LocalDateUtil.format(date, "yyyy-MM-dd"),office.getOfficeName(),  BigDecimalUtil.format(currentPmRate), "低", lowValue);
                        contentSb.append(ts3).append("\n");
                    }
                }
                if (contentSb.length() > 0) {
                    Set<Long> notifyMan = monitor.getNotifyMan();
                    SysMsgSendDTO sysMsgDTO = new SysMsgSendDTO();
                    sysMsgDTO.setType(SysMsgConstant.MsgTypeEnum.PRODUCT_PASS_RATE_ALARM.getCode());
                    sysMsgDTO.setTitle(SysMsgConstant.MsgTypeEnum.PRODUCT_PASS_RATE_ALARM.getTitle());
                    sysMsgDTO.setWarnFlag(SysMsgConstant.MsgTypeEnum.PRODUCT_PASS_RATE_ALARM.getWarnFlag());
                    sysMsgDTO.setUserList(notifyMan);
                    sysMsgDTO.setContent(contentSb.substring(0, contentSb.length() - 1));
                    sysMsgDTO.setPushType(WebsocketConstant.WsPushTypeEnum.MSG_SYS_REMIND.getType());
                    Boolean aBoolean = FeignOptional.of(() -> remoteMsgFeignApi.sendMsg(sysMsgDTO), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
                    log.info("lineCompleteMonitor方法消息发送 -> {}", aBoolean);
                }
            }
        }
    }

    @Override
    public QtLineChartBarDTO lineInStorage(Integer workShopId, LocalDate date) {
        Map<Integer, SysOfficeVO> sysOfficeMap = FeignOptional.of(() -> remoteOfficeFeign.listById(Sets.newHashSet(workShopId)), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        Assert.notEmpty(sysOfficeMap, "车间不能为空");
        Map<Integer, SysOfficeVO> wsLineMap = getWsLineMap(sysOfficeMap.get(workShopId));
        Assert.notEmpty(wsLineMap, "车间下暂无产线");
        //获取班次产量
        Map<String, BigDecimal> inStorageMap = pmsReportWorkService.lineClassInStorage(wsLineMap.keySet(), date, date, PmManagerConstant.MEASURE_UNIT_TON);
        SteccnTableAssertUtil.notEmpty(inStorageMap, "暂无数据");
        Map<String, BigDecimal> pfMap = pmsReportWorkService.lineClassUseCount(wsLineMap.keySet(), date, date, PmManagerConstant.MEASURE_UNIT_TON);
        //获取早晚班
        SysDictVO morningDict = FeignOptional.of(() -> remoteDictFeignApi.getByCode(PmManagerConstant.NUM_0, PmManagerConstant.SYS_SHIFT), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        SysDictVO nightDict = FeignOptional.of(() -> remoteDictFeignApi.getByCode(PmManagerConstant.NUM_1, PmManagerConstant.SYS_SHIFT), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        Assert.notNull(morningDict, "早班数据字典不存在");
        Assert.notNull(nightDict, "晚班数据字典不存在");
        List<StackGraphDTO<String, BigDecimal>> dataList = Lists.newArrayList();
        List<SysOfficeVO> sortLineList = wsLineMap.values().stream().sorted(Comparator.comparing(SysOfficeVO::getTreeSort)).collect(Collectors.toList());
        //根据产量map获取合格率
        Map<Integer, List<BigDecimal>> valueMap = Maps.newHashMap();
        sortLineList.forEach(x -> {
            Integer ele = x.getId();
            BigDecimal morning = inStorageMap.get(ele + "_" + morningDict.getId());
            BigDecimal night = inStorageMap.get(ele + "_" + nightDict.getId());
            BigDecimal sum = BigDecimalUtil.add(morning, night);
            MapUtil.compute(valueMap, ele, sum);
            MapUtil.compute(valueMap, ele, morning);
            MapUtil.compute(valueMap, ele, night);
            BigDecimal pfMorning = pfMap.get(ele + "_" + morningDict.getId());
            BigDecimal pfNight = pfMap.get(ele + "_" + nightDict.getId());
            BigDecimal pfSum = BigDecimalUtil.add(pfMorning, pfNight);
            MapUtil.compute(valueMap, ele, BigDecimalUtil.percent(sum, pfSum));
            MapUtil.compute(valueMap, ele, BigDecimalUtil.percent(morning, pfMorning));
            MapUtil.compute(valueMap, ele, BigDecimalUtil.percent(night, pfNight));
        });
        sortLineList.forEach(ele -> {
            String officeName = wsLineMap.get(ele.getId()).getOfficeName();
            StackGraphDTO<String, BigDecimal> stackGraphDTO = new StackGraphDTO<>();
            stackGraphDTO.setXAxis(officeName);
            List<TupleDTO<String, BigDecimal>> datList = Lists.newArrayList();
            datList.add(new TupleDTO<>("产线总入库数", valueMap.get(ele.getId()).get(0)));
            datList.add(new TupleDTO<>("白班总入库数", valueMap.get(ele.getId()).get(1)));
            datList.add(new TupleDTO<>("夜班总入库数", valueMap.get(ele.getId()).get(2)));
            datList.add(new TupleDTO<>("产线入库合格率", valueMap.get(ele.getId()).get(3)));
            datList.add(new TupleDTO<>("白班入库合格率", valueMap.get(ele.getId()).get(4)));
            datList.add(new TupleDTO<>("夜班入库合格率", valueMap.get(ele.getId()).get(5)));
            stackGraphDTO.setData(datList);
            dataList.add(stackGraphDTO);
        });
        return StackGraphUtil.convertData(dataList, Boolean.TRUE);
    }

    @Override
    public void lineInStorageMonitor(LocalDate date, String alarmCode) {
        //根据告警code获取
        List<DpBusiMonitorDTO> passRateList = FeignOptional.of(() -> remoteBusiMonitorFeignApi.getBusiMonitorAlarm(alarmCode), FeignConstant.STECCN_DP_SERVICE_NAME).get();
        if(CollUtil.isEmpty(passRateList)) {
            return ;
        }
        //获取深加工一车间,深加工二车间（东线）,深加工三车间（西线）
        Map<String, SysOfficeVO> sysOfficeMap = FeignOptional.of(() -> remoteOfficeFeign.listByCodeSet(Sets.newHashSet("STEC002", "STEC003", "STEC015")), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        Assert.notEmpty(sysOfficeMap, "车间不能为空");
        //获取两个车间的所有产线
        Map<Integer, SysOfficeVO> wsLineMap = Maps.newHashMap();
        for (Map.Entry<String, SysOfficeVO> wsOffice : sysOfficeMap.entrySet()) {
            Map<Integer, SysOfficeVO> tmpLineMap = getWsLineMap(wsOffice.getValue());
            wsLineMap.putAll(tmpLineMap);
            Assert.notEmpty(wsLineMap, "车间下暂无产线");
        }
        //获取班次入库量
        Map<String, BigDecimal> inStorageMap = pmsReportWorkService.lineClassInStorage(wsLineMap.keySet(), date, date, PmManagerConstant.MEASURE_UNIT_TON);
        SteccnTableAssertUtil.notEmpty(inStorageMap, "暂无数据");
        Map<String, BigDecimal> pfMap = pmsReportWorkService.lineClassUseCount(wsLineMap.keySet(), date, date, PmManagerConstant.MEASURE_UNIT_TON);
        //获取早晚班
        SysDictVO morningDict = FeignOptional.of(() -> remoteDictFeignApi.getByCode(PmManagerConstant.NUM_0, PmManagerConstant.SYS_SHIFT), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        SysDictVO nightDict = FeignOptional.of(() -> remoteDictFeignApi.getByCode(PmManagerConstant.NUM_1, PmManagerConstant.SYS_SHIFT), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        Assert.notNull(morningDict, "早班数据字典不存在");
        Assert.notNull(nightDict, "晚班数据字典不存在");
        List<SysOfficeVO> sortLineList = wsLineMap.values().stream().sorted(Comparator.comparing(SysOfficeVO::getTreeSort)).collect(Collectors.toList());
        //根据产量map获取合格率
        Map<Integer,BigDecimal> valueMap = Maps.newHashMap();
        sortLineList.forEach(x -> {
            Integer ele = x.getId();
            BigDecimal morning = inStorageMap.get(ele + "_" + morningDict.getId());
            BigDecimal night = inStorageMap.get(ele + "_" + nightDict.getId());
            BigDecimal sum = BigDecimalUtil.add(morning, night);
            BigDecimal pfMorning = pfMap.get(ele + "_" + morningDict.getId());
            BigDecimal pfNight = pfMap.get(ele + "_" + nightDict.getId());
            BigDecimal pfSum = BigDecimalUtil.add(pfMorning, pfNight);
            valueMap.put(ele, BigDecimalUtil.percent(sum, pfSum, 2));
        });
        //获取两个车间的合格率
        sysOfficeMap.forEach((k,v) ->{
            BigDecimal wsInStorage = inStorageRate(v.getId(), date);
            valueMap.put(v.getId(),wsInStorage);
        });
        for (DpBusiMonitorDTO monitor : passRateList) {
            if(CollUtil.isNotEmpty(monitor.getMonitorRange())){
                StringBuilder contentSb = new StringBuilder();
                BigDecimal upValue = monitor.getUpValue();
                BigDecimal lowValue = monitor.getLowValue();
                Map<Integer, SysOfficeVO> officeIdMap = FeignOptional.of(() -> remoteOfficeFeign.listById(Sets.newHashSet(monitor.getMonitorRange())), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
                for (SysOfficeVO office : officeIdMap.values()) {
                    //第一种情况，是车间集合
                    //第二种情况，是产线集合告警
                    //第三种情况，是车间+产线告警
                    BigDecimal currentPmRate = valueMap.get(office.getId());
                    //高于上限
                    String template = SysMsgConstant.MsgTypeEnum.IN_STORAGE_RATE_ALARM.getTemplate();
                    if (Objects.nonNull(currentPmRate) && Objects.nonNull(upValue) && BigDecimalUtil.gt(currentPmRate, upValue)) {
                        String ts1 = String.format(template, LocalDateUtil.format(date, "yyyy-MM-dd"),office.getOfficeName(), BigDecimalUtil.format(currentPmRate), "高", upValue);
                        contentSb.append(ts1).append("\n");
                    } else if (Objects.nonNull(currentPmRate) && Objects.nonNull(lowValue) && BigDecimalUtil.lt(currentPmRate, lowValue)) {
                        String ts3 = String.format(template, LocalDateUtil.format(date, "yyyy-MM-dd"),office.getOfficeName(),  BigDecimalUtil.format(currentPmRate), "低", lowValue);
                        contentSb.append(ts3).append("\n");
                    }
                }
                if (contentSb.length() > 0) {
                    Set<Long> notifyMan = monitor.getNotifyMan();
                    SysMsgSendDTO sysMsgDTO = new SysMsgSendDTO();
                    sysMsgDTO.setType(SysMsgConstant.MsgTypeEnum.IN_STORAGE_RATE_ALARM.getCode());
                    sysMsgDTO.setTitle(SysMsgConstant.MsgTypeEnum.IN_STORAGE_RATE_ALARM.getTitle());
                    sysMsgDTO.setWarnFlag(SysMsgConstant.MsgTypeEnum.IN_STORAGE_RATE_ALARM.getWarnFlag());
                    sysMsgDTO.setUserList(notifyMan);
                    sysMsgDTO.setContent(contentSb.substring(0, contentSb.length() - 1));
                    sysMsgDTO.setPushType(WebsocketConstant.WsPushTypeEnum.MSG_SYS_REMIND.getType());
                    Boolean aBoolean = FeignOptional.of(() -> remoteMsgFeignApi.sendMsg(sysMsgDTO), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
                    log.info("lineInStorageMonitor方法消息发送 -> {}", aBoolean);
                }
            }
        }
    }

    /**
     * 获取车间的产线集合
     *
     * @param wsOffice
     * @return
     */
    private Map<Integer, SysOfficeVO> getWsLineMap(SysOfficeVO wsOffice) {
        //根据车间获取产线集合
        SysOfficeParentDTO sysOfficeParent = new SysOfficeParentDTO();
        sysOfficeParent.setId(wsOffice.getId());
        sysOfficeParent.setOfficeType(SysOfficeConstant.PRODUCT_LINE);
        List<SysOfficeVO> lineOfficeList = FeignOptional.of(() -> remoteOfficeFeign.listByParentId(sysOfficeParent), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        Assert.notEmpty(lineOfficeList, "车间下暂无产线");
        Map<Integer, SysOfficeVO> resMap = Maps.newHashMap();
        lineOfficeList.forEach(ele -> {
            resMap.put(ele.getId(), ele);

        });
        return resMap;
    }
}
