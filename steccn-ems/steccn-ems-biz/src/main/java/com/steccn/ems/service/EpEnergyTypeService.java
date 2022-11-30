package com.steccn.ems.service;

import com.steccn.ems.domain.EpEnergyType;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.ems.dto.EpAddEnergyTypeDTO;

/**
* @author yequan
* @description 针对表【ep_energy_type(能源类型表)】的数据库操作Service
* @createDate 2022-11-29 19:27:28
*/
public interface EpEnergyTypeService extends IService<EpEnergyType> {

    /**
     * 新增能源类型
     * @param dto
     * @return
     */
    Integer add(EpAddEnergyTypeDTO dto);
}
