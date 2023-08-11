package com.steccn.platform.controller;

import cn.hutool.core.lang.Assert;
import com.google.common.collect.Sets;
import com.steccn.common.vo.R;
import com.steccn.platform.dto.SysConfigurationDTO;
import com.steccn.platform.service.SysConfigurationService;
import com.steccn.platform.vo.SysAppVersionVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * @author yequan
 * @desc
 * @date 2023/8/11
 */
@Api("动态配置控制器")
@RestController
@RequiredArgsConstructor
@RequestMapping("/platform/sys/conf")
public class SysConfigurationController {

    private final SysConfigurationService sysConfigurationService;

    @ApiOperation("获取配置")
    @GetMapping("/list")
    public R<List<String>> list(@RequestParam("namespace") String namespace,
                                @RequestParam("key") String key) {
        Map<String, List<String>> confMap = sysConfigurationService.confList(namespace, Sets.newHashSet(key), x -> x.getRefContent());
        return R.success(confMap.get(key));
    }
}
