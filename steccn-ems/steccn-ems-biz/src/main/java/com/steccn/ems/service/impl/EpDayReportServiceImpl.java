package com.steccn.ems.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.common.constant.FeignConstant;
import com.steccn.common.dto.TripleDTO;
import com.steccn.common.dto.TupleDTO;
import com.steccn.common.util.BigDecimalUtil;
import com.steccn.common.util.ClassUtil;
import com.steccn.common.util.LocalDateUtil;
import com.steccn.common.vo.EpChartGraphVO;
import com.steccn.common.vo.EpChartGraphXAxisVO;
import com.steccn.ems.constant.EpManagerConstant;
import com.steccn.ems.dto.EpTableDTO;
import com.steccn.ems.service.*;
import com.steccn.ems.vo.*;
import com.steccn.pms.feign.RemoteReportWorkFeignApi;
import com.steccn.qms.constant.QtManagerConstant;
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
 * @desc
 * @date 2023/6/1
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EpDayReportServiceImpl implements EpDayReportService {

    private final EpConsumePartitionChartService epConsumePartitionChartService;
    private final EpEnergyTypeService epEnergyTypeService;
    private final EpMeterReadDistantService epMeterReadDistantService;
    private final EpPartitionService epPartitionService;
    private final EpHeatPowerService epHeatPowerService;
    private final EpMeterReadService meterReadService;
    private final RemoteReportWorkFeignApi reportWorkFeignApi;

    @Override
    public EpDayReportCountVO count(LocalDate start) {

        EpDayReportCountVO resVo = new EpDayReportCountVO();
        List<EpDayReportCountRowVO> dataList = Lists.newArrayList();
        //获取一个月最后一天--如果当前日期大于end，则只显示到end，否则至显示到当前日期
        LocalDateTime endLdt = LocalDateUtil.getLastDayOfMonthStr(start);
        LocalDate end = endLdt.toLocalDate();
        LocalDate now = LocalDate.now();
        if (end.isAfter(now)) {
            end = now;
        }
        //获取表头
        resVo.setHead(countHead(start, end));
        //获取所有的能源类型
        List<EpEnergyTypeVO> energyTypeList = epEnergyTypeService.getEpEnergyTypeVOList();

        //获取全部厚度
        List<BigDecimal> allThickList = epMeterReadDistantService.selectAllThick(start, end);

        //获取指定的组织结构名称和主键
        Map<Integer, List<TripleDTO<Long, Integer, String>>> defaultNodeMap = epPartitionService.dayReportCountDefaultNode();

        //计算电耗
        Optional<EpEnergyTypeVO> eleOp = energyTypeList.stream().filter(v -> EpManagerConstant.ENERGY_ELECTRIC.equals(v.getId())).findFirst();
        List<EpDayReportCountRowVO> elecList = calElec(eleOp.get(), start, end, defaultNodeMap.get(EpManagerConstant.ENERGY_ELECTRIC));
        dataList.addAll(elecList);

        //计算余热发电
        List<BigDecimal> heatPowerList = epHeatPowerService.heatPower(start, end);
        //获取能源类型
        EpEnergyTypeVO eleEnergy = eleOp.get();
        EpDayReportCountRowVO heatPowerRow = new EpDayReportCountRowVO();
        List<BigDecimal> heatPowerListNew = Lists.newArrayList();
        heatPowerList.forEach(ele -> heatPowerListNew.add(BigDecimalUtil.setScale(ele, 0)));
        heatPowerRow.setConsumeList(heatPowerListNew);
        heatPowerRow.setEnergyType(eleEnergy.getName());
        heatPowerRow.setDepartmentName("余热发电");
        heatPowerRow.setUnit(eleEnergy.getMeasureUnitName());
        BigDecimal reduce = heatPowerList.stream().filter(v -> Objects.nonNull(v)).reduce(BigDecimal.ZERO, BigDecimal::add);
        heatPowerRow.setSumStr(BigDecimalUtil.setScale(reduce, 0));
        dataList.add(heatPowerRow);

        try {
            //获取用油量数据
            Optional<EpEnergyTypeVO> oilOp = energyTypeList.stream().filter(v -> EpManagerConstant.ENERGY_OIL.equals(v.getId())).findFirst();
            List<EpDayReportCountRowVO> oilRowList = consumeRow(oilOp.get(), start, end, defaultNodeMap.get(EpManagerConstant.ENERGY_OIL), allThickList);
            dataList.addAll(oilRowList);
        } catch (Exception e) {
            log.error("{}", e.getLocalizedMessage());
        }
        try {
            //获取用气量
            Optional<EpEnergyTypeVO> gasOp = energyTypeList.stream().filter(v -> EpManagerConstant.ENERGY_GAS.equals(v.getId())).findFirst();
            List<EpDayReportCountRowVO> gasRowList = consumeRow(gasOp.get(), start, end, defaultNodeMap.get(EpManagerConstant.ENERGY_GAS), allThickList);
            dataList.addAll(gasRowList);
        } catch (Exception e) {
            log.error("{}", e.getLocalizedMessage());
        }
        try {
            //获取用水量
            Optional<EpEnergyTypeVO> waterOp = energyTypeList.stream().filter(v -> EpManagerConstant.ENERGY_WATER.equals(v.getId())).findFirst();
            List<EpDayReportCountRowVO> waterRowList = consumeRow(waterOp.get(), start, end, defaultNodeMap.get(EpManagerConstant.ENERGY_WATER), allThickList);
            dataList.addAll(waterRowList);
        } catch (Exception e) {
            log.error("{}", e.getLocalizedMessage());
        }

        resVo.setDataList(dataList);
        return resVo;
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
        //深加工四条产线【一二三四】
        List<Integer> sjgLine14List = Lists.newArrayList(176, 185, 186, 187);
        //深加工四条产线【五六七八】
        List<Integer> sjgLine58List = Lists.newArrayList(181, 182, 183, 184);
        //员工餐厅和宿舍】
        List<Integer> diningList = Lists.newArrayList(652, 653);
        //公用电【深加工二车间（东线）->公用电,深加工二车间（西线）->公用电】
        List<Integer> gydList = Lists.newArrayList(633, 3653);
        //风冷螺杆式空压机6 + 风冷螺杆式空压机7 + 风冷螺杆式空压机8 + 风冷螺杆式空压机9 + 风冷螺杆式空压机10 + 微热吸附式干燥器3
        List<Integer> fl610 = Lists.newArrayList(666, 667, 668, 669, 670, 673);
        //风冷螺杆式空压机1 + 风冷螺杆式空压机2 + 风冷螺杆式空压机3 + 风冷螺杆式空压机4 + 风冷螺杆式空压机5 + 微热吸附式干燥器1 + 微热吸附式干燥器2
        List<Integer> fl15 = Lists.newArrayList(661, 662, 663, 664, 665, 671, 672);

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
        List<EpMeterReadAllDataVO> jpList = classDateAllData.stream().filter(v -> v.getOfficeName().equals("基片制造部")).collect(Collectors.toList());
        List<LocalDate> timeList = dateSet.stream().sorted(Comparator.comparing(x -> x)).collect(Collectors.toList());
        //构建基片制造部
        EpDayReportCountRowVO jpRow = new EpDayReportCountRowVO();
        BigDecimal jpSum = BigDecimal.ZERO;
        List<BigDecimal> jpValueList = Lists.newArrayList();

        Map<LocalDate, List<BigDecimal>> jpResMap = Maps.newHashMap();
        for (LocalDate ele : timeList) {
            List<EpMeterReadAllDataVO> timeDataList = jpList.stream().filter(v -> v.getReadDate().isEqual(ele)).collect(Collectors.toList());
            BigDecimal reduce = timeDataList.stream().filter(v -> Objects.nonNull(v.getConsume())).map(EpMeterReadAllDataVO::getConsume).reduce(BigDecimal.ZERO, BigDecimal::add);
            jpSum = BigDecimalUtil.add(jpSum, reduce);
            jpValueList.add(BigDecimalUtil.setScale(reduce, 0));
            jpResMap.put(ele, Lists.newArrayList(reduce));
        }
        jpRow.setSumStr(BigDecimalUtil.setScale(jpSum, 0));
        jpRow.setConsumeList(jpValueList);
        jpRow.setUnit(epEnergy.getMeasureUnitName());
        jpRow.setEnergyType(epEnergy.getName());
        jpRow.setDepartmentName("基片");

        //构建生活用电 = 员工餐厅 + 员工宿舍
        List<EpMeterReadAllDataVO> shList = classDateAllData.stream().filter(v -> diningList.contains(v.getSysOfficeId())).collect(Collectors.toList());
        //构建基片制造部
        EpDayReportCountRowVO shRow = new EpDayReportCountRowVO();
        BigDecimal shSum = BigDecimal.ZERO;
        List<BigDecimal> shValueList = Lists.newArrayList();
        for (LocalDate ele : timeList) {
            List<EpMeterReadAllDataVO> timeDataList = shList.stream().filter(v -> v.getReadDate().isEqual(ele)).collect(Collectors.toList());
            BigDecimal reduce = timeDataList.stream().filter(v -> Objects.nonNull(v.getConsume())).map(EpMeterReadAllDataVO::getConsume).reduce(BigDecimal.ZERO, BigDecimal::add);
            shSum = BigDecimalUtil.add(shSum, reduce);
            shValueList.add(BigDecimalUtil.setScale(reduce, 0));
        }
        shRow.setSumStr(BigDecimalUtil.setScale(shSum, 0));
        shRow.setConsumeList(shValueList);
        shRow.setUnit(epEnergy.getMeasureUnitName());
        shRow.setEnergyType(epEnergy.getName());
        shRow.setDepartmentName("生活用电");

        resList.add(jpRow);
        resList.add(shRow);

        //深加工一~八产线
        List<TripleDTO<Long, Integer, String>> linePartitionList = partitionList.stream().filter(v -> sjgLine14List.contains(v.getField2()) || sjgLine58List.contains(v.getField2())).collect(Collectors.toList());
        Set<Integer> lineOfficeIdSet = Sets.newHashSet();
        Map<String, List<BigDecimal>> lineResMap = Maps.newHashMap();

        for (TripleDTO<Long, Integer, String> line : linePartitionList) {
            //获取产线id集合
            lineOfficeIdSet.add(line.getField2());
            List<EpMeterReadAllDataVO> lineList = classDateAllData.stream().filter(v -> line.getField1().equals(v.getOfficeId())).collect(Collectors.toList());
            //构建基片制造部
            EpDayReportCountRowVO lineRow = new EpDayReportCountRowVO();
            BigDecimal lineSum = BigDecimal.ZERO;
            List<BigDecimal> lineValueList = Lists.newArrayList();
            for (LocalDate ele : timeList) {
                List<EpMeterReadAllDataVO> timeDataList = lineList.stream().filter(v -> v.getReadDate().isEqual(ele)).collect(Collectors.toList());
                BigDecimal reduce = timeDataList.stream().filter(v -> Objects.nonNull(v.getConsume())).map(EpMeterReadAllDataVO::getConsume).reduce(BigDecimal.ZERO, BigDecimal::add);
                lineSum = BigDecimalUtil.add(lineSum, reduce);
                lineValueList.add(BigDecimalUtil.setScale(reduce, 0));
                //a1.1 a2.1 a3.1 a4.1 a5.1 a6.1 a7.1 a8.1
                lineResMap.put(mapKey(line.getField2(), ele), Lists.newArrayList(reduce));
            }
            lineRow.setSumStr(BigDecimalUtil.setScale(lineSum, 0));
            lineRow.setConsumeList(lineValueList);
            lineRow.setUnit(epEnergy.getMeasureUnitName());
            lineRow.setEnergyType(epEnergy.getName());
            lineRow.setDepartmentName(line.getField3());
            resList.add(lineRow);
        }

        //获取几条产线的产量，用于分摊公用电
        Map<String, BigDecimal> productNumMap = FeignOptional.of(() -> reportWorkFeignApi.classDateProductNum(new TripleDTO<>(lineOfficeIdSet, start, end)), FeignConstant.STECCN_DATA_PLATFORM_SERVICE_NAME).get();
        TupleDTO<Map<String, BigDecimal>, Map<String, BigDecimal>> tupleDTO = getProductNumRateMap(sjgLine14List, sjgLine58List, timeList, productNumMap);
        Map<String, BigDecimal> productNumLineRateMap = tupleDTO.getField1();
        Map<String, BigDecimal> productNumRateMap = tupleDTO.getField2();

        //获取公用电
        gydRate(classDateAllData, gydList, Lists.newArrayList(sjgLine14List, sjgLine58List), timeList, productNumLineRateMap, lineResMap);

        //获取风冷螺杆式空压机6 + 风冷螺杆式空压机7 + 风冷螺杆式空压机8 + 风冷螺杆式空压机9 + 风冷螺杆式空压机10 + 微热吸附式干燥器3
        //a1.3 a2.3 a3.3 a4.3 a5.3 a6.3 a7.3 a8.3
        fl610(classDateAllData, fl610, sjgLineList, timeList, productNumRateMap, lineResMap);

        //获取风冷螺杆式空压机1 + 风冷螺杆式空压机2 + 风冷螺杆式空压机3 + 风冷螺杆式空压机4 + 风冷螺杆式空压机5 + 微热吸附式干燥器1 + 微热吸附式干燥器2
        //获取b2
        fl15(classDateAllData, fl15, timeList, jpResMap);

        //获取总能耗N 按b1、a1.1 + a1.2 + a1.3、a2.1 + a2.2 + a2.3、... 、a8.1 + a8.2 + a8.3按照比例分摊
        //N分摊为b3、a1.4、a2.4、... 、a8.4共9个值
        Map<String, BigDecimal> nRateMap = getNRateMap(sjgLineList, timeList, jpResMap, lineResMap);

        return resList;
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
                BigDecimal lineDaySum = lineDataList.stream().filter(v -> Objects.nonNull(v)).reduce(BigDecimal.ZERO, BigDecimal::add);
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
            List<EpMeterReadAllDataVO> timeDataList = fl610DataList.stream().filter(v -> v.getReadDate().isEqual(ele)).collect(Collectors.toList());
            BigDecimal b2 = timeDataList.stream().filter(v -> Objects.nonNull(v.getConsume())).map(EpMeterReadAllDataVO::getConsume).reduce(BigDecimal.ZERO, BigDecimal::add);
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
            List<EpMeterReadAllDataVO> timeDataList = fl610DataList.stream().filter(v -> v.getReadDate().isEqual(ele)).collect(Collectors.toList());
            BigDecimal reduce = timeDataList.stream().filter(v -> Objects.nonNull(v.getConsume())).map(EpMeterReadAllDataVO::getConsume).reduce(BigDecimal.ZERO, BigDecimal::add);
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
                List<EpMeterReadAllDataVO> timeDataList = gydDataList.stream().filter(v -> v.getReadDate().isEqual(ele)).collect(Collectors.toList());
                BigDecimal reduce = timeDataList.stream().filter(v -> Objects.nonNull(v.getConsume())).map(EpMeterReadAllDataVO::getConsume).reduce(BigDecimal.ZERO, BigDecimal::add);
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
     * @param energyType
     * @param start
     * @param end
     * @param partitionList
     * @return
     */
    private List<EpDayReportCountRowVO> consumeRow(EpEnergyTypeVO energyType, LocalDate start, LocalDate end, List<TripleDTO<Long, Integer, String>> partitionList, List<BigDecimal> allThickList) {
        List<EpDayReportCountRowVO> rowList = Lists.newArrayList();
        if (CollUtil.isEmpty(partitionList) || Objects.isNull(energyType)) {
            return rowList;
        }
        EpTableDTO epTableDTO = new EpTableDTO();
        epTableDTO.setEnergyType(energyType.getId());
        epTableDTO.setStart(start);
        epTableDTO.setEnd(end);
        epTableDTO.setGranularity(QtManagerConstant.QtGranularityEnum.DAY.getCode());
        List<Long> partitionIdList = partitionList.stream().map(TupleDTO::getField1).collect(Collectors.toList());
        epTableDTO.setPartitionIdList(partitionIdList);
        epTableDTO.setThick(allThickList);
        EpChartGraphVO<EpChartGraphXAxisVO, BigDecimal> epChart = epConsumePartitionChartService.consumePartitionContrast(epTableDTO);
        List<BigDecimal> data = epChart.getSeriesDataVO().get(0).getData();

        //不同厚度的数据要汇总到一起
        for (int i = 1; i < epChart.getSeriesDataVO().size(); i++) {
            List<BigDecimal> temp = epChart.getSeriesDataVO().get(i).getData();
            for (int j = 0; j < temp.size(); j++) {
                BigDecimal oldValue = data.get(j);
                oldValue = BigDecimalUtil.add(oldValue, temp.get(j));
                data.set(j, oldValue);
            }
        }

        //如果部门不止一个 ，需要构造一个合计
        List<BigDecimal> sumList = Lists.newArrayList();
        BigDecimal total = null;
        int count;

        for (int i = 0; i < partitionList.size(); i++) {
            count = 0;
            EpDayReportCountRowVO row = new EpDayReportCountRowVO();
            List<BigDecimal> consumeList = Lists.newArrayList();
            BigDecimal sum = null;
            for (int j = i; j < data.size(); j += partitionIdList.size()) {
                BigDecimal ele = data.get(j);
                consumeList.add(ele);
                //列合计
                if (count > sumList.size() - 1) {
                    sumList.add(count, BigDecimal.ZERO);
                }
                sumList.set(count, BigDecimalUtil.add(sumList.get(count), ele));
                count++;
                sum = BigDecimalUtil.add(sum, ele);
            }
            List<BigDecimal> newConsumeList = Lists.newArrayList();
            consumeList.forEach(ele -> newConsumeList.add(BigDecimalUtil.setScale(ele, 0)));
            row.setConsumeList(newConsumeList);
            row.setEnergyType(energyType.getName());
            row.setUnit(energyType.getMeasureUnitName());
            row.setDepartmentName(partitionList.get(i).getField3());
            row.setSumStr(BigDecimalUtil.setScale(sum, 0));
            rowList.add(row);
            //合计行总计
            total = BigDecimalUtil.add(total, sum);

        }

        //如果超过一行，则添加一个合计行
        if (partitionIdList.size() > 1) {
            EpDayReportCountRowVO row = new EpDayReportCountRowVO();
            List<BigDecimal> sumStrList = Lists.newArrayList();
            sumList.forEach(ele -> {
                sumStrList.add(BigDecimalUtil.setScale(ele, 0));
            });
            row.setConsumeList(sumStrList);
            row.setEnergyType(energyType.getName());
            row.setUnit(energyType.getMeasureUnitName());
            row.setDepartmentName("合计");
            row.setSumStr(BigDecimalUtil.setScale(total, 0));
            rowList.add(row);
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
