package com.steccn.platform.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.platform.domain.SysStationWarehouse;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.platform.dto.SysStationWarehouseAddDTO;
import com.steccn.platform.dto.SysStationWarehouseDTO;
import com.steccn.platform.vo.SysStationWarehouseVO;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
* @author yequan
* @description 针对表【sys_station_warehouse(工位配置)】的数据库操作Service
* @createDate 2022-11-02 15:28:34
*/
public interface SysStationWarehouseService extends IService<SysStationWarehouse> {

    /**
     * 分页查询工位配置
     * @param dto
     * @return
     */
    Page<SysStationWarehouseVO> pageList(SysStationWarehouseDTO dto);

    /**
     * 新增工位配置
     * @param dto
     * @return
     */
    Boolean add(SysStationWarehouseAddDTO dto);

    /**
     * 编辑工位配置
     *
     * @param dto
     * @return
     */
    Boolean edit(SysStationWarehouseDTO dto);


    /**
     * 批量获取工位绑定的线边仓信息
     * @param stationIds
     * @return
     */
    Map<Integer, List<SysStationWarehouseVO>> listByStationIds(Set<Integer> stationIds);


}
