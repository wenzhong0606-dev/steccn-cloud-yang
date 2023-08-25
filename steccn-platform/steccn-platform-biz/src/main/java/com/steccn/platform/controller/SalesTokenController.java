package com.steccn.platform.controller;

import com.steccn.authserver.util.SecurityUtils;
import com.steccn.common.vo.R;
import com.steccn.platform.service.SalesTokenService;
import com.steccn.platform.vo.SafetyTokenVO;
import com.steccn.platform.vo.SalesTokenVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 销售系统控制器
 *
 * @author AndrewLuo
 * @date 2023/08/23
 */
@Api("销售系统控制器")
@RestController
@RequiredArgsConstructor
@RequestMapping("/platform/sales")
public class SalesTokenController {
    private final SalesTokenService salesTokenService;
    @GetMapping("/token")
    @ApiOperation(value = "获取销售系统token")
    @ResponseBody
    public R<SalesTokenVO> getToken() {
        return R.success(salesTokenService.getSalesToken());
    }

}
