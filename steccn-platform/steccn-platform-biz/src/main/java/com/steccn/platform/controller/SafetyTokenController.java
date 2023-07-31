package com.steccn.platform.controller;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.common.constant.FeignConstant;
import com.steccn.common.vo.R;
import com.steccn.platform.constant.OaDataConstant;
import com.steccn.platform.feign.RemoteSysOauthClientDetailFeignApi;
import com.steccn.platform.service.SafetyTokenService;
import com.steccn.platform.vo.SafetyTokenVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 安环系统拦截器
 *
 * @author AndrewLuo
 * @date 2023/07/31
 */
@Api("安环控制器")
@RestController
@RequiredArgsConstructor
@RequestMapping("/platform/safety")
public class SafetyTokenController {
    private final SafetyTokenService safetyTokenService;

    private final RemoteSysOauthClientDetailFeignApi remoteSysOauthClientDetailFeignApi;

    @GetMapping("/token")
    @ApiOperation(value = "获取安环系统token")
    public R<SafetyTokenVO> getToken(HttpServletRequest request) {
        validRequestHeader(request);
        return R.success(safetyTokenService.getSafetyToken());
    }

    /**
     * 校验请求头信息
     *
     * @param request 请求
     */
    private void validRequestHeader(HttpServletRequest request) {
        List<String> pwdSet = FeignOptional.of(() -> remoteSysOauthClientDetailFeignApi.clientEncodePassword(), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        String clientId = request.getHeader(OaDataConstant.STR_CLIENT_ID);
        Assert.isTrue(StrUtil.isNotBlank(clientId), "请求头中必须携带client-id");
        Assert.isTrue(pwdSet.contains(clientId), "非法访问，请携带正确的client-id");
    }
}
