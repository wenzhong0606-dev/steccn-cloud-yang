package com.steccn.ems.controller;

import com.steccn.authserver.exception.BusinessException;
import com.steccn.common.vo.R;
import com.steccn.ems.vo.EpDayReportCountVO;
import com.steccn.ems.service.EpDayReportService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

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

    @GetMapping("/count/task")
    @ApiOperation(value = "能耗日统计报表-任务")
    public R<Boolean> count(@RequestParam("date") LocalDate date) {
        return R.success(epDayReportService.countTask(date));
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
}
