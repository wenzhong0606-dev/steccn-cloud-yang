package com.steccn.pms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Maps;
import com.steccn.authserver.exception.BusinessException;
import com.steccn.authserver.util.SecurityUtils;
import com.steccn.pms.domain.PmProcessStation;
import com.steccn.pms.dto.PmProcessDTO;
import com.steccn.pms.dto.PmProcessStationDTO;
import com.steccn.pms.service.PmProcessStationService;
import com.steccn.pms.mapper.PmProcessStationMapper;
import com.steccn.pms.vo.PmProcessStationVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author yequan
 * @description 针对表【pm_process_station(生产工序执行工位表)】的数据库操作Service实现
 * @createDate 2022-09-02 17:01:20
 */
@Service
@RequiredArgsConstructor
public class PmProcessStationServiceImpl extends ServiceImpl<PmProcessStationMapper, PmProcessStation>
        implements PmProcessStationService {

    private final PmProcessStationMapper processStationMapper;

    @Override
    public int deleteByProcessId(Long id) {
        return processStationMapper.deleteByProcessId(id);
    }

    @Override
    public Boolean saveStations(PmProcessDTO dto) {
        List<PmProcessStation> processStationList = new ArrayList<>();
        //保存可执行工位数据
        dto.getList().forEach(station -> {
            PmProcessStation pmProcessStation = new PmProcessStation();
            BeanUtil.copyProperties(station, pmProcessStation);
            pmProcessStation.setCreateBy(SecurityUtils.getUser().getUserId());
            pmProcessStation.setCreateName(SecurityUtils.getUser().getName());
            pmProcessStation.setProcessId(dto.getId());
            processStationList.add(pmProcessStation);
        });
        return saveBatch(processStationList);
    }

    @Override
    public Map<Long, List<PmProcessStationVO>> listByProcessId(Set<Long> ids) {
        Map<Long, List<PmProcessStationVO>> map = Maps.newHashMap();
        if (CollUtil.isEmpty(ids)) {
            return map;
        }
        List<PmProcessStation> list = list(new LambdaQueryWrapper<PmProcessStation>().in(PmProcessStation::getProcessId, ids));
        if (CollUtil.isNotEmpty(list)) {
            List<PmProcessStationVO> pmProcessStationVOS = BeanUtil.copyToList(list, PmProcessStationVO.class);
            map = pmProcessStationVOS.stream().collect(Collectors.groupingBy(PmProcessStationVO::getProcessId));
        }
        return map;
    }
}




