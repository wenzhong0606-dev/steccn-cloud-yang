package com.steccn.portal.service;

import com.steccn.common.vo.R;
import com.steccn.portal.constant.TokenConstant;
import com.steccn.portal.vo.SteccnTokenVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.Map;

/**
 * @desc 登录服务
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class LoginService {

    private final RemoteTokenApi remoteTokenApi;

    /**
     * 登录模块
     *
     * @param username
     * @param password
     * @return
     */
    public R<SteccnTokenVO> login(String username, String password) {
        //先去我们系统认证
        Object token;
        Map<String, String> map = null;
        boolean flag = Boolean.FALSE;
        try {
            token = remoteTokenApi.getToken(TokenConstant.GRANT_TYPE_PASSWORD, password, username, TokenConstant.CLIENT_ID, TokenConstant.SCOPE);
            flag = Boolean.TRUE;
            map = (Map<String, String>) token;
        } catch (Exception e) {
            log.error(e.getLocalizedMessage());
        }
        if (flag) {
            //表示登录成功
            SteccnTokenVO steccnTokenVO = new SteccnTokenVO();
            steccnTokenVO.setAccessToken(map.get(TokenConstant.STR_ACCESS_TOKEN));
            steccnTokenVO.setTokenType(map.get(TokenConstant.STR_TOKEN_TYPE));
            steccnTokenVO.setRefreshToken(map.get(TokenConstant.STR_REFRESH_TOKEN));
            steccnTokenVO.setExpiresIn(Long.valueOf(String.valueOf(map.get(TokenConstant.STR_EXPIRES_IN))));
            steccnTokenVO.setScope(map.get(TokenConstant.STR_SCOPE));
            //再去获取oa的token
            //再去获取mess的token
            return R.success(steccnTokenVO);
        } else {
            return R.failure("用户名密码错误");
        }
    }
}
