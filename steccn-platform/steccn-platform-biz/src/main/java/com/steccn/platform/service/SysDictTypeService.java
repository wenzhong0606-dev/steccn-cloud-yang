package com.steccn.platform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.common.constant.ResultCode;
import com.steccn.platform.domain.SysDictType;
import com.steccn.platform.dto.SysDictTypeDTO;

/**
* @author yequan
* @description 针对表【sys_dict_type(数据字典类型表)】的数据库操作Service
* @createDate 2022-09-02 08:54:10
*/
public interface SysDictTypeService extends IService<SysDictType> {

    /**
     * 根据字典类型id 获取字典类型数据
     * @param id
     * @return
     */
    SysDictTypeDTO get(Long id);


    /**
     * 保存字典类型数据
     * @param sysDictTypeDTO
     * @return
     */
    Boolean saveType(SysDictTypeDTO sysDictTypeDTO);
}
