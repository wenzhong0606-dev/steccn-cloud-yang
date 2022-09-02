package com.steccn.qms.controller;

import com.steccn.common.vo.R;
import com.steccn.qms.service.QtCheckItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/qms")
public class TestController {

    private final QtCheckItemService qtCheckItemService;


    @GetMapping("/test")
    public R test() {
        return R.success("success");
    }

    @DeleteMapping("/{id}")
    public R remove(@PathVariable("id") Long id) {
        boolean remove = qtCheckItemService.removeById(id);
        return R.success(remove);
    }
}
