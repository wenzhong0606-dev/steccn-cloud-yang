/**
 *
 */
package com.steccn.authserver.service;

import com.alibaba.fastjson.JSON;
import com.steccn.authserver.util.SteccnUser;
import com.steccn.common.constant.ResultCode;
import com.steccn.common.vo.R;
import com.steccn.oa.dto.OaUserDTO;
import com.steccn.oa.feign.OaInfoService;
import com.steccn.platform.feign.RemotePlatformUserFeignApi;
import com.steccn.platform.vo.PlatformUserVO;
import com.steccn.pms.feign.RemoteMessFeignApi;
import com.steccn.pms.vo.MessScopeVO;
import com.steccn.sales.feign.RemoteSaleFeignApi;
import com.steccn.sales.vo.ResUserVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

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

    @Autowired
    private RemoteSaleFeignApi remoteSaleFeignApi;

    @Autowired
    private RemotePlatformUserFeignApi remotePlatformUserFeignApi;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        if(username.contains("$sales")){
            username = username.split("\\$")[0];
            ResUserVO resUserVO = remoteSaleFeignApi.salesUserInfo(username).getData();
            if (Objects.isNull(resUserVO)) {
                throw new UsernameNotFoundException("查无此人");
            }
            String desPassword = resUserVO.getPassword().split("\\$")[4];
            return new SteccnUser(resUserVO.getUserId(),
                    resUserVO.getName(),
                    username,
                    desPassword,
                    AuthorityUtils.createAuthorityList("admin"));
        }else {
            PlatformUserVO r = remotePlatformUserFeignApi.userInfoByUserName(username).getData();
            if (Objects.isNull(r.getUserName())) {
                throw new UsernameNotFoundException("查无此人");
            }
            List<String> scope = r.getUserScope();
            String[] permissionArray =  scope.toArray(new String[scope.size()]);
            stringRedisTemplate.opsForValue().set(r.getUserName(), JSON.toJSONString(permissionArray));
            return new SteccnUser(r.getId(),
                    r.getName(),
                    r.getUserName(),
                    r.getPassword(),
                    AuthorityUtils.createAuthorityList("admin"));
        }
    }
}
