package com.steccn.oa.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 获取oa配置数据
 *
 * @author AndrewLuo
 * @date 2022/11/22
 */
@Component
@Data
public class OaConfig {
    @Value("${oa.sso-appid}")
    private String OA_SSO_APPID;

    @Value("${oa.api-appid}")
    private String OA_API_APPID;

    @Value("${oa.api-secret}")
    private String OA_API_SECRET;

    @Value("${oa.spk}")
    private String OA_SPK;

    @Value("${oa.system-userid}")
    private String OA_SYSTEM_USERID;

    @Value("${oa.weaver-site}")
    private String OA_WEAVER_SITE;

    @Value("${oa.right-string}")
    private String OA_RIGHT_STRING;
}
