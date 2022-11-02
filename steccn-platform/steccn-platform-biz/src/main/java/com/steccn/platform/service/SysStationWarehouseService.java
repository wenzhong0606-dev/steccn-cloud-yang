package com.steccn.platform.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.platform.domain.SysStationWarehouse;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.platform.dto.SysStationWarehouseDTO;

/**
* @author yequan
* @description 针对表【sys_station_warehouse(工位配置)】的数据库操作Service
* @createDate 2022-11-02 15:28:34
*/
public interface SysStationWarehouseService extends IService<SysStationWarehouse> {

    Page<SysStationWarehouse> pageList(SysStationWarehouseDTO dto);

    Boolean add(SysStationWarehouseDTO dto);

    Boolean edit(SysStationWarehouseDTO dto);


}
