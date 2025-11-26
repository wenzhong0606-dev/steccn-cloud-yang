package com.steccn.platform.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.steccn.platform.domain.PlatformRoleMenu;
import com.steccn.platform.domain.PlatformUserRole;
import com.steccn.platform.dto.PlatformRoleMenuDTO;
import com.steccn.platform.service.PlatformRoleMenuService;
import com.steccn.platform.mapper.PlatformRoleMenuMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
* @author Admin
* @description 针对表【platform_role_menu(角色与菜单关联表)】的数据库操作Service实现
* @createDate 2025-08-05 14:42:05
*/
@Service
@RequiredArgsConstructor
public class PlatformRoleMenuServiceImpl extends ServiceImpl<PlatformRoleMenuMapper, PlatformRoleMenu>
    implements PlatformRoleMenuService{
    private final PlatformRoleMenuMapper platformRoleMenuMapper;

    @Override
    public Boolean bind(PlatformRoleMenuDTO dto) {
        remove(new LambdaQueryWrapper<PlatformRoleMenu>()
                .eq(PlatformRoleMenu::getRoleId,dto.getRoleId()));
        if(CollUtil.isNotEmpty(dto.getMenuIds())){
            List<PlatformRoleMenu> models = Lists.newArrayList();
            //需要获取所有菜单的父级id
            List<Integer> integers = platformRoleMenuMapper.selectAncestors(dto.getMenuIds());
            dto.getMenuIds().addAll(integers);
            dto.getMenuIds().forEach(tmp->{
                PlatformRoleMenu model = new PlatformRoleMenu();
                model.setRoleId(dto.getRoleId());
                model.setMenuId(tmp);
                models.add(model);
            });
            saveBatch(models);
        }
        return true;
    }
}




