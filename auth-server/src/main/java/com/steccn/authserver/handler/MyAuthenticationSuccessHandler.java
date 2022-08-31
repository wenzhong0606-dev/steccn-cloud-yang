/**
 *
 */
package com.steccn.authserver.handler;

import com.steccn.common.vo.R;
import com.steccn.portal.feign.RemoteLoginFeign;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author zhailiang
 *
 */
@Component("myAuthenticationSuccessHandler")
@Slf4j
public class MyAuthenticationSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private RemoteLoginFeign remoteLoginFeign;

    /*
     * (non-Javadoc)
     *
     * @see org.springframework.security.web.authentication.
     * AuthenticationSuccessHandler#onAuthenticationSuccess(javax.servlet.http.
     * HttpServletRequest, javax.servlet.http.HttpServletResponse,
     * org.springframework.security.core.Authentication)
     */
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws ServletException, IOException {
        logger.debug("登录成功");
        log.debug("request username -> {}", request.getParameter("username"));
        log.debug("request password -> {}", request.getParameter("password"));
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        //todo port接口
        //OA, MESS-JAVA 一次性返回菜单
        R login = remoteLoginFeign.login(username, password);
        log.debug("login ->{} ", login);
        super.onAuthenticationSuccess(request, response, authentication);
    }

}
