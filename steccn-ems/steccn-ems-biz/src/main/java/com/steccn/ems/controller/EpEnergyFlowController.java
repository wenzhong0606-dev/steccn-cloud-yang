package com.steccn.ems.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.common.vo.EpTreeGraphVO;
import com.steccn.common.vo.R;
import com.steccn.ems.dto.EpEnergyFlowDTO;
import com.steccn.ems.dto.EpSectionPriceDTO;
import com.steccn.ems.service.EpEnergyFlowService;
import com.steccn.ems.vo.EpConsumeDetailVO;
import com.steccn.ems.vo.EpEnergyMoneyVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * 能源流向
 *
 * @author AndrewLuo
 * @date 2022/12/12
 */
@Api("能源流向图表相关")
@RestController
@RequiredArgsConstructor
@RequestMapping("/ems/consume/flow")
public class EpEnergyFlowController {
    private final EpEnergyFlowService epEnergyFlowService;

    @PostMapping("/detail")
    @ApiOperation(value = "能源流向-详情")
    public R<EpConsumeDetailVO> getDetail(@RequestBody @Valid EpEnergyFlowDTO dto) {
        return R.success(epEnergyFlowService.getDetails(dto));
    }

    @PostMapping("/chart")
    @ApiOperation(value = "能源流向-图表")
    public R<EpTreeGraphVO> getChart(@RequestBody @Valid EpEnergyFlowDTO dto) {
        return R.success(epEnergyFlowService.generateCharts(dto));
    }
}
