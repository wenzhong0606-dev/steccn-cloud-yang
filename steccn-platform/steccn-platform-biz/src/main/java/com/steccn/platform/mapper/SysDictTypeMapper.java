package com.steccn.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.steccn.platform.domain.SysDictType;
import org.apache.ibatis.annotations.Mapper;

/**
* @author yequan
* @description 针对表【sys_dict_type(数据字典类型表)】的数据库操作Mapper
* @createDate 2022-09-02 08:54:10
* @Entity com.steccn.platform.domain.SysDictType
*/
@Mapper
public interface SysDictTypeMapper extends BaseMapper<SysDictType> {

}




