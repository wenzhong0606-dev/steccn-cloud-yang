package com.steccn.pms.mapper;

import com.steccn.pms.domain.PmCraftRouteModule;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @author yequan
 * @description 针对表【pm_craft_route_module(工艺路线-组件)】的数据库操作Mapper
 * @createDate 2022-09-02 17:01:20
 * @Entity com.steccn.pms.domain.PmCraftRouteModule
 */
@Mapper
public interface PmCraftRouteModuleMapper extends BaseMapper<PmCraftRouteModule> {

    @Delete("delete from pm_craft_route_module where craft_route_id = #{id}")
    void deleteByRoteModuleId(@Param("id") Long id);
}




