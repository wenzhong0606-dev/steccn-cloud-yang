package com.steccn.ems.controller;

import com.steccn.common.vo.R;
import com.steccn.ems.dto.EpMeterReadAddDTO;
import com.steccn.ems.service.EpMeterReadService;
import com.steccn.ems.vo.EqMeterReadVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

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
    public R<EqMeterReadVO> list(@RequestParam("readMonth") String readMonth, @RequestParam("energyType") Integer energyType) {
        return R.success(epMeterReadService.list(readMonth, energyType));
    }

    @GetMapping("/add")
    @ApiOperation(value = "抄表管理新增")
    public R<Boolean> add(@RequestBody EpMeterReadAddDTO dto) {
        return R.success(epMeterReadService.add(dto));
    }
}
