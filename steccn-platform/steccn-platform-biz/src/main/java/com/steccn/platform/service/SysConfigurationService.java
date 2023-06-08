package com.steccn.platform.service;

import com.steccn.platform.domain.SysConfiguration;
import com.baomidou.mybatisplus.extension.service.IService;
import org.apache.poi.ss.formula.functions.T;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
* @author yequan
* @description 针对表【sys_configuration】的数据库操作Service
* @createDate 2023-01-04 15:06:46
*/
public interface SysConfigurationService extends IService<SysConfiguration> {

    /**
     * 获取根据ref内容提供获取映射配置
     * @param type 类型
     * @param refContentSet 映射数据
     * @return map
     */
    Map<String, String> sysConfRef(String type, Set<Integer> refContentSet);

    /**
     * 根据codelist返回集合
     * @param type 类型
     * @param contentList 类型集合
     * @return
     */
    <T> Map<String, List<T>> confList(String type, Set<String> contentList,Function<String, T> function);
}
