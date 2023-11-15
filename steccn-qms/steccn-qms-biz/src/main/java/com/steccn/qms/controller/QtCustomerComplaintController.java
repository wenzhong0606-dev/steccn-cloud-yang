package com.steccn.qms.controller;


import com.alibaba.excel.util.MapUtils;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.common.annotation.SysLog;
import com.steccn.common.valid.UpdateGroup;
import com.steccn.common.vo.R;
import com.steccn.pms.dto.CustomerComplainPageDTO;
import com.steccn.qms.constant.QtManagerConstant;
import com.steccn.qms.dto.QtCustomerComplainDTO;
import com.steccn.qms.dto.QtCustomerComplaintReportDTO;
import com.steccn.qms.dto.QtDefectReportDTO;
import com.steccn.qms.service.QtCustomerComplainService;
import com.steccn.qms.vo.QtCustomerComplainVO;
import com.steccn.qms.dto.QtCustomerComplaintReportResDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/qms/customer/complaint")
@ApiModel("客诉控制器")
@Slf4j
public class QtCustomerComplaintController {

    private final QtCustomerComplainService qtCustomerComplainService;


    @PostMapping("/add")
    @ApiOperation(value = "新增客诉记录")
    @SysLog("新增客诉记录")
    public R<Boolean> add(@RequestBody QtCustomerComplainDTO dto) {
        return R.success(qtCustomerComplainService.add(dto));
    }


    @ApiOperation(value = "客诉记录列表")
    @PostMapping("/list")
    public R<Page<QtCustomerComplainVO>> list(@RequestBody CustomerComplainPageDTO dto) {
        return R.success(qtCustomerComplainService.listPage(dto));
    }

    @ApiModelProperty("获取客诉记录列表详情")
    @GetMapping("/{id}")
    public R<QtCustomerComplainVO> getInfo(@PathVariable("id") Long id) {
        return R.success(qtCustomerComplainService.getInfo(id));
    }


    @ApiOperation("编辑客诉记录")
    @PutMapping()
    @SysLog("编辑客诉记录")
    public R<Boolean> edit(@RequestBody QtCustomerComplainDTO qtCustomerComplainDTO) {
        return R.success(qtCustomerComplainService.edit(qtCustomerComplainDTO));
    }


    @ApiOperation("处理客诉记录")
    @PutMapping("/dispose")
    @SysLog("处理客诉记录")
    public R<Boolean> dispose(@Validated(UpdateGroup.class) @RequestBody QtCustomerComplainDTO qtCustomerComplainDTO) {
        return R.success(qtCustomerComplainService.dispose(qtCustomerComplainDTO));
    }

    @ApiOperation("删除客诉记录")
    @DeleteMapping("/{id}")
    public R<Boolean> delete(@PathVariable("id") Long id) {
        return R.success(qtCustomerComplainService.delete(id));
    }


    @ApiOperation("获取客诉详情组件序列号是否存在")
    @GetMapping("/model/no/flag")
    public R<Boolean> modelNoFlag(@RequestParam("modelNo") String modelNo) {
        return R.success(qtCustomerComplainService.modelNoFlag(modelNo));
    }

    @ApiOperation("客诉报表")
    @PostMapping("/report")
    public R<List<QtCustomerComplaintReportResDTO>> report(@Valid @RequestBody QtCustomerComplaintReportDTO dto) {
        return R.success(qtCustomerComplainService.report(dto));
    }

    @PostMapping("/report/download")
    @ApiOperation(value = "客诉报表-下载")
    public void coatingFilmDownload(@Valid @RequestBody QtCustomerComplaintReportDTO dto, HttpServletResponse response) throws IOException {
        try {
            qtCustomerComplainService.reportDownload(dto, response);
        } catch (Exception e) {
            log.error("{}", e.getLocalizedMessage());
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
