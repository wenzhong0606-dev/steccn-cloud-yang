package com.steccn.pms.service;

import com.steccn.common.dto.TripleDTO;
import com.steccn.platform.vo.SysDictVO;

import java.math.BigDecimal;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author yequan
 * @description 单位转换公共接口，用于基础服务
 * @createDate 2024-01-11 15:46:08
 */
public interface PmsUnitConvertManager {

    <T> List<T> unitConvert(SysDictVO sourceSysDict,
                            SysDictVO targetSysDict,
                            List<T> dataList,
                            Function<T, Long> materialFunction,
                            Function<T, BigDecimal> valueFunction,
                            BiConsumer<T,BigDecimal> setValueConsume,
                            Integer bit);
}

