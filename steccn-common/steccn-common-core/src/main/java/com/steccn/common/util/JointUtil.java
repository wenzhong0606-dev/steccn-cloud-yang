package com.steccn.common.util;

import cn.hutool.core.collection.CollUtil;

import java.util.List;

/**
 * @author yequan
 * @desc 拼接字符串
 * @date 2023/10/8
 */
public class JointUtil {

    public static final String SEPARATOR_UNDERLINE = "_";
    public static final String SEPARATOR_COMMA = ",";

    /**
     * 拼接字符串
     *
     * @param split 分隔符
     * @param args  参数
     * @return
     */
    public static String joint(String split, Object... args) {
        if (args == null || args.length == 0) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < args.length; i++) {
            sb.append(args[i]);
            if (i != args.length - 1) {
                sb.append(split);
            }
        }
        return sb.toString();
    }

    /**
     * 拼接集合
     * @param split
     * @param list
     * @return
     */
    public static String jointList(String split, List<? extends Object> list) {
        if (CollUtil.isEmpty(list)) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            sb.append(list.get(i));
            if (i != list.size() - 1) {
                sb.append(split);
            }
        }
        return sb.toString();
    }
}
