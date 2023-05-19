package com.steccn.platform.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.common.constant.ResultCode;
import com.steccn.platform.domain.SysOfficeCollect;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.platform.dto.SysOfficeCollectAddDTO;
import com.steccn.platform.dto.SysOfficeCollectPageDTO;
import com.steccn.platform.dto.SysOfficeCollectUpdateDTO;
import com.steccn.platform.dto.SysStationWarehouseDTO;
import com.steccn.platform.vo.SysOfficeCollectVO;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author yequan
 * @description 针对表【sys_office_collect(工位配置)】的数据库操作Service
 * @createDate 2022-12-16 10:34:00
 */
public interface SysOfficeCollectService extends IService<SysOfficeCollect> {

    /**
     * 采集点编码配置分页列表
     * @param dto
     * @return
     */
    Page pageList(SysOfficeCollectPageDTO dto);

    /**
     * 新增采集点编码配置
     * @param dto
     * @return
     */
    Boolean add(SysOfficeCollectAddDTO dto);

    /**
     * 编辑采集点编码配置
     * @param dto
     * @return
     */
    Boolean edit(SysOfficeCollectUpdateDTO dto);

    /**
     * 查询工位的进出站的采集点编码配置
     *
     * @param type 进站0 出站1
     * @return
     */
    List<SysOfficeCollectVO> listOnlyStation(Integer type);

    /**
     * 获取工位的出站采集点
     * @param stationId 工位id
     * @return 出站采集点
     */
    SysOfficeCollectVO outCollect(Integer stationId);


    /**
     * 查询工位的进出站采集点
     * @param stationIdSet
     * @param type
     * @return
     */
    Map<Integer,List<String>> listByStation(Set<Integer> stationIdSet, Integer type);
}
