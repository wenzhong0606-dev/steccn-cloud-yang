package com.steccn.pms.controller.feign;

import com.steccn.common.annotation.SysLog;
import com.steccn.common.valid.UpdateGroup;
import com.steccn.common.vo.R;
import com.steccn.pms.dto.MaterialRequisitionApprovalDTO;
import com.steccn.pms.service.PmsMaterialRequisitionService;
import com.steccn.pms.service.PmsMaterialRequisitionWorkflowService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 领用单feign
 *
 * @author AndrewLuo
 * @date 2023/03/23
 */
@RestController
@Api("领料单feign")
@RequestMapping("/feign/pms/material/requisition")
@RequiredArgsConstructor
@Slf4j
public class MaterialRequisitionFeignController {
    private final PmsMaterialRequisitionService pmsMaterialRequisitionService;
    private final PmsMaterialRequisitionWorkflowService pmsMaterialRequisitionWorkflowService;

    @ApiOperation("领料单审批")
    @PutMapping("/approval")
    @SysLog("领料单审批")
    public R<Boolean> approval(@RequestBody MaterialRequisitionApprovalDTO dto) {
        return R.success(pmsMaterialRequisitionService.approval(dto));
    }

    @ApiOperation("根据流程id查询业务id")
    @GetMapping("/requestId")
    public R<Long> getIdByRequestId(@RequestParam("requestId") Long requestId) {
        return R.success(pmsMaterialRequisitionWorkflowService.getIdByRequestId(requestId));
    }
}
