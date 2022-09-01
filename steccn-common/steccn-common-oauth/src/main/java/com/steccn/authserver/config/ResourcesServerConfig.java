package com.steccn.authserver.config;

import com.steccn.authserver.path.IgnorenPath;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.authentication.BearerTokenExtractor;

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
//        //所有的访问都需要认证访问
//        http.authorizeRequests().anyRequest().authenticated();
//        //唯独user 可以访问 放行我们的资源
//        http.requestMatchers().antMatchers("/user/**","/portal/login");

        http.authorizeRequests().antMatchers("/portal/login").permitAll().anyRequest().authenticated();

    }

    @Override
    public void configure(ResourceServerSecurityConfigurer resources) {
        BearerTokenExtractor bearerTokenExtractor = new BearerTokenExtractor();
        resources.tokenExtractor(bearerTokenExtractor);
    }
}
