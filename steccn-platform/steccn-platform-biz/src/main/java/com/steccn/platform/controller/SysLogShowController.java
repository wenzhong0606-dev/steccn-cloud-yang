package com.steccn.platform.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.common.dto.TupleDTO;
import com.steccn.common.vo.R;
import com.steccn.platform.dto.SysLogShowPageDTO;
import com.steccn.platform.service.SysLogShowService;
import com.steccn.platform.vo.SysLogShowPageVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 用户访问日志控制器
 *
 * @author Adolph.Ye
 * @date 2023/2/16
 */
@Api("消息中心控制器")
@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/platform/sys/log/show")
public class SysLogShowController {

    private final SysLogShowService sysLogShowService;

    /**
     * 访问日志列表 - 用户
     * @param name
     * @return
     */
    @GetMapping("/user")
    public R<List<TupleDTO<Long, String>>> userList(@RequestParam(value = "name", required = false) String name) {
        return R.success(sysLogShowService.userList(name));
    }

    /**
     * 访问日志列表
     * @param dto
     * @return
     */
    @PostMapping("/page")
    public R<Page<SysLogShowPageVO>> page(@RequestBody SysLogShowPageDTO dto) {
        return R.success(sysLogShowService.pageList(dto));
    }

}
