package com.steccn.qms.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.common.vo.R;
import com.steccn.qms.dto.*;
import com.steccn.qms.service.QtCheckPlanService;
import com.steccn.qms.vo.QtCheckPlanDetailsVO;
import com.steccn.qms.vo.QtCheckPlanListAllVO;
import com.steccn.qms.vo.QtCheckPlanVO;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 类说明
 *
 * @author AndrewLuo
 * @date 2022/09/06
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/qms/check/plan")
@Slf4j
public class QtCheckPlanController {

    private final QtCheckPlanService qtCheckPlanService;

    @PostMapping
    @ApiOperation(value = "新建检验方案")
    public R<Boolean> add(@RequestBody QtCheckPlanDTO dto) {
        return R.success(qtCheckPlanService.add(dto));
    }

    @PostMapping("/list")
    @ApiOperation(value = "检验方案列表")
    public R<Page<QtCheckPlanVO>> pageList(@RequestBody QtCheckPlanPageDTO qtCheckPlanPageDTO) {
        return R.success(qtCheckPlanService.pageList(qtCheckPlanPageDTO));
    }

    @GetMapping("/{id}")
    @ApiOperation(value = "检验方案回显")
    public R<QtCheckPlanDetailsVO> info(@PathVariable Integer id) {
        return R.success(qtCheckPlanService.info(id,null));
    }

    @PutMapping
    @ApiOperation(value = "修改检验方案的值")
    public R<Boolean> edit(@RequestBody QtCheckPlanDTO dto){
        return R.success(qtCheckPlanService.edit(dto));
    }

    @PutMapping("/status")
    @ApiOperation(value = "停用启用")
    public R<Boolean> status(@Valid @RequestBody QtCheckPlanStausDTO dto) {
        return R.success(qtCheckPlanService.status(dto));
    }

    @DeleteMapping("/{id}")
    @ApiOperation(value = "删除检验方案")
    public R<Boolean> delete(@PathVariable("id") Integer id) {
        return R.success(qtCheckPlanService.delete(id));
    }

    @GetMapping("/listAll")
    @ApiOperation(value = "查询所有启用的检验方案")
    public R<List<QtCheckPlanListAllVO>> pageList() {
        return R.success(qtCheckPlanService.listAll());
    }
}
