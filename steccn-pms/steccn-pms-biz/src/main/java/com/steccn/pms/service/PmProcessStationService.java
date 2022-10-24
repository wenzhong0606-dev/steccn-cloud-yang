package com.steccn.pms.service;

import com.google.common.collect.Sets;
import com.steccn.pms.domain.PmProcessStation;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.pms.dto.PmProcessDTO;
import com.steccn.pms.dto.PmProcessStationDTO;
import com.steccn.pms.vo.PmProcessStationVO;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
* @author yequan
* @description 针对表【pm_process_station(生产工序执行工位表)】的数据库操作Service
* @createDate 2022-09-02 17:01:20
*/
public interface PmProcessStationService extends IService<PmProcessStation> {

    /**
     * 根据工序id删除工位信息
     * @param id
     * @return
     */
    int deleteByProcessId(Long id);

    /**
     * 保存工位信息
     * @param dto
     * @return
     */
    Boolean saveStations(PmProcessDTO dto);

    /**
     * 根据工序id 获取工序信息
     * @param ids
     * @return
     */
    Map<Long,List<PmProcessStationVO>> listByProcessId(Set<Long> ids);
}
