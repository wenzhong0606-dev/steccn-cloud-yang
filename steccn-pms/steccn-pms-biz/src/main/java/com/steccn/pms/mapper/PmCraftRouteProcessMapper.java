package com.steccn.pms.mapper;

import com.steccn.pms.domain.PmCraftRouteProcess;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.steccn.pms.domain.PmProcess;
import com.steccn.pms.vo.PmCraftRouteProcessVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
* @author yequan
* @description 针对表【pm_craft_route_process(生产工艺路线-工序信息)】的数据库操作Mapper
* @createDate 2022-09-02 17:01:20
* @Entity com.steccn.pms.domain.PmCraftRouteProcess
*/
@Mapper
public interface PmCraftRouteProcessMapper extends BaseMapper<PmCraftRouteProcess> {

    /**
     * 根据工艺路线id获取当前的线路下的工序信息
     * @param craftRouteId
     * @return
     */
    List<PmCraftRouteProcessVO> processList(@Param("craftRouteId") Long craftRouteId);
}




