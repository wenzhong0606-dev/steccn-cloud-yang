package com.steccn.ems.controller;

import com.alibaba.excel.util.MapUtils;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.common.annotation.SysLog;
import com.steccn.common.vo.R;
import com.steccn.ems.dto.*;
import com.steccn.ems.service.EpInstrumentAccountService;
import com.steccn.ems.vo.EpInstrumentAccountPageVO;
import com.steccn.ems.vo.EpInstrumentAccountVO;
import com.steccn.ems.vo.EpProductUnitConsumeStandardVO;
import com.steccn.qms.constant.QtManagerConstant;
import com.steccn.qms.dto.QtChartYieldDownloadDTO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import sun.misc.BASE64Decoder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.*;
import java.util.List;
import java.util.Map;

@Api("仪表台账")
@RestController
@RequiredArgsConstructor
@RequestMapping("/ems/Instrument/parameter")
public class EpInstrumentAccountController {

    private final EpInstrumentAccountService epInstrumentAccountService;

    @PostMapping("/page")
    @ApiOperation(value = "查询仪表台账分页列表")
    public R<Page<EpInstrumentAccountPageVO>> getEpInstrumentAccountVOPage(@Valid @RequestBody
                                                                                EpInstrumentAccountPageDTO dto){
        return R.success(epInstrumentAccountService.getEpInstrumentAccountVOPage(dto));
    }

    @PostMapping
    @ApiOperation(value = "新增或编辑仪表台账")
    @SysLog("新增或编辑仪表台账")
    public R<Boolean> addOrEdit(@Valid @RequestBody EpInstrumentAccountDTO dto) {
        return R.success(epInstrumentAccountService.addOrEdit(dto));
    }

    @PostMapping("/reviseStatus")
    @ApiOperation(value = "修改仪表台账状态")
    @SysLog("修改仪表台账状态")
    public R<Boolean>  reviseStatus(@RequestBody EpInstrumentAccountStatusDTO dto) {
        return R.success(epInstrumentAccountService.reviseStatus(dto));
    }

    @GetMapping("/{id}")
    @ApiOperation(value = "根据id查询仪表台账")
    public R<EpInstrumentAccountVO> getEpInstrumentAccountVO(@PathVariable("id") Integer id){
        return R.success(epInstrumentAccountService.getEpInstrumentAccountVO(id));
    }

    @DeleteMapping("/{id}")
    @ApiOperation(value = "根据id删除仪表台账")
    public R<Boolean> delete(@PathVariable("id") Integer id){
        return R.success(epInstrumentAccountService.delete(id));
    }

    @RequestMapping("/download")
    @ApiOperation(value = "下载资产卡片图片")
    @SysLog("下载资产卡片图片")
    public void downloadSubstrate(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try{
            epInstrumentAccountService.downloadSubstrate(response,request);
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
}
