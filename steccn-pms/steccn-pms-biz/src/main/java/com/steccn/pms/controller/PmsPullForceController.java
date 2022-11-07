package com.steccn.pms.controller;

import com.steccn.common.vo.R;
import com.steccn.pms.dto.PmsProducePlanDTO;
import com.steccn.pms.dto.PmsPullForceDTO;
import com.steccn.pms.dto.PmsPullForceDetailsDTO;
import com.steccn.pms.dto.PmsPullForceEditDTO;
import com.steccn.pms.service.PmsPullForceService;
import com.steccn.pms.vo.PmsPullForceDetailsVO;
import com.steccn.pms.vo.PmsPullForceVO;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * @author chenhuan
 * @date 2022/11/7 10:37
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/pms/pull/force")
public class PmsPullForceController {

    private final PmsPullForceService pmsPullForceService;

    @ApiOperation("拉引量列表")
    @GetMapping("/list")
    public R<List<PmsPullForceVO>> list(@RequestParam Integer workshopId, @RequestParam String monthDate) {
        return R.success(pmsPullForceService.list(workshopId, monthDate));
    }

    @ApiOperation("拉引量详情")
    @GetMapping("/details")
    public R<List<PmsPullForceDetailsVO>> details(@RequestParam Integer workshopId, @RequestParam String pullForceDate) {
        return R.success(pmsPullForceService.details(workshopId, pullForceDate));
    }

    @ApiOperation("拉引量详情批量编辑")
    @PutMapping
    public R<Boolean> editBatch(@RequestBody List<PmsPullForceEditDTO> dtoList) {
        return R.success(pmsPullForceService.editBatch(dtoList));
    }
}
