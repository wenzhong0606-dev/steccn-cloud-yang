package com.steccn.pms.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.common.util.BigDecimalUtil;
import com.steccn.common.util.LocalDateUtil;
import com.steccn.platform.dto.SysDictDTO;
import com.steccn.platform.dto.SysOfficeParentDTO;
import com.steccn.platform.feign.RemoteDictFeignApi;
import com.steccn.platform.feign.RemoteOfficeFeign;
import com.steccn.platform.vo.SysDictVO;
import com.steccn.platform.vo.SysOfficeVO;
import com.steccn.pms.constant.PmManagerConstant;
import com.steccn.pms.constant.PmReportConstant;
import com.steccn.pms.domain.PmsPullForce;
import com.steccn.pms.domain.PmsReportWork;
import com.steccn.pms.dto.EnergyCountDTO;
import com.steccn.pms.dto.PmsDayReportDTO;
import com.steccn.pms.mapper.PmsReportWorkMapper;
import com.steccn.pms.mapper.PmsProduceOrderMapper;
import com.steccn.pms.service.PmsPullForceService;
import com.steccn.pms.service.PmsReportWorkConsumeService;
import com.steccn.pms.service.PmsReportWorkService;
import com.steccn.pms.service.ReportService;
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

    private final PmsReportWorkConsumeService pmsReportWorkConsumeService;

    private final PmsReportWorkMapper pmsReportWorkMapper;

    private final RemoteDictFeignApi remoteDictFeignApi;

    private final PmsPullForceService pmsPullForceService;

    private final PmsReportWorkService pmsReportWorkService;

    private final PmsProduceOrderMapper pmsProduceOrderMapper;

    @Override
    public SubstrateDayReportVO substrateDayReport(PmsDayReportDTO dto) {
        SubstrateDayReportVO vo = new SubstrateDayReportVO();
        //构建表头
        SubstrateReportHeaderVO header = new SubstrateReportHeaderVO();
        header.setSpecList(new BaseReportHeaderVO("规格", Arrays.asList("长", "宽", "厚")));
        header.setDayShift(new BaseReportHeaderVO("早班", Arrays.asList("入库(片)", "产量(片)")));
        header.setNightShift(new BaseReportHeaderVO("晚班", Arrays.asList("入库(片)", "产量(片)")));
        header.setTotal(new BaseReportHeaderVO("合计", Arrays.asList("入库(片)", "产量(片)")));

        //查询所有数据
        List<SubstrateDataListVO> rowDataList = pmsReportWorkMapper.substrateReport(dto);

        //获取班次数据
        SysDictDTO sysDictDTO = new SysDictDTO();
        sysDictDTO.setType(PmManagerConstant.SYS_SHIFT);
        Map<String, SysDictVO> classMap = FeignOptional.of(remoteDictFeignApi.listByCode(sysDictDTO)).get();
        List<SysDictVO> classList = classMap.values().stream().collect(Collectors.toList());
        classList.sort(Comparator.comparing(SysDictVO::getSort));
        //将有数据的产线拉取出来
        Set<Integer> productLineSet = rowDataList.stream().map(SubstrateDataListVO::getProductLineId).collect(Collectors.toSet());
        Map<Integer, SysOfficeVO> productLineMap = FeignOptional.of(remoteOfficeFeign.listById(productLineSet)).get();
        //根据车间去划分
        Map<Integer, List<SysOfficeVO>> workShopMap = productLineMap.values().stream().collect(Collectors.groupingBy(SysOfficeVO::getParentId));
        List<SubstrateDayReportListVO> productLineVOList = Lists.newArrayList();
        workShopMap.forEach((k, v) -> {
            //根据产线遍历
            for (SysOfficeVO productLine : v) {
                //产线数量合计-早班，晚班，合计
                Map<Integer, BigDecimal> countMap1 = Maps.newHashMap();
                Map<Integer, BigDecimal> countMap2 = Maps.newHashMap();
                Map<Integer, BigDecimal> countMap3 = Maps.newHashMap();
                //获取当前产线下的数据
                List<SubstrateDataListVO> productLineDataList = rowDataList.stream().filter(row -> productLine.getId().equals(row.getProductLineId())).collect(Collectors.toList());
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
                    //遍历班次
                    for (SysDictVO c : classList) {
                        //获取班次信息
                        List<SubstrateDataListVO> classData = classDataMap.get(c.getId());
                        //将classData分成不同类型
                        Map<Integer, SubstrateDataListVO> typeMap = Maps.newHashMap();
                        SubstrateDataListVO defaultSubstrateDataListVO = new SubstrateDataListVO();
                        classData.forEach(cld -> typeMap.put(cld.getType(), cld));
                        ProduceQuantityVO classQuantity = new ProduceQuantityVO();
                        SubstrateDataListVO typeVO0 = typeMap.getOrDefault(PmReportConstant.TYPE_0, defaultSubstrateDataListVO);
                        SubstrateDataListVO typeVO1 = typeMap.getOrDefault(PmReportConstant.TYPE_1, defaultSubstrateDataListVO);
                        typeVO0.setQuantity(Objects.nonNull(typeVO0.getQuantity()) ? typeVO0.getQuantity() : BigDecimal.ZERO);
                        typeVO1.setQuantity(Objects.nonNull(typeVO1.getQuantity()) ? typeVO1.getQuantity() : BigDecimal.ZERO);
                        inCount = BigDecimalUtil.add(inCount, typeVO0.getQuantity());
                        proCount = BigDecimalUtil.add(proCount, typeVO1.getQuantity());
                        classQuantity.setQuantityVOList(Arrays.asList(String.valueOf(typeVO0.getQuantity()), String.valueOf(typeVO1.getQuantity())));
                        if (PmReportConstant.SYS_SHIFT_0.equals(c.getCode())) {
                            //早班
                            row.setMorning(classQuantity);
                            BigDecimalUtil.accumulate(countMap1, 0, typeVO0.getQuantity());
                            BigDecimalUtil.accumulate(countMap1, 1, typeVO1.getQuantity());
                        } else {
                            //晚班
                            row.setNight(classQuantity);
                            BigDecimalUtil.accumulate(countMap2, 0, typeVO0.getQuantity());
                            BigDecimalUtil.accumulate(countMap2, 1, typeVO1.getQuantity());
                        }
                    }
                    ProduceQuantityVO columnTotalQuantity = new ProduceQuantityVO();
                    columnTotalQuantity.setQuantityVOList(Arrays.asList(String.valueOf(inCount), String.valueOf(proCount)));
                    BigDecimalUtil.accumulate(countMap3, 0, inCount);
                    BigDecimalUtil.accumulate(countMap3, 1, proCount);
                    row.setTotal(columnTotalQuantity);
                    productLineVOList.add(row);
                }
                SubstrateDayReportListVO rowTotal = new SubstrateDayReportListVO();
                rowTotal.setType(PmReportConstant.TYPE_1);
                rowTotal.setWorkshopName(productLine.getParentName());
                rowTotal.setProductLineName(productLine.getOfficeName());
                rowTotal.setMorning(new ProduceQuantityVO(Arrays.asList(String.valueOf(countMap1.get(0)), String.valueOf(countMap1.get(1)))));
                rowTotal.setNight(new ProduceQuantityVO(Arrays.asList(String.valueOf(countMap2.get(0)), String.valueOf(countMap2.get(1)))));
                rowTotal.setTotal(new ProduceQuantityVO(Arrays.asList(String.valueOf(countMap3.get(0)), String.valueOf(countMap3.get(1)))));
                rowTotal.setSpecList(Arrays.asList("合计", "合计", "合计"));
                productLineVOList.add(rowTotal);

                //插入拉引量，合格率，耗水量，耗电量，耗油量，单耗(kcal/Kg玻璃液)
                addOther(dto, classList, productLine, productLineVOList);
            }
        });
        vo.setSpecsNum(3);
        vo.setDataNum(2);
        vo.setHeader(header);
        vo.setDataList(productLineVOList);
        return vo;
    }

    /**
     * 插入拉引量，合格率，耗水量，耗电量，耗油量，单耗(kcal/Kg玻璃液)
     *
     * @param dto               查询条件
     * @param classList         班次数据
     * @param productLine       产线信息
     * @param productLineVOList 数据存放集合
     */
    private void addOther(PmsDayReportDTO dto, List<SysDictVO> classList, SysOfficeVO productLine, List<SubstrateDayReportListVO> productLineVOList) {
        //拉引量(T)
        Map<Long, PmsPullForce> pmsPullForces = pmsPullForceService.pullForceDay(productLine.getId(), dto.getDate());
        PmsPullForce morning = pmsPullForces.get(classList.get(0).getId());
        PmsPullForce night = pmsPullForces.get(classList.get(1).getId());
        BigDecimal morningBigDecimal = Objects.nonNull(morning) ? morning.getQuantity() : BigDecimal.ZERO;
        BigDecimal nightBigDecimal = Objects.nonNull(night) ? night.getQuantity() : BigDecimal.ZERO;
        SubstrateDayReportListVO lyl = rowData(productLine, Lists.newArrayList(morningBigDecimal, nightBigDecimal, morningBigDecimal.add(nightBigDecimal)), "拉引量(T)");
        productLineVOList.add(lyl);
        //0耗电量 1耗水量 2耗油量 3耗气量
        List<SubstrateDayReportListVO> energyList = pmsReportWorkConsumeService.substrateEnergy(dto, classList, productLine);

        //单耗(kcal/Kg玻璃液) （耗气量 * 7800 + 耗油量 * 9300  ） / （拉引量 * 1000)
        SubstrateDayReportListVO bly = getSubstrateDayReportListVO(productLine, morningBigDecimal, nightBigDecimal, energyList);
        productLineVOList.addAll(energyList);
        productLineVOList.add(bly);
    }

    /**
     * 单耗(kcal/Kg玻璃液) （耗气量 * 7800 + 耗油量 * 9300  ） / （拉引量 * 1000)
     *
     * @param productLine
     * @param morningBigDecimal
     * @param nightBigDecimal
     * @param energyList
     * @return
     */
    private SubstrateDayReportListVO getSubstrateDayReportListVO(SysOfficeVO productLine, BigDecimal morningBigDecimal, BigDecimal nightBigDecimal, List<SubstrateDayReportListVO> energyList) {
        BigDecimal morningGas = new BigDecimal(energyList.get(3).getMorning().getQuantityVOList().get(0));
        BigDecimal morningOil = new BigDecimal(energyList.get(2).getMorning().getQuantityVOList().get(0));
        BigDecimal morningKcal = BigDecimal.ZERO;
        BigDecimal nightGas = new BigDecimal(energyList.get(3).getNight().getQuantityVOList().get(0));
        BigDecimal nightOil = new BigDecimal(energyList.get(2).getNight().getQuantityVOList().get(0));
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
        return bly;
    }


    @Override
    public SubstrateDayReportListVO rowData(SysOfficeVO productLine, List<BigDecimal> dataList, String specStr) {
        SubstrateDayReportListVO row = new SubstrateDayReportListVO();
        row.setWorkshopName(productLine.getParentName());
        row.setProductLineName(productLine.getOfficeName());
        row.setType(PmReportConstant.TYPE_1);
        row.setSpecList(Arrays.asList(specStr, specStr, specStr));
        String dataStr1 = String.valueOf(dataList.get(0));
        String dataStr2 = String.valueOf(dataList.get(1));
        String dataStr3 = String.valueOf(dataList.get(2));
        row.setMorning(new ProduceQuantityVO(Arrays.asList(dataStr1, dataStr1)));
        row.setNight(new ProduceQuantityVO(Arrays.asList(dataStr2, dataStr2)));
        row.setTotal(new ProduceQuantityVO(Arrays.asList(dataStr3, dataStr3)));
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
        Set<Integer> productLineIdList = productLineList.stream().map(SysOfficeVO::getId).collect(Collectors.toSet());
        List<String> productLineNameList = productLineList.stream().map(SysOfficeVO::getOfficeName).collect(Collectors.toList());

        //获取日期
        List<LocalDateTime> dateRangeList = LocalDateUtil.getFirstAndLastDate(dto.getDateRangeType());

        List<PmsReportWork> pmsReportWorkList = pmsReportWorkService.list(new QueryWrapper<PmsReportWork>()
                .select("classes_id as classesId,product_line_id as productLineId,process_quantity as processQuantity")
                .lambda().in(PmsReportWork::getProductLineId, productLineIdList)
                .ge(PmsReportWork::getCreateTime, dateRangeList.get(0))
                .le(PmsReportWork::getCreateTime, dateRangeList.get(1)));
        Map<Long, List<PmsReportWork>> classGroup = pmsReportWorkList.stream().collect(Collectors.groupingBy(PmsReportWork::getClassesId));

        OutputCountVO outputCountVO = new OutputCountVO();
        //获取车间下所有的产线名称
        outputCountVO.setProductLineList(productLineNameList);

        List<OutputCountListVO> outputCountListVOList = new ArrayList<>();

        outputCountVO.setDataList(outputCountListVOList);
        //获取产量数据
        for (Map.Entry<Long, List<PmsReportWork>> entry : classGroup.entrySet()) {
            OutputCountListVO listVO = new OutputCountListVO();
            listVO.setDataType("产量");
            List<BigDecimal> quantityList = new ArrayList<>();
            //获取该班次在产线的数量
            for (SysOfficeVO sysOfficeVO : productLineList) {
                Optional<PmsReportWork> present = entry.getValue().stream().filter(pmsReportWork -> pmsReportWork.getProductLineId().equals(sysOfficeVO.getId())).findAny();
                if (present.isPresent()) {
                    quantityList.add(present.get().getProcessQuantity());
                } else {
                    quantityList.add(null);
                }
            }
            listVO.setQuantityList(quantityList);

            outputCountListVOList.add(listVO);
        }

        //获取入库数
        List<CompleteStorageCountVO> completeStorageCount = pmsProduceOrderMapper.selectCompleteStorageCount(new ArrayList<>(productLineIdList), dateRangeList.get(0), dateRangeList.get(1));
        Map<Long, List<CompleteStorageCountVO>> completeMap = completeStorageCount.stream().collect(Collectors.groupingBy(CompleteStorageCountVO::getClassesId));
        for (Map.Entry<Long, List<CompleteStorageCountVO>> entry : completeMap.entrySet()) {
            OutputCountListVO listVO = new OutputCountListVO();
            listVO.setDataType("入库数");
            List<BigDecimal> quantityList = new ArrayList<>();
            //获取该班次在产线的数量
            for (SysOfficeVO sysOfficeVO : productLineList) {
                Optional<CompleteStorageCountVO> present = entry.getValue().stream().filter(completeStorage -> completeStorage.getProductionLineId().equals(sysOfficeVO.getId())).findAny();
                if (present.isPresent()) {
                    quantityList.add(present.get().getQuantity());
                } else {
                    quantityList.add(null);
                }
            }
            listVO.setQuantityList(quantityList);

            outputCountListVOList.add(listVO);
        }

        //获取拉引量


        return outputCountVO;
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
        resVo.setDx(dxList);
        resVo.setPullForceList(pullForceList);
        return resVo;
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
    private EnergyCountListVO countProductLineGauge(List<SysDictVO> sysDictVOList, String productLineName, String typeName, Integer type, List<EnergyGroupListVO> productGroup) {
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
    private EnergyCountListVO countClassGroupGauge(List<SysOfficeVO> productLineList, String name, String typeName, Integer type, List<EnergyGroupListVO> productGroup) {
        EnergyCountListVO vo = new EnergyCountListVO();
        vo.setName(name);
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
