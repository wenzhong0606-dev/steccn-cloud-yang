package com.steccn.qms.controller;

import com.steccn.common.vo.R;
import com.steccn.qms.service.QtMaterialLinkCheckPlanService;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 物料关联检验方案
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/qms/materiallinkcheckplan")
@Slf4j
public class QtMaterialLinkCheckPlanController {
    private final QtMaterialLinkCheckPlanService qtMaterialLinkCheckPlanService;

    @GetMapping("/{id}")
    @ApiOperation(value = "根据物料id查询检验类型")
    public R<List<Integer>> getCheckType(@PathVariable("id") Long id ){
        return R.success(qtMaterialLinkCheckPlanService.getCheckType(id));
    }
}
