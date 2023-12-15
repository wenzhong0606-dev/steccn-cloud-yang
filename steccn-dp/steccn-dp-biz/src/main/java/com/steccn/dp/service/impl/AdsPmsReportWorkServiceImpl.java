package com.steccn.dp.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Maps;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.common.constant.FeignConstant;
import com.steccn.common.constant.SysDictConstant;
import com.steccn.common.dto.TripleDTO;
import com.steccn.common.util.BigDecimalUtil;
import com.steccn.common.util.MapUtil;
import com.steccn.dp.domain.AdsPmsReportWork;
import com.steccn.dp.service.AdsPmsReportWorkService;
import com.steccn.dp.mapper.AdsPmsReportWorkMapper;
import com.steccn.platform.feign.RemoteDictFeignApi;
import com.steccn.platform.feign.RemoteSpareUnitConvertFeignApi;
import com.steccn.platform.vo.SysDictVO;
import com.steccn.platform.vo.SysSpareUnitConversionVO;
import com.steccn.pms.constant.PmManagerConstant;
import com.steccn.pms.util.UnitConvertUtil;
import com.steccn.qms.constant.QtManagerConstant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author yequan
 * @description 针对表【ads_pms_report_work(各车间产线工序每日报工统计)】的数据库操作Service实现
 * @createDate 2023-11-29 16:09:13
 */
