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
import com.steccn.common.dto.TripleDTO;
import com.steccn.common.dto.TupleDTO;
import com.steccn.common.vo.R;
import com.steccn.oa.constant.OaConstant;
import com.steccn.oa.service.*;
import com.steccn.oa.vo.*;
import com.steccn.platform.feign.RemoteSysOauthClientDetailFeignApi;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

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

    private final HrmjobtitlesService hrmjobtitlesService;

    private final KqFormatDetailService kqFormatDetailService;
    @GetMapping("/thirdParty/department/{id}")
    @ApiOperation(value = "第三方获取部门信息")
    public R<List<OaDepartmentTreeVO>> screenData(HttpServletRequest request, @PathVariable("id") Integer subId) {
        validRequestHeader(request);
        return R.success(hrmdepartmentService.getDepartmentTree(subId));
    }

    @PostMapping("/thirdParty/allDepartment")
    @ApiOperation(value = "获取所有部门")
    public R<List<OaDepartmentVO>> getAllDepartment(HttpServletRequest request, @RequestBody TupleDTO<LocalDateTime,LocalDateTime> dto) {
        validRequestHeader(request);
        return R.success(hrmdepartmentService.getAllDepartment(dto));
    }

    @PostMapping("/thirdParty/allJobTitle")
    @ApiOperation(value = "获取所有部门")
    public R<List<OaJobTitleVO>> getAllJobTitle(HttpServletRequest request, @RequestBody TupleDTO<LocalDateTime,LocalDateTime> dto) {
        validRequestHeader(request);
        return R.success(hrmjobtitlesService.getAllJobTitle(dto));
    }

    @PostMapping("/thirdParty/getAllOaUser")
    @ApiOperation(value = "第三方获取所有oa用户")
    public R<List<OaUserVO>> getAllOaUser(HttpServletRequest request, @RequestBody TripleDTO<LocalDateTime,LocalDateTime, Set<String>> dto) {
        validRequestHeader(request);
        return R.success(hrmresourceService.getAllOaUser(dto));
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

    @GetMapping("/thirdParty/mes/oaUser")
    @ApiOperation(value = "获取Oa用户")
    public R<OaUserVO> getOaUser(@RequestParam("loginCode") String loginCode) {
        return R.success(hrmresourceService.getOaUserByLoginCode(loginCode));
    }

    @GetMapping("/thirdParty/emp/num")
    @ApiOperation(value = "获取在职人数")
    public R<Integer> getOaNum(HttpServletRequest request) {
        validRequestHeader(request);
        return R.success(hrmresourceService.peopleNum());
    }

    @GetMapping("/thirdParty/emp/work")
    @ApiOperation(value = "获取厂内人数")
    public R<Integer> getOnWorkNum(HttpServletRequest request) {
//        validRequestHeader(request);
        return R.success(kqFormatDetailService.kqNum());
    }

    @GetMapping("/thirdParty/safety/num")
    @ApiOperation(value = "获取安全天数")
    public R<Long> getSafetyNum(HttpServletRequest request) {
        validRequestHeader(request);
        return R.success(hrmresourceService.safetyNum());
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
