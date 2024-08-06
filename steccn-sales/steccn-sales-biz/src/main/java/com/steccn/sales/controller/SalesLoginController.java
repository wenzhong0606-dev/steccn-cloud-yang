package com.steccn.sales.controller;

import com.steccn.authserver.exception.BusinessException;
import com.steccn.common.util.AESUtils;
import com.steccn.common.vo.R;
import com.steccn.portal.vo.SteccnTokenVO;
import com.steccn.sales.service.ResUsersService;
import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 销售系统登录控制器
 *
 * @author AndrewLuo
 * @date 2024/08/06
 */
@Api("销售系统登录控制器")
@RestController
@RequiredArgsConstructor
@RequestMapping("/sales")
public class SalesLoginController {
    private final ResUsersService resUsersService;

    @GetMapping("/login")
    R<SteccnTokenVO> login(@RequestParam("username") String username,
                           @RequestParam("password") String password,
                           @RequestParam(value = "clientNo", required = false) String clientNo,
                           @RequestParam(value = "loginStatus", required = false) Integer loginStatus){
        String decryptPassword;
        try {
            decryptPassword = AESUtils.decrypt(password);
        } catch (Exception e) {
            throw new BusinessException("密码解密失败");
        }
        return R.success(resUsersService.login(username,decryptPassword,clientNo,loginStatus));
    }
}
