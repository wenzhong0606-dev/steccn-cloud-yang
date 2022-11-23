package com.steccn.pms.service;

import com.steccn.pms.domain.PmCraftRouteModule;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.pms.dto.PmCraftRouteDTO;

/**
* @author yequan
* @description 针对表【pm_craft_route_module(工艺路线-组件)】的数据库操作Service
* @createDate 2022-09-02 17:01:20
*/
public interface PmCraftRouteModuleService extends IService<PmCraftRouteModule> {

    /**
     * 保存子组件
     * @param dto
     * @return
     */
    Boolean saveMaterial(PmCraftRouteDTO dto);


    /**
     * 根据工艺路线id删除数据
     * @param id
     * @return
     */
    Boolean deleteByRoteId(Long id);
}
