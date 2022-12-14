package com.steccn.ems.controller;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import com.alibaba.excel.util.MapUtils;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.authserver.exception.BusinessException;
import com.steccn.authserver.util.SteccnTableAssertUtil;
import com.steccn.common.vo.BaseSelectVO;
import com.steccn.common.vo.EpChartGraphVO;
import com.steccn.common.vo.EpChartGraphXAxisVO;
import com.steccn.common.vo.R;
import com.steccn.ems.dto.EpSectionPriceHisDTO;
import com.steccn.ems.dto.EpTableDTO;
import com.steccn.ems.service.EpConsumePartitionChartService;
import com.steccn.ems.vo.ConsumeHistoryContrastDetailVO;
import com.steccn.ems.vo.ConsumePartitionUnitClassDetailVO;
import com.steccn.ems.vo.EpConsumeDetailVO;
import com.steccn.qms.constant.QtManagerConstant;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
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

    @PostMapping("/history/download")
    @ApiOperation(value = "能耗历史对比-下载")
    public void consumeHistoryContrastDownload(@Valid @RequestBody EpTableDTO dto, HttpServletResponse response) throws IOException {
        try{
            epConsumePartitionChartService.downloadHistoryConsume(dto,response);
        } catch (Exception e) {
            // 若有异常则重置response
            this.exceptionResponse(response,e);
        }
    }

    @PostMapping("/unit/class/chart")
    @ApiOperation(value = "单耗班组对比-柱状图")
    public R<EpChartGraphVO<EpChartGraphXAxisVO, BigDecimal>> consumePartitionUnitClassChart(@Valid @RequestBody EpTableDTO dto) {
        SteccnTableAssertUtil.notEmpty(dto.getThick(), "产品厚度为空，图表无数据");
        if (CollUtil.isNotEmpty(dto.getThick()) && dto.getThick().size() != 1) {
            throw new BusinessException("产品厚度只能单选");
        }
        SteccnTableAssertUtil.notEmpty(dto.getPartitionIdList(), "组织机构不能为空");
        return R.success(epConsumePartitionChartService.consumePartitionUnitClassChart(dto));
    }

    @PostMapping("/contrast/download")
    @ApiOperation(value = "能耗分区下载-图表")
    public void downloadHis (@Valid @RequestBody EpTableDTO dto, HttpServletResponse response) throws IOException {
        try{
            epConsumePartitionChartService.downloadConsume(dto,response);
        } catch (Exception e) {
            // 若有异常则重置response
            this.exceptionResponse(response,e);
        }
    }

    /**
     * 对响应异常进行封装
     * @param response
     * @throws IOException
     */
    protected void exceptionResponse(HttpServletResponse response , Exception e) throws IOException{
        response.reset();
        response.setContentType("application/json");
        response.setCharacterEncoding(QtManagerConstant.QT_CHART_EXCEL_ENCODE);
        Map<String, String> map = MapUtils.newHashMap();
        map.put("status", "failure");
        map.put("message", "下载文件失败" + e.getMessage());
        response.getWriter().println(JSON.toJSONString(map));
    }

    @PostMapping("/unit/class/detail")
    @ApiOperation(value = "单耗班组对比-明细")
    public  R<Page<ConsumePartitionUnitClassDetailVO>> consumePartitionUnitClassDetail(@Valid @RequestBody EpTableDTO dto) {
        SteccnTableAssertUtil.notEmpty(dto.getThick(), "产品厚度为空，图表无数据");
        if (CollUtil.isNotEmpty(dto.getThick()) && dto.getThick().size() != 1) {
            throw new BusinessException("产品厚度只能单选");
        }
        SteccnTableAssertUtil.notEmpty(dto.getPartitionIdList(), "组织机构不能为空");
        return R.success(epConsumePartitionChartService.consumePartitionUnitClassDetail(dto));
    }

    @PostMapping("/unit/history/chart")
    @ApiOperation(value = "单耗历史对比-柱状图")
    public  R<EpChartGraphVO<EpChartGraphXAxisVO, BigDecimal>> consumePartitionUnitHistoryChart(@Valid @RequestBody EpTableDTO dto) {
        return R.success();
    }

    @PostMapping("/unit/history/detail")
    @ApiOperation(value = "单耗历史对比-明细")
    public  R<Page<ConsumePartitionUnitClassDetailVO>> consumePartitionUnitHistoryDetail(@Valid @RequestBody EpTableDTO dto) {
        return R.success();
    }

}
