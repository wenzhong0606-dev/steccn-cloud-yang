package com.steccn.pms.util;

import cn.hutool.core.collection.CollUtil;
import com.google.common.collect.Lists;
import com.steccn.common.util.BigDecimalUtil;
import com.steccn.platform.vo.SysSpareUnitConversionVO;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * @author yequan
 * @desc 单位转换
 * @date 2023/7/26
 */
public class UnitConvertUtil {

    /**
     * 单位转换map
     *
     * @param convertMap   单位转换
     * @param materialId   物料主键
     * @param sourceUnitId 源单位主键
     * @param targetUnitId 目标单位主键
     * @param quantity     数量
     * @return
     */
    public static BigDecimal convert(Map<Long, List<SysSpareUnitConversionVO>> convertMap, Long materialId, Long sourceUnitId, Long targetUnitId, BigDecimal quantity) {
        if (CollUtil.isNotEmpty(convertMap) && Objects.nonNull(materialId) && Objects.nonNull(sourceUnitId) && Objects.nonNull(targetUnitId) && Objects.nonNull(quantity) && !BigDecimalUtil.equals(quantity, BigDecimal.ZERO)) {
            List<SysSpareUnitConversionVO> convertList = convertMap.getOrDefault(materialId, Lists.newArrayList());
            Optional<SysSpareUnitConversionVO> sourceOp = convertList.stream().filter(ele -> sourceUnitId.equals(ele.getUnitId()) && targetUnitId.equals(ele.getConversionUnitId())).findFirst();
            if (sourceOp.isPresent()) {
                SysSpareUnitConversionVO source = sourceOp.get();
                BigDecimal res = BigDecimalUtil.divide(source.getQuantity(), source.getConversionQuantity(), BigDecimalUtil.SCALE_4);
                return res.multiply(quantity);
            }
        }
        return null;
    }
}
