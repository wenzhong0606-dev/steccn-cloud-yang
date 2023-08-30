package com.steccn.platform.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.steccn.authserver.exception.BusinessException;
import com.steccn.authserver.util.SecurityUtils;
import com.steccn.platform.constant.OaDataConstant;
import com.steccn.platform.feign.RemoteSafetyFeignApi;
import com.steccn.platform.feign.RemoteSrmFeignApi;
import com.steccn.platform.service.SrmTokenService;
import com.steccn.platform.vo.SafetyTokenVO;
import com.steccn.platform.vo.SrmTokenVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * 获取srmToken
 *
 * @author AndrewLuo
 * @date 2023/08/24
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SrmTokenServiceImpl implements SrmTokenService {
    private final RemoteSrmFeignApi remoteSrmFeignApi;
    @Override
    public SrmTokenVO getSrmToken() {
        String token = remoteSrmFeignApi.getToken(SecurityUtils.getUser().getUsername(),"sso-client","implicit");
        SrmTokenVO srmTokenVO = JSONObject.parseObject(token).toJavaObject(SrmTokenVO.class);
        if(!srmTokenVO.getMessage().equals(OaDataConstant.SUCCESS)){
            log.info(token);
            throw new BusinessException("获取SRM系统token失败");
        }
        return srmTokenVO;
    }

    @Override
    @Cacheable(cacheNames = "SrmOauthToken#82800")
    public String getSrmOauthToken() {
        String token = remoteSrmFeignApi.getOauthToken();
        String accessToken = JSONObject.parseObject(token).getString("access_token");
        return accessToken;
    }
}
