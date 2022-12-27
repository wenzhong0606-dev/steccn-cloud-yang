package com.steccn.common.util;

import cn.hutool.core.lang.Assert;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * @author yequan
 * @desc list分割
 */
@Slf4j
public class SplitListUtil {

    /**
     * list分割
     *
     * @param list list
     * @param size 按一个list的数量切分
     * @param <T>  泛型
     * @return
     */
    public static <T> List<List<T>> split(List<T> list, int size) {
        Assert.isTrue(size != 0, "分割list的size不能为0");
        Assert.notNull(list, "原始集合不能为空");

        List<List<T>> tNewList = new ArrayList<List<T>>();
        int priIndex = 0;
        int lastPriIndex = 0;
        int insertTimes = list.size() / size;

        for (int i = 0; i <= insertTimes; i++) {
            List<T> subList = new ArrayList<>();
            priIndex = size * i;
            lastPriIndex = priIndex + size;
            if (i == insertTimes) {
                for (int k = priIndex; k < list.size(); k++) {
                    subList.add(list.get(k));
                }
                subList = list.subList(priIndex, list.size());
            } else {
                for (int k = priIndex; k < lastPriIndex; k++) {
                    subList.add(list.get(k));
                }
            }
            if (subList.size() > 0) {
                tNewList.add(subList);
            }
        }
        return tNewList;
    }
}
