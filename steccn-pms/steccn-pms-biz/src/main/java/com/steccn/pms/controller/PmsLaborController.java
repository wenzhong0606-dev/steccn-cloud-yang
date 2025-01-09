package com.steccn.pms.controller;

import com.alibaba.excel.util.MapUtils;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.common.annotation.SysLog;
import com.steccn.common.dto.SteccnTableDataDTO;
import com.steccn.common.valid.InsertGroup;
import com.steccn.common.vo.R;
import com.steccn.platform.vo.EqpSpareVO;
import com.steccn.pms.dto.*;
import com.steccn.pms.service.PmsLaborProtectionRecordService;
import com.steccn.pms.service.PmsLaborProtectionStandardService;
import com.steccn.pms.vo.LaborExcelImportDataVO;
import com.steccn.pms.vo.PmsLaborRecordVO;
import com.steccn.pms.vo.PmsLaborStandardVO;
import com.steccn.qms.constant.QtManagerConstant;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 劳保控制器
 *
 * @author AndrewLuo
 * @date 2024/04/03
 */
@Api("劳保控制器")
@RestController
@RequiredArgsConstructor
@RequestMapping("/pms/labor")
public class PmsLaborController {
    private final PmsLaborProtectionStandardService pmsLaborProtectionStandardService;
    private final PmsLaborProtectionRecordService pmsLaborProtectionRecordService;

    @PostMapping("/standard")
    @ApiOperation(value = "新建劳保标准")
    @SysLog("新建劳保标准")
    public R<Boolean> add(@Validated @RequestBody PmsLaborStandardDTO dto) {
        return R.success(pmsLaborProtectionStandardService.add(dto));
    }

    @PutMapping("/standard")
    @ApiOperation(value = "修改劳保标准")
    @SysLog("修改劳保标准")
    public R<Boolean> updateData(@Validated @RequestBody PmsLaborStandardDTO dto) {
        return R.success(pmsLaborProtectionStandardService.updateData(dto));
    }

    @GetMapping("/standard/{id}")
    @ApiOperation(value = "劳保标准详情")
    public R<PmsLaborStandardVO> info(@PathVariable("id")Integer id) {
        return R.success(pmsLaborProtectionStandardService.info(id));
    }

    @GetMapping("/standard/material")
    @ApiOperation(value = "获取物料下拉框")
    public R<List<EqpSpareVO>> material() {
        return R.success(pmsLaborProtectionStandardService.getAvailSpare());
    }

    @PostMapping("/standard/delete")
    @ApiOperation(value = "删除劳保标准")
    @SysLog("删除劳保标准")
    public R<Boolean> batchDeleteById(@RequestBody PmsLaborStandardDTO dto) {
        return R.success(pmsLaborProtectionStandardService.batchDeleteById(dto));
    }

    @GetMapping("/standard/list")
    @ApiOperation(value = "劳保标准列表")
    public R<SteccnTableDataDTO> listInfo() {
        return R.success(pmsLaborProtectionStandardService.listInfo());
    }

    @PostMapping("/standard/detail")
    @ApiOperation(value = "劳保清单列表")
    public R<SteccnTableDataDTO> standardList(@RequestBody PmsStandardListDTO dto) {
        return R.success(pmsLaborProtectionStandardService.standardList(dto));
    }

    @PostMapping("/standard/download")
    @ApiOperation(value = "下载劳保清单列表")
    public void downloadList(@RequestBody PmsLaborGerReqDTO dto,HttpServletResponse response) throws IOException{
        try {
            pmsLaborProtectionRecordService.downloadDetail(dto,response);
        } catch (Exception e) {
            response.reset();
            response.setContentType("application/json");
            response.setCharacterEncoding(QtManagerConstant.QT_CHART_EXCEL_ENCODE);
            Map<String, String> map = MapUtils.newHashMap();
            map.put("status", "failure");
            map.put("message", "下载文件失败" + e.getMessage());
            response.getWriter().println(JSON.toJSONString(map));
        }
    }

    @PostMapping("/standard/record")
    @ApiOperation(value = "劳保记录列表")
    public R<Page<PmsLaborRecordVO>> record(@RequestBody PmsLaborRecordPageDTO dto) {
        return R.success(pmsLaborProtectionRecordService.pageList(dto));
    }

    @PostMapping("/standard/generateReq")
    @SysLog("生成领用单")
    @ApiOperation(value = "生成领用单")
    public R<Boolean> generateReq(@RequestBody PmsLaborGerReqDTO dto) {
        return R.success(pmsLaborProtectionRecordService.generateReq(dto));
    }

    /**
     * 下载劳保导入模板
     */
    @ApiModelProperty("下载模板")
    @GetMapping("/download")
    public void download(HttpServletResponse response) throws IOException {
        pmsLaborProtectionRecordService.download(response);
    }


    @ApiModelProperty("Excel导入")
    @PostMapping("/excelImportData")
    public R<LaborExcelImportDataVO> excelImportData(@Validated(InsertGroup.class) PmExcelDTO dto) throws IOException {
        return R.success(pmsLaborProtectionRecordService.excelImportData(dto));
    }

    @ApiModelProperty("Excel导入错误文件")
    @PostMapping("/download/excelImportDataError")
    public void excelImportDataError(PmExcelDTO dto,HttpServletResponse response) throws IOException {
        pmsLaborProtectionRecordService.excelImportDataError(dto,response);
    }




}
