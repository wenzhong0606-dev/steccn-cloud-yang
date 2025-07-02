package com.steccn.pms.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Sets;
import com.steccn.authserver.exception.BusinessException;
import com.steccn.common.annotation.SysLog;
import com.steccn.common.util.CalculateDateUtil;
import com.steccn.common.util.LocalDateUtil;
import com.steccn.common.vo.R;
import com.steccn.pms.domain.PmsReportWork;
import com.steccn.pms.dto.*;
import com.steccn.pms.service.PmsReportWorkService;
import com.steccn.pms.vo.PmsReportWorkVO;
import com.steccn.pms.vo.ProcessReportWorkPageVO;
import com.steccn.pms.vo.ReportWorkPreinstallValueVO;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.Lists;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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

    @ApiOperation("深加工提交报工")
    @PostMapping("/add/deepWork")
    @SysLog("提交报工")
    public R<Boolean> deepWorkSubmitReportWork(@RequestBody ProcessReportWorkSubmitDTO dto) {
        //时间不可以跨班次
        Assert.isTrue(CalculateDateUtil.isSameClassType(dto.getStartTime(), dto.getEndTime()), "报工时间不可以跨班次");
        dto.getProcessNumList().forEach(tmp->{
            ProcessReportWorkSubmitDTO model = new ProcessReportWorkSubmitDTO();
            BeanUtil.copyProperties(dto,model);
            model.setProcessId(tmp.getId());
            model.setProcessName(tmp.getProcessName());
            model.setBadnessQuantity(tmp.getBadnessQuantity());
            model.setProcessQuantity(tmp.getProcessQuantity());
            model.setConsumeDTOList(Lists.newArrayList());
//            model.setStationName(null);
//            model.setStationId(null);
            model.setIsDeepWork(true);
            if(CollUtil.isNotEmpty(model.getMaterialConsumeList())){
                model.setMaterialConsumeList(model.getMaterialConsumeList().stream().filter(v->v.getProcessId().equals(tmp.getId())).collect(Collectors.toList()));
            }
            pmsReportWorkService.submitReportWork(model);
        });
        return R.success(Boolean.TRUE);
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
    public R<ProcessReportWorkSubmitDTO> info(@PathVariable("id") Long id) {
        return R.success(pmsReportWorkService.info(id));
    }

    @PutMapping
    @ApiOperation("报工编辑")
    @SysLog("报工编辑")
    public R<Boolean> edit(@Valid @RequestBody ProcessReportWorkSubmitDTO dto){
        return R.success(pmsReportWorkService.edit(dto));
    }

    @DeleteMapping("/{id}")
    @ApiOperation("报工删除")
    @SysLog("报工删除")
    public R<Boolean> delete(@PathVariable("id") Long id){
        return R.success(pmsReportWorkService.delete(id));
    }

    @GetMapping("/refresh/data")
    public R<Boolean> refreshReportWork(){
        List<PmsReportWork> list = pmsReportWorkService.list();
        list.forEach(ele ->{
            ele.setClassDate(LocalDateUtil.getClassDate(ele.getStartTime(),Boolean.FALSE));
            pmsReportWorkService.updateById(ele);
        });
        return R.success(Boolean.TRUE);
    }


    @GetMapping("/preinstall/value")
    @ApiOperation("获取报工预填值（班次/班组 开始时间 结束时间）")
    public R<ReportWorkPreinstallValueVO> preinstallValue(@RequestParam("localDate") LocalDate localDate){
        return R.success(pmsReportWorkService.preinstallValue(localDate));
    }
}
