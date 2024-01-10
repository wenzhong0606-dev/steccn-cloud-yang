package com.steccn.common.util;

import com.google.common.collect.Lists;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author yequan
 * @desc 获取对象属性, map的key
 */
public class GetValueUtil {


    /**
     * 获取对象的属性，如果对象为空，则返回默认值
     *
     * @param obj
     * @param function
     * @param <T>
     * @param <R>
     * @return
     */
    public static <T, R> R get(T obj, Function<T, R> function) {
        return get(obj, function, null);
    }


    /**
     * 获取对象的属性，如果对象为空，则返回默认值
     *
     * @param obj
     * @param function
     * @param defaultRes
     * @param <T>
     * @param <R>
     * @return
     */
    public static <T, R> R get(T obj, Function<T, R> function, R defaultRes) {
        if (Objects.isNull(obj)) {
            return defaultRes;
        }
        R apply = function.apply(obj);
        if (Objects.isNull(apply)) {
            return defaultRes;
        }
        return apply;
    }

    /**
     * 获取一个集合对象的属性，如果对象为空，则返回默认值
     *
     * @param objList
     * @param function
     * @param <T>
     * @param <R>
     * @return
     */
    public static <T, R> List<R> collectionGet(Collection<T> objList, Function<T, R> function) {
        return collectionGet(objList, function, null);
    }

    /**
     * 获取一个集合对象的属性，如果对象为空，则返回默认值
     *
     * @param objList
     * @param function
     * @param <T>
     * @param <R>
     * @return
     */
    public static <T, R> List<R> collectionGet(Collection<T> objList, Function<T, R> function, R defaultRes) {
        List<R> collect = objList.stream().map(ele -> get(ele, function, defaultRes)).collect(Collectors.toList());
        return collect;
    }

}
