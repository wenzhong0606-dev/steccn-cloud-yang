package com.steccn.qms.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.common.annotation.SysLog;
import com.steccn.common.valid.InsertGroup;
import com.steccn.common.valid.UpdateGroup;
import com.steccn.common.vo.R;
import com.steccn.platform.vo.SysOfficeVO;
import com.steccn.qms.dto.QtCheckItemSaveDTO;
import com.steccn.qms.dto.QtCheckItemPageDTO;
import com.steccn.qms.dto.QtItemStatusDTO;
import com.steccn.qms.service.QtCheckItemService;
import com.steccn.qms.vo.QtCheckItemListVO;
import com.steccn.qms.vo.QtRangeArrayVO;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
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

    @PostMapping("/page")
    @ApiOperation(value = "检验项列表")
    public R<Page<QtCheckItemListVO>> pageList(@Valid @RequestBody QtCheckItemPageDTO qtCheckItemPageDTO) {
        return R.success(qtCheckItemService.pageList(qtCheckItemPageDTO));
    }

    @PostMapping
    @ApiOperation(value = "新建检验项")
    @SysLog("新建检验项")
    public R<Boolean> add(@Validated(InsertGroup.class) @RequestBody QtCheckItemSaveDTO dto) {
        return R.success(qtCheckItemService.add(dto));
    }

    @GetMapping("/{id}")
    @ApiOperation(value = "检验项详情")
    public R<QtCheckItemListVO> info(@PathVariable("id") Integer id) {
        return R.success(qtCheckItemService.info(id));
    }

    @PutMapping
    @ApiOperation(value = "编辑检验项")
    @SysLog("编辑检验项")
    public R<Boolean> edit(@Validated(UpdateGroup.class) @RequestBody QtCheckItemSaveDTO dto) {
        return R.success(qtCheckItemService.edit(dto));
    }

    @PutMapping("/status")
    @ApiOperation(value = "检验项停用启用")
    @SysLog("检验项停用启用")
    public R<Boolean> status(@Valid @RequestBody QtItemStatusDTO dto) {
        return R.success(qtCheckItemService.status(dto));
    }

    @DeleteMapping("/{id}")
    @ApiOperation(value = "删除检验项")
    @SysLog("删除检验项")
    public R<Boolean> delete(@PathVariable("id") Integer id) {
        return R.success(qtCheckItemService.delete(id));
    }

    @PostMapping("/select/item")
    @ApiOperation(value = "启用状态检验项列表")
    public R<List<QtCheckItemListVO>> selectItemList() {
        return R.success(qtCheckItemService.selectItemList());
    }

    @PostMapping("/select/station")
    @ApiOperation(value = "查询工位列表")
    public R<List<SysOfficeVO>> selectStationList(@RequestBody List<String> typeList) {
        return qtCheckItemService.selectStationList(typeList);
    }


}
