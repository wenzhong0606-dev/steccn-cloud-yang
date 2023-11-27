package com.steccn.pms.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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
import com.steccn.dp.feign.RemoteDpFeignApi;
import com.steccn.dp.vo.DpWeightVO;
import com.steccn.platform.constant.SysOfficeConstant;
import com.steccn.platform.dto.SysOfficeParentDTO;
import com.steccn.platform.feign.RemoteDictFeignApi;
import com.steccn.platform.feign.RemoteOfficeFeign;
import com.steccn.platform.feign.RemoteSpareFeignApi;
import com.steccn.platform.feign.RemoteSpareUnitConvertFeignApi;
import com.steccn.platform.vo.EqpSpareVO;
import com.steccn.platform.vo.SysDictVO;
import com.steccn.platform.vo.SysOfficeVO;
import com.steccn.platform.vo.SysSpareUnitConversionVO;
import com.steccn.pms.constant.PmManagerConstant;
import com.steccn.pms.domain.*;
import com.steccn.pms.dto.DashboardProducePageDTO;
import com.steccn.pms.service.*;
import com.steccn.pms.util.UnitConvertUtil;
import com.steccn.pms.vo.DashboardProducePageListVO;
import com.steccn.pms.vo.DashboardProducePageVO;
import io.swagger.annotations.ApiModelProperty;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.Lists;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
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

    @Override
    public BigDecimal pullForceToday(Integer workShopId, LocalDate date) {
        BigDecimal resVal = null;
        List<PmsPullForce> dbList = pmsPullForceService.list(new LambdaQueryWrapper<PmsPullForce>()
                .eq(PmsPullForce::getPullForceDate, date)
                .eq(PmsPullForce::getWorkshopId, workShopId)
                .select(PmsPullForce::getQuantity));
        if (CollUtil.isNotEmpty(dbList)) {
            List<BigDecimal> collect = dbList.stream().map(PmsPullForce::getQuantity).collect(Collectors.toList());
            resVal = BigDecimalUtil.sum(collect);
            resVal = BigDecimalUtil.setScale(resVal, 1);
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

    @Override
    public BigDecimal produceNumRate(Integer workShopId, LocalDate date) {
        BigDecimal num = produceNumToday(workShopId, date).get(0);
        BigDecimal pullForce = pullForceToday(workShopId, date);
        return BigDecimalUtil.percent(num, pullForce);
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
        BigDecimal num = inStorageToday(workShopId, date).get(0);
        BigDecimal pullForce = pullForceToday(workShopId, date);
        return BigDecimalUtil.percent(num, pullForce, 2);
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
}
