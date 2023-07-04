package com.steccn.platform.controller;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.common.constant.FeignConstant;
import com.steccn.common.vo.R;
import com.steccn.oa.constant.OaConstant;
import com.steccn.platform.feign.RemoteSysOauthClientDetailFeignApi;
import com.steccn.platform.service.SysOfficeService;
import com.steccn.platform.vo.SysOfficeVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 第三方同步机构数据
 *
 * @author AndrewLuo
 * @date 2023/06/29
 */
@Api("机构数据")
@RestController
@RequiredArgsConstructor
@RequestMapping("/platform/thirdParty")
public class SysOfficeThirdPartyController {
    private final SysOfficeService sysOfficeService;

    private final RemoteSysOauthClientDetailFeignApi remoteSysOauthClientDetailFeignApi;

    @GetMapping("/department")
    @ApiOperation("根据机构类型获取树状数据")
    public R<List<SysOfficeVO>> departmentTreeData(HttpServletRequest request) {
        validRequestHeader(request);
        return R.success(sysOfficeService.treeData());
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
