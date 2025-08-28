package com.steccn.platform.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.steccn.platform.domain.PlatformUser;
import com.steccn.platform.domain.PlatformUserMac;
import com.steccn.platform.mapper.PlatformUserMapper;
import com.steccn.platform.service.PlatformUserMacService;
import com.steccn.platform.mapper.PlatformUserMacMapper;
import com.steccn.platform.service.PlatformUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
* @author Admin
* @description 针对表【platform_user_mac(用户mac地址关联表)】的数据库操作Service实现
* @createDate 2025-08-28 15:27:49
*/
@Service
@RequiredArgsConstructor
public class PlatformUserMacServiceImpl extends ServiceImpl<PlatformUserMacMapper, PlatformUserMac>
    implements PlatformUserMacService{
    private final PlatformUserMapper platformUserMapper;

    @Override
    public Boolean compareMac(Long userId,String macAddress) {
        List<PlatformUserMac> list = list(new LambdaQueryWrapper<PlatformUserMac>()
                .eq(PlatformUserMac::getUserId, userId));
        //一个用户只允许两个设备登录
        if(CollUtil.isEmpty(list) || list.size()<2){
            PlatformUserMac model = new PlatformUserMac();
            model.setUserId(userId);
            model.setMacAddress(macAddress);
            PlatformUser byId = platformUserMapper.selectById(userId);
            model.setCreateBy(byId.getId());
            model.setCreateName(byId.getName());
            model.setUpdateBy(byId.getId());
            model.setUpdateName(byId.getName());
            save(model);
            return true;
        }
        PlatformUserMac platformUserMac = list.stream().filter(v -> v.getMacAddress().equals(macAddress)).findFirst().orElse(null);
        if(Objects.isNull(platformUserMac)){
            return false;
        }
        return true;
    }

    @Override
    public Boolean clearMac(Long userId) {
        remove(new LambdaQueryWrapper<PlatformUserMac>()
                .eq(PlatformUserMac::getUserId, userId));
        return true;
    }
}




