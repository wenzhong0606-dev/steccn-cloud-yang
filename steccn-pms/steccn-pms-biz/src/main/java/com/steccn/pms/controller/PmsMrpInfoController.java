package com.steccn.pms.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.common.vo.R;
import com.steccn.pms.domain.PmsMrpInfo;
import com.steccn.pms.dto.PmsMrpInfoPageDTO;
import com.steccn.pms.service.PmsMrpInfoService;
import com.steccn.pms.vo.PmsMrpInfoVO;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 物料需求计划详情控制器
 *
 * @author Mongknife
 * @date 2022/11/9
 */

@RestController
@RequestMapping("/pms/mrpInfo")
@RequiredArgsConstructor
@Slf4j
public class PmsMrpInfoController {

    private final PmsMrpInfoService pmsMrpInfoService;

    @ApiOperation("物料需求计划详情")
    @PostMapping
    public R<Page<PmsMrpInfoVO>> infoPage(@RequestBody PmsMrpInfoPageDTO dto){
        return R.success(pmsMrpInfoService.infoPage(dto));
    }

}
