package com.steccn.platform.service;

import com.steccn.platform.domain.CheckPointComparison;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Map;
import java.util.Set;

/**
* @author Admin
* @description 针对表【check_point_comparison(测点信息对照表)】的数据库操作Service
* @createDate 2024-03-01 16:29:40
*/
public interface CheckPointComparisonService extends IService<CheckPointComparison> {

    /**
     * 获取键值对
     * @param codeSet
     * @return
     */
    Map<String,String> getCodeNameMap(Set<String> codeSet);
}
