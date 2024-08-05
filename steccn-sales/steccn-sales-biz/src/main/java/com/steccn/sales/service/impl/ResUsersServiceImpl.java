package com.steccn.sales.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Sets;
import com.steccn.authserver.exception.BusinessException;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.common.constant.FeignConstant;
import com.steccn.common.constant.ResultCode;
import com.steccn.common.vo.R;
import com.steccn.oa.dto.OaUserInfo;
import com.steccn.platform.dto.SysUserClientRefDTO;
import com.steccn.platform.vo.SysOfficeVO;
import com.steccn.portal.constant.TokenConstant;
import com.steccn.portal.dto.LoginMesUserInfoDTO;
import com.steccn.portal.service.RemoteTokenApi;
import com.steccn.portal.vo.SteccnTokenVO;
import com.steccn.sales.domain.ResPartner;
import com.steccn.sales.domain.ResUsers;
import com.steccn.sales.service.ResPartnerService;
import com.steccn.sales.service.ResUsersService;
import com.steccn.sales.mapper.ResUsersMapper;
import com.steccn.sales.vo.ResUserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.time.Duration;
import java.util.Base64;
import java.util.Map;
import java.util.Objects;

/**
* @author Admin
* @description 针对表【res_users】的数据库操作Service实现
* @createDate 2024-08-05 09:47:57
*/
@Service
@RequiredArgsConstructor
@RefreshScope
public class ResUsersServiceImpl extends ServiceImpl<ResUsersMapper, ResUsers>
    implements ResUsersService{
    private final RemoteTokenApi remoteTokenApi;
    private final ResPartnerService resPartnerService;

    @Override
    public SteccnTokenVO login(String username, String password, String clientNo, Integer loginStatus) {
        //查询查到盐和密码相关
        ResUsers resUsers = getOne(new LambdaQueryWrapper<ResUsers>().eq(ResUsers::getLogin, username).isNotNull(ResUsers::getPassword).last("LIMIT 1"));
        if(Objects.isNull(resUsers)){
            throw new BusinessException("用户名密码错误");
        }
        //由于是销售系统,加标识
        username = username + "$sales";
        //密码加盐
        String[] passSplit = resUsers.getPassword().split("\\$");
        int iterations = Integer.parseInt(passSplit[2]);
        int keyLength = 512;
        byte[] salt = Base64.getDecoder().decode(passSplit[3].replace(".", "+"));
        String hash = hashPassword(password, salt, iterations, keyLength);
        password = hash.replace("+",".");
        //1.定义变量
        //自己系统的token返回
        Object token = null;
        //返回vo
        SteccnTokenVO steccnTokenVO = null;
        //自己系统返回的token以map接收数据
        Map<String, String> map;
        //成功标识【全部成功才算成功】
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
        return steccnTokenVO;
    }

    @Override
    public ResUserVO userInfo(String userName) {
        ResUsers resUsers = getOne(new LambdaQueryWrapper<ResUsers>().eq(ResUsers::getLogin, userName).isNotNull(ResUsers::getPassword).last("LIMIT 1"));
        ResUserVO vo = new ResUserVO();
        if(Objects.nonNull(resUsers)){
            ResPartner partner = resPartnerService.getOne(new LambdaQueryWrapper<ResPartner>().eq(ResPartner::getId, resUsers.getPartnerId()).last("LIMIT 1"));
            vo.setUserId(resUsers.getId().longValue());
            vo.setPassword(resUsers.getPassword());
            vo.setUsername(resUsers.getLogin());
            vo.setLogisticsCompanyId(resUsers.getLogisticsCompanyId());
            vo.setName(partner.getName());
        }
        return vo;
    }

    public static String hashPassword(String password, byte[] salt, int iterations, int keyLength) {
        try {
            PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterations, keyLength);
            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");
            byte[] hash = skf.generateSecret(spec).getEncoded();
            return Base64.getEncoder().withoutPadding().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}




