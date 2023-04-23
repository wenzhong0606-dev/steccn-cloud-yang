package com.steccn.platform.util;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Lists;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author yequan
 * @desc 逻辑分页
 */
public class LogicalPageUtil {

    /**
     * 逻辑分页代码
     * @param dataList 数据集合
     * @param current 当前页
     * @param size 一页条数
     * @param <T> 泛型
     * @return
     */
    public static <T> Page<T> page(List<T> dataList, int current, int size) {
        if(Objects.isNull(dataList)){
            dataList = Lists.newArrayList();
        }
        int idx = (current - 1) * size;
        if (idx < 0 || size < 0) {
            throw new IllegalArgumentException("current ，size 参数不合法");
        }
        List<T> pageData = dataList.stream().skip(idx).limit(size).collect(Collectors.toList());
        Page<T> page = new Page();
        page.setCurrent(current);
        page.setSize(size);
        page.setRecords(pageData);
        page.setTotal(dataList.size());
        return page;
    }
}

