package com.steccn.pms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.authserver.util.SecurityUtils;
import com.steccn.platform.feign.RemoteDictFeignApi;
import com.steccn.pms.constant.PmManagerConstant;
import com.steccn.pms.domain.PmCraftRouteModule;
import com.steccn.pms.domain.PmCraftRouteProcess;
import com.steccn.pms.domain.PmProcess;
import com.steccn.pms.dto.PmCraftRouteDTO;
import com.steccn.pms.service.PmCraftRouteProcessService;
import com.steccn.pms.mapper.PmCraftRouteProcessMapper;
import com.steccn.pms.service.PmProcessStationService;
import com.steccn.pms.vo.PmCraftRouteProcessVO;
import com.steccn.pms.vo.PmProcessStationVO;
import com.steccn.pms.vo.PmProcessVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
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
    @Autowired
    private PmProcessStationService processStationService;

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
        //1.自定义sql查询数据库
        List<PmCraftRouteProcessVO> list = this.baseMapper.processList(craftRouteId);
        Set<Long> dictIds = Sets.newHashSet();
        Set<String> codes = Sets.newHashSet();
        Set<Long> processIds = Sets.newHashSet();
        //2.处理数据
        if (CollUtil.isNotEmpty(list)) {
            //2.1处理单位数据
            list.forEach(pmCraftRouteProcess -> {
                dictIds.add(pmCraftRouteProcess.getUnitId());
                dictIds.add(pmCraftRouteProcess.getStandardUnitId());
                codes.add(pmCraftRouteProcess.getTypeCode());
                processIds.add(pmCraftRouteProcess.getProcessId());
            });
            //2.2.获取单位数据并处理
            Map<Long, String> map = FeignOptional.of(remoteDictFeignApi.listByIdCh(dictIds)).get();
            //2.3 获取类型数据
            Map<String, String> types = FeignOptional.of(remoteDictFeignApi.listByCode(codes, PmManagerConstant.PROCESS_TYPE)).get();
            //2.4 获取工位数据
            Map<Long, List<PmProcessStationVO>> longListMap = processStationService.listByProcessId(processIds);
            List<PmProcessStationVO> defaultProcess = Lists.newArrayList();
            list.forEach(pmCraftRouteProcessVO -> {
                pmCraftRouteProcessVO.setUnit(map.get(pmCraftRouteProcessVO.getUnitId()));
                pmCraftRouteProcessVO.setStandardUnit(map.get(pmCraftRouteProcessVO.getStandardUnitId()));
                pmCraftRouteProcessVO.setTypeName(types.get(pmCraftRouteProcessVO.getTypeCode()));
                List<PmProcessStationVO> orDefault = longListMap.getOrDefault(pmCraftRouteProcessVO.getProcessId(), defaultProcess);
                pmCraftRouteProcessVO.setProcessStationVOList(orDefault);
            });
        }
        return list;
    }

    @Override
    public Boolean deleteByRoteId(Long id) {
        LambdaQueryWrapper<PmCraftRouteProcess> eq = new LambdaQueryWrapper<PmCraftRouteProcess>().eq(PmCraftRouteProcess::getRouteId, id);
        return remove(eq);
    }


}




