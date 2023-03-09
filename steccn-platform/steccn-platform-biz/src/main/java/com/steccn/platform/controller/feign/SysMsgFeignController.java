package com.steccn.platform.controller.feign;

import com.steccn.common.annotation.SysLog;
import com.steccn.common.vo.R;
import com.steccn.platform.dto.SysMsgSendDTO;
import com.steccn.platform.service.SysMsgService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * @author yequan
 * @desc 消息的feign调用
 * @date 2023/3/9
 */
@Api("用户操作日志")
@RestController
@RequiredArgsConstructor
@RequestMapping("/feign/platform/sys/msg")
public class SysMsgFeignController {

    private final SysMsgService sysMsgService;

    @SysLog("发送消息")
    @PostMapping
    @ApiOperation("发送消息")
    public R<Boolean> sendMsg(@Valid @RequestBody SysMsgSendDTO sysMsgDTO) {
        return R.success(sysMsgService.sendMsg(sysMsgDTO));
    }
}
