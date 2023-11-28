package com.steccn.wms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Sets;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.common.constant.FeignConstant;
import com.steccn.common.constant.MpConstant;
import com.steccn.common.dto.TupleDTO;
import com.steccn.common.util.BigDecimalUtil;
import com.steccn.common.util.LocalDateUtil;
import com.steccn.platform.feign.RemoteDictFeignApi;
import com.steccn.platform.feign.RemoteOfficeFeign;
import com.steccn.platform.feign.RemoteSpareFeignApi;
import com.steccn.platform.vo.EqpSpareVO;
import com.steccn.pms.vo.PanoramicTracingVO;
import com.steccn.pms.vo.WmsInventoryRecordVO;
import com.steccn.pms.vo.WmsInventoryVO;
import com.steccn.wms.constant.WmsManagerConstant;
import com.steccn.wms.domain.WmsInventory;
import com.steccn.wms.domain.WmsInventoryRecord;
import com.steccn.wms.dto.WmsInventoryRecordDTO;
import com.steccn.wms.dto.WmsInventoryRecordPageDTO;
import com.steccn.wms.dto.WmsReportWarehouseDTO;
import com.steccn.wms.mapper.WmsInventoryMapper;
import com.steccn.wms.service.WmsInventoryRecordService;
import com.steccn.wms.mapper.WmsInventoryRecordMapper;
import com.steccn.wms.service.WmsInventoryService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.compress.utils.Lists;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
* @author 34872
* @description 针对表【wms_inventory_record(库存流水记录表)】的数据库操作Service实现
* @createDate 2023-10-11 14:17:05
*/
@Service
@RequiredArgsConstructor
public class WmsInventoryRecordServiceImpl extends ServiceImpl<WmsInventoryRecordMapper, WmsInventoryRecord>
    implements WmsInventoryRecordService {

    private final RemoteSpareFeignApi remoteSpareFeignApi;
    private final RemoteDictFeignApi remoteDictFeignApi;
    private final RemoteOfficeFeign remoteOfficeFeign;
    private final WmsInventoryMapper wmsInventoryMapper;

    @Override
    public Page<WmsInventoryRecordVO> pageList(WmsInventoryRecordPageDTO dto) {
        Page<WmsInventoryRecordVO> page = new Page<>();
        LambdaQueryWrapper<WmsInventoryRecord> like = new LambdaQueryWrapper<WmsInventoryRecord>()
                .eq(Objects.nonNull(dto.getTypeId()), WmsInventoryRecord::getType, dto.getTypeId())
                .like(Objects.nonNull(dto.getMaterialName()), WmsInventoryRecord::getMaterialName, dto.getMaterialName())
                .like(Objects.nonNull(dto.getCode()), WmsInventoryRecord::getCode, dto.getCode())
                .eq(Objects.nonNull(dto.getWarehouseId()), WmsInventoryRecord::getWarehouseId, dto.getWarehouseId())
                .eq(Objects.nonNull(dto.getShelfId()), WmsInventoryRecord::getShelfId, dto.getShelfId())
                .eq(Objects.nonNull(dto.getWarehouseId()), WmsInventoryRecord::getWarehouseId, dto.getWarehouseId())
                .between(Objects.nonNull(dto.getStartTime()) && Objects.nonNull(dto.getEndTime()), WmsInventoryRecord::getCreateTime, dto.getStartTime(), dto.getEndTime());
        //自定义排序
        if (Objects.nonNull(dto.getSortField())) {
            if (WmsManagerConstant.WMS_RECORD_CODE.equals(dto.getSortField())) {
                like.orderBy(Boolean.TRUE, dto.getAscFlag(), WmsInventoryRecord::getCode);
            } else if (WmsManagerConstant.WMS_RECORD_MATERIAL_NAME.equals(dto.getSortField())) {
                like.orderBy(Boolean.TRUE, dto.getAscFlag(), WmsInventoryRecord::getMaterialName);
            } else if (WmsManagerConstant.WMS_RECORD_DATETIME.equals(dto.getSortField())) {
                like.orderBy(Boolean.TRUE, dto.getAscFlag(), WmsInventoryRecord::getCreateTime);
            }
        }
        Page<WmsInventoryRecord> resPage = page(new Page<>(dto.getCurrent(), dto.getSize()), like);
        BeanUtil.copyProperties(resPage, page);
        List<WmsInventoryRecordVO> res = BeanUtil.copyToList(resPage.getRecords(), WmsInventoryRecordVO.class);
        if (CollUtil.isNotEmpty(res)) {
            List<Long> materialIds = Lists.newArrayList();
            Set<Long> dictIds = Sets.newHashSet();
            Set<Integer> officeIds = Sets.newHashSet();
            res.forEach(wmsInventoryVO -> {
                materialIds.add(wmsInventoryVO.getMaterialId());
                dictIds.add(wmsInventoryVO.getType());
                officeIds.add(Math.toIntExact(wmsInventoryVO.getShelfId()));
                officeIds.add(Math.toIntExact(wmsInventoryVO.getWarehouseId()));
            });
            Map<Long, String> dictMap = FeignOptional.of(() -> remoteDictFeignApi.listByIdCh(dictIds), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            Map<Long, EqpSpareVO> materialMap = FeignOptional.of(() -> remoteSpareFeignApi.listById(materialIds), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            Map<Integer, String> officeMap = FeignOptional.of(() -> remoteOfficeFeign.listByIdCh(officeIds), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            res.forEach(wmsInventoryVO -> {
                wmsInventoryVO.setSpecification(materialMap.get(wmsInventoryVO.getMaterialId()).getSpecifications());
                wmsInventoryVO.setTypeName(dictMap.get(wmsInventoryVO.getType()));
                wmsInventoryVO.setShelfName(officeMap.get(Math.toIntExact(wmsInventoryVO.getShelfId())));
                wmsInventoryVO.setWarehouseName(officeMap.get(Math.toIntExact(wmsInventoryVO.getWarehouseId())));
            });
            //排序
            if (Objects.nonNull(dto.getSortField())) {
                if (WmsManagerConstant.WMS_RECORD_SPECIFICATION.equals(dto.getSortField())) {
                    res.sort(Comparator.comparing(WmsInventoryRecordVO::getSpecification, Comparator.nullsFirst(String::compareTo)));
                    if (!dto.getAscFlag()) {
                        res.sort(Comparator.comparing(WmsInventoryRecordVO::getSpecification, Comparator.nullsFirst(String::compareTo)).reversed());
                    }
                }
                if (WmsManagerConstant.WMS_RECORD_SHELF_NAME.equals(dto.getSortField())) {
                    res.sort(Comparator.comparing(WmsInventoryRecordVO::getShelfName, Comparator.nullsFirst(String::compareTo)));
                    if (!dto.getAscFlag()) {
                        res.sort(Comparator.comparing(WmsInventoryRecordVO::getShelfName, Comparator.nullsFirst(String::compareTo)).reversed());
                    }
                }
                if (WmsManagerConstant.WMS_RECORD_WAREHOUSE_NAME.equals(dto.getSortField())) {
                    res.sort(Comparator.comparing(WmsInventoryRecordVO::getWarehouseName, Comparator.nullsFirst(String::compareTo)));
                    if (!dto.getAscFlag()) {
                        res.sort(Comparator.comparing(WmsInventoryRecordVO::getWarehouseName, Comparator.nullsFirst(String::compareTo)).reversed());
                    }
                }
            }
        }
        page.setRecords(res);
        return page;
    }

    @Override
    public BigDecimal openingInventory(WmsReportWarehouseDTO dto) {
        BigDecimal quantity = null;
        if (Objects.nonNull(dto.getDate())) {
            dto.setDate(dto.getDate().minusDays(1));
        }
        List<TupleDTO<Long, BigDecimal>> resList = wmsInventoryMapper.openingInventory(dto.getWarehouseIds(), dto.getDate());
        if (CollUtil.isNotEmpty(resList)) {
            List<BigDecimal> collect = resList.stream().map(TupleDTO::getField2).collect(Collectors.toList());
            quantity = BigDecimalUtil.sum(collect);
        }
        return quantity;
    }

    @Override
    public BigDecimal endingInventory(WmsReportWarehouseDTO dto) {
        BigDecimal quantity = null;
        List<TupleDTO<Long, BigDecimal>> resList = wmsInventoryMapper.openingInventory(dto.getWarehouseIds(), dto.getDate());
        if (CollUtil.isNotEmpty(resList)) {
            List<BigDecimal> collect = resList.stream().map(TupleDTO::getField2).collect(Collectors.toList());
            quantity = BigDecimalUtil.sum(collect);
        }
        return quantity;
    }

}




