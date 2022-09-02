package com.steccn.pms.controller;

import com.steccn.common.vo.R;
import com.steccn.pms.service.FileInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/pms")
public class TestController {

    private final FileInfoService fileInfoService;

    @GetMapping("/test")
    public R get() {
        return R.success(fileInfoService.getById(1L));
    }
}
