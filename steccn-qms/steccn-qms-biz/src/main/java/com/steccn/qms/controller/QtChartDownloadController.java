package com.steccn.qms.controller;

import com.alibaba.excel.util.MapUtils;
import com.alibaba.fastjson.JSON;
import com.steccn.common.annotation.SysLog;
import com.steccn.common.valid.InsertGroup;
import com.steccn.common.vo.R;
import com.steccn.qms.constant.QtManagerConstant;
import com.steccn.qms.dto.QtChartYieldDownloadDTO;
import com.steccn.qms.dto.QtCheckItemSaveDTO;
import com.steccn.qms.service.QtChartDownloadService;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

/**
 * 下载图表数据
 *
 * @author AndrewLuo
 * @date 2022/11/09
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/qms/chart/download")
@Slf4j
public class QtChartDownloadController {
    private final QtChartDownloadService qtChartDownloadService;

    @PostMapping("/substrate")
    @ApiOperation(value = "下载基片成品率数据")
    @SysLog("下载基片成品率数据")
    public void downloadSubstrate(@RequestBody @Validated QtChartYieldDownloadDTO dto, HttpServletResponse response) throws IOException {
        try{
            qtChartDownloadService.downloadSubstrate(response,dto);
        } catch (Exception e) {
            // 若有异常则重置response
            this.exceptionResponse(response,e);
        }
    }
    @GetMapping("/deep/process")
    @ApiOperation(value = "下载基片成品率数据")
    @SysLog("下载基片成品率数据")
    public void downloadDeepProcess(HttpServletResponse response) throws IOException {
        try{
            qtChartDownloadService.downloadDeepProcess(response);
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
    protected void exceptionResponse(HttpServletResponse response ,Exception e) throws IOException{
        response.reset();
        response.setContentType("application/json");
        response.setCharacterEncoding(QtManagerConstant.QT_CHART_EXCEL_ENCODE);
        Map<String, String> map = MapUtils.newHashMap();
        map.put("status", "failure");
        map.put("message", "下载文件失败" + e.getMessage());
        response.getWriter().println(JSON.toJSONString(map));
    }

//    @PostMapping("/substrate/details")
//    @ApiOperation(value = "下载基片成品率数据详情")
//    @SysLog("下载基片成品率数据")
//    public void downloadSubstrate(@RequestBody @Validated QtChartYieldDownloadDTO dto) throws IOException {
//        try{
//            qtChartDownloadService.downloadSubstrate(response,dto);
//        } catch (Exception e) {
//            // 若有异常则重置response
//            this.exceptionResponse(response,e);
//        }
//    }

}
