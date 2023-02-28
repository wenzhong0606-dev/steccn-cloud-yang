package com.steccn.platform.controller;

import com.steccn.common.vo.R;
import com.steccn.platform.dto.SysMobilePushDTO;
import com.steccn.platform.dto.SysMsgSendDTO;
import com.steccn.platform.service.SysMobilePushService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api("数据字典控制器")
@RestController
@RequiredArgsConstructor
@RequestMapping("/platform/sys/mobile/push")
public class SysMobilePushController {

    private final SysMobilePushService sysMobilePushService;

    @PostMapping
    @ApiOperation("发送消息")
    public R<Boolean> push(@RequestBody SysMobilePushDTO dto) {
        return R.success(sysMobilePushService.push(dto));
    }
}
