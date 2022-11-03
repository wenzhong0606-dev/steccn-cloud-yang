package com.steccn.pms.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.common.annotation.SysLog;
import com.steccn.common.valid.InsertGroup;
import com.steccn.common.valid.UpdateGroup;
import com.steccn.common.vo.R;
import com.steccn.pms.dto.MaterialRequisitionApprovalDTO;
import com.steccn.pms.dto.MaterialRequisitionDTO;
import com.steccn.pms.dto.MaterialRequisitionPageDTO;
import com.steccn.pms.service.PmsMaterialRequisitionService;
import com.steccn.pms.vo.MaterialRequisitionVO;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 类说明
 *
 * @author Mongknife
 * @date 2022/11/3
 */
@RestController
@RequestMapping("/pms/material/requisition")
@RequiredArgsConstructor
@Slf4j
public class MaterialRequisitionController {

    private final PmsMaterialRequisitionService pmsMaterialRequisitionService;

    @ApiOperation("生产领料列表")
    @PostMapping("/list")
    public R<Page<MaterialRequisitionVO>> pageList(@RequestBody MaterialRequisitionPageDTO materialRequisitionPageDTO) {
        return R.success(pmsMaterialRequisitionService.pageList(materialRequisitionPageDTO));
    }

    @ApiOperation("生产领料单新增")
    @PostMapping
    @SysLog("生产领料单新增")
    public R<Boolean> add(@Validated(InsertGroup.class) @RequestBody MaterialRequisitionDTO materialRequisitionDTO) {
        return R.success(pmsMaterialRequisitionService.add(materialRequisitionDTO));
    }

    @ApiOperation("生产领料单编辑")
    @PutMapping
    @SysLog("生产领料单编辑")
    public R<Boolean> edit(@Validated(UpdateGroup.class) @RequestBody MaterialRequisitionDTO materialRequisitionDTO) {
        return R.success(pmsMaterialRequisitionService.edit(materialRequisitionDTO));
    }

    @ApiOperation("生产领料单删除")
    @DeleteMapping("/{id}")
    @SysLog("生产领料单删除")
    public R<Boolean> delete(@PathVariable("id") Long id) {
        return R.success(pmsMaterialRequisitionService.delete(id));
    }

    @ApiOperation("领料单详情")
    @GetMapping("/{id}")
    public R<MaterialRequisitionVO> get(@PathVariable("id") Long id) {
        return R.success(pmsMaterialRequisitionService.get(id));
    }

    @ApiOperation("领料单审批")
    @PutMapping("/approval")
    @SysLog("领料单审批")
    public R<Boolean> approval(@Validated(UpdateGroup.class) @RequestBody MaterialRequisitionApprovalDTO dto) {
        return R.success(pmsMaterialRequisitionService.approval(dto));
    }
}
