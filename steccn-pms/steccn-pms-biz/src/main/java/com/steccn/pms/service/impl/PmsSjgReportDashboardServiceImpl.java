package com.steccn.pms.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import com.google.common.collect.Sets;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.authserver.util.SteccnTableAssertUtil;
import com.steccn.common.constant.FeignConstant;
import com.steccn.common.constant.SysDictConstant;
import com.steccn.common.dto.QtLineChartBarDTO;
import com.steccn.common.dto.TupleDTO;
import com.steccn.common.util.BigDecimalUtil;
import com.steccn.common.util.LocalDateUtil;
import com.steccn.common.util.UnitLengthUtil;
import com.steccn.platform.constant.SysOfficeConstant;
import com.steccn.platform.dto.SysOfficeParentDTO;
import com.steccn.platform.feign.RemoteDictFeignApi;
import com.steccn.platform.feign.RemoteOfficeFeign;
import com.steccn.platform.feign.RemoteSpareUnitConvertFeignApi;
import com.steccn.platform.feign.RemoteStationWarehouseFeignApi;
import com.steccn.platform.vo.SysDictVO;
import com.steccn.platform.vo.SysOfficeVO;
import com.steccn.platform.vo.SysSpareUnitConversionVO;
import com.steccn.platform.vo.SysStationWarehouseVO;
import com.steccn.pms.constant.PmManagerConstant;
import com.steccn.pms.dto.DashboardProducePageDTO;
import com.steccn.pms.service.PmsReportWorkService;
import com.steccn.pms.service.PmsSjgReportDashboardService;
import com.steccn.pms.util.UnitConvertUtil;
import com.steccn.pms.vo.DashboardProducePageVO;
import com.steccn.wms.feign.RemoteWmsTransferSlipFeignApi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
        List<SysOfficeVO> wsLineList = getWsLineList(workShopId);
        SteccnTableAssertUtil.notEmpty(wsLineList, "车间下暂无产线");
        List<Integer> lineIdList = wsLineList.stream().map(SysOfficeVO::getId).collect(Collectors.toList());
        Map<Integer, BigDecimal> consumeMap = pmsReportWorkService.getDateRangeUseCountUnit(lineIdList, date, date, unit);
        BigDecimal sum = BigDecimalUtil.sum(consumeMap.values());
        return BigDecimalUtil.setScale(sum, unitLength);
    }

    @Override
    public List<BigDecimal> produceNumToday(Integer workShopId, LocalDate date) {
        Map<Integer, SysOfficeVO> sysOfficeMap = FeignOptional.of(() -> remoteOfficeFeign.listById(Sets.newHashSet(workShopId)), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        Assert.notEmpty(sysOfficeMap, "车间不能为空");
        SysOfficeVO wsOffice = sysOfficeMap.get(workShopId);
        int type = wsOffice.getOfficeName().indexOf("基片") != -1 ? 0 : 1;
        //根据车间区分深加工还是基片
        //①基片产量：所有报工加工数之和
        //②深加工产量:磨边工位报工加工数之和
        List<Integer> lineIdList = getWsLineList(wsOffice);
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
        Map<Integer, SysOfficeVO> sysOfficeMap = FeignOptional.of(() -> remoteOfficeFeign.listById(Sets.newHashSet(workShopId)), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        Assert.notEmpty(sysOfficeMap, "车间不能为空");
        SysOfficeVO wsOffice = sysOfficeMap.get(workShopId);
        List<Integer> lineIdList = getWsLineList(wsOffice);
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
        return null;
    }

    @Override
    public DashboardProducePageVO producePage(DashboardProducePageDTO dto) {
        return null;
    }

    @Override
    public QtLineChartBarDTO lineUseCount(Integer workShopId, LocalDate date) {
        return null;
    }

    @Override
    public QtLineChartBarDTO lineComplete(Integer workShopId, LocalDate date) {
        return null;
    }

    @Override
    public QtLineChartBarDTO lineInStorage(Integer workShopId, LocalDate date) {
        return null;
    }

    /**
     * 获取车间的产线集合
     *
     * @param workShopId 车间
     * @return
     */
    private List<SysOfficeVO> getWsLineList(Integer workShopId) {
        //根据车间获取产线集合
        SysOfficeParentDTO sysOfficeParent = new SysOfficeParentDTO();
        sysOfficeParent.setId(workShopId);
        sysOfficeParent.setOfficeType(SysOfficeConstant.PRODUCT_LINE);
        List<SysOfficeVO> lineOfficeList = FeignOptional.of(() -> remoteOfficeFeign.listByParentId(sysOfficeParent), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        return lineOfficeList;
    }

    /**
     * 获取车间的产线集合
     *
     * @param wsOffice 车间
     * @return
     */
    private List<Integer> getWsLineList(SysOfficeVO wsOffice) {
        //根据车间获取产线集合
        SysOfficeParentDTO sysOfficeParent = new SysOfficeParentDTO();
        sysOfficeParent.setId(wsOffice.getId());
        sysOfficeParent.setOfficeType(SysOfficeConstant.PRODUCT_LINE);
        List<SysOfficeVO> lineOfficeList = FeignOptional.of(() -> remoteOfficeFeign.listByParentId(sysOfficeParent), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        Assert.notEmpty(lineOfficeList, "车间下暂无产线");
        List<Integer> lineIdList = lineOfficeList.stream().map(SysOfficeVO::getId).collect(Collectors.toList());
        return lineIdList;
    }
}
