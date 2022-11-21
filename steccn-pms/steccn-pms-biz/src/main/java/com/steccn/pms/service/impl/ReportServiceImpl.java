package com.steccn.pms.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.nacos.common.utils.CollectionUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.steccn.authserver.exception.BusinessException;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.authserver.util.SteccnTableAssertUtil;
import com.steccn.common.dto.TupleDTO;
import com.steccn.common.util.BigDecimalUtil;
import com.steccn.common.util.LocalDateUtil;
import com.steccn.common.vo.BaseSelectVO;
import com.steccn.platform.dto.SysDictDTO;
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
import com.steccn.pms.constant.PmReportConstant;
import com.steccn.pms.domain.PmProcess;
import com.steccn.pms.domain.PmsProduceOrderCraft;
import com.steccn.pms.domain.PmsPullForce;
import com.steccn.pms.domain.PmsReportWork;
import com.steccn.pms.dto.EnergyCountDTO;
import com.steccn.pms.dto.PmsDayReportDTO;
import com.steccn.pms.mapper.PmsProduceOrderMapper;
import com.steccn.pms.mapper.PmsReportWorkMapper;
import com.steccn.pms.service.*;
import com.steccn.pms.vo.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author yequan
 * @desc 报表接口实现
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ReportServiceImpl implements ReportService {

    private final RemoteOfficeFeign remoteOfficeFeign;
    private final RemoteSpareFeignApi remoteSpareFeignApi;
    private final PmsReportWorkConsumeService pmsReportWorkConsumeService;
    private final PmsReportWorkMapper pmsReportWorkMapper;
    private final RemoteDictFeignApi remoteDictFeignApi;
    private final PmsPullForceService pmsPullForceService;
    private final PmsReportWorkService pmsReportWorkService;
    private final PmsProduceOrderMapper pmsProduceOrderMapper;
    private final PmProcessService pmProcessService;
    private final PmsCompleteStorageInfoService pmsCompleteStorageInfoService;
    private final PmsWorkOrderBomService pmsWorkOrderBomService;
    private final PmsCompleteDeliveryService completeDeliveryService;
    private final RemoteSpareUnitConvertFeignApi remoteSpareUnitConvertFeignApi;
    private final PmsProduceOrderCraftService pmsProduceOrderCraftService;

    @Override
    public SubstrateDayReportVO substrateDayReport(PmsDayReportDTO dto) {
        //type data 0  line 1 workshop 2 total 3
        List<SubstrateDataListVO> defaultSubstrateDataListVOList = Lists.newArrayList();
        SubstrateDataListVO defaultSubstrateDataListVO = new SubstrateDataListVO();

        SubstrateDayReportVO vo = new SubstrateDayReportVO();
        //查询所有数据
        List<SubstrateDataListVO> rowDataList = pmsReportWorkMapper.substrateReport(dto.getProductLineIdList(), dto.getClassGroupIdList(), dto.getDate());
        SteccnTableAssertUtil.notEmpty(rowDataList, "数据为空，暂无基片日报表");
        rowDataList = rowDataList.stream().filter(v -> Objects.nonNull(v.getMaterialId()) && Objects.nonNull(v.getClassesId())).collect(Collectors.toList());
        SteccnTableAssertUtil.notEmpty(rowDataList, "数据为空，暂无基片日报表");

        Set<Long> materialSpecSet = rowDataList.stream().map(SubstrateDataListVO::getMaterialId).collect(Collectors.toSet());
        Map<Long, EqpSpareVO> specMap = FeignOptional.of(remoteSpareFeignApi.listById(Lists.newArrayList(materialSpecSet))).get();
        EqpSpareVO defaultEqpSpareVO = new EqpSpareVO();

        //获取平方米和吨的id
        List<Long> unitList = allUnit(Lists.newArrayList("片","ton","㎡"),"measure_unit");
        Map<Long, List<SysSpareUnitConversionVO>> unitConvertMap = FeignOptional.of(remoteSpareUnitConvertFeignApi.convert(materialSpecSet)).get();

        //获取班次数据
        List<SysDictVO> classList = getClassList();
        //构建表头
        List<SysDictVO> filterClassList = Lists.newArrayList();
        SubstrateReportHeaderVO header = getSubstrateHeader(dto.getClassGroupIdList(),classList,filterClassList);

        //将有数据的产线拉取出来
        Set<Integer> productLineSet = rowDataList.stream().map(SubstrateDataListVO::getProductLineId).collect(Collectors.toSet());
        Map<Integer, SysOfficeVO> productLineMap = FeignOptional.of(remoteOfficeFeign.listById(productLineSet)).get();
        //根据车间去划分
        Map<Integer, List<SysOfficeVO>> workShopMap = productLineMap.values().stream().collect(Collectors.groupingBy(SysOfficeVO::getParentId));
        List<SubstrateDayReportListVO> productLineVOList = Lists.newArrayList();
        //全局统计
        Map<Integer, BigDecimal> allCountMap = Maps.newHashMap();

        List<SubstrateDataListVO> finalRowDataList = rowDataList;

        workShopMap.forEach((k, v) -> {
            //车间统计
            Map<Integer, BigDecimal> workshopCountMap = Maps.newHashMap();
            //根据产线遍历
            for (SysOfficeVO productLine : v) {
                //产线数量合计-早班，晚班，合计
                Map<Integer, BigDecimal> countMap1 = Maps.newHashMap();
                Map<Integer, BigDecimal> countMap2 = Maps.newHashMap();
                Map<Integer, BigDecimal> countMap3 = Maps.newHashMap();
                //获取当前产线下的数据
                List<SubstrateDataListVO> productLineDataList = finalRowDataList.stream().filter(row -> productLine.getId().equals(row.getProductLineId())).collect(Collectors.toList());
                //将当前产线下的数据根据物料分组
                Map<Long, List<SubstrateDataListVO>> materialMap = productLineDataList.stream().collect(Collectors.groupingBy(SubstrateDataListVO::getMaterialId));
                //遍历物料分组的数据，来组装数据
                for (Map.Entry<Long, List<SubstrateDataListVO>> entry : materialMap.entrySet()) {
                    SubstrateDayReportListVO row = new SubstrateDayReportListVO();
                    row.setType(PmReportConstant.TYPE_0);
                    row.setWorkshopName(productLine.getParentName());
                    row.setProductLineName(productLine.getOfficeName());
                    Map<Long, List<SubstrateDataListVO>> classDataMap = entry.getValue().stream().collect(Collectors.groupingBy(SubstrateDataListVO::getClassesId));
                    //入库inCount 产量proCount
                    BigDecimal inCount = BigDecimal.ZERO;
                    BigDecimal proCount = BigDecimal.ZERO;
                    //遍历已筛选的班次
                    for (SysDictVO c : filterClassList) {
                        //获取班次信息
                        List<SubstrateDataListVO> classData = classDataMap.getOrDefault(c.getId(), defaultSubstrateDataListVOList);
                        //将classData分成不同类型
                        Map<Integer, SubstrateDataListVO> typeMap = Maps.newHashMap();
                        classData.forEach(cld -> typeMap.put(cld.getType(), cld));
                        SubstrateDataListVO typeVO0 = typeMap.getOrDefault(PmReportConstant.TYPE_0, defaultSubstrateDataListVO);
                        SubstrateDataListVO typeVO1 = typeMap.getOrDefault(PmReportConstant.TYPE_1, defaultSubstrateDataListVO);
                        inCount = BigDecimalUtil.add(inCount, typeVO0.getQuantity());
                        proCount = BigDecimalUtil.add(proCount, typeVO1.getQuantity());

                        BigDecimal bigDecimal1 = unitConvert(unitList, unitConvertMap, entry.getKey(), typeVO0.getQuantity(), 1);
                        BigDecimal bigDecimal2 = unitConvert(unitList, unitConvertMap, entry.getKey(), typeVO0.getQuantity(), 2);
                        BigDecimal bigDecimal3 = unitConvert(unitList, unitConvertMap, entry.getKey(), typeVO1.getQuantity(), 1);
                        BigDecimal bigDecimal4 = unitConvert(unitList, unitConvertMap, entry.getKey(), typeVO1.getQuantity(), 2);

                        if (PmReportConstant.SYS_SHIFT_0.equals(c.getCode())) {
                            //早班
                            row.setMorning(Arrays.asList(BigDecimalUtil.format(typeVO0.getQuantity()),
                                    BigDecimalUtil.format(bigDecimal1),
                                    BigDecimalUtil.format(bigDecimal2),
                                    BigDecimalUtil.format(typeVO1.getQuantity()),
                                    BigDecimalUtil.format(bigDecimal3),
                                    BigDecimalUtil.format(bigDecimal4)
                            ));
                            BigDecimalUtil.accumulate(countMap1, 0, typeVO0.getQuantity());
                            BigDecimalUtil.accumulate(countMap1, 1, bigDecimal1);
                            BigDecimalUtil.accumulate(countMap1, 2, bigDecimal2);
                            BigDecimalUtil.accumulate(countMap1, 3, typeVO1.getQuantity());
                            BigDecimalUtil.accumulate(countMap1, 4, bigDecimal3);
                            BigDecimalUtil.accumulate(countMap1, 5, bigDecimal4);
                        } else {
                            //晚班
                            row.setNight(Arrays.asList(BigDecimalUtil.format(typeVO0.getQuantity()),
                                    BigDecimalUtil.format(bigDecimal1),
                                    BigDecimalUtil.format(bigDecimal2),
                                    BigDecimalUtil.format(typeVO1.getQuantity()),
                                    BigDecimalUtil.format(bigDecimal3),
                                    BigDecimalUtil.format(bigDecimal4)
                            ));
                            BigDecimalUtil.accumulate(countMap2, 0, typeVO0.getQuantity());
                            BigDecimalUtil.accumulate(countMap2, 1, typeVO0.getQuantity());
                            BigDecimalUtil.accumulate(countMap2, 2, typeVO0.getQuantity());
                            BigDecimalUtil.accumulate(countMap2, 3, typeVO1.getQuantity());
                            BigDecimalUtil.accumulate(countMap2, 4, typeVO1.getQuantity());
                            BigDecimalUtil.accumulate(countMap2, 5, typeVO1.getQuantity());
                        }
                    }
                    BigDecimal m1 = unitConvert(unitList, unitConvertMap, entry.getKey(), inCount, 1);
                    BigDecimal m2 = unitConvert(unitList, unitConvertMap, entry.getKey(), inCount, 2);
                    BigDecimal m3 = unitConvert(unitList, unitConvertMap, entry.getKey(), proCount, 1);
                    BigDecimal m4 = unitConvert(unitList, unitConvertMap, entry.getKey(), proCount, 2);

                    BigDecimalUtil.accumulate(countMap3, 0, inCount);
                    BigDecimalUtil.accumulate(countMap3, 1, m1);
                    BigDecimalUtil.accumulate(countMap3, 2, m2);
                    BigDecimalUtil.accumulate(countMap3, 3, proCount);
                    BigDecimalUtil.accumulate(countMap3, 4, m3);
                    BigDecimalUtil.accumulate(countMap3, 5, m4);
                    row.setTotal(Arrays.asList(BigDecimalUtil.format(inCount),
                            BigDecimalUtil.format(m1),
                            BigDecimalUtil.format(m2),
                            BigDecimalUtil.format(proCount),
                            BigDecimalUtil.format(m3),
                            BigDecimalUtil.format(m4)));
                    EqpSpareVO eqpSpareVO = specMap.getOrDefault(entry.getKey(), defaultEqpSpareVO);
                    row.setSpecList(Arrays.asList(BigDecimalUtil.format(eqpSpareVO.getLengthQuantity()), BigDecimalUtil.format(eqpSpareVO.getWideQuantity()), BigDecimalUtil.format(eqpSpareVO.getHighQuantity())));
                    productLineVOList.add(row);
                }
                SubstrateDayReportListVO rowTotal = new SubstrateDayReportListVO();
                rowTotal.setType(PmReportConstant.TYPE_1);
                rowTotal.setWorkshopName(productLine.getParentName());
                rowTotal.setProductLineName(productLine.getOfficeName());
                if(dayOrNight(dto.getClassGroupIdList(),classList) == PmReportConstant.TYPE_0){
                    rowTotal.setMorning(valueStr(countMap1));
                    rowTotal.setNight(valueStr(countMap2));
                }else if(dayOrNight(dto.getClassGroupIdList(),classList) == PmReportConstant.TYPE_1){
                    rowTotal.setMorning(valueStr(countMap1));
                }else if(dayOrNight(dto.getClassGroupIdList(),classList) == PmReportConstant.TYPE_2){
                    rowTotal.setNight(valueStr(countMap2));
                }
                rowTotal.setTotal(valueStr(countMap3));
                rowTotal.setSpecList(Arrays.asList("合计", "合计", "合计"));
                productLineVOList.add(rowTotal);
                //插入拉引量，合格率，耗水量，耗电量，耗油量，单耗(kcal/Kg玻璃液)
                addProductLineOther(dto, classList, productLine, productLineVOList, workshopCountMap);
            }
            //添加车间库存
            addWorkShopOther(v.get(0), productLineVOList, workshopCountMap);
            //计算总的库存
            for (PmReportConstant.SubstrateReportConsumeEnum sub : PmReportConstant.SubstrateReportConsumeEnum.values()) {
                BigDecimalUtil.accumulate(allCountMap, sub.getCode(), workshopCountMap.get(sub.getCode()));
            }
        });
        //最后一个总计
        addAllOther(productLineVOList, allCountMap);
        //如果只选择一个班次，则只展示一个班次
        filterClass(dayOrNight(dto.getClassGroupIdList(),classList),productLineVOList);
        vo.setSpecsNum(3);
        vo.setDataNum(6);
        vo.setHeader(header);
        vo.setDataList(productLineVOList);
        return vo;
    }

    /**
     * 获取map中的数据
     * @param countMap
     * @return
     */
    private List<String> valueStr(Map<Integer, BigDecimal> countMap){
        List<String> resList = Lists.newArrayList();
        for(int i = 0 ; i <=5 ;i ++){
            resList.add(BigDecimalUtil.format(countMap.get(i)));
        }
        return resList;
    }


    /**
     * 获取所有的单位的id  依次是片，
     * @param codeSet
     * @param measureUnit
     * @return
     */
    private List<Long> allUnit(List<String> codeSet, String measureUnit) {
        List<Long> resList = Lists.newArrayList();
        SysDictDTO sysDictDTO = new SysDictDTO();
        sysDictDTO.setType(measureUnit);
        sysDictDTO.setCodeList(codeSet);
        SysDictVO defaultSysDictVO = new SysDictVO();
        Map<String, SysDictVO> resMap = FeignOptional.of(remoteDictFeignApi.listByCode(sysDictDTO)).get();
        for (String code : codeSet) {
            SysDictVO sysDictVO = resMap.getOrDefault(code,defaultSysDictVO);
            resList.add(sysDictVO.getId());
        }
        return resList;
    }


    /**
     * 返回0-早班夜班 ， 1-早班 ，2-夜班
     * @param classesIdList
     * @param classList
     * @return
     */
    private int dayOrNight(List<Long> classesIdList, List<SysDictVO> classList){
        if(CollUtil.isEmpty(classesIdList)){
            return PmReportConstant.TYPE_0;
        }
        List<Long> collect = classList.stream().map(SysDictVO::getId).collect(Collectors.toList());
        if(classesIdList.containsAll(collect)){
            return PmReportConstant.TYPE_0;
        }
        for (Long cid : classesIdList) {
            if(collect.indexOf(cid) == PmReportConstant.TYPE_0){
                return PmReportConstant.TYPE_1;
            }else if(collect.indexOf(cid) == PmReportConstant.TYPE_1){
                return PmReportConstant.TYPE_2;
            }else{
                throw new IllegalArgumentException("班次产线异常");
            }
        }
        return PmReportConstant.TYPE_0;
    }


    /**
     * 物料单位转换
     * @param unitList 单位list
     * @param unitConvertMap 单位转换map
     * @param materialId 物料id
     * @param num 数量
     * @param idx 单位id
     * @return 转换后的数值
     */
    private BigDecimal unitConvert(List<Long> unitList,Map<Long, List<SysSpareUnitConversionVO>> unitConvertMap , Long materialId ,BigDecimal num, Integer idx){
        //1.获取物料对应的单位转换
        List<SysSpareUnitConversionVO> orDefault = unitConvertMap.getOrDefault(materialId, Lists.newArrayList());
        //2.遍历此物料所有单位转换集合
        if(CollUtil.isNotEmpty(orDefault)){
            Map<Long,SysSpareUnitConversionVO> resMap = Maps.newHashMap();
            for (SysSpareUnitConversionVO spare : orDefault) {
                resMap.put(spare.getConversionUintId(),spare);
            }
            SysSpareUnitConversionVO vo = resMap.getOrDefault(unitList.get(idx), new SysSpareUnitConversionVO());
            if(Objects.nonNull(vo)){
                BigDecimal multiply = BigDecimalUtil.multiply(vo.getConversionQuantity(), num);
                BigDecimal divide = BigDecimalUtil.divide(multiply, vo.getQuantity());
                return divide;
            }
        }
        return BigDecimal.ZERO;
    }


    /**
     * 获取基片日报表 表头
     *
     * @param classesIdList dto的参数
     * @param classList 所有的班次对象集合
     */
    private SubstrateReportHeaderVO getSubstrateHeader(List<Long> classesIdList, List<SysDictVO> classList, List<SysDictVO> filterClassList ) {
        SubstrateReportHeaderVO header = new SubstrateReportHeaderVO();
        header.setSpecList(new BaseReportHeaderVO("规格", Arrays.asList("长", "宽", "厚")));

        List<Long> allClassIdList = classList.stream().map(SysDictVO::getId).collect(Collectors.toList());
        if(CollUtil.isEmpty(classesIdList)){
            classesIdList = allClassIdList;
        }
        //只查询白班，或者夜班
        for (Long cid : classesIdList) {
            int i = allClassIdList.indexOf(cid);
            if (i == 0) {
                header.setMorning(new BaseReportHeaderVO("早班", Arrays.asList("入库(片)", "入库(㎡)", "入库(t)", "产量(片)", "产量(㎡)", "产量(t)")));
            } else if(i == 1){
                header.setNight(new BaseReportHeaderVO("晚班", Arrays.asList("入库(片)", "入库(㎡)", "入库(t)", "产量(片)", "产量(㎡)", "产量(t)")));
            }else{
                throw new IllegalArgumentException("班次数据异常");
            }
            filterClassList.add(classList.get(i));
        }
        header.setTotal(new BaseReportHeaderVO("合计", Arrays.asList("入库(片)", "入库(㎡)", "入库(t)", "产量(片)", "产量(㎡)", "产量(t)")));
        return header;
    }

    /**
     * 获取数据字典班次数据
     *
     * @return list
     */
    private List<SysDictVO> getClassList() {
        SysDictDTO sysDictDTO = new SysDictDTO();
        sysDictDTO.setType(PmManagerConstant.SYS_SHIFT);
        Map<String, SysDictVO> classMap = FeignOptional.of(remoteDictFeignApi.listByCode(sysDictDTO)).get();
        List<SysDictVO> classList = classMap.values().stream().collect(Collectors.toList());
        classList.sort(Comparator.comparing(SysDictVO::getSort));
        return classList;
    }

    /**
     * 全局总计
     *
     * @param productLineVOList
     * @param allCountMap
     */
    private void addAllOther(List<SubstrateDayReportListVO> productLineVOList, Map<Integer, BigDecimal> allCountMap) {
        for (PmReportConstant.SubstrateReportConsumeEnum consumeEnum : PmReportConstant.SubstrateReportConsumeEnum.values()) {
            Integer idx = consumeEnum.getCode();
            SubstrateDayReportListVO subVo = rowData(new SysOfficeVO(), Arrays.asList(allCountMap.get(idx), allCountMap.get(idx), allCountMap.get(idx)), consumeEnum.getName());
            subVo.setWorkshopName("总计");
            subVo.setProductLineName("总计");
            subVo.setSpecList(Arrays.asList(consumeEnum.getName(), consumeEnum.getName(), consumeEnum.getName()));
            subVo.setType(PmReportConstant.TYPE_3);
            productLineVOList.add(subVo);
        }
    }

    /**
     * 构造车间总计
     *
     * @param productLine       产线名称
     * @param productLineVOList 返回集合
     * @param workshopCountMap  车间数据总计
     */
    private void addWorkShopOther(SysOfficeVO productLine, List<SubstrateDayReportListVO> productLineVOList, Map<Integer, BigDecimal> workshopCountMap) {
        for (PmReportConstant.SubstrateReportConsumeEnum consumeEnum : PmReportConstant.SubstrateReportConsumeEnum.values()) {
            Integer idx = consumeEnum.getCode();
            SubstrateDayReportListVO subVo = rowData(productLine, Arrays.asList(workshopCountMap.get(idx), workshopCountMap.get(idx), workshopCountMap.get(idx)), consumeEnum.getName());
            subVo.setWorkshopName(productLine.getParentName());
            subVo.setProductLineName("总计");
            subVo.setType(PmReportConstant.TYPE_2);
            subVo.setSpecList(Arrays.asList(consumeEnum.getName(), consumeEnum.getName(), consumeEnum.getName()));
            productLineVOList.add(subVo);
        }
    }

    /**
     * 插入拉引量，合格率，耗水量，耗电量，耗油量，单耗(kcal/Kg玻璃液)
     *
     * @param dto               查询条件
     * @param classList         班次数据
     * @param productLine       产线信息
     * @param productLineVOList 数据存放集合
     */
    private void addProductLineOther(PmsDayReportDTO dto, List<SysDictVO> classList, SysOfficeVO productLine, List<SubstrateDayReportListVO> productLineVOList, Map<Integer, BigDecimal> workshopCountMap) {
        //拉引量(T)
        Map<Long, PmsPullForce> pmsPullForcesMap = pmsPullForceService.pullForceDay(productLine.getId(), dto.getDate());
        PmsPullForce morning = pmsPullForcesMap.get(classList.get(0).getId());
        PmsPullForce night = pmsPullForcesMap.get(classList.get(1).getId());
        BigDecimal morningBigDecimal = Objects.nonNull(morning) ? morning.getQuantity() : BigDecimal.ZERO;
        BigDecimal nightBigDecimal = Objects.nonNull(night) ? night.getQuantity() : BigDecimal.ZERO;
        BigDecimal sum = BigDecimalUtil.add(morningBigDecimal, nightBigDecimal);
        SubstrateDayReportListVO lyl = rowData(productLine, Lists.newArrayList(morningBigDecimal, nightBigDecimal, sum), "拉引量(T)");

        productLineVOList.add(lyl);
        BigDecimalUtil.accumulate(workshopCountMap, 0, morningBigDecimal.add(nightBigDecimal));

        //合格率：根据当日当班产量(t)/拉引量计算，按百分比显示
        Map<Long, PmsReportWork> pmsReportWorkMap = pmsReportWorkService.dateProductNum(productLine.getId(), dto.getDate());
        PmsReportWork defaultPmsReportWork = new PmsReportWork();
        PmsReportWork morningPms = pmsReportWorkMap.getOrDefault(classList.get(0).getId(), defaultPmsReportWork);
        PmsReportWork nightPms = pmsReportWorkMap.getOrDefault(classList.get(1).getId(), defaultPmsReportWork);
        BigDecimal dayPq = BigDecimalUtil.multiply(BigDecimalUtil.HUNDRED, morningPms.getProcessQuantity());
        BigDecimal nightPq = BigDecimalUtil.multiply(BigDecimalUtil.HUNDRED, nightPms.getProcessQuantity());
        BigDecimal morningRate = BigDecimalUtil.divide(dayPq, morningBigDecimal);
        BigDecimal nightRate = BigDecimalUtil.divide(nightPq, nightBigDecimal);
        BigDecimal rateSum = BigDecimalUtil.add(morningRate, nightRate);
        SubstrateDayReportListVO rateVo = rowData(productLine, Arrays.asList(morningRate, nightRate, rateSum), "合格率(%)");
        BigDecimalUtil.accumulate(workshopCountMap, 1, morningRate.add(nightRate));

        productLineVOList.add(rateVo);
        //0耗电量 1耗水量 2耗油量 3耗气量
        List<SubstrateDayReportListVO> energyList = pmsReportWorkConsumeService.substrateEnergy(dto, classList, productLine, workshopCountMap);

        //单耗(kcal/Kg玻璃液) （耗气量 * 7800 + 耗油量 * 9300  ） / （拉引量 * 1000)
        SubstrateDayReportListVO bly = getSubstrateDayReportListVO(productLine, morningBigDecimal, nightBigDecimal, energyList, workshopCountMap);
        productLineVOList.addAll(energyList);
        productLineVOList.add(bly);
    }

    /**
     * 过滤班级
     * @param flag
     * @param dataList
     */
    private void filterClass(int flag, List<SubstrateDayReportListVO> dataList) {
        if (CollUtil.isNotEmpty(dataList)) {
            if (flag == PmReportConstant.TYPE_1) {
                for (SubstrateDayReportListVO vo : dataList) {
                    vo.setNight(null);
                }
            } else if (flag == PmReportConstant.TYPE_2) {
                for (SubstrateDayReportListVO vo : dataList) {
                    vo.setMorning(null);
                }
            }
        }
    }


    /**
     * 单耗(kcal/Kg玻璃液) （耗气量 * 7800 + 耗油量 * 9300  ） / （拉引量 * 1000)
     *
     * @param productLine       产线id
     * @param morningBigDecimal 早上数量
     * @param nightBigDecimal   晚上数量
     * @param energyList        能耗列表
     * @return 统计数据
     */
    private SubstrateDayReportListVO getSubstrateDayReportListVO(SysOfficeVO productLine, BigDecimal morningBigDecimal, BigDecimal nightBigDecimal, List<SubstrateDayReportListVO> energyList, Map<Integer, BigDecimal> workshopCountMap) {
        BigDecimal morningGas = new BigDecimal(energyList.get(3).getMorning().get(0));
        BigDecimal morningOil = new BigDecimal(energyList.get(2).getMorning().get(0));
        BigDecimal morningKcal = BigDecimal.ZERO;
        BigDecimal nightGas = new BigDecimal(energyList.get(3).getNight().get(0));
        BigDecimal nightOil = new BigDecimal(energyList.get(2).getNight().get(0));
        BigDecimal nightKcal = BigDecimal.ZERO;
        if (BigDecimal.ZERO.compareTo(morningBigDecimal.multiply(BigDecimal.valueOf(1000))) != 0) {
            BigDecimal morningMultiply = morningGas.multiply(BigDecimal.valueOf(7800)).add(morningOil.multiply(BigDecimal.valueOf(9300)));
            morningKcal = morningMultiply.divide(morningBigDecimal.multiply(BigDecimal.valueOf(1000)), BigDecimalUtil.DEFAULT_SCALE, RoundingMode.HALF_UP);
        }
        if (BigDecimal.ZERO.compareTo(nightBigDecimal.multiply(BigDecimal.valueOf(1000))) != 0) {
            BigDecimal nightMultiply = nightGas.multiply(BigDecimal.valueOf(7800)).add(nightOil.multiply(BigDecimal.valueOf(9300)));
            nightKcal = nightMultiply.divide(nightBigDecimal.multiply(BigDecimal.valueOf(1000)), BigDecimalUtil.DEFAULT_SCALE, RoundingMode.HALF_UP);
        }
        SubstrateDayReportListVO bly = rowData(productLine, Lists.newArrayList(morningKcal, nightKcal, morningKcal.add(nightKcal)), "单耗(kcal/Kg玻璃液)");
        BigDecimalUtil.accumulate(workshopCountMap, 6, morningKcal.add(nightKcal));
        return bly;
    }


    @Override
    public SubstrateDayReportListVO rowData(SysOfficeVO productLine, List<BigDecimal> dataList, String specStr) {
        SubstrateDayReportListVO row = new SubstrateDayReportListVO();
        row.setWorkshopName(productLine.getParentName());
        row.setProductLineName(productLine.getOfficeName());
        row.setType(PmReportConstant.TYPE_1);
        row.setSpecList(Arrays.asList(specStr, specStr, specStr));
        String dataStr1 = BigDecimalUtil.format(dataList.get(0));
        String dataStr2 = BigDecimalUtil.format(dataList.get(1));
        String dataStr3 = BigDecimalUtil.format(dataList.get(2));
        row.setMorning(Arrays.asList(dataStr1, dataStr1,dataStr1,dataStr1,dataStr1,dataStr1));
        row.setNight(Arrays.asList(dataStr2, dataStr2, dataStr2, dataStr2, dataStr2, dataStr2));
        row.setTotal(Arrays.asList(dataStr3, dataStr3, dataStr3, dataStr3, dataStr3, dataStr3));
        return row;
    }

    @Override
    public FurtherProduceReportVO furtherProduceDayReport(PmsDayReportDTO dto) {

        FurtherProduceReportVO vo = new FurtherProduceReportVO();
        vo.setSpecsNum(PmReportConstant.TYPE_3);

        //根据产线去查询所有订单的工序分组，用于表头
        List<FurtherProduceReportDataVO> dataList = pmsReportWorkMapper.furtherProduceReport(dto.getProductLineIdList(), dto.getClassGroupIdList(), dto.getDate());
        Assert.notEmpty(dataList, "数据为空，暂无报表信息");

        Set<Long> materialSpecSet = dataList.stream().map(FurtherProduceReportDataVO::getMaterialId).collect(Collectors.toSet());
        Map<Long, EqpSpareVO> specMap = FeignOptional.of(remoteSpareFeignApi.listById(Lists.newArrayList(materialSpecSet))).get();
        EqpSpareVO defaultEqpSpareVO = new EqpSpareVO();

        //遍历所有的物料查询完工数量
        Set<Long> materialSet = dataList.stream().map(FurtherProduceReportDataVO::getMaterialId).collect(Collectors.toSet());
        Map<Long, Map<Long, BigDecimal>> materialCompleteMap = pmsCompleteStorageInfoService.materialMap(materialSet, dto.getDate());

        //构建表头
        FurtherReportHeaderVO header = new FurtherReportHeaderVO();
        header.setSpecList(new BaseReportHeaderVO("规格", Arrays.asList("长", "宽", "厚")));
        //工序id集合
        Set<Long> processIdSet = dataList.stream().map(FurtherProduceReportDataVO::getProcessId).collect(Collectors.toSet());
        //查询工序的信息

        List<PmsProduceOrderCraft> pmsProduceOrderCraftList = pmsProduceOrderCraftService.listByIds(processIdSet);
        Assert.notEmpty(pmsProduceOrderCraftList,"工艺信息不可为空");
        Set<Long> pidSet = pmsProduceOrderCraftList.stream().map(PmsProduceOrderCraft::getProcessId).collect(Collectors.toSet());
        List<PmProcess> pmProcessList = pmProcessService.listByIds(pidSet);
        Assert.notEmpty(pmsProduceOrderCraftList,"工序信息不可为空");
        SteccnTableAssertUtil.notEmpty(dataList, "工序信息为空，暂无报表信息");
        //工序信息转换
        Map<Long,Long> craftProcessIdMap = Maps.newHashMap();
        pmsProduceOrderCraftList.forEach(ele -> craftProcessIdMap.put(ele.getId(),ele.getProcessId()));
        dataList.forEach(ele -> ele.setProcessId(craftProcessIdMap.get(ele.getProcessId())));

        List<BaseReportHeaderVO> module = Lists.newArrayList();
        pmProcessList.forEach(ele -> {
            module.add(new BaseReportHeaderVO(ele.getName(), Arrays.asList("加工(片)", "合格(片)", "不良(片)", "合格率")));
        });
        module.add(new BaseReportHeaderVO("合计", Arrays.asList("加工(片)", "合格(片)", "不良(片)", "合格率")));
        module.add(new BaseReportHeaderVO("入库", Arrays.asList("数量(片)", "数量(㎡)", "成品率")));
        header.setModule(module);
        vo.setHeader(header);

        //获取班次数据
        List<SysDictVO> classList = getClassList();
        Map<Long, SysDictVO> classMap = Maps.newHashMap();
        classList.forEach(ele -> classMap.put(ele.getId(), ele));

        //汇总数据
        FurtherProduceReportDataVO defaultFurtherProduceReportDataVO = new FurtherProduceReportDataVO();
        List<FurtherProduceReportListVO> tableData = Lists.newArrayList();
        //查询产线车间信息
        Set<Integer> productLineSet = dataList.stream().map(FurtherProduceReportDataVO::getProductLineId).collect(Collectors.toSet());
        Map<Integer, SysOfficeVO> productLineMap = FeignOptional.of(remoteOfficeFeign.listById(productLineSet)).get();
        //根据车间去划分
        Map<Integer, List<SysOfficeVO>> workShopMap = productLineMap.values().stream().collect(Collectors.groupingBy(SysOfficeVO::getParentId));
        workShopMap.forEach((workshop, productLineList) -> {
            //车间统计
            Map<Long, Map<Integer, BigDecimal>> workshopCountMap = Maps.newHashMap();
            //根据产线遍历
            for (SysOfficeVO productLine : productLineList) {
                //产线的合计 key-产线 value【key为数量类别，value为数量】
                Map<Long, Map<Integer, BigDecimal>> lineCountMap = Maps.newHashMap();
                //过滤出产线数据并将产线数据分成不同班次
                Map<Long, List<FurtherProduceReportDataVO>> lineDataMap = dataList.stream().filter(row -> productLine.getId().equals(row.getProductLineId())).collect(Collectors.groupingBy(FurtherProduceReportDataVO::getClassesId));
                //遍历不同的班次
                for (Map.Entry<Long, List<FurtherProduceReportDataVO>> classEntry : lineDataMap.entrySet()) {
                    //班次的合计 key-工序 value【key为数量类别，value为数量】
                    Map<Long, Map<Integer, BigDecimal>> classCountMap = Maps.newHashMap();
                    //获取班次的数据
                    SysDictVO sysDictVO = classMap.get(classEntry.getKey());
                    Map<Long, List<FurtherProduceReportDataVO>> materialMap = classEntry.getValue().stream().collect(Collectors.groupingBy(FurtherProduceReportDataVO::getMaterialId));
                    //遍历物料分组
                    for (Map.Entry<Long, List<FurtherProduceReportDataVO>> materialEntry : materialMap.entrySet()) {
                        //记录行合计
                        Map<Integer, BigDecimal> rowCountMap = Maps.newHashMap();
                        //将同一条物料数据，根据工序分组
                        Map<Long, FurtherProduceReportDataVO> processMap = Maps.newHashMap();
                        materialEntry.getValue().forEach(ele -> processMap.put(ele.getProcessId(), ele));
                        List<FurtherValueStrVO> moduleList = Lists.newArrayList();
                        //遍历所有的工序
                        for (PmProcess pmProcess : pmProcessList) {
                            FurtherProduceReportDataVO orDefault = processMap.getOrDefault(pmProcess.getId(), defaultFurtherProduceReportDataVO);
                            List<String> valueStrList = Lists.newArrayList();
                            valueStrList.add(BigDecimalUtil.format(orDefault.getProcessQuantity()));
                            BigDecimal subtract = BigDecimalUtil.subtract(orDefault.getProcessQuantity(), orDefault.getBadnessQuantity());
                            valueStrList.add(BigDecimalUtil.format(subtract));
                            valueStrList.add(BigDecimalUtil.format(orDefault.getBadnessQuantity()));
                            BigDecimal divide = BigDecimalUtil.divide(subtract, orDefault.getProcessQuantity());
                            valueStrList.add(BigDecimalUtil.format(divide.multiply(BigDecimal.valueOf(100))));
                            moduleList.add(new FurtherValueStrVO(valueStrList));
                            //列统计数据
                            pmColumnCount(rowCountMap, orDefault, subtract, divide);
                            //班组求和
                            pmRowCount(classCountMap, pmProcess.getId(), orDefault, subtract, divide);
                            //产线求和
                            pmRowCount(lineCountMap, pmProcess.getId(), orDefault, subtract, divide);
                            //车间求和
                            pmRowCount(workshopCountMap, pmProcess.getId(), orDefault, subtract, divide);
                        }
                        //合计
                        List<String> rowCountList = Lists.newArrayList();
                        rowCountList.add(BigDecimalUtil.format(rowCountMap.get(0)));
                        rowCountList.add(BigDecimalUtil.format(rowCountMap.get(1)));
                        rowCountList.add(BigDecimalUtil.format(rowCountMap.get(2)));
                        rowCountList.add(BigDecimalUtil.format(rowCountMap.get(3)));
                        //班组合计
                        pmColumnCount(classCountMap, 0L, rowCountMap);
                        //产线合计
                        pmColumnCount(lineCountMap, 0L, rowCountMap);
                        //车间合计
                        pmColumnCount(workshopCountMap, 0L, rowCountMap);
                        moduleList.add(new FurtherValueStrVO(rowCountList));
                        //入库
                        Map<Long, BigDecimal> orDefaultMap = materialCompleteMap.getOrDefault(materialEntry.getKey(), Maps.newHashMap());
                        String format = BigDecimalUtil.format(orDefaultMap.get(classEntry.getKey()));
                        moduleList.add(new FurtherValueStrVO(Arrays.asList(format, format, format)));
                        //班次，产线，车间
                        countIn(classEntry.getKey(), classCountMap, orDefaultMap);
                        countIn(classEntry.getKey(), lineCountMap, orDefaultMap);
                        countIn(classEntry.getKey(), workshopCountMap, orDefaultMap);

                        EqpSpareVO eqpSpareVO = specMap.getOrDefault(materialEntry.getKey(), defaultEqpSpareVO);
                        List<String> specDataList = Arrays.asList(BigDecimalUtil.format(eqpSpareVO.getLengthQuantity()), BigDecimalUtil.format(eqpSpareVO.getWideQuantity()), BigDecimalUtil.format(eqpSpareVO.getHighQuantity()));
                        FurtherProduceReportListVO rowData = createFurtherProduceReportListVO(Arrays.asList(productLine.getParentName(), productLine.getOfficeName(), sysDictVO.getName()), PmReportConstant.TYPE_0, moduleList, specDataList);
                        tableData.add(rowData);
                    }
                    //班次的合计
                    FurtherProduceReportListVO classCountRow = getFurtherProduceReportListVO(pmProcessList, productLine, classCountMap, sysDictVO, Arrays.asList("合计", "合计", "合计"));
                    tableData.add(classCountRow);
                }
                //产线的合计
                FurtherProduceReportListVO lineCountRow = getFurtherProduceReportListVO(pmProcessList, productLine, lineCountMap, new SysDictVO(), Arrays.asList("合计", "合计", "合计"));
                lineCountRow.setClassName("合计");
                lineCountRow.setType(PmReportConstant.TYPE_2);
                tableData.add(lineCountRow);
            }
            //车间的合计
            FurtherProduceReportListVO workshopCountRow = getFurtherProduceReportListVO(pmProcessList, productLineList.get(0), workshopCountMap, new SysDictVO(), Arrays.asList("合计", "合计", "合计"));
            workshopCountRow.setClassName("合计");
            workshopCountRow.setProductLineName("合计");
            workshopCountRow.setType(PmReportConstant.TYPE_3);
            tableData.add(workshopCountRow);

        });
        vo.setTableData(tableData);
        return vo;
    }

    @Override
    public FurtherIngredientReportVO furtherIngredientDayReport(PmsDayReportDTO dto) {
        //每一行数据是一个生产订单生产的主物料
        //定义返回vo
        FurtherIngredientReportVO vo = new FurtherIngredientReportVO();
        vo.setSpecsNum(PmReportConstant.TYPE_3);
        //根据产线去查询所有订单的工序分组，用于表头
        List<FurtherIngredientMapperVO> dataList = pmsReportWorkMapper.furtherIngredient(dto.getProductLineIdList(), dto.getClassGroupIdList(), dto.getDate());
        Assert.notEmpty(dataList, "数据为空，暂无深加工日报表");
        Set<Long> materialSpecSet = dataList.stream().map(FurtherIngredientMapperVO::getMaterialId).collect(Collectors.toSet());
        Map<Long, EqpSpareVO> specMap = FeignOptional.of(remoteSpareFeignApi.listById(Lists.newArrayList(materialSpecSet))).get();
        EqpSpareVO defaultEqpSpareVO = new EqpSpareVO();

        //定义
        List<FurtherIngredientReportListVO> tableData = Lists.newArrayList();

        //构建表头
        FurtherReportHeaderVO header = new FurtherReportHeaderVO();
        header.setSpecList(new BaseReportHeaderVO("规格", Arrays.asList("长", "宽", "厚")));

        //获取所有的工序id
        Set<Long> processIdSet = dataList.stream().map(FurtherIngredientMapperVO::getProcessId).collect(Collectors.toSet());
        //查询工序的信息
        List<PmsProduceOrderCraft> pmsProduceOrderCraftList = pmsProduceOrderCraftService.listByIds(processIdSet);
        Assert.notEmpty(pmsProduceOrderCraftList,"工艺信息不可为空");
        Set<Long> pidSet = pmsProduceOrderCraftList.stream().map(PmsProduceOrderCraft::getProcessId).collect(Collectors.toSet());
        List<PmProcess> pmProcessList = pmProcessService.listByIds(pidSet);
        Assert.notEmpty(dataList, "工序信息为空，暂无报表信息");
        Map<Long, PmProcess> pmProcessMap = Maps.newHashMap();
        pmProcessList.forEach(ele -> pmProcessMap.put(ele.getId(), ele));
        PmProcess defaultPmProcess = new PmProcess();

        Map<Long,Long> craftProcessIdMap = Maps.newHashMap();
        Map<Long,Long> processIdCraftMap = Maps.newHashMap();
        pmsProduceOrderCraftList.forEach(ele -> {
            craftProcessIdMap.put(ele.getId(),ele.getProcessId());
            processIdCraftMap.put(ele.getProcessId(),ele.getId());
        });
        dataList.forEach(ele -> ele.setProcessId(craftProcessIdMap.get(ele.getProcessId())));

        //获取生产订单id集合
        Set<Long> produceOrderIdSet = dataList.stream().map(FurtherIngredientMapperVO::getProduceOrderId).collect(Collectors.toSet());

        //查询生产订单的耗料
        Map<Long, Map<Long, Map<Long, ProduceOrderProcessNumVO>>> numConsumeMap = completeDeliveryService.processNumConsumeMap(produceOrderIdSet);
        Map<Long, Map<Long, ProduceOrderProcessNumVO>> defaultProcessNumMap = Maps.newHashMap();
        Map<Long, ProduceOrderProcessNumVO> defaultMaterialNumMap = Maps.newHashMap();
        ProduceOrderProcessNumVO defaultProduceOrderProcessNumVO = new ProduceOrderProcessNumVO();

        //查询工序需要的物料,兼容所有的工序物料
        Map<Long, List<BaseSelectVO<Long>>> materialProcessMapCopy = pmsWorkOrderBomService.getProcessMaterial(produceOrderIdSet, pidSet);
        Map<Long, List<BaseSelectVO<Long>>> materialProcessMap = Maps.newHashMap();
        materialProcessMapCopy.forEach((k,v) ->{
            materialProcessMap.put(processIdCraftMap.get(k),v);
        });

        Assert.notEmpty(materialProcessMap, "工序信息未配置物料信息，暂无报表数据");
        List<BaseReportHeaderVO> module = Lists.newArrayList();
        materialProcessMap.forEach((k, v) -> {
            BaseReportHeaderVO moduleHeader = new BaseReportHeaderVO(pmProcessMap.getOrDefault(craftProcessIdMap.get(k), defaultPmProcess).getName(), v.stream().map(BaseSelectVO::getName).collect(Collectors.toList()));
            module.add(moduleHeader);
        });
        header.setModule(module);
        vo.setHeader(header);

        //获取班次数据
        List<SysDictVO> classList = getClassList();
        Map<Long, SysDictVO> classMap = Maps.newHashMap();
        classList.forEach(ele -> classMap.put(ele.getId(), ele));

        //获取车间数据
        Set<Integer> productLineSet = dataList.stream().map(FurtherIngredientMapperVO::getProductLineId).collect(Collectors.toSet());
        Map<Integer, SysOfficeVO> productLineMap = FeignOptional.of(remoteOfficeFeign.listById(productLineSet)).get();
        //根据车间去划分
        Map<Integer, List<SysOfficeVO>> workShopMap = productLineMap.values().stream().collect(Collectors.groupingBy(SysOfficeVO::getParentId));
        workShopMap.forEach((workshop, productLineList) -> {
            //根据产线遍历
            for (SysOfficeVO productLine : productLineList) {
                //产线统计
                Map<Long, Map<Long, BigDecimal>> productCountMap1 = Maps.newHashMap();
                Map<Long, Map<Long, BigDecimal>> productCountMap2 = Maps.newHashMap();
                //过滤出产线数据并将产线数据分成不同班次
                Map<Long, List<FurtherIngredientMapperVO>> lineDataMap = dataList.stream().filter(row -> productLine.getId().equals(row.getProductLineId())).collect(Collectors.groupingBy(FurtherIngredientMapperVO::getClassesId));
                //遍历不同的班次
                for (Map.Entry<Long, List<FurtherIngredientMapperVO>> classEntry : lineDataMap.entrySet()) {
                    //班次统计
                    Map<Long, Map<Long, BigDecimal>> classesCountMap1 = Maps.newHashMap();
                    Map<Long, Map<Long, BigDecimal>> classesCountMap2 = Maps.newHashMap();

                    Map<Long, List<FurtherIngredientMapperVO>> produceOrderMap = classEntry.getValue().stream().collect(Collectors.groupingBy(FurtherIngredientMapperVO::getProduceOrderId));
                    //获取班次的数据
                    SysDictVO sysDictVO = classMap.get(classEntry.getKey());
                    //遍历生产订单物料分组
                    for (Map.Entry<Long, List<FurtherIngredientMapperVO>> po : produceOrderMap.entrySet()) {
                        List<FurtherValueVO> processModule = Lists.newArrayList();
                        Map<Long, Map<Long, ProduceOrderProcessNumVO>> processNumMap = numConsumeMap.getOrDefault(po.getKey(), defaultProcessNumMap);
                        //遍历工序物料集合
                        materialProcessMap.forEach((k, v) -> {
                            List<FurtherValueStrVO> strList = Lists.newArrayList();
                            Map<Long, ProduceOrderProcessNumVO> materialNumMap = processNumMap.getOrDefault(k, defaultMaterialNumMap);
                            for (BaseSelectVO<Long> mBase : v) {
                                ProduceOrderProcessNumVO materialBase = materialNumMap.getOrDefault(mBase.getId(), defaultProduceOrderProcessNumVO);
                                strList.add(new FurtherValueStrVO(Arrays.asList(BigDecimalUtil.format(materialBase.getQuantity()), BigDecimalUtil.format(materialBase.getAvaQuantity()))));
                                BigDecimalUtil.biAccumulate(classesCountMap1, k, mBase.getId(), materialBase.getQuantity());
                                BigDecimalUtil.biAccumulate(classesCountMap2, k, mBase.getId(), materialBase.getAvaQuantity());
                                BigDecimalUtil.biAccumulate(productCountMap1, k, mBase.getId(), materialBase.getQuantity());
                                BigDecimalUtil.biAccumulate(productCountMap2, k, mBase.getId(), materialBase.getAvaQuantity());
                            }
                            processModule.add(new FurtherValueVO(strList));
                        });
                        EqpSpareVO specDataVo = specMap.getOrDefault(po.getValue().get(0).getMaterialId(), defaultEqpSpareVO);
                        List<String> specDataList = Arrays.asList(BigDecimalUtil.format(specDataVo.getLengthQuantity()), BigDecimalUtil.format(specDataVo.getWideQuantity()), BigDecimalUtil.format(specDataVo.getHighQuantity()));
                        tableData.add(getFurtherIngredientReportListVO(Arrays.asList(productLine.getParentName(), productLine.getOfficeName(), sysDictVO.getName()), PmReportConstant.TYPE_0, processModule, specDataList));
                    }
                    List<FurtherValueVO> classModuleList = Lists.newArrayList();
                    materialProcessMap.forEach((k, v) -> {
                        List<FurtherValueStrVO> strList = Lists.newArrayList();
                        for (BaseSelectVO<Long> mBase : v) {
                            BigDecimal classBig1 = classesCountMap1.get(k).get(mBase.getId());
                            BigDecimal classBig2 = classesCountMap2.get(k).get(mBase.getId());
                            strList.add(new FurtherValueStrVO(Arrays.asList(BigDecimalUtil.format(classBig1), BigDecimalUtil.format(classBig2))));
                        }
                        classModuleList.add(new FurtherValueVO(strList));
                    });
                    tableData.add(getFurtherIngredientReportListVO(Arrays.asList(productLine.getParentName(), productLine.getOfficeName(), sysDictVO.getName()), PmReportConstant.TYPE_1, classModuleList, Arrays.asList("合计", "合计", "合计")));
                }

                List<FurtherValueVO> lineModuleList = Lists.newArrayList();
                materialProcessMap.forEach((k, v) -> {
                    List<FurtherValueStrVO> strList = Lists.newArrayList();
                    for (BaseSelectVO<Long> mBase : v) {
                        BigDecimal lineBig1 = productCountMap1.get(k).get(mBase.getId());
                        BigDecimal lineBig2 = productCountMap2.get(k).get(mBase.getId());
                        strList.add(new FurtherValueStrVO(Arrays.asList(BigDecimalUtil.format(lineBig1), BigDecimalUtil.format(lineBig2))));
                    }
                    lineModuleList.add(new FurtherValueVO(strList));
                });
                tableData.add(getFurtherIngredientReportListVO(Arrays.asList(productLine.getParentName(), productLine.getOfficeName(), "合计"), PmReportConstant.TYPE_2, lineModuleList, Arrays.asList("合计", "合计", "合计")));
            }
        });
        vo.setTableData(tableData);
        return vo;
    }

    @Override
    public List<SubstrateIngredientVO> subIngredientReport(PmsDayReportDTO dto) {
        //根据条件确定生产订单
        List<SubstrateIngredientVO> dataList = completeDeliveryService.subIngredientReport(dto);
        List<TupleDTO<Long, Long>> conditionList = Lists.newArrayList();
        dataList.forEach(ele -> {
            //根据订单和子物料查询工位信息【订单的子物料关联工序，工序关联可执行工位】
            if (Objects.nonNull(ele.getMaterialId()) && CollUtil.isNotEmpty(ele.getProduceOrderIdSet())) {
                for (Long poId : ele.getProduceOrderIdSet()) {
                    conditionList.add(new TupleDTO<>(poId, ele.getMaterialId()));
                }
            }
        });
        Map<Long, Set<Long>> materialStationMap = pmsWorkOrderBomService.materialOrderStation(conditionList);
        log.info("{}", materialStationMap);
        return dataList;
    }

    /**
     * 构造FurtherIngredientReportListVO数据
     *
     * @param nameList
     * @param type
     * @param module
     * @return
     */
    private FurtherIngredientReportListVO getFurtherIngredientReportListVO(List<String> nameList, Integer type, List<FurtherValueVO> module, List<String> defaultSpecList) {
        FurtherIngredientReportListVO row = new FurtherIngredientReportListVO();
        row.setType(type);
        row.setWorkshopName(nameList.get(0));
        row.setProductLineName(nameList.get(1));
        row.setClassName(nameList.get(2));
        row.setModule(module);
        row.setSpecList(defaultSpecList);
        return row;
    }


    /**
     * 统计入库
     *
     * @param classesId     班次
     * @param classCountMap 统计map
     * @param orDefaultMap  数据map
     */
    private void countIn(Long classesId, Map<Long, Map<Integer, BigDecimal>> classCountMap, Map<Long, BigDecimal> orDefaultMap) {
        BigDecimalUtil.biAccumulate(classCountMap, 1L, 0, orDefaultMap.get(classesId));
        BigDecimalUtil.biAccumulate(classCountMap, 1L, 1, orDefaultMap.get(classesId));
        BigDecimalUtil.biAccumulate(classCountMap, 1L, 2, orDefaultMap.get(classesId));
    }

    /**
     * 生成班次产线车间的合计
     *
     * @param pmProcessList 工序集合
     * @param productLine   产线信息
     * @param classCountMap 班次map
     * @param sysDictVO     班次信息
     * @return 合计
     */
    private FurtherProduceReportListVO getFurtherProduceReportListVO(List<PmProcess> pmProcessList, SysOfficeVO productLine, Map<Long, Map<Integer, BigDecimal>> classCountMap, SysDictVO sysDictVO, List<String> defaultSpecList) {
        List<FurtherValueStrVO> classCountModuleList = Lists.newArrayList();
        for (PmProcess pmProcess : pmProcessList) {
            List<String> classModuleList = Lists.newArrayList();
            classModuleList.add(BigDecimalUtil.format(classCountMap.get(pmProcess.getId()).get(0)));
            classModuleList.add(BigDecimalUtil.format(classCountMap.get(pmProcess.getId()).get(1)));
            classModuleList.add(BigDecimalUtil.format(classCountMap.get(pmProcess.getId()).get(2)));
            classModuleList.add(BigDecimalUtil.format(classCountMap.get(pmProcess.getId()).get(3)));
            classCountModuleList.add(new FurtherValueStrVO(classModuleList));
        }
        Map<Integer, BigDecimal> classCountChildMap = classCountMap.get(0L);
        Map<Integer, BigDecimal> classInChildMap = classCountMap.get(1L);
        List<String> classCountChildList = Lists.newArrayList();
        classCountChildList.add(BigDecimalUtil.format(classCountChildMap.get(0)));
        classCountChildList.add(BigDecimalUtil.format(classCountChildMap.get(1)));
        classCountChildList.add(BigDecimalUtil.format(classCountChildMap.get(2)));
        classCountChildList.add(BigDecimalUtil.format(classCountChildMap.get(3)));
        List<String> classInChildList = Lists.newArrayList();
        classInChildList.add(BigDecimalUtil.format(classInChildMap.get(0)));
        classInChildList.add(BigDecimalUtil.format(classInChildMap.get(1)));
        classInChildList.add(BigDecimalUtil.format(classInChildMap.get(2)));
        classCountModuleList.add(new FurtherValueStrVO(classCountChildList));
        classCountModuleList.add(new FurtherValueStrVO(classInChildList));
        return createFurtherProduceReportListVO(Arrays.asList(productLine.getParentName(), productLine.getOfficeName(), sysDictVO.getName()), PmReportConstant.TYPE_1, classCountModuleList, defaultSpecList);
    }

    /**
     * 合计列统计数据
     *
     * @param pmProcessId 合计
     * @param countMap    统计map
     * @param rowCountMap 行
     */
    private void pmColumnCount(Map<Long, Map<Integer, BigDecimal>> countMap, Long pmProcessId, Map<Integer, BigDecimal> rowCountMap) {
        BigDecimalUtil.biAccumulate(countMap, pmProcessId, 0, rowCountMap.get(0));
        BigDecimalUtil.biAccumulate(countMap, pmProcessId, 1, rowCountMap.get(1));
        BigDecimalUtil.biAccumulate(countMap, pmProcessId, 2, rowCountMap.get(2));
        BigDecimalUtil.biAccumulate(countMap, pmProcessId, 3, rowCountMap.get(3));
    }

    /**
     * 工序行统计数据
     *
     * @param countMap    求和map
     * @param pmProcessId 工序主键
     * @param dataVo      数据vo
     * @param passNum     合格数
     * @param passRate    合格率
     */
    private void pmRowCount(Map<Long, Map<Integer, BigDecimal>> countMap, Long pmProcessId, FurtherProduceReportDataVO dataVo, BigDecimal passNum, BigDecimal passRate) {
        BigDecimalUtil.biAccumulate(countMap, pmProcessId, 0, dataVo.getProcessQuantity());
        BigDecimalUtil.biAccumulate(countMap, pmProcessId, 1, passNum);
        BigDecimalUtil.biAccumulate(countMap, pmProcessId, 2, dataVo.getBadnessQuantity());
        BigDecimalUtil.biAccumulate(countMap, pmProcessId, 3, passRate.multiply(BigDecimal.valueOf(100)));
    }

    /**
     * 工序列统计数据
     *
     * @param rowCountMap 列统计map
     * @param dataVo      数据vo
     * @param passNum     合格数
     * @param passRate    合格率
     */
    private void pmColumnCount(Map<Integer, BigDecimal> rowCountMap, FurtherProduceReportDataVO dataVo, BigDecimal passNum, BigDecimal passRate) {
        BigDecimalUtil.accumulate(rowCountMap, 0, dataVo.getProcessQuantity());
        BigDecimalUtil.accumulate(rowCountMap, 1, passNum);
        BigDecimalUtil.accumulate(rowCountMap, 2, dataVo.getBadnessQuantity());
        BigDecimalUtil.accumulate(rowCountMap, 3, passRate.multiply(BigDecimal.valueOf(100)));
    }


    /**
     * 创建对象
     *
     * @param nameList 名称【车间，产线，班次】
     * @param type     类型
     * @param module   工序数据
     * @return vo
     */
    private FurtherProduceReportListVO createFurtherProduceReportListVO(List<String> nameList, Integer type, List<FurtherValueStrVO> module, List<String> specList) {
        FurtherProduceReportListVO row = new FurtherProduceReportListVO();
        row.setSpecList(specList);
        row.setType(type);
        row.setWorkshopName(nameList.get(0));
        row.setProductLineName(nameList.get(1));
        row.setClassName(nameList.get(2));
        row.setModule(module);
        return row;
    }


    @Override
    public EnergyCountVO consume(EnergyCountDTO dto) {
        Assert.isTrue(PmReportConstant.DataGaugeTypeEnum.all().contains(dto.getDataGaugeType()), "统计维度不存在");
        Assert.isTrue(PmReportConstant.DateRangeTypeEnum.all().contains(dto.getDateRangeType()), "日期范围不存在");
        //获取日期
        List<LocalDateTime> dateRangeList = LocalDateUtil.getFirstAndLastDate(dto.getDateRangeType());

        //获取产线数据
        List<SysOfficeVO> productLineList = productLineList(dto.getWorkshopId());
        Assert.notNull(productLineList, "车间下暂无产线");

        //产线按照treeSort排序
        productLineList.sort(Comparator.comparing(SysOfficeVO::getTreeSort));

        //定义返回vo
        EnergyCountVO resVo = null;
        //根据产线id查询分组数据
        List<Integer> productLineIdList = productLineList.stream().map(SysOfficeVO::getId).collect(Collectors.toList());
        List<EnergyGroupListVO> energyGroupList = pmsReportWorkConsumeService.energyGroup(productLineIdList, dateRangeList);
        if (CollUtil.isNotEmpty(energyGroupList)) {
            //获取班组信息-甲班 乙班 丙班 组成map
            SysDictDTO sysDictDTO = new SysDictDTO();
            sysDictDTO.setType(PmManagerConstant.SYS_TEAM);
            Map<String, SysDictVO> sysDictMap = FeignOptional.of(remoteDictFeignApi.listByCode(sysDictDTO)).get();
            //数据字典数据对应
            Map<Long, SysDictVO> dictMap = Maps.newHashMap();
            sysDictMap.forEach((k, v) -> dictMap.put(v.getId(), v));
            List<SysDictVO> sysDictVOList = dictMap.values().stream().collect(Collectors.toList());
            //按照sort排序
            sysDictVOList.sort(Comparator.comparing(SysDictVO::getSort));
            if (PmReportConstant.DataGaugeTypeEnum.CLASS_GROUP.getCode().equals(dto.getDataGaugeType())) {
                //按照班组统计
                resVo = classGroup(sysDictVOList, productLineList, energyGroupList);
            } else {
                //按照产线统计
                resVo = productLine(sysDictVOList, productLineList, energyGroupList);
            }

        }
        return resVo;
    }

    @Override
    public OutputCountVO outputCount(EnergyCountDTO dto) {
        //查询车间下所有的产线
        SysOfficeParentDTO sysOfficeParentDTO = new SysOfficeParentDTO();
        sysOfficeParentDTO.setId(dto.getWorkshopId());
        sysOfficeParentDTO.setOfficeType(PmManagerConstant.STATE_9);
        List<SysOfficeVO> productLineList = remoteOfficeFeign.listByParentId(sysOfficeParentDTO).getData();
        if (CollectionUtils.isEmpty(productLineList)) {
            throw new BusinessException("该车间没有绑定产线");
        }

        //获取日期
        List<LocalDateTime> dateRangeList = LocalDateUtil.getFirstAndLastDate(dto.getDateRangeType());

        //根据统计维度获取数据
        OutputCountVO outputCountVO = new OutputCountVO();
        if (PmReportConstant.DataGaugeTypeEnum.PRODUCT_LINE.getCode().equals(dto.getDataGaugeType())) {
            outputCountVO = productLineCount(productLineList, dateRangeList);
        } else {
            outputCountVO = classGroupCount(productLineList, dateRangeList);
        }

        return outputCountVO;
    }

    private List<PmsReportWork> getReportWorkList(Set<Integer> productLineIdList, List<LocalDateTime> dateRangeList, List<SFunction<PmsReportWork, ?>> columnList) {

        return pmsReportWorkService.list(new QueryWrapper<PmsReportWork>()
                .select("class_group_id as classGroupId,product_line_id as productLineId,sum(process_quantity) as processQuantity")
                .lambda().in(PmsReportWork::getProductLineId, productLineIdList)
                .ge(PmsReportWork::getCreateTime, dateRangeList.get(0))
                .le(PmsReportWork::getCreateTime, dateRangeList.get(1)).groupBy(columnList));
    }

    private OutputCountVO productLineCount(List<SysOfficeVO> productLineList, List<LocalDateTime> dateRangeList) {

        //获取产线id和产线名称
        Set<Integer> productLineIdList = productLineList.stream().map(SysOfficeVO::getId).collect(Collectors.toSet());
        List<String> productLineNameList = productLineList.stream().map(SysOfficeVO::getOfficeName).collect(Collectors.toList());
        //获取班组
        List<SysDictVO> sysDictVOList = remoteDictFeignApi.listByType("sys_team").getData();


        //添加分组字段获取数据
        List<SFunction<PmsReportWork, ?>> columnList = new ArrayList<>();
        columnList.add(PmsReportWork::getClassGroupId);
        columnList.add(PmsReportWork::getProductLineId);
        List<PmsReportWork> pmsReportWorkList = getReportWorkList(productLineIdList, dateRangeList, columnList);

        OutputCountVO outputCountVO = new OutputCountVO();
        List<OutputCountListVO> outputCountListVOList = new ArrayList<>();
        //获取车间下所有的产线名称
        outputCountVO.setDx(productLineNameList);
        outputCountVO.setDataList(outputCountListVOList);


        for (SysDictVO sysDictVO : sysDictVOList) {
            //获取产量数据
            OutputCountListVO reportWork = getReportWork(sysDictVO, pmsReportWorkList, productLineList);
            outputCountListVOList.add(reportWork);
        }

        //获取入库数
        List<CompleteStorageCountVO> completeStorageCount = pmsProduceOrderMapper.selectCompleteStorageCount(new ArrayList<>(productLineIdList)
                , dateRangeList.get(0)
                , dateRangeList.get(1)
                , PmReportConstant.DataGaugeTypeEnum.PRODUCT_LINE.getCode());
        for (SysDictVO sysDictVO : sysDictVOList) {
            OutputCountListVO listVO = new OutputCountListVO();
            List<BigDecimal> quantityList = new ArrayList<>();
            listVO.setDataType("入库数");
            listVO.setDataName(sysDictVO.getName());

            List<CompleteStorageCountVO> storageCountVOList = completeStorageCount.stream().filter(storageCountVO -> sysDictVO.getId().equals(storageCountVO.getClassGroupId())).collect(Collectors.toList());
            for (SysOfficeVO sysOfficeVO : productLineList) {
                if (CollectionUtils.isEmpty(storageCountVOList)) {
                    quantityList.add(BigDecimal.ZERO);
                    continue;
                }
                Optional<CompleteStorageCountVO> present = storageCountVOList.stream().filter(storageCountVO -> storageCountVO.getProductionLineId().equals(sysOfficeVO.getId())).findAny();
                if (present.isPresent()) {
                    quantityList.add(present.get().getQuantity());
                } else {
                    quantityList.add(BigDecimal.ZERO);
                }
            }
            listVO.setQuantityList(quantityList);
            outputCountListVOList.add(listVO);
        }

        //获取拉引量
        for (SysDictVO sysDictVO : sysDictVOList) {
            OutputCountListVO listVO = new OutputCountListVO();
            listVO.setDataName(sysDictVO.getName());
            listVO.setDataType("拉引量");
            List<BigDecimal> quantityList = new ArrayList<>();
            for (SysOfficeVO sysOfficeVO : productLineList) {
                quantityList.add(BigDecimal.ZERO);
            }
            listVO.setQuantityList(quantityList);

            outputCountListVOList.add(listVO);
        }
        return outputCountVO;
    }

    private OutputCountVO classGroupCount(List<SysOfficeVO> productLineList, List<LocalDateTime> dateRangeList) {
        //获取产线id
        Set<Integer> productLineIdList = productLineList.stream().map(SysOfficeVO::getId).collect(Collectors.toSet());
        List<SysDictVO> sysDictVOList = remoteDictFeignApi.listByType("sys_team").getData();
        //获取班组名称
        List<String> classGroupNameList = sysDictVOList.stream().map(SysDictVO::getName).collect(Collectors.toList());
        //添加分组字段获取数据
        List<SFunction<PmsReportWork, ?>> columnList = new ArrayList<>();
        columnList.add(PmsReportWork::getProductLineId);
        columnList.add(PmsReportWork::getClassGroupId);
        List<PmsReportWork> pmsReportWorkList = getReportWorkList(productLineIdList, dateRangeList, columnList);

        List<OutputCountListVO> outputCountListVOList = new ArrayList<>();
        OutputCountVO outputCountVO = new OutputCountVO();
        outputCountVO.setDx(classGroupNameList);
        outputCountVO.setDataList(outputCountListVOList);
        //获取产量数据
        for (SysOfficeVO sysOfficeVO : productLineList) {
            OutputCountListVO listVO = new OutputCountListVO();
            List<BigDecimal> quantityList = new ArrayList<>();

            listVO.setDataType("产量");
            listVO.setDataName(sysOfficeVO.getOfficeName());

            List<PmsReportWork> reportWorkList = pmsReportWorkList.stream().filter(reportWork -> reportWork.getProductLineId().equals(sysOfficeVO.getId())).collect(Collectors.toList());
            for (SysDictVO sysDictVO : sysDictVOList) {
                if (CollectionUtils.isEmpty(reportWorkList)) {
                    quantityList.add(BigDecimal.ZERO);
                    continue;
                }
                Optional<PmsReportWork> present = reportWorkList.stream().filter(reportWork -> sysDictVO.getId().equals(reportWork.getClassGroupId())).findAny();
                if (present.isPresent()) {
                    quantityList.add(present.get().getProcessQuantity());
                } else {
                    quantityList.add(BigDecimal.ZERO);
                }
            }
            listVO.setQuantityList(quantityList);
            outputCountListVOList.add(listVO);
        }

        //获取入库数
        List<CompleteStorageCountVO> completeStorageCount = pmsProduceOrderMapper.selectCompleteStorageCount(new ArrayList<>(productLineIdList)
                , dateRangeList.get(0)
                , dateRangeList.get(1)
                , PmReportConstant.DataGaugeTypeEnum.CLASS_GROUP.getCode());

        for (SysOfficeVO sysOfficeVO : productLineList) {
            OutputCountListVO listVO = new OutputCountListVO();
            List<BigDecimal> quantityList = new ArrayList<>();
            listVO.setDataType("入库数");
            listVO.setDataName(sysOfficeVO.getOfficeName());

            List<CompleteStorageCountVO> storageCountVOList = completeStorageCount.stream().filter(storageCountVO -> sysOfficeVO.getId().equals(storageCountVO.getProductionLineId())).collect(Collectors.toList());
            for (SysDictVO sysDictVO : sysDictVOList) {
                if (CollectionUtils.isEmpty(storageCountVOList)) {
                    quantityList.add(BigDecimal.ZERO);
                    continue;
                }
                Optional<CompleteStorageCountVO> present = storageCountVOList.stream().filter(storageCountVO -> storageCountVO.getClassGroupId().equals(sysDictVO.getId())).findAny();
                if (present.isPresent()) {
                    quantityList.add(present.get().getQuantity());
                } else {
                    quantityList.add(BigDecimal.ZERO);
                }
            }
            listVO.setQuantityList(quantityList);
            outputCountListVOList.add(listVO);
        }

        //获取拉引量
        for (SysOfficeVO sysOfficeVO : productLineList) {
            OutputCountListVO listVO = new OutputCountListVO();
            listVO.setDataType("拉引量");
            listVO.setDataName(sysOfficeVO.getOfficeName());
            List<BigDecimal> quantityList = new ArrayList<>();
            for (SysDictVO sysDictVO : sysDictVOList) {
                quantityList.add(BigDecimal.ZERO);
            }
            listVO.setQuantityList(quantityList);
            outputCountListVOList.add(listVO);

        }

        return outputCountVO;
    }

    private OutputCountListVO getReportWork(SysDictVO sysDictVO, List<PmsReportWork> pmsReportWorkList, List<SysOfficeVO> productLineList) {
        OutputCountListVO listVO = new OutputCountListVO();
        List<BigDecimal> quantityList = new ArrayList<>();
        listVO.setDataType("产量");
        listVO.setDataName(sysDictVO.getName());

        List<PmsReportWork> workFilterList = pmsReportWorkList.stream().filter(reportWork -> sysDictVO.getId().equals(reportWork.getClassGroupId())).collect(Collectors.toList());
        for (SysOfficeVO sysOfficeVO : productLineList) {
            if (CollectionUtils.isEmpty(workFilterList)) {
                quantityList.add(BigDecimal.ZERO);
                continue;
            }
            Optional<PmsReportWork> present = workFilterList.stream().filter(pmsReportWork -> pmsReportWork.getProductLineId().equals(sysOfficeVO.getId())).findAny();
            if (present.isPresent()) {
                quantityList.add(present.get().getProcessQuantity());
            } else {
                quantityList.add(BigDecimal.ZERO);
            }
        }
        listVO.setQuantityList(quantityList);

        return listVO;
    }

    /**
     * 按照产线统计
     *
     * @param productLineList 所有产线
     * @param energyGroupList 数据集合
     * @return
     */
    private EnergyCountVO productLine(List<SysDictVO> sysDictVOList, List<SysOfficeVO> productLineList, List<EnergyGroupListVO> energyGroupList) {
        //定义返回vo
        EnergyCountVO resVo = new EnergyCountVO();
        //遍历
        List<String> dxList = Lists.newArrayList();
        List<BigDecimal> pullForceList = Lists.newArrayList();

        //班次分组map
        Map<Long, List<EnergyGroupListVO>> classGroupDataMap = energyGroupList.stream().collect(Collectors.groupingBy(EnergyGroupListVO::getClassGroupId));

        //x轴的数据获取，拉引量获取
        for (SysOfficeVO sysOfficeVO : productLineList) {
            dxList.add(sysOfficeVO.getOfficeName());
            pullForceList.add(BigDecimal.ZERO);
        }
        //定义变量，水电油气
        EnergySeriesDataVO seriesDataVO = new EnergySeriesDataVO();
        List<EnergyCountListVO> electric = Lists.newArrayList();
        List<EnergyCountListVO> water = Lists.newArrayList();
        List<EnergyCountListVO> oil = Lists.newArrayList();
        List<EnergyCountListVO> gas = Lists.newArrayList();
        //遍历班次
        for (SysDictVO sysDictVO : sysDictVOList) {
            //获取产线分组的数据，也许会为空
            List<EnergyGroupListVO> classGroupList = classGroupDataMap.get(sysDictVO.getId());
            //0耗电量 1耗水量 2耗油量 3耗气量
            electric.add(countClassGroupGauge(productLineList, sysDictVO.getName(), PmReportConstant.ELECTRIC, 0, classGroupList));
            water.add(countClassGroupGauge(productLineList, sysDictVO.getName(), PmReportConstant.WATER, 1, classGroupList));
            oil.add(countClassGroupGauge(productLineList, sysDictVO.getName(), PmReportConstant.OIL, 2, classGroupList));
            gas.add(countClassGroupGauge(productLineList, sysDictVO.getName(), PmReportConstant.GAS, 3, classGroupList));
        }

        seriesDataVO.setElectric(electric);
        seriesDataVO.setWater(water);
        seriesDataVO.setOil(oil);
        seriesDataVO.setGas(gas);

        List<List<BigDecimal>> reactiveList = Lists.newArrayList();

        LinkedHashMap<Integer, BigDecimal> waterSumMap = lineSum(seriesDataVO.getWater());
        LinkedHashMap<Integer, BigDecimal> electricSumMap = lineSum(seriesDataVO.getElectric());
        LinkedHashMap<Integer, BigDecimal> oilSumMap = lineSum(seriesDataVO.getOil());
        LinkedHashMap<Integer, BigDecimal> gasSumMap = lineSum(seriesDataVO.getGas());

        for (int i = 0; i < dxList.size(); i++) {
            List<BigDecimal> dataList = Lists.newArrayList();
            dataList.add(waterSumMap.get(i));
            dataList.add(electricSumMap.get(i));
            dataList.add(oilSumMap.get(i));
            dataList.add(gasSumMap.get(i));
            reactiveList.add(dataList);
        }
        resVo.setReactive(reactiveList);

        resVo.setSeriesData(seriesDataVO);
        resVo.setDx(dxList);
        resVo.setPullForceList(pullForceList);
        return resVo;
    }

    /**
     * 按照班组统计
     *
     * @param productLineList 产线集合
     * @param energyGroupList 班组集合
     * @return
     */
    private EnergyCountVO classGroup(List<SysDictVO> sysDictVOList, List<SysOfficeVO> productLineList, List<EnergyGroupListVO> energyGroupList) {
        //定义返回vo
        EnergyCountVO resVo = new EnergyCountVO();
        //产线分组map
        Map<Integer, List<EnergyGroupListVO>> productLineMap = energyGroupList.stream().collect(Collectors.groupingBy(EnergyGroupListVO::getProductLineId));

        //遍历
        List<String> dxList = Lists.newArrayList();
        List<BigDecimal> pullForceList = Lists.newArrayList();

        //x轴的数据获取，拉引量获取
        for (SysDictVO sysDictVO : sysDictVOList) {
            dxList.add(sysDictVO.getName());
            pullForceList.add(BigDecimal.ZERO);
        }

        //定义变量，水电油气
        EnergySeriesDataVO seriesDataVO = new EnergySeriesDataVO();
        List<EnergyCountListVO> electric = Lists.newArrayList();
        List<EnergyCountListVO> water = Lists.newArrayList();
        List<EnergyCountListVO> oil = Lists.newArrayList();
        List<EnergyCountListVO> gas = Lists.newArrayList();

        //遍历产线
        for (SysOfficeVO productLine : productLineList) {
            //获取产线分组的数据，也许会为空
            List<EnergyGroupListVO> productGroup = productLineMap.get(productLine.getId());
            //0耗电量 1耗水量 2耗油量 3耗气量
            electric.add(countProductLineGauge(sysDictVOList, productLine.getOfficeName(), PmReportConstant.ELECTRIC, 0, productGroup));
            water.add(countProductLineGauge(sysDictVOList, productLine.getOfficeName(), PmReportConstant.WATER, 1, productGroup));
            oil.add(countProductLineGauge(sysDictVOList, productLine.getOfficeName(), PmReportConstant.OIL, 2, productGroup));
            gas.add(countProductLineGauge(sysDictVOList, productLine.getOfficeName(), PmReportConstant.GAS, 3, productGroup));
        }
        seriesDataVO.setElectric(electric);
        seriesDataVO.setWater(water);
        seriesDataVO.setOil(oil);
        seriesDataVO.setGas(gas);
        resVo.setSeriesData(seriesDataVO);

        List<List<BigDecimal>> reactiveList = Lists.newArrayList();

        LinkedHashMap<Integer, BigDecimal> waterSumMap = lineSum(seriesDataVO.getWater());
        LinkedHashMap<Integer, BigDecimal> electricSumMap = lineSum(seriesDataVO.getElectric());
        LinkedHashMap<Integer, BigDecimal> oilSumMap = lineSum(seriesDataVO.getOil());
        LinkedHashMap<Integer, BigDecimal> gasSumMap = lineSum(seriesDataVO.getGas());

        for (int i = 0; i < dxList.size(); i++) {
            List<BigDecimal> dataList = Lists.newArrayList();
            dataList.add(waterSumMap.get(i));
            dataList.add(electricSumMap.get(i));
            dataList.add(oilSumMap.get(i));
            dataList.add(gasSumMap.get(i));
            reactiveList.add(dataList);
        }
        resVo.setReactive(reactiveList);
        resVo.setDx(dxList);
        resVo.setPullForceList(pullForceList);
        return resVo;
    }

    /**
     * 产线求和-用于汇总能耗统计
     * @param data data
     * @return
     */
    private LinkedHashMap<Integer, BigDecimal> lineSum(List<EnergyCountListVO> data) {
        LinkedHashMap<Integer, BigDecimal> sumMap = new LinkedHashMap<>();
        //获取所有的产线数据
        List<List<BigDecimal>> collect = data.stream().map(v -> v.getData()).collect(Collectors.toList());
        for (int i = 0; i < collect.size(); i++) {
            List<BigDecimal> coList = collect.get(i);
            for (int j = 0; j < coList.size(); j++) {
                //j代表产线
                BigDecimalUtil.accumulate(sumMap, j, coList.get(j));
            }
        }
        return sumMap;

    }

    /**
     * 统计维度
     *
     * @param sysDictVOList   班组信息
     * @param productLineName 产线名字
     * @param typeName        类型名称
     * @param type            类型信息
     * @param productGroup    累计分组结果
     * @return
     */
    private EnergyCountListVO countProductLineGauge(List<SysDictVO> sysDictVOList, String
            productLineName, String typeName, Integer type, List<EnergyGroupListVO> productGroup) {
        EnergyCountListVO vo = new EnergyCountListVO();
        vo.setName(productLineName);
        vo.setStack(typeName);
        vo.setType(PmReportConstant.STR_BAR);
        Map<Long, BigDecimal> dataMap = Maps.newHashMap();
        if (CollUtil.isNotEmpty(productGroup)) {
            productGroup.forEach(ele -> {
                if (type.equals(ele.getType())) {
                    dataMap.put(ele.getClassGroupId(), ele.getQuantity());
                }
            });
        }
        List<BigDecimal> dataList = Lists.newArrayList();
        for (SysDictVO sysDictVO : sysDictVOList) {
            dataList.add(dataMap.getOrDefault(sysDictVO.getId(), BigDecimal.ZERO));
        }
        vo.setData(dataList);
        return vo;
    }

    /**
     * 统计维度
     *
     * @param productLineList 产线信息
     * @param name            产线名字
     * @param typeName        类型名称
     * @param type            类型信息
     * @param productGroup    累计分组结果
     * @return
     */
    private EnergyCountListVO countClassGroupGauge(List<SysOfficeVO> productLineList, String name, String
            typeName, Integer type, List<EnergyGroupListVO> productGroup) {
        EnergyCountListVO vo = new EnergyCountListVO();
        vo.setName(name);
        vo.setStack(typeName);
        vo.setType(PmReportConstant.STR_BAR);
        Map<Integer, BigDecimal> dataMap = Maps.newHashMap();
        if (CollUtil.isNotEmpty(productGroup)) {
            productGroup.forEach(ele -> {
                if (type.equals(ele.getType())) {
                    dataMap.put(ele.getProductLineId(), ele.getQuantity());
                }
            });
        }
        List<BigDecimal> dataList = Lists.newArrayList();
        for (SysOfficeVO sysOfficeVO : productLineList) {
            dataList.add(dataMap.getOrDefault(sysOfficeVO.getId(), BigDecimal.ZERO));
        }
        vo.setData(dataList);
        return vo;
    }

    /**
     * 根据车间去查询所有产线
     *
     * @param workShopId 车间id
     * @return
     */
    private List<SysOfficeVO> productLineList(Integer workShopId) {
        SysOfficeParentDTO sysOfficeParentDTO = new SysOfficeParentDTO();
        sysOfficeParentDTO.setId(workShopId);
        sysOfficeParentDTO.setOfficeType(PmManagerConstant.STATE_9);
        return FeignOptional.of(remoteOfficeFeign.listByParentId(sysOfficeParentDTO)).get();
    }

}
