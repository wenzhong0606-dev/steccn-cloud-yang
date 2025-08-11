package com.steccn.platform.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.crypto.digest.MD5;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.steccn.authserver.exception.BusinessException;
import com.steccn.authserver.util.SecurityUtils;
import com.steccn.authserver.util.SteccnUser;
import com.steccn.common.util.AESUtils;
import com.steccn.oa.constant.OaConstant;
import com.steccn.platform.domain.PlatformMenu;
import com.steccn.platform.domain.PlatformRole;
import com.steccn.platform.domain.PlatformUser;
import com.steccn.platform.domain.PlatformUserRole;
import com.steccn.platform.dto.PlatformUserDTO;
import com.steccn.platform.mapper.PlatformMenuMapper;
import com.steccn.platform.mapper.PlatformRoleMenuMapper;
import com.steccn.platform.service.PlatformRoleMenuService;
import com.steccn.platform.service.PlatformUserRoleService;
import com.steccn.platform.service.PlatformUserService;
import com.steccn.platform.mapper.PlatformUserMapper;
import com.steccn.platform.vo.PlatformUserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
* @author Admin
* @description 针对表【platform_user(用户表)】的数据库操作Service实现
* @createDate 2025-07-29 14:19:10
*/
@Service
@RequiredArgsConstructor
public class PlatformUserServiceImpl extends ServiceImpl<PlatformUserMapper, PlatformUser>
    implements PlatformUserService{
    private final PlatformUserMapper platformUserMapper;
    private final PlatformMenuMapper platformMenuMapper;

    @Override
    public Boolean add(PlatformUserDTO dto) {
        PlatformUser one = getOne(new LambdaQueryWrapper<PlatformUser>()
                .eq(PlatformUser::getUserName, dto.getUserName())
                .last("limit 1"));
        if(Objects.nonNull(one)){
            throw new BusinessException("用户名重复");
        }
        PlatformUser user = new PlatformUser();
        BeanUtil.copyProperties(dto,user);
        user.setPassword(SecureUtil.md5(AESUtils.decrypt(dto.getPassword())));
        user.setUserCode(dto.getUserName()+"_" +RandomUtil.randomString(4));
        save(user);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean update(PlatformUserDTO dto) {
        if(Objects.isNull(dto.getId())){
            throw new BusinessException("数据Id不能为空");
        }
        if(!SecurityUtils.getUser().getUserId().equals(dto.getId())&&!SecurityUtils.getUser().getUsername().equals("system")){
            throw new BusinessException("只有本人才能修改账号数据");
        }
        PlatformUser user = new PlatformUser();
        BeanUtil.copyProperties(dto,user);
        //编码无法修改
        user.setUserCode(null);
        user.setPassword(SecureUtil.md5(AESUtils.decrypt(dto.getPassword())));
        updateById(user);
        List<PlatformUser> list = list(new LambdaQueryWrapper<PlatformUser>().eq(PlatformUser::getUserName, dto.getUserName()));
        if(CollUtil.isNotEmpty(list)&&list.size()!=1){
            throw new BusinessException("用户名重复");
        }
        return true;
    }

    @Override
    public Boolean delete(Long id) {
        if(id.equals(OaConstant.NUM_1.longValue())){
            throw new BusinessException("超级管理员无法删除");
        }
        removeById(id);
        return true;
    }

    @Override
    public PlatformUserVO getByUserName(String userName) {
        PlatformUser one = getOne(new LambdaQueryWrapper<PlatformUser>()
                .eq(PlatformUser::getUserName, userName));
        Set<String> permission = Sets.newHashSet();
        if(userName.equals("system")){
            List<PlatformMenu> platformMenus = platformMenuMapper.selectList(new LambdaQueryWrapper<PlatformMenu>()
                    .select(PlatformMenu::getPermission)
                    .eq(PlatformMenu::getMenuType, 1));
            if(CollUtil.isNotEmpty(platformMenus)){
                permission = platformMenus.stream().map(PlatformMenu::getPermission).collect(Collectors.toSet());
            }
        }else {
            permission = platformUserMapper.selectPermission(userName);
        }
        PlatformUserVO vo = new PlatformUserVO();
        BeanUtil.copyProperties(one,vo);
        if(CollUtil.isNotEmpty(permission)){
            vo.setUserScope(permission.stream().collect(Collectors.toList()));
        }else {
            vo.setUserScope(Lists.newArrayList());
        }
        return vo;
    }

    @Override
    public Page<PlatformUserVO> pageList(PlatformUserDTO dto) {
        Page<PlatformUser> page = page(new Page<>(dto.getCurrent(), dto.getSize()), new LambdaQueryWrapper<PlatformUser>()
                .like(StrUtil.isNotBlank(dto.getUserCode()), PlatformUser::getUserCode, dto.getUserCode())
                .like(StrUtil.isNotBlank(dto.getUserName()), PlatformUser::getUserName, dto.getUserName())
                .like(StrUtil.isNotBlank(dto.getName()), PlatformUser::getName, dto.getName())
                .eq(Objects.nonNull(dto.getStatus()), PlatformUser::getStatus, dto.getStatus())
                .eq(Objects.nonNull(dto.getOfficeId()), PlatformUser::getOfficeId, dto.getOfficeId())
                .eq(Objects.nonNull(dto.getPostId()), PlatformUser::getPostId, dto.getPostId()));
        Page resPage = new Page<PlatformUserVO>(dto.getCurrent(), dto.getSize());
        BeanUtils.copyProperties(page, resPage);
        return resPage;
    }
}




