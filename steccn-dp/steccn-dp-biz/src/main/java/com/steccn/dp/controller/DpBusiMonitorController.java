package com.steccn.dp.controller;

import com.steccn.common.dto.ValidList;
import com.steccn.common.vo.R;
import com.steccn.dp.dto.DpBusiMonitorSaveDTO;
import com.steccn.dp.service.TbBusiMonitorService;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import javax.validation.Valid;

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
    public R<Boolean> saveList(@Valid @RequestBody ValidList<DpBusiMonitorSaveDTO> validList) {
        return R.success(busiMonitorService.saveList(validList.getList()));
    }
}
