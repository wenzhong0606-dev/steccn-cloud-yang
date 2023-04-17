package com.steccn.qms.controller;

import com.steccn.common.vo.R;
import com.steccn.qms.dto.QtComplainTypeDTO;
import com.steccn.qms.dto.QtDealWayDTO;
import com.steccn.qms.service.QtComplainTypeService;
import com.steccn.qms.service.QtDealWayService;
import com.steccn.qms.vo.QtComplainTypeVO;
import com.steccn.qms.vo.QtDealWayVO;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * @author yequan
 * @desc 处理方式
 * @date 2023/3/15
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/qms/deal/way")
@Slf4j
public class QtDealWayController {

    private final QtDealWayService qtDealWayService;

    @GetMapping("/{type}")
    @ApiOperation(value = "处理方式-列表")
    public R<List<QtDealWayVO>> wayList(@PathVariable("type") Integer type) {
        return R.success(qtDealWayService.wayList(type));
    }

    @PostMapping
    @ApiOperation(value = "处理方式-保存")
    public R<Boolean> save(@Valid @RequestBody QtDealWayDTO dto) {
        return R.success(qtDealWayService.saveWay(dto));
    }

    @DeleteMapping("/{id}")
    @ApiOperation(value = "处理方式-删除")
    public R<Boolean> delete(@PathVariable("id") Integer id) {
        return R.success(qtDealWayService.delete(id));
    }
}

