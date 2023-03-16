package com.steccn.qms.controller;

import com.steccn.common.vo.R;
import com.steccn.qms.service.QtComplainTypeService;
import com.steccn.qms.dto.QtComplainTypeDTO;
import com.steccn.qms.vo.QtComplainTypeVO;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import java.util.List;

/**
 * @author yequan
 * @desc 投诉类型
 * @date 2023/3/15
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/qms/complain/type")
@Slf4j
public class QtComplainTypeController {

    private final QtComplainTypeService qtComplainTypeService;

    @GetMapping
    @ApiOperation(value = "投诉类型-列表")
    public R<List<QtComplainTypeVO>> typeList() {
        return R.success(qtComplainTypeService.typeList());
    }

    @PostMapping
    @ApiOperation(value = "投诉类型-保存")
    public R<Boolean> save(@Valid @RequestBody QtComplainTypeDTO dto) {
        return R.success(qtComplainTypeService.saveType(dto));
    }

    @DeleteMapping("/{id}")
    @ApiOperation(value = "投诉类型-删除")
    public R<Boolean> delete(@PathVariable("id") Integer id) {
        return R.success(qtComplainTypeService.delete(id));
    }
}

