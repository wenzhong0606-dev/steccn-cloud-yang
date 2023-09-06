package com.steccn.portal.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.steccn.authserver.exception.BusinessException;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.common.constant.FeignConstant;
import com.steccn.common.constant.ResultCode;
import com.steccn.common.vo.R;
import com.steccn.oa.dto.OaLoginParam;
import com.steccn.oa.dto.OaLoginResult;
import com.steccn.oa.dto.OaSsoTokenResult;
import com.steccn.oa.dto.OaUserInfo;
import com.steccn.oa.feign.OaInfoService;
import com.steccn.platform.dto.SysUserClientRefDTO;
import com.steccn.platform.feign.RemoteSysUserClientFeignApi;
import com.steccn.portal.constant.TokenConstant;
import com.steccn.portal.feign.RemotePortalMesFeignApi;
import com.steccn.portal.vo.SteccnTokenVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Objects;

/**
 * @author yequan
 * @desc 登录服务
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class LoginService {

    private final RemoteTokenApi remoteTokenApi;
    private final OaInfoService oaInfoService;
    private final MesMenuService mesMenuService;
    private final RemotePortalMesFeignApi remotePortalMesFeignApi;
    private final RemoteSysUserClientFeignApi remoteSysUserClientFeignApi;

    /**
     * 登录模块
     *
     *
     * @param username 用户名
     * @param password 密码
     * @param clientNo 移动端唯一标识
     * @return
     */
    public R<SteccnTokenVO> login(String username, String password,String clientNo,Integer loginStatus) {
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

            String userCode = remotePortalMesFeignApi.getUserCode(username);
            steccnTokenVO.setUsercode(userCode);

//            //3.用户oa登录
//            OaLoginParam oaLoginParam = new OaLoginParam();
//            oaLoginParam.setLoginid(username);
//            oaLoginParam.setUserpassword(password);
//            R r = oaInfoService.checkLogin(oaLoginParam);
//            Map oaMap = (Map) r.getData();
//            OaLoginResult oaLoginResult = new OaLoginResult();
//            BeanUtil.fillBeanWithMap(oaMap, oaLoginResult, true);
//            steccnTokenVO.setOaUserId(oaLoginResult.getUserId());
//            //4.如果oa登录失败，则直接跑异常
//            if (Objects.isNull(oaLoginResult) || !oaLoginResult.getLoginStatus()) {
//                log.error("r -> {}",r);
//                throw new BusinessException("OA用户登录失败");
//            }
            //将用户和clientNo绑定起来,用于push消息
            if(StrUtil.isNotBlank(clientNo)){
                SysUserClientRefDTO sysUserClientRefDTO = new SysUserClientRefDTO();
                sysUserClientRefDTO.setClientNo(clientNo);
                sysUserClientRefDTO.setUserId(steccnTokenVO.getOaUserId().longValue());
                sysUserClientRefDTO.setUsername(steccnTokenVO.getUsername());
                Boolean aBoolean = FeignOptional.of(() -> remoteSysUserClientFeignApi.bind(sysUserClientRefDTO), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
                log.info("移动端绑定结果 -> {}", aBoolean);
            }
//            //5.获取ssotoken
//            R ssoToken = oaInfoService.getSsoToken(String.valueOf(username));
//            Map ssoMap = (Map) ssoToken.getData();
//            OaSsoTokenResult oaSsoTokenResult = new OaSsoTokenResult();
//            BeanUtil.fillBeanWithMap(ssoMap, oaSsoTokenResult, true);
//            if (Objects.isNull(oaSsoTokenResult) || !oaSsoTokenResult.isStatus()) {
//                log.error(" ssoToken -> {} ", ssoToken);
//                throw new BusinessException("OA用户获取ssotoken失败");
//            }
//            steccnTokenVO.setOaToken(oaSsoTokenResult.getToken());
//            //6.获取mess的token
//            String mesToken = mesMenuService.getMesToken(username);
//            steccnTokenVO.setMessToken(mesToken);
            flag = Boolean.TRUE;
        } catch (Exception e) {
            log.error(e.getLocalizedMessage());
            throw new BusinessException(e.getLocalizedMessage());
        }
        if (flag) {
            return R.success(steccnTokenVO);
        } else {
            return R.failure("用户名密码错误");
        }
    }
}
