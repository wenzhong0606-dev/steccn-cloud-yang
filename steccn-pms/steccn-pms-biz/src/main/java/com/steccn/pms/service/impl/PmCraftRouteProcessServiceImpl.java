package com.steccn.pms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.steccn.authserver.util.SecurityUtils;
import com.steccn.pms.domain.PmCraftRouteModule;
import com.steccn.pms.domain.PmCraftRouteProcess;
import com.steccn.pms.dto.PmCraftRouteDTO;
import com.steccn.pms.service.PmCraftRouteProcessService;
import com.steccn.pms.mapper.PmCraftRouteProcessMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
* @author yequan
* @description 针对表【pm_craft_route_process(生产工艺路线-工序信息)】的数据库操作Service实现
* @createDate 2022-09-02 17:01:20
*/
@Service
public class PmCraftRouteProcessServiceImpl extends ServiceImpl<PmCraftRouteProcessMapper, PmCraftRouteProcess>
    implements PmCraftRouteProcessService{

    @Override
    public Boolean saveProcess(PmCraftRouteDTO dto) {
        List<PmCraftRouteProcess> pmCraftRouteProcessList = Lists.newArrayList();
        //保存组件信息
        dto.getProcessDTOList().forEach(process -> {
            PmCraftRouteProcess pmCraftRouteProcess = new PmCraftRouteProcess();
            BeanUtil.copyProperties(process, pmCraftRouteProcess);
            pmCraftRouteProcess.setCreateBy(SecurityUtils.getUser().getUserId());
            pmCraftRouteProcess.setCreateName(SecurityUtils.getUser().getName());
            pmCraftRouteProcess.setRouteId(dto.getId());
            pmCraftRouteProcessList.add(pmCraftRouteProcess);
        });
        return saveBatch(pmCraftRouteProcessList);
    }
}




