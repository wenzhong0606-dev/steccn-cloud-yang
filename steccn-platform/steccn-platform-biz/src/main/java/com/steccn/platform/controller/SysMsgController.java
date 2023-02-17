package com.steccn.platform.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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
import org.springframework.web.bind.annotation.*;
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
    public R<Boolean> sendMsg(@RequestBody SysMsgSendDTO sysMsgDTO) {
        return R.success(sysMsgService.sendMsg(sysMsgDTO));
    }

    @GetMapping("/tab")
    @ApiOperation("获取tab集合")
    public R<List<SysMsgTabVO>> tab() {
        return R.success();
    }

    @SysLog("读取消息")
    @PutMapping
    @ApiOperation("读取消息")
    public R<Boolean> read(@RequestBody List<Long> msgIdList) {
        return R.success();
    }

    @PostMapping("/page")
    @ApiOperation("消息列表")
    public R<Page<SysMsgPageVO>> page(@RequestBody SysMsgPageDTO dto) {
        return R.success();
    }
}
