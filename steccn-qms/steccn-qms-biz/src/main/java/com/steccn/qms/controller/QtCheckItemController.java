package com.steccn.qms.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.common.vo.R;
import com.steccn.qms.dto.QtCheckItemAddDTO;
import com.steccn.qms.dto.QtCheckItemPageDTO;
import com.steccn.qms.service.QtCheckItemService;
import com.steccn.qms.vo.QtCheckItemPageVO;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @desc 检验项管理
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/qms/check/item")
@Slf4j
public class QtCheckItemController {

    private final QtCheckItemService qtCheckItemService;

    @PostMapping("/list")
    @ApiOperation(value = "检验项列表")
    public R<Page<QtCheckItemPageVO>> pageList(@RequestBody QtCheckItemPageDTO qtCheckItemPageDTO) {
        return R.success(qtCheckItemService.pageList(qtCheckItemPageDTO));
    }

    @PostMapping
    @ApiOperation(value = "新建检验项")
    public R<Boolean> add(@RequestBody QtCheckItemAddDTO qtCheckItemAddDTO) {
        return R.success(qtCheckItemService.add(qtCheckItemAddDTO));
    }
}
