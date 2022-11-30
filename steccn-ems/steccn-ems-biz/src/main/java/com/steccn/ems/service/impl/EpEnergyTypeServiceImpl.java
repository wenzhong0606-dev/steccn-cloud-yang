package com.steccn.ems.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.steccn.authserver.exception.BusinessException;
import com.steccn.ems.domain.EpEnergyType;
import com.steccn.ems.dto.EpAddEnergyTypeDTO;
import com.steccn.ems.service.EpEnergyTypeService;
import com.steccn.ems.mapper.EpEnergyTypeMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
* @author yequan
* @description 针对表【ep_energy_type(能源类型表)】的数据库操作Service实现
* @createDate 2022-11-29 19:27:28
*/
@Service
public class EpEnergyTypeServiceImpl extends ServiceImpl<EpEnergyTypeMapper, EpEnergyType>
    implements EpEnergyTypeService{

    /**
     * 新增能源类型
     * @param dto
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer add(EpAddEnergyTypeDTO dto) {
        List<EpEnergyType> epEnergyTypes = list(new LambdaQueryWrapper<EpEnergyType>().eq(EpEnergyType::getName, dto.getName()));
        if (epEnergyTypes.size()>1){
            throw new BusinessException("能源类型的名称不能重复!");
        }
        EpEnergyType epEnergyType = new EpEnergyType();
        BeanUtils.copyProperties(dto,epEnergyType);
        save(epEnergyType);
        return epEnergyType.getId();
    }

}




