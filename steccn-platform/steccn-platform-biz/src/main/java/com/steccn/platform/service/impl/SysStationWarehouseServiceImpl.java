package com.steccn.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.steccn.authserver.exception.BusinessException;
import com.steccn.platform.constant.SysOfficeConstant;
import com.steccn.platform.domain.SysStationWarehouse;
import com.steccn.platform.dto.SysStationWarehouseAddDTO;
import com.steccn.platform.dto.SysStationWarehouseDTO;
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
        queryWrapper.eq(SysStationWarehouse::getStationId, dto.getStationId());
        Page<SysStationWarehouse> page = page(new Page<>(dto.getCurrent(), dto.getSize()), queryWrapper);
        Page<SysStationWarehouseVO> voPage = new Page<>();
        BeanUtils.copyProperties(page,voPage);

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
            sysStationWarehouseVOList.add(sysStationWarehouseVO);
        }
        voPage.setRecords(sysStationWarehouseVOList);
        return voPage;
    }

    @Override
    public Boolean add(SysStationWarehouseAddDTO dto) {
        SysOfficeVO sysOfficeVO = sysOfficeService.parentOffice(dto.getShelfId());
        if (!SysOfficeConstant.WAREHOUSE_TYPE.equals(sysOfficeVO.getOfficeType())) {
            throw new BusinessException("货架节点错误");
        }
        if (!dto.getWarehouseId().equals(sysOfficeVO.getId())) {
            throw new BusinessException("仓库和货架不构成父子节点");
        }
        SysStationWarehouse sysStationWarehouse = new SysStationWarehouse();
        BeanUtils.copyProperties(dto, sysStationWarehouse);
        return save(sysStationWarehouse);
    }

    @Override
    public Boolean edit(SysStationWarehouseDTO dto) {
        SysStationWarehouse sysStationWarehouse = new SysStationWarehouse();
        BeanUtils.copyProperties(dto, sysStationWarehouse);
        return updateById(sysStationWarehouse);
    }
}




