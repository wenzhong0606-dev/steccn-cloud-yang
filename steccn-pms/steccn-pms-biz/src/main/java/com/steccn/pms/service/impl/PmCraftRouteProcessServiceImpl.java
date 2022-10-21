package com.steccn.pms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.authserver.util.SecurityUtils;
import com.steccn.platform.feign.RemoteDictFeignApi;
import com.steccn.pms.domain.PmCraftRouteModule;
import com.steccn.pms.domain.PmCraftRouteProcess;
import com.steccn.pms.dto.PmCraftRouteDTO;
import com.steccn.pms.service.PmCraftRouteProcessService;
import com.steccn.pms.mapper.PmCraftRouteProcessMapper;
import com.steccn.pms.vo.PmCraftRouteProcessVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author yequan
 * @description 针对表【pm_craft_route_process(生产工艺路线-工序信息)】的数据库操作Service实现
 * @createDate 2022-09-02 17:01:20
 */
@Service
public class PmCraftRouteProcessServiceImpl extends ServiceImpl<PmCraftRouteProcessMapper, PmCraftRouteProcess>
        implements PmCraftRouteProcessService {

    @Autowired
    private RemoteDictFeignApi remoteDictFeignApi;

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

    @Override
    public List<PmCraftRouteProcessVO> list(Long craftRouteId) {
        //1.定义查询条件获取数据库
        List<PmCraftRouteProcess> list = list(new LambdaQueryWrapper<PmCraftRouteProcess>().eq(PmCraftRouteProcess::getRouteId, craftRouteId));
        //2.处理数据
        List<PmCraftRouteProcessVO> pmCraftRouteProcessList = Lists.newArrayList();
        Set<Long> dictIds = Sets.newHashSet();
        if (CollUtil.isNotEmpty(list)) {
            //2.1处理单位数据
            list.forEach(pmCraftRouteProcess-> {
                PmCraftRouteProcessVO pmCraftRouteProcessVO = new PmCraftRouteProcessVO();
                BeanUtil.copyProperties(pmCraftRouteProcess,pmCraftRouteProcessVO);
                dictIds.add(pmCraftRouteProcess.getUnitId());
                dictIds.add(pmCraftRouteProcess.getStandardUnitId());
                pmCraftRouteProcessList.add(pmCraftRouteProcessVO);
            });
        }
        //3.获取单位数据并处理
        Map<Long, String> map = FeignOptional.of(remoteDictFeignApi.listByIdCh(dictIds)).get();
         if (CollUtil.isNotEmpty(pmCraftRouteProcessList)){
             pmCraftRouteProcessList.forEach( pmCraftRouteProcessVO -> {
                 pmCraftRouteProcessVO.setUnit(map.get(pmCraftRouteProcessVO.getUnitId()));
                 pmCraftRouteProcessVO.setStandardUnit(map.get(pmCraftRouteProcessVO.getStandardUnitId()));
             });
         }
        return pmCraftRouteProcessList;
    }

    @Override
    public Boolean deleteByRoteId(Long id) {
        LambdaQueryWrapper<PmCraftRouteProcess> eq = new LambdaQueryWrapper<PmCraftRouteProcess>().eq(PmCraftRouteProcess::getRouteId, id);
        return remove(eq);
    }
}