@Service
@RequiredArgsConstructor
public class AdsPmsReportWorkServiceImpl extends ServiceImpl<AdsPmsReportWorkMapper, AdsPmsReportWork>
        implements AdsPmsReportWorkService {

    private final RemoteDictFeignApi remoteDictFeignApi;
    private final RemoteSpareUnitConvertFeignApi remoteSpareUnitConvertFeignApi;

    @Override
    public Map<String, BigDecimal> sjgComplete(QtManagerConstant.QtGranularityEnum granularity, LocalDate start, LocalDate end, Set<Integer> wsIdSet, Integer bit) {
        List<TripleDTO<String, Long, BigDecimal>> completeList;
        if (QtManagerConstant.QtGranularityEnum.DAY.equals(granularity)) {
            completeList = baseMapper.sjgCompleteMaterialDay(start, end, wsIdSet);
        } else if (QtManagerConstant.QtGranularityEnum.WEEK.equals(granularity)) {
            completeList = baseMapper.sjgCompleteMaterialWeek(start, end, wsIdSet);
        } else if (QtManagerConstant.QtGranularityEnum.MONTH.equals(granularity)) {
            completeList = baseMapper.sjgCompleteMaterialMonth(start, end, wsIdSet);
        } else {
            completeList = baseMapper.sjgCompleteMaterialYear(start, end, wsIdSet);
        }
        //进行单位转换
        SysDictVO pieceSysDict = FeignOptional.of(() -> remoteDictFeignApi.getByCode(PmManagerConstant.MEASURE_UNIT_PIECE, SysDictConstant.TYPE_MEASURE_UNIT), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        SysDictVO sqSysDict = FeignOptional.of(() -> remoteDictFeignApi.getByCode(PmManagerConstant.MEASURE_UNIT_SM, SysDictConstant.TYPE_MEASURE_UNIT), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        Assert.notNull(pieceSysDict, "片单位不存在");
        Assert.notNull(sqSysDict, "平方米单位不存在");
        return unitConvert(pieceSysDict, sqSysDict, completeList, bit);
    }

    @Override
    public Map<String, BigDecimal> jpComplete(QtManagerConstant.QtGranularityEnum granularity, LocalDate start, LocalDate end, Set<Integer> wsIdSet, int bit) {
        List<TripleDTO<String, Long, BigDecimal>> completeList;
        if (QtManagerConstant.QtGranularityEnum.DAY.equals(granularity)) {
            completeList = baseMapper.jpCompleteMaterialDay(start, end, wsIdSet);
        } else if (QtManagerConstant.QtGranularityEnum.WEEK.equals(granularity)) {
            completeList = baseMapper.jpCompleteMaterialWeek(start, end, wsIdSet);
        } else if (QtManagerConstant.QtGranularityEnum.MONTH.equals(granularity)) {
            completeList = baseMapper.jpCompleteMaterialMonth(start, end, wsIdSet);
        } else {
            completeList = baseMapper.jpCompleteMaterialYear(start, end, wsIdSet);
        }
        //进行单位转换
        SysDictVO pieceSysDict = FeignOptional.of(() -> remoteDictFeignApi.getByCode(PmManagerConstant.MEASURE_UNIT_PIECE, SysDictConstant.TYPE_MEASURE_UNIT), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        SysDictVO sqSysDict = FeignOptional.of(() -> remoteDictFeignApi.getByCode(PmManagerConstant.MEASURE_UNIT_SM, SysDictConstant.TYPE_MEASURE_UNIT), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        Assert.notNull(pieceSysDict, "片单位不存在");
        Assert.notNull(sqSysDict, "平方米单位不存在");
        return unitConvert(pieceSysDict, sqSysDict, completeList, bit);
    }

    @Override
    public Map<String, BigDecimal> actualUse(QtManagerConstant.QtGranularityEnum granularity, LocalDate start, LocalDate end, Set<Integer> wsIdSet, int bit) {
        List<TripleDTO<String, Long, BigDecimal>> actualList;
        if (QtManagerConstant.QtGranularityEnum.DAY.equals(granularity)) {
            actualList = baseMapper.actualUseMaterialDay(start, end, wsIdSet);
        } else if (QtManagerConstant.QtGranularityEnum.WEEK.equals(granularity)) {
            actualList = baseMapper.actualUseMaterialWeek(start, end, wsIdSet);
        } else if (QtManagerConstant.QtGranularityEnum.MONTH.equals(granularity)) {
            actualList = baseMapper.actualUseMaterialMonth(start, end, wsIdSet);
        } else {
            actualList = baseMapper.actualUseMaterialYear(start, end, wsIdSet);
        }
        //进行单位转换
        SysDictVO pieceSysDict = FeignOptional.of(() -> remoteDictFeignApi.getByCode(PmManagerConstant.MEASURE_UNIT_PIECE, SysDictConstant.TYPE_MEASURE_UNIT), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        SysDictVO sqSysDict = FeignOptional.of(() -> remoteDictFeignApi.getByCode(PmManagerConstant.MEASURE_UNIT_SM, SysDictConstant.TYPE_MEASURE_UNIT), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        Assert.notNull(pieceSysDict, "片单位不存在");
        Assert.notNull(sqSysDict, "平方米单位不存在");
        return unitConvert(pieceSysDict, sqSysDict, actualList, bit);
    }

    /**
     * 单位转换
     *
     * @param sourceSysDict 原始单位
     * @param targetSysDict 目标单位
     * @param dataList      数据集合
     * @return map
     */
    private Map<String, BigDecimal> unitConvert(SysDictVO sourceSysDict, SysDictVO targetSysDict, List<TripleDTO<String, Long, BigDecimal>> dataList, Integer bit) {
        Map<String, BigDecimal> resMap = Maps.newHashMap();
        if (CollUtil.isNotEmpty(dataList)) {
            Set<Long> materialIdSet = dataList.stream().map(TripleDTO::getField2).collect(Collectors.toSet());
            Map<Long, List<SysSpareUnitConversionVO>> materialConvertMap = FeignOptional.of(() -> remoteSpareUnitConvertFeignApi.convert(materialIdSet), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            dataList.forEach(ele -> {
                List<SysSpareUnitConversionVO> convertList = materialConvertMap.get(ele.getField2());
                BigDecimal convertQuantity = UnitConvertUtil.convertMaterial(convertList, sourceSysDict.getId(), targetSysDict.getId(), ele.getField3(), bit);
                MapUtil.add(resMap, String.valueOf(ele.getField1()), convertQuantity, (x, y) -> BigDecimalUtil.add(x, y, bit));
            });
        }
        return resMap;
    }
}




