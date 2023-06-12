package com.steccn.platform.controller.feign;

import cn.hutool.core.lang.Assert;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.steccn.common.dto.TripleDTO;
import com.steccn.common.dto.TupleDTO;
import com.steccn.common.vo.R;
import com.steccn.platform.domain.SysConfiguration;
import com.steccn.platform.dto.SysConfigurationDTO;
import com.steccn.platform.service.SysConfigurationService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

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
    public R<Map<String, String>> sysConfRef(@RequestBody TupleDTO<String, Set<Integer>> tupleDTO) {
        return R.success(sysConfigurationService.sysConfRef(tupleDTO.getField1(), tupleDTO.getField2()));
    }

    @ApiOperation(value = "根据code获取list的配置")
    @PostMapping("/list/integer")
    public R<Map<String, List<Integer>>> confListInteger(@RequestBody TripleDTO<String, Set<String>, Integer> dto) {
        Integer idx = dto.getField3();
        Assert.checkBetween(idx, 1, 3, "下标越界");
        return R.success(sysConfigurationService.confList(dto.getField1(), dto.getField2(), x -> {
            String content;
            if (idx == 1) {
                content = x.getContent();
            } else if (idx == 2) {
                content = x.getRefContent();
            } else {
                content = x.getOtherContent();
            }
            return Integer.parseInt(content);
        }));
    }

    @ApiOperation(value = "根据code获取list的配置")
    @PostMapping("/list/string")
    public R<Map<String, List<String>>> confListString(@RequestBody TripleDTO<String, Set<String>, Integer> dto) {
        Integer idx = dto.getField3();
        Assert.checkBetween(idx, 1, 3, "下标越界");
        return R.success(sysConfigurationService.confList(dto.getField1(), dto.getField2(), x -> {
            String content;
            if (idx == 1) {
                content = x.getContent();
            } else if (idx == 2) {
                content = x.getRefContent();
            } else {
                content = x.getOtherContent();
            }
            return content;
        }));
    }

    @ApiOperation(value = "根据code获取list的配置")
    @PostMapping("/list")
    public R<Map<String, List<SysConfigurationDTO>>> confList(@RequestBody TupleDTO<String, Set<String>> dto) {
        return R.success(sysConfigurationService.confList(dto.getField1(), dto.getField2(), (x) -> {
            SysConfigurationDTO configurationDTO = new SysConfigurationDTO();
            BeanUtils.copyProperties(x, configurationDTO);
            return configurationDTO;
        }));
    }
}
