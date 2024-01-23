package com.steccn.common.util;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import java.time.LocalDate;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Predicate;

/**
 * @author yequan
 * @desc map的工具类
 */
public class MapUtil {

    /**
     * 2层map的插入，k1和k2相同的元素，存放到集合中
     *
     * @param map   map集合
     * @param k1    k1键
     * @param k2    k2键
     * @param value 元素
     * @param <K1>  k1泛型
     * @param <K2>  k2泛型
     * @param <V>   元素泛型
     */
    public static <K1, K2, V> void biCompute(Map<K1, Map<K2, List<V>>> map, K1 k1, K2 k2, V value) {
        map.compute(k1, (k, v) -> {
            if (Objects.isNull(v)) {
                v = new LinkedHashMap<>();
            }
            v.compute(k2, (p, q) -> {
                if (Objects.isNull(q)) {
                    q = Lists.newArrayList();
                }
                q.add(value);
                return q;
            });
            return v;
        });
    }

    /**
     * 单层map 相同key存放到list中
     *
     * @param map   map集合
     * @param key   键
     * @param value 值
     * @param <K>   key泛型
     * @param <V>   value泛型
     */
    public static <K, V> void compute(Map<K, List<V>> map, K key, V value) {
        map.compute(key, (k, v) -> {
            if (Objects.isNull(v)) {
                v = Lists.newArrayList();
            }
            v.add(value);
            return v;
        });
    }

    /**
     * 单层map 相同key存放到list中
     *
     * @param map   map集合
     * @param key   键
     * @param value 值
     * @param <K>   key泛型
     * @param <V>   value泛型
     */
    public static <K, V> void computeSet(Map<K, Set<V>> map, K key, V value) {
        map.compute(key, (k, v) -> {
            if (Objects.isNull(v)) {
                v = Sets.newLinkedHashSet();
            }
            v.add(value);
            return v;
        });
    }


    /**
     * 单层map 相同key存放到list中
     *
     * @param map   map集合
     * @param key   键
     * @param value 值
     * @param <K>   key泛型
     * @param <V>   value泛型
     */
    public static <K, V> void add(Map<K, V> map, K key, V value, BiFunction<V, V, V> function) {
        V v = map.get(key);
        V apply = function.apply(v, value);
        map.put(key, apply);
    }

    /**
     * 单层map 相同key存放到list中
     *
     * @param map   map集合
     * @param key   键
     * @param value 值
     * @param <K>   key泛型
     * @param <V>   value泛型
     */
    public static <K, V> void putIfNotNull(Map<K, V> map, K key, V value) {
        if (Objects.nonNull(value)) {
            map.put(key, value);
        }
    }

}
