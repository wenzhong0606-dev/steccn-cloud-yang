package com.steccn.pms.util;

import com.steccn.common.dto.TupleDTO;
import com.steccn.pms.constant.PmManagerConstant;

/**
 * @author yequan
 * @desc 单位转换及精度工具类
 * @date 2023/9/11
 */
public class PmsUnitUtil {

    /**
     * 根据单位类型，获取转换单位
     *
     * @param unitType 单位类型
     * @return
     */
    public static TupleDTO<String, Integer> unit(Integer unitType) {
        if (unitType == 0) {
            return new TupleDTO(PmManagerConstant.MEASURE_UNIT_PIECE, 0);
        } else if (unitType == 1) {
            return new TupleDTO(PmManagerConstant.MEASURE_UNIT_TON, 1);
        } else {
            return new TupleDTO(PmManagerConstant.MEASURE_UNIT_SM, 0);
        }
    }
}
