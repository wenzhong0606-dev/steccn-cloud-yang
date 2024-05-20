package com.steccn.platform.controller;

import com.steccn.common.vo.R;
import com.steccn.platform.dto.HikDbDTO;
import com.steccn.platform.service.HikResponseService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 接收海康数据
 *
 * @author AndrewLuo
 * @date 2024/05/20
 */
@Api("海康接口数据接收")
@RestController
@RequiredArgsConstructor
@RequestMapping("/platform/hik")
public class HikController {
    private final HikResponseService hikResponseService;

    @PostMapping("/db")
    @ApiOperation(value = "保存地磅信息")
    public R<Boolean> saveDbData(@RequestBody HikDbDTO dto) {
        return R.success(hikResponseService.saveResponse(dto));
    }
}
