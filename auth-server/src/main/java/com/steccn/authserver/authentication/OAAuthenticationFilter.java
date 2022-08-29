/**
 *
 */
package com.steccn.authserver.authentication;

import com.alibaba.fastjson.JSONObject;
import com.steccn.authserver.dto.OaLoginParam;
import com.steccn.authserver.dto.OaLoginResult;
import com.steccn.authserver.service.OaApi;
import com.steccn.common.constant.Constant;
import com.steccn.common.vo.R;
import com.steccn.usercenter.dto.UserSaveDTO;
import com.steccn.usercenter.feign.UserInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.util.Assert;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Objects;

/**
 * @author zhailiang
 *
 */
@Slf4j
public class OAAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

    private UserInfoService userInfoService;

    private OaApi oaApi;

    private PasswordEncoder bCryptPasswordEncoder;

    /**
     * 默认的OA登录请求处理url
     */
    public static final String DEFAULT_LOGIN_PROCESSING_URL_OA = "/login";

    private String usernameParameter = "username";
    private String passwordParameter = "password";
    private boolean postOnly = true;

    // ~ Constructors
    // ===================================================================================================

    public OAAuthenticationFilter() {
        super(new AntPathRequestMatcher(DEFAULT_LOGIN_PROCESSING_URL_OA, "POST"));
    }

    // ~ Methods
    // ========================================================================================================

    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {
        if (postOnly && !request.getMethod().equals("POST")) {
            throw new AuthenticationServiceException("Authentication method not supported: " + request.getMethod());
        }
        String username = request.getParameter(usernameParameter);
        String password = request.getParameter(passwordParameter);
        //去OA验证，如果成功，插入数据库中
        log.info("oaApi ->{}", oaApi);
        log.info("userInfoService ->{}", userInfoService);
        OaLoginParam oaLoginParam = new OaLoginParam();
        oaLoginParam.setLoginid(username);
        oaLoginParam.setUserpassword(password);
        String encode = bCryptPasswordEncoder.encode(password);
        log.info("encode -> {}",encode);
        
        String s = oaApi.checkLogin(oaLoginParam);
        log.info("s ->{}", s);
        OaLoginResult oaLoginResult = JSONObject.parseObject(s, OaLoginResult.class);
        if (Objects.nonNull(oaLoginResult) && Constant.OA_SUCCESS_CODE.equals(oaLoginResult.getMsgCode())) {
            //如果不为空，并且成功，会去判断数据库的用户表中是否存在用户，如果不存在则直接插入数据库
            UserSaveDTO userSaveDTO = new UserSaveDTO();
            userSaveDTO.setUsername(username);
            userSaveDTO.setPassword(password);
            R<Boolean> booleanR = userInfoService.saveUserInfo(userSaveDTO);
            log.info("booleanR -> {}", booleanR);
        }else{
            throw new AuthenticationServiceException(oaLoginResult.getMsg());
        }
        OAAuthenticationToken authRequest = new OAAuthenticationToken(username, bCryptPasswordEncoder.encode(password));
        setDetails(request, authRequest);
        return this.getAuthenticationManager().authenticate(authRequest);
    }


    /**
     * Provided so that subclasses may configure what is put into the
     * authentication request's details property.
     *
     * @param request
     *            that an authentication request is being created for
     * @param authRequest
     *            the authentication request object that should have its details
     *            set
     */
    protected void setDetails(HttpServletRequest request, OAAuthenticationToken authRequest) {
        authRequest.setDetails(authenticationDetailsSource.buildDetails(request));
    }

    /**
     * Sets the parameter name which will be used to obtain the username from
     * the login request.
     *
     * @param usernameParameter
     *            the parameter name. Defaults to "username".
     */
    public void setUsernameParameter(String usernameParameter) {
        Assert.hasText(usernameParameter, "Username parameter must not be empty or null");
        this.usernameParameter = usernameParameter;
    }


    /**
     * Defines whether only HTTP POST requests will be allowed by this filter.
     * If set to true, and an authentication request is received which is not a
     * POST request, an exception will be raised immediately and authentication
     * will not be attempted. The <tt>unsuccessfulAuthentication()</tt> method
     * will be called as if handling a failed authentication.
     * <p>
     * Defaults to <tt>true</tt> but may be overridden by subclasses.
     */
    public void setPostOnly(boolean postOnly) {
        this.postOnly = postOnly;
    }

    public final String getUsernameParameter() {
        return usernameParameter;
    }


    public void setUserInfoService(UserInfoService userInfoService) {
        this.userInfoService = userInfoService;
    }

    public void setOaApi(OaApi oaApi) {
        this.oaApi = oaApi;
    }

    public void setBCryptPasswordEncoder(PasswordEncoder bCryptPasswordEncoder) {
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }
}
