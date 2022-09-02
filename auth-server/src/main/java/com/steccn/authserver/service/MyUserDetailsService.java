/**
 *
 */
package com.steccn.authserver.service;

import com.steccn.common.constant.ResultCode;
import com.steccn.common.vo.R;
import com.steccn.oa.dto.OaUserDTO;
import com.steccn.oa.feign.OaInfoService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

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

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("表单登录用户名:" + username);
        R<OaUserDTO> r = oaInfoService.loadByUsername(username);
        if (Objects.isNull(r) || ResultCode.SUCCESS.getCode() != r.getCode()) {
            throw new UsernameNotFoundException("查无此人");
        }
        OaUserDTO userDTO = r.getData();
        return User.withUsername(userDTO.getUsername()).password(userDTO.getPassword()).authorities("admin").build();
    }
}
