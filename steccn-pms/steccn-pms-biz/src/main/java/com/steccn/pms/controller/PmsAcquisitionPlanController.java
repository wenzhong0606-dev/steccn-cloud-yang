package com.steccn.pms.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.common.vo.R;
import com.steccn.pms.dto.PmsAcquisitionPlanPageDTO;
import com.steccn.pms.service.PmsAcquisitionPlanService;
import com.steccn.pms.vo.PmsAcquisitionPlanVO;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 领料计划
 *
 * @author Mongknife
 * @date 2022/11/4
 */


@RestController
@RequestMapping("/pms/acquisition/plan")
@RequiredArgsConstructor
@Slf4j
public class PmsAcquisitionPlanController {

    private final PmsAcquisitionPlanService pmsAcquisitionPlanService;

    @ApiOperation("领料计划分页列表")
    @PostMapping("/list")
    public R<Page<PmsAcquisitionPlanVO>> pageList(@RequestBody PmsAcquisitionPlanPageDTO dto) {
        return R.success(pmsAcquisitionPlanService.pageList(dto));
    }

}
