package com.steccn.qms.controller;

import com.steccn.common.vo.R;
import com.steccn.qms.mapper.QtCheckItemMapper;
import com.steccn.qms.service.QtCheckItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * @desc 仅用于测试controller
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/qms")
public class TestController {

    private final QtCheckItemService qtCheckItemService;

    private final QtCheckItemMapper qtCheckItemMapper;

    @GetMapping("/{id}")
    public R getById(@PathVariable("id") Long id) {
        return R.success(qtCheckItemService.getById(id));
    }

    @DeleteMapping("/{id}")
    public R remove(@PathVariable("id") Long id) {
        return R.success(qtCheckItemService.removeById(id));
    }
}
