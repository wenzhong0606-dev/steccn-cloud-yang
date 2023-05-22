package com.steccn.platform.controller.feign;

import com.steccn.common.vo.R;
import com.steccn.platform.domain.SysLog;
import com.steccn.platform.dto.SysLogDTO;
import com.steccn.platform.service.SysLogService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author chenhuan
 * @date 2022/10/20 10:03
 */
@Api("用户操作日志")
@RestController
@RequiredArgsConstructor
@RequestMapping("/feign/platform/sys/log")
public class SysLogFeignController {

    private final SysLogService sysLogService;

    /**
     * 千万不可以加@Operation ,会无限递归加入日志
     *
     * @param sysLogDTO
     * @return
     */
    @PostMapping
    public R<Boolean> add(@RequestBody SysLogDTO sysLogDTO) {
        return R.success(sysLogService.add(sysLogDTO));
    }
}
