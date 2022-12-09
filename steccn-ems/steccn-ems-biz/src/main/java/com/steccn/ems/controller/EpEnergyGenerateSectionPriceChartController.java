package com.steccn.ems.controller;

import com.alibaba.excel.util.MapUtils;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.common.vo.EpChartGraphVO;
import com.steccn.common.vo.EpChartGraphXAxisVO;
import com.steccn.common.vo.R;
import com.steccn.ems.dto.EpSectionPriceDTO;
import com.steccn.ems.dto.EpSectionPriceLeaderDTO;
import com.steccn.ems.dto.EpTableDTO;
import com.steccn.ems.service.EpEnergyGenerateSectionPriceChart;
import com.steccn.ems.vo.EpEnergyChartPieDisVO;
import com.steccn.ems.vo.EpEnergyMoneyVO;
import com.steccn.qms.constant.QtManagerConstant;
import com.steccn.qms.dto.QtChartYieldDownloadDTO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Map;

/**
 * 峰平谷图表相关控制器
 *
 * @author AndrewLuo
 * @date 2022/12/08
 */
@Api("峰平谷图表相关")
@RestController
@RequiredArgsConstructor
@RequestMapping("/ems/consume/distribution")
public class EpEnergyGenerateSectionPriceChartController {
    private final EpEnergyGenerateSectionPriceChart epEnergyGenerateSectionPriceChart;

    @PostMapping("/detail")
    @ApiOperation(value = "峰平谷对比-详情")
    public R<Page<EpEnergyMoneyVO>> getDisDetail(@RequestBody EpSectionPriceDTO dto) {
        return R.success(epEnergyGenerateSectionPriceChart.getDistributionDetails(dto));
    }

    @PostMapping("/chart")
    @ApiOperation(value = "峰平谷对比-图表")
    public R<EpEnergyChartPieDisVO> getDisChart(@RequestBody EpSectionPriceDTO dto) {
        return R.success(epEnergyGenerateSectionPriceChart.generateDistribution(dto));
    }

    @PostMapping("/chart/leader")
    @ApiOperation(value = "峰平谷对比-图表-领导驾驶舱")
    public R<EpEnergyChartPieDisVO> getDisChartLeader(@RequestBody EpSectionPriceLeaderDTO dto) {
        return R.success(epEnergyGenerateSectionPriceChart.generateDistributionLeader(dto));
    }

    @PostMapping("/download")
    @ApiOperation(value = "峰平谷下载-图表")
    public void downloadDis (@RequestBody EpSectionPriceDTO dto, HttpServletResponse response) throws IOException {
        try{
            epEnergyGenerateSectionPriceChart.downloadDistribution(dto,response);
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
}
