package com.steccn.pms.controller;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Sets;
import com.steccn.authserver.exception.BusinessException;
import com.steccn.common.annotation.SysLog;
import com.steccn.common.util.CalculateDateUtil;
import com.steccn.common.vo.R;
import com.steccn.pms.dto.*;
import com.steccn.pms.service.PmsReportWorkService;
import com.steccn.pms.vo.PmsReportWorkVO;
import com.steccn.pms.vo.ProcessReportWorkInfoVO;
import com.steccn.pms.vo.ProcessReportWorkPageVO;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

/**
 * @author Adolph.Ye
 * @desc 工序报工控制器
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/pms/process/report/work")
@Slf4j
public class ProcessReportWorkController {

    private final PmsReportWorkService pmsReportWorkService;

    @ApiOperation("工序分页列表")
    @PostMapping("/page")
    public R<Page<ProcessReportWorkPageVO>> pageList(@RequestBody ProcessReportWorkPageDTO dto) {
        return R.success(pmsReportWorkService.pageList(dto));
    }

    @ApiOperation("提交报工")
    @PostMapping("/add")
    @SysLog("提交报工")
    public R<Boolean> submitReportWork(@Valid @RequestBody ProcessReportWorkSubmitDTO dto) {
        //时间不可以跨班次
        Assert.isTrue(CalculateDateUtil.isSameClassType(dto.getStartTime(), dto.getEndTime()), "报工时间不可以跨班次");
        if(CollUtil.isNotEmpty(dto.getConsumeDTOList())){
            Set<Integer> set = Sets.newHashSet();
            for (ProcessReportWorkConsumeDTO consumeDTO : dto.getConsumeDTOList()) {
                boolean add = set.add(consumeDTO.getType());
                if(!add){
                    throw new BusinessException("能耗类型不能重复");
                }
            }
        }
        return R.success(pmsReportWorkService.submitReportWork(dto));
    }

    @ApiOperation("报工加工数预填")
    @PostMapping("/preload")
    public R<BigDecimal> preload(@Valid @RequestBody PmRwPreloadDTO dto) {
        //不考虑秒数  只考虑时钟和分钟数
        LocalDateTime startTime = dto.getStartTime();
        LocalDateTime startTimeNew = LocalDateTime.of(startTime.toLocalDate(), LocalTime.of(startTime.getHour(), startTime.getMinute(), 0));
        LocalDateTime endTime = dto.getEndTime();
        LocalDateTime endTimeNew = LocalDateTime.of(endTime.toLocalDate(), LocalTime.of(endTime.getHour(), endTime.getMinute(), 0));
        return R.success(pmsReportWorkService.preload(dto.getStationId(), startTimeNew, endTimeNew));
    }

    @ApiOperation("报工记录")
    @PostMapping("/record/page")
    public R<Page<ProcessReportWorkPageVO>> recordPage(@Valid @RequestBody ProcessReportWorkRecordDTO dto) {
        return R.success(pmsReportWorkService.recordPage(dto));
    }

    @ApiOperation("物料消耗")
    @PostMapping("/material/consume")
    public R<List<ProcessReportWorkMaterialListDTO>> materialConsume(@Valid @RequestBody ProcessReportWorkMaterialConsumeDTO dto) {
        return R.success(pmsReportWorkService.materialConsume(dto));
    }


    @ApiOperation("物料消耗-根据工位获取线边仓数据")
    @GetMapping("/material/line/warehouse")
    public R<List<ProcessReportWorkMaterialListDTO>> materialLineWarehouse(@RequestParam Integer id) {
        return R.success(pmsReportWorkService.materialLineWarehouse(id));
    }
    @ApiOperation("工艺信息-报工分页列表")
    @PostMapping("/craft/page")
    public R<Page<PmsReportWorkVO>> reportWorkPageList(@RequestBody PmsReportWorkPageDTO dto){
        return R.success(pmsReportWorkService.reportWorkPageList(dto));
    }

    @ApiOperation("报工详情")
    @GetMapping("/{id}")
    public R<ProcessReportWorkInfoVO> info(@PathVariable("id") Long id) {
        return R.success(pmsReportWorkService.info(id));
    }
}
