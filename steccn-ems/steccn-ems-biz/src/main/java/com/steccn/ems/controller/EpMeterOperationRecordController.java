package com.steccn.ems.controller;

import com.steccn.common.vo.R;
import com.steccn.ems.service.EpMeterOperationRecordService;
import com.steccn.ems.vo.EpMeterOperationRecordVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/**
 * @author chenhuan
 * @date 2022/12/7 17:14
 */
@Api("抄表管理操作记录")
@RestController
@RequiredArgsConstructor
@RequestMapping("/ems/meter/operation/record")
public class EpMeterOperationRecordController {

    private final EpMeterOperationRecordService epMeterOperationRecordService;

    @GetMapping("/list")
    @ApiOperation(value = "抄表管理炒作记录列表")
    public R<List<EpMeterOperationRecordVO>> list(@RequestParam("meterId") Integer meterId, @RequestParam("readDate") LocalDate readDate) {
        return R.success(epMeterOperationRecordService.list(meterId, readDate));
    }
}
