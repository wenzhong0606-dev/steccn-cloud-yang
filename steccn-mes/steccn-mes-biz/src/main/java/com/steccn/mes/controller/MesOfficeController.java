package com.steccn.mes.controller;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.common.constant.FeignConstant;
import com.steccn.common.vo.R;
import com.steccn.mes.dto.ErpCustomerDTO;
import com.steccn.mes.service.JsSysOfficeService;
import com.steccn.mes.vo.MesCutomerVO;
import com.steccn.oa.constant.OaConstant;
import com.steccn.platform.feign.RemoteSysOauthClientDetailFeignApi;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 类说明
 *
 * @author AndrewLuo
 * @date 2023/07/26
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/mes/office")
@Slf4j
public class MesOfficeController {
    private final RemoteSysOauthClientDetailFeignApi remoteSysOauthClientDetailFeignApi;
    private final JsSysOfficeService jsSysOfficeService;

    @PostMapping("/synchronousCustomer")
    @ApiOperation(value = "同步客户")
    public R<Boolean> synchronousCustomer(@RequestBody ErpCustomerDTO dto) {
        return R.success(jsSysOfficeService.synchronousCustomer(dto));
    }

    @PostMapping("/thirdparty/customer")
    @ApiOperation(value = "三方拉取客户")
    public R<List<MesCutomerVO>> getAllCustomer(HttpServletRequest request, @RequestBody ErpCustomerDTO dto) {
        validRequestHeader(request);
        return R.success(jsSysOfficeService.getAllCustomer(dto));
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
