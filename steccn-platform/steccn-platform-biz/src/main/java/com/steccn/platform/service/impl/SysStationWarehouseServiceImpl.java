package com.steccn.platform.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.alibaba.nacos.common.utils.CollectionUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.steccn.authserver.exception.BusinessException;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.platform.constant.SysOfficeConstant;
import com.steccn.platform.domain.SysStationWarehouse;
import com.steccn.platform.dto.SysStationWarehouseAddDTO;
import com.steccn.platform.dto.SysStationWarehouseDTO;
import com.steccn.platform.feign.RemoteOfficeFeign;
import com.steccn.platform.service.SysOfficeService;
import com.steccn.platform.service.SysStationWarehouseService;
import com.steccn.platform.mapper.SysStationWarehouseMapper;
import com.steccn.platform.vo.SysOfficeVO;
import com.steccn.platform.vo.SysStationWarehouseVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author yequan
 * @description 针对表【sys_station_warehouse(工位配置)】的数据库操作Service实现
 * @createDate 2022-11-02 15:28:34
 */
@RequiredArgsConstructor
@Service
public class SysStationWarehouseServiceImpl extends ServiceImpl<SysStationWarehouseMapper, SysStationWarehouse>
        implements SysStationWarehouseService {

    private final SysOfficeService sysOfficeService;

    @Override
    public Page<SysStationWarehouseVO> pageList(SysStationWarehouseDTO dto) {


        LambdaQueryWrapper<SysStationWarehouse> queryWrapper = new LambdaQueryWrapper<SysStationWarehouse>();
        queryWrapper.eq(SysStationWarehouse::getStationId, dto.getOrganId())
//                .eq(SysStationWarehouse::getOrganType, dto.getOrganType())
                .orderByDesc(SysStationWarehouse::getCreateTime);
        Page<SysStationWarehouse> page = page(new Page<>(dto.getCurrent(), dto.getSize()), queryWrapper);
        Page<SysStationWarehouseVO> voPage = new Page<>();
        BeanUtils.copyProperties(page, voPage);
        if (CollectionUtils.isEmpty(page.getRecords())) {
            return voPage;
        }

        Set<Integer> warehouseIdSet = page.getRecords().stream().map(SysStationWarehouse::getWarehouseId).collect(Collectors.toSet());
        Set<Integer> shelfIdSet = page.getRecords().stream().map(SysStationWarehouse::getShelfId).collect(Collectors.toSet());

        Map<Integer, SysOfficeVO> warehouseMap = sysOfficeService.listById(warehouseIdSet);
        Map<Integer, SysOfficeVO> shelfIdMap = sysOfficeService.listById(shelfIdSet);

        List<SysStationWarehouseVO> sysStationWarehouseVOList = new ArrayList<>();

        for (SysStationWarehouse sysStationWarehouse : page.getRecords()) {
            SysOfficeVO warehouse = warehouseMap.get(sysStationWarehouse.getWarehouseId());
            SysOfficeVO shelf = shelfIdMap.get(sysStationWarehouse.getShelfId());

            SysStationWarehouseVO sysStationWarehouseVO = new SysStationWarehouseVO();
            sysStationWarehouseVO.setId(sysStationWarehouse.getId());
            sysStationWarehouseVO.setWarehouseName(warehouse.getOfficeName());
            sysStationWarehouseVO.setShelfName(shelf.getOfficeName());
            sysStationWarehouseVO.setNote(sysStationWarehouse.getNote());
            sysStationWarehouseVO.setShelfId(sysStationWarehouse.getShelfId());
            sysStationWarehouseVO.setWarehouseId(sysStationWarehouse.getWarehouseId());
            sysStationWarehouseVOList.add(sysStationWarehouseVO);
        }
        voPage.setRecords(sysStationWarehouseVOList);
        return voPage;
    }

    @Override
    public Boolean add(SysStationWarehouseAddDTO dto) {
        List<SysStationWarehouse> list = list(new LambdaQueryWrapper<SysStationWarehouse>().eq(SysStationWarehouse::getStationId, dto.getOrganId())
                .eq(SysStationWarehouse::getWarehouseId, dto.getWarehouseId())
                .eq(SysStationWarehouse::getShelfId, dto.getShelfId())
                .eq(SysStationWarehouse::getOrganType, dto.getOrganType()));
        if (CollectionUtils.isNotEmpty(list)) {
            throw new BusinessException("当前节点下存在相同的仓库货架信息");
        }
        SysOfficeVO sysOfficeVO = sysOfficeService.parentOffice(dto.getShelfId());
        if (!SysOfficeConstant.WAREHOUSE_TYPE.equals(sysOfficeVO.getOfficeType())) {
            throw new BusinessException("货架节点错误");
        }
        if (!dto.getWarehouseId().equals(sysOfficeVO.getId())) {
            throw new BusinessException("仓库和货架不构成父子节点");
        }
        SysStationWarehouse sysStationWarehouse = new SysStationWarehouse();
        BeanUtils.copyProperties(dto, sysStationWarehouse);
        sysStationWarehouse.setStationId(dto.getOrganId());
        return save(sysStationWarehouse);
    }

    @Override
    public Boolean edit(SysStationWarehouseDTO dto) {
        SysStationWarehouse sysStationWarehouse = new SysStationWarehouse();
        BeanUtils.copyProperties(dto, sysStationWarehouse);
        return updateById(sysStationWarehouse);
    }

    @Override
    public Map<Integer, List<SysStationWarehouseVO>> listByStationIds(Set<Integer> stationIds) {
        Map<Integer, List<SysStationWarehouseVO>> resMap = Maps.newHashMap();
        if (CollUtil.isNotEmpty(stationIds)) {
            List<SysStationWarehouse> list = list(new LambdaQueryWrapper<SysStationWarehouse>().in(SysStationWarehouse::getStationId, stationIds));
            if (CollUtil.isNotEmpty(list)) {
                List<SysStationWarehouseVO> dtoList = BeanUtil.copyToList(list, SysStationWarehouseVO.class);
                Set<Integer> officeId = Sets.newHashSet();
                dtoList.forEach(sysStationWarehouseVO -> {
                    officeId.add(sysStationWarehouseVO.getShelfId());
                    officeId.add(sysStationWarehouseVO.getWarehouseId());
                });
                resMap = dtoList.stream().collect(Collectors.groupingBy(SysStationWarehouseVO::getStationId));
                //处理数据，获取编码
                Map<Integer, SysOfficeVO> officeVOMap = sysOfficeService.listById(officeId);
                SysOfficeVO sysOfficeVO = new SysOfficeVO();
                resMap.forEach((key, value) -> {
                    value.forEach(
                            sysStationWarehouseVO -> {
                                SysOfficeVO orDefault = officeVOMap.getOrDefault(sysStationWarehouseVO.getShelfId(), sysOfficeVO);
                                sysStationWarehouseVO.setShelfCode(orDefault.getOfficeCode());
                                sysStationWarehouseVO.setShelfName(orDefault.getOfficeName());
                                SysOfficeVO warehouse = officeVOMap.getOrDefault(sysStationWarehouseVO.getWarehouseId(), sysOfficeVO);
                                sysStationWarehouseVO.setWarehouseName(warehouse.getOfficeName());
                            }
                    );
                });
            }
        }
        return resMap;
    }
}




