package com.steccn.ems.controller;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.util.MapUtils;
import com.alibaba.fastjson.JSON;
import com.steccn.authserver.exception.BusinessException;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.common.constant.FeignConstant;
import com.steccn.common.dto.TupleDTO;
import com.steccn.common.util.LocalDateUtil;
import com.steccn.common.vo.R;
import com.steccn.ems.vo.EpDayReportCountVO;
import com.steccn.ems.service.EpDayReportService;
import com.steccn.oa.constant.OaConstant;
import com.steccn.platform.feign.RemoteSysOauthClientDetailFeignApi;
import com.steccn.qms.constant.QtManagerConstant;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * @author chenhuan
 * @date 2022/11/30 15:30
 */
@Api("能耗日报表")
@RestController
@RequiredArgsConstructor
@RequestMapping("/ems/day/report")
@Slf4j
public class EpDayReportController {

    private final EpDayReportService epDayReportService;
    private final RemoteSysOauthClientDetailFeignApi remoteSysOauthClientDetailFeignApi;

    @GetMapping("/count")
    @ApiOperation(value = "能耗日统计报表")
    public R<EpDayReportCountVO> count(@RequestParam("date") String date) {
        //校验日期格式是否正确
        LocalDate parse;
        try {
            date = date + "-01";
            parse = LocalDate.parse(date);
        } catch (Exception e) {
            throw new BusinessException("日期格式异常");
        }
        boolean flag = Boolean.FALSE;
        if (LocalDate.now().getMonthValue() > parse.getMonthValue()) {
            flag = Boolean.TRUE;
        }
        return R.success(epDayReportService.count(parse, flag));
    }

    @GetMapping("/dataTwin")
    @ApiOperation(value = "能耗日统计报表")
    public R<List<TupleDTO<String, String>>> dataTwin(HttpServletRequest request) {
        validRequestHeader(request);
        return R.success(epDayReportService.dataTwinCount());
    }

    @GetMapping("/count/download")
    @ApiOperation(value = "能耗日统计报表导出")
    public void countDownload(@RequestParam("date") String date, HttpServletResponse response) throws IOException {
        //校验日期格式是否正确
        LocalDate parse;
        try {
            date = date + "-01";
            parse = LocalDate.parse(date);
        } catch (Exception e) {
            throw new BusinessException("日期格式异常");
        }
        boolean flag = Boolean.FALSE;
        if (LocalDate.now().getMonthValue() > parse.getMonthValue()) {
            flag = Boolean.TRUE;
        }
        try {
            epDayReportService.countDownload(parse, flag,response);
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

    @GetMapping("/count/task")
    @ApiOperation(value = "能耗日统计报表-任务")
    public R<Boolean> count(@RequestParam("date") LocalDate date) {
        return R.success(epDayReportService.countTask(date));
    }

    @RequestMapping("/count/task")
    @ApiOperation(value = "能耗日统计报表-任务")
    public R<Boolean> count(@RequestBody TupleDTO<List<LocalDate>,String> dto) {
        for (LocalDate date : dto.getField1()) {
            epDayReportService.countTask(date);
        }
        return R.success(Boolean.TRUE);
    }

    @GetMapping("/unit/consume")
    @ApiOperation(value = "单耗日统计报表")
    public R<EpDayReportCountVO> unitConsumeCount(@RequestParam("date") String date) {
        //校验日期格式是否正确
        LocalDate parse;
        try {
            date = date + "-01";
            parse = LocalDate.parse(date);
        } catch (Exception e) {
            throw new BusinessException("日期格式异常");
        }
        boolean flag = Boolean.FALSE;
        if (LocalDate.now().getMonthValue() > parse.getMonthValue()) {
            flag = Boolean.TRUE;
        }
        return R.success(epDayReportService.unitConsumeCount(parse, flag));
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
