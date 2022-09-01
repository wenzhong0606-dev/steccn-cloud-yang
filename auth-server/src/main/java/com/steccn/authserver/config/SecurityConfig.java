package com.steccn.authserver.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * @author wanglufei
 * @description: SpringSecurity配置类
 * @date 2022/4/11/6:31 PM
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    /**
     * OAuth2AuthenticationManager 来处理我们密码模式的密码
     *
     * @return org.springframework.security.authentication.AuthenticationManager
     */
    @Bean
    public AuthenticationManager authenticationManager() throws Exception {
        return super.authenticationManager();
    }

    /**
     * 自定义加密逻辑
     *
     * @return org.springframework.security.crypto.password.PasswordEncoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 自定义web相关的属性
     *
     * @param http
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        //关闭CSRF防护
        http.csrf().disable()
                //授权
                .authorizeRequests()
                .antMatchers("/oauth/**", "/login/**", "/logout/**")
                .permitAll()
                .anyRequest().authenticated()
                .and()
                .formLogin()
                .permitAll();

    }
}
