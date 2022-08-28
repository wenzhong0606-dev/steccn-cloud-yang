package com.steccn.authserver.service;

import com.steccn.usercenter.feign.UserInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

/**
 * 类说明
 *
 * @author Jinglihui
 * @date 2022/8/8
 */
@Slf4j
public class CustomUserDetailService implements UserDetailsService {

    @Autowired
    private UserInfoService userInfoService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
//        R r = userInfoService.getUserInfoByname(username);
//        if (Objects.isNull(r) || r.getCode() != ResultCode.SUCCESS.getCode()) {
//            throw new UsernameNotFoundException("查询用户" + username + "出错。");
//        }
//        Map<String, Object> map = (Map<String, Object>) r.getData();
//        UserDTO userInfo = new UserDTO();
//        BeanUtil.fillBeanWithMap(map, userInfo, true);
//        if (userInfo == null) {
//            throw new UsernameNotFoundException("查无此人：" + username);
//        }
//        log.info("#### passowrd is {}", userInfo.getPassword());
//        return User.withUsername(userInfo.getUsername())
//                .password(userInfo.getPassword())
//                .roles(userInfo.getRoles().toString())
//                .build();
        return null;
    }


}
