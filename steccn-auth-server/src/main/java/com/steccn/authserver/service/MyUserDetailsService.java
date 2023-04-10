/**
 *
 */
package com.steccn.authserver.service;

import cn.hutool.crypto.digest.MD5;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.nacos.common.utils.MD5Utils;
import com.steccn.authserver.util.SteccnUser;
import com.steccn.common.constant.ResultCode;
import com.steccn.common.vo.R;
import com.steccn.oa.dto.OaUserDTO;
import com.steccn.oa.feign.OaInfoService;
import com.steccn.platform.feign.RemoteMesFeignApi;
import com.steccn.pms.feign.RemoteMessFeignApi;
import com.steccn.pms.vo.MessScopeVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * @author zhailiang
 *
 */
@Component
@Slf4j
public class MyUserDetailsService implements UserDetailsService {


    @Autowired
    private OaInfoService oaInfoService;

    @Autowired
    private RemoteMessFeignApi remoteMessFeignApi;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        R<OaUserDTO> r = oaInfoService.loadByUsername(username);
        if (Objects.isNull(r) || ResultCode.SUCCESS.getCode() != r.getCode()) {
            throw new UsernameNotFoundException("查无此人");
        }
        OaUserDTO userDTO = r.getData();
        MessScopeVO scopeVO = remoteMessFeignApi.getScope(username);
        List<String> scope = scopeVO.getStringList();
        String[] permissionArray =  scope.toArray(new String[scope.size()]);
        stringRedisTemplate.opsForValue().set(userDTO.getUsername(), JSON.toJSONString(permissionArray));

        return new SteccnUser(userDTO.getUserId(),
                userDTO.getName(),
                userDTO.getUsername(),
                userDTO.getPassword(),
                AuthorityUtils.createAuthorityList("admin"));
    }
}
