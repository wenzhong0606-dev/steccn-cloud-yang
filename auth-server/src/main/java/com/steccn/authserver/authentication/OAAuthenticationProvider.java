/**
 * 
 */
package com.steccn.authserver.authentication;

import com.steccn.usercenter.feign.UserInfoService;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

/**
 * @author zhailiang
 *
 */
public class OAAuthenticationProvider implements AuthenticationProvider {

	private UserDetailsService userDetailsService;

	private UserInfoService userInfoService;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.security.authentication.AuthenticationProvider#
	 * authenticate(org.springframework.security.core.Authentication)
	 */
	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {

		OAAuthenticationToken authenticationToken = (OAAuthenticationToken) authentication;
		
		UserDetails user = userDetailsService.loadUserByUsername((String) authenticationToken.getPrincipal());

		if (user == null) {
//			throw new InternalAuthenticationServiceException("无法获取用户信息");
			//前面oa验证成功，插入数据库之后，这里可以抛异常
			user =  User.withUsername((String)authentication.getPrincipal())
					.password("123445")
					.authorities("admin")
					.build();
		}
		OAAuthenticationToken authenticationResult = new OAAuthenticationToken(user, user.getAuthorities());
		authenticationResult.setDetails(authenticationToken.getDetails());
		return authenticationResult;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.security.authentication.AuthenticationProvider#
	 * supports(java.lang.Class)
	 */
	@Override
	public boolean supports(Class<?> authentication) {
		return OAAuthenticationToken.class.isAssignableFrom(authentication);
	}

	public UserDetailsService getUserDetailsService() {
		return userDetailsService;
	}

	public void setUserDetailsService(UserDetailsService userDetailsService) {
		this.userDetailsService = userDetailsService;
	}

	public void setUserInfoService(UserInfoService userInfoService) {
		this.userInfoService = userInfoService;
	}
}
