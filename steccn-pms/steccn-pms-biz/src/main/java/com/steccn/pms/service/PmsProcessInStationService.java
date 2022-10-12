package com.steccn.pms.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.pms.domain.PmsProcessInStation;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.pms.dto.PmsProcessInStationDTO;
import com.steccn.pms.vo.PmsProcessInStationVO;

/**
* @author yequan
* @description 针对表【pms_process_in_station(进站信息)】的数据库操作Service
* @createDate 2022-09-28 15:18:51
*/
public interface PmsProcessInStationService extends IService<PmsProcessInStation> {

    /**
     * 工艺信息 -进站信息列表
     * @param dto
     * @return
     */
    Page<PmsProcessInStationVO> inPage(PmsProcessInStationDTO dto);
}
