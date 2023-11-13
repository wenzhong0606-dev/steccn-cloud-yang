package com.steccn.authserver.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.config.annotation.builders.InMemoryClientDetailsServiceBuilder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;
import org.springframework.security.oauth2.provider.token.TokenEnhancerChain;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;

import java.util.ArrayList;
import java.util.List;

/**
 * @description: 授权服务器的配置
 * 用来对资源拥有者的身份进行认证、对访问资源进行授权。客户端要想访问资源需要通过认证服务器由资源拥有者授权后可访问。
 */
@Configuration
@EnableAuthorizationServer
public class AuthorizationServerConfig extends AuthorizationServerConfigurerAdapter {

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserDetailsService userDetailsService;

    @Autowired
    @Qualifier("jwtTokenStore")
    TokenStore tokenStore;

    @Autowired
    JwtAccessTokenConverter jwtAccessTokenConverter;

    @Autowired
    JwtTokenEnhancer jwtTokenEnhancer;

    /**
     * 密码模式是直接将我们的密码传给授权服务器
     * 使用密码所需要的配置
     *
     * @param endpoints
     */
    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
        //配置Jwt内容增强器
        TokenEnhancerChain tokenEnhancerChain = new TokenEnhancerChain();
        List<TokenEnhancer> delegates = new ArrayList<>();
        delegates.add(jwtTokenEnhancer);
        delegates.add(jwtAccessTokenConverter);
        tokenEnhancerChain.setTokenEnhancers(delegates);
        endpoints.authenticationManager(authenticationManager)
                .userDetailsService(userDetailsService)
                //配置使用redis存储token 令牌
                //配置存储令牌策略
                .tokenStore(tokenStore)
                //accessToken要和JwtToken进行相互转换
                .accessTokenConverter(jwtAccessTokenConverter)
                .tokenEnhancer(tokenEnhancerChain);
    }


    /**
     * 授权服务器的4个端点
     * * - `Authorize Endpoint` ：授权端点，进行授权
     * * - `Token Endpoint` ：令牌端点，进过授权拿到对应的Token
     * * - `Introspection Endpoint`：校验端点，校验Token的合法性
     * * - `Revocat ion Endpoint` ：撤销端点，撒销授权
     *
     * @param clients
     */
    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
        InMemoryClientDetailsServiceBuilder builder = clients.inMemory();
        builder
                //配置client Id
                .withClient("admin")
                //client-secret
                .secret(passwordEncoder.encode("admin"))
                //配置访问token的有效期设置为1个月
                .accessTokenValiditySeconds(3600 * 24 * 30)
                //配置重定向的跳转，用于授权成功之后的跳转
                .redirectUris("http://127.0.0.1:9001/api/portal/login")
                //作用域
                .scopes("all")
                //Grant_type  授权码模式
                // 密码模式和刷新令牌模式
                .authorizedGrantTypes("password", "refresh_token", "authorization_code");
        builder
                //配置client Id
                .withClient("temp")
                //client-secret
                .secret(passwordEncoder.encode("steccn_temp"))
                //配置访问token的有效期设置为30分钟
                .accessTokenValiditySeconds(7200)
                //配置重定向的跳转，用于授权成功之后的跳转
                .redirectUris("http://127.0.0.1:9001/api/portal/login")
                //作用域
                .scopes("all")
                //Grant_type  授权码模式
                // 密码模式和刷新令牌模式
                .authorizedGrantTypes("password", "refresh_token", "authorization_code");
    }

}
