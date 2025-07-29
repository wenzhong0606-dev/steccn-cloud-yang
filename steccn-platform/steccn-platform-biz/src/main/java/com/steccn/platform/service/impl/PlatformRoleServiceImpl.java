package com.steccn.platform.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.steccn.authserver.exception.BusinessException;
import com.steccn.authserver.util.SecurityUtils;
import com.steccn.platform.domain.PlatformRole;
import com.steccn.platform.domain.PlatformUser;
import com.steccn.platform.domain.PlatformUserRole;
import com.steccn.platform.dto.PlatformRoleDTO;
import com.steccn.platform.service.PlatformRoleService;
import com.steccn.platform.mapper.PlatformRoleMapper;
import com.steccn.platform.service.PlatformUserRoleService;
import com.steccn.platform.vo.PlatformRoleVO;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
* @author Admin
* @description 针对表【platform_role(角色表)】的数据库操作Service实现
* @createDate 2025-07-29 15:39:59
*/
@Service
@RequiredArgsConstructor
public class PlatformRoleServiceImpl extends ServiceImpl<PlatformRoleMapper, PlatformRole>
    implements PlatformRoleService{
    private final PlatformUserRoleService platformUserRoleService;

    @Override
    public Boolean add(PlatformRoleDTO dto) {
        PlatformRole one = getOne(new LambdaQueryWrapper<PlatformRole>()
                .eq(PlatformRole::getRoleCode, dto.getRoleCode())
                .last("limit 1"));
        if(Objects.nonNull(one)){
            throw new BusinessException("角色编码重复");
        }
        PlatformRole role = new PlatformRole();
        BeanUtil.copyProperties(dto,role);
        save(role);
        return true;
    }

    @Override
    public Boolean update(PlatformRoleDTO dto) {
        if(Objects.isNull(dto.getId())){
            throw new BusinessException("数据Id不能为空");
        }
        PlatformRole role = new PlatformRole();
        BeanUtil.copyProperties(dto,role);
        updateById(role);
        return true;
    }

    @Override
    public Boolean delete(Long id) {
        removeById(id);
        return true;
    }

    @Override
    public Page<PlatformRoleVO> pageList(PlatformRoleDTO dto) {
        Page<PlatformRole> page = page(new Page<>(dto.getCurrent(), dto.getSize()), new LambdaQueryWrapper<PlatformRole>()
                .like(StrUtil.isNotBlank(dto.getRoleCode()), PlatformRole::getRoleCode, dto.getRoleCode())
                .like(StrUtil.isNotBlank(dto.getRoleName()), PlatformRole::getRoleName, dto.getRoleName())
                .eq(Objects.nonNull(dto.getStatus()), PlatformRole::getStatus, dto.getStatus())
                .orderByDesc(PlatformRole::getSort));
        Page resPage = new Page<>(dto.getCurrent(), dto.getSize());
        BeanUtils.copyProperties(page, resPage);
        return resPage;
    }

    @Override
    public Page<PlatformRoleVO> bindPageList(PlatformRoleDTO dto) {
        Assert.isNull(dto.getUserId(),"用户id不能为空");
        Page<PlatformRoleVO> platformRoleVOPage = pageList(dto);
        List<PlatformRoleVO> results = Lists.newArrayList();
        //找到用户绑定的角色
        List<PlatformUserRole> list = platformUserRoleService.list(new LambdaQueryWrapper<PlatformUserRole>()
                .eq(PlatformUserRole::getUserId, dto.getUserId()));
        if(CollUtil.isNotEmpty(list)){
            Map<Integer, PlatformUserRole> collect = list.stream().collect(Collectors.toMap(PlatformUserRole::getRoleId, Function.identity()));
            platformRoleVOPage.getRecords().forEach(tmp->{
                PlatformRoleVO vo = new PlatformRoleVO();
                BeanUtil.copyProperties(tmp,vo);
                if(Objects.nonNull(collect.get(tmp.getId()))){
                    vo.setIsBind(1);
                }
                results.add(vo);
            });
            platformRoleVOPage.setRecords(results);

        }
        return platformRoleVOPage;
    }


}




