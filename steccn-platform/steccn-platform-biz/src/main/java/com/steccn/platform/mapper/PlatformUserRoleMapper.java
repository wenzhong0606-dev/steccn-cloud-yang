package com.steccn.platform.mapper;

import com.steccn.platform.domain.PlatformUserRole;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @author Admin
* @description 针对表【platform_user_role(用户与角色关联表)】的数据库操作Mapper
* @createDate 2025-07-29 16:23:29
* @Entity com.steccn.platform.domain.PlatformUserRole
*/
@Mapper
public interface PlatformUserRoleMapper extends BaseMapper<PlatformUserRole> {

}




