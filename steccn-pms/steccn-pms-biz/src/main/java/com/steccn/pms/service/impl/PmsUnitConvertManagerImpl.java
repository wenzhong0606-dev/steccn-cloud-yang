package com.steccn.pms.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.google.common.collect.Maps;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.common.constant.FeignConstant;
import com.steccn.common.dto.TripleDTO;
import com.steccn.common.util.BigDecimalUtil;
import com.steccn.common.util.MapUtil;
import com.steccn.platform.feign.RemoteSpareUnitConvertFeignApi;
import com.steccn.platform.vo.SysDictVO;
import com.steccn.platform.vo.SysSpareUnitConversionVO;
import com.steccn.pms.service.PmsUnitConvertManager;
import com.steccn.pms.util.UnitConvertUtil;
import lombok.RequiredArgsConstructor;
import org.apache.commons.compress.utils.Lists;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PmsUnitConvertManagerImpl implements PmsUnitConvertManager {

    private final RemoteSpareUnitConvertFeignApi remoteSpareUnitConvertFeignApi;

    @Override
    public <T> List<T> unitConvert(SysDictVO sourceSysDict,
                                   SysDictVO targetSysDict,
                                   List<T> dataList,
                                   Function<T, Long> materialFunction,
                                   Function<T, BigDecimal> valueFunction,
                                   BiConsumer<T,BigDecimal> setValueConsume,
                                   Integer bit) {
        //1.如何获取物料
        //2.如何获取原值的方法
        //3.如何设置值的方法
        List<Object> resList = Lists.newArrayList();
        if (CollUtil.isNotEmpty(dataList)) {
            Set<Long> materialIdSet = dataList.stream().map(e -> materialFunction.apply(e)).collect(Collectors.toSet());
            Map<Long, List<SysSpareUnitConversionVO>> materialConvertMap = FeignOptional.of(() -> remoteSpareUnitConvertFeignApi.convert(materialIdSet), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            dataList.forEach(ele -> {
                List<SysSpareUnitConversionVO> convertList = materialConvertMap.get(materialFunction.apply(ele));
                BigDecimal convertQuantity = UnitConvertUtil.convertMaterial(convertList, sourceSysDict.getId(), targetSysDict.getId(), valueFunction.apply(ele), bit);
                setValueConsume.accept(ele,convertQuantity);
                resList.add(ele);
            });
        }
        return (List<T>) resList;
    }
}
