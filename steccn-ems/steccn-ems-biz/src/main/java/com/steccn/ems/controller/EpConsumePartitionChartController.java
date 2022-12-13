package com.steccn.ems.controller;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.authserver.exception.BusinessException;
import com.steccn.authserver.exception.SteccnChartException;
import com.steccn.authserver.util.SteccnTableAssertUtil;
import com.steccn.common.vo.BaseSelectVO;
import com.steccn.common.vo.EpChartGraphVO;
import com.steccn.common.vo.EpChartGraphXAxisVO;
import com.steccn.common.vo.R;
import com.steccn.ems.dto.EpTableDTO;
import com.steccn.ems.service.EpConsumePartitionChartService;
import com.steccn.ems.vo.ConsumeHistoryContrastDetailVO;
import com.steccn.ems.vo.EpConsumeDetailVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

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

    @PostMapping("/thick")
    @ApiOperation(value = "厚度选项")
    public R<List<BaseSelectVO<BigDecimal>>> thick(@RequestBody EpTableDTO dto) {
        return R.success(epConsumePartitionChartService.thick(dto));
    }

    @PostMapping("/contrast/chart")
    @ApiOperation(value = "能耗分区对比-柱状图")
    public R<EpChartGraphVO<EpChartGraphXAxisVO, BigDecimal>> consumePartitionContrastChart(@Valid @RequestBody EpTableDTO dto) {
        Assert.notNull(dto.getGranularity(),"时间粒度不可为空");
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
        Assert.notNull(dto.getGranularity(),"时间粒度不可为空");
        Assert.notNull(dto.getHistoryStart(),"对比开始时间不可为空");
        Assert.notNull(dto.getHistoryEnd(),"对比结束时间不可为空");
        return R.success(epConsumePartitionChartService.consumeHistoryContrast(dto));
    }

    @PostMapping("/history/detail")
    @ApiOperation(value = "能耗历史对比-明细")
    public R<Page<ConsumeHistoryContrastDetailVO>> consumeHistoryContrastDetail(@Valid @RequestBody EpTableDTO dto) {
        Assert.notNull(dto.getHistoryStart(),"对比开始时间不可为空");
        Assert.notNull(dto.getHistoryEnd(),"对比结束时间不可为空");
        return R.success(epConsumePartitionChartService.consumeHistoryContrastDetail(dto));
    }

    @PostMapping("/unit/chart")
    @ApiOperation(value = "单耗班组对比-柱状图")
    public R<EpChartGraphVO<EpChartGraphXAxisVO, BigDecimal>> consumePartitionUnitChart(@Valid @RequestBody EpTableDTO dto) {
        SteccnTableAssertUtil.notEmpty(dto.getThick(), "产品厚度为空，图表无数据");
        if (CollUtil.isNotEmpty(dto.getThick()) && dto.getThick().size() != 1) {
            throw new BusinessException("产品厚度只能单选");
        }
        return R.success(epConsumePartitionChartService.consumePartitionUnitChart(dto));
    }
}
