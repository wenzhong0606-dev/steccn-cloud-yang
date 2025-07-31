package com.steccn.platform.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.steccn.authserver.exception.BusinessException;
import com.steccn.platform.domain.PlatformPost;
import com.steccn.platform.domain.PlatformRole;
import com.steccn.platform.dto.PlatformPostDTO;
import com.steccn.platform.service.PlatformPostService;
import com.steccn.platform.mapper.PlatformPostMapper;
import com.steccn.platform.vo.PlatformPostVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
* @author Admin
* @description 针对表【platform_post(岗位表)】的数据库操作Service实现
* @createDate 2025-07-31 16:40:21
*/
@Service
public class PlatformPostServiceImpl extends ServiceImpl<PlatformPostMapper, PlatformPost>
    implements PlatformPostService{

    @Override
    public Boolean add(PlatformPostDTO dto) {
        PlatformPost one = getOne(new LambdaQueryWrapper<PlatformPost>()
                .eq(PlatformPost::getPostCode, dto.getPostCode())
                .last("limit 1"));
        if(Objects.nonNull(one)){
            throw new BusinessException("岗位编码重复");
        }
        PlatformPost post = new PlatformPost();
        BeanUtil.copyProperties(dto,post);
        save(post);
        return true;
    }

    @Override
    public Boolean update(PlatformPostDTO dto) {
        if(Objects.isNull(dto.getId())){
            throw new BusinessException("数据Id不能为空");
        }
        dto.setPostCode(null);
        PlatformPost post = new PlatformPost();
        BeanUtil.copyProperties(dto,post);
        updateById(post);
        return true;
    }

    @Override
    public Boolean delete(Long id) {
        removeById(id);
        return true;
    }

    @Override
    public Page<PlatformPostVO> pageList(PlatformPostDTO dto) {
        Page<PlatformPost> page = page(new Page<>(dto.getCurrent(), dto.getSize()), new LambdaQueryWrapper<PlatformPost>()
                .like(StrUtil.isNotBlank(dto.getPostCode()), PlatformPost::getPostCode, dto.getPostCode())
                .like(StrUtil.isNotBlank(dto.getPostName()), PlatformPost::getPostName, dto.getPostName())
                .eq(Objects.nonNull(dto.getStatus()), PlatformPost::getStatus, dto.getStatus())
                .orderByDesc(PlatformPost::getSort));
        Page resPage = new Page<>(dto.getCurrent(), dto.getSize());
        BeanUtils.copyProperties(page, resPage);
        return resPage;
    }
}




