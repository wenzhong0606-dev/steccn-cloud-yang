package com.steccn.qms.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.common.valid.InsertGroup;
import com.steccn.common.valid.UpdateGroup;
import com.steccn.common.vo.R;
import com.steccn.qms.dto.*;
import com.steccn.qms.service.QtDefectiveItemService;
import com.steccn.qms.vo.QtCheckItemListVO;
import com.steccn.qms.vo.QtDefectiveItemTreeVo;
import com.steccn.qms.vo.QtDefectiveItemVO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.io.Serializable;
import java.util.List;

/**
 * @desc 缺陷项类别管理
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/qms/defective/item")
@Slf4j
@ApiModel("缺陷项控制器")
public class QtDefectiveItemController implements Serializable {

    private final QtDefectiveItemService qtDefectiveItemService;

    @PostMapping("/page")
    @ApiOperation(value = "缺陷项列表")
    public R<Page<QtDefectiveItemVO>> pageList(@Valid @RequestBody QtDefectiveItemPageDTO dto) {
        return R.success(qtDefectiveItemService.pageList(dto));
    }

    @PostMapping
    @ApiOperation(value = "新建缺陷项")
    public R<Boolean> add(@Validated(InsertGroup.class) @RequestBody QtDefectiveItemSaveDTO dto) {
        return R.success(qtDefectiveItemService.add(dto));
    }

    @PutMapping
    @ApiOperation(value = "编辑缺陷项")
    public R<Boolean> edit(@Validated(UpdateGroup.class) @RequestBody QtDefectiveItemSaveDTO dto) {
        return R.success(qtDefectiveItemService.edit(dto));
    }

    @PutMapping("/status")
    @ApiOperation(value = "停用启用缺陷项")
    public R<Boolean> status(@Valid @RequestBody QtItemStatusDTO dto) {
        return R.success(qtDefectiveItemService.status(dto));
    }

    @DeleteMapping("/{id}")
    @ApiOperation(value = "删除缺陷项")
    public R<Boolean> delete(@PathVariable("id") Integer id) {
        return R.success(qtDefectiveItemService.delete(id));
    }

    @PostMapping("/select/item")
    @ApiOperation(value = "启用状态缺陷项列表")
    public R<List<QtDefectiveItemTreeVo>> selectItemList() {
        return R.success(qtDefectiveItemService.selectItemList());
    }
}
