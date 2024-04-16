package com.steccn.dp.controller;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.common.constant.FeignConstant;
import com.steccn.common.dto.TupleDTO;
import com.steccn.common.vo.R;
import com.steccn.dp.service.DpColdEndCheckpointValueService;
import com.steccn.dp.vo.DpColdEndCheckpointValueVO;
import com.steccn.dp.vo.DpWeightVO;
import com.steccn.oa.constant.OaConstant;
import com.steccn.platform.feign.RemoteSysOauthClientDetailFeignApi;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 冷端数采接口
 *
 * @author AndrewLuo
 * @date 2024/03/01
 */
@Api("备件信息")
@RestController
@RequiredArgsConstructor
@RequestMapping("/dp/coldEnd")
public class DpColdEndController {

    private final DpColdEndCheckpointValueService dpColdEndCheckpointValueService;

    private final RemoteSysOauthClientDetailFeignApi remoteSysOauthClientDetailFeignApi;

    @GetMapping("/list")
    @ApiOperation(value = "冷端数采数据")
    public R<List<DpColdEndCheckpointValueVO>> count(HttpServletRequest request) {
        validRequestHeader(request);
        return R.success(dpColdEndCheckpointValueService.getColdEndValue());
    }

    @GetMapping("/screen/{num}")
    @ApiOperation(value = "冷端数采数据")
    public BigDecimal count(@PathVariable Integer num) {
        return dpColdEndCheckpointValueService.getScreenData(num);
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
