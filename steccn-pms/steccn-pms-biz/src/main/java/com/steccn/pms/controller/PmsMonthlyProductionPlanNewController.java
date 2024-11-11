package com.steccn.pms.controller;

import com.steccn.authserver.exception.BusinessException;
import com.steccn.common.vo.R;
import com.steccn.pms.dto.PmsMonthlyProductionPlanNewDTO;
import com.steccn.pms.service.PmsMonthlyProductionPlanNewService;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import javax.validation.Valid;
import java.time.LocalDate;

/**
 * 生产月计划汇总-new 控制器
 *
 * @author yequan
 * @date 2024/11/11
 */
@RestController
@RequestMapping("/pms/month/plan/new")
@RequiredArgsConstructor
@Slf4j
public class PmsMonthlyProductionPlanNewController {

    private final PmsMonthlyProductionPlanNewService pmsMonthlyProductionPlanNewService;

    @ApiOperation("新增")
    @PostMapping
    public R<Boolean> add(@Valid @RequestBody PmsMonthlyProductionPlanNewDTO dto) {
        LocalDate parse;
        try {
            String date = dto.getDate() + "-01";
            parse = LocalDate.parse(date);
        } catch (Exception e) {
            throw new BusinessException("日期格式异常");
        }
        return R.success(pmsMonthlyProductionPlanNewService.insertData(parse, dto.getClientId(), dto.getMaterialId()));
    }

    @ApiOperation("删除")
    @PostMapping("/delete")
    public R<Boolean> delete(@Valid @RequestBody PmsMonthlyProductionPlanNewDTO dto) {
        LocalDate parse;
        try {
            String date = dto.getDate() + "-01";
            parse = LocalDate.parse(date);
        } catch (Exception e) {
            throw new BusinessException("日期格式异常");
        }
        return R.success(pmsMonthlyProductionPlanNewService.deleteData(parse, dto.getClientId(), dto.getMaterialId()));
    }

    @ApiOperation("下达")
    @PostMapping("/release")
    public R<Boolean> release(@Valid @RequestBody PmsMonthlyProductionPlanNewDTO dto) {
        return R.success(pmsMonthlyProductionPlanNewService.release(dto.getLinkId(), dto.getDate(), dto.getClientId(), dto.getMaterialId()));
    }

    @ApiOperation("编辑")
    @PostMapping("/update")
    public R<Boolean> update(@Valid @RequestBody PmsMonthlyProductionPlanNewDTO dto) {
        LocalDate parse;
        try {
            String date = dto.getDate() + "-01";
            parse = LocalDate.parse(date);
        } catch (Exception e) {
            throw new BusinessException("日期格式异常");
        }
        return R.success(pmsMonthlyProductionPlanNewService.updateData(parse, dto.getClientId(), dto.getMaterialId(),dto.getValue()));
    }
}
