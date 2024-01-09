package com.steccn.platform.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.steccn.platform.domain.SysConfiguration;
import com.steccn.platform.dto.SysConfigurationDTO;
import com.steccn.platform.service.SysConfigurationService;
import com.steccn.platform.mapper.SysConfigurationMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author yequan
 * @description 针对表【sys_configuration】的数据库操作Service实现
 * @createDate 2023-01-04 15:06:46
 */
@Service
@Slf4j
public class SysConfigurationServiceImpl extends ServiceImpl<SysConfigurationMapper, SysConfiguration>
        implements SysConfigurationService {

    @Override
    public Map<String, String> sysConfRef(String type, Set<Integer> refContentSet) {
        Map<String, String> resMap = Maps.newHashMap();
        if (StrUtil.isNotBlank(type)) {
            List<SysConfiguration> dbList = list(new LambdaQueryWrapper<SysConfiguration>()
                    .eq(SysConfiguration::getType, type)
                    .in(CollUtil.isNotEmpty(refContentSet), SysConfiguration::getRefContent, refContentSet));
            if (CollUtil.isNotEmpty(dbList)) {
                dbList.forEach(ele -> {
                    resMap.put(ele.getContent(), ele.getRefContent());
                });
            }
        }
        return resMap;
    }

    @Override
    public <T> Map<String, List<T>> confList(String type, Set<String> contentList, Function<SysConfigurationDTO, T> function) {
        Map<String, List<T>> resMap = Maps.newHashMap();
        List<SysConfiguration> dbList = list(new LambdaQueryWrapper<SysConfiguration>().eq(SysConfiguration::getType, type)
                .in(SysConfiguration::getContent, contentList));
        if (CollUtil.isNotEmpty(dbList)) {
            Map<String, List<SysConfiguration>> dbMap = dbList.stream().collect(Collectors.groupingBy(SysConfiguration::getContent));
            dbMap.forEach((k, v) -> {
                List<T> resList = Lists.newArrayList();
                v.forEach(ele -> {
                    try {
                        SysConfigurationDTO configurationDTO = new SysConfigurationDTO();
                        BeanUtils.copyProperties(ele, configurationDTO);
                        T apply = function.apply(configurationDTO);
                        resList.add(apply);
                    } catch (Exception e) {
                       log.error("function 执行异常 {}",e.getLocalizedMessage());
                    }
                });
                resMap.put(k, resList);
            });
        }
        return resMap;
    }

    @Override
    @Cacheable(cacheNames = "MesSignal")
    public Set<String> contentByType(String type) {
        Set<String> results = Sets.newHashSet();
        List<SysConfiguration> list = list(new LambdaQueryWrapper<SysConfiguration>()
                .select(SysConfiguration::getContent)
                .eq(SysConfiguration::getType, type));
        if(CollUtil.isNotEmpty(list)){
            results = list.stream().map(SysConfiguration::getContent).collect(Collectors.toSet());
        }
        return results;
    }

    @Override
    @Cacheable(cacheNames = "ConfigTypeContent")
    public SysConfigurationDTO infoByTypeAndContent(String type, String content) {
        SysConfigurationDTO result = new SysConfigurationDTO();
        SysConfiguration config = getOne(new LambdaQueryWrapper<SysConfiguration>().eq(SysConfiguration::getType, type)
                .eq(SysConfiguration::getContent,content),false);
        if(Objects.nonNull(config)){
            BeanUtils.copyProperties(config, result);
        }
        return result;
    }


}


