package com.steccn.dp.controller;

import com.steccn.common.dto.TupleDTO;
import com.steccn.common.dto.ValidList;
import com.steccn.common.vo.R;
import com.steccn.dp.dto.DpBusiMonitorDTO;
import com.steccn.dp.service.TbBusiMonitorService;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * @author yequan
 * @desc 业务监控
 */

@RestController
@RequiredArgsConstructor
@RequestMapping("/dp/busi/monitor")
public class DpBusiMonitorController {

    private final TbBusiMonitorService busiMonitorService;

    @PostMapping
    @ApiOperation(value = "业务监控-保存配置")
    public R<Boolean> saveList(@Valid @RequestBody ValidList<DpBusiMonitorDTO> validList) {
        return R.success(busiMonitorService.saveList(validList.getList()));
    }

    @PostMapping("/show")
    @ApiOperation(value = "业务监控-显示所有配置")
    public R<List<TupleDTO<Integer, List<TupleDTO<String, List<DpBusiMonitorDTO>>>>>> showAll(@RequestBody List<String> alarmCodeList) {
        return R.success(busiMonitorService.showAll(alarmCodeList));
    }
}
