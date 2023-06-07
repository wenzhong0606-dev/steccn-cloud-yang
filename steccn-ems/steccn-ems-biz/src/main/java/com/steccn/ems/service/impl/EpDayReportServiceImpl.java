package com.steccn.ems.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.steccn.authserver.exception.BusinessException;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.common.constant.FeignConstant;
import com.steccn.common.dto.TripleDTO;
import com.steccn.common.dto.TupleDTO;
import com.steccn.common.util.BigDecimalUtil;
import com.steccn.common.util.ClassUtil;
import com.steccn.common.util.LocalDateUtil;
import com.steccn.ems.constant.EpManagerConstant;
import com.steccn.ems.service.*;
import com.steccn.ems.vo.*;
import com.steccn.platform.feign.RemoteSysConfigurationFeignApi;
import com.steccn.pms.feign.RemoteReportWorkFeignApi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * @author yequan
 * @desc 能耗日统计报表
 * @date 2023/6/1
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EpDayReportServiceImpl implements EpDayReportService {

    private final EpEnergyTypeService epEnergyTypeService;
    private final EpMeterReadDistantService epMeterReadDistantService;
    private final EpPartitionService epPartitionService;
    private final EpHeatPowerService epHeatPowerService;
    private final EpMeterReadService meterReadService;
    private final RemoteReportWorkFeignApi reportWorkFeignApi;
    private final RemoteSysConfigurationFeignApi remoteSysConfigurationFeignApi;

    @Override
    @Cacheable(cacheNames = "ems_count#86400", unless = "#flag == false")
    public EpDayReportCountVO count(LocalDate start, boolean flag) {
        EpDayReportCountVO resVo = new EpDayReportCountVO();
        //获取一个月最后一天--如果当前日期大于end，则只显示到end，否则至显示到当前日期
        LocalDate end = getEndDate(start);
        //获取表头
        resVo.setHead(countHead(start, end));
        //获取所有的能源类型
        List<EpEnergyTypeVO> energyTypeList = epEnergyTypeService.getEpEnergyTypeVOList();
        //获取指定的组织结构名称和主键
        Map<Integer, List<TripleDTO<Long, Integer, String>>> defaultNodeMap = epPartitionService.dayReportCountDefaultNode();
        //异步执行任务
        try {
            List<EpDayReportCountRowVO> dataList = asyncTask(start, end, energyTypeList, defaultNodeMap);
            resVo.setDataList(dataList);
        } catch (Exception e) {
            log.error("{}", e.getLocalizedMessage());
        }
        return resVo;
    }

    /**
     * 异步执行任务
     *
     * @param start          开始日期
     * @param end            结束日期
     * @param energyTypeList 能源类型
     * @param defaultNodeMap 默认节点map
     * @return
     * @throws ExecutionException
     * @throws InterruptedException
     */
    private List<EpDayReportCountRowVO> asyncTask(LocalDate start, LocalDate end, List<EpEnergyTypeVO> energyTypeList, Map<Integer, List<TripleDTO<Long, Integer, String>>> defaultNodeMap) throws ExecutionException, InterruptedException {
        List<EpDayReportCountRowVO> dataList = Lists.newArrayList();
        //获取全部厚度
        List<BigDecimal> allThickList = epMeterReadDistantService.selectAllThick(start, end);
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();

        CompletableFuture<List<EpDayReportCountRowVO>> elecFuture = CompletableFuture.supplyAsync(() -> {
            try {
                RequestContextHolder.setRequestAttributes(attributes);
                //计算电耗
                Optional<EpEnergyTypeVO> eleOp = energyTypeList.stream().filter(v -> EpManagerConstant.ENERGY_ELECTRIC.equals(v.getId())).findFirst();
                EpEnergyTypeVO elecEnergy = eleOp.get();
                List<EpDayReportCountRowVO> elecList = calElec(elecEnergy, start, end, defaultNodeMap.get(EpManagerConstant.ENERGY_ELECTRIC));
                //获取能源类型
                EpDayReportCountRowVO heatPowerRow = heatPowerRow(start, end, elecEnergy);
                //插入合计行
                elecList.add(heatPowerRow);
                EpDayReportCountRowVO sumRow = countRow(elecList, elecEnergy);
                elecList.add(sumRow);
                return elecList;
            } catch (Exception e) {
                log.error("elec -> {}", e.getLocalizedMessage());
                throw new BusinessException("业务执行异常");
            }
        }).exceptionally(ex -> Lists.newArrayList());

        CompletableFuture<List<EpDayReportCountRowVO>> oilFuture = CompletableFuture.supplyAsync(() -> {
            try {
                RequestContextHolder.setRequestAttributes(attributes);
                //获取用油量数据
                Optional<EpEnergyTypeVO> oilOp = energyTypeList.stream().filter(v -> EpManagerConstant.ENERGY_OIL.equals(v.getId())).findFirst();
                List<EpDayReportCountRowVO> oilRowList = consumeRow(oilOp.get(), start, end, defaultNodeMap.get(EpManagerConstant.ENERGY_OIL), allThickList);
                return oilRowList;
            } catch (Exception e) {
                log.error("oil -> {}", e.getLocalizedMessage());
                throw new BusinessException("业务执行异常");
            }
        }).exceptionally(ex -> Lists.newArrayList());

        CompletableFuture<List<EpDayReportCountRowVO>> gasFuture = CompletableFuture.supplyAsync(() -> {
            try {
                RequestContextHolder.setRequestAttributes(attributes);
                //获取用气量
                Optional<EpEnergyTypeVO> gasOp = energyTypeList.stream().filter(v -> EpManagerConstant.ENERGY_GAS.equals(v.getId())).findFirst();
                return consumeRow(gasOp.get(), start, end, defaultNodeMap.get(EpManagerConstant.ENERGY_GAS), allThickList);
            } catch (Exception e) {
                log.error("gas -> {}", e.getLocalizedMessage());
                throw new BusinessException("业务执行异常");
            }
        }).exceptionally(ex -> Lists.newArrayList());

        CompletableFuture<List<EpDayReportCountRowVO>> waterFuture = CompletableFuture.supplyAsync(() -> {
            try {
                RequestContextHolder.setRequestAttributes(attributes);
                //获取用水量
                Optional<EpEnergyTypeVO> waterOp = energyTypeList.stream().filter(v -> EpManagerConstant.ENERGY_WATER.equals(v.getId())).findFirst();
                return consumeRow(waterOp.get(), start, end, defaultNodeMap.get(EpManagerConstant.ENERGY_WATER), allThickList);
            } catch (Exception e) {
                log.error("water -> {}", e.getLocalizedMessage());
                throw new BusinessException("业务执行异常");
            }
        }).exceptionally(ex -> Lists.newArrayList());
        //4个都执行完毕，才来执行继续
        CompletableFuture<Void> completableFuture = CompletableFuture.allOf(elecFuture, oilFuture, gasFuture, waterFuture);
        completableFuture.isDone();
        dataList.addAll(elecFuture.get());
        dataList.addAll(oilFuture.get());
        dataList.addAll(gasFuture.get());
        dataList.addAll(waterFuture.get());
        return dataList;
    }

    /**
     * 根据开始时间获取结束时间
     *
     * @param start 开始时间
     * @return
     */
    private LocalDate getEndDate(LocalDate start) {
        LocalDateTime endLdt = LocalDateUtil.getLastDayOfMonthStr(start);
        LocalDate end = endLdt.toLocalDate();
        LocalDate now = LocalDate.now();
        if (end.isAfter(now)) {
            end = now;
        }
        return end;
    }

    /**
     * 获取余热发电行
     *
     * @param start  开始日期
     * @param end    结束日期
     * @param energy 能源类型
     * @return
     */
    private EpDayReportCountRowVO heatPowerRow(LocalDate start, LocalDate end, EpEnergyTypeVO energy) {
        //计算余热发电
        List<BigDecimal> heatPowerList = epHeatPowerService.heatPower(start, end);
        EpDayReportCountRowVO heatPowerRow = new EpDayReportCountRowVO();
        List<BigDecimal> heatPowerListNew = Lists.newArrayList();
        heatPowerList.forEach(ele -> heatPowerListNew.add(BigDecimalUtil.setScale(ele, 0)));
        heatPowerRow.setConsumeList(heatPowerListNew);
        heatPowerRow.setEnergyType(energy.getName());
        heatPowerRow.setDepartmentName("余热发电");
        heatPowerRow.setUnit(energy.getMeasureUnitName());
        BigDecimal heatPowerSum = BigDecimalUtil.sum(heatPowerList);
        heatPowerRow.setSumStr(BigDecimalUtil.setScale(heatPowerSum, 0));
        return heatPowerRow;
    }

    /**
     * 插入合计行
     *
     * @param dataList 数据集合
     * @param energy   能源类型
     * @return
     */
    private EpDayReportCountRowVO countRow(List<EpDayReportCountRowVO> dataList, EpEnergyTypeVO energy) {
        //对行进行合计
        List<BigDecimal> countRowList = Lists.newArrayList();
        for (EpDayReportCountRowVO rowVO : dataList) {
            List<BigDecimal> consumeList = rowVO.getConsumeList();
            for (int i = 0; i < consumeList.size(); i++) {
                if (i > countRowList.size() - 1) {
                    countRowList.add(i, null);
                }
                BigDecimal add = BigDecimalUtil.add(countRowList.get(i), consumeList.get(i));
                countRowList.set(i, BigDecimalUtil.setScale(add, 0));
            }
        }
        EpDayReportCountRowVO countRow = new EpDayReportCountRowVO();
        countRow.setConsumeList(countRowList);
        countRow.setEnergyType(energy.getName());
        countRow.setUnit(energy.getMeasureUnitName());
        countRow.setDepartmentName("合计");
        BigDecimal countRowValue = BigDecimalUtil.sum(countRowList);
        countRow.setSumStr(BigDecimalUtil.setScale(countRowValue, 0));
        return countRow;
    }

    /**
     * 计算电
     *
     * @param epEnergy      能源类型
     * @param start         开始日期
     * @param end           结束日期
     * @param partitionList 所有节点的集合
     * @return
     */
    private List<EpDayReportCountRowVO> calElec(EpEnergyTypeVO epEnergy, LocalDate start, LocalDate end, List<TripleDTO<Long, Integer, String>> partitionList) {
        List<EpDayReportCountRowVO> resList = Lists.newArrayList();
        //获取配置map
        Map<String, List<Integer>> confMap = FeignOptional.of(() -> remoteSysConfigurationFeignApi.confListInteger(new TupleDTO<>(EpManagerConstant.CONF_EMS_DAY_COUNT_ELEC, EpManagerConstant.ELECConfContentEnum.allContentConf())), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        //基片制造部
        List<Integer> jpzzbList = confMap.getOrDefault(EpManagerConstant.ELECConfContentEnum.JPZZB_LIST.toString(), Lists.newArrayList());
        //深加工四条产线【一二三四】
        List<Integer> sjgLine14List = confMap.getOrDefault(EpManagerConstant.ELECConfContentEnum.SJG_LINE14_LIST.toString(), Lists.newArrayList());
        //深加工四条产线【五六七八】
        List<Integer> sjgLine58List = confMap.getOrDefault(EpManagerConstant.ELECConfContentEnum.SJG_LINE58_LIST.toString(), Lists.newArrayList());
        //员工餐厅和宿舍】
        List<Integer> diningList = confMap.getOrDefault(EpManagerConstant.ELECConfContentEnum.DINING_AND_ROOM_LIST.toString(), Lists.newArrayList());
        //公用电【深加工二车间（东线）->公用电,深加工二车间（西线）->公用电】
        List<Integer> gydList = confMap.getOrDefault(EpManagerConstant.ELECConfContentEnum.GYD_LIST.toString(), Lists.newArrayList());
        //风冷螺杆式空压机6 + 风冷螺杆式空压机7 + 风冷螺杆式空压机8 + 风冷螺杆式空压机9 + 风冷螺杆式空压机10 + 微热吸附式干燥器3
        List<Integer> fl610List = confMap.getOrDefault(EpManagerConstant.ELECConfContentEnum.FL610_LIST.toString(), Lists.newArrayList());
        //风冷螺杆式空压机1 + 风冷螺杆式空压机2 + 风冷螺杆式空压机3 + 风冷螺杆式空压机4 + 风冷螺杆式空压机5 + 微热吸附式干燥器1 + 微热吸附式干燥器2
        List<Integer> fl15List = confMap.getOrDefault(EpManagerConstant.ELECConfContentEnum.FL15_LIST.toString(), Lists.newArrayList());
        //大数据控制中心
        List<Integer> dsjList = confMap.getOrDefault(EpManagerConstant.ELECConfContentEnum.DSJ_LIST.toString(), Lists.newArrayList());
        //深加工8条产线
        List<Integer> sjgLineList = Lists.newArrayList();
        sjgLine14List.forEach(ele -> sjgLineList.add(ele));
        sjgLine58List.forEach(ele -> sjgLineList.add(ele));

        LocalDateTime startLdt = LocalDateTime.of(start, ClassUtil.MORNING_START_TIME);
        LocalDateTime endLdt = LocalDateTime.of(end.plusDays(1), ClassUtil.MORNING_START_TIME.minusNanos(1));
        //②时间区间内所有班次集
        List<TupleDTO<LocalDate, Integer>> classRangeList = ClassUtil.classRange(startLdt, endLdt);
        Set<LocalDate> dateSet = classRangeList.stream().map(v -> v.getField1()).collect(Collectors.toSet());
        List<Long> partitionIdList = partitionList.stream().map(TupleDTO::getField1).collect(Collectors.toList());
        //获取全量数据表
        List<EpMeterReadAllDataVO> classDateAllData = meterReadService.classDateAllData(dateSet, epEnergy.getId(), partitionIdList);
        //每个节点，每天的数据
        //基片制造部=b1+b2+b3
        List<EpMeterReadAllDataVO> jpList = classDateAllData.stream().filter(v -> jpzzbList.contains(v.getSysOfficeId())).collect(Collectors.toList());
        List<LocalDate> timeList = dateSet.stream().sorted(Comparator.comparing(x -> x)).collect(Collectors.toList());
        //构建基片制造部
        EpDayReportCountRowVO jpRow = new EpDayReportCountRowVO();
        List<BigDecimal> jpValueList = Lists.newArrayList();

        Map<LocalDate, List<BigDecimal>> jpResMap = Maps.newHashMap();
        for (LocalDate ele : timeList) {
            List<BigDecimal> timeDataList = jpList.stream().filter(v -> v.getReadDate().isEqual(ele)).map(EpMeterReadAllDataVO::getConsume).collect(Collectors.toList());
            BigDecimal reduce = BigDecimalUtil.sum(timeDataList);
            jpResMap.put(ele, Lists.newArrayList(reduce));
        }
        jpRow.setConsumeList(jpValueList);
        jpRow.setUnit(epEnergy.getMeasureUnitName());
        jpRow.setEnergyType(epEnergy.getName());
        jpRow.setDepartmentName("基片");

        //构建生活用电 = 员工餐厅 + 员工宿舍
        EpDayReportCountRowVO shRow = new EpDayReportCountRowVO();
        List<EpMeterReadAllDataVO> shList = classDateAllData.stream().filter(v -> diningList.contains(v.getSysOfficeId())).collect(Collectors.toList());
        List<BigDecimal> shValueList = Lists.newArrayList();
        for (LocalDate ele : timeList) {
            List<BigDecimal> timeDataList = shList.stream().filter(v -> v.getReadDate().isEqual(ele)).map(EpMeterReadAllDataVO::getConsume).collect(Collectors.toList());
            BigDecimal reduce = BigDecimalUtil.sum(timeDataList);
            shValueList.add(BigDecimalUtil.setScale(reduce, 0));
        }
        BigDecimal shSum = BigDecimalUtil.sum(shValueList);
        shRow.setSumStr(BigDecimalUtil.setScale(shSum, 0));
        shRow.setConsumeList(shValueList);
        shRow.setUnit(epEnergy.getMeasureUnitName());
        shRow.setEnergyType(epEnergy.getName());
        shRow.setDepartmentName("生活用电");

        //深加工一~八产线
        List<TripleDTO<Long, Integer, String>> linePartitionList = partitionList.stream().filter(v -> sjgLine14List.contains(v.getField2()) || sjgLine58List.contains(v.getField2())).collect(Collectors.toList());
        Set<Integer> lineOfficeIdSet = Sets.newHashSet();
        Map<String, List<BigDecimal>> lineResMap = Maps.newHashMap();

        List<EpDayReportCountRowVO> lineRowList = Lists.newArrayList();
        for (TripleDTO<Long, Integer, String> line : linePartitionList) {
            //获取产线id集合
            lineOfficeIdSet.add(line.getField2());
            List<EpMeterReadAllDataVO> lineList = classDateAllData.stream().filter(v -> line.getField1().equals(v.getOfficeId())).collect(Collectors.toList());
            //构建基片制造部
            EpDayReportCountRowVO lineRow = new EpDayReportCountRowVO();
            List<BigDecimal> lineValueList = Lists.newArrayList();
            for (LocalDate ele : timeList) {
                List<BigDecimal> timeDataList = lineList.stream().filter(v -> v.getReadDate().isEqual(ele)).map(EpMeterReadAllDataVO::getConsume).collect(Collectors.toList());
                BigDecimal timeValue = BigDecimalUtil.sum(timeDataList);
                //a1.1 a2.1 a3.1 a4.1 a5.1 a6.1 a7.1 a8.1
                lineResMap.put(mapKey(line.getField2(), ele), Lists.newArrayList(timeValue));
            }
            lineRow.setConsumeList(lineValueList);
            lineRow.setUnit(epEnergy.getMeasureUnitName());
            lineRow.setEnergyType(epEnergy.getName());
            lineRow.setDepartmentName(line.getField3());
            lineRow.setOfficeId(line.getField2());
            lineRowList.add(lineRow);
        }
        //基片，产线行，生活用电
        resList.add(jpRow);
        resList.addAll(lineRowList);
        resList.add(shRow);

        //获取几条产线的产量及对应比例，用于分摊公用电
        Map<String, BigDecimal> productNumMap = FeignOptional.of(() -> reportWorkFeignApi.classDateProductNum(new TripleDTO<>(lineOfficeIdSet, start, end)), FeignConstant.STECCN_DATA_PLATFORM_SERVICE_NAME).get();
        TupleDTO<Map<String, BigDecimal>, Map<String, BigDecimal>> tupleDTO = getProductNumRateMap(sjgLine14List, sjgLine58List, timeList, productNumMap);
        Map<String, BigDecimal> productNumLineRateMap = tupleDTO.getField1();
        Map<String, BigDecimal> productNumRateMap = tupleDTO.getField2();

        //获取公用电
        gydRate(classDateAllData, gydList, Lists.newArrayList(sjgLine14List, sjgLine58List), timeList, productNumLineRateMap, lineResMap);

        //获取风冷螺杆式空压机6 + 风冷螺杆式空压机7 + 风冷螺杆式空压机8 + 风冷螺杆式空压机9 + 风冷螺杆式空压机10 + 微热吸附式干燥器3
        //a1.3 a2.3 a3.3 a4.3 a5.3 a6.3 a7.3 a8.3
        fl610(classDateAllData, fl610List, sjgLineList, timeList, productNumRateMap, lineResMap);

        //获取风冷螺杆式空压机1 + 风冷螺杆式空压机2 + 风冷螺杆式空压机3 + 风冷螺杆式空压机4 + 风冷螺杆式空压机5 + 微热吸附式干燥器1 + 微热吸附式干燥器2
        //获取b2
        fl15(classDateAllData, fl15List, timeList, jpResMap);

        //获取总能耗N 按b1、a1.1 + a1.2 + a1.3、a2.1 + a2.2 + a2.3、... 、a8.1 + a8.2 + a8.3按照比例分摊
        //N分摊为b3、a1.4、a2.4、... 、a8.4共9个值
        Map<String, BigDecimal> nRateMap = getNRateMap(sjgLineList, timeList, jpResMap, lineResMap);
        dsj(classDateAllData, dsjList, sjgLineList, timeList, nRateMap, jpResMap, lineResMap);

        //重新计算基片行和产线行
        ftJpRow(jpRow, timeList, jpResMap);
        ftLineRow(lineRowList, timeList, lineResMap);

        return resList;
    }

    /**
     * 产线行分摊-数据+合计调整
     *
     * @param lineRowList 产线行集合
     * @param timeList    班次时间集合
     * @param lineResMap  产线数据集合
     */
    private void ftLineRow(List<EpDayReportCountRowVO> lineRowList, List<LocalDate> timeList, Map<String, List<BigDecimal>> lineResMap) {
        for (EpDayReportCountRowVO row : lineRowList) {
            BigDecimal rowSum = BigDecimal.ZERO;
            List<BigDecimal> consumeList = row.getConsumeList();
            for (int i = 0; i < timeList.size(); i++) {
                List<BigDecimal> valueList = lineResMap.get(mapKey(row.getOfficeId(), timeList.get(i)));
                BigDecimal sum = BigDecimalUtil.sum(valueList);
                BigDecimal format = BigDecimalUtil.setScale(sum, 0);
                consumeList.add(i, format);
                rowSum = BigDecimalUtil.add(format, rowSum);
            }
            row.setSumStr(BigDecimalUtil.setScale(rowSum, 0));
        }
    }

    /**
     * 重新计算分摊的行-数据+合计调整
     *
     * @param jpRow
     * @param timeList
     * @param jpResMap
     */
    private void ftJpRow(EpDayReportCountRowVO jpRow, List<LocalDate> timeList, Map<LocalDate, List<BigDecimal>> jpResMap) {
        List<BigDecimal> consumeList = jpRow.getConsumeList();
        BigDecimal rowSum = BigDecimal.ZERO;
        for (int i = 0; i < timeList.size(); i++) {
            List<BigDecimal> valueList = jpResMap.get(timeList.get(i));
            BigDecimal sum = BigDecimalUtil.sum(valueList);
            BigDecimal format = BigDecimalUtil.setScale(sum, 0);
            consumeList.add(i, format);
            rowSum = BigDecimalUtil.add(format, rowSum);
        }
        jpRow.setSumStr(BigDecimalUtil.setScale(rowSum, 0));
    }

    /**
     * 基于N按b1、a1.1 + a1.2 + a1.3、a2.1 + a2.2 + a2.3、... 、a8.1 + a8.2 + a8.3共9部分的比例分摊，则N分摊为b3、a1.4、a2.4、... 、a8.4共9个值
     *
     * @param classDateAllData 全量数据表
     * @param dsjList          大数据节点
     * @param sjgLineList      深加工产线节点
     * @param timeList         时间集合
     * @param nRateMap         分摊比例map
     * @param jpResMap         基片map
     * @param lineResMap       深加工产线map
     */
    private void dsj(List<EpMeterReadAllDataVO> classDateAllData, List<Integer> dsjList, List<Integer> sjgLineList, List<LocalDate> timeList, Map<String, BigDecimal> nRateMap, Map<LocalDate, List<BigDecimal>> jpResMap, Map<String, List<BigDecimal>> lineResMap) {
        List<EpMeterReadAllDataVO> dsjDataList = classDateAllData.stream().filter(v -> dsjList.contains(v.getSysOfficeId())).collect(Collectors.toList());
        for (LocalDate ele : timeList) {
            List<BigDecimal> timeDataList = dsjDataList.stream().filter(v -> v.getReadDate().isEqual(ele)).map(EpMeterReadAllDataVO::getConsume).collect(Collectors.toList());
            BigDecimal dsjValue = BigDecimalUtil.sum(timeDataList);
            //基片分摊
            BigDecimal jpValue = BigDecimalUtil.multiply(dsjValue, nRateMap.get(mapKey(-1, ele)));
            jpResMap.get(ele).add(jpValue);
            //产线分摊value
            for (Integer line : sjgLineList) {
                BigDecimal lineValue = BigDecimalUtil.multiply(dsjValue, nRateMap.get(mapKey(line, ele)));
                lineResMap.get(mapKey(line, ele)).add(lineValue);
            }
        }
    }

    /**
     * N按b1、a1.1 + a1.2 + a1.3、a2.1 + a2.2 + a2.3、... 、a8.1 + a8.2 + a8.3共9部分的比例分摊，
     * 则N分摊为b3、a1.4、a2.4、... 、a8.4共9个值
     *
     * @param sjgLineList 深加工产线集合
     * @param timeList    班次日期集合
     * @param jpResMap    基片制造部数据
     * @param lineResMap  产线数据
     * @return 分摊比例map
     */
    private Map<String, BigDecimal> getNRateMap(List<Integer> sjgLineList, List<LocalDate> timeList, Map<LocalDate, List<BigDecimal>> jpResMap, Map<String, List<BigDecimal>> lineResMap) {
        Map<String, BigDecimal> resMap = Maps.newHashMap();
        for (LocalDate classDate : timeList) {
            BigDecimal b1 = jpResMap.get(classDate).get(0);
            BigDecimal sum = b1;
            Map<Integer, BigDecimal> lineDataMap = Maps.newHashMap();

            for (Integer line : sjgLineList) {
                List<BigDecimal> lineDataList = lineResMap.get(mapKey(line, classDate));
                //ax.1 + ax.2 + ax.3
                BigDecimal lineDaySum = BigDecimalUtil.sum(lineDataList);
                sum = BigDecimalUtil.add(sum, lineDaySum);
                lineDataMap.put(line, lineDaySum);
            }
            //基片制造部比例
            resMap.put(mapKey(-1, classDate), BigDecimalUtil.divide(b1, sum));
            //深加工产线比例
            for (Map.Entry<Integer, BigDecimal> entry : lineDataMap.entrySet()) {
                resMap.put(mapKey(entry.getKey(), classDate), BigDecimalUtil.divide(entry.getValue(), sum));
            }
        }
        return resMap;
    }

    /**
     * 获取b2
     *
     * @param classDateAllData 全量数据表
     * @param fl15             风冷螺杆式空压机1 + 风冷螺杆式空压机2 + 风冷螺杆式空压机3 + 风冷螺杆式空压机4 + 风冷螺杆式空压机5 + 微热吸附式干燥器1 + 微热吸附式干燥器2
     * @param timeList         时间集合
     * @param jpResMap         基片的数据集合
     */
    private void fl15(List<EpMeterReadAllDataVO> classDateAllData, List<Integer> fl15, List<LocalDate> timeList, Map<LocalDate, List<BigDecimal>> jpResMap) {
        List<EpMeterReadAllDataVO> fl610DataList = classDateAllData.stream().filter(v -> fl15.contains(v.getSysOfficeId())).collect(Collectors.toList());
        for (LocalDate ele : timeList) {
            List<BigDecimal> timeDataList = fl610DataList.stream().filter(v -> v.getReadDate().isEqual(ele)).map(EpMeterReadAllDataVO::getConsume).collect(Collectors.toList());
            BigDecimal b2 = BigDecimalUtil.sum(timeDataList);
            jpResMap.get(ele).add(b2);
        }
    }

    /**
     * 查询的6个总能耗值加总记为M 分摊到产线
     *
     * @param classDateAllData  全量数据表
     * @param fl610             风冷螺杆式空压机6 + 风冷螺杆式空压机7 + 风冷螺杆式空压机8 + 风冷螺杆式空压机9 + 风冷螺杆式空压机10 + 微热吸附式干燥器3
     * @param sjgLineList       深加工产线集合
     * @param timeList          时间集合
     * @param productNumRateMap 产量比例集合
     * @param lineResMap        产线数据集合
     */
    private void fl610(List<EpMeterReadAllDataVO> classDateAllData, List<Integer> fl610, List<Integer> sjgLineList, List<LocalDate> timeList, Map<String, BigDecimal> productNumRateMap, Map<String, List<BigDecimal>> lineResMap) {
        List<EpMeterReadAllDataVO> fl610DataList = classDateAllData.stream().filter(v -> fl610.contains(v.getSysOfficeId())).collect(Collectors.toList());
        for (LocalDate ele : timeList) {
            List<BigDecimal> timeDataList = fl610DataList.stream().filter(v -> v.getReadDate().isEqual(ele)).map(EpMeterReadAllDataVO::getConsume).collect(Collectors.toList());
            BigDecimal reduce = BigDecimalUtil.sum(timeDataList);
            //a1.3 a2.3 a3.3 a4.3 a5.3 a6.3 a7.3 a8.3
            for (Integer sjgLine : sjgLineList) {
                String mapKey = mapKey(sjgLine, ele);
                BigDecimal rateValue = BigDecimalUtil.multiply(reduce, productNumRateMap.get(mapKey));
                lineResMap.get(mapKey).add(rateValue);
            }
        }
    }

    /**
     * 公用电 对应2个集合
     *
     * @param classDateAllData  全量数据表
     * @param gydList           公用电集合
     * @param lineList          产线集合【1-4 ，5-8】
     * @param timeList          时间集合
     * @param productNumRateMap 产量集合
     * @param lineResMap        产线返回集合
     */
    private void gydRate(List<EpMeterReadAllDataVO> classDateAllData, List<Integer> gydList, List<List<Integer>> lineList, List<LocalDate> timeList, Map<String, BigDecimal> productNumRateMap, Map<String, List<BigDecimal>> lineResMap) {
        for (int i = 0; i < gydList.size(); i++) {
            int finalI = i;
            List<EpMeterReadAllDataVO> gydDataList = classDateAllData.stream().filter(v -> gydList.get(finalI).equals(v.getSysOfficeId())).collect(Collectors.toList());
            //a1.2 a2.2 a3.2 a4.2
            //a5.2 a6.2 a7.2 a8.2
            for (LocalDate ele : timeList) {
                List<BigDecimal> timeDataList = gydDataList.stream().filter(v -> v.getReadDate().isEqual(ele)).map(EpMeterReadAllDataVO::getConsume).collect(Collectors.toList());
                BigDecimal reduce = BigDecimalUtil.sum(timeDataList);
                for (Integer sjgLine : lineList.get(i)) {
                    String mapKey = mapKey(sjgLine, ele);
                    BigDecimal rateValue = BigDecimalUtil.multiply(reduce, productNumRateMap.get(mapKey));
                    lineResMap.get(mapKey).add(rateValue);
                }
            }
        }
    }


    /**
     * 获取产量比例的map
     *
     * @param sjgLine14List 深加工四条产线【一二三四】
     * @param sjgLine58List 深加工四条产线【五六七八】
     * @param timeList      班次日期集合
     * @param productNumMap 产量集合
     * @return
     */
    private TupleDTO<Map<String, BigDecimal>, Map<String, BigDecimal>> getProductNumRateMap(List<Integer> sjgLine14List, List<Integer> sjgLine58List, List<LocalDate> timeList, Map<String, BigDecimal> productNumMap) {
        Map<String, BigDecimal> productNumLineRateMap = Maps.newHashMap();
        Map<String, BigDecimal> productNumRateMap = Maps.newHashMap();

        //深加工8条产线
        List<Integer> sjgLineList = Lists.newArrayList();
        sjgLine14List.forEach(ele -> sjgLineList.add(ele));
        sjgLine58List.forEach(ele -> sjgLineList.add(ele));

        //获取几条产线的每日产量对比
        for (LocalDate classDate : timeList) {
            //深加工四条产线【一二三四】
            BigDecimal sum14 = BigDecimal.ZERO;
            for (Integer line : sjgLine14List) {
                sum14 = BigDecimalUtil.add(sum14, productNumMap.getOrDefault(mapKey(line, classDate), BigDecimal.ZERO));
            }
            for (Integer line : sjgLine14List) {
                String mapKey = mapKey(line, classDate);
                BigDecimal num = productNumMap.getOrDefault(mapKey, BigDecimal.ZERO);
                productNumLineRateMap.put(mapKey, BigDecimalUtil.divide(num, sum14));
            }
            //深加工四条产线【五六七八】
            BigDecimal sum58 = BigDecimal.ZERO;
            for (Integer line : sjgLine58List) {
                sum58 = BigDecimalUtil.add(sum58, productNumMap.getOrDefault(mapKey(line, classDate), BigDecimal.ZERO));
            }
            for (Integer line : sjgLine58List) {
                String mapKey = mapKey(line, classDate);
                BigDecimal num = productNumMap.getOrDefault(mapKey, BigDecimal.ZERO);
                productNumLineRateMap.put(mapKey, BigDecimalUtil.divide(num, sum58));
            }
            //深加工八条产线【一二三四五六七八】
            BigDecimal sum = BigDecimal.ZERO;
            for (Integer line : sjgLineList) {
                sum = BigDecimalUtil.add(sum, productNumMap.getOrDefault(mapKey(line, classDate), BigDecimal.ZERO));
            }
            for (Integer line : sjgLineList) {
                String mapKey = mapKey(line, classDate);
                BigDecimal num = productNumMap.getOrDefault(mapKey, BigDecimal.ZERO);
                productNumRateMap.put(mapKey, BigDecimalUtil.divide(num, sum));
            }
        }
        return new TupleDTO<>(productNumLineRateMap, productNumRateMap);
    }

    /**
     * 产线和日期拼接成key
     *
     * @param line
     * @param date
     * @return
     */
    private String mapKey(Integer line, LocalDate date) {
        StringBuilder sb = new StringBuilder();
        sb.append(line).append("_").append(date);
        return sb.toString();
    }

    /**
     * 获取能耗行
     *
     * @param epEnergy      能源类型
     * @param start         开始日期
     * @param end           结束日期
     * @param partitionList 节点集合
     * @return
     */
    private List<EpDayReportCountRowVO> consumeRow(EpEnergyTypeVO epEnergy, LocalDate start, LocalDate end, List<TripleDTO<Long, Integer, String>> partitionList, List<BigDecimal> allThickList) {
        List<EpDayReportCountRowVO> rowList = Lists.newArrayList();
        if (CollUtil.isEmpty(partitionList) || Objects.isNull(epEnergy)) {
            return rowList;
        }

        //查询全量数据表
        LocalDateTime startLdt = LocalDateTime.of(start, ClassUtil.MORNING_START_TIME);
        LocalDateTime endLdt = LocalDateTime.of(end.plusDays(1), ClassUtil.MORNING_START_TIME.minusNanos(1));
        //②时间区间内所有班次集
        List<TupleDTO<LocalDate, Integer>> classRangeList = ClassUtil.classRange(startLdt, endLdt);
        Set<LocalDate> dateSet = classRangeList.stream().map(v -> v.getField1()).collect(Collectors.toSet());
        List<Long> partitionIdList = partitionList.stream().map(TupleDTO::getField1).collect(Collectors.toList());
        //获取全量数据表
        List<EpMeterReadAllDataVO> classDateAllData = meterReadService.classDateAllData(dateSet, epEnergy.getId(), partitionIdList);

        for (TripleDTO<Long, Integer, String> tripleDTO : partitionList) {
            EpDayReportCountRowVO row = new EpDayReportCountRowVO();
            List<BigDecimal> consumeList = Lists.newArrayList();
            List<EpMeterReadAllDataVO> dataList = classDateAllData.stream().filter(v -> v.getOfficeId().equals(tripleDTO.getField1())).collect(Collectors.toList());
            List<LocalDate> timeList = dateSet.stream().sorted(Comparator.comparing(x -> x)).collect(Collectors.toList());
            for (LocalDate classDate : timeList) {
                List<BigDecimal> valueList = dataList.stream().filter(v -> v.getReadDate().isEqual(classDate)).map(EpMeterReadAllDataVO::getConsume).collect(Collectors.toList());
                BigDecimal dataValue = BigDecimalUtil.sum(valueList);
                consumeList.add(BigDecimalUtil.setScale(dataValue, 0));
            }
            row.setConsumeList(consumeList);
            row.setEnergyType(epEnergy.getName());
            row.setUnit(epEnergy.getMeasureUnitName());
            row.setDepartmentName(tripleDTO.getField3());
            BigDecimal sum = BigDecimalUtil.sum(consumeList);
            row.setSumStr(BigDecimalUtil.setScale(sum, 0));
            rowList.add(row);
        }

        //如果超过一行，则添加一个合计行
        if (rowList.size() > 1) {
            EpDayReportCountRowVO countRow = countRow(rowList, epEnergy);
            rowList.add(countRow);
        }
        return rowList;
    }

    /**
     * 获取表头
     *
     * @param start 开始时间
     * @param end   结束时间
     * @return
     */
    private EpDayReportCountHeadVO countHead(LocalDate start, LocalDate end) {
        //获取一个月的所有日期
        List<LocalDate> dateList = LocalDateUtil.getLocalDateByStartEndDate(start, end);
        EpDayReportCountHeadVO head = new EpDayReportCountHeadVO();
        head.setEnergyType("能源类型");
        head.setUnit("单位");
        head.setDepartmentName("部门");
        List<String> dateStrList = Lists.newArrayList();
        dateList.forEach(ele -> dateStrList.add(LocalDateTimeUtil.format(ele, "MM月dd日")));
        head.setConsumeList(dateStrList);
        head.setSumStr("总计");
        return head;
    }
}
