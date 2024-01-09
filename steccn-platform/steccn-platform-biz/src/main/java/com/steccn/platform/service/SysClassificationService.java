package com.steccn.platform.service;

import com.steccn.platform.domain.SysClassification;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Map;
import java.util.Set;

/**
* @author 34872
* @description 针对表【sys_classification(分类表)】的数据库操作Service
* @createDate 2024-01-08 16:34:19
*/
public interface SysClassificationService extends IService<SysClassification> {

    Map<Long, String> listByIdCh(Set<Long> typeIds);
}
