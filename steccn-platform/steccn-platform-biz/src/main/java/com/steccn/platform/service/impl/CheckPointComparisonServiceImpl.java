package com.steccn.platform.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Maps;
import com.steccn.platform.domain.CheckPointComparison;
import com.steccn.platform.service.CheckPointComparisonService;
import com.steccn.platform.mapper.CheckPointComparisonMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
* @author Admin
* @description 针对表【check_point_comparison(测点信息对照表)】的数据库操作Service实现
* @createDate 2024-03-01 16:29:40
*/
@Service
public class CheckPointComparisonServiceImpl extends ServiceImpl<CheckPointComparisonMapper, CheckPointComparison>
    implements CheckPointComparisonService{

    @Override
    public Map<String, String> getCodeNameMap(Set<String> codeSet) {
        List<CheckPointComparison> list = list(new LambdaQueryWrapper<CheckPointComparison>().in(CheckPointComparison::getCheckPointCode, codeSet));
        Map<String,String> result = Maps.newHashMap();
        if(CollUtil.isNotEmpty(list)){
            result = list.stream().collect(Collectors
                    .toMap(CheckPointComparison::getCheckPointCode, CheckPointComparison::getCheckPointName));
        }
        return result;
    }
}




