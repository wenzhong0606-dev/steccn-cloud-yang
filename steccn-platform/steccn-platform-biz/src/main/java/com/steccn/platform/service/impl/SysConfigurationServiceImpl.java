package com.steccn.platform.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Maps;
import com.steccn.platform.domain.SysConfiguration;
import com.steccn.platform.service.SysConfigurationService;
import com.steccn.platform.mapper.SysConfigurationMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * @author yequan
 * @description 针对表【sys_configuration】的数据库操作Service实现
 * @createDate 2023-01-04 15:06:46
 */
@Service
public class SysConfigurationServiceImpl extends ServiceImpl<SysConfigurationMapper, SysConfiguration>
        implements SysConfigurationService {

    @Override
    public Map<String, String> sysConfRef(String type, Set<Integer> refContentSet) {
        Map<String, String> resMap = Maps.newHashMap();
        if (StrUtil.isNotBlank(type) && CollUtil.isNotEmpty(refContentSet)) {
            List<SysConfiguration> dbList = list(new LambdaQueryWrapper<SysConfiguration>()
                    .eq(SysConfiguration::getType, type)
                    .in(SysConfiguration::getRefContent, refContentSet));
            if (CollUtil.isNotEmpty(dbList)) {
                dbList.forEach(ele -> {
                    resMap.put(ele.getRefContent(), ele.getContent());
                });
            }
        }
        return resMap;
    }
}


