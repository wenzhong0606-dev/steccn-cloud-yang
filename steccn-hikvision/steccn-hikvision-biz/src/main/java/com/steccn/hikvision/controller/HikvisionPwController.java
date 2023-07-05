package com.steccn.hikvision.controller;


import com.steccn.common.vo.R;
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

    @ApiOperation("权限新增")
    @PostMapping
    public R<Boolean> add() {
        return R.success();
    }

    @ApiOperation("权限编辑")
    @PutMapping
    public R<Boolean> edit() {
        return R.success();
    }

    @ApiOperation("权限删除")
    @DeleteMapping
    public R<Boolean> delete() {
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
