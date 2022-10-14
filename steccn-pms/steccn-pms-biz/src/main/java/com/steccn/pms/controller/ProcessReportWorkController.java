package com.steccn.pms.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.common.vo.R;
import com.steccn.pms.dto.*;
import com.steccn.pms.service.PmsReportWorkService;
import com.steccn.common.vo.BaseSelectVO;
import com.steccn.pms.vo.PmsReportWorkVO;
import com.steccn.pms.vo.ProcessReportWorkPageVO;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public R<Boolean> submitReportWork(@RequestBody ProcessReportWorkSubmitDTO dto) {
        return R.success(pmsReportWorkService.submitReportWork(dto));
    }

    @ApiOperation("报工记录")
    @PostMapping("/record/page")
    public R<Page<ProcessReportWorkPageVO>> recordPage(@RequestBody ProcessReportWorkRecordDTO dto) {
        return R.success(pmsReportWorkService.recordPage(dto));
    }

    @ApiOperation("物料消耗")
    @PostMapping("/material/consume")
    public R<List<ProcessReportWorkMaterialListDTO>> materialConsume(@RequestBody ProcessReportWorkMaterialConsumeDTO dto) {
        return R.success(pmsReportWorkService.materialConsume(dto));
    }
    @ApiOperation("工艺信息-报工分页列表")
    @PostMapping("/craft/page")
    public R<Page<PmsReportWorkVO>> reportWorkPageList(@RequestBody PmsReportWorkPageDTO dto){
        return R.success(pmsReportWorkService.reportWorkPageList(dto));
    }

    @ApiOperation("工序报工-确定工位")
    @GetMapping("/station/scan/{id}")
    public R<BaseSelectVO<Integer>> stationScan(@PathVariable("id") Integer id){
        return R.success(pmsReportWorkService.stationScan(id));
    }
}
