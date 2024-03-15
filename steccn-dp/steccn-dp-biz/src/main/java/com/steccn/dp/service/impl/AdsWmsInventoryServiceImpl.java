package com.steccn.dp.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.steccn.common.dto.TupleDTO;
import com.steccn.common.util.BigDecimalUtil;
import com.steccn.dp.domain.AdsWmsInventory;
import com.steccn.dp.dto.DpWmsDataDTO;
import com.steccn.dp.mapper.AdsPmsReportWorkMapper;
import com.steccn.dp.service.AdsWmsInventoryService;
import com.steccn.dp.mapper.AdsWmsInventoryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

/**
* @author 34872
* @description 针对表【ads_wms_inventory(仓储期末库存统计)】的数据库操作Service实现
* @createDate 2024-03-15 10:43:26
*/
@Service
@RequiredArgsConstructor
public class AdsWmsInventoryServiceImpl extends ServiceImpl<AdsWmsInventoryMapper, AdsWmsInventory>
    implements AdsWmsInventoryService{

    @Override
    public TupleDTO<BigDecimal, BigDecimal> ending(DpWmsDataDTO dto) {
        TupleDTO<BigDecimal, BigDecimal> tupleDTO = new TupleDTO<>();
        if (Objects.nonNull(dto.getDate())) {
            dto.setDate(dto.getDate().minusDays(1));
        }
        List<AdsWmsInventory> list = list(new LambdaQueryWrapper<AdsWmsInventory>()
                .eq(AdsWmsInventory::getDt, dto.getDate())
                .in(AdsWmsInventory::getWarehouseId, dto.getWarehouseIds()));
        if (CollUtil.isNotEmpty(list)){
            tupleDTO.setField1(list.stream().map(AdsWmsInventory::getTotalQuantity1).reduce(BigDecimal.ZERO,BigDecimalUtil::add));
            tupleDTO.setField2(list.stream().map(AdsWmsInventory::getTotalQuantity2).reduce(BigDecimal.ZERO,BigDecimalUtil::add));
        }
        return tupleDTO;
    }

    @Override
    public TupleDTO<BigDecimal, BigDecimal> openning(DpWmsDataDTO dto) {
        TupleDTO<BigDecimal, BigDecimal> tupleDTO = new TupleDTO<>();
        List<AdsWmsInventory> list = list(new LambdaQueryWrapper<AdsWmsInventory>()
                .eq(AdsWmsInventory::getDt, dto.getDate())
                .in(AdsWmsInventory::getWarehouseId, dto.getWarehouseIds()));
        if (CollUtil.isNotEmpty(list)){
            tupleDTO.setField1(list.stream().map(AdsWmsInventory::getTotalQuantity1).reduce(BigDecimal.ZERO,BigDecimalUtil::add));
            tupleDTO.setField2(list.stream().map(AdsWmsInventory::getTotalQuantity2).reduce(BigDecimal.ZERO,BigDecimalUtil::add));
        }
        return tupleDTO;
    }
}




