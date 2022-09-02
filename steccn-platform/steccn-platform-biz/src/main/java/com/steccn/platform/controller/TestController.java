package com.steccn.platform.controller;

import com.steccn.common.vo.R;
import com.steccn.platform.domain.SysDict;
import com.steccn.platform.service.SysDictService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/platform")
public class TestController {

    private final SysDictService sysDictService;

    @GetMapping("/test")
    public R test(){
        return R.success(sysDictService.getById(1));
    }
}
