package com.steccn.common.util;

/**
 * @author yequan
 * @desc 拼接字符串
 * @date 2023/10/8
 */
public class JointUtil {

    public static final String SEPARATOR_UNDERLINE = "_";

    /**
     * 拼接字符串
     *
     * @param split 分隔符
     * @param args  参数
     * @return
     */
    public static String joint(String split, Object... args) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < args.length; i++) {
            sb.append(args[i]);
            if (i != args.length - 1) {
                sb.append(split);
            }
        }
        return sb.toString();
    }
}
