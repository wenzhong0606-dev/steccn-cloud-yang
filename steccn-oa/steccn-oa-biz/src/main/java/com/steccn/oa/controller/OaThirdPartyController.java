package com.steccn.oa.controller;

/**
 * 类说明
 *
 * @author AndrewLuo
 * @date 2023/06/29
 */

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.common.constant.FeignConstant;
import com.steccn.common.vo.R;
import com.steccn.oa.constant.OaConstant;
import com.steccn.oa.domain.KqGroup;
import com.steccn.oa.service.HrmdepartmentService;
import com.steccn.oa.service.HrmresourceService;
import com.steccn.oa.service.KqGroupService;
import com.steccn.oa.vo.OaDepartmentTreeVO;
import com.steccn.oa.vo.OaKqGroupDetailVO;
import com.steccn.oa.vo.OaMesUserVO;
import com.steccn.platform.feign.RemoteSysOauthClientDetailFeignApi;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * oa第三方接口
 *
 * @author AndrewLuo
 * @date 2022/08/30
 */
@Api(tags = "泛微oa接口")
@RestController
@RequestMapping("/oa")
@Slf4j
@RequiredArgsConstructor
public class OaThirdPartyController {
    private final RemoteSysOauthClientDetailFeignApi remoteSysOauthClientDetailFeignApi;

    private final HrmdepartmentService hrmdepartmentService;

    private final KqGroupService kqGroupService;

    private final HrmresourceService hrmresourceService;
    @GetMapping("/thirdParty/department/{id}")
    @ApiOperation(value = "第三方获取部门信息")
    public R<List<OaDepartmentTreeVO>> screenData(HttpServletRequest request, @PathVariable("id") Integer subId) {
        validRequestHeader(request);
        return R.success(hrmdepartmentService.getDepartmentTree(subId));
    }

    @GetMapping("/thirdParty/kqGroup")
    @ApiOperation(value = "第三方排班规则")
    public R<List<OaKqGroupDetailVO>> screenData(HttpServletRequest request) {
        validRequestHeader(request);
        return R.success(kqGroupService.getKqGroupDetails());
    }

    @GetMapping("/thirdParty/getAllUser")
    @ApiOperation(value = "第三方获取所有用户")
    public R<List<OaMesUserVO>> getAllUser(HttpServletRequest request) {
        validRequestHeader(request);
        return R.success(hrmresourceService.getMesUser());
    }

    @GetMapping("/thirdParty/synchronizeUser")
    @ApiOperation(value = "同步用户")
    public R<String> synchronizeUserUser(HttpServletRequest request) {
        return R.success(hrmresourceService.synchronizeUser());
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
