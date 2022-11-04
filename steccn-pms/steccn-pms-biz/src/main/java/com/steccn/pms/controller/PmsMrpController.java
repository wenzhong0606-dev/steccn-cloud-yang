package com.steccn.pms.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.common.vo.R;
import com.steccn.pms.dto.PmsMrpPageDTO;
import com.steccn.pms.service.PmsMrpService;
import com.steccn.pms.vo.MaterialRequisitionVO;
import com.steccn.pms.vo.PmsMrpVO;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 物料需求计划控制器
 *
 * @author Mongknife
 * @date 2022/11/4
 */
@RestController
@RequestMapping("/pms/mrp")
@RequiredArgsConstructor
@Slf4j
public class PmsMrpController {

    private final PmsMrpService pmsMrpService;

    @ApiOperation("物料需求计划列表")
    @PostMapping
    public R<Page<PmsMrpVO>> pageList(@RequestBody PmsMrpPageDTO dto) {
        return R.success(pmsMrpService.pageList(dto));
    }

}
