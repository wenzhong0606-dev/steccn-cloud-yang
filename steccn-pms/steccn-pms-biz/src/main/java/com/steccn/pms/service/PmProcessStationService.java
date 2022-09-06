package com.steccn.pms.service;

import com.steccn.pms.domain.PmProcessStation;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.pms.dto.PmProcessDTO;
import com.steccn.pms.dto.PmProcessStationDTO;

import java.util.List;

/**
* @author yequan
* @description 针对表【pm_process_station(生产工序执行工位表)】的数据库操作Service
* @createDate 2022-09-02 17:01:20
*/
public interface PmProcessStationService extends IService<PmProcessStation> {

    int deleteByProcessId(Integer id);

    Boolean saveStations(PmProcessDTO dto);
}
