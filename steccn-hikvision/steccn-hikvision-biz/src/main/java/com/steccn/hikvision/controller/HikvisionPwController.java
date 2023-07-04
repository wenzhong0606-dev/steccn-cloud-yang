package com.steccn.hikvision.controller;


import com.steccn.common.vo.R;
import com.steccn.hikvision.dto.SysHikPwDTO;
import com.steccn.hikvision.service.SysHikPwService;
import com.steccn.hikvision.vo.CameraVO;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * @author yequan
 * @desc 海康威视权限控制器
 */
@RestController
@RequestMapping("/hikvision/pw")
@RequiredArgsConstructor
public class HikvisionPwController {

    private final SysHikPwService sysHikPwService;

    @ApiOperation("权限新增")
    @PostMapping
    public R<Boolean> save(@RequestBody SysHikPwDTO sysHikPwDTO) {
        return R.success();
    }

    @ApiOperation("权限查询")
    @GetMapping
    public R<List<CameraVO>> get() {
        return R.success();
    }

    @ApiOperation("用户权限-树型结构输出")
    @GetMapping("/tree")
    public R<List<CameraVO>> tree() {
        return R.success();
    }
}
