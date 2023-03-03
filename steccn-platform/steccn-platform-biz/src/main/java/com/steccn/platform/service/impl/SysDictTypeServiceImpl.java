package com.steccn.platform.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.steccn.platform.domain.SysDict;
import com.steccn.platform.domain.SysDictType;
import com.steccn.platform.dto.SysDictTypeDTO;
import com.steccn.platform.mapper.SysDictTypeMapper;
import com.steccn.platform.service.SysDictTypeService;
import org.springframework.stereotype.Service;

/**
 * @author yequan
 * @description 针对表【sys_dict_type(数据字典类型表)】的数据库操作Service实现
 * @createDate 2022-09-02 08:54:10
 */
@Service
public class SysDictTypeServiceImpl extends ServiceImpl<SysDictTypeMapper, SysDictType> implements SysDictTypeService {

    @Override
    public SysDictTypeDTO get(Long id) {
        SysDictType dictType = getById(id);
        SysDictTypeDTO sysDictTypeDTO = new SysDictTypeDTO();
        BeanUtil.copyProperties(dictType, sysDictTypeDTO);
        return sysDictTypeDTO;
    }

    @Override
    public Boolean saveType(SysDictTypeDTO sysDictTypeDTO) {
        SysDictType sysDictType = new SysDictType();
        BeanUtil.copyProperties(sysDictTypeDTO, sysDictType);
        return save(sysDictType);
    }
}




