package com.steccn.portal.constant;

import com.steccn.portal.config.HikVisionConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 海康威视常量
 *
 * @author AndrewLuo
 * @date 2022/11/22
 */
public class HikVisionConstant {
    public static final String HIK_HOST = HikVisionConfigHelper.HIK_HOST;
    public static final String HIK_APP_KEY = HikVisionConfigHelper.HIK_APP_KEY;
    public static final String HIK_APP_SECRET = HikVisionConfigHelper.HIK_APP_SECRET;
    public static final String PATH = HikVisionConfigHelper.PATH;
    public static final String SERVICE = HikVisionConfigHelper.SERVICE;
}

/**
 * 把配置转成静态属性
 */
@Component
class HikVisionConfigHelper{
    static String HIK_HOST;
    static String HIK_APP_KEY;
    static String HIK_APP_SECRET;
    static String PATH;
    static String SERVICE;

    @Autowired
    public void setHikVisionConfig(HikVisionConfig hikVisionConfig){
        HIK_HOST = hikVisionConfig.getHIK_HOST();
        HIK_APP_KEY = hikVisionConfig.getHIK_APP_KEY();
        HIK_APP_SECRET = hikVisionConfig.getHIK_APP_SECRET();
        PATH = hikVisionConfig.getPATH();
        SERVICE = hikVisionConfig.getService();
    }
}
