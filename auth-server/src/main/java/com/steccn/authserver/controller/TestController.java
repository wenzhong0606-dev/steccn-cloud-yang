package com.steccn.authserver.controller;

import com.steccn.common.vo.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class TestController {

    @GetMapping("/user/test")
    public R test() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        log.info("authentication ->{}", authentication);
        return R.success("success");
    }

    @PostMapping("/user/test1")
    public R test1() {
        return R.success("success1");
    }
}
