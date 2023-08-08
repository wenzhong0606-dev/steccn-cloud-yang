package com.steccn.mes.controller;

import com.alibaba.excel.EasyExcel;
import com.steccn.mes.service.MesEqpSpareService;
import com.steccn.mes.vo.MesSpareDownloadVO;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;

/**
 * 类说明
 *
 * @author AndrewLuo
 * @date 2023/08/04
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/mes/spare")
@Slf4j
public class MesSpareController {

    private final MesEqpSpareService mesEqpSpareService;

    @GetMapping("/download")
    @ApiOperation(value = "下载备品备件")
    public void download(HttpServletResponse response) throws IOException {
        // 这里注意 有同学反应使用swagger 会导致各种问题，请直接用浏览器或者用postman
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        // 这里URLEncoder.encode可以防止中文乱码 当然和easyexcel没有关系
        String fileName = URLEncoder.encode("备品备件", "UTF-8").replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
        EasyExcel.write(response.getOutputStream(), MesSpareDownloadVO.class).sheet("备品备件").doWrite(mesEqpSpareService.downloadSpare());
    }
}
