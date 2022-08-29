/**
 * 
 */
package com.steccn.authserver.authentication;

import com.steccn.authserver.service.OaApi;
import com.steccn.usercenter.feign.UserInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.SecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Component;

/**
 * @author zhailiang
 *
 */
@Component
public class OAAuthenticationSecurityConfig extends SecurityConfigurerAdapter<DefaultSecurityFilterChain, HttpSecurity> {
	@Autowired
	private UserDetailsService userDetailsService;

	@Autowired
	private UserInfoService userInfoService;

	@Autowired
	private OaApi oaApi;

	@Autowired
	private PasswordEncoder bCryptPasswordEncoder;
	
	@Override
	public void configure(HttpSecurity http) throws Exception {
		OAAuthenticationFilter oaAuthenticationFilter = new OAAuthenticationFilter();
		oaAuthenticationFilter.setAuthenticationManager(http.getSharedObject(AuthenticationManager.class));
		oaAuthenticationFilter.setUserInfoService(userInfoService);
		oaAuthenticationFilter.setBCryptPasswordEncoder(bCryptPasswordEncoder);
		oaAuthenticationFilter.setOaApi(oaApi);

		OAAuthenticationProvider oaAuthenticationProvider = new OAAuthenticationProvider();
		oaAuthenticationProvider.setUserDetailsService(userDetailsService);
		oaAuthenticationProvider.setUserInfoService(userInfoService);
		
		http.authenticationProvider(oaAuthenticationProvider)
			.addFilterBefore(oaAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
				.csrf().disable();
	}

}
