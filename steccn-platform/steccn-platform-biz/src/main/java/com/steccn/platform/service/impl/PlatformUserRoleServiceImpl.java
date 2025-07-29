package com.steccn.platform.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.steccn.platform.domain.PlatformUserRole;
import com.steccn.platform.domain.PlatformUserRoleDTO;
import com.steccn.platform.service.PlatformUserRoleService;
import com.steccn.platform.mapper.PlatformUserRoleMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
* @author Admin
* @description 针对表【platform_user_role(用户与角色关联表)】的数据库操作Service实现
* @createDate 2025-07-29 16:23:29
*/
@Service
public class PlatformUserRoleServiceImpl extends ServiceImpl<PlatformUserRoleMapper, PlatformUserRole>
    implements PlatformUserRoleService{

    @Override
    public Boolean bind(PlatformUserRoleDTO dto) {
        remove(new LambdaQueryWrapper<PlatformUserRole>()
                .eq(PlatformUserRole::getUserId,dto.getUserId()));
        if(CollUtil.isNotEmpty(dto.getRoleIds())){
            List<PlatformUserRole> models = Lists.newArrayList();
            dto.getRoleIds().forEach(tmp->{
                PlatformUserRole model = new PlatformUserRole();
                model.setUserId(dto.getUserId());
                model.setRoleId(tmp);
                models.add(model);
            });
            saveBatch(models);
        }
        return true;
    }
}




