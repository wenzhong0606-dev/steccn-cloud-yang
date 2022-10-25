package com.steccn.pms.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.common.annotation.SysLog;
import com.steccn.common.vo.R;
import com.steccn.pms.dto.BarcodePrintDTO;
import com.steccn.pms.dto.BarcodePrintRecordDTO;
import com.steccn.pms.dto.PmsCompleteStorageNumDTO;
import com.steccn.pms.service.PmsBarcodePrintService;
import com.steccn.pms.vo.BarcodePrintRecordVO;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author yequan
 * @desc 条码打印控制器
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/pms/barcode")
@Slf4j
public class BarcodePrintController {

    private final PmsBarcodePrintService pmsBarcodePrintService;

    @ApiOperation("打印")
    @PostMapping("/print")
    @SysLog("打印")
    public R<Boolean> print(@Valid @RequestBody BarcodePrintDTO dto) {
        return R.success(pmsBarcodePrintService.print(dto));
    }

    @ApiOperation("打印记录")
    @PostMapping("/print/record")
    public R<Page<BarcodePrintRecordVO>> printRecord(@Valid @RequestBody BarcodePrintRecordDTO dto) {
        return R.success(pmsBarcodePrintService.printRecord(dto));
    }

    @PostMapping("/print/record/remove")
    @ApiOperation("打印记录删除")
    @SysLog("打印记录删除")
    public R<Boolean> delete(@RequestBody List<Long> idList) {
        return R.success(pmsBarcodePrintService.deleteBatch(idList));
    }

    @GetMapping("/print/record/{id}")
    @ApiOperation("打印记录详细")
    public R<BarcodePrintRecordVO> info(@PathVariable("id") Long id) {
        return R.success(pmsBarcodePrintService.info(id));
    }

    @PostMapping("/print/order/batch/sum")
    @ApiOperation("订单批次总数")
    R<Map<String, PmsCompleteStorageNumDTO>> batchSum(@RequestBody Set<String> batchNoSet){
        return R.success(pmsBarcodePrintService.batchSum(batchNoSet));
    }

    @PostMapping("/print/order/sum")
    @ApiOperation("订单总数")
    R<Map<Long, PmsCompleteStorageNumDTO>> orderSum(@RequestBody Set<Long> orderSet){
        return R.success(pmsBarcodePrintService.orderSum(orderSet));
    }
}
