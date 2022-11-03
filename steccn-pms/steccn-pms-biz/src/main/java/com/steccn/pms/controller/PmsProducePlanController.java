package com.steccn.pms.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.common.vo.R;
import com.steccn.pms.domain.PmsProducePlan;
import com.steccn.pms.dto.PmsProducePlanDTO;
import com.steccn.pms.dto.ProcessReportWorkPageDTO;
import com.steccn.pms.service.PmsProducePlanService;
import com.steccn.pms.vo.ProcessReportWorkPageVO;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author chenhuan
 * @date 2022/11/3 14:07
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/pms/produce/plan")
public class PmsProducePlanController {

    private final PmsProducePlanService pmsProducePlanService;

    @ApiOperation("月计划列表")
    @GetMapping
    public R<List<PmsProducePlanDTO>> list(@RequestParam String monthDate) {
        return null;
    }

    @ApiOperation("月计划批量新增")
    @PostMapping
    public R<Boolean> addBatch(@RequestBody PmsProducePlanDTO dto) {
        return R.success(pmsProducePlanService.addOrEditBatch(dto));
    }

    @ApiOperation("月计划批量编辑")
    @PutMapping
    public R<Boolean> editBatch(@RequestBody PmsProducePlanDTO dto) {
        return R.success(pmsProducePlanService.addOrEditBatch(dto));
    }

}
