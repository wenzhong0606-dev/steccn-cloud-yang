package com.steccn.platform.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.steccn.platform.domain.SysLogShowConf;
import com.steccn.platform.service.SysLogShowConfService;
import com.steccn.platform.mapper.SysLogShowConfMapper;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * @author yequan
 * @description 针对表【sys_log_show_conf】的数据库操作Service实现
 * @createDate 2024-03-27 15:39:38
 */
@Service
public class SysLogShowConfServiceImpl extends ServiceImpl<SysLogShowConfMapper, SysLogShowConf>
        implements SysLogShowConfService {

    @Override
    @Cacheable(cacheNames = "requestUrlList#1296000")
    public List<SysLogShowConf> requestUrlList() {
        List<SysLogShowConf> list = list(new LambdaQueryWrapper<SysLogShowConf>().select(SysLogShowConf::getMethod,SysLogShowConf::getRequestUrl));
        return Objects.isNull(list) ? Lists.newArrayList() : list;
    }
}




