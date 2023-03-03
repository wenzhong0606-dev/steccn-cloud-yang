package com.steccn.platform.controller;

import cn.hutool.core.lang.Assert;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.common.vo.R;
import com.steccn.platform.dto.SysCustomerMaterialDTO;
import com.steccn.platform.dto.SysMsgPageDTO;
import com.steccn.platform.service.SysCustomerMaterialService;
import com.steccn.platform.vo.SysCustomerMaterialPageVO;
import com.steccn.platform.vo.SysMsgPageVO;
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
 * @desc 客户物料控制器
 */
@Api("客户物料控制器")
@RestController
@RequiredArgsConstructor
@RequestMapping("/platform/sys/customer/material")
public class SysCustomerMaterialController {

    private final SysCustomerMaterialService sysCustomerMaterialService;

    @PostMapping("/page")
    @ApiOperation("客户物料列表")
    public R<Page<SysCustomerMaterialPageVO>> page(@Valid @RequestBody SysCustomerMaterialDTO dto) {
        return R.success(sysCustomerMaterialService.pageList(dto));
    }
}
