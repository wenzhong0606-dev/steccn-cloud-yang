package com.steccn.authserver.config;

import com.steccn.authserver.constant.JwtAdditionConstant;
import com.steccn.authserver.util.SteccnUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import java.util.Collection;
import java.util.Map;

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
                try {
                    userId = Long.parseLong(String.valueOf(map.get(JwtAdditionConstant.JWT_ADDITION_USERID)));
                    name = (String) map.get(JwtAdditionConstant.JWT_ADDITION_NAME);
                } catch (Exception ee) {
                    log.error(ee.getLocalizedMessage());
                }
                Collection<? extends GrantedAuthority> authorities = userAuthentication.getAuthorities();
                SteccnUser steccnUser = new SteccnUser(userId, name, username, "N/A", AuthorityUtils.createAuthorityList("admin"));
                userAuthentication = new UsernamePasswordAuthenticationToken(steccnUser,
                        userAuthentication.getCredentials(), authorities);
            } catch (Exception e) {
                log.error(e.getLocalizedMessage());
            }
        }
        return new OAuth2Authentication(authentication.getOAuth2Request(), userAuthentication);
    }
}