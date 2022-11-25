package com.steccn.common.util;


import cn.hutool.crypto.Mode;
import cn.hutool.crypto.Padding;
import cn.hutool.crypto.symmetric.AES;

/**
 * @desc 对称加密和解密工具类
 * @author yequan
 * @date 2022-11-25
 */
public class AESUtils {

    /**
     * 密钥(AES,密钥的长度必须是16个字节)
     */
    private static final String KEY = "tongSmartFactory";

    /**
     * 加密
     *
     * @param content 明文
     * @return 返回密文
     */
    public static String encrypt(String content) {
        AES aes = new AES(Mode.CBC, Padding.ZeroPadding, KEY.getBytes(), KEY.getBytes());
        String encode = aes.encryptBase64(content);
        return encode;
    }

    /**
     * 解密
     *
     * @param content 密文
     * @return 返回原文
     */
    public static String decrypt(String content) {
        AES aes = new AES(Mode.CBC, Padding.ZeroPadding, KEY.getBytes(), KEY.getBytes());
        return aes.decryptStr(content);
    }
}
