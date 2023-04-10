package com.steccn.authserver.config;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.steccn.authserver.constant.JwtAdditionConstant;
import com.steccn.authserver.util.SteccnUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
public class CustomJwtAccessTokenConverter extends JwtAccessTokenConverter {

    @Override
    public OAuth2Authentication extractAuthentication(Map<String, ?> map) {
        OAuth2Authentication authentication = super.extractAuthentication(map);
        Authentication userAuthentication = authentication.getUserAuthentication();
        if (userAuthentication != null) {
            try {
                String username = (String) authentication.getPrincipal();
                String name = null;
                Long userId = null;
                String[] permissionArray = null;
                try {
                    userId = Long.parseLong(String.valueOf(map.get(JwtAdditionConstant.JWT_ADDITION_USERID)));
                    name = (String) map.get(JwtAdditionConstant.JWT_ADDITION_NAME);
                    String pwStr = (String) map.get("pwStr");
                    JSONArray jsonArray = JSONUtil.parseArray(pwStr);
                    permissionArray = new String[jsonArray.size()];
                    for (int i = 0; i < jsonArray.size(); i++) {
                        permissionArray[i] = (String) jsonArray.get(i);
                    }
                } catch (Exception ee) {
                    log.error(ee.getLocalizedMessage());
                }
                //如果没有权限，就返回空集合
                if(Objects.isNull(permissionArray)){
                    permissionArray = new String[]{};
                }
                List<GrantedAuthority> authorityList = AuthorityUtils.createAuthorityList(permissionArray);
                SteccnUser steccnUser = new SteccnUser(userId, name, username, "N/A", authorityList);
                userAuthentication = new UsernamePasswordAuthenticationToken(steccnUser,
                        userAuthentication.getCredentials(), authorityList);
            } catch (Exception e) {
                log.error(e.getLocalizedMessage());
            }
        }
        return new OAuth2Authentication(authentication.getOAuth2Request(), userAuthentication);
    }
}