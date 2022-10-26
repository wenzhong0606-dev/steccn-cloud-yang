package com.steccn.pms.mapper;

import com.steccn.pms.domain.PmCraftRoute;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.steccn.pms.dto.PmProcessStationDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
* @author yequan
* @description 针对表【pm_craft_route(生产工艺路线
)】的数据库操作Mapper
* @createDate 2022-09-02 17:01:20
* @Entity com.steccn.pms.domain.PmCraftRoute
*/
@Mapper
public interface PmCraftRouteMapper extends BaseMapper<PmCraftRoute> {

    /**
     * 根据工艺路线id获取工序执行工位信息
     * @param craftRouteId
     * @return
     */
    List<PmProcessStationDTO> stations(@Param("craftRouteId") Integer craftRouteId);
}




