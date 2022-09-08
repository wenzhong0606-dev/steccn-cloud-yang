package com.steccn.pms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.steccn.authserver.util.SecurityUtils;
import com.steccn.pms.domain.PmCraftRoute;
import com.steccn.pms.domain.PmCraftRouteModule;
import com.steccn.pms.domain.PmProcessStation;
import com.steccn.pms.dto.PmCraftRouteDTO;
import com.steccn.pms.service.PmCraftRouteModuleService;
import com.steccn.pms.mapper.PmCraftRouteModuleMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
* @author yequan
* @description 针对表【pm_craft_route_module(工艺路线-组件)】的数据库操作Service实现
* @createDate 2022-09-02 17:01:20
*/
@Service
public class PmCraftRouteModuleServiceImpl extends ServiceImpl<PmCraftRouteModuleMapper, PmCraftRouteModule>
    implements PmCraftRouteModuleService{

    @Override
    public Boolean saveMaterial(PmCraftRouteDTO dto) {
        List<PmCraftRouteModule> craftRouteModuleList = Lists.newArrayList();
        //保存组件信息
        dto.getList().forEach(model -> {
            PmCraftRouteModule pmCraftRouteModule = new PmCraftRouteModule();
            BeanUtils.copyProperties(model, pmCraftRouteModule);
            pmCraftRouteModule.setCreateBy(SecurityUtils.getUser().getUserId());
            pmCraftRouteModule.setCreateName(SecurityUtils.getUser().getName());
            pmCraftRouteModule.setCraftRouteId(dto.getId());
            craftRouteModuleList.add(pmCraftRouteModule);
        });
        return saveBatch(craftRouteModuleList);
    }
}




