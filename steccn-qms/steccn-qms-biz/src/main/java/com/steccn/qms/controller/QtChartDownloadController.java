package com.steccn.qms.controller;

import com.alibaba.excel.util.MapUtils;
import com.alibaba.fastjson.JSON;
import com.steccn.common.annotation.SysLog;
import com.steccn.common.valid.InsertGroup;
import com.steccn.common.vo.R;
import com.steccn.qms.constant.QtManagerConstant;
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

    @GetMapping("/substrate")
    @ApiOperation(value = "新建检验项")
    @SysLog("下载基片成品率数据")
    public void downloadSubstrate(HttpServletResponse response) throws IOException {
        try{
            qtChartDownloadService.downloadSubstrate(response);
        } catch (Exception e) {
            // 若有异常则重置response
            response.reset();
            response.setContentType("application/json");
            response.setCharacterEncoding(QtManagerConstant.QT_CHART_EXCEL_ENCODE);
            Map<String, String> map = MapUtils.newHashMap();
            map.put("status", "failure");
            map.put("message", "下载文件失败" + e.getMessage());
            response.getWriter().println(JSON.toJSONString(map));
        }
    }
}
