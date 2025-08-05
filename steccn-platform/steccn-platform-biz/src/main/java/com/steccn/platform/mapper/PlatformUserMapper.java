package com.steccn.platform.mapper;

import com.steccn.platform.domain.PlatformUser;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Set;

/**
* @author Admin
* @description 针对表【platform_user(用户表)】的数据库操作Mapper
* @createDate 2025-07-29 14:19:10
* @Entity com.steccn.platform.domain.PlatformUser
*/
@Mapper
public interface PlatformUserMapper extends BaseMapper<PlatformUser> {
    /**
     * 获取权限数据
     * @param userName
     * @return
     */
    Set<String> selectPermission(@Param("userName")String userName);
}




