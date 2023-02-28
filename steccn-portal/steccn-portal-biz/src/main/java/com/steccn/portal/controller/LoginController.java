package com.steccn.portal.controller;

import com.steccn.authserver.exception.BusinessException;
import com.steccn.common.util.AESUtils;
import com.steccn.common.vo.R;
import com.steccn.portal.service.LoginService;
import com.steccn.portal.vo.SteccnTokenVO;
import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author yequan
 * @desc 统一登录
 */
@Api(tags = "统一登录")
@RestController
@RequestMapping("/portal")
@Slf4j
@RequiredArgsConstructor
public class LoginController {

    private final LoginService loginService;

    /**
     * 统一登录，获取OA ， MESS-JAVA的菜单和token
     *
     * @param username 用户名
     * @param password 密码
     * @param clientNo 客户端编码
     * @return r
     */
    @GetMapping("/login")
    public R<SteccnTokenVO> login(@RequestParam("username") String username,
                                  @RequestParam("password") String password,
                                  @RequestParam(value = "clientNo", required = false) String clientNo) {
        String decryptPassword;
        try {
            decryptPassword = AESUtils.decrypt(password);
        } catch (Exception e) {
            throw new BusinessException("密码解密失败");
        }
        return loginService.login(username, decryptPassword,clientNo);
    }

    /**
     * 退出登录
     *
     * @return r
     */
    @GetMapping("/logout")
    public R<Boolean> loginOut() {
        //清除缓存上下文
        SecurityContextHolder.clearContext();
        return R.success(Boolean.TRUE);
    }
}
