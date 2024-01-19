package com.steccn.pms.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.lang.Assert;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.steccn.authserver.exception.BusinessException;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.authserver.util.SteccnTableAssertUtil;
import com.steccn.common.constant.FeignConstant;
import com.steccn.common.constant.ResultCode;
import com.steccn.common.constant.SysDictConstant;
import com.steccn.common.dto.*;
import com.steccn.common.util.*;
import com.steccn.common.vo.EpChartGraphVO;
import com.steccn.common.vo.R;
import com.steccn.dp.dto.DpPmsOperateDTO;
import com.steccn.dp.feign.RemoteDpFeignApi;
import com.steccn.dp.vo.DpOnOffCountVO;
import com.steccn.dp.vo.DpValueDateVO;
import com.steccn.dp.vo.DpWeightVO;
import com.steccn.oa.constant.OaConstant;
import com.steccn.platform.constant.SysOfficeConstant;
import com.steccn.platform.dto.SysOfficeParentDTO;
import com.steccn.platform.feign.*;
import com.steccn.platform.vo.*;
import com.steccn.pms.constant.PmManagerConstant;
import com.steccn.pms.controller.ProduceOrderController;
import com.steccn.pms.domain.*;
import com.steccn.pms.dto.DashboardProducePageDTO;
import com.steccn.pms.dto.PmsOperateDriverDTO;
import com.steccn.pms.dto.PmsPullForceDayCountDTO;
import com.steccn.pms.dto.RawMaterialReportDTO;
import com.steccn.pms.mapper.PmsPullForceMapper;
import com.steccn.pms.service.*;
import com.steccn.pms.util.UnitConvertUtil;
import com.steccn.pms.vo.DashboardProducePageListVO;
import com.steccn.pms.vo.DashboardProducePageVO;
import com.steccn.pms.vo.PmsDeepProcessingNumVO;
import com.steccn.pms.vo.PmsPullForceMeltingNumVO;
import com.steccn.qms.constant.QtManagerConstant;
import com.steccn.qms.util.MysqlDateKeyUtil;
import com.steccn.qms.util.XAxisUtil;
import com.steccn.wms.feign.RemoteWmsTransferSlipFeignApi;
import com.steccn.wms.vo.WmsTransferSlipInfoVO;
import io.swagger.annotations.ApiModelProperty;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.Lists;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author yequan
 * @desc 生产看板服务实现
 * @date 2023/8/28
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PmsReportDashboardServiceImpl implements PmsReportDashboardService {

    private final PmsPullForceServiceImpl pmsPullForceService;
    private final RemoteOfficeFeign remoteOfficeFeign;
    private final PmsReportWorkService pmsReportWorkService;
    private final RemoteSpareUnitConvertFeignApi spareUnitConvertFeignApi;
    private final RemoteDictFeignApi remoteDictFeignApi;
    private final PmsProduceOrderService pmsProduceOrderService;
    private final PmsWorkOrderBatchService pmsWorkOrderBatchService;
    private final RemoteSpareFeignApi remoteSpareFeignApi;
    private final PmsCompleteStorageInfoService pmsCompleteStorageInfoService;
    private final RemoteDpFeignApi remoteDpFeignApi;
    private final PmsPullForceMapper pmsPullForceMapper;
    private final PmsOfficeManager pmsOfficeManager;
    private final RemoteStationWarehouseFeignApi remoteStationWarehouseFeignApi;
    private final RemoteWmsTransferSlipFeignApi remoteWmsTransferSlipFeignApi;
    private final RemoteSpareUnitConvertFeignApi remoteSpareUnitConvertFeignApi;

    @Override
    public BigDecimal pullForceToday(Integer workShopId, LocalDate date) {
        BigDecimal resVal = null;
        List<PmsPullForce> dbList = pmsPullForceService.list(new LambdaQueryWrapper<PmsPullForce>()
                .eq(PmsPullForce::getPullForceDate, date)
                .eq(PmsPullForce::getWorkshopId, workShopId)
                .select(PmsPullForce::getQuantity));
        if (CollUtil.isNotEmpty(dbList)) {
            List<BigDecimal> collect = GetValueUtil.collectionGet(dbList, x -> x.getQuantity());
            resVal = BigDecimalUtil.sum(collect);
            resVal = BigDecimalUtil.setScale(resVal, UnitLengthUtil.UNIT_TON_LENGTH);
        }
        return resVal;
    }

    @Override
    public BigDecimal pullForceDateRange(Integer partId, LocalDate start, LocalDate end) {
        //获取部门下的车间
        BigDecimal resVal = null;
        List<SysOfficeVO> workshopList = pmsOfficeManager.getChildren(partId, SysOfficeConstant.WORKSHOP);
        Assert.notNull(workshopList, "制造部下暂无车间");
        List<Integer> workshopIdList = GetValueUtil.collectionGet(workshopList, x -> x.getId());
        List<PmsPullForce> dbList = pmsPullForceService.list(new LambdaQueryWrapper<PmsPullForce>()
                .between(PmsPullForce::getPullForceDate, start, end)
                .in(PmsPullForce::getWorkshopId, workshopIdList)
                .select(PmsPullForce::getQuantity));
        if (CollUtil.isNotEmpty(dbList)) {
            List<BigDecimal> collect = GetValueUtil.collectionGet(dbList, x -> x.getQuantity());
            resVal = BigDecimalUtil.sum(collect);
            resVal = BigDecimalUtil.setScale(resVal, UnitLengthUtil.UNIT_TON_LENGTH);
        }
        return resVal;
    }

    @Override
    public BigDecimal meltingRate(Integer workShopId, LocalDate date) {
        BigDecimal pullForce = pullForceToday(workShopId, date);
        List<DpWeightVO> dpWeightVOS = FeignOptional.of(() -> remoteDpFeignApi.count(new TupleDTO<>(date.atTime(8, 0, 0)
                , date.plusDays(1).atTime(7, 59, 59))), FeignConstant.STECCN_DP_SERVICE_NAME).get();
        BigDecimal weight = dpWeightVOS.stream().map(DpWeightVO::getValue).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal divide = BigDecimalUtil.divide(weight, new BigDecimal(1000));
        BigDecimal result = BigDecimalUtil.percent(pullForce, divide, 2);
        return result;
    }

    @Override
    public List<BigDecimal> produceNumToday(Integer workShopId, LocalDate date) {
        SysOfficeVO wsOffice = FeignOptional.of(() -> remoteOfficeFeign.sysOfficeInfo(workShopId), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        Assert.notNull(wsOffice, "车间不能为空");
        int type = wsOffice.getOfficeName().indexOf("基片") != -1 ? 0 : 1;
        //根据车间区分深加工还是基片
        //①基片产量：所有报工加工数之和
        //②深加工产量:磨边工位报工加工数之和
        List<SysOfficeVO> wsLineList = pmsOfficeManager.getWsLineList(wsOffice.getId());
        Assert.notEmpty(wsLineList, "车间下暂无产线");
        List<Integer> lineIdList = GetValueUtil.collectionGet(wsLineList, x -> x.getId());
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
    public List<BigDecimal> produceNumDateRange(Integer partId, LocalDate start, LocalDate end) {
        SysOfficeVO partOffice = FeignOptional.of(() -> remoteOfficeFeign.sysOfficeInfo(partId), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        Assert.notNull(partOffice, "制造部不能为空");
        int type = partOffice.getOfficeName().indexOf("基片") != -1 ? 0 : 1;
        //根据车间区分深加工还是基片
        //①基片产量：所有报工加工数之和
        //②深加工产量:磨边工位报工加工数之和
        List<SysOfficeVO> wsList = pmsOfficeManager.getChildren(partOffice.getId(), SysOfficeConstant.WORKSHOP);
        Assert.notEmpty(wsList, "制造部下暂无车间");
        List<Integer> wsIdList = GetValueUtil.collectionGet(wsList, x -> x.getId());
        //获取产线
        List<SysOfficeVO> lineList = pmsOfficeManager.getChildren(wsIdList, SysOfficeConstant.PRODUCT_LINE);
        Assert.notEmpty(lineList, "车间下暂无产线");
        List<Integer> lineIdList = GetValueUtil.collectionGet(lineList, x -> x.getId());
        Map<Long, BigDecimal> dateRangePmMap = pmsReportWorkService.getDateRangePm(lineIdList, start, end, type);
        //获取物料转换单位
        Map<Long, List<SysSpareUnitConversionVO>> convertMap = FeignOptional.of(() -> spareUnitConvertFeignApi.convert(dateRangePmMap.keySet()), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
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
        for (Map.Entry<Long, BigDecimal> entry : dateRangePmMap.entrySet()) {
            BigDecimal value = entry.getValue();
            BigDecimal convert1 = UnitConvertUtil.convert(convertMap, entry.getKey(), sysDict1.getId(), sysDict2.getId(), value);
            BigDecimal convert2 = UnitConvertUtil.convert(convertMap, entry.getKey(), sysDict1.getId(), sysDict3.getId(), value);
            sum1 = BigDecimalUtil.add(sum1, value);
            sum2 = BigDecimalUtil.add(sum2, convert1);
            sum3 = BigDecimalUtil.add(sum3, convert2);
        }
        return Arrays.asList(BigDecimalUtil.setScale(sum2, UnitLengthUtil.UNIT_TON_LENGTH), BigDecimalUtil.setScale(sum1, UnitLengthUtil.UNIT_PIECE_LENGTH), BigDecimalUtil.setScale(sum3, UnitLengthUtil.UNIT_SQUARE_METER_LENGTH));
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
        Map<Integer, SysOfficeVO> resMap = Maps.newHashMap();
        lineOfficeList.forEach(ele -> {
            resMap.put(ele.getId(), ele);

        });
        return resMap;
    }

    @Override
    public BigDecimal produceNumRate(Integer workShopId, LocalDate date) {
        BigDecimal num = produceNumToday(workShopId, date).get(0);
        BigDecimal pullForce = pullForceToday(workShopId, date);
        return BigDecimalUtil.percent(num, pullForce);
    }

    @Override
    public BigDecimal produceNumRateDateRange(Integer partId, LocalDate start, LocalDate end) {
        BigDecimal num = produceNumDateRange(partId, start, end).get(0);
        BigDecimal pullForce = pullForceDateRange(partId, start, end);
        return BigDecimalUtil.percent(num, pullForce);
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
    public List<BigDecimal> inStorageDateRange(Integer partId, LocalDate start, LocalDate end) {
        SysOfficeVO partOffice = FeignOptional.of(() -> remoteOfficeFeign.sysOfficeInfo(partId), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        Assert.notNull(partOffice, "制造部不能为空");
        List<SysOfficeVO> workshopList = pmsOfficeManager.getChildren(partOffice.getId(), SysOfficeConstant.WORKSHOP);
        Assert.notEmpty(workshopList, "制造部下暂无车间");
        List<Integer> wsIdList = GetValueUtil.collectionGet(workshopList, x -> x.getId());
        //获取产线
        List<SysOfficeVO> lineList = pmsOfficeManager.getChildren(wsIdList, SysOfficeConstant.PRODUCT_LINE);
        Assert.notEmpty(lineList, "车间下暂无产线");
        List<Integer> lineIdList = GetValueUtil.collectionGet(lineList, x -> x.getId());
        Map<Long, BigDecimal> inStorageMap = pmsReportWorkService.getDateRangeInStorage(lineIdList, start, end);
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
        for (Map.Entry<Long, BigDecimal> entry : inStorageMap.entrySet()) {
            BigDecimal value = entry.getValue();
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
        BigDecimal pullForce = pullForceToday(workShopId, date);
        return BigDecimalUtil.percent(num, pullForce, 2);
    }

    @Override
    public BigDecimal inStorageRateDateRange(Integer partId, LocalDate start, LocalDate end) {
        BigDecimal num = inStorageDateRange(partId,start,end).get(0);
        BigDecimal pullForce = pullForceDateRange(partId,start,end);
        return BigDecimalUtil.percent(num, pullForce);
    }

    @Override
    public DashboardProducePageVO producePage(DashboardProducePageDTO dto) {
        //获取产线
        Map<Integer, SysOfficeVO> sysOfficeMap = FeignOptional.of(() -> remoteOfficeFeign.listById(Sets.newHashSet(dto.getProductLineId())), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        Assert.notEmpty(sysOfficeMap, "车间不能为空");
        SysOfficeVO wsOffice = sysOfficeMap.get(dto.getProductLineId());
        int type = wsOffice.getOfficeName().indexOf("基片") != -1 ? 0 : 1;
        DashboardProducePageVO resVo = new DashboardProducePageVO();
        //获取当前产线正在执行的订单批次
        PmsProduceOrder currentOrder = pmsProduceOrderService.getOne(new LambdaQueryWrapper<PmsProduceOrder>()
                .eq(PmsProduceOrder::getState, 2)
                .eq(PmsProduceOrder::getProductionLineId, dto.getProductLineId()), false);
        if (Objects.nonNull(currentOrder)) {
            //根据当前订单获取当前批次
            PmsWorkOrderBatch currentBatch = pmsWorkOrderBatchService.getOne(new LambdaQueryWrapper<PmsWorkOrderBatch>().eq(PmsWorkOrderBatch::getProduceOrderId, currentOrder.getId()).eq(PmsWorkOrderBatch::getCurrentBatchFlag, 1), false);
            if (Objects.nonNull(currentBatch)) {
                resVo.setBatchNo(currentBatch.getCode());
                resVo.setStartTime(currentBatch.getStartTime());
                //批次加工总数：入库单中明细批次为本批次的数量汇总，数量需转换为重量T
                List<PmsCompleteStorageInfo> csList = pmsCompleteStorageInfoService.list(new LambdaQueryWrapper<PmsCompleteStorageInfo>()
                        .eq(PmsCompleteStorageInfo::getProduceOrderId, currentOrder.getId())
                        .eq(PmsCompleteStorageInfo::getBatchNo, currentBatch.getCode()));
                if (CollUtil.isNotEmpty(csList)) {
                    List<BigDecimal> completeQuantityList = csList.stream().map(PmsCompleteStorageInfo::getQuantity).collect(Collectors.toList());
                    resVo.setCompleteQuantity(BigDecimalUtil.sum(completeQuantityList));
                }
            }
            //获取物料规格
            EqpSpareVO eqpSpareVO = FeignOptional.of(() -> remoteSpareFeignApi.getById(currentOrder.getMaterialId()), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            if (Objects.nonNull(eqpSpareVO)) {
                resVo.setSpecifications(eqpSpareVO.getSpecifications());
            }
        }
        //10、工单列表：展示本产线所有订单（对应状态 已下达  已暂停 暂停中 执行中）
        //状态（0 ：已创建  1：已下单  2：执行中 3：暂停中  4：已暂停 5：完成中 6：已完成 7：已关闭 ）
        //分页获取订单，按创建时间倒序
        Page<PmsProduceOrder> page = pmsProduceOrderService.page(new Page<>(dto.getCurrent(), dto.getSize()), new LambdaQueryWrapper<PmsProduceOrder>()
                .in(PmsProduceOrder::getState, Arrays.asList(1, 3, 4, 2))
                .eq(Objects.nonNull(dto.getProductLineId()), PmsProduceOrder::getProductionLineId, dto.getProductLineId())
                .eq(Objects.nonNull(dto.getState()), PmsProduceOrder::getState, dto.getState())
                .orderByDesc(PmsProduceOrder::getCreateTime));
        //获取到订单
        List<DashboardProducePageListVO> resList = new ArrayList<>();
        if (CollUtil.isNotEmpty(page.getRecords())) {
            //获取所有的规格
            Set<Long> materialIdSet = Sets.newHashSet();
            List<Integer> lineList = Lists.newArrayList();
            Set<Long> orderIdSet = Sets.newHashSet();
            page.getRecords().forEach(ele -> {
                materialIdSet.add(ele.getMaterialId());
                lineList.add(ele.getProductionLineId());
                orderIdSet.add(ele.getId());
            });
            Map<Long, BigDecimal> pmByOrderMap = pmsReportWorkService.getPmByOrder(lineList, orderIdSet, type);
            Map<Long, BigDecimal> inStorageMap = pmsReportWorkService.getInStorageByOrder(orderIdSet);
            Map<Long, EqpSpareVO> spareMap = FeignOptional.of(() -> remoteSpareFeignApi.listById(new ArrayList<>(materialIdSet)), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            page.getRecords().forEach(ele -> {
                DashboardProducePageListVO res = new DashboardProducePageListVO();
                BeanUtils.copyProperties(ele, res);
                res.setPlanQuantity(ele.getQuantity());
                res.setOrderId(ele.getId());
                res.setSpecifications(spareMap.getOrDefault(ele.getMaterialId(), new EqpSpareVO()).getSpecifications());
                //完工数量：订单对应的报工数量汇总
                res.setCompleteQuantity(pmByOrderMap.get(ele.getId()));
                //入库数量：订单对应的入库单数量汇总
                res.setInStorageQuantity(inStorageMap.get(ele.getId()));
                //欠额：计划数量 - 入库数量
                res.setDebtQuantity(BigDecimalUtil.subtract(res.getPlanQuantity(), res.getInStorageQuantity()));
                resList.add(res);
            });
        }
        Page<DashboardProducePageListVO> resPage = new Page<>();
        BeanUtils.copyProperties(page, resPage);
        resPage.setRecords(resList);
        resVo.setPageList(resPage);
        return resVo;
    }

    @Override
    public QtLineChartBarDTO linePullForce(Integer workShopId, LocalDate date) {
        Map<Integer, SysOfficeVO> sysOfficeMap = FeignOptional.of(() -> remoteOfficeFeign.listById(Sets.newHashSet(workShopId)), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        Assert.notEmpty(sysOfficeMap, "车间不能为空");
        Map<Integer, SysOfficeVO> wsLineMap = getWsLineMap(sysOfficeMap.get(workShopId));
        Assert.notEmpty(wsLineMap, "车间下暂无产线");
        //获取班次拉引量
        List<PmsPullForce> list = pmsPullForceService.list(new LambdaQueryWrapper<PmsPullForce>().eq(PmsPullForce::getPullForceDate, date).in(PmsPullForce::getProductLineId, wsLineMap.keySet()));
        SteccnTableAssertUtil.notEmpty(list, "拉引量不存在");
        //获取早晚班
        SysDictVO morningDict = FeignOptional.of(() -> remoteDictFeignApi.getByCode(PmManagerConstant.NUM_0, PmManagerConstant.SYS_SHIFT), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        SysDictVO nightDict = FeignOptional.of(() -> remoteDictFeignApi.getByCode(PmManagerConstant.NUM_1, PmManagerConstant.SYS_SHIFT), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        Assert.notNull(morningDict, "早班数据字典不存在");
        Assert.notNull(nightDict, "晚班数据字典不存在");
        List<StackGraphDTO<String, BigDecimal>> dataList = Lists.newArrayList();

        //对line排序
        List<SysOfficeVO> sortLineList = wsLineMap.values().stream().sorted(Comparator.comparing(SysOfficeVO::getTreeSort)).collect(Collectors.toList());
        Map<Integer, List<PmsPullForce>> lineMap = list.stream().collect(Collectors.groupingBy(PmsPullForce::getProductLineId));
        Map<Integer, List<PmsPullForce>> objMap = Maps.newLinkedHashMap();
        sortLineList.forEach(ele -> objMap.put(ele.getId(), lineMap.getOrDefault(ele.getId(), Lists.newArrayList())));

        for (Map.Entry<Integer, List<PmsPullForce>> lineEntry : objMap.entrySet()) {
            List<PmsPullForce> value = lineEntry.getValue();
            StackGraphDTO<String, BigDecimal> stackGraphDTO = new StackGraphDTO<>();
            String officeName = wsLineMap.get(lineEntry.getKey()).getOfficeName();
            stackGraphDTO.setXAxis(officeName);
            List<TupleDTO<String, BigDecimal>> datList = Lists.newArrayList();
            PmsPullForce morning = value.stream().filter(ele -> morningDict.getId().equals(ele.getClassesId())).findFirst().orElse(new PmsPullForce());
            PmsPullForce night = value.stream().filter(ele -> nightDict.getId().equals(ele.getClassesId())).findFirst().orElse(new PmsPullForce());
            datList.add(new TupleDTO<>("产线总拉引量", BigDecimalUtil.add(morning.getQuantity(), night.getQuantity())));
            datList.add(new TupleDTO<>("白班拉引量", morning.getQuantity()));
            datList.add(new TupleDTO<>("夜班拉引量", night.getQuantity()));
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
        List<PmsPullForce> list = pmsPullForceService.list(new LambdaQueryWrapper<PmsPullForce>().eq(PmsPullForce::getPullForceDate, date).in(PmsPullForce::getProductLineId, wsLineMap.keySet()));
        Map<String, BigDecimal> pfMap = Maps.newHashMap();
        if (CollUtil.isNotEmpty(list)) {
            list.forEach(ele -> pfMap.put(ele.getProductLineId() + "_" + ele.getClassesId(), ele.getQuantity()));
        }
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
    public QtLineChartBarDTO lineInStorage(Integer workShopId, LocalDate date) {
        Map<Integer, SysOfficeVO> sysOfficeMap = FeignOptional.of(() -> remoteOfficeFeign.listById(Sets.newHashSet(workShopId)), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        Assert.notEmpty(sysOfficeMap, "车间不能为空");
        Map<Integer, SysOfficeVO> wsLineMap = getWsLineMap(sysOfficeMap.get(workShopId));
        Assert.notEmpty(wsLineMap, "车间下暂无产线");
        //获取班次产量
        Map<String, BigDecimal> inStorageMap = pmsReportWorkService.lineClassInStorage(wsLineMap.keySet(), date, date, PmManagerConstant.MEASURE_UNIT_TON);
        SteccnTableAssertUtil.notEmpty(inStorageMap, "暂无数据");
        List<PmsPullForce> list = pmsPullForceService.list(new LambdaQueryWrapper<PmsPullForce>().eq(PmsPullForce::getPullForceDate, date).in(PmsPullForce::getProductLineId, wsLineMap.keySet()));
        Map<String, BigDecimal> pfMap = Maps.newHashMap();
        if (CollUtil.isNotEmpty(list)) {
            list.forEach(ele -> pfMap.put(ele.getProductLineId() + "_" + ele.getClassesId(), ele.getQuantity()));
        }
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
            datList.add(new TupleDTO<>("产线产量合格率", valueMap.get(ele.getId()).get(3)));
            datList.add(new TupleDTO<>("白班产量合格率", valueMap.get(ele.getId()).get(4)));
            datList.add(new TupleDTO<>("夜班产量合格率", valueMap.get(ele.getId()).get(5)));
            stackGraphDTO.setData(datList);
            dataList.add(stackGraphDTO);
        });
        return StackGraphUtil.convertData(dataList, Boolean.TRUE);
    }

    @Override
    public QtLineChartBarDTO pullForceMelting(DpPmsOperateDTO dto) {
        QtManagerConstant.QtGranularityEnum granularity = QtManagerConstant.QtGranularityEnum.getByCode(dto.getGranularity());
        Assert.notNull(granularity, "粒度不存在");
        //获取电子秤读数
        List<DpValueDateVO> weightCount = FeignOptional.of(() -> remoteDpFeignApi.countAsDay(dto), FeignConstant.STECCN_DP_SERVICE_NAME).get();
        //拉引量(基片车间)
        List<PmsPullForceDayCountDTO> pullAmountCount;
        if (QtManagerConstant.QtGranularityEnum.DAY.equals(granularity)) {
            pullAmountCount = pmsPullForceMapper.selectPullForceByDay(dto.getStart(),dto.getEnd(),Sets.newHashSet(75,76));
        } else if (QtManagerConstant.QtGranularityEnum.WEEK.equals(granularity)) {
            pullAmountCount = pmsPullForceMapper.selectPullForceByWeek(dto.getStart(),dto.getEnd(),Sets.newHashSet(75,76));
        } else if (QtManagerConstant.QtGranularityEnum.MONTH.equals(granularity)) {
            pullAmountCount = pmsPullForceMapper.selectPullForceByMonth(dto.getStart(),dto.getEnd(),Sets.newHashSet(75,76));
        } else {
            pullAmountCount = pmsPullForceMapper.selectPullForceByYear(dto.getStart(),dto.getEnd(),Sets.newHashSet(75,76));
        }

        if(CollUtil.isEmpty(weightCount)&&CollUtil.isEmpty(pullAmountCount)){
            SteccnTableAssertUtil.notEmpty(Lists.newArrayList(), "暂无数据");
        }
        List<StackGraphDTO<String, BigDecimal>> dataList = Lists.newArrayList();
        List<LocalDate> dateList = ShardTimeUtil.cutDate(granularity.getType(), dto.getStart(), dto.getEnd());
//        if(dto.getStart().equals(dto.getEnd())&&CollUtil.isEmpty(dateList)){
//            dateList.add(dto.getStart());
//        }
        //投料量接口问题,算总数
        Map<Integer,BigDecimal> weightCountMap = Maps.newHashMap();
        for (int i = 1; i < dateList.size(); i++) {
            LocalDate start = dateList.get(i - 1);
            StackGraphDTO<String, BigDecimal> stackGraphDTO = new StackGraphDTO<>();
            stackGraphDTO.setXAxis(XAxisUtil.convert(granularity, start));
            String key = MysqlDateKeyUtil.convert(granularity, start);
            List<TupleDTO<String, BigDecimal>> datList = Lists.newArrayList();
            //耗料总量
            BigDecimal weightCountTmp = BigDecimal.ZERO;
            if(CollUtil.isNotEmpty(weightCount)){
                DpValueDateVO valueDateVO = weightCount.stream().filter(v -> v.getDateStr().equals(key)).findFirst().orElse(null);
                if(Objects.nonNull(valueDateVO)){
                    weightCountTmp = BigDecimalUtil.divide(valueDateVO.getValue(),new BigDecimal(1000),2) ;
                    BigDecimalUtil.accumulate(weightCountMap,1,weightCountTmp);
                }
            }
            //拉引总量
            BigDecimal pullCountTmp = BigDecimal.ZERO;
            if(CollUtil.isNotEmpty(pullAmountCount)){
                PmsPullForceDayCountDTO pmsPullForce = pullAmountCount.stream().filter(v -> v.getPullForceDateStr().equals(key)).findFirst().orElse(null);
                if(Objects.nonNull(pmsPullForce)){
                    pullCountTmp = pmsPullForce.getQuantity();
                }
            }
            datList.add(new TupleDTO<>("料耗总量", weightCountTmp));
            datList.add(new TupleDTO<>("拉引量T", pullCountTmp));
            datList.add(new TupleDTO<>("熔化率", BigDecimalUtil.percent(pullCountTmp,weightCountTmp,2)));
            stackGraphDTO.setData(datList);
            dataList.add(stackGraphDTO);
        }
        //算总和
        PmsPullForceMeltingNumVO allNum = new PmsPullForceMeltingNumVO();
        if(CollUtil.isNotEmpty(weightCount)){
//            BigDecimal sum = weightCount.stream()
//                    .map(DpValueDateVO::getValue)
//                    .reduce(BigDecimal.ZERO, BigDecimal::add);
//            allNum.setWeightCount(BigDecimalUtil.divide(sum,new BigDecimal(1000),2));
            allNum.setWeightCount(weightCountMap.get(1));
        }
        if(CollUtil.isNotEmpty(pullAmountCount)){
            BigDecimal sum = pullAmountCount.stream()
                    .map(PmsPullForceDayCountDTO::getQuantity)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            allNum.setPullForce(sum);
        }
        allNum.setPercent(BigDecimalUtil.percent(allNum.getPullForce(),allNum.getWeightCount(),2));
        QtLineChartBarDTO<String,PmsPullForceMeltingNumVO> chat = StackGraphUtil.convertData(dataList, Boolean.TRUE);
        chat.setOther(allNum);
        return chat;
    }

    @Override
    public PmsPullForceMeltingNumVO pullForceMeltingNum(LocalDate start, LocalDate end) {
        PmsPullForceMeltingNumVO result = new PmsPullForceMeltingNumVO();
        //获取电子秤读数
        List<DpWeightVO> weightCount = FeignOptional.of(() -> remoteDpFeignApi.count(new TupleDTO<LocalDateTime, LocalDateTime>(start.atTime(8, 0, 0), end.plusDays(1).atTime(7, 59, 59))), FeignConstant.STECCN_DP_SERVICE_NAME).get();
        //拉引量(基片车间)
        List<PmsPullForceDayCountDTO> pullAmountCount = pmsPullForceMapper.selectPullForceByDay(start,end,Sets.newHashSet(75,76));
        if(CollUtil.isNotEmpty(weightCount)){
            BigDecimal sum = weightCount.stream()
                    .map(DpWeightVO::getValue)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            result.setWeightCount(BigDecimalUtil.divide(sum,new BigDecimal(1000),2));
        }
        if(CollUtil.isNotEmpty(pullAmountCount)){
            BigDecimal sum = pullAmountCount.stream()
                    .map(PmsPullForceDayCountDTO::getQuantity)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            result.setPullForce(sum);
        }
        result.setPercent(BigDecimalUtil.percent(result.getPullForce(),result.getWeightCount(),2));
        return result;
    }

    @Override
    public PmsDeepProcessingNumVO deepProcessingNum(PmsOperateDriverDTO dto) {
        PmsDeepProcessingNumVO resVO = new PmsDeepProcessingNumVO();
        //领用总数
        //获取深加工车间下的工位绑定的线边仓
        List<SysOfficeVO> sysOfficeVOList = FeignOptional.of(() -> remoteOfficeFeign.listWorkshop(1), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        Set<Integer> stationIds = sysOfficeVOList.stream().filter(v -> v.getOfficeType().equals("13")).map(SysOfficeVO::getId).collect(Collectors.toSet());
        //获取工位绑定的线边仓数据
        Map<Integer, List<SysStationWarehouseVO>> stationMap = FeignOptional.of(() -> remoteStationWarehouseFeignApi.listByStationIds(stationIds), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        List<Integer> warehouseIds = stationMap.values().stream().flatMap(List::stream).map(SysStationWarehouseVO::getWarehouseId).collect(Collectors.toList());
        //获取生产订单的执行时间为当前时间段的生产订单生产物料的主物料
        List<Long> materialIds =  pmsProduceOrderService.listOrderMaterialIdByDate(dto.getStart(),dto.getEnd());
        //获取调拨单目标货架为线边仓的指定物料数
        RawMaterialReportDTO rawMaterialReportDTO = new RawMaterialReportDTO();
        rawMaterialReportDTO.setStartDate(dto.getStart());
        rawMaterialReportDTO.setEndDate(dto.getEnd());
        rawMaterialReportDTO.setMaterialIds(materialIds);
        rawMaterialReportDTO.setWarehouseIds(warehouseIds);
        Map<Long, WmsTransferSlipInfoVO> receiveMap = FeignOptional.of(() -> remoteWmsTransferSlipFeignApi.materialQuantityByDateAndWarehouse(rawMaterialReportDTO), FeignConstant.STECCN_WMS_SERVICE_NAME).get();
        Set<Long> materialIdSet = receiveMap.keySet();
        //进行单位转换
        SysDictVO sqSysDict = FeignOptional.of(() -> remoteDictFeignApi.getByCode(PmManagerConstant.MEASURE_UNIT_SM, SysDictConstant.TYPE_MEASURE_UNIT), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        Map<Long, List<SysSpareUnitConversionVO>> materialConvertMap = FeignOptional.of(() -> remoteSpareUnitConvertFeignApi.convert(materialIdSet), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        BigDecimal receiveQuantity = BigDecimal.ZERO;
        for (WmsTransferSlipInfoVO value : receiveMap.values()) {
            BigDecimal m= UnitConvertUtil.convertMaterial(materialConvertMap.get(value.getMaterialId()), value.getUnit(), sqSysDict.getId(), value.getQuantity(), 4);
            receiveQuantity = BigDecimalUtil.add(receiveQuantity,m);
        }
        resVO.setReceiveQuantity(receiveQuantity);
        //耗用总数（取报工物料耗用总数）
        //获取深加工的八条产线的id
        Set<String> lineNames = Sets.newHashSet();
        lineNames.add(PmManagerConstant.DeepProcessingLine.ONE.getName());
        lineNames.add(PmManagerConstant.DeepProcessingLine.TWO.getName());
        lineNames.add(PmManagerConstant.DeepProcessingLine.THREE.getName());
        lineNames.add(PmManagerConstant.DeepProcessingLine.FOUR.getName());
        lineNames.add(PmManagerConstant.DeepProcessingLine.FIVE.getName());
        lineNames.add(PmManagerConstant.DeepProcessingLine.SIX.getName());
        lineNames.add(PmManagerConstant.DeepProcessingLine.SEVEN.getName());
        lineNames.add(PmManagerConstant.DeepProcessingLine.EIGHT.getName());
        Map<String, SysOfficeVO> sysOfficeVOMap = FeignOptional.of(() -> remoteOfficeFeign.listByName(lineNames), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        List<Integer> lineIdList = Lists.newArrayList();
        sysOfficeVOMap.values().forEach(e->lineIdList.add(e.getId()));
        Map<Integer, BigDecimal> consumptionMap  = pmsReportWorkService.getDateRangeUseCountUnit(lineIdList, dto.getStart(), dto.getEnd(), PmManagerConstant.MEASURE_UNIT_SM);
        BigDecimal consumptionQuantity = BigDecimal.ZERO;
        for (BigDecimal value : consumptionMap.values()) {
            BigDecimalUtil.add(consumptionQuantity,value);
        }
        resVO.setConsumptionQuantity(consumptionQuantity);
        resVO.setUseQuantity(BigDecimalUtil.subtract(receiveQuantity,consumptionQuantity));
        return resVO;
    }

    @Override
    public EpChartGraphVO<List<String>,TripleDTO<BigDecimal,BigDecimal,BigDecimal>> deepProcessingHistogram(PmsOperateDriverDTO dto) {
        QtManagerConstant.QtGranularityEnum granularity = QtManagerConstant.QtGranularityEnum.getByCode(dto.getGranularity());
        Assert.notNull(granularity, "粒度不存在");
        List<LocalDate> timeList = ShardTimeUtil.cutDate(granularity.getType(), dto.getStart(),dto.getEnd());
        //领用总数
        //获取深加工车间下的工位绑定的线边仓
        List<SysOfficeVO> sysOfficeVOList = FeignOptional.of(() -> remoteOfficeFeign.listWorkshop(1), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        Set<Integer> stationIds = sysOfficeVOList.stream().filter(v -> v.getOfficeType().equals("13")).map(SysOfficeVO::getId).collect(Collectors.toSet());
        //获取工位绑定的线边仓数据
        Map<Integer, List<SysStationWarehouseVO>> stationMap = FeignOptional.of(() -> remoteStationWarehouseFeignApi.listByStationIds(stationIds), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        List<Integer> warehouseIds = stationMap.values().stream().flatMap(List::stream).map(SysStationWarehouseVO::getWarehouseId).collect(Collectors.toList());
        //获取生产订单的执行时间为当前时间段的生产订单生产物料的主物料
        List<Long> materialIds =  pmsProduceOrderService.listOrderMaterialIdByDate(dto.getStart(),dto.getEnd());
        //获取调拨单目标货架为线边仓的指定物料数
        RawMaterialReportDTO rawMaterialReportDTO = new RawMaterialReportDTO();
        rawMaterialReportDTO.setStartDate(dto.getStart());
        rawMaterialReportDTO.setEndDate(dto.getEnd());
        rawMaterialReportDTO.setMaterialIds(materialIds);
        rawMaterialReportDTO.setWarehouseIds(warehouseIds);
        Map<String, BigDecimal> receiveMap = FeignOptional.of(() -> remoteWmsTransferSlipFeignApi.materialQuantityByDateAndWarehouseQuantity(rawMaterialReportDTO), FeignConstant.STECCN_WMS_SERVICE_NAME).get();
        //耗用数（磨边工位报工物料消耗数）
        //磨边工位
        Set<Integer> edgingStationIds = sysOfficeVOList.stream().filter(v -> v.getOfficeType().equals("13") && v.getOfficeName().contains("磨边工位")).map(SysOfficeVO::getId).collect(Collectors.toSet());
        //获取深加工的八条产线的id
        Set<String> lineNames = Sets.newHashSet();
        lineNames.add(PmManagerConstant.DeepProcessingLine.ONE.getName());
        lineNames.add(PmManagerConstant.DeepProcessingLine.TWO.getName());
        lineNames.add(PmManagerConstant.DeepProcessingLine.THREE.getName());
        lineNames.add(PmManagerConstant.DeepProcessingLine.FOUR.getName());
        lineNames.add(PmManagerConstant.DeepProcessingLine.FIVE.getName());
        lineNames.add(PmManagerConstant.DeepProcessingLine.SIX.getName());
        lineNames.add(PmManagerConstant.DeepProcessingLine.SEVEN.getName());
        lineNames.add(PmManagerConstant.DeepProcessingLine.EIGHT.getName());
        Map<String, SysOfficeVO> sysOfficeVOMap = FeignOptional.of(() -> remoteOfficeFeign.listByName(lineNames), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        Set<Integer> lineIdSet = Sets.newHashSet();
        sysOfficeVOMap.values().forEach(e->lineIdSet.add(e.getId()));
        Map<String, BigDecimal> consumptionMap = pmsReportWorkService.lineUseCount(lineIdSet, edgingStationIds, dto.getStart(), dto.getEnd(), granularity);
        //不良数
        Map<String, BigDecimal> badnessQuantityMap = pmsReportWorkService.badnessQuantity(lineIdSet, edgingStationIds, dto.getStart(), dto.getEnd(), granularity);
        //组数据
        EpChartGraphVO<List<String>,TripleDTO<BigDecimal,BigDecimal,BigDecimal>> res = new EpChartGraphVO<>();
        List<QtSeriesDataDTO> seriesDataVO = Lists.newArrayList();
        List<String> xAisList = Lists.newArrayList();
        List<BigDecimal> receiveQuantityList = Lists.newArrayList();
        List<BigDecimal> consumptionQuantityList = Lists.newArrayList();
        List<BigDecimal> badnessQuantityList = Lists.newArrayList();
        List<BigDecimal> useQuantityList = Lists.newArrayList();
        for (int i = 1; i < timeList.size(); i++) {
            LocalDate ele = timeList.get(i - 1);
            xAisList.add(XAxisUtil.convert(granularity, ele));
            //获取值的方式必须统一
            String key = MysqlDateKeyUtil.convert(granularity, ele);
            //领用数
            BigDecimal receive = receiveMap.get(key) == null ? BigDecimal.ZERO : receiveMap.get(key);
            //耗用数
            BigDecimal consumption = consumptionMap.get(key) == null ? BigDecimal.ZERO : consumptionMap.get(key);
            //不良数
            BigDecimal badness = badnessQuantityMap.get(key)== null ? BigDecimal.ZERO : badnessQuantityMap.get(key);
            //实际使用数（耗用数-不良数）
            BigDecimal use = BigDecimalUtil.subtract(consumption, badness, 3);
            receiveQuantityList.add(receive);
            consumptionQuantityList.add(consumption);
            badnessQuantityList.add(badness);
            useQuantityList.add(use);
        }
        QtSeriesDataDTO<String,BigDecimal> receiveDataVO = new QtSeriesDataDTO<String,BigDecimal>();
        receiveDataVO.setName("领用数量"+PmManagerConstant.MEASURE_UNIT_SM);
        receiveDataVO.setData(receiveQuantityList);
        QtSeriesDataDTO<String,BigDecimal> consumptionDataVO = new QtSeriesDataDTO<String,BigDecimal>();
        consumptionDataVO.setName("耗用数"+PmManagerConstant.MEASURE_UNIT_SM);
        consumptionDataVO.setData(consumptionQuantityList);
        QtSeriesDataDTO<String,BigDecimal> badnessDataVO = new QtSeriesDataDTO<String,BigDecimal>();
        badnessDataVO.setName("原片不良"+PmManagerConstant.MEASURE_UNIT_SM);
        badnessDataVO.setData(badnessQuantityList);
        QtSeriesDataDTO<String,BigDecimal> useDataVO = new QtSeriesDataDTO<String,BigDecimal>();
        useDataVO.setName("实际使用数"+PmManagerConstant.MEASURE_UNIT_SM);
        useDataVO.setData(useQuantityList);
        seriesDataVO.add(receiveDataVO);
        seriesDataVO.add(consumptionDataVO);
        seriesDataVO.add(badnessDataVO);
        seriesDataVO.add(useDataVO);
        res.setSeriesDataVO(seriesDataVO);
        res.setXData(xAisList);
        List<String> legends =   Arrays.asList( "领用数量"+PmManagerConstant.MEASURE_UNIT_SM,"原片不良"+PmManagerConstant.MEASURE_UNIT_SM,"耗用数"+PmManagerConstant.MEASURE_UNIT_SM,"实际使用数"+PmManagerConstant.MEASURE_UNIT_SM);
        res.setLegend(legends);
        //数据合计
        //领用总数
        BigDecimal receiveQuantity = BigDecimal.ZERO;
        for (BigDecimal value : receiveMap.values()) {
            receiveQuantity = BigDecimalUtil.add(receiveQuantity,value);
        }
        //耗用总数
        BigDecimal consumptionQuantity = BigDecimal.ZERO;
        for (BigDecimal value : consumptionMap.values()) {
            consumptionQuantity = BigDecimalUtil.add(consumptionQuantity,value);
        }
        //不良数
        BigDecimal badnessQuantity = BigDecimal.ZERO;
        for (BigDecimal value : badnessQuantityMap.values()) {
            badnessQuantity =  BigDecimalUtil.add(badnessQuantity,value);
        }
        TripleDTO<BigDecimal, BigDecimal, BigDecimal> tripleDTO = new TripleDTO<>();
        tripleDTO.setField1(receiveQuantity);
        tripleDTO.setField2(consumptionQuantity);
        tripleDTO.setField3(BigDecimalUtil.subtract(consumptionQuantity,badnessQuantity));
        res.setOther(tripleDTO);
        return res;
    }
}
