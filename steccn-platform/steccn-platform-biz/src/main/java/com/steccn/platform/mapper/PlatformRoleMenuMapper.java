package com.steccn.platform.mapper;

import com.steccn.platform.domain.PlatformRoleMenu;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

/**
* @author Admin
* @description 针对表【platform_role_menu(角色与菜单关联表)】的数据库操作Mapper
* @createDate 2025-08-05 14:42:05
* @Entity com.steccn.platform.domain.PlatformRoleMenu
*/
@Mapper
public interface PlatformRoleMenuMapper extends BaseMapper<PlatformRoleMenu> {
    List<Integer> selectAncestors(@Param("leafIds") Set<Integer> leafIds);
}




