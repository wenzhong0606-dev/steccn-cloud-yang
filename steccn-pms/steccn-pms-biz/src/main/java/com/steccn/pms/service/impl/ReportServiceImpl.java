package com.steccn.pms.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.authserver.util.SteccnTableAssertUtil;
import com.steccn.common.constant.FeignConstant;
import com.steccn.common.constant.SysDictConstant;
import com.steccn.common.dto.*;
import com.steccn.common.util.*;
import com.steccn.common.vo.BaseSelectVO;
import com.steccn.common.vo.EpChartGraphVO;
import com.steccn.data.feign.RemoteDataPlatformFeignApi;
import com.steccn.dp.feign.RemoteDpFeignApi;
import com.steccn.oa.dto.OaClassUserNameDTO;
import com.steccn.oa.feign.OaInfoService;
import com.steccn.platform.dto.SysDictDTO;
import com.steccn.platform.dto.SysOfficeParentDTO;
import com.steccn.platform.feign.*;
import com.steccn.platform.vo.*;
import com.steccn.pms.constant.PmManagerConstant;
import com.steccn.pms.constant.PmReportConstant;
import com.steccn.pms.domain.*;
import com.steccn.pms.dto.*;
import com.steccn.pms.mapper.PmsProduceOrderMapper;
import com.steccn.pms.mapper.PmsReportWorkMapper;
import com.steccn.pms.service.*;
import com.steccn.pms.util.UnitConvertUtil;
import com.steccn.pms.vo.*;
import com.steccn.qms.constant.QtManagerConstant;
import com.steccn.qms.dto.OutputQualityDetailDTO;
import com.steccn.qms.feign.RemoteCheckTaskDetailFeignApi;
import com.steccn.qms.util.MysqlDateKeyUtil;
import com.steccn.qms.util.XAxisUtil;
import com.steccn.qms.vo.OutputQualityDetailVO;
import com.steccn.wms.constant.WmsManagerConstant;
import com.steccn.wms.feign.RemoteWmsStorageReceiptFeignApi;
import com.steccn.wms.feign.RemoteWmsTransferSlipFeignApi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.BitFieldSubCommands;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import reactor.util.function.Tuple5;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
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
    private final PmsProduceOrderService pmsProduceOrderService;
    private final PmProcessService pmProcessService;
    private final PmsCompleteStorageInfoService pmsCompleteStorageInfoService;
    private final PmsWorkOrderBomService pmsWorkOrderBomService;
    private final PmsCompleteDeliveryService completeDeliveryService;
    private final RemoteSpareUnitConvertFeignApi remoteSpareUnitConvertFeignApi;
    private final PmsProduceOrderCraftService pmsProduceOrderCraftService;
    private final PmsMaterialRequisitionInfoService pmsMaterialRequisitionInfoService;
    private final RemoteCheckTaskDetailFeignApi remoteCheckTaskDetailFeignApi;
    private final OaInfoService oaInfoService;
    private final RemoteSysOfficeCollectFeignApi remoteSysOfficeCollectFeignApi;
    private final RemoteDataPlatformFeignApi remoteDataPlatformFeignApi;
    private final RemoteWmsStorageReceiptFeignApi remoteWmsStorageReceiptFeignApi;
    private final RemoteWmsTransferSlipFeignApi remoteWmsTransferSlipFeignApi;
    private final RemoteDpFeignApi remoteDpFeignApi;

    @Override
    public SubstrateDayReportVO substrateDayReport(PmsDayReportDTO dto) {
        //type data 0  line 1 workshop 2 total 3
        //类型 数据0 产线1 车间2 合计3
        List<SubstrateDataListVO> defaultSubstrateDataListVOList = Lists.newArrayList();
        SubstrateDataListVO defaultSubstrateDataListVO = new SubstrateDataListVO();

        SubstrateDayReportVO vo = new SubstrateDayReportVO();
        //查询所有数据

        LocalDateTime start = LocalDateUtil.getMorning(dto.getDate());
        LocalDateTime end = start.plusDays(1);

        List<SubstrateDataListVO> rowDataList = pmsReportWorkMapper.substrateReport(dto.getProductLineIdList(), dto.getClassGroupIdList(), start, end);
        SteccnTableAssertUtil.notEmpty(rowDataList, "数据为空，暂无基片日报表");
        rowDataList = rowDataList.stream().filter(v -> Objects.nonNull(v.getMaterialId()) && Objects.nonNull(v.getClassesId())).collect(Collectors.toList());
        SteccnTableAssertUtil.notEmpty(rowDataList, "数据为空，暂无基片日报表");

        Set<Long> materialSpecSet = rowDataList.stream().map(SubstrateDataListVO::getMaterialId).collect(Collectors.toSet());
        Map<Long, EqpSpareVO> specMap = FeignOptional.of(() -> remoteSpareFeignApi.listById(Lists.newArrayList(materialSpecSet)), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        EqpSpareVO defaultEqpSpareVO = new EqpSpareVO();

        //获取平方米和吨的id
        List<Long> unitList = allUnit(Lists.newArrayList(PmManagerConstant.MEASURE_UNIT_PIECE, PmManagerConstant.MEASURE_UNIT_TON, PmManagerConstant.MEASURE_UNIT_SM), PmManagerConstant.MEASURE_UNIT);
        Map<Long, List<SysSpareUnitConversionVO>> unitConvertMap = FeignOptional.of(() -> remoteSpareUnitConvertFeignApi.convert(materialSpecSet), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();

        //获取班次数据
        List<SysDictVO> classList = getClassList();
        //构建表头
        List<SysDictVO> filterClassList = Lists.newArrayList();
        SubstrateReportHeaderVO header = getSubstrateHeader(dto.getClassGroupIdList(), classList, filterClassList);

        //将有数据的产线拉取出来
        Set<Integer> productLineSet = rowDataList.stream().map(SubstrateDataListVO::getProductLineId).collect(Collectors.toSet());
        Map<Integer, SysOfficeVO> productLineMap = FeignOptional.of(() -> remoteOfficeFeign.listById(productLineSet), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        //根据车间去划分
        Map<Integer, List<SysOfficeVO>> workShopMap = productLineMap.values().stream().collect(Collectors.groupingBy(SysOfficeVO::getParentId));
        List<SubstrateDayReportListVO> productLineVOList = Lists.newArrayList();
        //全局统计
        Map<Integer, BigDecimal> allCountMap = Maps.newHashMap();

        List<SubstrateDataListVO> finalRowDataList = rowDataList;

        workShopMap.forEach((k, v) -> {
            //车间统计
            Map<Integer, BigDecimal> workshopCountMap = Maps.newHashMap();
            //产线排序
            v.sort(Comparator.comparing(SysOfficeVO::getTreeSort));
            //根据产线遍历
            for (SysOfficeVO productLine : v) {
                //产线数量合计-早班，晚班，合计
                Map<Integer, BigDecimal> countMap1 = Maps.newHashMap();
                Map<Integer, BigDecimal> countMap2 = Maps.newHashMap();
                Map<Integer, BigDecimal> countMap3 = Maps.newHashMap();
                //获取当前产线下的数据
                List<SubstrateDataListVO> productLineDataList = finalRowDataList.stream().filter(row -> productLine.getId().equals(row.getProductLineId())).collect(Collectors.toList());
                //根据规格分组
                Map<String, List<SubstrateDataListVO>> materialMap = productLineDataList.stream().collect(Collectors.groupingBy(SubstrateDataListVO::getMaterialCode));
                //将当前产线下的数据根据物料分组
//                Map<Long, List<SubstrateDataListVO>> materialMap = productLineDataList.stream().collect(Collectors.groupingBy(SubstrateDataListVO::getProduceOrderId));
                //遍历物料分组的数据，来组装数据
                for (Map.Entry<String, List<SubstrateDataListVO>> entry : materialMap.entrySet()) {
                    Long materialId = entry.getValue().get(0).getMaterialId();
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
                        classData.forEach(cld -> {
                            if (typeMap.containsKey(cld.getType())) {
                                BigDecimal tmpQuantity = typeMap.get(cld.getType()).getQuantity();
                                cld.setQuantity(BigDecimalUtil.add(cld.getQuantity(), tmpQuantity));
                            }
                            typeMap.put(cld.getType(), cld);
                        });
//                        classData.forEach(cld -> typeMap.put(cld.getType(), cld));
                        SubstrateDataListVO typeVO0 = typeMap.getOrDefault(PmReportConstant.TYPE_0, defaultSubstrateDataListVO);
                        SubstrateDataListVO typeVO1 = typeMap.getOrDefault(PmReportConstant.TYPE_1, defaultSubstrateDataListVO);
                        inCount = BigDecimalUtil.add(inCount, typeVO0.getQuantity());
                        proCount = BigDecimalUtil.add(proCount, typeVO1.getQuantity());

                        BigDecimal bigDecimal1 = unitConvert(unitList, unitConvertMap, materialId, typeVO0.getQuantity(), 2);
                        BigDecimal bigDecimal2 = unitConvert(unitList, unitConvertMap, materialId, typeVO0.getQuantity(), 1);
                        BigDecimal bigDecimal3 = unitConvert(unitList, unitConvertMap, materialId, typeVO1.getQuantity(), 2);
                        BigDecimal bigDecimal4 = unitConvert(unitList, unitConvertMap, materialId, typeVO1.getQuantity(), 1);

                        if (PmReportConstant.SYS_SHIFT_0.equals(c.getCode())) {
                            //早班
                            row.setMorning(Arrays.asList(BigDecimalUtil.format(typeVO0.getQuantity()), BigDecimalUtil.format(bigDecimal1), BigDecimalUtil.format(bigDecimal2), BigDecimalUtil.format(typeVO1.getQuantity()), BigDecimalUtil.format(bigDecimal3), BigDecimalUtil.format(bigDecimal4)));
                            BigDecimalUtil.accumulate(countMap1, 0, typeVO0.getQuantity());
                            BigDecimalUtil.accumulate(countMap1, 1, bigDecimal1);
                            BigDecimalUtil.accumulate(countMap1, 2, bigDecimal2);
                            BigDecimalUtil.accumulate(countMap1, 3, typeVO1.getQuantity());
                            BigDecimalUtil.accumulate(countMap1, 4, bigDecimal3);
                            BigDecimalUtil.accumulate(countMap1, 5, bigDecimal4);
                        } else {
                            //晚班
                            row.setNight(Arrays.asList(BigDecimalUtil.format(typeVO0.getQuantity()), BigDecimalUtil.format(bigDecimal1), BigDecimalUtil.format(bigDecimal2), BigDecimalUtil.format(typeVO1.getQuantity()), BigDecimalUtil.format(bigDecimal3), BigDecimalUtil.format(bigDecimal4)));
                            BigDecimalUtil.accumulate(countMap2, 0, typeVO0.getQuantity());
                            BigDecimalUtil.accumulate(countMap2, 1, bigDecimal1);
                            BigDecimalUtil.accumulate(countMap2, 2, bigDecimal2);
                            BigDecimalUtil.accumulate(countMap2, 3, typeVO1.getQuantity());
                            BigDecimalUtil.accumulate(countMap2, 4, bigDecimal3);
                            BigDecimalUtil.accumulate(countMap2, 5, bigDecimal4);
                        }
                    }
                    //1，2代表不同的单位转换
                    BigDecimal m1 = unitConvert(unitList, unitConvertMap, materialId, inCount, 2);
                    BigDecimal m2 = unitConvert(unitList, unitConvertMap, materialId, inCount, 1);
                    BigDecimal m3 = unitConvert(unitList, unitConvertMap, materialId, proCount, 2);
                    BigDecimal m4 = unitConvert(unitList, unitConvertMap, materialId, proCount, 1);
                    BigDecimalUtil.accumulate(countMap3, 0, inCount);
                    BigDecimalUtil.accumulate(countMap3, 1, m1);
                    BigDecimalUtil.accumulate(countMap3, 2, m2);
                    BigDecimalUtil.accumulate(countMap3, 3, proCount);
                    BigDecimalUtil.accumulate(countMap3, 4, m3);
                    BigDecimalUtil.accumulate(countMap3, 5, m4);
                    row.setTotal(Arrays.asList(BigDecimalUtil.format(inCount), BigDecimalUtil.format(m1), BigDecimalUtil.format(m2), BigDecimalUtil.format(proCount), BigDecimalUtil.format(m3), BigDecimalUtil.format(m4)));
                    EqpSpareVO eqpSpareVO = specMap.getOrDefault(materialId, defaultEqpSpareVO);
                    row.setSpecList(Arrays.asList(BigDecimalUtil.format(eqpSpareVO.getLengthQuantity()), BigDecimalUtil.format(eqpSpareVO.getWideQuantity()), BigDecimalUtil.format(eqpSpareVO.getHighQuantity())));
                    productLineVOList.add(row);
                }
                SubstrateDayReportListVO rowTotal = new SubstrateDayReportListVO();
                rowTotal.setType(PmReportConstant.TYPE_0);
                rowTotal.setWorkshopName(productLine.getParentName());
                rowTotal.setProductLineName(productLine.getOfficeName());
                if (dayOrNight(dto.getClassGroupIdList(), classList) == PmReportConstant.TYPE_0) {
                    rowTotal.setMorning(valueStr(countMap1));
                    rowTotal.setNight(valueStr(countMap2));
                } else if (dayOrNight(dto.getClassGroupIdList(), classList) == PmReportConstant.TYPE_1) {
                    rowTotal.setMorning(valueStr(countMap1));
                } else if (dayOrNight(dto.getClassGroupIdList(), classList) == PmReportConstant.TYPE_2) {
                    rowTotal.setNight(valueStr(countMap2));
                }
                rowTotal.setTotal(valueStr(countMap3));
                rowTotal.setSpecList(Arrays.asList("合计", "合计", "合计"));
                productLineVOList.add(rowTotal);
                //插入产量总量[-1]，入库总量[-2]，拉引量，合格率，耗水量，耗电量，耗油量，单耗(kcal/Kg玻璃液)
                BigDecimalUtil.accumulate(workshopCountMap, -1, countMap3.get(5));
                BigDecimalUtil.accumulate(workshopCountMap, -2, countMap3.get(2));
                addProductLineOther(countMap1, countMap2, dto, classList, productLine, productLineVOList, workshopCountMap);
            }
            //添加车间库存
            addWorkShopOther(v.get(0), productLineVOList, workshopCountMap);
            //计算总的库存
            for (PmReportConstant.SubstrateReportConsumeEnum sub : PmReportConstant.SubstrateReportConsumeEnum.values()) {
                BigDecimalUtil.accumulate(allCountMap, sub.getCode(), workshopCountMap.get(sub.getCode()));
            }
            //计算合格率
            BigDecimalUtil.accumulate(allCountMap, -1, workshopCountMap.get(-1));
            BigDecimalUtil.accumulate(allCountMap, -2, workshopCountMap.get(-2));
        });
        //最后一个总计
        addAllOther(productLineVOList, allCountMap);
        //如果只选择一个班次，则只展示一个班次
        filterClass(dayOrNight(dto.getClassGroupIdList(), classList), productLineVOList);
        vo.setSpecsNum(3);
        vo.setDataNum(6);
        vo.setHeader(header);
        vo.setDataList(productLineVOList);
        return vo;
    }

    /**
     * 获取map中的数据
     *
     * @param countMap
     * @return
     */
    private List<String> valueStr(Map<Integer, BigDecimal> countMap) {
        List<String> resList = Lists.newArrayList();
        for (int i = 0; i <= 5; i++) {
            resList.add(BigDecimalUtil.format(countMap.get(i)));
        }
        return resList;
    }


    /**
     * 获取所有的单位的id  依次是片，
     *
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
        Map<String, SysDictVO> resMap = FeignOptional.of(() -> remoteDictFeignApi.listByCode(sysDictDTO), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        for (String code : codeSet) {
            SysDictVO sysDictVO = resMap.getOrDefault(code, defaultSysDictVO);
            resList.add(sysDictVO.getId());
        }
        return resList;
    }


    /**
     * 返回0-早班夜班 ， 1-早班 ，2-夜班
     *
     * @param classesIdList
     * @param classList
     * @return
     */
    private int dayOrNight(List<Long> classesIdList, List<SysDictVO> classList) {
        if (CollUtil.isEmpty(classesIdList)) {
            return PmReportConstant.TYPE_0;
        }
        List<Long> collect = classList.stream().map(SysDictVO::getId).collect(Collectors.toList());
        if (classesIdList.containsAll(collect)) {
            return PmReportConstant.TYPE_0;
        }
        for (Long cid : classesIdList) {
            if (collect.indexOf(cid) == PmReportConstant.TYPE_0) {
                return PmReportConstant.TYPE_1;
            } else if (collect.indexOf(cid) == PmReportConstant.TYPE_1) {
                return PmReportConstant.TYPE_2;
            } else {
                throw new IllegalArgumentException("班次产线异常");
            }
        }
        return PmReportConstant.TYPE_0;
    }


    /**
     * 物料单位转换
     *
     * @param unitList       单位list
     * @param unitConvertMap 单位转换map
     * @param materialId     物料id
     * @param num            数量
     * @param idx            单位id
     * @return 转换后的数值
     */
    private BigDecimal unitConvert(List<Long> unitList, Map<Long, List<SysSpareUnitConversionVO>> unitConvertMap, Long materialId, BigDecimal num, Integer idx) {
        //1.获取物料对应的单位转换
        List<SysSpareUnitConversionVO> orDefault = unitConvertMap.getOrDefault(materialId, Lists.newArrayList());
        //2.遍历此物料所有单位转换集合
        if (CollUtil.isNotEmpty(orDefault)) {
            Map<Long, SysSpareUnitConversionVO> resMap = Maps.newHashMap();
            for (SysSpareUnitConversionVO spare : orDefault) {
                resMap.put(spare.getConversionUnitId(), spare);
            }
            SysSpareUnitConversionVO vo = resMap.getOrDefault(unitList.get(idx), new SysSpareUnitConversionVO());
            if (Objects.nonNull(vo)) {
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
     * @param classList     所有的班次对象集合
     */
    private SubstrateReportHeaderVO getSubstrateHeader(List<Long> classesIdList, List<SysDictVO> classList, List<SysDictVO> filterClassList) {
        SubstrateReportHeaderVO header = new SubstrateReportHeaderVO();
        header.setSpecList(new BaseReportHeaderVO("规格", Arrays.asList("长", "宽", "厚")));

        List<Long> allClassIdList = classList.stream().map(SysDictVO::getId).collect(Collectors.toList());
        if (CollUtil.isEmpty(classesIdList)) {
            classesIdList = allClassIdList;
        }
        //只查询白班，或者夜班
        for (Long cid : classesIdList) {
            int i = allClassIdList.indexOf(cid);
            if (i == 0) {
                header.setMorning(new BaseReportHeaderVO("早班", Arrays.asList("入库(片)", "入库(㎡)", "入库(t)", "产量(片)", "产量(㎡)", "产量(t)")));
            } else if (i == 1) {
                header.setNight(new BaseReportHeaderVO("晚班", Arrays.asList("入库(片)", "入库(㎡)", "入库(t)", "产量(片)", "产量(㎡)", "产量(t)")));
            } else {
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
        Map<String, SysDictVO> classMap = FeignOptional.of(() -> remoteDictFeignApi.listByCode(sysDictDTO), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
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
            if (PmReportConstant.SubstrateReportConsumeEnum.PASS_RATE.getCode().equals(consumeEnum.getCode())) {
                //产量合格率需要重新算
                BigDecimal divide = BigDecimalUtil.divide(BigDecimalUtil.multiply(allCountMap.get(-1), BigDecimalUtil.HUNDRED), allCountMap.get(0));
                subVo = rowData(new SysOfficeVO(), Arrays.asList(divide, divide, divide), consumeEnum.getName());
            } else if (PmReportConstant.SubstrateReportConsumeEnum.IN_PASS_RATE.getCode().equals(consumeEnum.getCode())) {
                //入库合格率需要重新算
                BigDecimal divide = BigDecimalUtil.divide(BigDecimalUtil.multiply(allCountMap.get(-2), BigDecimalUtil.HUNDRED), allCountMap.get(0));
                subVo = rowData(new SysOfficeVO(), Arrays.asList(divide, divide, divide), consumeEnum.getName());
            } else if (PmReportConstant.SubstrateReportConsumeEnum.Kcal.getCode().equals(consumeEnum.getCode())) {
                //单耗(kcal/Kg玻璃液) 需要重新计算
                BigDecimal oil = allCountMap.get(PmReportConstant.SubstrateReportConsumeEnum.OIL.getCode());
                BigDecimal gas = allCountMap.get(PmReportConstant.SubstrateReportConsumeEnum.GAS.getCode());
                BigDecimal lyl = allCountMap.get(PmReportConstant.SubstrateReportConsumeEnum.PULL_FORCE.getCode());
                BigDecimal kcal = kcal(oil, gas, lyl);
                subVo = rowData(new SysOfficeVO(), Arrays.asList(kcal, kcal, kcal), consumeEnum.getName());
            }
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
            if (PmReportConstant.SubstrateReportConsumeEnum.PASS_RATE.getCode().equals(consumeEnum.getCode())) {
                //合格率需要重新计算
                BigDecimal rate = BigDecimalUtil.divide(BigDecimalUtil.multiply(workshopCountMap.get(-1), BigDecimalUtil.HUNDRED), workshopCountMap.get(0));
                subVo = rowData(productLine, Arrays.asList(rate, rate, rate), consumeEnum.getName());
            } else if (PmReportConstant.SubstrateReportConsumeEnum.IN_PASS_RATE.getCode().equals(consumeEnum.getCode())) {
                //合格率需要重新计算
                BigDecimal rate = BigDecimalUtil.divide(BigDecimalUtil.multiply(workshopCountMap.get(-2), BigDecimalUtil.HUNDRED), workshopCountMap.get(0));
                subVo = rowData(productLine, Arrays.asList(rate, rate, rate), consumeEnum.getName());
            } else if (PmReportConstant.SubstrateReportConsumeEnum.Kcal.getCode().equals(consumeEnum.getCode())) {
                //单耗(kcal/Kg玻璃液)需要重新计算
                BigDecimal oil = workshopCountMap.get(PmReportConstant.SubstrateReportConsumeEnum.OIL.getCode());
                BigDecimal gas = workshopCountMap.get(PmReportConstant.SubstrateReportConsumeEnum.GAS.getCode());
                BigDecimal lyl = workshopCountMap.get(PmReportConstant.SubstrateReportConsumeEnum.PULL_FORCE.getCode());
                BigDecimal kcal = kcal(oil, gas, lyl);
                subVo = rowData(productLine, Arrays.asList(kcal, kcal, kcal), consumeEnum.getName());
            }
            subVo.setWorkshopName(productLine.getParentName());
            subVo.setProductLineName("总计");
            subVo.setType(PmReportConstant.TYPE_2);
            subVo.setSpecList(Arrays.asList(consumeEnum.getName(), consumeEnum.getName(), consumeEnum.getName()));
            productLineVOList.add(subVo);
        }
    }

    /**
     * 单耗(kcal/Kg玻璃液) （耗气量 * 7800 + 耗油量 * 9300  ） / （拉引量 * 1000)
     *
     * @param oil 油
     * @param gas 气
     * @param lyl 拉引量
     * @return
     */
    private BigDecimal kcal(BigDecimal oil, BigDecimal gas, BigDecimal lyl) {
        BigDecimal gasMultiply = BigDecimalUtil.multiply(gas, BigDecimal.valueOf(7800));
        BigDecimal oilMultiply = BigDecimalUtil.multiply(oil, BigDecimal.valueOf(9300));
        BigDecimal lylMultiply = BigDecimalUtil.multiply(lyl, BigDecimal.valueOf(1000));
        BigDecimal divide = BigDecimalUtil.divide(BigDecimalUtil.add(gasMultiply, oilMultiply), lylMultiply);
        return divide;
    }

    /**
     * 插入拉引量，合格率，耗水量，耗电量，耗油量，单耗(kcal/Kg玻璃液)
     *
     * @param countMap1         早班数据集合
     * @param countMap2         夜班数据集合
     * @param dto               查询条件
     * @param classList         班次数据
     * @param productLine       产线信息
     * @param productLineVOList 数据存放集合
     */
    private void addProductLineOther(Map<Integer, BigDecimal> countMap1, Map<Integer, BigDecimal> countMap2, PmsDayReportDTO dto, List<SysDictVO> classList, SysOfficeVO productLine, List<SubstrateDayReportListVO> productLineVOList, Map<Integer, BigDecimal> workshopCountMap) {
        //拉引量(T)
        Map<Long, PmsPullForce> pmsPullForcesMap = pmsPullForceService.pullForceDay(productLine.getId(), dto.getDate(), dto.getClassGroupIdList());
        PmsPullForce morning = pmsPullForcesMap.get(classList.get(0).getId());
        PmsPullForce night = pmsPullForcesMap.get(classList.get(1).getId());
        BigDecimal morningBigDecimal = Objects.nonNull(morning) ? morning.getQuantity() : BigDecimal.ZERO;
        BigDecimal nightBigDecimal = Objects.nonNull(night) ? night.getQuantity() : BigDecimal.ZERO;
        BigDecimal sum = BigDecimalUtil.add(morningBigDecimal, nightBigDecimal);
        SubstrateDayReportListVO lyl = rowData(productLine, Lists.newArrayList(morningBigDecimal, nightBigDecimal, sum), "拉引量(T)");

        productLineVOList.add(lyl);
        BigDecimalUtil.accumulate(workshopCountMap, 0, BigDecimalUtil.add(morningBigDecimal,nightBigDecimal));

        BigDecimal dayPq = BigDecimalUtil.multiply(BigDecimalUtil.HUNDRED, countMap1.get(5));
        BigDecimal nightPq = BigDecimalUtil.multiply(BigDecimalUtil.HUNDRED, countMap2.get(5));
        BigDecimal morningRate = BigDecimalUtil.divide(dayPq, morningBigDecimal);
        BigDecimal nightRate = BigDecimalUtil.divide(nightPq, nightBigDecimal);
        //产量的合格率
        BigDecimal sumNum = BigDecimalUtil.add(countMap1.get(5), countMap2.get(5));
        BigDecimal rateSum = BigDecimalUtil.divide(BigDecimalUtil.multiply(sumNum, BigDecimalUtil.HUNDRED), sum);
        SubstrateDayReportListVO rateVo = rowData(productLine, Arrays.asList(morningRate, nightRate, rateSum), "产量合格率(%)");
        BigDecimalUtil.accumulate(workshopCountMap, 1, BigDecimalUtil.add(morningRate,nightRate));

        BigDecimal inDayPq = BigDecimalUtil.multiply(BigDecimalUtil.HUNDRED, countMap1.get(2));
        BigDecimal inNightPq = BigDecimalUtil.multiply(BigDecimalUtil.HUNDRED, countMap2.get(2));
        BigDecimal inMorningRate = BigDecimalUtil.divide(inDayPq, morningBigDecimal);
        BigDecimal inNightRate = BigDecimalUtil.divide(inNightPq, nightBigDecimal);
        //产量的合格率
        BigDecimal inSumNum = BigDecimalUtil.add(countMap1.get(2), countMap2.get(2));
        BigDecimal inRateSum = BigDecimalUtil.divide(BigDecimalUtil.multiply(sumNum, BigDecimalUtil.HUNDRED), inSumNum);
        SubstrateDayReportListVO inRateVo = rowData(productLine, Arrays.asList(inMorningRate, inNightRate, inRateSum), "入库合格率(%)");
        BigDecimalUtil.accumulate(workshopCountMap, 7, BigDecimalUtil.add(morningRate,nightRate));
        productLineVOList.add(rateVo);
        productLineVOList.add(inRateVo);
        //0耗电量 1耗水量 2耗气量 3耗油量
        List<SubstrateDayReportListVO> energyList = pmsReportWorkConsumeService.substrateEnergy(dto, classList, productLine, workshopCountMap);

        //单耗(kcal/Kg玻璃液) （耗气量 * 7800 + 耗油量 * 9300  ） / （拉引量 * 1000)
        SubstrateDayReportListVO bly = getSubstrateDayReportListVO(productLine, morningBigDecimal, nightBigDecimal, energyList, workshopCountMap);
        productLineVOList.addAll(energyList);
        productLineVOList.add(bly);
    }

    /**
     * 过滤班级
     *
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
     * 0耗电量 1耗水量 2耗气量 3耗油量
     *
     * @param productLine       产线id
     * @param morningBigDecimal 早上数量
     * @param nightBigDecimal   晚上数量
     * @param energyList        能耗列表
     * @return 统计数据
     */
    private SubstrateDayReportListVO getSubstrateDayReportListVO(SysOfficeVO productLine, BigDecimal morningBigDecimal, BigDecimal nightBigDecimal, List<SubstrateDayReportListVO> energyList, Map<Integer, BigDecimal> workshopCountMap) {
        BigDecimal morningGas = new BigDecimal(energyList.get(2).getMorning().get(0));
        BigDecimal morningOil = new BigDecimal(energyList.get(3).getMorning().get(0));
        BigDecimal morningKcal = BigDecimal.ZERO;
        BigDecimal nightGas = new BigDecimal(energyList.get(2).getNight().get(0));
        BigDecimal nightOil = new BigDecimal(energyList.get(3).getNight().get(0));
        BigDecimal nightKcal = BigDecimal.ZERO;
        if (BigDecimal.ZERO.compareTo(morningBigDecimal.multiply(BigDecimal.valueOf(1000))) != 0) {
            BigDecimal morningMultiply = morningGas.multiply(BigDecimal.valueOf(7800)).add(morningOil.multiply(BigDecimal.valueOf(9300)));
            morningKcal = morningMultiply.divide(morningBigDecimal.multiply(BigDecimal.valueOf(1000)), BigDecimalUtil.DEFAULT_SCALE, RoundingMode.HALF_UP);
        }
        if (BigDecimal.ZERO.compareTo(nightBigDecimal.multiply(BigDecimal.valueOf(1000))) != 0) {
            BigDecimal nightMultiply = nightGas.multiply(BigDecimal.valueOf(7800)).add(nightOil.multiply(BigDecimal.valueOf(9300)));
            nightKcal = nightMultiply.divide(nightBigDecimal.multiply(BigDecimal.valueOf(1000)), BigDecimalUtil.DEFAULT_SCALE, RoundingMode.HALF_UP);
        }
        //单耗的合计得重新计算
        BigDecimal gas = BigDecimalUtil.add(morningGas, nightGas);
        BigDecimal oil = BigDecimalUtil.add(morningOil, nightOil);
        BigDecimal lyl = BigDecimalUtil.add(morningBigDecimal, nightBigDecimal);
        BigDecimal divide = kcal(oil, gas, lyl);

        SubstrateDayReportListVO bly = rowData(productLine, Lists.newArrayList(morningKcal, nightKcal, divide), "单耗(kcal/Kg玻璃液)");
        BigDecimalUtil.accumulate(workshopCountMap, 6, divide);
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
        row.setMorning(Arrays.asList(dataStr1, dataStr1, dataStr1, dataStr1, dataStr1, dataStr1));
        row.setNight(Arrays.asList(dataStr2, dataStr2, dataStr2, dataStr2, dataStr2, dataStr2));
        row.setTotal(Arrays.asList(dataStr3, dataStr3, dataStr3, dataStr3, dataStr3, dataStr3));
        return row;
    }

    @Override
    public FurtherProduceReportVO furtherProduceDayReport(PmsDayReportDTO dto) {

        FurtherProduceReportVO vo = new FurtherProduceReportVO();
        vo.setSpecsNum(PmReportConstant.TYPE_3);

        LocalDateTime start = LocalDateUtil.getMorning(dto.getDate());
        LocalDateTime end = start.plusDays(1);

        //根据产线去查询所有订单的工序分组，用于表头
        List<FurtherProduceReportDataVO> dataList = pmsReportWorkMapper.furtherProduceReport(dto.getProductLineIdList(), dto.getClassGroupIdList(), start, end);
        SteccnTableAssertUtil.notEmpty(dataList, "数据为空，暂无报表信息");

        //根据产线去查询所有订单的主物料的不良数
        List<SysOfficeVO> sysOfficeVOList = FeignOptional.of(() -> remoteOfficeFeign.listWorkshop(1), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        //磨边工位
        Set<Integer> edgingStationIds = sysOfficeVOList.stream().filter(v -> v.getOfficeType().equals("13") && v.getOfficeName().contains("磨边工位")).map(SysOfficeVO::getId).collect(Collectors.toSet());
        List<FurtherProduceReportBadnessVO> badnessList = pmsReportWorkMapper.badness(dto.getProductLineIdList(),dto.getClassGroupIdList(),edgingStationIds,start,end);
        Map<Integer, List<FurtherProduceReportBadnessVO>> badnessLineMap = badnessList.stream().collect(Collectors.groupingBy(FurtherProduceReportBadnessVO::getProductLineId));

        Set<Long> materialSpecSet = dataList.stream().map(FurtherProduceReportDataVO::getMaterialId).collect(Collectors.toSet());
        Map<Long, EqpSpareVO> specMap = FeignOptional.of(() -> remoteSpareFeignApi.listById(Lists.newArrayList(materialSpecSet)), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        EqpSpareVO defaultEqpSpareVO = new EqpSpareVO();

        //遍历所有的物料查询完工数量
        Set<Long> materialSet = dataList.stream().map(FurtherProduceReportDataVO::getMaterialId).collect(Collectors.toSet());
        Map<String, BigDecimal> materialCompleteMap = pmsCompleteStorageInfoService.materialMap(dto.getProductLineIdList(), materialSet, dto.getDate());

        //构建表头
        FurtherReportHeaderVO header = new FurtherReportHeaderVO();
        header.setSpecList(new BaseReportHeaderVO("规格", Arrays.asList("长", "宽", "厚")));
        //工序id集合
        Set<Long> processIdSet = dataList.stream().map(FurtherProduceReportDataVO::getProcessId).collect(Collectors.toSet());
        //查询工序的信息
        List<PmsProduceOrderCraft> pmsProduceOrderCraftList = pmsProduceOrderCraftService.listByIds(processIdSet);
        SteccnTableAssertUtil.notEmpty(pmsProduceOrderCraftList, "工艺信息不可为空");
        Set<Long> pidSet = pmsProduceOrderCraftList.stream().map(PmsProduceOrderCraft::getProcessId).collect(Collectors.toSet());
        List<PmProcess> pmProcessList = pmProcessService.listByIds(pidSet);
        SteccnTableAssertUtil.notEmpty(pmsProduceOrderCraftList, "工序信息不可为空");
        SteccnTableAssertUtil.notEmpty(dataList, "工序信息为空，暂无报表信息");
        //工序信息转换
        Map<Long, Long> craftProcessIdMap = Maps.newHashMap();
        pmsProduceOrderCraftList.forEach(ele -> craftProcessIdMap.put(ele.getId(), ele.getProcessId()));
        dataList.forEach(ele -> ele.setProcessId(craftProcessIdMap.get(ele.getProcessId())));

        List<BaseReportHeaderVO> module = Lists.newArrayList();
        module.add(new BaseReportHeaderVO("不良数",null));
        module.add(new BaseReportHeaderVO("耗用数",null));
        pmProcessList.forEach(ele -> {
            module.add(new BaseReportHeaderVO(ele.getName(), Arrays.asList("加工(片)", "加工(㎡)", "合格(片)", "合格(㎡)", "不良(片)", "不良(㎡)", "合格率")));
        });
        //合格率合计得单独算【实际为加权平均】
        module.add(new BaseReportHeaderVO("合计", Arrays.asList("加工(片)", "加工(㎡)", "合格(片)", "合格(㎡)", "合格率")));
        module.add(new BaseReportHeaderVO("入库", Arrays.asList("数量(片)", "数量(㎡)", "成品率")));
        header.setModule(module);
        vo.setHeader(header);

        //获取平方米和吨的id
        List<Long> unitList = allUnit(Lists.newArrayList(PmManagerConstant.MEASURE_UNIT_PIECE, PmManagerConstant.MEASURE_UNIT_TON, PmManagerConstant.MEASURE_UNIT_SM), PmManagerConstant.MEASURE_UNIT);
        Map<Long, List<SysSpareUnitConversionVO>> unitConvertMap = FeignOptional.of(() -> remoteSpareUnitConvertFeignApi.convert(materialSpecSet), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();

        //获取班次数据
        List<SysDictVO> classList = getClassList();
        Map<Long, SysDictVO> classMap = Maps.newHashMap();
        classList.forEach(ele -> classMap.put(ele.getId(), ele));

        List<FurtherProduceReportBadnessVO> furtherProduceReportBadnessVOList = Lists.newArrayList();
        //汇总数据
        FurtherProduceReportDataVO defaultFurtherProduceReportDataVO = new FurtherProduceReportDataVO();
        List<FurtherProduceReportListVO> tableData = Lists.newArrayList();
        //查询产线车间信息
        Set<Integer> productLineSet = dataList.stream().map(FurtherProduceReportDataVO::getProductLineId).collect(Collectors.toSet());
        Map<Integer, SysOfficeVO> productLineMap = FeignOptional.of(() -> remoteOfficeFeign.listById(productLineSet), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        //根据车间去划分
        Map<Integer, List<SysOfficeVO>> workShopMap = productLineMap.values().stream().collect(Collectors.groupingBy(SysOfficeVO::getParentId));
        workShopMap.forEach((workshop, productLineList) -> {
            //车间统计
            Map<Long, Map<Integer, BigDecimal>> workshopCountMap = Maps.newHashMap();
            //车间不良数统计
            BigDecimal workshopBadnessQuantity = null;
            //耗用数
            BigDecimal workshopConsumptionQuantity = null;
            //根据产线遍历
            for (SysOfficeVO productLine : productLineList) {
                //产线的合计 key-产线 value【key为数量类别，value为数量】
                Map<Long, Map<Integer, BigDecimal>> lineCountMap = Maps.newHashMap();
                //不良数
                List<FurtherProduceReportBadnessVO> badnessLineList = badnessLineMap.getOrDefault(productLine.getId(),furtherProduceReportBadnessVOList);
                Map<Long, List<FurtherProduceReportBadnessVO>> badnessClassMap = badnessLineList.stream().collect(Collectors.groupingBy(FurtherProduceReportBadnessVO::getClassesId));
                //过滤出产线数据并将产线数据分成不同班次
                Map<Long, List<FurtherProduceReportDataVO>> lineDataMap = dataList.stream().filter(row -> productLine.getId().equals(row.getProductLineId())).collect(Collectors.groupingBy(FurtherProduceReportDataVO::getClassesId));
                //遍历不同的班次
                for (Map.Entry<Long, List<FurtherProduceReportDataVO>> classEntry : lineDataMap.entrySet()) {
                    //班次的合计 key-工序 value【key为数量类别，value为数量】
                    Map<Long, Map<Integer, BigDecimal>> classCountMap = Maps.newHashMap();
                    //获取班次的数据
                    SysDictVO sysDictVO = classMap.get(classEntry.getKey());
                    //不良数
                    List<FurtherProduceReportBadnessVO> badnessClassList = badnessClassMap.getOrDefault(sysDictVO.getId(),furtherProduceReportBadnessVOList);
                    Map<Long, List<FurtherProduceReportBadnessVO>> badnessMaterialMap = badnessClassList.stream().collect(Collectors.groupingBy(FurtherProduceReportBadnessVO::getProductId));
                    Map<Long, List<FurtherProduceReportDataVO>> materialMap = classEntry.getValue().stream().collect(Collectors.groupingBy(FurtherProduceReportDataVO::getProduceOrderId));
                    //遍历物料分组
                    for (Map.Entry<Long, List<FurtherProduceReportDataVO>> materialEntry : materialMap.entrySet()) {
                        Long materialId = materialEntry.getValue().get(0).getMaterialId();
                        //不良数
                        List<FurtherProduceReportBadnessVO> badnessMaterialList = badnessMaterialMap.getOrDefault(materialId,furtherProduceReportBadnessVOList);
                        BigDecimal badnessQuantity = badnessMaterialList.stream()
                                .filter(Objects::nonNull)
                                .map(FurtherProduceReportBadnessVO::getBadnessQuantity)
                                .filter(Objects::nonNull)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                        BigDecimal consumptionQuantity = badnessMaterialList.stream()
                                .filter(Objects::nonNull)
                                .map(FurtherProduceReportBadnessVO::getConsumptionQuantity)
                                .filter(Objects::nonNull)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);
                        //记录行合计
                        Map<Integer, BigDecimal> rowCountMap = Maps.newHashMap();
                        //将同一条物料数据，根据工序分组
                        Map<Long, FurtherProduceReportDataVO> processMap = Maps.newHashMap();
                        materialEntry.getValue().forEach(ele -> processMap.put(ele.getProcessId(), ele));
                        List<FurtherValueStrVO> moduleList = Lists.newArrayList();
                        BigDecimal firstProcessQuantity = null;
                        BigDecimal lastPassQuantity = null;
                        //遍历所有的工序
                        for (int i = 0; i < pmProcessList.size(); i++) {
                            PmProcess pmProcess = pmProcessList.get(i);
                            //获取工序的数据
                            FurtherProduceReportDataVO orDefault = processMap.getOrDefault(pmProcess.getId(), defaultFurtherProduceReportDataVO);
                            List<String> valueStrList = Lists.newArrayList();
                            valueStrList.add(BigDecimalUtil.format(orDefault.getProcessQuantity()));
                            BigDecimal value2 = unitConvert(unitList, unitConvertMap, materialId, orDefault.getProcessQuantity(), 2);
                            valueStrList.add(BigDecimalUtil.format(value2));
                            BigDecimal subtract = BigDecimalUtil.subtract(orDefault.getProcessQuantity(), orDefault.getBadnessQuantity());
                            valueStrList.add(BigDecimalUtil.format(subtract));
                            BigDecimal value4 = unitConvert(unitList, unitConvertMap, materialId, subtract, 2);
                            valueStrList.add(BigDecimalUtil.format(value4));
                            valueStrList.add(BigDecimalUtil.format(orDefault.getBadnessQuantity()));
                            BigDecimal value6 = unitConvert(unitList, unitConvertMap, materialId, orDefault.getBadnessQuantity(), 2);
                            valueStrList.add(BigDecimalUtil.format(value6));
                            BigDecimal value7 = BigDecimalUtil.divide(BigDecimalUtil.multiply(subtract, BigDecimalUtil.HUNDRED), orDefault.getProcessQuantity());
                            valueStrList.add(BigDecimalUtil.format(value7));
                            moduleList.add(new FurtherValueStrVO(valueStrList));
                            //每列的合格率算法 - 最后一道工序的合格数 / 第一道工序的加工数
                            if (i == 0) {
                                firstProcessQuantity = orDefault.getProcessQuantity();
                            }
                            if (i == pmProcessList.size() - 1) {
                                lastPassQuantity = subtract;
                            }
                            //列统计数据  一行数据汇总
                            pmColumnCount(rowCountMap, Lists.newArrayList(orDefault.getProcessQuantity(), value2, subtract, value4, orDefault.getBadnessQuantity(), value6, value7));
                            //班组求和
                            pmRowCount(classCountMap, pmProcess.getId(), Lists.newArrayList(orDefault.getProcessQuantity(), value2, subtract, value4, orDefault.getBadnessQuantity(), value6, value7));
                            //产线求和
                            pmRowCount(lineCountMap, pmProcess.getId(), Lists.newArrayList(orDefault.getProcessQuantity(), value2, subtract, value4, orDefault.getBadnessQuantity(), value6, value7));
                            //车间求和
                            pmRowCount(workshopCountMap, pmProcess.getId(), Lists.newArrayList(orDefault.getProcessQuantity(), value2, subtract, value4, orDefault.getBadnessQuantity(), value6, value7));
                        }
                        //合计
                        List<String> rowCountList = Lists.newArrayList();
                        rowCountList.add(BigDecimalUtil.format(firstProcessQuantity));
                        BigDecimal f1Convert = unitConvert(unitList, unitConvertMap, materialId, firstProcessQuantity, 2);
                        rowCountList.add(BigDecimalUtil.format(f1Convert));
                        rowCountList.add(BigDecimalUtil.format(lastPassQuantity));
                        BigDecimal f2Convert = unitConvert(unitList, unitConvertMap, materialId, lastPassQuantity, 2);
                        rowCountList.add(BigDecimalUtil.format(f2Convert));
                        BigDecimal passRate = BigDecimalUtil.divide(BigDecimalUtil.multiply(lastPassQuantity, BigDecimalUtil.HUNDRED), firstProcessQuantity);
                        rowCountList.add(BigDecimalUtil.format(passRate));
                        //班次合计
                        pmColumnCount(classCountMap, -2L, Lists.newArrayList(firstProcessQuantity, f1Convert, lastPassQuantity, f2Convert, passRate));
                        //产线合计
                        pmColumnCount(lineCountMap, -2L, Lists.newArrayList(firstProcessQuantity, f1Convert, lastPassQuantity, f2Convert, passRate));
                        //车间合计
                        pmColumnCount(workshopCountMap, -2L, Lists.newArrayList(firstProcessQuantity, f1Convert, lastPassQuantity, f2Convert, passRate));
                        moduleList.add(new FurtherValueStrVO(rowCountList));
                        //入库
                        BigDecimal completeNum = materialCompleteMap.get(JointUtil.joint(JointUtil.SEPARATOR_UNDERLINE, productLine.getId(), materialId, classEntry.getKey()));
                        BigDecimal completeConvert = unitConvert(unitList, unitConvertMap, materialId, completeNum, 2);
                        //成品率 = 入库平方米数 / 上片加工平方米数
                        BigDecimal firstProcessNum = unitConvert(unitList, unitConvertMap, materialId, firstProcessQuantity, 2);
                        BigDecimal productRate = BigDecimalUtil.divide(BigDecimalUtil.multiply(completeConvert, BigDecimalUtil.HUNDRED), firstProcessNum);
                        moduleList.add(new FurtherValueStrVO(Arrays.asList(BigDecimalUtil.format(completeNum), BigDecimalUtil.format(completeConvert), BigDecimalUtil.format(productRate))));
                        //班次，产线，车间
                        countIn(classEntry.getKey(), classCountMap, completeNum, completeConvert);
                        countIn(classEntry.getKey(), lineCountMap, completeNum, completeConvert);
                        countIn(classEntry.getKey(), workshopCountMap, completeNum, completeConvert);
                        EqpSpareVO eqpSpareVO = specMap.getOrDefault(materialId, defaultEqpSpareVO);
                        List<String> specDataList = Arrays.asList(BigDecimalUtil.format(eqpSpareVO.getLengthQuantity()), BigDecimalUtil.format(eqpSpareVO.getWideQuantity()), BigDecimalUtil.format(eqpSpareVO.getHighQuantity()));
                        FurtherProduceReportListVO rowData = createFurtherProduceReportListVO(Arrays.asList(productLine.getParentName(), productLine.getOfficeName(), sysDictVO.getName()), PmReportConstant.TYPE_0, moduleList, specDataList,badnessQuantity,consumptionQuantity);
                        tableData.add(rowData);
                    }
                    //班次的合计
                    BigDecimal reduce = badnessClassList.stream().filter(Objects::nonNull).map(FurtherProduceReportBadnessVO::getBadnessQuantity).filter(Objects::nonNull)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    BigDecimal reduceConsumptionQuantity = badnessClassList.stream().filter(Objects::nonNull).map(FurtherProduceReportBadnessVO::getConsumptionQuantity).filter(Objects::nonNull)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    FurtherProduceReportListVO classCountRow = getFurtherProduceReportListVO(pmProcessList, productLine, classCountMap, sysDictVO, Arrays.asList("合计", "合计", "合计"),reduce,reduceConsumptionQuantity);
                    tableData.add(classCountRow);
                }
                BigDecimal reduce = badnessLineList.stream().filter(Objects::nonNull).map(FurtherProduceReportBadnessVO::getBadnessQuantity)
                        .filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
                workshopBadnessQuantity = BigDecimalUtil.add(workshopBadnessQuantity,reduce);
                BigDecimal reduceConsumptionQuantity = badnessLineList.stream().filter(Objects::nonNull).map(FurtherProduceReportBadnessVO::getConsumptionQuantity)
                        .filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
                workshopConsumptionQuantity = BigDecimalUtil.add(reduceConsumptionQuantity,reduce);
                //产线的合计
                FurtherProduceReportListVO lineCountRow = getFurtherProduceReportListVO(pmProcessList, productLine, lineCountMap, new SysDictVO(), Arrays.asList("合计", "合计", "合计"),reduce,reduceConsumptionQuantity);
                lineCountRow.setClassName("合计");
                lineCountRow.setType(PmReportConstant.TYPE_2);
                tableData.add(lineCountRow);
            }
            //车间的合计
            FurtherProduceReportListVO workshopCountRow = getFurtherProduceReportListVO(pmProcessList, productLineList.get(0), workshopCountMap, new SysDictVO(), Arrays.asList("合计", "合计", "合计"),workshopBadnessQuantity,workshopConsumptionQuantity);
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
        SteccnTableAssertUtil.notEmpty(dataList, "数据为空，暂无深加工日报表");
        Set<Long> materialSpecSet = dataList.stream().map(FurtherIngredientMapperVO::getMaterialId).collect(Collectors.toSet());
        Map<Long, EqpSpareVO> specMap = FeignOptional.of(() -> remoteSpareFeignApi.listById(Lists.newArrayList(materialSpecSet)), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
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
        SteccnTableAssertUtil.notEmpty(pmsProduceOrderCraftList, "工艺信息不可为空");
        Set<Long> pidSet = pmsProduceOrderCraftList.stream().map(PmsProduceOrderCraft::getProcessId).collect(Collectors.toSet());
        List<PmProcess> pmProcessList = pmProcessService.listByIds(pidSet);
        SteccnTableAssertUtil.notEmpty(dataList, "工序信息为空，暂无报表信息");
        Map<Long, PmProcess> pmProcessMap = Maps.newHashMap();
        pmProcessList.forEach(ele -> pmProcessMap.put(ele.getId(), ele));
        PmProcess defaultPmProcess = new PmProcess();

        Map<Long, Long> craftProcessIdMap = Maps.newHashMap();
        Map<Long, Long> processIdCraftMap = Maps.newHashMap();
        pmsProduceOrderCraftList.forEach(ele -> {
            craftProcessIdMap.put(ele.getId(), ele.getProcessId());
            processIdCraftMap.put(ele.getProcessId(), ele.getId());
        });
        dataList.forEach(ele -> ele.setProcessId(craftProcessIdMap.get(ele.getProcessId())));

        //获取生产订单id集合
        Set<Long> produceOrderIdSet = dataList.stream().map(FurtherIngredientMapperVO::getProduceOrderId).collect(Collectors.toSet());

        //查询生产订单的耗料 key-订单id value【key-工序id value【key-物料id，value-数据对象】】
        Map<Long, Map<Long, Map<Long, ProduceOrderProcessNumVO>>> numConsumeMap = completeDeliveryService.processNumConsumeMap(produceOrderIdSet);
        Map<Long, Map<Long, ProduceOrderProcessNumVO>> defaultProcessNumMap = Maps.newHashMap();
        Map<Long, ProduceOrderProcessNumVO> defaultMaterialNumMap = Maps.newHashMap();
        ProduceOrderProcessNumVO defaultProduceOrderProcessNumVO = new ProduceOrderProcessNumVO();

        //查询工序需要的物料,兼容所有的工序物料
        Map<Long, List<BaseSelectVO<Long>>> materialProcessMapCopy = pmsWorkOrderBomService.getProcessMaterial(produceOrderIdSet, pidSet);
        Map<Long, List<BaseSelectVO<Long>>> materialProcessMap = Maps.newHashMap();
        materialProcessMapCopy.forEach((k, v) -> {
            materialProcessMap.put(processIdCraftMap.get(k), v);
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
        Map<Integer, SysOfficeVO> productLineMap = FeignOptional.of(() -> remoteOfficeFeign.listById(productLineSet), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
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
                        //遍历工序物料集合,key为工序id
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
        if (CollUtil.isNotEmpty(dataList)) {
            List<TupleDTO<Long, Long>> conditionList = Lists.newArrayList();
            dataList.forEach(ele -> {
                //根据订单和子物料查询工位信息【订单的子物料关联工序，工序关联可执行工位】
                if (Objects.nonNull(ele.getMaterialId()) && CollUtil.isNotEmpty(ele.getProduceOrderIdSet())) {
                    for (Long poId : ele.getProduceOrderIdSet()) {
                        conditionList.add(new TupleDTO<>(poId, ele.getMaterialId()));
                    }
                }
            });
            //构建map，查询领料数据
            Map<Long, Set<Integer>> materialStationMap = pmsWorkOrderBomService.materialOrderStation(conditionList);
            TupleDTO<LocalDate, Map<Long, Set<Integer>>> tupleDTO = new TupleDTO<>(dto.getDate(), materialStationMap);
            Map<Long, BigDecimal> map = Maps.newHashMap();
            if (CollUtil.isNotEmpty(materialStationMap)) {
                map = pmsMaterialRequisitionInfoService.quantityMap(tupleDTO);
            }
            for (SubstrateIngredientVO data : dataList) {
                data.setInKg(map.getOrDefault(data.getMaterialId(), BigDecimal.ZERO));
                data.setInTon(BigDecimalUtil.divide(data.getInKg(), BigDecimalUtil.THOUSAND));
            }
        }
        return dataList;
    }

    /**
     * 生产订单进度跟踪
     *
     * @param workshopId
     * @return
     */
    @Override
    public QtLineChartBarDTO orderProgress(Integer workshopId) {
        //根据车间id查询产线
        SysOfficeParentDTO sysOfficeParentDTO = new SysOfficeParentDTO();
        sysOfficeParentDTO.setId(workshopId);
        sysOfficeParentDTO.setOfficeType("9");
        List<SysOfficeVO> officeData = FeignOptional.of(() -> remoteOfficeFeign.listByParentId(sysOfficeParentDTO), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        SteccnTableAssertUtil.notEmpty(officeData, "车间下的产线为空");
        //排一下序
        QtLineChartBarDTO<String, Long> result = new QtLineChartBarDTO();
        //放入x轴数据
        List<String> xData = Lists.newArrayList();
        //产线idList
        List<Integer> productLineIds = Lists.newArrayList();
        officeData.forEach(tmp -> {
            xData.add(tmp.getOfficeName());
            productLineIds.add(tmp.getId());
        });

        result.setXData(xData);
        //放入y轴数据
        List<QtSeriesDataDTO> seriesDataVO = Lists.newArrayList();
        //计划数量
        QtSeriesDataDTO<String, BigDecimal> planNum = new QtSeriesDataDTO<>();
        planNum.setName(PmManagerConstant.PLAN_NUM);
        //完工数量
        QtSeriesDataDTO<String, BigDecimal> finishNum = new QtSeriesDataDTO<>();
        finishNum.setName(PmManagerConstant.FINISH_NUM);
        //入库数量
        QtSeriesDataDTO<String, BigDecimal> receiptNum = new QtSeriesDataDTO<>();
        receiptNum.setName(PmManagerConstant.RECEIPT_NUM);
        //获得执行中的生产订单数据
        List<PmsProduceOrder> pmsProduceOrderList = pmsProduceOrderService.list(new LambdaQueryWrapper<PmsProduceOrder>().eq(PmsProduceOrder::getState, PmManagerConstant.OrderStateEnum.TWO.getCode()).in(PmsProduceOrder::getProductionLineId, productLineIds));
        SteccnTableAssertUtil.notEmpty(pmsProduceOrderList, "产线下执行中的生产订单为空");
        List<Long> pmsProduceOrderIdList = Lists.newArrayList();
        pmsProduceOrderList.forEach(tmp -> {
            pmsProduceOrderIdList.add(tmp.getId());
        });
        //获得报工数据
        List<PmsReportWork> pmsReportWorkList = pmsReportWorkService.list(new LambdaQueryWrapper<PmsReportWork>().in(PmsReportWork::getProduceOrderId, pmsProduceOrderIdList));
        //完工信息
        List<PmsCompleteStorageInfo> pmsCompleteStorageInfoList = pmsCompleteStorageInfoService.list(new LambdaQueryWrapper<PmsCompleteStorageInfo>().in(PmsCompleteStorageInfo::getProduceOrderId, pmsProduceOrderIdList, pmsProduceOrderIdList));
        //计划数量data
        List<BigDecimal> planNumData = Lists.newArrayList();
        //完工数量data
        List<BigDecimal> finishNumData = Lists.newArrayList();
        //入库数量data
        List<BigDecimal> receiptNumData = Lists.newArrayList();
        productLineIds.forEach(tmp -> {
            PmsProduceOrder pmsProduceOrder = pmsProduceOrderList.stream().filter(v -> v.getProductionLineId().equals(tmp)).findFirst().orElse(null);
            if (Objects.isNull(pmsProduceOrder)) {
                planNumData.add(BigDecimal.ZERO);
                finishNumData.add(BigDecimal.ZERO);
                receiptNumData.add(BigDecimal.ZERO);
            } else {
                if (pmsProduceOrder.getQuantity() == null) {
                    planNumData.add(BigDecimal.ZERO);
                } else {
                    planNumData.add(pmsProduceOrder.getQuantity());
                }
                //完工数量
                if (CollUtil.isNotEmpty(pmsReportWorkList)) {
                    List<PmsReportWork> pmsReportWorkListTmp = pmsReportWorkList.stream().filter(v -> v.getProduceOrderId().equals(pmsProduceOrder.getId())).collect(Collectors.toList());
                    BigDecimal pmsData = BigDecimal.ZERO;
                    if (CollUtil.isNotEmpty(pmsReportWorkListTmp)) {
                        for (PmsReportWork v : pmsReportWorkListTmp) {
                            pmsData = BigDecimalUtil.add(pmsData, v.getProcessQuantity());
                        }
                    }
                    finishNumData.add(pmsData);
                } else {
                    finishNumData.add(BigDecimal.ZERO);
                }
                //入库数量
                if (CollUtil.isNotEmpty(pmsCompleteStorageInfoList)) {
                    List<PmsCompleteStorageInfo> pmsCompleteStorageInfoListTmp = pmsCompleteStorageInfoList.stream().filter(v -> v.getProduceOrderId().equals(pmsProduceOrder.getId())).collect(Collectors.toList());
                    BigDecimal pmsData = BigDecimal.ZERO;
                    if (CollUtil.isNotEmpty(pmsCompleteStorageInfoListTmp)) {
                        for (PmsCompleteStorageInfo v : pmsCompleteStorageInfoListTmp) {
                            pmsData = BigDecimalUtil.add(pmsData, v.getQuantity());
                        }
                    }
                    receiptNumData.add(pmsData);

                } else {
                    receiptNumData.add(BigDecimal.ZERO);
                }
            }
        });
        planNum.setData(planNumData);
        seriesDataVO.add(planNum);
        finishNum.setData(finishNumData);
        seriesDataVO.add(finishNum);
        receiptNum.setData(receiptNumData);
        seriesDataVO.add(receiptNum);
        result.setSeriesDataVO(seriesDataVO);
        //完工数量
        return result;
    }

    @Override
    public List<OutputQualityVO> outputQualityJp(OutputQualityDTO dto) {
        Assert.isTrue(CollUtil.isNotEmpty(dto.getProductWorkshop()), "车间不能为空");
        Assert.isTrue(Objects.nonNull(dto.getDate()), "日期不能为空");
        //查询缺陷数据
        List<OutputQualityDetailVO> defectiveData = getQmsTaskDefectData(dto.getDate(), dto.getProductWorkshop());
        //缺陷数量根据班次拆分
        Map<Integer, List<OutputQualityDetailVO>> groupQualityMap = defectiveData.stream().collect(Collectors.groupingBy(OutputQualityDetailVO::getClassId));
        //白班缺陷数据
        Map<String, List<OutputQualityDetailVO>> dayShiftQuantityMap  =  Optional.ofNullable(groupQualityMap.get(PmManagerConstant.FORE_SHIFT_0)).orElse(Collections.emptyList()).stream().collect(Collectors.groupingBy(vo -> Objects.toString(vo.getProductOrder(), "")));
        //夜班缺陷数据
        Map<String, List<OutputQualityDetailVO>> nightShiftQuantityMap = Optional.ofNullable(groupQualityMap.get(PmManagerConstant.NIGHT_SHIFT_1)).orElse(Collections.emptyList()).stream().collect(Collectors.groupingBy(vo ->Objects.toString(vo.getProductOrder(),"")));
        //获取车间的名称
        Map<Integer, String> workshopMap = FeignOptional.of(() -> remoteOfficeFeign.listByIdCh(new HashSet<>(dto.getProductWorkshop())), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        Set<String> workshopName = workshopMap.values().stream().collect(Collectors.toSet());
        //获取班组长姓名
        Map<String, List<OaClassUserNameDTO>> monitorName = FeignOptional.of(() -> oaInfoService.getLeaderName(workshopName), FeignConstant.STECCN_OA_SERVICE_NAME).get();
        OutputQualityDetailDTO outputQualityDetailDTO = new OutputQualityDetailDTO();
        outputQualityDetailDTO.setStartDate(dto.getDate());
        outputQualityDetailDTO.setEndDate(dto.getDate());
        outputQualityDetailDTO.setProductWorkshopId(dto.getProductWorkshop());

        //获取平均厚度数据
        Optional<List<OutputQualityDetailVO>> optionalPlyData = Optional.ofNullable(FeignOptional.of(() -> remoteCheckTaskDetailFeignApi.listPollingDate(outputQualityDetailDTO), FeignConstant.STECCN_QMS_SERVICE_NAME).get());
        List<OutputQualityDetailVO> plyData = optionalPlyData.get();
        //将平均厚度根据班次拆分
        Map<Integer, List<OutputQualityDetailVO>> plyDataMap = plyData.stream().filter(ele -> Objects.nonNull(ele.getProductOrder())).collect(Collectors.groupingBy(OutputQualityDetailVO::getClassId));
        //早班
        Map<String, List<OutputQualityDetailVO>> dayShiftPlyDataMap = Optional.ofNullable(plyDataMap.get(PmManagerConstant.FORE_SHIFT_0)).orElse(Collections.emptyList()).stream().filter(ele -> Objects.nonNull(ele.getProductOrder())).collect(Collectors.groupingBy(OutputQualityDetailVO::getProductOrder));
        //晚班
        Map<String, List<OutputQualityDetailVO>> nightShiftPlyDataMap = Optional.ofNullable(plyDataMap.get(PmManagerConstant.NIGHT_SHIFT_1)).orElse(Collections.emptyList()).stream().collect(Collectors.groupingBy(OutputQualityDetailVO::getProductOrder));
        //获取当班拉引量数据
        Map<Integer, List<PmsPullForce>> pullForceMap = pmsPullForceService.pullForceDayByWorkshop(dto.getDate(), dto.getProductWorkshop());
        //获取截至当前日期的当月拉引量数据
        List<PmsPullForce> pullDataList = pmsPullForceService.monthlyData(dto.getDate(), dto.getProductWorkshop());
        //获取车间下的产线
        Map<Integer, List<Integer>> integerListMap = FeignOptional.of(() -> remoteOfficeFeign.childWorkshop(dto.getProductWorkshop()), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        //获取产线合集
        List<Integer> lineIds = integerListMap.values().stream().flatMap(List::stream).collect(Collectors.toList());
        //获取产线的上级车间的数据
        Map<Integer, SysOfficeVO> lineGetWorkshopMap = FeignOptional.of(() -> remoteOfficeFeign.getParentOfficeMap(lineIds), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        //获取产线名称
        Map<Integer, SysOfficeVO> lineNameMap = FeignOptional.of(() -> remoteOfficeFeign.listById(new HashSet<>(lineIds)), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();

        //获取报工记录
        List<PmsReportWorkVO> pmsReportWorkVOS = pmsReportWorkService.listBylineIds(lineIds, dto.getDate());
        Map<Long, List<PmsReportWorkVO>> reportWorkMap = pmsReportWorkVOS.stream().collect(Collectors.groupingBy(PmsReportWorkVO::getMaterialId));
        Set<Long> classIds = pmsReportWorkVOS.stream().map(PmsReportWorkVO::getClassesId).collect(Collectors.toSet());
        classIds.addAll(pmsReportWorkVOS.stream().map(PmsReportWorkVO::getClassGroupId).collect(Collectors.toSet()));
        //获取截至当前日期的当月报工数据
        List<PmsReportWorkVO> reportWorkDataList = pmsReportWorkService.monthlyData(dto.getDate(), lineIds);

        //获取厚度和生产规格数据
        List<Long> materialIds = pmsReportWorkVOS.stream().map(PmsReportWorkVO::getMaterialId).collect(Collectors.toList());
        List<Long> materialIds2 = reportWorkDataList.stream().map(PmsReportWorkVO::getMaterialId).collect(Collectors.toList());
        materialIds.addAll(materialIds2);
        //单位转换
        Map<Long, List<SysSpareUnitConversionVO>> unitConvertMap = FeignOptional.of(() -> remoteSpareUnitConvertFeignApi.convert(Sets.newHashSet(materialIds)), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        //获取班次 班组的id
        Map<Long, String> classMap = FeignOptional.of(() -> remoteDictFeignApi.listByIdCh(classIds), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();

        //获取班次的id
        Set<String> classNames = Sets.newHashSet(PmManagerConstant.FORE_SHIFT, PmManagerConstant.NIGHT_SHIFT);
        Map<String, Long> classNameMap = FeignOptional.of(() -> remoteDictFeignApi.listByNameCh(classNames), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();

        Map<Long, EqpSpareVO> materialMap = FeignOptional.of(() -> remoteSpareFeignApi.listById(materialIds), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        List<EqpSpareVO> materialList = new ArrayList<>(materialMap.values());
        //构建表格模型
        List<OutputQualityVO> resList = Lists.newArrayList();
        //构建主表格模型
        OutputQualityVO outputQualityVO = new OutputQualityVO();
        outputQualityVO.setType(1);

        SysDictVO sysDict1 = FeignOptional.of(() -> remoteDictFeignApi.getByCode(PmManagerConstant.MEASURE_UNIT_PIECE, SysDictConstant.TYPE_MEASURE_UNIT), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        SysDictVO sysDict2 = FeignOptional.of(() -> remoteDictFeignApi.getByCode(PmManagerConstant.MEASURE_UNIT_TON, SysDictConstant.TYPE_MEASURE_UNIT), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        SysDictVO sysDict3 = FeignOptional.of(() -> remoteDictFeignApi.getByCode(PmManagerConstant.MEASURE_UNIT_SM, SysDictConstant.TYPE_MEASURE_UNIT), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        cn.hutool.core.lang.Assert.notNull(sysDict1, "换算单位1不存在");
        cn.hutool.core.lang.Assert.notNull(sysDict2, "换算单位2不存在");
        cn.hutool.core.lang.Assert.notNull(sysDict3, "换算单位3不存在");

        //构建主表格数据
        List<OutputQualityDataVO> outputQualityDataVOS = Lists.newArrayList();
        if (CollUtil.isNotEmpty(materialList)) {
            for (EqpSpareVO eqpSpareVO : materialList) {
                List<PmsReportWorkVO> reportWorks = reportWorkMap.getOrDefault(eqpSpareVO.getId(),Lists.newArrayList());
                for (PmsReportWorkVO reportWork : reportWorks) {
                    OutputQualityDataVO outputQualityDataVO = new OutputQualityDataVO();
                    String classGroupName = classMap.get(reportWork.getClassGroupId());
                    //班组
                    outputQualityDataVO.setClassGroup(classGroupName);
                    //班次
                    String classes = classMap.get(reportWork.getClassesId());
                    outputQualityDataVO.setClasses(classes);
                    //获取缺陷数据
                    List<OutputQualityDetailVO> outputQualityDetailVOS = Lists.newArrayList();
                    //平均厚度
                    List<OutputQualityDetailVO> plyDataList = new ArrayList<>();
                    if (PmManagerConstant.FORE_SHIFT.equals(classes)){
                       outputQualityDetailVOS = dayShiftQuantityMap.get(reportWork.getProduceOrder());
                        plyDataList = dayShiftPlyDataMap.get(reportWork.getProduceOrder());
                    }
                    if (PmManagerConstant.NIGHT_SHIFT.equals(classes)){
                        outputQualityDetailVOS = nightShiftQuantityMap.get(reportWork.getProduceOrder());
                        plyDataList = nightShiftPlyDataMap.get(reportWork.getProduceOrder());
                    }
                    //获取厚度以及规格
                    outputQualityDataVO.setThickQuantity(eqpSpareVO.getThickQuantity());
                    outputQualityDataVO.setSpecifications(eqpSpareVO.getSpecifications());
                    BigDecimal quantity = reportWork.getProcessQuantity();

                    Integer shutUp = 0;
                    Integer startToTalk = 0;
                    Integer stone = 0;
                    Integer variant = 0;
                    BigDecimal averageThickness = BigDecimal.ZERO;
                    if (CollUtil.isNotEmpty(outputQualityDetailVOS)) {
                        for (OutputQualityDetailVO outputQualityDetailVO : outputQualityDetailVOS) {
                            if (PmManagerConstant.QMS_TASK_ITEM_SHUT_UP.equals(outputQualityDetailVO.getDefectName())) {
                                shutUp = shutUp + outputQualityDetailVO.getDefectNum();
                            }
                            if (PmManagerConstant.QMS_TASK_ITEM_START_TO_TALK.equals(outputQualityDetailVO.getDefectName())) {
                                startToTalk = startToTalk + outputQualityDetailVO.getDefectNum();
                            }
                            if (PmManagerConstant.QMS_TASK_ITEM_STONE.equals(outputQualityDetailVO.getDefectName())) {
                                stone = stone + outputQualityDetailVO.getDefectNum();
                            }
                            if (PmManagerConstant.QMS_TASK_ITEM_VARIANT.equals(outputQualityDetailVO.getDefectName())) {
                                variant = variant + outputQualityDetailVO.getDefectNum();
                            }
                        }
                        outputQualityDataVO.setAverageThickness(averageThickness);
                        outputQualityDataVO.setShutUp(shutUp);
                        outputQualityDataVO.setStartToTalk(startToTalk);
                        outputQualityDataVO.setStone(stone);
                        outputQualityDataVO.setVariant(variant);
                        //总疵点
                        outputQualityDataVO.setFlaw(shutUp + startToTalk + stone + variant);
                    } else {
                        outputQualityDataVO.setShutUp(null);
                        outputQualityDataVO.setStartToTalk(null);
                        outputQualityDataVO.setStone(null);
                        outputQualityDataVO.setVariant(null);
                        outputQualityDataVO.setFlaw(null);
                    }
                    //平均厚度
                    if (CollUtil.isNotEmpty(plyDataList)) {
                        for (OutputQualityDetailVO outputQualityDetailVO : plyDataList) {
                            if (Objects.nonNull(outputQualityDetailVO.getCheckResult())){
                                averageThickness = averageThickness.add(new BigDecimal(outputQualityDetailVO.getCheckResult()));
                            }
                        }
                    }

                    //根据报工数量计算产量 吨/平方米
                    List<SysSpareUnitConversionVO> unitConvertList = unitConvertMap.get(eqpSpareVO.getId());
                    BigDecimal value1 = UnitConvertUtil.convertMaterial(unitConvertList, sysDict1.getId(), sysDict2.getId(), quantity, BigDecimalUtil.DEFAULT_SCALE);
                    BigDecimal value2 = UnitConvertUtil.convertMaterial(unitConvertList, sysDict1.getId(), sysDict3.getId(), quantity, BigDecimalUtil.DEFAULT_SCALE);
                    outputQualityDataVO.setOutputTon(BigDecimalUtil.setScale(value1, UnitLengthUtil.UNIT_TON_LENGTH_TWO));
                    outputQualityDataVO.setOutputSq(BigDecimalUtil.setScale(value2, UnitLengthUtil.UNIT_SQUARE_METER_LENGTH));
                    //产线
                    outputQualityDataVO.setProductionLine(lineNameMap.get(reportWork.getProductLineId()).getOfficeName());
                    //产线排序
                    outputQualityDataVO.setSort(lineNameMap.get(reportWork.getProductLineId()).getTreeSort());
                    //根据产线获取车间的名称
                    SysOfficeVO sysOfficeVO = lineGetWorkshopMap.get(reportWork.getProductLineId());
                    //获取车间下的班组长信息
                    List<OaClassUserNameDTO> oaClassUserNameDTOS = monitorName.get(sysOfficeVO.getParentName());
                    if (CollUtil.isNotEmpty(oaClassUserNameDTOS)) {
                        String userName = oaClassUserNameDTOS.stream().filter(d -> classGroupName.equals(d.getClassName())).findFirst().map(OaClassUserNameDTO::getUserName).orElse(null);
                        outputQualityDataVO.setMonitor(userName);
                    }
                    //当班拉引量
                    List<PmsPullForce> pmsPullForces = pullForceMap.get(reportWork.getProductLineId());
                    if (CollUtil.isNotEmpty(pmsPullForces)) {
                        List<PmsPullForce> pullForces = pmsPullForces.stream().collect(Collectors.groupingBy(PmsPullForce::getClassesId)).get(reportWork.getClassesId());
                        if (CollUtil.isNotEmpty(pullForces)) {
                            outputQualityDataVO.setCurrentClass(pullForces.get(0).getQuantity());
                        }else {
                            //当班拉引量不存在  赋值0
                            outputQualityDataVO.setCurrentClass(BigDecimal.ZERO);
                        }
                        //计算成品率
                        outputQualityDataVO.setFinishedProduct(BigDecimalUtil.percent(value1, outputQualityDataVO.getCurrentClass(), UnitLengthUtil.UNIT_PERCENT_LENGTH));
                    }
                    outputQualityDataVOS.add(outputQualityDataVO);
                }
            }
        }
        //主表格合计计算
        if (CollUtil.isNotEmpty(outputQualityDataVOS)) {
            Map<String, List<OutputQualityDataVO>> collect = outputQualityDataVOS.stream().collect(Collectors.groupingBy(OutputQualityDataVO::getClasses));
            //早班数据
            List<OutputQualityDataVO> foreData = collect.get(PmManagerConstant.FORE_SHIFT);
            //晚班数据
            List<OutputQualityDataVO> nightData = collect.get(PmManagerConstant.NIGHT_SHIFT);
            if (CollUtil.isNotEmpty(foreData)) {
                Map<String, OutputQualityDataVO> valueMap = jpDataByline(foreData);
                for (OutputQualityDataVO foreDatum : foreData) {
                    OutputQualityDataVO outputQualityDataVO = valueMap.get(foreDatum.getProductionLine());
                    foreDatum.setShutUp(outputQualityDataVO.getShutUp());
                    foreDatum.setStartToTalk(outputQualityDataVO.getStartToTalk());
                    foreDatum.setStone(outputQualityDataVO.getStone());
                    foreDatum.setVariant(outputQualityDataVO.getVariant());
                    foreDatum.setFlaw(outputQualityDataVO.getFlaw());
                    foreDatum.setAverageThickness(outputQualityDataVO.getAverageThickness());
                    foreDatum.setCurrentClass(outputQualityDataVO.getCurrentClass());
                    foreDatum.setOutputSq(outputQualityDataVO.getOutputSq());
                    foreDatum.setOutputTon(outputQualityDataVO.getOutputTon());
                    foreDatum.setFinishedProduct(outputQualityDataVO.getFinishedProduct());
                }
                outputQualityDataVOS.add(totalJp(foreData));

            }
            if (CollUtil.isNotEmpty(nightData)) {
                Map<String, OutputQualityDataVO> valueMap = jpDataByline(nightData);
                for (OutputQualityDataVO foreDatum : nightData) {
                    OutputQualityDataVO outputQualityDataVO = valueMap.get(foreDatum.getProductionLine());
                    foreDatum.setShutUp(outputQualityDataVO.getShutUp());
                    foreDatum.setStartToTalk(outputQualityDataVO.getStartToTalk());
                    foreDatum.setStone(outputQualityDataVO.getStone());
                    foreDatum.setVariant(outputQualityDataVO.getVariant());
                    foreDatum.setFlaw(outputQualityDataVO.getFlaw());
                    foreDatum.setAverageThickness(outputQualityDataVO.getAverageThickness());
                    foreDatum.setCurrentClass(outputQualityDataVO.getCurrentClass());
                    foreDatum.setOutputSq(outputQualityDataVO.getOutputSq());
                    foreDatum.setOutputTon(outputQualityDataVO.getOutputTon());
                    foreDatum.setFinishedProduct(outputQualityDataVO.getFinishedProduct());
                }
                outputQualityDataVOS.add(totalJp(nightData));
            }
            //如果早晚班都有，则加入总计
            if(CollUtil.isNotEmpty(foreData) && CollUtil.isNotEmpty(nightData)){
                outputQualityDataVOS.add(totalAllJp(foreData,nightData));
            }


            //累计拉引量
            if (CollUtil.isNotEmpty(pullDataList)) {
                BigDecimal quantity = BigDecimal.ZERO;
                BigDecimal nightQuantity = BigDecimal.ZERO;
                Long classId = classNameMap.get(PmManagerConstant.NIGHT_SHIFT);
                for (PmsPullForce pmsPullForce : pullDataList) {
                    quantity = quantity.add(pmsPullForce.getQuantity());
                    if (dto.getDate().equals(pmsPullForce.getPullForceDate()) && classId.equals(pmsPullForce.getClassesId())) {
                        //截至日期的夜班拉引量
                        nightQuantity = BigDecimalUtil.add(pmsPullForce.getQuantity(),nightQuantity,4);
                    }
                }
                for (OutputQualityDataVO outputQualityDataVO : outputQualityDataVOS) {
                    outputQualityDataVO.setTotalPull(BigDecimalUtil.setScale(quantity, UnitLengthUtil.UNIT_TON_LENGTH));
                    if (PmManagerConstant.FORE_SHIFT.equals(outputQualityDataVO.getClasses())) {
                        outputQualityDataVO.setTotalPull(BigDecimalUtil.subtract(quantity, nightQuantity, UnitLengthUtil.UNIT_TON_LENGTH));
                    }
                }
            }
            //累计产量
            if (CollUtil.isNotEmpty(reportWorkDataList)) {
                BigDecimal quantity = BigDecimal.ZERO;
                BigDecimal quantitySq = null;
                BigDecimal nightQuantity = BigDecimal.ZERO;
                BigDecimal nightQuantitySq = null;
                LocalDateTime start = LocalDateUtil.getMorning(dto.getDate());
                Long classId = classNameMap.get(PmManagerConstant.NIGHT_SHIFT);
                for (PmsReportWorkVO pmsReportWorkVO : reportWorkDataList) {
                    //单位转换
                    //根据报工数量计算产量 吨
                    BigDecimal value1 = UnitConvertUtil.convertMaterial(unitConvertMap.get(pmsReportWorkVO.getMaterialId()), sysDict1.getId(), sysDict2.getId(), pmsReportWorkVO.getProcessQuantity(),3);
                    BigDecimal value2 = UnitConvertUtil.convertMaterial(unitConvertMap.get(pmsReportWorkVO.getMaterialId()), sysDict1.getId(), sysDict3.getId(), pmsReportWorkVO.getProcessQuantity(), 3);
                    quantity = BigDecimalUtil.add(quantity, value1);
                    quantitySq = BigDecimalUtil.add(quantitySq, value2);
                    if (pmsReportWorkVO.getClassDate().isEqual(start.toLocalDate()) && classId.equals(pmsReportWorkVO.getClassesId())) {
                        //截至日期的夜班产量
                        //根据报工数量计算产量 吨
                        BigDecimal nightQuantityTmp1 = UnitConvertUtil.convertMaterial(unitConvertMap.get(pmsReportWorkVO.getMaterialId()), sysDict1.getId(), sysDict2.getId(), pmsReportWorkVO.getProcessQuantity(), 3);
                        BigDecimal nightQuantityTmp2 = UnitConvertUtil.convertMaterial(unitConvertMap.get(pmsReportWorkVO.getMaterialId()), sysDict1.getId(), sysDict3.getId(), pmsReportWorkVO.getProcessQuantity(), 3);
                        nightQuantity = BigDecimalUtil.add(nightQuantity, nightQuantityTmp1);
                        nightQuantitySq = BigDecimalUtil.add(nightQuantitySq, nightQuantityTmp2);
                    }
                }
                for (OutputQualityDataVO outputQualityDataVO : outputQualityDataVOS) {
                    quantity = Objects.isNull(quantity) ? BigDecimal.ZERO : quantity;
                    quantitySq = Objects.isNull(quantitySq) ? BigDecimal.ZERO : quantitySq;
                    String quantity1 = BigDecimalUtil.format(quantity, UnitLengthUtil.UNIT_TON_LENGTH_TWO);
                    String quantity2 = BigDecimalUtil.format(quantitySq, UnitLengthUtil.UNIT_TON_LENGTH);
                    outputQualityDataVO.setTotalOutput(quantity1 + "/" + quantity2);
                    if (PmManagerConstant.FORE_SHIFT.equals(outputQualityDataVO.getClasses())) {
                        BigDecimal lastBd = BigDecimalUtil.subtract(quantity, nightQuantity, UnitLengthUtil.UNIT_TON_LENGTH_TWO);
                        BigDecimal lastBdSq = BigDecimalUtil.subtract(quantitySq, nightQuantitySq, UnitLengthUtil.UNIT_TON_LENGTH);
                        lastBd = Objects.isNull(lastBd) ? BigDecimal.ZERO : lastBd;
                        lastBdSq = Objects.isNull(lastBdSq) ? BigDecimal.ZERO : lastBdSq;
                        String lastBd1 = BigDecimalUtil.format(lastBd, UnitLengthUtil.UNIT_TON_LENGTH_TWO);
                        String lastBdSq2 = BigDecimalUtil.format(lastBdSq, UnitLengthUtil.UNIT_TON_LENGTH);
                        outputQualityDataVO.setTotalOutput(lastBd1 + "/" + lastBdSq2);
                    }
                }
            }
            //累计成品率
            for (OutputQualityDataVO outputQualityDataVO : outputQualityDataVOS) {
                String totalOutput = outputQualityDataVO.getTotalOutput();
                String[] split = totalOutput.split("/");
                BigDecimal totalOp = new BigDecimal(split[0]);
                BigDecimal percent = BigDecimalUtil.percent(totalOp, outputQualityDataVO.getTotalPull());
                outputQualityDataVO.setTotalFinishedProduct(percent);
            }
            //排序
            outputQualityDataVOS = outputQualityDataVOS.stream()
                    .sorted(Comparator.comparing(OutputQualityDataVO::getClasses, Comparator.nullsLast(String::compareTo))
                            .thenComparing(OutputQualityDataVO::getType)
                            .thenComparing(OutputQualityDataVO::getClassGroup, Comparator.nullsLast(String::compareTo))
                            .thenComparing(OutputQualityDataVO::getSort,Comparator.nullsLast(Integer::compareTo)))
                    .collect(Collectors.toList());

        }
        outputQualityVO.setColumns(getJpTableHeader());
        outputQualityVO.setTableList(outputQualityDataVOS);
        //从表格
        OutputQualityVO fromTableData = new OutputQualityVO();
        fromTableData.setColumns(getFromTableHeader());
        fromTableData.setFromTableList(getFromData(pmsReportWorkVOS, lineNameMap));
        fromTableData.setType(2);
        resList.add(outputQualityVO);
        resList.add(fromTableData);
        return resList;
    }

    /**
     * 合计列
     * @param dayData 白班数据
     * @param nightData 夜班数据
     * @return
     */
    private OutputQualityDataVO totalAllJp(List<OutputQualityDataVO> dayData, List<OutputQualityDataVO> nightData) {
        Integer shutUp = 0;
        Integer startToTalk = 0;
        Integer stone = 0;
        Integer variant = 0;
        BigDecimal averageThickness = BigDecimal.ZERO;
        BigDecimal currentClass = BigDecimal.ZERO;
        BigDecimal outputTon = BigDecimal.ZERO;
        BigDecimal outputSq = BigDecimal.ZERO;
        //避免重复值合计,一条产线只合计一次
        Map<String,Integer> dayProductLineCount = dayData.stream()
                .map(OutputQualityDataVO::getProductionLine).distinct().collect(Collectors.toMap(name -> name,name -> 0));
        Map<String,Integer> nightProductLineCount = nightData.stream()
                .map(OutputQualityDataVO::getProductionLine).distinct().collect(Collectors.toMap(name -> name,name -> 0));
        for (OutputQualityDataVO foreDatum : dayData) {
            if (!dayProductLineCount.get(foreDatum.getProductionLine()).equals(QtManagerConstant.NUM_0)) {
                continue;
            }
            shutUp = IntegerUtil.add(shutUp, foreDatum.getShutUp());
            startToTalk = IntegerUtil.add(startToTalk, foreDatum.getStartToTalk());
            stone = IntegerUtil.add(stone, foreDatum.getStone());
            variant = IntegerUtil.add(variant, foreDatum.getVariant());
            averageThickness = BigDecimalUtil.add(averageThickness, foreDatum.getAverageThickness());
            outputTon = BigDecimalUtil.add(outputTon, foreDatum.getOutputTon());
            outputSq = BigDecimalUtil.add(outputSq, foreDatum.getOutputSq());
            currentClass = BigDecimalUtil.add(currentClass, foreDatum.getCurrentClass());
            //累加产线
            dayProductLineCount.computeIfPresent(foreDatum.getProductionLine(), (k, v) -> v + 1);
        }

        for (OutputQualityDataVO foreDatum : nightData) {
            if (!nightProductLineCount.get(foreDatum.getProductionLine()).equals(QtManagerConstant.NUM_0)) {
                continue;
            }
            shutUp = IntegerUtil.add(shutUp, foreDatum.getShutUp());
            startToTalk = IntegerUtil.add(startToTalk, foreDatum.getStartToTalk());
            stone = IntegerUtil.add(stone, foreDatum.getStone());
            variant = IntegerUtil.add(variant, foreDatum.getVariant());
            averageThickness = BigDecimalUtil.add(averageThickness, foreDatum.getAverageThickness());
            outputTon = BigDecimalUtil.add(outputTon, foreDatum.getOutputTon());
            outputSq = BigDecimalUtil.add(outputSq, foreDatum.getOutputSq());
            currentClass = BigDecimalUtil.add(currentClass, foreDatum.getCurrentClass());
            //累加产线
            nightProductLineCount.computeIfPresent(foreDatum.getProductionLine(), (k, v) -> v + 1);
        }

        OutputQualityDataVO outputQualityDataVO = new OutputQualityDataVO();
        outputQualityDataVO.setType(2);
        outputQualityDataVO.setProductionLine("合计");
        outputQualityDataVO.setCurrentClass(BigDecimalUtil.setScale(currentClass, UnitLengthUtil.UNIT_TON_LENGTH));
        outputQualityDataVO.setOutputSq(BigDecimalUtil.setScale(outputSq, UnitLengthUtil.UNIT_SQUARE_METER_LENGTH));
        outputQualityDataVO.setOutputTon(BigDecimalUtil.setScale(outputTon, UnitLengthUtil.UNIT_TON_LENGTH));
        outputQualityDataVO.setShutUp(shutUp);
        outputQualityDataVO.setStartToTalk(startToTalk);
        outputQualityDataVO.setStone(stone);
        outputQualityDataVO.setVariant(variant);
        outputQualityDataVO.setFlaw(IntegerUtil.add(IntegerUtil.add(shutUp, startToTalk), IntegerUtil.add(stone, variant)));
        outputQualityDataVO.setAverageThickness(averageThickness.divide(new BigDecimal(dayData.size() + nightData.size())));
        outputQualityDataVO.setFinishedProduct(BigDecimalUtil.percent(outputTon, currentClass));
        outputQualityDataVO.setClasses(null);
        return outputQualityDataVO;
    }

    @Override
    public List<OutputQualitySjgVO> outputQualitySjg(OutputQualityDTO dto) {
        Assert.isTrue(CollUtil.isNotEmpty(dto.getProductWorkshop()), "车间不能为空");
        Assert.isTrue(Objects.nonNull(dto.getDate()), "日期不能为空");
        //获取缺陷数据
        List<OutputQualityDetailVO> qmsTaskDefectData = getQmsTaskDefectData(dto.getDate(), dto.getProductWorkshop());
        //缺陷数量根据班次拆分
        Map<Integer, List<OutputQualityDetailVO>> groupQualityMap = qmsTaskDefectData.stream().collect(Collectors.groupingBy(OutputQualityDetailVO::getClassId));
        //白班缺陷数据
        Map<String, List<OutputQualityDetailVO>> dayShiftQuantityMap  =  Optional.ofNullable(groupQualityMap.get(PmManagerConstant.FORE_SHIFT_0)).orElse(Collections.emptyList()).stream().collect(Collectors.groupingBy(vo -> Objects.toString(vo.getProductOrder(), "")));
        //夜班缺陷数据
        Map<String, List<OutputQualityDetailVO>> nightShiftQuantityMap = Optional.ofNullable(groupQualityMap.get(PmManagerConstant.NIGHT_SHIFT_1)).orElse(Collections.emptyList()).stream().collect(Collectors.groupingBy(vo ->Objects.toString(vo.getProductOrder(),"")));
        //获取车间下的产线
        Map<Integer, List<Integer>> integerListMap = FeignOptional.of(() -> remoteOfficeFeign.childWorkshop(dto.getProductWorkshop()), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        //获取产线合集
        List<Integer> lineIds = integerListMap.values().stream().flatMap(List::stream).collect(Collectors.toList());
        Set<Integer> lineIdSet = new HashSet<>(lineIds);
        //获取产线的上级车间的数据
        Map<Integer, SysOfficeVO> lineGetWorkshopMap = FeignOptional.of(() -> remoteOfficeFeign.getParentOfficeMap(lineIds), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        //获取产线名称
        Map<Integer, SysOfficeVO> lineNameMap = FeignOptional.of(() -> remoteOfficeFeign.listById(lineIdSet), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        SysOfficeParentDTO parentDTO = new SysOfficeParentDTO();
        parentDTO.setParentIds(lineIdSet);
        parentDTO.setOfficeType(PmManagerConstant.NUM_13);
        //获取产线下的工位
        List<SysOfficeVO> officeList = FeignOptional.of(() -> remoteOfficeFeign.listByParentIdAndType(parentDTO), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        //包含磨边工位的
        Set<Integer> stationIds = officeList.stream().filter(office -> office.getOfficeName().contains("钢化工位")).map(SysOfficeVO::getId).collect(Collectors.toSet());
        //获取报工记录
        List<PmsReportWorkVO> pmsReportWorkVOS = pmsReportWorkService.listBylineIds(lineIds, dto.getDate());
        Set<Long> reportIds = pmsReportWorkVOS.stream().map(PmsReportWorkVO::getId).collect(Collectors.toSet());
        //获取截至当前日期的当月报工数据
        List<PmsReportWorkVO> reportWorkDataList = pmsReportWorkService.monthlyData(dto.getDate(), lineIds);
        Map<Long, List<PmsReportWorkVO>> reportWorkMap = pmsReportWorkVOS.stream().collect(Collectors.groupingBy(PmsReportWorkVO::getMaterialId));
        reportIds.addAll(reportWorkDataList.stream().map(PmsReportWorkVO::getId).collect(Collectors.toSet()));
        //根据报工记录获取物料消耗
        Map<Long, List<PmsCompleteDeliveryInfoDTO>> reportWorkMaterialMap = completeDeliveryService.reportWorkMaterial(reportIds);
        //根据报工记录获取生产订单的主物料
        Map<Long, Long> materialIdMap = pmsReportWorkService.mainMaterialByReportWork(reportIds);
        //获取物料id
        List<Long> materialIds = pmsReportWorkVOS.stream().map(PmsReportWorkVO::getMaterialId).collect(Collectors.toList());
        //获取班次 班组的id
        Set<Long> classIds = pmsReportWorkVOS.stream().map(PmsReportWorkVO::getClassesId).collect(Collectors.toSet());
        classIds.addAll(pmsReportWorkVOS.stream().map(PmsReportWorkVO::getClassGroupId).collect(Collectors.toSet()));
        Map<Long, String> classMap = FeignOptional.of(() -> remoteDictFeignApi.listByIdCh(classIds), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        //获取厚度和规格数据
        Map<Long, EqpSpareVO> materialMap = FeignOptional.of(() -> remoteSpareFeignApi.listById(materialIds), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        List<EqpSpareVO> materialList = new ArrayList<>(materialMap.values());
        //获取入库产量（据订单完工入库信息，获取入库时间介于当班时间的入库数量）
        Map<Long, Map<Long, BigDecimal>> completeStorageNumMap = pmsCompleteStorageInfoService.materialMap(new HashSet<>(materialIds), dto.getDate());

        Map<String, BigDecimal> materialCompleteMap = pmsCompleteStorageInfoService.materialMap(lineIds, new HashSet<>(materialIds), dto.getDate());
        //获取当月截至当前日期的入库数量
        List<FurtherProduceReportDataVO> completeStorageList = pmsCompleteStorageInfoService.materialByDateList(new HashSet<>(materialIds), dto.getDate());
        //单位转换
        Map<Long, List<SysSpareUnitConversionVO>> unitConvertMap = FeignOptional.of(() -> remoteSpareUnitConvertFeignApi.convert(Sets.newHashSet(materialIds)), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        //获取车间的名称
        Map<Integer, String> workshopMap = FeignOptional.of(() -> remoteOfficeFeign.listByIdCh(new HashSet<>(dto.getProductWorkshop())), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        Set<String> workshopName = new HashSet<>(workshopMap.values());
        //获取班组长姓名
        Map<String, List<OaClassUserNameDTO>> monitorName = FeignOptional.of(() -> oaInfoService.getLeaderName(workshopName), FeignConstant.STECCN_OA_SERVICE_NAME).get();
        //构建表格模型
        List<OutputQualitySjgVO> resList = Lists.newArrayList();
        //构建主表格模型
        OutputQualitySjgVO outputQualitySjgVO = new OutputQualitySjgVO();
        outputQualitySjgVO.setType(1);
        //构建数据模型
        List<OutputQualitySjgDataVO> dataList = Lists.newArrayList();

        SysDictVO pieceSysDict = FeignOptional.of(() -> remoteDictFeignApi.getByCode(PmManagerConstant.MEASURE_UNIT_PIECE, SysDictConstant.TYPE_MEASURE_UNIT), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        SysDictVO meterSysDict = FeignOptional.of(() -> remoteDictFeignApi.getByCode(PmManagerConstant.MEASURE_UNIT_SM, SysDictConstant.TYPE_MEASURE_UNIT), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        Assert.notNull(meterSysDict, "片单位不存在");
        Assert.notNull(meterSysDict, "平方米单位不存在");

        //处理主数据
        if (CollUtil.isNotEmpty(materialList)) {
            for (EqpSpareVO eqpSpareVO : materialList) {
                //获取物料的报工数据
                List<PmsReportWorkVO> reportWorks = reportWorkMap.get(eqpSpareVO.getId());
                if (CollUtil.isNotEmpty(reportWorks)) {
                    for (PmsReportWorkVO reportWork : reportWorks) {
                        OutputQualitySjgDataVO outputQualitySjgDataVO = new OutputQualitySjgDataVO();
                        //班次
                        String className = classMap.get(reportWork.getClassesId());
                        outputQualitySjgDataVO.setClasses(className);
                        //班组
                        String classGroupName = classMap.get(reportWork.getClassGroupId());
                        outputQualitySjgDataVO.setClassGroup(classGroupName);
                        //产线
                        outputQualitySjgDataVO.setProductLine(lineNameMap.get(reportWork.getProductLineId()).getOfficeName());
                        //产线排序
                        outputQualitySjgDataVO.setSort(lineNameMap.get(reportWork.getProductLineId()).getTreeSort());
                        //根据产线获取车间的名称
                        SysOfficeVO sysOfficeVO = lineGetWorkshopMap.get(reportWork.getProductLineId());
                        //获取车间下的班组长信息
                        List<OaClassUserNameDTO> oaClassUserNameDTOS = monitorName.get(sysOfficeVO.getOfficeName());
                        if (CollUtil.isNotEmpty(oaClassUserNameDTOS)) {
                            String userName = oaClassUserNameDTOS.stream().filter(d -> classGroupName.equals(d.getClassName())).findFirst().map(OaClassUserNameDTO::getUserName).orElse(null);
                            outputQualitySjgDataVO.setMonitor(userName);
                        }
                        //获取厚度以及规格
                        outputQualitySjgDataVO.setThickQuantity(eqpSpareVO.getThickQuantity());
                        outputQualitySjgDataVO.setSpecifications(eqpSpareVO.getSpecifications());
                        //缺陷数据
                        List<OutputQualityDetailVO> defectData = new ArrayList<>();
                        if (PmManagerConstant.FORE_SHIFT.equals(className)){
                            defectData = dayShiftQuantityMap.get(reportWork.getProduceOrder());
                        }
                        if (PmManagerConstant.NIGHT_SHIFT.equals(className)){
                            defectData = nightShiftQuantityMap.get(reportWork.getProduceOrder());
                        }
                        //原片不良
                        Integer rawClassBadness = 0;
                        //磨边不良
                        Integer edgingBadness = 0;
                        //打孔不良
                        Integer punchingBadness = 0;
                        //丝印不良
                        Integer silkScreenBadness = 0;
                        //钢化不良
                        Integer temperingBadness = 0;
                        if (CollUtil.isNotEmpty(defectData)) {
                            for (OutputQualityDetailVO defectDatum : defectData) {
                                if (PmManagerConstant.BadnessType.EDGING.getName().equals(defectDatum.getDefectCategoryName())) {
                                    edgingBadness = IntegerUtil.add(edgingBadness, defectDatum.getDefectNum());
                                }
                                if (PmManagerConstant.BadnessType.RAW_CLASS.getName().equals(defectDatum.getDefectCategoryName())) {
                                    rawClassBadness = IntegerUtil.add(rawClassBadness, defectDatum.getDefectNum());
                                }
                                if (PmManagerConstant.BadnessType.PUNCHING.getName().equals(defectDatum.getDefectCategoryName())) {
                                    punchingBadness = IntegerUtil.add(punchingBadness, defectDatum.getDefectNum());
                                }
                                if (PmManagerConstant.BadnessType.SILK_SCREEN.getName().equals(defectDatum.getDefectCategoryName())) {
                                    silkScreenBadness = IntegerUtil.add(silkScreenBadness, defectDatum.getDefectNum());
                                }
                                if (PmManagerConstant.BadnessType.TEMPERING.getName().equals(defectDatum.getDefectCategoryName())) {
                                    temperingBadness = IntegerUtil.add(temperingBadness, defectDatum.getDefectNum());
                                }
                            }
                        }
                        outputQualitySjgDataVO.setRawClassBadness(rawClassBadness);
                        outputQualitySjgDataVO.setEdgingBadness(edgingBadness);
                        outputQualitySjgDataVO.setPunchingBadness(punchingBadness);
                        outputQualitySjgDataVO.setSilkScreenBadness(silkScreenBadness);
                        outputQualitySjgDataVO.setTemperingBadness(temperingBadness);
                        Integer totalSum = IntegerUtil.sum(Arrays.asList(rawClassBadness, edgingBadness, punchingBadness, silkScreenBadness, temperingBadness));
                        //不良合计
                        outputQualitySjgDataVO.setBadTotal(totalSum);
                        //原片耗用量
                        //获取该产线下的磨边工位数据
                        if (stationIds.contains(reportWork.getStationId())) {
                            List<PmsCompleteDeliveryInfoDTO> pmsCompleteDeliveryInfoDTOS = reportWorkMaterialMap.get(reportWork.getId());
                            //获取生产订单的主物料
                            Long materialId = materialIdMap.get(reportWork.getId());
                            if (CollUtil.isNotEmpty(pmsCompleteDeliveryInfoDTOS) && Objects.nonNull(materialId)) {
                                List<BigDecimal> collect = pmsCompleteDeliveryInfoDTOS.stream().filter(p -> materialId.equals(p.getMaterialId())).map(PmsCompleteDeliveryInfoDTO::getQuantity).collect(Collectors.toList());
                                BigDecimal sum = BigDecimalUtil.sum(collect);
                                sum = BigDecimalUtil.setScale(sum, UnitLengthUtil.UNIT_PIECE_LENGTH);
                                //根据物料获取转换公式
                                BigDecimal square = UnitConvertUtil.convertMaterial(unitConvertMap.get(materialId), pieceSysDict.getId(), meterSysDict.getId(), sum, UnitLengthUtil.UNIT_SQUARE_METER_LENGTH);
                                outputQualitySjgDataVO.setCurrentClassSlice(sum);
                                outputQualitySjgDataVO.setCurrentClassSquare(square);
                            }
                        }

                        //入库产量
//                        Map<Long, BigDecimal> longBigDecimalMap = completeStorageNumMap.get(eqpSpareVO.getId());
                        //入库产量
                        BigDecimal completeNum = materialCompleteMap.get(JointUtil.joint(JointUtil.SEPARATOR_UNDERLINE,reportWork.getProductLineId(), reportWork.getMaterialId(), reportWork.getClassesId()));

                        //根据班次计算入库数量
                        BigDecimal completeStorageNum = BigDecimal.ZERO;
                        if (Objects.nonNull(completeNum)) {
                            completeStorageNum = BigDecimalUtil.setScale(completeNum, UnitLengthUtil.UNIT_PIECE_LENGTH);
                            //单位转换
                            BigDecimal square = UnitConvertUtil.convertMaterial(unitConvertMap.get(eqpSpareVO.getId()), pieceSysDict.getId(), meterSysDict.getId(), completeStorageNum, UnitLengthUtil.UNIT_SQUARE_METER_LENGTH);
                            outputQualitySjgDataVO.setInStorageSlice(completeStorageNum);
                            outputQualitySjgDataVO.setInStorageSquare(square);
                        }
                        //加工产量（该产线下的钢化工位的报工数据）
                        BigDecimal processQuantity = BigDecimal.ZERO;
                        if (stationIds.contains(reportWork.getStationId())) {
                            //加工产量：根据该产线钢化工位，该产品当班报工记录，合格数量累加，根据片数计算平方米
                            processQuantity = BigDecimalUtil.subtract(reportWork.getProcessQuantity(), reportWork.getBadnessQuantity());
                            //单位转换
                            processQuantity = BigDecimalUtil.setScale(processQuantity, UnitLengthUtil.UNIT_PIECE_LENGTH);
                            BigDecimal square = UnitConvertUtil.convertMaterial(unitConvertMap.get(eqpSpareVO.getId()), pieceSysDict.getId(), meterSysDict.getId(), processQuantity, UnitLengthUtil.UNIT_SQUARE_METER_LENGTH);
                            outputQualitySjgDataVO.setProcessQuantitySlice(processQuantity);
                            outputQualitySjgDataVO.setProcessQuantitySquare(square);
                        }
                        //成品率
                        outputQualitySjgDataVO.setFinishedProduct(BigDecimalUtil.percent(completeStorageNum, processQuantity));
                        dataList.add(outputQualitySjgDataVO);
                    }
                }
            }
        }
        //合计 月累计
        if (CollUtil.isNotEmpty(dataList)) {
            Map<String, List<OutputQualitySjgDataVO>> collect = dataList.stream().collect(Collectors.groupingBy(OutputQualitySjgDataVO::getClasses));
            //早班
            List<OutputQualitySjgDataVO> foreShiftData = collect.get(PmManagerConstant.FORE_SHIFT);
            //晚班
            List<OutputQualitySjgDataVO> nightShiftData = collect.get(PmManagerConstant.NIGHT_SHIFT);
            if (CollUtil.isNotEmpty(foreShiftData)) {
                dataList.add(totalSjg(foreShiftData));
                OutputQualitySjgDataVO outputQualitySjgDataVO = monthToTotal(PmManagerConstant.FORE_SHIFT, dto.getDate(), reportWorkDataList, completeStorageList, stationIds, reportWorkMaterialMap, materialIdMap, unitConvertMap);
                outputQualitySjgDataVO.setClasses(PmManagerConstant.FORE_SHIFT);
                dataList.add(outputQualitySjgDataVO);
            }
            if (CollUtil.isNotEmpty(nightShiftData)) {
                dataList.add(totalSjg(nightShiftData));
                OutputQualitySjgDataVO outputQualitySjgDataVO = monthToTotal(PmManagerConstant.NIGHT_SHIFT, dto.getDate(), reportWorkDataList, completeStorageList, stationIds, reportWorkMaterialMap, materialIdMap, unitConvertMap);
                outputQualitySjgDataVO.setClasses(PmManagerConstant.NIGHT_SHIFT);
                dataList.add(outputQualitySjgDataVO);
            }
            //排序
            dataList = dataList.stream()
                    .sorted(Comparator.comparing(OutputQualitySjgDataVO::getClasses, Comparator.nullsFirst(String::compareTo))
                            .thenComparing(OutputQualitySjgDataVO::getType)
                            .thenComparing(OutputQualitySjgDataVO::getClassGroup, Comparator.nullsFirst(String::compareTo))
                            .thenComparing(OutputQualitySjgDataVO::getSort,Comparator.nullsFirst(Integer::compareTo)))
                    .collect(Collectors.toList());
        }
        outputQualitySjgVO.setTableList(dataList);
        outputQualitySjgVO.setColumns(getSjgTableHeader());
        resList.add(outputQualitySjgVO);
        //构建从表格模型
        OutputQualitySjgVO outputQualitySjgVO1 = new OutputQualitySjgVO();
        outputQualitySjgVO.setType(2);
        outputQualitySjgVO1.setColumns(getFromTableHeader());
        outputQualitySjgVO1.setFromTableList(getFromData(pmsReportWorkVOS, lineNameMap));
        resList.add(outputQualitySjgVO1);
        return resList;
    }

    @Override
    public List<SubstrateDataListVO> substrateDataList(SubstrateDataDTO dto) {
        List<SubstrateDataListVO> rowDataList = pmsReportWorkMapper.qmsSubstrateReport(dto.getProductLineIdList(), dto.getStart(), dto.getEnd());
        return rowDataList;
    }

    @Override
    public EpChartGraphVO<List<String>,List<TripleDTO<String, BigDecimal, String>> > rawMaterialWorkshopHistogram(LocalDate startDate, LocalDate endDate) {
        //获取计划数（等待采购数据）
        //获取到货数（入库管理 采购入库数据）
        List<String> materialNames =Arrays.asList("石英砂（低铁硅砂）","纯碱","石灰石","白云石（普通白云石）","三氧化二铝","复合澄清剂","小料","碎玻璃（外购）");
        RawMaterialReportDTO rawMaterialReportDTO = new RawMaterialReportDTO();
        rawMaterialReportDTO.setMaterialName(materialNames);
        rawMaterialReportDTO.setStartTime(startDate.atTime(8, 0, 0));
        rawMaterialReportDTO.setEndTime(endDate.plusDays(1).atTime(7, 59, 59));
        Map<String, BigDecimal> storageQuantityMap = FeignOptional.of(() -> remoteWmsStorageReceiptFeignApi.purchaseQuantity(rawMaterialReportDTO), FeignConstant.STECCN_WMS_SERVICE_NAME).get();
        //获取领用数（调拨单源仓库为主要材料库数据）
        Map<String, BigDecimal> transferSlipMap = FeignOptional.of(() -> remoteWmsTransferSlipFeignApi.materialDepot(rawMaterialReportDTO),FeignConstant.STECCN_WMS_SERVICE_NAME).get();
        //获取耗用数（电子秤读数）
        Map<String, BigDecimal> countMap = FeignOptional.of(() -> remoteDpFeignApi.countMap(new TupleDTO<LocalDateTime, LocalDateTime>(startDate.atTime(8, 0, 0), endDate.plusDays(1).atTime(7, 59, 59))), FeignConstant.STECCN_DP_SERVICE_NAME).get();
        //表格
        EpChartGraphVO<List<String>,  List<TripleDTO<String, BigDecimal, String>> > res = new EpChartGraphVO<>();
        //图例
        List<String> legend = Arrays.asList("申请数","到货数","消耗数","领用数");
        res.setLegend(legend);
        //主数据
        List<QtSeriesDataDTO> seriesDataVO = Lists.newArrayList();

        QtSeriesDataDTO<String,BigDecimal> qtSeriesDataDTO = new QtSeriesDataDTO<String,BigDecimal>();
        qtSeriesDataDTO.setName("申请数");
        List<BigDecimal> dataQuantityList = Lists.newArrayList();

        QtSeriesDataDTO<String,BigDecimal> storageQuantityDTO = new QtSeriesDataDTO<String,BigDecimal>();
        storageQuantityDTO.setName("到货数");
        List<BigDecimal> storageQuantityList = Lists.newArrayList();

        QtSeriesDataDTO<String,BigDecimal>  transferSlipDTO = new QtSeriesDataDTO<String,BigDecimal>();
        transferSlipDTO.setName("消耗数");
        List<BigDecimal> transferSlipQuantityList = Lists.newArrayList();

        QtSeriesDataDTO<String,BigDecimal> countDTO = new QtSeriesDataDTO<String,BigDecimal>();
        countDTO.setName("领用数");
        List<BigDecimal> countQuantityList = Lists.newArrayList();
        BigDecimal planTotalQuantity = BigDecimal.ZERO;
        BigDecimal storageTotalQuantity = BigDecimal.ZERO;
        BigDecimal transferSlipTotalQuantity = BigDecimal.ZERO;
        BigDecimal countTotalQuantity = BigDecimal.ZERO;

        for (String material : materialNames) {
            // 到货数
            BigDecimal storageQuantity = Optional.ofNullable(storageQuantityMap.get(material)).orElse(BigDecimal.ZERO);
            storageQuantityList.add(storageQuantity);
            storageTotalQuantity = storageTotalQuantity.add(storageQuantity);

            // 消耗数
            BigDecimal transferSlipQuantity = Optional.ofNullable(transferSlipMap.get(material)).orElse(BigDecimal.ZERO);
            transferSlipQuantityList.add(transferSlipQuantity);
            transferSlipTotalQuantity = transferSlipTotalQuantity.add(transferSlipQuantity);

            // 领用数
            BigDecimal countQuantity = Optional.ofNullable(countMap.get(material)).orElse(BigDecimal.ZERO);
            countQuantityList.add(countQuantity);
            countTotalQuantity = countTotalQuantity.add(countQuantity);
        };
        qtSeriesDataDTO.setData(dataQuantityList);
        storageQuantityDTO.setData(storageQuantityList);
        transferSlipDTO.setData(transferSlipQuantityList);
        countDTO.setData(countQuantityList);
        seriesDataVO.add(qtSeriesDataDTO);
        seriesDataVO.add(storageQuantityDTO);
        seriesDataVO.add(transferSlipDTO);
        seriesDataVO.add(countDTO);
        res.setSeriesDataVO(seriesDataVO);
        //其他数据
        List<TripleDTO<String, BigDecimal, String>>  list = Lists.newArrayList();
        TripleDTO<String, BigDecimal, String> planTotalQuantityDTO = new TripleDTO<>();
        planTotalQuantityDTO.setField1("计划到货总数");
        planTotalQuantityDTO.setField2(planTotalQuantity);
        planTotalQuantityDTO.setField3("吨");
        list.add(planTotalQuantityDTO);
        TripleDTO<String, BigDecimal, String> storageTotalQuantityDTO = new TripleDTO<>();
        storageTotalQuantityDTO.setField1("实际收货总数");
        storageTotalQuantityDTO.setField2(storageTotalQuantity);
        storageTotalQuantityDTO.setField3("吨");
        list.add(storageTotalQuantityDTO);
        TripleDTO<String, BigDecimal, String> transferSlipTotalQuantityDTO = new TripleDTO<>();
        transferSlipTotalQuantityDTO.setField1("消耗总数");
        transferSlipTotalQuantityDTO.setField2(transferSlipTotalQuantity);
        transferSlipTotalQuantityDTO.setField3("吨");
        list.add(transferSlipTotalQuantityDTO);
        TripleDTO<String, BigDecimal, String> countTotalQuantityDTO = new TripleDTO<>();
        countTotalQuantityDTO.setField1("领用总数");
        countTotalQuantityDTO.setField2(countTotalQuantity);
        countTotalQuantityDTO.setField3("吨");
        list.add(countTotalQuantityDTO);
        res.setOther(list);
        res.setXData(materialNames);
        return res;
    }

    @Override
    public EpChartGraphVO<List<String>, List<TripleDTO<String, BigDecimal, String>>> materialConsumeSjg(PmsOperateDriverDTO dto) {
        QtManagerConstant.QtGranularityEnum granularity = QtManagerConstant.QtGranularityEnum.getByCode(dto.getGranularity());
        cn.hutool.core.lang.Assert.notNull(granularity, "粒度不存在");
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
        Set<Integer> lineIds = Sets.newHashSet();
        sysOfficeVOMap.values().forEach(e->lineIds.add(e.getId()));
        LocalDateTime start = LocalDateUtil.getMorning(dto.getStart());
        LocalDateTime end = LocalDateUtil.getMorning(dto.getEnd()).plusDays(1);
        //获取报工的物料消耗
        List<Tuple4DTO<String, String, BigDecimal,Long>> dataList = pmsReportWorkService.materialConsumeNoMain(lineIds,start,end, granularity);
        Map<String,Map<String,BigDecimal>> map = Maps.newHashMap();
        Map<String,String> materialUnitMap = Maps.newHashMap();
        Set<Long> unitIds = dataList.stream().collect(Collectors.groupingBy(Tuple4DTO::getField4)).keySet();
        Map<Long, String> unitMap = FeignOptional.of(() -> remoteDictFeignApi.listByIdCh(unitIds), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        Map<String,BigDecimal> valueTotalMap = Maps.newHashMap();
        if (CollUtil.isNotEmpty(dataList)){
            //数据根据物料名称分组
            Map<String, List<Tuple4DTO<String, String, BigDecimal,Long>>> dataByDateMap = dataList.stream().collect(Collectors.groupingBy(Tuple4DTO::getField2));
            dataByDateMap.forEach((k,v)->{
                Map<String, BigDecimal> dataMap = Maps.newHashMap();
                v.forEach(ele->{
                    MapUtil.add(dataMap,String.valueOf(ele.getField1()),ele.getField3(),(x,y)->BigDecimalUtil.add(x,y,3));
                    MapUtil.add(valueTotalMap,k,ele.getField3(),(x,y)->BigDecimalUtil.add(x,y,3));
                });
                map.put(k,dataMap);
                materialUnitMap.put(k,unitMap.get(v.get(0).getField4()));
            });
        }
        //组数据
        EpChartGraphVO<List<String>,List<TripleDTO<String, BigDecimal, String>>> res = new EpChartGraphVO<>();
        //图例
        List<String> legends = new ArrayList<>(dataList.stream().collect(Collectors.groupingBy(Tuple4DTO::getField2)).keySet());
        res.setLegend(legends);
        List<String> xAisList = Lists.newArrayList();
        List<QtSeriesDataDTO> seriesDataVO = Lists.newArrayList();
        //x主数据
        List<LocalDate> timeList = ShardTimeUtil.cutDate(granularity.getType(), dto.getStart(),dto.getEnd());
        for (int i = 1; i < timeList.size(); i++) {
            LocalDate ele = timeList.get(i - 1);
            xAisList.add(XAxisUtil.convert(granularity, ele));
        }
        res.setXData(xAisList);
        //冗余数据
        List<TripleDTO<String,BigDecimal,String>> otherList = Lists.newArrayList();
        TripleDTO<String,BigDecimal,String> totalOther = new TripleDTO<>();
        totalOther.setField1("耗料总量");
        totalOther.setField2(valueTotalMap.values().stream().reduce(BigDecimal.ZERO, BigDecimalUtil::add));
        otherList.add(totalOther);
        if (CollUtil.isNotEmpty(legends)){
            legends.forEach( legend->{
                //主数据明细
                QtSeriesDataDTO<String,BigDecimal> dataVO = new QtSeriesDataDTO<>();
                dataVO.setName(legend);
                List<BigDecimal> dataVOList = Lists.newArrayList();
                Map<String, BigDecimal> bigDecimalMap = map.get(legend);
                for (int i = 1; i < timeList.size(); i++) {
                    LocalDate ele = timeList.get(i - 1);
                    String key = MysqlDateKeyUtil.convert(granularity, ele);
                    BigDecimal bigDecimal = bigDecimalMap.get(key) == null ? BigDecimal.ZERO : bigDecimalMap.get(key);
                    dataVOList.add(bigDecimal);
                }
                dataVO.setData(dataVOList);
                seriesDataVO.add(dataVO);
                //冗余明细数据
                TripleDTO<String,BigDecimal,String> other = new TripleDTO<>();
                other.setField1(legend);
                other.setField2(valueTotalMap.get(legend));
                other.setField3(materialUnitMap.get(legend));
                otherList.add(other);
            });
        }
        res.setSeriesDataVO(seriesDataVO);
        res.setOther(otherList);
        return res;
    }


    /**
     * 获取基片质量报表基片车间根据产线的合计数据
     */
    private Map<String,OutputQualityDataVO> jpDataByline(List<OutputQualityDataVO> dataList){
        Map<String,OutputQualityDataVO> valueMap = new HashMap<>();
        if (CollUtil.isNotEmpty(dataList)) {
            //根据线别将数据拆分
            Map<String, List<OutputQualityDataVO>> lineMap = dataList.stream().collect(Collectors.groupingBy(OutputQualityDataVO::getProductionLine));
            for (Map.Entry<String, List<OutputQualityDataVO>> entry : lineMap.entrySet()) {
                Integer shutUp = null;
                Integer startToTalk = null;
                Integer stone = null;
                Integer variant = null;
                Integer flaw = null;
                BigDecimal averageThickness = null;
                BigDecimal currentClass =null;
                BigDecimal outputTon = null;
                BigDecimal outputSq = null;
                BigDecimal finishedProduct = null;
                for (OutputQualityDataVO outputQualityDataVO : entry.getValue()) {
                    shutUp = IntegerUtil.add(shutUp, outputQualityDataVO.getShutUp());
                    startToTalk = IntegerUtil.add(startToTalk, outputQualityDataVO.getStartToTalk());
                    stone = IntegerUtil.add(stone, outputQualityDataVO.getStone());
                    variant = IntegerUtil.add(variant, outputQualityDataVO.getVariant());
                    flaw = IntegerUtil.add(flaw, outputQualityDataVO.getFlaw());
                    averageThickness = BigDecimalUtil.add(averageThickness, outputQualityDataVO.getAverageThickness());
                    //不需要累加当班拉引量
//                    currentClass = BigDecimalUtil.add(currentClass, outputQualityDataVO.getCurrentClass());
                    currentClass = outputQualityDataVO.getCurrentClass();
                    outputTon = BigDecimalUtil.add(outputTon,outputQualityDataVO.getOutputTon());
                    outputSq = BigDecimalUtil.add(outputSq,outputQualityDataVO.getOutputSq());
                    finishedProduct = BigDecimalUtil.add(finishedProduct,outputQualityDataVO.getFinishedProduct());
                }
                OutputQualityDataVO outputQualityDataVO = new OutputQualityDataVO();
                outputQualityDataVO.setShutUp(shutUp);
                outputQualityDataVO.setStartToTalk(startToTalk);
                outputQualityDataVO.setStone(stone);
                outputQualityDataVO.setVariant(variant);
                outputQualityDataVO.setFlaw(flaw);
                outputQualityDataVO.setAverageThickness(averageThickness);
                outputQualityDataVO.setCurrentClass(currentClass);
                outputQualityDataVO.setOutputSq(outputSq);
                outputQualityDataVO.setOutputTon(outputTon);
                outputQualityDataVO.setFinishedProduct(finishedProduct);
                valueMap.put(entry.getKey(),outputQualityDataVO);
            }
        }
        return valueMap;
    }


    /**
     * 产量质量报表从表格
     */
    private List<OutputQualityFromDataVO> getFromData(List<PmsReportWorkVO> pmsReportWorkVOS, Map<Integer, SysOfficeVO> lineNameMap) {
        List<OutputQualityFromDataVO> fromTableDataList = Lists.newArrayList();
        if (CollUtil.isNotEmpty(pmsReportWorkVOS)) {
            Set<Long> unitIds = Sets.newHashSet();
            for (PmsReportWorkVO pmsReportWorkVO : pmsReportWorkVOS) {
                if (CollUtil.isNotEmpty(pmsReportWorkVO.getList())) {
                    for (PmsReportWorkHourVO pmsReportWorkHourVO : pmsReportWorkVO.getList()) {
                        unitIds.add(pmsReportWorkHourVO.getUnitId());
                        unitIds.add(pmsReportWorkHourVO.getCauseId());
                        OutputQualityFromDataVO outputQualityFromDataVO = new OutputQualityFromDataVO();
                        outputQualityFromDataVO.setProduceLineName(lineNameMap.get(pmsReportWorkVO.getProductLineId()).getOfficeName());
                        outputQualityFromDataVO.setClassName(pmsReportWorkVO.getClasses());
                        outputQualityFromDataVO.setStartTime(pmsReportWorkHourVO.getStartTime());
                        outputQualityFromDataVO.setEndTime(pmsReportWorkHourVO.getEndTime());
                        outputQualityFromDataVO.setDuration(pmsReportWorkHourVO.getManHour());
                        outputQualityFromDataVO.setRemarks(pmsReportWorkHourVO.getNote());
                        outputQualityFromDataVO.setUnitId(pmsReportWorkHourVO.getUnitId());
                        outputQualityFromDataVO.setCauseId(pmsReportWorkHourVO.getCauseId());
                        fromTableDataList.add(outputQualityFromDataVO);
                    }
                }
            }
            //获取工时单位以及原因
            Map<Long, String> nameMap = FeignOptional.of(() -> remoteDictFeignApi.listByIdCh(unitIds), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            fromTableDataList.forEach(fromDataVO -> {
                fromDataVO.setCause(nameMap.get(fromDataVO.getCauseId()));
                fromDataVO.setUnitName(nameMap.get(fromDataVO.getUnitId()));
            });
        }
        return fromTableDataList;
    }

    /**
     * 深加工表格表头
     *
     * @return
     */
    private List<OutputQualityHeaderVO> getSjgTableHeader() {
        //构建主表格表头
        List<OutputQualityHeaderVO> columns = Lists.newArrayList();
        //班次
        OutputQualityHeaderVO classHeader = new OutputQualityHeaderVO();
        classHeader.setLabel("班次");
        classHeader.setProp("classes");
        columns.add(classHeader);
        //班组
        OutputQualityHeaderVO groupHeader = new OutputQualityHeaderVO();
        groupHeader.setLabel("班组");
        groupHeader.setProp("classGroup");
        columns.add(groupHeader);
        //班长
//        OutputQualityHeaderVO monitorHeader = new OutputQualityHeaderVO();
//        monitorHeader.setLabel("班长");
//        monitorHeader.setProp("monitor");
//        columns.add(monitorHeader);
        //线别
        OutputQualityHeaderVO productionLineHeader = new OutputQualityHeaderVO();
        productionLineHeader.setLabel("线别");
        productionLineHeader.setProp("productLine");
        columns.add(productionLineHeader);
        //厚度
        OutputQualityHeaderVO thicknessHeader = new OutputQualityHeaderVO();
        thicknessHeader.setLabel("厚度");
        thicknessHeader.setProp("thickQuantity");
        List<OutputQualityHeaderVO> childThicknessHeader = Lists.newArrayList();
        OutputQualityHeaderVO thicknessMmHeader = new OutputQualityHeaderVO();
        thicknessMmHeader.setProp("thickQuantity");
        thicknessMmHeader.setLabel("mm");
        childThicknessHeader.add(thicknessMmHeader);
        thicknessHeader.setChildren(childThicknessHeader);
        columns.add(thicknessHeader);
        //生产规格
        OutputQualityHeaderVO productionStandardHeader = new OutputQualityHeaderVO();
        productionStandardHeader.setLabel("生产规格");
        productionStandardHeader.setProp("specifications");
        List<OutputQualityHeaderVO> childProductionStandardHeader = Lists.newArrayList();
        OutputQualityHeaderVO productionStandardMmHeader = new OutputQualityHeaderVO();
        productionStandardMmHeader.setProp("specifications");
        productionStandardMmHeader.setLabel("mm");
        childProductionStandardHeader.add(productionStandardMmHeader);
        productionStandardHeader.setChildren(childProductionStandardHeader);
        columns.add(productionStandardHeader);
        //原片不良
        OutputQualityHeaderVO header1 = new OutputQualityHeaderVO();
        header1.setLabel("原片不良");
        header1.setProp("rawClassBadness");
        List<OutputQualityHeaderVO> childHeaders1 = Lists.newArrayList();
        OutputQualityHeaderVO unitHeader1 = new OutputQualityHeaderVO();
        unitHeader1.setProp("rawClassBadness");
        unitHeader1.setLabel(PmManagerConstant.MEASURE_UNIT_PIECE);
        childHeaders1.add(unitHeader1);
        header1.setChildren(childHeaders1);
        columns.add(header1);
        //磨边不良
        OutputQualityHeaderVO header2 = new OutputQualityHeaderVO();
        header2.setLabel("磨边不良");
        header2.setProp("edgingBadness");
        List<OutputQualityHeaderVO> childHeaders2 = Lists.newArrayList();
        OutputQualityHeaderVO unitHeader2 = new OutputQualityHeaderVO();
        unitHeader2.setProp("edgingBadness");
        unitHeader2.setLabel(PmManagerConstant.MEASURE_UNIT_PIECE);
        childHeaders2.add(unitHeader2);
        header2.setChildren(childHeaders2);
        columns.add(header2);
        //打孔不良
        OutputQualityHeaderVO header3 = new OutputQualityHeaderVO();
        header3.setLabel("打孔不良");
        header3.setProp("punchingBadness");
        List<OutputQualityHeaderVO> childHeaders3 = Lists.newArrayList();
        OutputQualityHeaderVO unitHeader3 = new OutputQualityHeaderVO();
        unitHeader3.setProp("punchingBadness");
        unitHeader3.setLabel(PmManagerConstant.MEASURE_UNIT_PIECE);
        childHeaders3.add(unitHeader3);
        header3.setChildren(childHeaders3);
        columns.add(header3);
        //丝印/镀膜不良
        OutputQualityHeaderVO header4 = new OutputQualityHeaderVO();
        header4.setLabel("丝印/镀膜不良");
        header4.setProp("silkScreenBadness");
        List<OutputQualityHeaderVO> childHeaders4 = Lists.newArrayList();
        OutputQualityHeaderVO unitHeader4 = new OutputQualityHeaderVO();
        unitHeader4.setProp("silkScreenBadness");
        unitHeader4.setLabel(PmManagerConstant.MEASURE_UNIT_PIECE);
        childHeaders4.add(unitHeader4);
        header4.setChildren(childHeaders4);
        columns.add(header4);
        //钢化不良
        OutputQualityHeaderVO header5 = new OutputQualityHeaderVO();
        header5.setLabel("钢化不良");
        header5.setProp("temperingBadness");
        List<OutputQualityHeaderVO> childHeaders5 = Lists.newArrayList();
        OutputQualityHeaderVO unitHeader5 = new OutputQualityHeaderVO();
        unitHeader5.setProp("temperingBadness");
        unitHeader5.setLabel(PmManagerConstant.MEASURE_UNIT_PIECE);
        childHeaders5.add(unitHeader5);
        header5.setChildren(childHeaders5);
        columns.add(header5);
        //不良合计
        OutputQualityHeaderVO header6 = new OutputQualityHeaderVO();
        header6.setLabel("不良合计");
        header6.setProp("badTotal");
        List<OutputQualityHeaderVO> childHeaders6 = Lists.newArrayList();
        OutputQualityHeaderVO unitHeader6 = new OutputQualityHeaderVO();
        unitHeader6.setProp("badTotal");
        unitHeader6.setLabel(PmManagerConstant.MEASURE_UNIT_PIECE);
        childHeaders6.add(unitHeader6);
        header6.setChildren(childHeaders6);
        columns.add(header6);
        //原片耗用量
        OutputQualityHeaderVO header7 = new OutputQualityHeaderVO();
        header7.setLabel("原片耗用量");
        header7.setProp("7");
        List<OutputQualityHeaderVO> childHeaders7 = Lists.newArrayList();
        OutputQualityHeaderVO unitHeader71 = new OutputQualityHeaderVO();
        unitHeader71.setProp("currentClassSlice");
        unitHeader71.setLabel(PmManagerConstant.MEASURE_UNIT_PIECE);
        childHeaders7.add(unitHeader71);
        OutputQualityHeaderVO unitHeader72 = new OutputQualityHeaderVO();
        unitHeader72.setProp("currentClassSquare");
        unitHeader72.setLabel(PmManagerConstant.MEASURE_UNIT_SM);
        childHeaders7.add(unitHeader72);
        header7.setChildren(childHeaders7);
        columns.add(header7);
        //入库产量
        OutputQualityHeaderVO header8 = new OutputQualityHeaderVO();
        header8.setLabel("产量");
        header8.setProp("8");
        List<OutputQualityHeaderVO> childHeaders8 = Lists.newArrayList();
        OutputQualityHeaderVO unitHeader81 = new OutputQualityHeaderVO();
        unitHeader81.setProp("inStorageSlice");
        unitHeader81.setLabel(PmManagerConstant.MEASURE_UNIT_PIECE);
        childHeaders8.add(unitHeader81);
        OutputQualityHeaderVO unitHeader82 = new OutputQualityHeaderVO();
        unitHeader82.setProp("inStorageSquare");
        unitHeader82.setLabel(PmManagerConstant.MEASURE_UNIT_SM);
        childHeaders8.add(unitHeader82);
        header8.setChildren(childHeaders8);
        columns.add(header8);
        //加工产量
        OutputQualityHeaderVO header9 = new OutputQualityHeaderVO();
        header9.setLabel("加工产量");
        header9.setProp("9");
        List<OutputQualityHeaderVO> childHeaders9 = Lists.newArrayList();
        OutputQualityHeaderVO unitHeader91 = new OutputQualityHeaderVO();
        unitHeader91.setProp("processQuantitySlice");
        unitHeader91.setLabel(PmManagerConstant.MEASURE_UNIT_PIECE);
        childHeaders9.add(unitHeader91);
        OutputQualityHeaderVO unitHeader92 = new OutputQualityHeaderVO();
        unitHeader92.setProp("processQuantitySquare");
        unitHeader92.setLabel(PmManagerConstant.MEASURE_UNIT_SM);
        childHeaders9.add(unitHeader92);
        header9.setChildren(childHeaders9);
        columns.add(header9);
        //成品率
        OutputQualityHeaderVO finishedProductHeader = new OutputQualityHeaderVO();
        finishedProductHeader.setLabel("成品率");
        finishedProductHeader.setProp("finishedProduct");
        List<OutputQualityHeaderVO> childFinishedProductHeader = Lists.newArrayList();
        OutputQualityHeaderVO childFinishedProductUnitHeader = new OutputQualityHeaderVO();
        childFinishedProductUnitHeader.setProp("finishedProduct");
        childFinishedProductUnitHeader.setLabel("%");
        childFinishedProductHeader.add(childFinishedProductUnitHeader);
        finishedProductHeader.setChildren(childFinishedProductHeader);
        columns.add(finishedProductHeader);
        return columns;
    }

    /**
     * 产量质量报表月累计
     */
    private OutputQualitySjgDataVO monthToTotal(String className, LocalDate date, List<PmsReportWorkVO> reportWorkDataList, List<FurtherProduceReportDataVO> completeStorageList, Set<Integer> stationIds, Map<Long, List<PmsCompleteDeliveryInfoDTO>> reportWorkMaterialMap, Map<Long, Long> materialIdMap, Map<Long, List<SysSpareUnitConversionVO>> unitConvertMap) {
        SysDictVO pieceSysDict = FeignOptional.of(() -> remoteDictFeignApi.getByCode(PmManagerConstant.MEASURE_UNIT_PIECE, SysDictConstant.TYPE_MEASURE_UNIT), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        SysDictVO meterSysDict = FeignOptional.of(() -> remoteDictFeignApi.getByCode(PmManagerConstant.MEASURE_UNIT_SM, SysDictConstant.TYPE_MEASURE_UNIT), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        //月累计
        OutputQualitySjgDataVO outputQualitySjgDataVO = new OutputQualitySjgDataVO();
        outputQualitySjgDataVO.setType(2);
        outputQualitySjgDataVO.setProductLine("月累计");
        LocalDateTime nightStart = LocalDateUtil.getNight(date);
        //晚班结束时间
        LocalDateTime nightEnd = LocalDateUtil.getMorning(date).plusDays(1);
        //获取当月原片耗用量
        if (CollUtil.isNotEmpty(reportWorkDataList)) {
            //总计
            BigDecimal slice = BigDecimal.ZERO;
            BigDecimal square = BigDecimal.ZERO;
            //夜班
            BigDecimal nightSlice = BigDecimal.ZERO;
            BigDecimal nightSquare = BigDecimal.ZERO;
            //获取产线下的磨边工位
            List<PmsReportWorkVO> reportWorkVOList = reportWorkDataList.stream().filter(r -> stationIds.contains(r.getStationId())).collect(Collectors.toList());
            for (PmsReportWorkVO pmsReportWorkVO : reportWorkVOList) {
                //获取该报工记录的原片产量
                List<PmsCompleteDeliveryInfoDTO> pmsCompleteDeliveryInfoDTOS = reportWorkMaterialMap.get(pmsReportWorkVO);
                if (CollUtil.isNotEmpty(pmsCompleteDeliveryInfoDTOS)) {
                    //获取该报工关联的生产订单的主物料
                    Long materialId = materialIdMap.get(pmsReportWorkVO.getId());
                    List<BigDecimal> quantity = pmsCompleteDeliveryInfoDTOS.stream().filter(p -> materialId.equals(p.getMaterialId())).map(PmsCompleteDeliveryInfoDTO::getQuantity).collect(Collectors.toList());
                    BigDecimal sum = BigDecimalUtil.sum(quantity);
                    sum = BigDecimalUtil.setScale(sum, UnitLengthUtil.UNIT_PERCENT_LENGTH);
                    slice = BigDecimalUtil.add(slice, sum);
                    BigDecimal squareConvert = UnitConvertUtil.convertMaterial(unitConvertMap.get(materialId), pieceSysDict.getId(), meterSysDict.getId(), sum, UnitLengthUtil.UNIT_SQUARE_METER_LENGTH);
                    square = BigDecimalUtil.add(square, squareConvert);
                    if (pmsReportWorkVO.getCreateTime().isAfter(nightStart) && pmsReportWorkVO.getCreateTime().isBefore(nightEnd)) {
                        //获取截至当天夜班的数据
                        nightSlice = BigDecimalUtil.add(nightSlice, sum);
                        BigDecimal nightSquareConvert = UnitConvertUtil.convertMaterial(unitConvertMap.get(materialId), pieceSysDict.getId(), meterSysDict.getId(), sum, UnitLengthUtil.UNIT_SQUARE_METER_LENGTH);
                        nightSquare = BigDecimalUtil.add(nightSquare, nightSquareConvert);
                    }
                }
            }
            if (PmManagerConstant.NIGHT_SHIFT.equals(className)) {
                //晚班
                outputQualitySjgDataVO.setCurrentClassSquare(BigDecimalUtil.setScale(square, UnitLengthUtil.UNIT_SQUARE_METER_LENGTH));
                outputQualitySjgDataVO.setCurrentClassSlice(BigDecimalUtil.setScale(slice, UnitLengthUtil.UNIT_PIECE_LENGTH));
            } else {
                outputQualitySjgDataVO.setCurrentClassSquare(BigDecimalUtil.subtract(square, nightSquare, UnitLengthUtil.UNIT_SQUARE_METER_LENGTH));
                outputQualitySjgDataVO.setCurrentClassSlice(BigDecimalUtil.subtract(slice, nightSlice, UnitLengthUtil.UNIT_PIECE_LENGTH));
            }
        }
        //入库产量的月累计
        if (CollUtil.isNotEmpty(completeStorageList)) {
            //总累计
            BigDecimal slice = BigDecimal.ZERO;
            BigDecimal square = BigDecimal.ZERO;
            //夜班
            BigDecimal nightSlice = BigDecimal.ZERO;
            BigDecimal nightSquare = BigDecimal.ZERO;
            for (FurtherProduceReportDataVO f : completeStorageList) {
                Long materialId = f.getMaterialId();
                BigDecimal processQuantity = BigDecimalUtil.setScale(f.getProcessQuantity(), UnitLengthUtil.UNIT_PIECE_LENGTH);
                //单位转换
                BigDecimal squareConvert = UnitConvertUtil.convertMaterial(unitConvertMap.get(materialId), pieceSysDict.getId(), meterSysDict.getId(), processQuantity, UnitLengthUtil.UNIT_SQUARE_METER_LENGTH);
                slice = BigDecimalUtil.add(slice, processQuantity);
                square = BigDecimalUtil.add(square, squareConvert);
                if (f.getCreateTime().isAfter(nightStart) && f.getCreateTime().isBefore(nightEnd)) {
                    //获取截至当天夜班的数据
                    nightSlice = BigDecimalUtil.add(nightSlice, processQuantity);
                    BigDecimal nightSquareConvert = UnitConvertUtil.convertMaterial(unitConvertMap.get(materialId), pieceSysDict.getId(), meterSysDict.getId(), processQuantity, UnitLengthUtil.UNIT_SQUARE_METER_LENGTH);
                    nightSquare = BigDecimalUtil.add(nightSquare, nightSquareConvert);
                }
            }
            if (PmManagerConstant.NIGHT_SHIFT.equals(className)) {
                //晚班
                outputQualitySjgDataVO.setInStorageSquare(BigDecimalUtil.setScale(square, UnitLengthUtil.UNIT_SQUARE_METER_LENGTH));
                outputQualitySjgDataVO.setInStorageSlice(BigDecimalUtil.setScale(slice, UnitLengthUtil.UNIT_PIECE_LENGTH));
            } else {
                outputQualitySjgDataVO.setInStorageSquare(BigDecimalUtil.subtract(square, nightSquare, UnitLengthUtil.UNIT_SQUARE_METER_LENGTH));
                outputQualitySjgDataVO.setInStorageSlice(BigDecimalUtil.subtract(slice, nightSlice, UnitLengthUtil.UNIT_PIECE_LENGTH));
            }
        }
        //加工产量的月累计
        if (CollUtil.isNotEmpty(reportWorkDataList)) {
            //总计
            BigDecimal slice = BigDecimal.ZERO;
            BigDecimal square = BigDecimal.ZERO;
            //夜班
            BigDecimal nightSlice = BigDecimal.ZERO;
            BigDecimal nightSquare = BigDecimal.ZERO;
            //获取产线下的磨边工位
            List<PmsReportWorkVO> reportWorkVOList = reportWorkDataList.stream().filter(r -> stationIds.contains(r.getStationId())).collect(Collectors.toList());
            if (CollUtil.isNotEmpty(reportWorkVOList)) {
                for (PmsReportWorkVO pmsReportWorkVO : reportWorkVOList) {
                    Long materialId = pmsReportWorkVO.getMaterialId();
                    BigDecimal processQuantity = BigDecimalUtil.setScale(pmsReportWorkVO.getProcessQuantity(), UnitLengthUtil.UNIT_PIECE_LENGTH);
                    //单位转换
                    slice = BigDecimalUtil.add(slice, processQuantity);
                    BigDecimal squareConvert = UnitConvertUtil.convertMaterial(unitConvertMap.get(materialId), pieceSysDict.getId(), meterSysDict.getId(), processQuantity, UnitLengthUtil.UNIT_SQUARE_METER_LENGTH);
                    square = BigDecimalUtil.add(square, squareConvert);
                    if (pmsReportWorkVO.getCreateTime().isAfter(nightStart) && pmsReportWorkVO.getCreateTime().isBefore(nightEnd)) {
                        //获取截至当天夜班的数据
                        nightSlice = BigDecimalUtil.add(nightSlice, processQuantity);
                        BigDecimal nightSquareConvert = UnitConvertUtil.convertMaterial(unitConvertMap.get(materialId), pieceSysDict.getId(), meterSysDict.getId(), processQuantity, UnitLengthUtil.UNIT_SQUARE_METER_LENGTH);
                        nightSquare = BigDecimalUtil.add(nightSquare, nightSquareConvert);
                    }
                }
            }
            if (PmManagerConstant.NIGHT_SHIFT.equals(className)) {
                //晚班
                outputQualitySjgDataVO.setProcessQuantitySquare(BigDecimalUtil.setScale(square, UnitLengthUtil.UNIT_SQUARE_METER_LENGTH));
                outputQualitySjgDataVO.setProcessQuantitySlice(BigDecimalUtil.setScale(slice, UnitLengthUtil.UNIT_PIECE_LENGTH));
            } else {
                outputQualitySjgDataVO.setProcessQuantitySquare(BigDecimalUtil.subtract(square, nightSquare, UnitLengthUtil.UNIT_SQUARE_METER_LENGTH));
                outputQualitySjgDataVO.setProcessQuantitySlice(BigDecimalUtil.subtract(slice, nightSlice, UnitLengthUtil.UNIT_PIECE_LENGTH));
            }
        }
        //成品率
        BigDecimal percent = BigDecimalUtil.percent(outputQualitySjgDataVO.getInStorageSlice(), outputQualitySjgDataVO.getCurrentClassSlice());
        outputQualitySjgDataVO.setFinishedProduct(percent);
        return outputQualitySjgDataVO;
    }


    /**
     * 获取质量在线检缺陷数据
     *
     * @param date
     * @param productWorkshop
     * @return
     */
    private List<OutputQualityDetailVO> getQmsTaskDefectData(LocalDate date, List<Integer> productWorkshop) {
        OutputQualityDetailDTO outputQualityDetailDTO = new OutputQualityDetailDTO();
        outputQualityDetailDTO.setStartDate(date);
        outputQualityDetailDTO.setEndDate(date);
        outputQualityDetailDTO.setProductWorkshopId(productWorkshop);
        return FeignOptional.of(() -> remoteCheckTaskDetailFeignApi.listDefectiveData(outputQualityDetailDTO), FeignConstant.STECCN_QMS_SERVICE_NAME).get();
    }

    /**
     * 产量质量报表从表格表格
     *
     * @return
     */
    private List<OutputQualityHeaderVO> getFromTableHeader() {
        //构建从表格表头
        List<OutputQualityHeaderVO> columns1 = Lists.newArrayList();
        OutputQualityHeaderVO produceLineHeader = new OutputQualityHeaderVO();
        produceLineHeader.setLabel("产线");
        produceLineHeader.setProp("produceLineName");
        columns1.add(produceLineHeader);
        //班次
        OutputQualityHeaderVO classHeader1 = new OutputQualityHeaderVO();
        classHeader1.setLabel("班次");
        classHeader1.setProp("className");
        columns1.add(classHeader1);
        //时间
        OutputQualityHeaderVO dateHeader = new OutputQualityHeaderVO();
        dateHeader.setLabel("时间");
        dateHeader.setProp("startTime");
        columns1.add(dateHeader);
        //时长
        OutputQualityHeaderVO duration = new OutputQualityHeaderVO();
        duration.setLabel("时长");
        duration.setProp("duration");
        columns1.add(duration);
        //原因
        OutputQualityHeaderVO cause = new OutputQualityHeaderVO();
        cause.setLabel("原因");
        cause.setProp("cause");
        columns1.add(cause);
        //备注
        OutputQualityHeaderVO remark = new OutputQualityHeaderVO();
        remark.setLabel("备注");
        remark.setProp("remarks");
        columns1.add(remark);
        return columns1;
    }

    /**
     * 产量质量报表基片表头
     *
     * @return
     */
    private List<OutputQualityHeaderVO> getJpTableHeader() {
        //构建主表格表头
        List<OutputQualityHeaderVO> columns = Lists.newArrayList();
        //班次
        OutputQualityHeaderVO classHeader = new OutputQualityHeaderVO();
        classHeader.setLabel("班次");
        classHeader.setProp("classes");
        columns.add(classHeader);
        //班组
        OutputQualityHeaderVO groupHeader = new OutputQualityHeaderVO();
        groupHeader.setLabel("班组");
        groupHeader.setProp("classGroup");
        columns.add(groupHeader);
        //班长
        OutputQualityHeaderVO monitorHeader = new OutputQualityHeaderVO();
        monitorHeader.setLabel("班长");
        monitorHeader.setProp("monitor");
        columns.add(monitorHeader);
        //线别
        OutputQualityHeaderVO productionLineHeader = new OutputQualityHeaderVO();
        productionLineHeader.setLabel("线别");
        productionLineHeader.setProp("productionLine");
        columns.add(productionLineHeader);
        //厚度
        OutputQualityHeaderVO thicknessHeader = new OutputQualityHeaderVO();
        thicknessHeader.setLabel("厚度");
        thicknessHeader.setProp("thickQuantity");
        List<OutputQualityHeaderVO> childThicknessHeader = Lists.newArrayList();
        OutputQualityHeaderVO thicknessMmHeader = new OutputQualityHeaderVO();
        thicknessMmHeader.setProp("thickQuantity");
        thicknessMmHeader.setLabel("mm");
        childThicknessHeader.add(thicknessMmHeader);
        thicknessHeader.setChildren(childThicknessHeader);
        columns.add(thicknessHeader);
        //生产规格
        OutputQualityHeaderVO productionStandardHeader = new OutputQualityHeaderVO();
        productionStandardHeader.setLabel("生产规格");
        productionStandardHeader.setProp("specifications");
        List<OutputQualityHeaderVO> childProductionStandardHeader = Lists.newArrayList();
        OutputQualityHeaderVO productionStandardMmHeader = new OutputQualityHeaderVO();
        productionStandardMmHeader.setProp("specifications");
        productionStandardMmHeader.setLabel("mm");
        childProductionStandardHeader.add(productionStandardMmHeader);
        productionStandardHeader.setChildren(childProductionStandardHeader);
        columns.add(productionStandardHeader);
        //闭口泡
        OutputQualityHeaderVO shutUpHeader = new OutputQualityHeaderVO();
        shutUpHeader.setLabel(PmManagerConstant.QMS_TASK_ITEM_SHUT_UP);
        shutUpHeader.setProp(PmManagerConstant.QMS_TASK_ITEM_SHUT_UP_FIELD);
        List<OutputQualityHeaderVO> childShutUpHeader = Lists.newArrayList();
        OutputQualityHeaderVO shutUpUnitHeader = new OutputQualityHeaderVO();
        shutUpUnitHeader.setProp(PmManagerConstant.QMS_TASK_ITEM_SHUT_UP_FIELD);
        shutUpUnitHeader.setLabel("个/班");
        childShutUpHeader.add(shutUpUnitHeader);
        shutUpHeader.setChildren(childShutUpHeader);
        columns.add(shutUpHeader);
        //开口泡
        OutputQualityHeaderVO startToTalkHeader = new OutputQualityHeaderVO();
        startToTalkHeader.setLabel(PmManagerConstant.QMS_TASK_ITEM_START_TO_TALK);
        startToTalkHeader.setProp(PmManagerConstant.QMS_TASK_ITEM_START_TO_TALK_FIELD);
        List<OutputQualityHeaderVO> childStartToTalkHeader = Lists.newArrayList();
        OutputQualityHeaderVO childStartToTalkUnitHeader = new OutputQualityHeaderVO();
        childStartToTalkUnitHeader.setProp(PmManagerConstant.QMS_TASK_ITEM_START_TO_TALK_FIELD);
        childStartToTalkUnitHeader.setLabel("个/班");
        childStartToTalkHeader.add(childStartToTalkUnitHeader);
        startToTalkHeader.setChildren(childStartToTalkHeader);
        columns.add(startToTalkHeader);
        //结石掉落物
        OutputQualityHeaderVO stoneHeader = new OutputQualityHeaderVO();
        stoneHeader.setLabel(PmManagerConstant.QMS_TASK_ITEM_STONE);
        stoneHeader.setProp(PmManagerConstant.QMS_TASK_ITEM_STONE_FIELD);
        List<OutputQualityHeaderVO> childStoneHeader = Lists.newArrayList();
        OutputQualityHeaderVO childStoneUnitHeader = new OutputQualityHeaderVO();
        childStoneUnitHeader.setProp(PmManagerConstant.QMS_TASK_ITEM_STONE_FIELD);
        childStoneUnitHeader.setLabel("个/班");
        childStoneHeader.add(childStoneUnitHeader);
        stoneHeader.setChildren(childStoneHeader);
        columns.add(stoneHeader);
        //辊印变形
        OutputQualityHeaderVO variantHeader = new OutputQualityHeaderVO();
        variantHeader.setLabel(PmManagerConstant.QMS_TASK_ITEM_VARIANT);
        variantHeader.setProp(PmManagerConstant.QMS_TASK_ITEM_VARIANT_FIELD);
        List<OutputQualityHeaderVO> childVariantHeader = Lists.newArrayList();
        OutputQualityHeaderVO childVariantUnitHeader = new OutputQualityHeaderVO();
        childVariantUnitHeader.setProp(PmManagerConstant.QMS_TASK_ITEM_VARIANT_FIELD);
        childVariantUnitHeader.setLabel("个/班");
        childVariantHeader.add(childVariantUnitHeader);
        variantHeader.setChildren(childVariantHeader);
        columns.add(variantHeader);
        //总疵点
        OutputQualityHeaderVO flawHeader = new OutputQualityHeaderVO();
        flawHeader.setLabel(PmManagerConstant.QMS_TASK_ITEM_FLAW);
        flawHeader.setProp(PmManagerConstant.QMS_TASK_ITEM_FLAW_FIELD);
        List<OutputQualityHeaderVO> childFlawHeader = Lists.newArrayList();
        OutputQualityHeaderVO childFlawUnitHeader = new OutputQualityHeaderVO();
        childFlawUnitHeader.setProp(PmManagerConstant.QMS_TASK_ITEM_FLAW_FIELD);
        childFlawUnitHeader.setLabel("个/班");
        childFlawHeader.add(childFlawUnitHeader);
        flawHeader.setChildren(childFlawHeader);
        columns.add(flawHeader);
        //当班拉引量
        OutputQualityHeaderVO currentClassHeader = new OutputQualityHeaderVO();
        currentClassHeader.setLabel("当班拉引量");
        currentClassHeader.setProp("currentClass");
        List<OutputQualityHeaderVO> childCurrentClassHeader = Lists.newArrayList();
        OutputQualityHeaderVO childCurrentClassUnitHeader = new OutputQualityHeaderVO();
        childCurrentClassUnitHeader.setProp("currentClass");
        childCurrentClassUnitHeader.setLabel("吨/班");
        childCurrentClassHeader.add(childCurrentClassUnitHeader);
        currentClassHeader.setChildren(childCurrentClassHeader);
        columns.add(currentClassHeader);
        //平均厚度
        OutputQualityHeaderVO averageThicknessHeader = new OutputQualityHeaderVO();
        averageThicknessHeader.setLabel("平均厚度");
        averageThicknessHeader.setProp("averageThickness");
        List<OutputQualityHeaderVO> childAverageThicknessHeader = Lists.newArrayList();
        OutputQualityHeaderVO childAverageThicknessUnitHeader = new OutputQualityHeaderVO();
        childAverageThicknessUnitHeader.setProp("averageThickness");
        childAverageThicknessUnitHeader.setLabel("mm");
        childAverageThicknessHeader.add(childAverageThicknessUnitHeader);
        averageThicknessHeader.setChildren(childAverageThicknessHeader);
        columns.add(averageThicknessHeader);
        //产量
        OutputQualityHeaderVO outputHeader = new OutputQualityHeaderVO();
        outputHeader.setLabel("产量");
        outputHeader.setProp("output");
        List<OutputQualityHeaderVO> childOutputHeader = Lists.newArrayList();
        OutputQualityHeaderVO childOutputUnitHeader = new OutputQualityHeaderVO();
        childOutputUnitHeader.setProp("outputTon");
        childOutputUnitHeader.setLabel("吨/班");
        childOutputHeader.add(childOutputUnitHeader);
        OutputQualityHeaderVO childOutputUnitHeader2 = new OutputQualityHeaderVO();
        childOutputUnitHeader2.setProp("outputSq");
        childOutputUnitHeader2.setLabel(PmManagerConstant.MEASURE_UNIT_SM);
        childOutputHeader.add(childOutputUnitHeader2);
        outputHeader.setChildren(childOutputHeader);
        columns.add(outputHeader);
        //成品率
        OutputQualityHeaderVO finishedProductHeader = new OutputQualityHeaderVO();
        finishedProductHeader.setLabel("成品率");
        finishedProductHeader.setProp("finishedProduct");
        List<OutputQualityHeaderVO> childFinishedProductHeader = Lists.newArrayList();
        OutputQualityHeaderVO childFinishedProductUnitHeader = new OutputQualityHeaderVO();
        childFinishedProductUnitHeader.setProp("finishedProduct");
        childFinishedProductUnitHeader.setLabel("%");
        childFinishedProductHeader.add(childFinishedProductUnitHeader);
        finishedProductHeader.setChildren(childFinishedProductHeader);
        columns.add(finishedProductHeader);
        //累计拉引量
        OutputQualityHeaderVO totalPullHeader = new OutputQualityHeaderVO();
        totalPullHeader.setLabel("累计拉引量(吨)");
        totalPullHeader.setProp("totalPull");
        columns.add(totalPullHeader);
        //累计产量
        OutputQualityHeaderVO totalOutputHeader = new OutputQualityHeaderVO();
        totalOutputHeader.setLabel("累计产量(吨/㎡)");
        totalOutputHeader.setProp("totalOutput");
        columns.add(totalOutputHeader);
        //累计成品率
        OutputQualityHeaderVO totalFinishedProductHeader = new OutputQualityHeaderVO();
        totalFinishedProductHeader.setLabel("累计成品率");
        totalFinishedProductHeader.setProp("totalFinishedProduct");
        columns.add(totalFinishedProductHeader);
        return columns;
    }


    /**
     * 早晚班数据合计(深加工)
     */
    private OutputQualitySjgDataVO totalSjg(List<OutputQualitySjgDataVO> data) {
        //原片不良
        Integer rawClassBadness = 0;
        //磨边不良
        Integer edgingBadness = 0;
        //打孔不良
        Integer punchingBadness = 0;
        //丝印不良
        Integer silkScreenBadness = 0;
        //钢化不良
        Integer temperingBadness = 0;
        //不良合计
        Integer badTotal = 0;
        //原片耗用量
        BigDecimal currentClassSlice = BigDecimal.ZERO;
        BigDecimal currentClassSquare = BigDecimal.ZERO;
        //入库产量
        BigDecimal inStorageSlice = BigDecimal.ZERO;
        BigDecimal inStorageSquare = BigDecimal.ZERO;
        //加工产量
        BigDecimal processQuantitySlice = BigDecimal.ZERO;
        BigDecimal processQuantitySquare = BigDecimal.ZERO;
        for (OutputQualitySjgDataVO datum : data) {
            rawClassBadness = IntegerUtil.add(rawClassBadness, datum.getRawClassBadness());
            edgingBadness = IntegerUtil.add(edgingBadness, datum.getEdgingBadness());
            punchingBadness = IntegerUtil.add(punchingBadness, datum.getPunchingBadness());
            silkScreenBadness = IntegerUtil.add(silkScreenBadness, datum.getSilkScreenBadness());
            temperingBadness = IntegerUtil.add(temperingBadness, datum.getTemperingBadness());
            badTotal = IntegerUtil.add(badTotal, datum.getBadTotal());
            currentClassSlice = BigDecimalUtil.add(currentClassSlice, datum.getCurrentClassSlice());
            currentClassSquare = BigDecimalUtil.add(currentClassSquare, datum.getCurrentClassSquare());
            inStorageSlice = BigDecimalUtil.add(inStorageSlice, datum.getInStorageSlice());
            inStorageSquare = BigDecimalUtil.add(inStorageSquare, datum.getInStorageSlice());
            processQuantitySlice = BigDecimalUtil.add(processQuantitySlice, datum.getProcessQuantitySlice());
            processQuantitySquare = BigDecimalUtil.add(processQuantitySquare, datum.getProcessQuantitySquare());
        }
        OutputQualitySjgDataVO outputQualitySjgDataVO = new OutputQualitySjgDataVO();
        outputQualitySjgDataVO.setType(1);
        outputQualitySjgDataVO.setProductLine("合计");
        outputQualitySjgDataVO.setRawClassBadness(rawClassBadness);
        outputQualitySjgDataVO.setEdgingBadness(edgingBadness);
        outputQualitySjgDataVO.setPunchingBadness(punchingBadness);
        outputQualitySjgDataVO.setSilkScreenBadness(silkScreenBadness);
        outputQualitySjgDataVO.setTemperingBadness(temperingBadness);
        outputQualitySjgDataVO.setBadTotal(badTotal);
        outputQualitySjgDataVO.setCurrentClassSlice(BigDecimalUtil.setScale(currentClassSlice, UnitLengthUtil.UNIT_PIECE_LENGTH));
        outputQualitySjgDataVO.setCurrentClassSquare(BigDecimalUtil.setScale(currentClassSquare, UnitLengthUtil.UNIT_SQUARE_METER_LENGTH));
        outputQualitySjgDataVO.setInStorageSlice(BigDecimalUtil.setScale(inStorageSlice, UnitLengthUtil.UNIT_PIECE_LENGTH));
        outputQualitySjgDataVO.setInStorageSquare(BigDecimalUtil.setScale(inStorageSquare, UnitLengthUtil.UNIT_SQUARE_METER_LENGTH));
        outputQualitySjgDataVO.setProcessQuantitySlice(BigDecimalUtil.setScale(processQuantitySlice, UnitLengthUtil.UNIT_PIECE_LENGTH));
        outputQualitySjgDataVO.setProcessQuantitySquare(BigDecimalUtil.setScale(processQuantitySquare, UnitLengthUtil.UNIT_SQUARE_METER_LENGTH));
        outputQualitySjgDataVO.setClasses(data.get(0).getClasses());
        BigDecimal percent = BigDecimalUtil.percent(inStorageSlice, currentClassSlice);
        outputQualitySjgDataVO.setFinishedProduct(percent);
        return outputQualitySjgDataVO;
    }

    /**
     * 早晚班数据合计(基片)
     */
    private OutputQualityDataVO totalJp(List<OutputQualityDataVO> data) {

        int size = data.stream().map(OutputQualityDataVO::getProductionLine).distinct().collect(Collectors.toSet()).size();
        //避免重复值合计,一条产线只合计一次
        Map<String,Integer> productLineCount = data.stream()
                .map(OutputQualityDataVO::getProductionLine).distinct().collect(Collectors.toMap(name -> name,name -> 0));
        Integer shutUp = 0;
        Integer startToTalk = 0;
        Integer stone = 0;
        Integer variant = 0;
        BigDecimal averageThickness = BigDecimal.ZERO;
        BigDecimal currentClass = BigDecimal.ZERO;
        BigDecimal outputTon = BigDecimal.ZERO;
        BigDecimal outputSq = BigDecimal.ZERO;
        for (OutputQualityDataVO foreDatum : data) {
            //一条产线只合计一次
            if(!productLineCount.get(foreDatum.getProductionLine()).equals(QtManagerConstant.NUM_0)){
                continue;
            }
            shutUp = IntegerUtil.add(shutUp, foreDatum.getShutUp());
            startToTalk = IntegerUtil.add(startToTalk, foreDatum.getStartToTalk());
            stone = IntegerUtil.add(stone, foreDatum.getStone());
            variant = IntegerUtil.add(variant, foreDatum.getVariant());
            averageThickness = BigDecimalUtil.add(averageThickness, foreDatum.getAverageThickness());
            outputTon = BigDecimalUtil.add(outputTon, foreDatum.getOutputTon());
            outputSq = BigDecimalUtil.add(outputSq, foreDatum.getOutputSq());
            currentClass = BigDecimalUtil.add(currentClass, foreDatum.getCurrentClass());
            //累加产线
            productLineCount.computeIfPresent(foreDatum.getProductionLine(),(k,v)->v+1);
        }
        OutputQualityDataVO outputQualityDataVO = new OutputQualityDataVO();
        outputQualityDataVO.setType(1);
        outputQualityDataVO.setProductionLine("合计");
        outputQualityDataVO.setCurrentClass(BigDecimalUtil.setScale(currentClass, UnitLengthUtil.UNIT_TON_LENGTH));
        outputQualityDataVO.setOutputSq(BigDecimalUtil.setScale(outputSq, UnitLengthUtil.UNIT_SQUARE_METER_LENGTH));
        outputQualityDataVO.setOutputTon(BigDecimalUtil.setScale(outputTon, UnitLengthUtil.UNIT_TON_LENGTH));
        outputQualityDataVO.setShutUp(shutUp);
        outputQualityDataVO.setStartToTalk(startToTalk);
        outputQualityDataVO.setStone(stone);
        outputQualityDataVO.setVariant(variant);
        outputQualityDataVO.setFlaw(IntegerUtil.add(IntegerUtil.add(shutUp, startToTalk), IntegerUtil.add(stone, variant)));
        outputQualityDataVO.setAverageThickness(averageThickness.divide(new BigDecimal(size)));
        outputQualityDataVO.setFinishedProduct(BigDecimalUtil.percent(outputTon, currentClass));
        outputQualityDataVO.setClasses(data.get(0).getClasses());
        return outputQualityDataVO;
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
     * @param value1        数据1
     * @param value2        数据2
     */
    private void countIn(Long classesId, Map<Long, Map<Integer, BigDecimal>> classCountMap, BigDecimal value1, BigDecimal value2) {
        BigDecimalUtil.biAccumulate(classCountMap, -1L, 0, value1);
        BigDecimalUtil.biAccumulate(classCountMap, -1L, 1, value2);
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
    private FurtherProduceReportListVO getFurtherProduceReportListVO(List<PmProcess> pmProcessList, SysOfficeVO productLine, Map<Long, Map<Integer, BigDecimal>> classCountMap, SysDictVO sysDictVO, List<String> defaultSpecList,BigDecimal badnessQuantity,BigDecimal reduceConsumptionQuantity) {
        List<FurtherValueStrVO> classCountModuleList = Lists.newArrayList();
        BigDecimal firstTotal = null;
        for (int i = 0; i < pmProcessList.size(); i++) {
            PmProcess pmProcess = pmProcessList.get(i);
            List<String> classModuleList = Lists.newArrayList();
            classModuleList.add(BigDecimalUtil.format(classCountMap.get(pmProcess.getId()).get(0)));
            BigDecimal rate = classCountMap.get(pmProcess.getId()).get(1);
            classModuleList.add(BigDecimalUtil.format(rate));
            classModuleList.add(BigDecimalUtil.format(classCountMap.get(pmProcess.getId()).get(2)));
            classModuleList.add(BigDecimalUtil.format(classCountMap.get(pmProcess.getId()).get(3)));
            classModuleList.add(BigDecimalUtil.format(classCountMap.get(pmProcess.getId()).get(4)));
            classModuleList.add(BigDecimalUtil.format(classCountMap.get(pmProcess.getId()).get(5)));
            BigDecimal divide = BigDecimalUtil.divide(classCountMap.get(pmProcess.getId()).get(2), classCountMap.get(pmProcess.getId()).get(0));
            classModuleList.add(BigDecimalUtil.format(BigDecimalUtil.multiply(divide, BigDecimalUtil.HUNDRED)));
            classCountModuleList.add(new FurtherValueStrVO(classModuleList));
            if (i == 0) {
                firstTotal = rate;
            }
        }
        Map<Integer, BigDecimal> classCountChildMap = classCountMap.get(-2L);
        Map<Integer, BigDecimal> classInChildMap = classCountMap.get(-1L);
        List<String> classCountChildList = Lists.newArrayList();
        classCountChildList.add(BigDecimalUtil.format(classCountChildMap.get(0)));
        classCountChildList.add(BigDecimalUtil.format(classCountChildMap.get(1)));
        classCountChildList.add(BigDecimalUtil.format(classCountChildMap.get(2)));
        classCountChildList.add(BigDecimalUtil.format(classCountChildMap.get(3)));
        //合格率
        BigDecimal divide = BigDecimalUtil.divide(classCountChildMap.get(2), classCountChildMap.get(0));
        classCountChildList.add(BigDecimalUtil.format(BigDecimalUtil.multiply(divide, BigDecimalUtil.HUNDRED)));
        List<String> classInChildList = Lists.newArrayList();
        classInChildList.add(BigDecimalUtil.format(classInChildMap.get(0)));
        classInChildList.add(BigDecimalUtil.format(classInChildMap.get(1)));
        //成品率
        BigDecimal rate = BigDecimalUtil.divide(classInChildMap.get(1), firstTotal);
        BigDecimal multiply = BigDecimalUtil.multiply(rate, BigDecimalUtil.HUNDRED);
        classInChildList.add(BigDecimalUtil.format(multiply));
        //合计
        classCountModuleList.add(new FurtherValueStrVO(classCountChildList));
        //完工
        classCountModuleList.add(new FurtherValueStrVO(classInChildList));
        return createFurtherProduceReportListVO(Arrays.asList(productLine.getParentName(), productLine.getOfficeName(), sysDictVO.getName()), PmReportConstant.TYPE_1, classCountModuleList, defaultSpecList,badnessQuantity,reduceConsumptionQuantity);
    }

    /**
     * 合计列统计数据
     *
     * @param pmProcessId 合计
     * @param countMap    统计map
     * @param rowCountMap 行
     */
    private void pmColumnCount(Map<Long, Map<Integer, BigDecimal>> countMap, Long pmProcessId, List<BigDecimal> rowCountMap) {
        for (int i = 0; i <= 4; i++) {
            BigDecimalUtil.biAccumulate(countMap, pmProcessId, i, rowCountMap.get(i));
        }
    }

    /**
     * 工序行统计数据
     *
     * @param countMap    求和map
     * @param pmProcessId 工序主键
     * @param valueList   数据集合
     */
    private void pmRowCount(Map<Long, Map<Integer, BigDecimal>> countMap, Long pmProcessId, List<BigDecimal> valueList) {
        for (int i = 0; i < 7; i++) {
            BigDecimalUtil.biAccumulate(countMap, pmProcessId, i, valueList.get(i));
        }
    }

    /**
     * 工序列统计数据
     *
     * @param rowCountMap 列统计map
     * @param valueList   数据集合
     */
    private void pmColumnCount(Map<Integer, BigDecimal> rowCountMap, List<BigDecimal> valueList) {
        for (int i = 0; i < 7; i++) {
            BigDecimalUtil.accumulate(rowCountMap, i, valueList.get(i));
        }
    }


    /**
     * 创建对象
     *
     * @param nameList 名称【车间，产线，班次】
     * @param type     类型
     * @param module   工序数据
     * @return vo
     */
    private FurtherProduceReportListVO createFurtherProduceReportListVO(List<String> nameList, Integer type, List<FurtherValueStrVO> module, List<String> specList,BigDecimal badnessQuantity,BigDecimal consumptionQuantity) {
        FurtherProduceReportListVO row = new FurtherProduceReportListVO();
        row.setSpecList(specList);
        row.setBadnessQuantity(badnessQuantity);
        row.setConsumptionQuantity(BigDecimalUtil.add(badnessQuantity,consumptionQuantity));
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
            Map<String, SysDictVO> sysDictMap = FeignOptional.of(() -> remoteDictFeignApi.listByCode(sysDictDTO), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
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
        //查询产线
        List<SysOfficeVO> productLineList = FeignOptional.of(() -> remoteOfficeFeign.listByParentId(sysOfficeParentDTO), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        Assert.notEmpty(productLineList, "该车间没有绑定产线");

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

    /**
     * 获取报工信息
     *
     * @param productLineIdList
     * @param dateRangeList
     * @return
     */
    private List<PmsReportWork> getReportWorkList(Set<Integer> productLineIdList, List<LocalDateTime> dateRangeList) {
        return pmsReportWorkService.list(new QueryWrapper<PmsReportWork>().lambda().in(PmsReportWork::getProductLineId, productLineIdList).ge(PmsReportWork::getCreateTime, dateRangeList.get(0)).le(PmsReportWork::getCreateTime, dateRangeList.get(1)));
    }

    /**
     * 获取产线统计
     *
     * @param productLineList 产线集合
     * @param dateRangeList   日期集合
     * @return
     */
    private OutputCountVO productLineCount(List<SysOfficeVO> productLineList, List<LocalDateTime> dateRangeList) {

        //获取产线id和产线名称
        Set<Integer> productLineIdList = productLineList.stream().map(SysOfficeVO::getId).collect(Collectors.toSet());
        List<String> productLineNameList = productLineList.stream().map(SysOfficeVO::getOfficeName).collect(Collectors.toList());
        //获取班组
        List<SysDictVO> sysDictVOList = FeignOptional.of(() -> remoteDictFeignApi.listByType(PmManagerConstant.SYS_TEAM), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        //获取报工的信息
        List<PmsReportWork> pmsReportWorkList = getReportWorkList(productLineIdList, dateRangeList);

        OutputCountVO outputCountVO = new OutputCountVO();
        List<OutputCountListVO> outputCountListVOList = new ArrayList<>();
        //获取车间下所有的产线名称
        outputCountVO.setDx(productLineNameList);
        outputCountVO.setDataList(outputCountListVOList);

        //x轴对应的报工 key-甲乙丙班 value 【key-产线 value-报工集合】
        Map<Long, Map<Integer, Set<Long>>> xReportMap = Maps.newHashMap();
        //报工对应拉引量
        Map<Long, String> reportPfMap = Maps.newHashMap();
        pmsReportWorkList.forEach(ele -> {
            LocalDate classDate = LocalDateUtil.getClassDate(ele.getStartTime(), Boolean.FALSE);
            reportPfMap.put(ele.getId(), classDate + "," + ele.getProductLineId() + "," + ele.getClassesId());
        });
        //根据报工的信息获取拉引量
        Map<String, BigDecimal> pullForceMap = pmsPullForceService.getPullForce(Sets.newHashSet(reportPfMap.values()));

        //遍历班组，甲乙丙班
        for (SysDictVO sysDictVO : sysDictVOList) {
            //获取产量数据
            OutputCountListVO reportWork = getReportWork(sysDictVO, pmsReportWorkList, productLineList, xReportMap);
            outputCountListVOList.add(reportWork);
        }

        //获取入库数
        List<CompleteStorageCountVO> completeStorageCount = pmsProduceOrderMapper.selectCompleteStorageCount(new ArrayList<>(productLineIdList), dateRangeList.get(0), dateRangeList.get(1), PmReportConstant.DataGaugeTypeEnum.PRODUCT_LINE.getCode());
        for (SysDictVO sysDictVO : sysDictVOList) {
            OutputCountListVO listVO = new OutputCountListVO();
            List<BigDecimal> quantityList = new ArrayList<>();
            listVO.setDataType("入库数");
            listVO.setDataName(sysDictVO.getName());

            List<CompleteStorageCountVO> storageCountVOList = completeStorageCount.stream().filter(storageCountVO -> sysDictVO.getId().equals(storageCountVO.getClassGroupId())).collect(Collectors.toList());
            for (SysOfficeVO sysOfficeVO : productLineList) {
                Optional<CompleteStorageCountVO> present = storageCountVOList.stream().filter(storageCountVO -> sysOfficeVO.getId().equals(storageCountVO.getProductionLineId())).findAny();
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

            Map<Integer, Set<Long>> lineMap = xReportMap.getOrDefault(sysDictVO.getId(), Maps.newHashMap());
            for (SysOfficeVO sysOfficeVO : productLineList) {
                //获取报工id的集合
                Set<Long> reportWorkIdSet = lineMap.getOrDefault(sysOfficeVO.getId(), Sets.newHashSet());
                BigDecimal sumPullForce = BigDecimal.ZERO;
                Set<String> pfSet = Sets.newHashSet();
                //将报工的日期，产线，班次拼接成字符串，放入set集合中，目的是为了去重，避免重复计算
                for (Long rwId : reportWorkIdSet) {
                    pfSet.add(reportPfMap.get(rwId));
                }
                for (String pf : pfSet) {
                    sumPullForce = BigDecimalUtil.add(sumPullForce, pullForceMap.get(pf));
                }
                quantityList.add(sumPullForce);
            }
            listVO.setQuantityList(quantityList);
            outputCountListVOList.add(listVO);
        }
        return outputCountVO;
    }

    /**
     * 班组统计
     *
     * @param productLineList 产线集合
     * @param dateRangeList   日期集合
     * @return
     */
    private OutputCountVO classGroupCount(List<SysOfficeVO> productLineList, List<LocalDateTime> dateRangeList) {
        //获取产线id
        Set<Integer> productLineIdList = productLineList.stream().map(SysOfficeVO::getId).collect(Collectors.toSet());
        List<SysDictVO> sysDictVOList = FeignOptional.of(() -> remoteDictFeignApi.listByType(PmManagerConstant.SYS_TEAM), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        //获取班组名称
        List<String> classGroupNameList = sysDictVOList.stream().map(SysDictVO::getName).collect(Collectors.toList());

        List<PmsReportWork> pmsReportWorkList = getReportWorkList(productLineIdList, dateRangeList);

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

            List<PmsReportWork> reportWorkList = pmsReportWorkList.stream().filter(reportWork -> sysOfficeVO.getId().equals(reportWork.getProductLineId())).collect(Collectors.toList());
            for (SysDictVO sysDictVO : sysDictVOList) {
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
        List<CompleteStorageCountVO> completeStorageCount = pmsProduceOrderMapper.selectCompleteStorageCount(new ArrayList<>(productLineIdList), dateRangeList.get(0), dateRangeList.get(1), PmReportConstant.DataGaugeTypeEnum.CLASS_GROUP.getCode());

        for (SysOfficeVO sysOfficeVO : productLineList) {
            OutputCountListVO listVO = new OutputCountListVO();
            List<BigDecimal> quantityList = new ArrayList<>();
            listVO.setDataType("入库数");
            listVO.setDataName(sysOfficeVO.getOfficeName());

            List<CompleteStorageCountVO> storageCountVOList = completeStorageCount.stream().filter(storageCountVO -> sysOfficeVO.getId().equals(storageCountVO.getProductionLineId())).collect(Collectors.toList());
            for (SysDictVO sysDictVO : sysDictVOList) {
                Optional<CompleteStorageCountVO> present = storageCountVOList.stream().filter(storageCountVO -> sysDictVO.getId().equals(storageCountVO.getClassGroupId())).findAny();
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

    /**
     * 获取报工的产量信息
     *
     * @param sysDictVO         班组字典
     * @param pmsReportWorkList 报工数据集合
     * @param productLineList   产线集合
     * @param xReportMap        班组 + 产线 -> 报工id集合
     * @return
     */
    private OutputCountListVO getReportWork(SysDictVO sysDictVO, List<PmsReportWork> pmsReportWorkList, List<SysOfficeVO> productLineList, Map<Long, Map<Integer, Set<Long>>> xReportMap) {
        OutputCountListVO listVO = new OutputCountListVO();
        List<BigDecimal> quantityList = new ArrayList<>();
        listVO.setDataType("产量");
        listVO.setDataName(sysDictVO.getName());
        Map<Integer, Set<Long>> lineReportWorkMap = Maps.newHashMap();
        xReportMap.put(sysDictVO.getId(), lineReportWorkMap);
        //根据班组过滤出结果集
        List<PmsReportWork> workFilterList = pmsReportWorkList.stream().filter(reportWork -> sysDictVO.getId().equals(reportWork.getClassGroupId())).collect(Collectors.toList());
        for (SysOfficeVO sysOfficeVO : productLineList) {
            List<PmsReportWork> reportWorkList = workFilterList.stream().filter(pmsReportWork -> sysOfficeVO.getId().equals(pmsReportWork.getProductLineId())).collect(Collectors.toList());
            BigDecimal sumQuantity = BigDecimal.ZERO;
            lineReportWorkMap.put(sysOfficeVO.getId(), Sets.newHashSet());
            for (PmsReportWork pmsReportWork : reportWorkList) {
                lineReportWorkMap.get(sysOfficeVO.getId()).add(pmsReportWork.getId());
                sumQuantity = BigDecimalUtil.add(sumQuantity, pmsReportWork.getProcessQuantity());
            }
            quantityList.add(sumQuantity);
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

        Map<String, BigDecimal> pullForceMap = Maps.newHashMap();
        //存放报工对应的日期集合
        Map<Long, String> reportWorkDateMap = Maps.newHashMap();
        energyGroupList.forEach(ele -> {
            pullForceMap.put(ele.getPullForceStr(), ele.getPullForce());
            reportWorkDateMap.put(ele.getReportWorkId(), ele.getPullForceStr());
        });

        //x轴上每个点是由多少报工id渲染出来的
        Map<Integer, Set<Long>> dxReportWorkMap = Maps.newHashMap();

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
            electric.add(countClassGroupGauge(productLineList, sysDictVO.getName(), PmReportConstant.ELECTRIC, 0, classGroupList, dxReportWorkMap));
            water.add(countClassGroupGauge(productLineList, sysDictVO.getName(), PmReportConstant.WATER, 1, classGroupList, dxReportWorkMap));
            oil.add(countClassGroupGauge(productLineList, sysDictVO.getName(), PmReportConstant.OIL, 2, classGroupList, dxReportWorkMap));
            gas.add(countClassGroupGauge(productLineList, sysDictVO.getName(), PmReportConstant.GAS, 3, classGroupList, dxReportWorkMap));
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

        //x轴的数据获取，拉引量获取
        for (SysOfficeVO sysOfficeVO : productLineList) {
            dxList.add(sysOfficeVO.getOfficeName());
            Set<Long> reportWorkIdSet = dxReportWorkMap.getOrDefault(sysOfficeVO.getId(), Sets.newHashSet());
            BigDecimal pullForce = BigDecimal.ZERO;
            //从报工信息中，获取需要计算拉引量的唯一条件，去重
            Set<String> pullForceSet = Sets.newHashSet();
            for (Long reportWorkId : reportWorkIdSet) {
                pullForceSet.add(reportWorkDateMap.get(reportWorkId));
            }
            for (String ele : pullForceSet) {
                pullForce = BigDecimalUtil.add(pullForce, pullForceMap.get(ele));
            }
            pullForceList.add(pullForce);
        }

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

        Map<String, BigDecimal> pullForceMap = Maps.newHashMap();
        //存放报工对应的日期集合
        Map<Long, String> reportWorkDateMap = Maps.newHashMap();
        energyGroupList.forEach(ele -> {
            pullForceMap.put(ele.getPullForceStr(), ele.getPullForce());
            reportWorkDateMap.put(ele.getReportWorkId(), ele.getPullForceStr());
        });

        //x轴上每个点是由多少报工id渲染出来的
        Map<Long, Set<Long>> dxReportWorkMap = Maps.newHashMap();

        //遍历
        List<String> dxList = Lists.newArrayList();
        List<BigDecimal> pullForceList = Lists.newArrayList();

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
            electric.add(countProductLineGauge(sysDictVOList, productLine.getOfficeName(), PmReportConstant.ELECTRIC, 0, productGroup, dxReportWorkMap));
            water.add(countProductLineGauge(sysDictVOList, productLine.getOfficeName(), PmReportConstant.WATER, 1, productGroup, dxReportWorkMap));
            oil.add(countProductLineGauge(sysDictVOList, productLine.getOfficeName(), PmReportConstant.OIL, 2, productGroup, dxReportWorkMap));
            gas.add(countProductLineGauge(sysDictVOList, productLine.getOfficeName(), PmReportConstant.GAS, 3, productGroup, dxReportWorkMap));
        }
        seriesDataVO.setElectric(electric);
        seriesDataVO.setWater(water);
        seriesDataVO.setOil(oil);
        seriesDataVO.setGas(gas);

        //x轴的数据获取，拉引量获取
        for (SysDictVO sysDictVO : sysDictVOList) {
            dxList.add(sysDictVO.getName());
            Set<Long> reportWorkIdSet = dxReportWorkMap.getOrDefault(sysDictVO.getId(), Sets.newHashSet());
            BigDecimal pullForce = BigDecimal.ZERO;
            //从报工信息中，获取需要计算拉引量的唯一条件，去重
            Set<String> pullForceSet = Sets.newHashSet();
            for (Long reportWorkId : reportWorkIdSet) {
                pullForceSet.add(reportWorkDateMap.get(reportWorkId));
            }
            for (String ele : pullForceSet) {
                pullForce = BigDecimalUtil.add(pullForce, pullForceMap.get(ele));
            }
            pullForceList.add(pullForce);
        }
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
     *
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
     * @param dxReportWorkMap
     * @return
     */
    private EnergyCountListVO countProductLineGauge(List<SysDictVO> sysDictVOList, String productLineName, String typeName, Integer type, List<EnergyGroupListVO> productGroup, Map<Long, Set<Long>> dxReportWorkMap) {
        EnergyCountListVO vo = new EnergyCountListVO();
        vo.setName(productLineName);
        vo.setStack(typeName);
        vo.setType(PmReportConstant.STR_BAR);
        Map<Long, BigDecimal> dataMap = Maps.newHashMap();
        if (CollUtil.isNotEmpty(productGroup)) {
            //将原始根据能耗类型和班组分组
            Map<Long, List<EnergyGroupListVO>> gpDataMap = productGroup.stream().filter(v -> type.equals(v.getType())).collect(Collectors.groupingBy(EnergyGroupListVO::getClassGroupId));
            gpDataMap.forEach((g, list) -> {
                BigDecimal sumQuantity = BigDecimal.ZERO;
                dxReportWorkMap.put(g, Sets.newHashSet());
                for (EnergyGroupListVO ele : list) {
                    sumQuantity = BigDecimalUtil.add(sumQuantity, ele.getQuantity());
                    dxReportWorkMap.get(g).add(ele.getReportWorkId());
                }
                dataMap.put(g, sumQuantity);
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
    private EnergyCountListVO countClassGroupGauge(List<SysOfficeVO> productLineList, String name, String typeName, Integer type, List<EnergyGroupListVO> productGroup, Map<Integer, Set<Long>> dxReportWorkMap) {
        EnergyCountListVO vo = new EnergyCountListVO();
        vo.setName(name);
        vo.setStack(typeName);
        vo.setType(PmReportConstant.STR_BAR);
        Map<Integer, BigDecimal> dataMap = Maps.newHashMap();
        if (CollUtil.isNotEmpty(productGroup)) {
            Map<Integer, List<EnergyGroupListVO>> gpDataMap = productGroup.stream().filter(v -> type.equals(v.getType())).collect(Collectors.groupingBy(EnergyGroupListVO::getProductLineId));
            gpDataMap.forEach((g, list) -> {
                BigDecimal sumQuantity = BigDecimal.ZERO;
                dxReportWorkMap.put(g, Sets.newHashSet());
                for (EnergyGroupListVO ele : list) {
                    sumQuantity = BigDecimalUtil.add(sumQuantity, ele.getQuantity());
                    dxReportWorkMap.get(g).add(ele.getReportWorkId());
                }
                dataMap.put(g, sumQuantity);
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
        return FeignOptional.of(() -> remoteOfficeFeign.listByParentId(sysOfficeParentDTO), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
    }

}
