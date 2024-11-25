package com.steccn.common.util;

import cn.hutool.core.collection.CollUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.steccn.common.dto.FrontTableDTO;
import com.steccn.common.dto.FrontTableElement;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

public class FrontTableConvertUtil implements Serializable {

    /**
     * 两层表头
     *
     * @param headList
     * @param dataList
     * @param headGenerate
     * @param <T>
     * @return
     */
    public static <T> FrontTableDTO twoHeadConvert(List<FrontTableElement> headList, List<List<T>> dataList, BiFunction<String, Integer, String> headGenerate) {
        //标记映射位置
        Map<Integer, String> siteMap = Maps.newHashMap();
        Integer count = 0;
        FrontTableDTO<T> dto = new FrontTableDTO();
        List<FrontTableElement> columns = Lists.newArrayList();
        List<Map<String, T>> tableList = Lists.newArrayList();
        dto.setColumns(columns);
        dto.setTableList(tableList);
        for (FrontTableElement parentHead : headList) {
            //孩子不为空
            if (CollUtil.isNotEmpty(parentHead.getChildren())) {
                String apply = headGenerate.apply(parentHead.getLabel(), null);

                List<FrontTableElement> children = Lists.newArrayList();
                //多层表头
                for (FrontTableElement childHead : parentHead.getChildren()) {
                    String prop = headGenerate.apply(childHead.getLabel(), count);
                    children.add(new FrontTableElement(childHead.getLabel(), prop, null));
                    siteMap.put(count++, prop);
                }
                //孩子为空的情况下，自己就是自己，不占用位置
                columns.add(new FrontTableElement(parentHead.getLabel(), apply, children));
            } else {
                String apply = headGenerate.apply(parentHead.getLabel(), count);
                //孩子为空，则自己是元素
                siteMap.put(count++, apply);
                columns.add(new FrontTableElement(parentHead.getLabel(), apply));
            }
        }
        for (List<T> row : dataList) {
            Map<String, T> dataMap = Maps.newLinkedHashMap();
            for (int i = 0; i < row.size(); i++) {
                dataMap.put(siteMap.get(i), row.get(i));
            }
            tableList.add(dataMap);
        }
        return dto;
    }

}
