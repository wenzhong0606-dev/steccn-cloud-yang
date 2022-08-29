package com.steccn.authserver.config;

import com.steccn.authserver.authentication.OAAuthenticationSecurityConfig;
import com.steccn.authserver.service.CustomUserDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * 类说明
 *
 * @author Jinglihui
 * @date 2022/7/4
 */
@EnableWebSecurity(debug = false)
public class SpringSecurityConfiguration {

    @Autowired
    private OAAuthenticationSecurityConfig oaAuthenticationSecurityConfig;

    @Bean
    SecurityFilterChain defaultSecurityFilterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity.authorizeHttpRequests(
                        authorizeRequests -> authorizeRequests
                                .antMatchers("/login", "/actuator/health").permitAll()
                                .anyRequest().authenticated()
                )
                .formLogin(Customizer.withDefaults());
        httpSecurity.apply(oaAuthenticationSecurityConfig);
        return httpSecurity.build();
    }

    /**
     * 方法说明
     * 配置UserDetails
     *
     * @return
     * @author Jinglihui
     * @date 2022/7/20 16:26
     */
    @Bean
    public UserDetailsService userDetailsService(){
        CustomUserDetailService customUserDetailService = new CustomUserDetailService();

        return customUserDetailService;
    }

    @Bean
    BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
