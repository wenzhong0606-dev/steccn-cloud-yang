package com.steccn.platform.controller.feign;

import com.steccn.common.dto.TupleDTO;
import com.steccn.common.vo.R;
import com.steccn.platform.service.SysConfigurationService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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

    private final SysConfigurationService sysConfigurationService;

    @ApiOperation(value = "获取根据ref内容提供获取映射配置")
    @PostMapping("/ref")
    R<Map<String, String>> sysConfRef(@RequestBody TupleDTO<String, Set<Integer>> tupleDTO) {
        return R.success(sysConfigurationService.sysConfRef(tupleDTO.getField1(), tupleDTO.getField2()));
    }
}
