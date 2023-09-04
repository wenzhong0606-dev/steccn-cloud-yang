package com.steccn.mes.controller;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.common.constant.FeignConstant;
import com.steccn.common.vo.R;
import com.steccn.mes.service.MesEquipmentService;
import com.steccn.mes.vo.MesEqpVo;
import com.steccn.oa.constant.OaConstant;
import com.steccn.platform.feign.RemoteSysOauthClientDetailFeignApi;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 获取设备信息
 *
 * @author AndrewLuo
 * @date 2023/09/01
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/mes/eqp")
@Slf4j
public class MesEqpController {
    private final MesEquipmentService mesEquipmentService;
    private final RemoteSysOauthClientDetailFeignApi remoteSysOauthClientDetailFeignApi;

    @GetMapping("/thirdParty")
    @ApiOperation(value = "获取设备信息")
    public R<List<MesEqpVo>> getEqpInfo(HttpServletRequest request) {
        validRequestHeader(request);
        return R.success(mesEquipmentService.getEqpInfo());
    }

    /**
     * 校验请求头信息
     *
     * @param request 请求
     */
    private void validRequestHeader(HttpServletRequest request) {
        List<String> pwdSet = FeignOptional.of(() -> remoteSysOauthClientDetailFeignApi.clientEncodePassword(), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        String clientId = request.getHeader(OaConstant.CLIENT_ID);
        Assert.isTrue(StrUtil.isNotBlank(clientId), "请求头中必须携带client-id");
        Assert.isTrue(pwdSet.contains(clientId), "非法访问，请携带正确的client-id");
    }
}
