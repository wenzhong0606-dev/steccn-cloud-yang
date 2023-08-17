package com.steccn.portal.util;

import com.alibaba.nacos.common.utils.MD5Utils;
import com.steccn.common.exception.SteccnRuntimeException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;

import javax.crypto.SecretKey;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.Date;

/**
 * mes系统 token工具类
 *
 * @author Mongknife
 * @date 2022/8/31
 */
public class MesToken {
    private static final String SECRET_KEY = "steccn"; //安全密钥，与mes系统配置一致；
    public static final String UTF_8 = "UTF-8";
    private static final String MD5 = "MD5";

    /**
     * mes 生成token
     * @param userName
     * @return
     */
    public static String getMesToken(String userName) {
        return md5(SECRET_KEY + userName + DateFormatUtils.format(new Date(), "yyyyMMdd"));
    }

    /**
     * 对输入字符串进行md5散列.
     *
     * @param input 加密字符串
     */
    public static String md5(String input) {
        return md5(input, 1);
    }

    /**
     * 对输入字符串进行md5散列.
     *
     * @param input      加密字符串
     * @param iterations 迭代次数
     */
    public static String md5(String input, int iterations) {
        try {
            return encodeHex(digest(input.getBytes(UTF_8), MD5, null, iterations));
        } catch (UnsupportedEncodingException e) {
            return "";
        }
    }

    /**
     * Hex编码.
     */
    public static String encodeHex(byte[] input) {
        return new String(Hex.encodeHex(input));
    }

    /**
     * 对字符串进行散列, 支持md5与sha1算法.
     *
     * @param input      需要散列的字符串
     * @param algorithm  散列算法（"SHA-1"、"MD5"）
     * @param salt
     * @param iterations 迭代次数
     * @return
     */
    public static byte[] digest(byte[] input, String algorithm, byte[] salt, int iterations) {
        try {
            MessageDigest digest = MessageDigest.getInstance(algorithm);

            if (salt != null) {
                digest.update(salt);
            }

            byte[] result = digest.digest(input);

            for (int i = 1; i < iterations; i++) {
                digest.reset();
                result = digest.digest(result);
            }
            return result;
        } catch (GeneralSecurityException e) {
            throw unchecked(e);
        }
    }

    /**
     * 将CheckedException转换为UncheckedException.
     */
    public static SteccnRuntimeException unchecked(Exception e) {
        if (e instanceof SteccnRuntimeException) {
            return (SteccnRuntimeException) e;
        } else {
            return new SteccnRuntimeException(e);
        }
    }
}
