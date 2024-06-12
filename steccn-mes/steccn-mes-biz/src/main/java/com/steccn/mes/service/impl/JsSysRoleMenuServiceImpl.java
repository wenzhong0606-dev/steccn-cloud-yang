package com.steccn.mes.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.steccn.mes.domain.JsSysRoleMenu;
import com.steccn.mes.domain.JsSysUser;
import com.steccn.mes.domain.JsSysUserRole;
import com.steccn.mes.mapper.JsSysRoleMenuMapper;
import com.steccn.mes.mapper.JsSysUserMapper;
import com.steccn.mes.mapper.JsSysUserRoleMapper;
import com.steccn.mes.service.JsSysRoleMenuService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
* @author Admin
* @description 针对表【js_sys_role_menu(角色与菜单关联表)】的数据库操作Service实现
* @createDate 2024-05-29 10:18:41
*/
@Service
@RequiredArgsConstructor
@Slf4j
public class JsSysRoleMenuServiceImpl extends ServiceImpl<JsSysRoleMenuMapper, JsSysRoleMenu>
    implements JsSysRoleMenuService{
    private final JsSysUserMapper jsSysUserMapper;
    private final JsSysUserRoleMapper jsSysUserRoleMapper;
    private final JsSysRoleMenuMapper jsSysRoleMenuMapper;

    @Override
    public List<Long> menuIds(String userName) {
        List<Long> results = Lists.newArrayList();
        JsSysUser jsSysUser = jsSysUserMapper.selectOne(new LambdaQueryWrapper<JsSysUser>()
                .select(JsSysUser::getUserCode)
                .eq(JsSysUser::getLoginCode, userName)
                .eq(JsSysUser::getStatus, "0")
                .orderByDesc(JsSysUser::getCreateDate)
                .last("LIMIT 1"));
        if(Objects.nonNull(jsSysUser)){
            List<JsSysUserRole> jsSysUserRoles = jsSysUserRoleMapper.selectList(new LambdaQueryWrapper<JsSysUserRole>()
                    .eq(JsSysUserRole::getUserCode, jsSysUser.getUserCode()));
            if(CollUtil.isNotEmpty(jsSysUserRoles)){
                List<JsSysRoleMenu> jsSysRoleMenus = jsSysRoleMenuMapper.selectList(new QueryWrapper<JsSysRoleMenu>()
                        .select("DISTINCT menu_code").lambda()
                        .in(JsSysRoleMenu::getRoleCode, jsSysUserRoles.stream().map(JsSysUserRole::getRoleCode).collect(Collectors.toSet())));
                if(CollUtil.isNotEmpty(jsSysRoleMenus)){
                    results = jsSysRoleMenus.stream().map(JsSysRoleMenu::getMenuCode).map(Long::valueOf).collect(Collectors.toList());
                }
            }
        }
        return results;
    }
}




