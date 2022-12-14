package com.steccn.ems.controller;

import com.alibaba.excel.util.MapUtils;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.common.vo.EpTreeGraphVO;
import com.steccn.common.vo.R;
import com.steccn.ems.dto.EpEnergyFlowDTO;
import com.steccn.ems.dto.EpEnergyFlowLeaderDTO;
import com.steccn.ems.dto.EpSectionPriceDTO;
import com.steccn.ems.service.EpEnergyFlowService;
import com.steccn.ems.vo.EpConsumeDetailVO;
import com.steccn.ems.vo.EpEnergyMoneyVO;
import com.steccn.qms.constant.QtManagerConstant;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.util.Map;

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

    @PostMapping("/leader/chart")
    @ApiOperation(value = "电流流向领导驾驶舱-图表")
    public R<EpTreeGraphVO> getLeaderChart(@RequestBody @Valid EpEnergyFlowLeaderDTO dto) {
        return R.success(epEnergyFlowService.generateLeaderCharts(dto));
    }

    @PostMapping("/download")
    @ApiOperation(value = "能源流向-下载")
    public void getDetail(@RequestBody @Valid EpEnergyFlowDTO dto,HttpServletResponse response) throws IOException {
        try{
            epEnergyFlowService.downloadFlow(dto,response);
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
