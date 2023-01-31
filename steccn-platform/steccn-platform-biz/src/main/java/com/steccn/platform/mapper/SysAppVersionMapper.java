package com.steccn.platform.mapper;

import com.steccn.platform.domain.SysAppVersion;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @author yequan
* @description 针对表【sys_app_version(app的版本控制表)】的数据库操作Mapper
* @createDate 2023-01-31 14:02:33
* @Entity com.steccn.platform.domain.SysAppVersion
*/
@Mapper
public interface SysAppVersionMapper extends BaseMapper<SysAppVersion> {

}




