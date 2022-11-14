package com.steccn.pms.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.common.vo.R;
import com.steccn.pms.dto.MaterialRequisitionDTO;
import com.steccn.pms.dto.MaterialRequisitionInfoPlanDTO;
import com.steccn.pms.dto.MaterialRequisitionPlanDTO;
import com.steccn.pms.service.PmsMaterialRequisitionService;
import com.steccn.pms.vo.MaterialRequisitionPlanVO;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 类说明
 *
 * @author Mongknife
 * @date 2022/11/11
 */
@RestController
@RequestMapping("/pms/material/requisition/plan")
@RequiredArgsConstructor
@Slf4j
public class MaterialRequisitionPlanController {

    private final PmsMaterialRequisitionService pmsMaterialRequisitionService;

    @ApiOperation("领料计划")
    @PostMapping("/list")
    public R<Page<MaterialRequisitionPlanVO>> planPageList(@RequestBody MaterialRequisitionPlanDTO dto) {
        return R.success(pmsMaterialRequisitionService.planPageList(dto));
    }

    @ApiOperation("创建领用单")
    @PostMapping
    public R<String> create(@RequestBody List<MaterialRequisitionInfoPlanDTO> dtoList) {
        return R.success(pmsMaterialRequisitionService.create(dtoList));
    }

}
