package com.steccn.platform.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.crypto.digest.MD5;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.steccn.authserver.exception.BusinessException;
import com.steccn.authserver.util.SecurityUtils;
import com.steccn.platform.domain.PlatformRole;
import com.steccn.platform.domain.PlatformUser;
import com.steccn.platform.dto.PlatformUserDTO;
import com.steccn.platform.service.PlatformUserService;
import com.steccn.platform.mapper.PlatformUserMapper;
import com.steccn.platform.vo.PlatformUserVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
* @author Admin
* @description 针对表【platform_user(用户表)】的数据库操作Service实现
* @createDate 2025-07-29 14:19:10
*/
@Service
public class PlatformUserServiceImpl extends ServiceImpl<PlatformUserMapper, PlatformUser>
    implements PlatformUserService{

    @Override
    public Boolean add(PlatformUserDTO dto) {
        PlatformUser one = getOne(new LambdaQueryWrapper<PlatformUser>()
                .eq(PlatformUser::getUserName, dto.getUserName())
                .last("limit 1"));
        if(Objects.nonNull(one)){
            throw new BusinessException("登录名重复");
        }
        PlatformUser user = new PlatformUser();
        BeanUtil.copyProperties(dto,user);
        user.setPassword(DigestUtil.md5Hex(dto.getPassword()));
        if(Objects.isNull(dto.getUserCode())){
            user.setUserCode(dto.getUserName()+"_" +RandomUtil.randomString(4));
        }
        save(user);
        return true;
    }

    @Override
    public Boolean update(PlatformUserDTO dto) {
        if(Objects.isNull(dto.getId())){
            throw new BusinessException("数据Id不能为空");
        }
        if(!SecurityUtils.getUser().getUserId().equals(dto.getId())&&!SecurityUtils.getUser().getUsername().equals("system")){
            throw new BusinessException("只有本人才能修改账号数据");
        }
        PlatformUser user = new PlatformUser();
        BeanUtil.copyProperties(dto,user);
        user.setPassword(DigestUtil.md5Hex(dto.getPassword()));
        updateById(user);
        return true;
    }

    @Override
    public Boolean delete(Long id) {
        removeById(id);
        return true;
    }

    @Override
    public PlatformUserVO getByUserName(String userName) {
        PlatformUser one = getOne(new LambdaQueryWrapper<PlatformUser>()
                .eq(PlatformUser::getUserName, userName));
        //todo 获取用户权限列表
        PlatformUserVO vo = new PlatformUserVO();
        BeanUtil.copyProperties(one,vo);
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




