package com.steccn.portal.service;

import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Sets;
import com.steccn.authserver.exception.BusinessException;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.common.constant.FeignConstant;
import com.steccn.common.constant.ResultCode;
import com.steccn.common.vo.R;
import com.steccn.oa.dto.OaUserInfo;
import com.steccn.oa.feign.OaInfoService;
import com.steccn.platform.dto.SysUserClientRefDTO;
import com.steccn.platform.feign.RemoteOfficeFeign;
import com.steccn.platform.feign.RemoteSysUserClientFeignApi;
import com.steccn.platform.vo.SysOfficeVO;
import com.steccn.portal.constant.TokenConstant;
import com.steccn.portal.dto.LoginMesUserInfoDTO;
import com.steccn.portal.feign.RemotePortalMesFeignApi;
import com.steccn.portal.service.checkcode.CheckCodeService;
import com.steccn.portal.vo.SteccnTokenVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.Objects;

/**
 * @author yequan
 * @desc 登录服务
 */
@Service
@Slf4j
@RequiredArgsConstructor
@RefreshScope
public class LoginService {

    private final RemoteTokenApi remoteTokenApi;
    private final OaInfoService oaInfoService;
    private final RemotePortalMesFeignApi remotePortalMesFeignApi;
    private final RemoteSysUserClientFeignApi remoteSysUserClientFeignApi;
    private final RemoteOfficeFeign remoteOfficeFeign;
    private final CheckCodeService checkCodeService;
    private final RedisTemplate<String, Integer> redisTemplate;
    /**
     * 失败登录最大次数
     */
    @Value("${fail.login.count}")
    final Integer FAIL_LOGIN_MAX_COUNT = 10;

    /**
     * 失败登录最大次数
     */
    @Value("${fail.login.time}")
    final Integer FAIL_LOGIN_MAX_TIME = 30;


    /**
     * 登录模块
     *
     * @param username 用户名
     * @param password 密码
     * @param clientNo 移动端唯一标识
     * @return
     */
    public R<SteccnTokenVO> login(String username, String password, String clientNo, Integer loginStatus, String checkKey, String checkCode) {
        //校验验证码
        if (StrUtil.isNotBlank(checkKey) && StrUtil.isNotBlank(checkCode) && !checkCodeService.verify(checkKey, checkCode)) {
            throw new RuntimeException("验证码错误，请重新输入");
        }
        //校验阈值
        String redisKey = "LOGIN_FAIL_COUNT:" + username;
        Integer expire = redisTemplate.opsForValue().get(redisKey);
        if (Objects.nonNull(expire) && expire >= FAIL_LOGIN_MAX_COUNT) {
            throw new BusinessException("账号已被锁定，请"+ FAIL_LOGIN_MAX_TIME +"分钟后再试");
        }
        //1.定义变量
        //自己系统的token返回
        Object token = null;
        //返回vo
        SteccnTokenVO steccnTokenVO = null;
        //自己系统返回的token以map接收数据
        Map<String, String> map;
        //成功标识【全部成功才算成功】
        boolean flag = Boolean.FALSE;
        try {
            //2.调用自己系统的获取token信息
            try {
                if (Objects.nonNull(loginStatus) && 1 == loginStatus) {
                    token = remoteTokenApi.getTempToken(TokenConstant.GRANT_TYPE_PASSWORD, password, username, TokenConstant.CLIENT_TEMP, TokenConstant.SCOPE);
                } else {
                    token = remoteTokenApi.getToken(TokenConstant.GRANT_TYPE_PASSWORD, password, username, TokenConstant.CLIENT_ID, TokenConstant.SCOPE);
                }
            } catch (Exception e) {
                log.error(e.getLocalizedMessage());
                throw new BusinessException("用户名密码错误");
            }
            //2.1map接受token的数据
            map = (Map<String, String>) token;
            //2.2定义返回vo并且组装token的数据
            steccnTokenVO = new SteccnTokenVO();
            steccnTokenVO.setAccessToken(map.get(TokenConstant.STR_ACCESS_TOKEN));
            steccnTokenVO.setTokenType(map.get(TokenConstant.STR_TOKEN_TYPE));
            steccnTokenVO.setRefreshToken(map.get(TokenConstant.STR_REFRESH_TOKEN));
            steccnTokenVO.setExpiresIn(Long.valueOf(String.valueOf(map.get(TokenConstant.STR_EXPIRES_IN))));
            steccnTokenVO.setScope(map.get(TokenConstant.STR_SCOPE));
            steccnTokenVO.setUsername(username);
            R<OaUserInfo> oaUserInfoRes = oaInfoService.getOaUserInfo(username);
            if (Objects.isNull(oaUserInfoRes) || ResultCode.SUCCESS.getCode() != oaUserInfoRes.getCode()) {
                log.error("获取用户信息失败 -> {}", oaUserInfoRes);
                throw new BusinessException("获取用户信息失败");
            }
            OaUserInfo oaUserInfo = oaUserInfoRes.getData();
            steccnTokenVO.setDepartment(oaUserInfo.getDepartmentName());
            steccnTokenVO.setHeadImage(oaUserInfo.getHeadPortrait());
            steccnTokenVO.setRealName(oaUserInfo.getName());
            steccnTokenVO.setOaUserId(oaUserInfo.getOaUserId());

            LoginMesUserInfoDTO userInfoStr = remotePortalMesFeignApi.getUserCode(username);
            if (Objects.nonNull(userInfoStr)) {
                steccnTokenVO.setUsercode(userInfoStr.getUserCode());
                if (StrUtil.isNotBlank(userInfoStr.getOfficeCode())) {
                    Map<String, SysOfficeVO> officeMap = FeignOptional.of(() -> remoteOfficeFeign.listByCodeSet(Sets.newHashSet(userInfoStr.getOfficeCode())), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
                    steccnTokenVO.setDepartmentId(officeMap.getOrDefault(userInfoStr.getOfficeCode(), new SysOfficeVO()).getId());
                }
            }
            //将用户和clientNo绑定起来,用于push消息
            if (StrUtil.isNotBlank(clientNo)) {
                SysUserClientRefDTO sysUserClientRefDTO = new SysUserClientRefDTO();
                sysUserClientRefDTO.setClientNo(clientNo);
                sysUserClientRefDTO.setUserId(steccnTokenVO.getOaUserId().longValue());
                sysUserClientRefDTO.setUsername(steccnTokenVO.getUsername());
                Boolean aBoolean = FeignOptional.of(() -> remoteSysUserClientFeignApi.bind(sysUserClientRefDTO), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
                log.info("移动端绑定结果 -> {}", aBoolean);
            }
            flag = Boolean.TRUE;
        } catch (Exception e) {
            log.error(e.getLocalizedMessage());
            //失败次数+1
            redisTemplate.opsForValue().increment(redisKey);
            redisTemplate.expire(redisKey, Duration.ofMinutes(FAIL_LOGIN_MAX_TIME));
            throw new BusinessException(e.getLocalizedMessage());
        }
        if (flag) {
            //删除
            redisTemplate.delete(redisKey);
            return R.success(steccnTokenVO);
        } else {
            //失败次数+1
            redisTemplate.opsForValue().increment(redisKey);
            redisTemplate.expire(redisKey, Duration.ofMinutes(FAIL_LOGIN_MAX_TIME));
            return R.failure("用户名密码错误");
        }
    }
}
