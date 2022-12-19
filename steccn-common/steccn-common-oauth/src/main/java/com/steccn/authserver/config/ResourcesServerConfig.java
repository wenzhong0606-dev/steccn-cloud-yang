package com.steccn.authserver.config;

import com.google.common.collect.Lists;
import com.steccn.authserver.filter.WhiteListFilter;
import com.steccn.authserver.path.IgnorenPath;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.SecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.authentication.BearerTokenExtractor;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationProcessingFilter;
import org.springframework.security.web.DefaultSecurityFilterChain;
import java.util.List;

/**
 * @description: 资源服务器的配置
 * 通常为用户，也可以是应用程序，既该资源的拥有者。
 */
@Configuration
@EnableResourceServer
public class ResourcesServerConfig extends ResourceServerConfigurerAdapter {

    @Autowired
    private IgnorenPath ignorenPath;

    @Override
    public void configure(HttpSecurity http) throws Exception {
        String[] allIgnoredPath = ignorenPath.getAllIgnoredPath();
        List<String> list = Lists.newArrayList(allIgnoredPath);
        list.add("/v3/api-docs");
        http.apply(new SecurityConfigurerAdapter<DefaultSecurityFilterChain, HttpSecurity>() {
            @Override
            public void configure(HttpSecurity builder) {
                builder.addFilterBefore(new WhiteListFilter(ignorenPath) , OAuth2AuthenticationProcessingFilter.class);
            }
        })
        .and().authorizeRequests().antMatchers(list.toArray(new String[list.size()])).permitAll().anyRequest().authenticated();

    }

    @Override
    public void configure(ResourceServerSecurityConfigurer resources) {
        BearerTokenExtractor bearerTokenExtractor = new BearerTokenExtractor();
        resources.tokenExtractor(bearerTokenExtractor);
    }
}
