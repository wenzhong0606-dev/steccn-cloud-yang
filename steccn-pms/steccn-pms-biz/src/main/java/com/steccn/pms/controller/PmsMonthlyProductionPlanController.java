package com.steccn.pms.controller;


import cn.hutool.core.lang.Assert;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.common.annotation.SysLog;
import com.steccn.common.valid.InsertGroup;
import com.steccn.common.vo.R;
import com.steccn.pms.domain.PmsMonthlyProductionPlan;
import com.steccn.pms.dto.PmsMonthlyProductionPlanDTO;
import com.steccn.pms.service.PmsMonthlyProductionPlanService;
import com.steccn.pms.vo.PmsMonthlyProductionPlanVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Api("月度生产主计划")
@RestController
@RequiredArgsConstructor
@RequestMapping("/pms/monthly/production/plan")
public class PmsMonthlyProductionPlanController {

    private final PmsMonthlyProductionPlanService pmsMonthlyProductionPlanService;

    @ApiOperation("月计划新增")
    @PostMapping
    @SysLog("月计划新增")
    public R<Boolean> add(@Validated(InsertGroup.class) @RequestBody PmsMonthlyProductionPlanDTO dto) {
        return R.success(pmsMonthlyProductionPlanService.add(dto));
    }

    @ApiOperation("月计划编辑")
    @PutMapping
    @SysLog("月计划编辑")
    public R<Boolean> edit(@Validated(InsertGroup.class) @RequestBody PmsMonthlyProductionPlanDTO dto) {
        Assert.notNull(dto.getId(), "计划id不能为空");
        return R.success(pmsMonthlyProductionPlanService.edit(dto));
    }

    @ApiModelProperty("计划下达")
    @PutMapping("/order")
    @SysLog("计划下达")
    public R<Boolean> order(@RequestBody PmsMonthlyProductionPlanDTO dto){
        Assert.notNull(dto.getId(), "计划id不能为空");
        return R.success(pmsMonthlyProductionPlanService.order(dto));
    }

    @ApiOperation("暂停")
    @PutMapping("/stop")
    @SysLog("暂停")
    public R<Boolean> stop(@RequestBody PmsMonthlyProductionPlanDTO dto) {
        Assert.notNull(dto.getId(), "计划id不能为空");
        return R.success(pmsMonthlyProductionPlanService.stop(dto.getId()));
    }


    @ApiOperation("完成")
    @PutMapping("/complete")
    @SysLog("完成")
    public R<Boolean> complete(@RequestBody PmsMonthlyProductionPlanDTO dto) {
        Assert.notNull(dto.getId(), "计划id不能为空");
        return R.success(pmsMonthlyProductionPlanService.complete(dto.getId()));
    }

    @ApiOperation("列表")
    @PostMapping("/list")
    @SysLog("列表")
    public R<Page<PmsMonthlyProductionPlanVO>> list(@RequestBody PmsMonthlyProductionPlanDTO dto){
        return R.success(pmsMonthlyProductionPlanService.pageList(dto));
    }

    @ApiOperation("详情")
    @GetMapping("/info")
    @SysLog("详情")
    public R<PmsMonthlyProductionPlanVO> info(@RequestParam("id") Long id){
        return R.success(pmsMonthlyProductionPlanService.info(id));
    }

    @ApiOperation("创建工单")
    @PostMapping("/create/order")
    @SysLog("计划转工单")
    public R<Boolean> createOrder(@RequestBody PmsMonthlyProductionPlanDTO dto){
        return R.success(pmsMonthlyProductionPlanService.createOrder(dto));
    }





}
