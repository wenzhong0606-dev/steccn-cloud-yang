package com.steccn.platform.controller;

import cn.hutool.core.lang.Assert;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.authserver.util.SecurityUtils;
import com.steccn.common.annotation.SysLog;
import com.steccn.common.vo.R;
import com.steccn.platform.dto.SysMsgPageDTO;
import com.steccn.platform.dto.SysMsgSendDTO;
import com.steccn.platform.service.SysMsgService;
import com.steccn.platform.vo.SysMsgPageVO;
import com.steccn.platform.vo.SysMsgTabVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

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

    private final SysMsgService sysMsgService;

    @SysLog("发送消息")
    @PostMapping
    @ApiOperation("发送消息")
    public R<Boolean> sendMsg(@Valid @RequestBody SysMsgSendDTO sysMsgDTO) {
        return R.success(sysMsgService.sendMsg(sysMsgDTO));
    }

    @GetMapping("/tab")
    @ApiOperation("获取tab集合")
    public R<List<SysMsgTabVO>> tab() {
        return R.success(sysMsgService.tab());
    }

    @GetMapping("/unread/count")
    @ApiOperation("未读消息数")
    @PreAuthorize("hasAuthority('inspection:audititemClassify:view:1')")
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
    public R<Page<SysMsgPageVO>> page(@Valid @RequestBody SysMsgPageDTO dto) {
        return R.success(sysMsgService.msgPage(dto));
    }
}
