package com.steccn.dp.controller;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.common.constant.FeignConstant;
import com.steccn.common.vo.R;
import com.steccn.dp.service.AdsOpcUaAll3Service;
import com.steccn.dp.service.DpColdEndCheckpointValueService;
import com.steccn.dp.vo.DpColdEndCheckpointValueVO;
import com.steccn.dp.vo.DpTempVO;
import com.steccn.oa.constant.OaConstant;
import com.steccn.platform.feign.RemoteSysOauthClientDetailFeignApi;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 类说明
 *
 * @author AndrewLuo
 * @date 2025/02/19
 */
@Api("数字孪生数据")
@RestController
@RequiredArgsConstructor
@RequestMapping("/dp/dataTwin")
public class DpDataTwinController {

    private final AdsOpcUaAll3Service adsOpcUaAll3Service;
    private final RemoteSysOauthClientDetailFeignApi remoteSysOauthClientDetailFeignApi;
    private final DpColdEndCheckpointValueService dpColdEndCheckpointValueService;
    @GetMapping("/weather")
    @ApiOperation(value = "天气数据")
    public R<List<DpTempVO>> weather(HttpServletRequest request) {
        validRequestHeader(request);
        return R.success(adsOpcUaAll3Service.queryDpTemp());
    }

    @GetMapping("/ep")
    @ApiOperation(value = "环保数据")
    public R<List<DpColdEndCheckpointValueVO>> ep(HttpServletRequest request) {
        validRequestHeader(request);
        return R.success(dpColdEndCheckpointValueService.getEpColdEndValue());
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
