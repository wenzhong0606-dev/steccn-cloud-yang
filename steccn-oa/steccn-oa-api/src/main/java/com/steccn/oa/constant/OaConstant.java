package com.steccn.oa.constant;

import com.steccn.oa.config.OaConfig;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

/**
 * oa常量
 *
 * @author AndrewLuo
 * @date 2022/09/02
 */
public class OaConstant {
    /**
     * checklogin json
     */
    public static final String OA_JSON_LOGIN_STATUS = "loginstatus";
    public static final String OA_JSON_USER_ID = "userid";
    public static final String OA_JSON_MSG_CODE = "msgcode";
    public static final String OA_JSON_MSG = "msg";
    public static final String OA_JSON_DATA = "data";
    public static final String OA_JSON_DATA_LIST = "dataList";
    public static final String OA_CAPITAL_DATA = "Data";
    public static final String OA_JSON_DATAS = "datas";
    public static final String OA_JSON_STATUS = "status";
    public static final String OA_JSON_TOKEN = "token";
    public static final String OA_JSON_CHILD = "child";
    public static final String OA_JSON_PID = "pid";
    public static final String OA_JSON_ID = "id";
    public static final String OA_JSON_NAME = "name";
    public static final String OA_JSON_URL = "url";
    public static final String OA_JSON_DEPARTMENTNAME = "departmentname";
    public static final String OA_JSON_ICON = "icon";
    public static final String OA_JSON_CODE = "code";
    public static final String OA_JSON_ERRMSG = "errMsg";
    public static final String OA_WORKFLOW_SUCCESS = "SUCCESS";
    public static final String OA_WORKFLOW_PARAM_ERROR = "PARAM_ERROR";
    public static final String OA_WORKFLOW_REQUESTID = "requestid";
    public static final String OA_JSON_GROUPITEM1 = "groupitem1";
    public static final String OA_JSON_GROUPITEM3 = "groupitem3";
    public static final String OA_JSON_VALUE = "value";
    public static final String OA_JSON_ITEMS = "items";
    public static final String OA_JSON_IMPORTDOCID = "importdocid";


    /**
     * 泛微oa常量
     */
    public static final String OA_SSO_APPID = OaConfigHelper.OA_SSO_APPID;
    public static final String OA_API_APPID = OaConfigHelper.OA_API_APPID;
    public static final String OA_API_SECRET = OaConfigHelper.OA_API_SECRET;
    public static final String OA_SPK = OaConfigHelper.OA_SPK;
    public static final String OA_SYSTEM_USERID = OaConfigHelper.OA_SYSTEM_USERID;
    public static final String OA_WEAVER_SITE = OaConfigHelper.OA_WEAVER_SITE;
    public static final String OA_RIGHT_STRING = OaConfigHelper.OA_RIGHT_STRING;
    public static final String OA_BASIC_AUTH = OaConfigHelper.OA_BASIC_AUTH;
    public static final Integer YES = 1;
    public static final Integer NO = 0;
    public static final Integer NONE = -1;
    public static final String OA_SESSION_KEY = "sessionkey";

    /**
     * 时 分 秒 天
     */
    public static final String BEFORE_MINUTES = "分钟之前";
    public static final String BEFORE_HOURS = "小时之前";
    public static final String BEFORE_DAYS = "天之前";
    public static final String ONE = "1";
    public static final String TWO = "2";
    public static final int SIXTY = 60;


    public static final String GROUP_LEADER = "班长";
}

/**
 * 把配置转成静态属性
 */
@Component
class OaConfigHelper{
    static String OA_SSO_APPID;
    static String OA_API_APPID;
    static String OA_API_SECRET;
    static String OA_SPK;
    static String OA_SYSTEM_USERID;
    static String OA_WEAVER_SITE;
    static String OA_RIGHT_STRING;
    static String OA_BASIC_AUTH;

    @Autowired
    public void setOaConfig(OaConfig oaConfig){
        OA_SSO_APPID = oaConfig.getOA_SSO_APPID();
        OA_API_APPID = oaConfig.getOA_API_APPID();
        OA_API_SECRET = oaConfig.getOA_API_SECRET();
        OA_SPK = oaConfig.getOA_SPK();
        OA_SYSTEM_USERID = oaConfig.getOA_SYSTEM_USERID();
        OA_WEAVER_SITE = oaConfig.getOA_WEAVER_SITE();
        OA_RIGHT_STRING = oaConfig.getOA_RIGHT_STRING();
        OA_BASIC_AUTH = oaConfig.getOA_BASIC_AUTH();
    }
}
