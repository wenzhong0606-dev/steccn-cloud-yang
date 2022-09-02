package com.steccn.platform.mapper;

import com.steccn.platform.domain.SysDict;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @author yequan
* @description 针对表【sys_dict(数据字典表)】的数据库操作Mapper
* @createDate 2022-09-02 08:54:09
* @Entity com.steccn.platform.domain.SysDict
*/
@Mapper
public interface SysDictMapper extends BaseMapper<SysDict> {

}




