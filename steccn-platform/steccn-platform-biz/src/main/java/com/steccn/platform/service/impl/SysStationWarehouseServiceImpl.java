package com.steccn.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.steccn.platform.domain.SysStationWarehouse;
import com.steccn.platform.dto.SysStationWarehouseDTO;
import com.steccn.platform.service.SysStationWarehouseService;
import com.steccn.platform.mapper.SysStationWarehouseMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * @author yequan
 * @description 针对表【sys_station_warehouse(工位配置)】的数据库操作Service实现
 * @createDate 2022-11-02 15:28:34
 */
@Service
public class SysStationWarehouseServiceImpl extends ServiceImpl<SysStationWarehouseMapper, SysStationWarehouse>
        implements SysStationWarehouseService {

    @Override
    public Page<SysStationWarehouse> pageList(SysStationWarehouseDTO dto) {
        LambdaQueryWrapper<SysStationWarehouse> queryWrapper = new LambdaQueryWrapper<SysStationWarehouse>();
        queryWrapper.eq(SysStationWarehouse::getStationId, dto.getStationId());

        return page(new Page<>(dto.getCurrent(), dto.getSize()), queryWrapper);
    }

    @Override
    public Boolean add(SysStationWarehouseDTO dto) {
        SysStationWarehouse sysStationWarehouse = new SysStationWarehouse();
        BeanUtils.copyProperties(dto,sysStationWarehouse);
        return save(sysStationWarehouse);
    }

    @Override
    public Boolean edit(SysStationWarehouseDTO dto) {
        SysStationWarehouse sysStationWarehouse = new SysStationWarehouse();
        BeanUtils.copyProperties(dto,sysStationWarehouse);
        return updateById(sysStationWarehouse);
    }
}




