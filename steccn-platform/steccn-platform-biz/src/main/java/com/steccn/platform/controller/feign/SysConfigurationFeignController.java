package com.steccn.platform.controller.feign;

import com.steccn.common.vo.R;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Set;

/**
 * @author yequan
 * @desc 系统配置
 */
@Api("系统配置")
@RestController
@RequiredArgsConstructor
@RequestMapping("/feign/platform/sys/conf")
public class SysConfigurationFeignController {

    @ApiOperation(value = "获取产线的映射配置")
    @PostMapping("/line")
    R<Map<Integer, String>> sysConfLine(Set<Integer> lineSet) {
        return R.success();
    }
}
