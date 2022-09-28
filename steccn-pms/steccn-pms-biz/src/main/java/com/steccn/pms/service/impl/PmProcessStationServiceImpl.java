package com.steccn.pms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.steccn.authserver.exception.BusinessException;
import com.steccn.authserver.util.SecurityUtils;
import com.steccn.pms.domain.PmProcessStation;
import com.steccn.pms.dto.PmProcessDTO;
import com.steccn.pms.dto.PmProcessStationDTO;
import com.steccn.pms.service.PmProcessStationService;
import com.steccn.pms.mapper.PmProcessStationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

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
}




