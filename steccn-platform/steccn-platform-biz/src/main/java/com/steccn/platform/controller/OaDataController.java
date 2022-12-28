package com.steccn.platform.controller;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.common.annotation.SysLog;
import com.steccn.common.vo.R;
import com.steccn.platform.constant.OaDataConstant;
import com.steccn.platform.dto.OaMaterialDTO;
import com.steccn.platform.dto.OaMessagePushDTO;
import com.steccn.platform.dto.OaProcessNotifyDTO;
import com.steccn.platform.dto.OaProcessPushDTO;
import com.steccn.platform.feign.RemoteSysOauthClientDetailFeignApi;
import com.steccn.platform.service.OaResponseService;
import com.steccn.platform.vo.EqpSpareVO;
import com.steccn.platform.vo.OaMaterialVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.Set;

/**
 * OA消息推送接口
 *
 * @author AndrewLuo
 * @date 2022/12/26
 */
@Api("OA接口数据接收")
@RestController
@RequiredArgsConstructor
@RequestMapping("/platform/oa")
public class OaDataController {

    private final OaResponseService oaResponseService;

    private final RemoteSysOauthClientDetailFeignApi remoteSysOauthClientDetailFeignApi;

    @PostMapping("/process/push")
    @ApiOperation(value = "接收oa流程的值")
    public R<Boolean> addOaProcess(HttpServletRequest request, @Valid @RequestBody OaProcessPushDTO dto) {
        validRequestHeader(request);
        return R.success(oaResponseService.addProcessPush(dto));
    }

    @PostMapping("/message/push")
    @ApiOperation(value = "接收oa消息的值")
    public R<Boolean> addOaMessage(HttpServletRequest request, @Valid @RequestBody OaMessagePushDTO dto) {
        validRequestHeader(request);
        return R.success(oaResponseService.addMessagePush(dto));
    }

    @PutMapping("/process/notify")
    @ApiOperation(value = "接收oa消息的值")
    public R<Boolean> addOaNotify(HttpServletRequest request, @Valid @RequestBody OaProcessNotifyDTO dto) {
        validRequestHeader(request);
        return R.success(oaResponseService.addProcessNotify(dto));
    }

    @GetMapping("/eqpSpare/page")
    @ApiOperation(value = "查询物料分页数据")
    public R<Page<OaMaterialVO>> listPage(HttpServletRequest request,OaMaterialDTO dto) {
        validRequestHeader(request);
        return R.success(oaResponseService.listPage(dto));
    }

    /**
     * 校验请求头信息
     *
     * @param request 请求
     */
    private void validRequestHeader(HttpServletRequest request) {
        Set<String> pwdSet = FeignOptional.of(remoteSysOauthClientDetailFeignApi.clientEncodePassword()).get();
        String clientId = request.getHeader(OaDataConstant.STR_CLIENT_ID);
        Assert.isTrue(StrUtil.isNotBlank(clientId), "请求头中必须携带client-id");
        Assert.isTrue(pwdSet.contains(clientId), "非法访问，请携带正确的client-id");
    }
}
