package com.steccn.platform.controller;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.authserver.util.SecurityUtils;
import com.steccn.common.annotation.SysLog;
import com.steccn.common.constant.FeignConstant;
import com.steccn.common.vo.R;
import com.steccn.oa.constant.OaConstant;
import com.steccn.platform.dto.SysMsgPageDTO;
import com.steccn.platform.dto.SysMsgSendDTO;
import com.steccn.platform.dto.SysMsgSendMesDTO;
import com.steccn.platform.feign.RemoteSysOauthClientDetailFeignApi;
import com.steccn.platform.service.SysMsgService;
import com.steccn.platform.vo.SysMsgPageVO;
import com.steccn.platform.vo.SysMsgTabVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;

/**
 * 消息中心控制器
 *
 * @author Adolph.Ye
 * @date 2023/2/16
 */
@Api("消息中心控制器")
@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/platform/sys/msg")
public class SysMsgController {
    private final RemoteSysOauthClientDetailFeignApi remoteSysOauthClientDetailFeignApi;

    private final SysMsgService sysMsgService;

    @SysLog("发送消息")
    @PostMapping
    @ApiOperation("发送消息")
    public R<Boolean> sendMsg(@Valid @RequestBody SysMsgSendDTO sysMsgDTO) {
        return R.success(sysMsgService.sendMsg(sysMsgDTO));
    }

    @SysLog("三方发送消息")
    @PostMapping("/thirdParty")
    @ApiOperation("三方发送消息")
    public R<Boolean> thirdPartySendMsg(HttpServletRequest request,@Valid @RequestBody SysMsgSendDTO sysMsgDTO) {
        validRequestHeader(request);
        return R.success(sysMsgService.sendMsg(sysMsgDTO));
    }

    @SysLog("mes发送消息")
    @PostMapping("/mes")
    @ApiOperation("发送消息")
    public R<Boolean> sendMesMsg(@RequestBody SysMsgSendMesDTO sysMsgDTO) {
        return R.success(sysMsgService.sendMesMsg(sysMsgDTO));
    }

    @GetMapping("/tab")
    @ApiOperation("获取tab集合")
    public R<List<SysMsgTabVO>> tab() {
        return R.success(sysMsgService.tab());
    }

    @GetMapping("/unread/count")
    @ApiOperation("未读消息数")
    public R<Integer> unreadCount() {
        return R.success(sysMsgService.unreadCount(SecurityUtils.getUser().getUserId()));
    }

    @SysLog("读取消息")
    @PutMapping
    @ApiOperation("读取消息")
    public R<Boolean> read(@RequestBody List<Long> msgIdList) {
        Assert.notEmpty(msgIdList, "消息不能为空");
        return R.success(sysMsgService.read(msgIdList));
    }

    @SysLog("标记全部已读")
    @PutMapping("/mark/read/all")
    @ApiOperation("标记全部已读")
    public R<Boolean> markReadAll() {
        return R.success(sysMsgService.markReadAll(SecurityUtils.getUser().getUserId()));
    }

    @PostMapping("/page")
    @ApiOperation("消息列表")
    public R<List<SysMsgPageVO>> page(@Valid @RequestBody SysMsgPageDTO dto) {
        Assert.notNull(dto.getSize(), "页条数不可为空");
        return R.success(sysMsgService.msgPage(dto));
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
