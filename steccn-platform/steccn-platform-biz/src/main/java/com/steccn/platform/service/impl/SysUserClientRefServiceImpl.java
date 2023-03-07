package com.steccn.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.steccn.platform.domain.SysUserClientRef;
import com.steccn.platform.dto.SysUserClientRefDTO;
import com.steccn.platform.service.SysUserClientRefService;
import com.steccn.platform.mapper.SysUserClientRefMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * @author yequan
 * @description 针对表【sys_user_client_ref】的数据库操作Service实现
 * @createDate 2023-02-28 13:26:08
 */
@Service
public class SysUserClientRefServiceImpl extends ServiceImpl<SysUserClientRefMapper, SysUserClientRef>
        implements SysUserClientRefService {

    @Override
    public Boolean bind(SysUserClientRefDTO dto) {
        //查询旧的记录
        SysUserClientRef oldModel = getOne(new LambdaQueryWrapper<SysUserClientRef>().eq(SysUserClientRef::getUserId, dto.getUserId()), false);
        if (Objects.isNull(oldModel)) {
            oldModel = new SysUserClientRef();
        }
        oldModel.setClientNo(dto.getClientNo());
        oldModel.setUserId(dto.getUserId());
        oldModel.setUsername(dto.getUsername());
        oldModel.setLastLoginTime(LocalDateTime.now());
        //新增或更新新的记录
        return saveOrUpdate(oldModel);
    }

    @Override
    public String lastClientId(Long userId) {
        SysUserClientRef one = getOne(new LambdaQueryWrapper<SysUserClientRef>()
                .eq(SysUserClientRef::getUserId, userId), false);
        if (Objects.nonNull(one)) {
            return one.getClientNo();
        } else {
            return null;
        }
    }
}




