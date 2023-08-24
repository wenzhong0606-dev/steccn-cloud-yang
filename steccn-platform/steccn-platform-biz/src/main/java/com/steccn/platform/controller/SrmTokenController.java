package com.steccn.platform.controller;

import com.steccn.common.vo.R;
import com.steccn.platform.service.SalesTokenService;
import com.steccn.platform.service.SrmTokenService;
import com.steccn.platform.vo.SalesTokenVO;
import com.steccn.platform.vo.SrmTokenVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 类说明
 *
 * @author AndrewLuo
 * @date 2023/08/24
 */
@Api("SRM系统控制器")
@RestController
@RequiredArgsConstructor
@RequestMapping("/platform/srm")
public class SrmTokenController {
    private final SrmTokenService srmTokenService;
    @GetMapping("/token")
    @ApiOperation(value = "获取srm系统token")
    @ResponseBody
    public R<SrmTokenVO> getToken() {
        return R.success(srmTokenService.getSrmToken());
    }
}
