package com.steccn.platform.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Maps;
import com.steccn.platform.domain.SysClassification;
import com.steccn.platform.domain.SysDict;
import com.steccn.platform.service.SysClassificationService;
import com.steccn.platform.mapper.SysClassificationMapper;
import com.steccn.platform.vo.SysDictVO;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
* @author 34872
* @description 针对表【sys_classification(分类表)】的数据库操作Service实现
* @createDate 2024-01-08 16:34:19
*/
@Service
public class SysClassificationServiceImpl extends ServiceImpl<SysClassificationMapper, SysClassification>
    implements SysClassificationService{

    @Override
    public Map<Long, String> listByIdCh(Set<Long> typeIds) {
        Map<Long, String> resMap = Maps.newHashMap();
        if (CollUtil.isNotEmpty(typeIds)) {
            List<SysClassification> sysClassifications = list(new LambdaQueryWrapper<SysClassification>().in(SysClassification::getId, typeIds));
            if (CollUtil.isNotEmpty(sysClassifications)) {
                for (SysClassification sysClassification : sysClassifications) {
                    resMap.put(sysClassification.getId(), sysClassification.getName());
                }
            }
        }
        return resMap;
    }
}




