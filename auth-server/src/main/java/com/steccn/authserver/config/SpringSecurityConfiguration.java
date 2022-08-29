package com.steccn.authserver.config;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.digest.MD5;
import com.steccn.authserver.authentication.OAAuthenticationSecurityConfig;
import com.steccn.authserver.service.CustomUserDetailService;
import org.bouncycastle.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
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
    public UserDetailsService userDetailsService() {
        CustomUserDetailService customUserDetailService = new CustomUserDetailService();

        return customUserDetailService;
    }
//    public UserDetailsService users(PasswordEncoder encoder) {
//
//        UserDetails user = User.withUsername("user")
//                .password(encoder.encode("password"))
//                .roles("USER")
//                .build();
//        return new InMemoryUserDetailsManager(user);
//
//    }

//    @Bean
//    BCryptPasswordEncoder passwordEncoder() {
//        return new BCryptPasswordEncoder();
//    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new PasswordEncoder() {
            @Override
            public String encode(CharSequence rawPassword) {
                String s = SecureUtil.md5((String) rawPassword);
                return Strings.toUpperCase(s);
            }

            @Override
            public boolean matches(CharSequence rawPassword, String encodedPassword) {
                if (StrUtil.isBlank(encodedPassword)) {
                    throw new IllegalArgumentException("rawPassword is blank");
                }
                return encodedPassword.equalsIgnoreCase((String) rawPassword);
            }
        };
    }

//    public static void main(String[] args) {
//        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
//        String ari = encoder.encode("Ari");
//        boolean ari1 = encoder.matches("Ari", ari);
//        System.out.println(ari);
//        System.out.println(ari1);
//
//
//    }

//
//    public static void main(String[] args) {
//        String str = "123456";
//        String md5Str = SecureUtil.md5(str);
//        System.out.println(md5Str);
//    }
}
