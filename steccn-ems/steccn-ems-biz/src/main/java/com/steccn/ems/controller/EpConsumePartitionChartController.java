package com.steccn.ems.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.common.vo.EpChartGraphVO;
import com.steccn.common.vo.EpChartGraphXAxisVO;
import com.steccn.common.vo.R;
import com.steccn.ems.dto.EpTableDTO;
import com.steccn.ems.service.EpConsumePartitionChartService;
import com.steccn.ems.vo.EpConsumeDetailVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import javax.validation.Valid;
import java.math.BigDecimal;

/**
 * @author yequan
 * @desc 能耗分区图控制器
 */
@Api("能源类型")
@RestController
@RequiredArgsConstructor
@RequestMapping("/ems/consume/partition")
public class EpConsumePartitionChartController {

    private final EpConsumePartitionChartService epConsumePartitionChartService;

    @PostMapping("/contrast/chart")
    @ApiOperation(value = "能耗分区对比-柱状图")
    public R<EpChartGraphVO<EpChartGraphXAxisVO, BigDecimal>> consumePartitionContrastChart(@Valid @RequestBody EpTableDTO dto) {
        return R.success(epConsumePartitionChartService.consumePartitionContrast(dto));
    }

    @PostMapping("/contrast/detail")
    @ApiOperation(value = "能耗分区对比-明细")
    public R<EpConsumeDetailVO> consumePartitionContrastDetail(@Valid @RequestBody EpTableDTO dto) {
        return R.success(epConsumePartitionChartService.consumePartitionContrastDetail(dto));
    }

    @PostMapping("/history/chart")
    @ApiOperation(value = "能耗历史对比-柱状图")
    public R<EpChartGraphVO<EpChartGraphXAxisVO, BigDecimal>> consumeHistoryContrast(@Valid @RequestBody EpTableDTO dto) {
        return R.success();
    }

    @PostMapping("/history/detail")
    @ApiOperation(value = "能耗历史对比-明细")
    public R<Page<EpConsumeDetailVO>> consumeHistoryContrastDetail(@Valid @RequestBody EpTableDTO dto) {
        return R.success();
    }
}
