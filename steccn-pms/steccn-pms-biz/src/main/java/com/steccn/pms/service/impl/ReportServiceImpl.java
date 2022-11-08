package com.steccn.pms.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.common.util.LocalDateUtil;
import com.steccn.platform.dto.SysDictDTO;
import com.steccn.platform.dto.SysOfficeParentDTO;
import com.steccn.platform.feign.RemoteDictFeignApi;
import com.steccn.platform.feign.RemoteOfficeFeign;
import com.steccn.platform.vo.SysDictVO;
import com.steccn.platform.vo.SysOfficeVO;
import com.steccn.pms.constant.PmManagerConstant;
import com.steccn.pms.constant.PmReportConstant;
import com.steccn.pms.dto.EnergyCountDTO;
import com.steccn.pms.dto.PmsDayReportDTO;
import com.steccn.pms.mapper.PmsReportWorkMapper;
import com.steccn.pms.service.PmsReportWorkConsumeService;
import com.steccn.pms.service.ReportService;
import com.steccn.pms.vo.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.math.BigDecimal;
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

    @Override
    public SubstrateDayReportVO substrateDayReport(PmsDayReportDTO dto) {
        SubstrateDayReportVO vo = new SubstrateDayReportVO();
        //构建表头
        SubstrateReportHeaderVO header = new SubstrateReportHeaderVO();
        BaseReportHeaderVO specList = new BaseReportHeaderVO("规格", Arrays.asList("长", "宽", "厚"));
        header.setSpecList(specList);
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

        workShopMap.forEach((k, v) -> {
            //根据产线遍历
            v.forEach(productLine -> {
                //获取当前产线下的数据
                List<SubstrateDataListVO> productLineDataList = rowDataList.stream().filter(row -> productLineMap.equals(row.getProductLineId())).collect(Collectors.toList());
                //将当前产线下的数据根据物料分组
                Map<Long, List<SubstrateDataListVO>> materialMap = productLineDataList.stream().collect(Collectors.groupingBy(SubstrateDataListVO::getMaterialId));
                //遍历物料分组的数据，来组装数据
                materialMap.forEach((m, n) -> {
                    SubstrateDayReportListVO row = new SubstrateDayReportListVO();
                    row.setType(PmReportConstant.TYPE_0);
                    Map<Long, List<SubstrateDataListVO>> classDataMap = n.stream().collect(Collectors.groupingBy(SubstrateDataListVO::getClassesId));
                    //遍历班次
                    classList.forEach(c -> {
                        //获取班次信息
                        List<SubstrateDataListVO> classData = classDataMap.get(c);
                        //将classData分成不同类型
                        Map<Integer,SubstrateDataListVO> typeMap = Maps.newHashMap();
                        classData.forEach(cld ->{
                            typeMap.put(cld.getType(),cld);
                        });
                    });
                });
            });

        });
        return vo;
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
