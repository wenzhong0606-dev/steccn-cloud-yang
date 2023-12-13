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
import com.steccn.dp.domain.AdsPmsCompleteStorage;
import com.steccn.dp.service.AdsPmsCompleteStorageService;
import com.steccn.dp.mapper.AdsPmsCompleteStorageMapper;
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
 * @description 针对表【ads_pms_complete_storage(各车间产线每日入库统计)】的数据库操作Service实现
 * @createDate 2023-11-29 16:09:13
 */
@Service
@RequiredArgsConstructor
public class AdsPmsCompleteStorageServiceImpl extends ServiceImpl<AdsPmsCompleteStorageMapper, AdsPmsCompleteStorage>
        implements AdsPmsCompleteStorageService {

    private final RemoteDictFeignApi remoteDictFeignApi;
    private final RemoteSpareUnitConvertFeignApi remoteSpareUnitConvertFeignApi;

    @Override
    public Map<String, BigDecimal> sjgInStorage(QtManagerConstant.QtGranularityEnum granularity, LocalDate start, LocalDate end, Set<Integer> wsIdSet, Integer bit) {
        SysDictVO pieceSysDict = FeignOptional.of(() -> remoteDictFeignApi.getByCode(PmManagerConstant.MEASURE_UNIT_PIECE, SysDictConstant.TYPE_MEASURE_UNIT), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        SysDictVO sqSysDict = FeignOptional.of(() -> remoteDictFeignApi.getByCode(PmManagerConstant.MEASURE_UNIT_SM, SysDictConstant.TYPE_MEASURE_UNIT), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        Assert.notNull(pieceSysDict, "片单位不存在");
        Assert.notNull(sqSysDict, "平方米单位不存在");
        return inStorage(granularity, start, end, wsIdSet, bit, pieceSysDict, sqSysDict);
    }

    @Override
    public Map<String, BigDecimal> jpInStorage(QtManagerConstant.QtGranularityEnum granularity, LocalDate start, LocalDate end, Set<Integer> wsIdSet, Integer bit) {
        SysDictVO pieceSysDict = FeignOptional.of(() -> remoteDictFeignApi.getByCode(PmManagerConstant.MEASURE_UNIT_PIECE, SysDictConstant.TYPE_MEASURE_UNIT), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        SysDictVO tonSysDict = FeignOptional.of(() -> remoteDictFeignApi.getByCode(PmManagerConstant.MEASURE_UNIT_TON, SysDictConstant.TYPE_MEASURE_UNIT), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        Assert.notNull(pieceSysDict, "片单位不存在");
        Assert.notNull(tonSysDict, "吨单位不存在");
        return inStorage(granularity, start, end, wsIdSet, bit, pieceSysDict, tonSysDict);
    }

    /**
     * 入库
     *
     * @param granularity 粒度
     * @param start       开始日期
     * @param end         结束日期
     * @param wsIdSet     车间id集合
     * @param bit         位数
     * @param source      源头
     * @param target      目标
     * @return
     */
    public Map<String, BigDecimal> inStorage(QtManagerConstant.QtGranularityEnum granularity, LocalDate start, LocalDate end, Set<Integer> wsIdSet, Integer bit, SysDictVO source, SysDictVO target) {
        List<TripleDTO<String, Long, BigDecimal>> inStorageList;
        if (QtManagerConstant.QtGranularityEnum.DAY.equals(granularity)) {
            inStorageList = baseMapper.inStorageMaterialDay(start, end, wsIdSet);
        } else if (QtManagerConstant.QtGranularityEnum.WEEK.equals(granularity)) {
            inStorageList = baseMapper.inStorageMaterialWeek(start, end, wsIdSet);
        } else if (QtManagerConstant.QtGranularityEnum.MONTH.equals(granularity)) {
            inStorageList = baseMapper.inStorageMaterialMonth(start, end, wsIdSet);
        } else {
            inStorageList = baseMapper.inStorageMaterialYear(start, end, wsIdSet);
        }
        return unitConvert(source, target, inStorageList, bit);
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




