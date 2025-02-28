package com.steccn.ems.controller;

import com.steccn.common.annotation.SysLog;
import com.steccn.common.dto.TupleDTO;
import com.steccn.common.vo.R;
import com.steccn.ems.dto.EpMeterReadAddDTO;
import com.steccn.ems.dto.EpMeterReadUpdateDTO;
import com.steccn.ems.service.EpMeterReadService;
import com.steccn.ems.vo.EpMeterReadAllDataVO;
import com.steccn.ems.vo.EpMeterReadDetailsVO;
import com.steccn.ems.vo.EpMeterReadVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.apache.tomcat.jni.Local;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

/**
 * @author chenhuan
 * @date 2022/11/30 15:30
 */
@Api("抄表管理")
@RestController
@RequiredArgsConstructor
@RequestMapping("/ems/meter/read")
public class EpMeterReadController {

    private final EpMeterReadService epMeterReadService;

    @GetMapping("/list")
    @ApiOperation(value = "抄表管理列表")
    public R<EpMeterReadVO> list(@RequestParam("readMonth") String readMonth, @RequestParam("energyType") Integer energyType) {
        return R.success(epMeterReadService.list(readMonth, energyType));
    }

    @GetMapping("/listRecord")
    @ApiOperation(value = "抄表管理列表")
    public R<EpMeterReadVO> listRecord(@RequestParam("readMonth") String readMonth, @RequestParam("energyType") Integer energyType) {
        return R.success(epMeterReadService.listRecord(readMonth, energyType));
    }

    @GetMapping("/kvDay")
    @ApiOperation(value = "35kV开关站日报表")
    public R<EpMeterReadVO> kvDay(@RequestParam("readMonth") String readMonth, @RequestParam("energyType") Integer energyType) {
        EpMeterReadVO epMeterReadVO = epMeterReadService.kvDay(readMonth, energyType);
        return R.success(epMeterReadVO);
    }

    @PostMapping("/add")
    @ApiOperation(value = "抄表管理新增")
    @SysLog("抄表管理新增")
    public R<Boolean> add(@RequestBody EpMeterReadAddDTO dto) {
        return R.success(epMeterReadService.add(dto));
    }

    @GetMapping("/details")
    @ApiOperation(value = "抄表管理详情")
    public R<EpMeterReadDetailsVO> details(@RequestParam("readDate") String readDate, @RequestParam("meterId") Integer meterId) {
        return R.success(epMeterReadService.details(readDate,meterId));
    }

    @PutMapping("/updateBatch")
    @ApiOperation(value = "抄表管理批量修改")
    @SysLog("抄表管理批量修改")
    public R<Boolean> updateBatch(@RequestBody List<EpMeterReadUpdateDTO> dtoList) {
        return R.success(epMeterReadService.updateBatch(dtoList));
    }

    @PostMapping("/class/all/data/share")
    @ApiOperation(value = "能耗-分摊")
    public R<List<EpMeterReadAllDataVO>> classAllDataShare(@RequestBody TupleDTO<Set<LocalDate>, List<Long>> tupleDto) {
        return R.success(epMeterReadService.classDateAllDataShare(tupleDto.getField1(), 1, tupleDto.getField2()));
    }

}
