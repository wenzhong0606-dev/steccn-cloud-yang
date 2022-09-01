/**
 * 
 */
package com.steccn.authserver.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * @author zhailiang
 *
 */
@Component
public class MyUserDetailsService implements UserDetailsService {

	private Logger logger = LoggerFactory.getLogger(getClass());
	
	@Autowired
	private PasswordEncoder passwordEncoder;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		logger.info("表单登录用户名:" + username);
		return User.withUsername(username).password(passwordEncoder.encode("123456")).authorities("admin").build();
	}

}
