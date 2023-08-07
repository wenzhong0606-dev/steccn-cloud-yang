package com.steccn.pms.controller;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.common.annotation.SysLog;
import com.steccn.common.constant.FeignConstant;
import com.steccn.common.valid.InsertGroup;
import com.steccn.common.valid.UpdateGroup;
import com.steccn.common.vo.R;
import com.steccn.oa.constant.OaConstant;
import com.steccn.platform.feign.RemoteSysOauthClientDetailFeignApi;
import com.steccn.pms.dto.PmExcelDTO;
import com.steccn.pms.dto.PmsProducePlanDTO;
import com.steccn.pms.dto.PmsProducePlanExcelDTO;
import com.steccn.pms.dto.PmsProducePlanOrderDTO;
import com.steccn.pms.service.PmsProducePlanService;
import com.steccn.pms.vo.ProducePlanImportVO;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * @author chenhuan
 * @date 2022/11/3 14:07
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/pms/produce/plan")
public class PmsProducePlanController {

    private final PmsProducePlanService pmsProducePlanService;

    private final RemoteSysOauthClientDetailFeignApi remoteSysOauthClientDetailFeignApi;

    @ApiOperation("月计划列表")
    @GetMapping("/list")
    public R<List<PmsProducePlanDTO>> list(@RequestParam("monthDate") String monthDate) {
        return R.success(pmsProducePlanService.list(monthDate));
    }

    @ApiOperation("月计划批量新增")
    @PostMapping
    public R<Boolean> addBatch(@Validated(InsertGroup.class) @RequestBody PmsProducePlanDTO dto) {
        return R.success(pmsProducePlanService.addOrEditBatch(dto));
    }

    @ApiOperation("月计划批量编辑")
    @PutMapping
    public R<Boolean> editBatch(@Validated(UpdateGroup.class) @RequestBody PmsProducePlanDTO dto) {
        return R.success(pmsProducePlanService.addOrEditBatch(dto));
    }

    @ApiModelProperty("Excel导入")
    @PostMapping("/excelImportData")
    public R<ProducePlanImportVO> excelImportData(@Validated(InsertGroup.class) PmExcelDTO dto) throws IOException {
        return R.success(pmsProducePlanService.excelImportData(dto));
    }

    @ApiModelProperty("Excel导出")
    @GetMapping("/excelExportData")
    public void excelExportData(HttpServletRequest request, HttpServletResponse response) {
        pmsProducePlanService.excelExportData(request,response);
    }

    @ApiModelProperty("计划下达")
    @PutMapping("/order")
    @SysLog("计划下达")
    public R<Boolean> order(@RequestBody PmsProducePlanOrderDTO dto){
        return R.success(pmsProducePlanService.order(dto));
    }

    /**
     * 下载月生产计划模板
     */
    @ApiModelProperty("下载模板")
    @GetMapping("/download")
    public void download(HttpServletResponse response) {
        pmsProducePlanService.download(response);
    }

    @ApiOperation("月计划批量新增")
    @PostMapping("/thirdparty")
    public R<Boolean> addBatch(@RequestBody List<PmsProducePlanDTO> dto,HttpServletRequest request) {
        validRequestHeader(request);
        return R.success(pmsProducePlanService.addMaterialBatch(dto));
    }

    /**
     * 校验请求头信息
     *
     * @param request 请求
     */
    private void validRequestHeader(HttpServletRequest request) {
        List<String> pwdSet = FeignOptional.of(() -> remoteSysOauthClientDetailFeignApi.clientEncodePassword(), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        String clientId = request.getHeader(OaConstant.CLIENT_ID);
        Assert.isTrue(StrUtil.isNotBlank(clientId), "请求头中必须携带client-id");
        Assert.isTrue(pwdSet.contains(clientId), "非法访问，请携带正确的client-id");
    }
}
