package com.steccn.ems.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.common.annotation.SysLog;
import com.steccn.common.vo.R;
import com.steccn.ems.dto.*;
import com.steccn.ems.service.EpInstrumentAccountService;
import com.steccn.ems.vo.EpInstrumentAccountPageVO;
import com.steccn.ems.vo.EpInstrumentAccountVO;
import com.steccn.ems.vo.EpProductUnitConsumeStandardVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@Api("仪表台账")
@RestController
@RequiredArgsConstructor
@RequestMapping("/ems/Instrument/parameter")
public class EpInstrumentAccountController {

    private final EpInstrumentAccountService epInstrumentAccountService;

    @PostMapping("/page")
    @ApiOperation(value = "查询仪表台账分页列表")
    public R<Page<EpInstrumentAccountPageVO>> getEpInstrumentAccountVOPage(@Valid @RequestBody
                                                                                EpInstrumentAccountPageDTO dto){
        return R.success(epInstrumentAccountService.getEpInstrumentAccountVOPage(dto));
    }

    @PostMapping
    @ApiOperation(value = "新增或编辑仪表台账")
    @SysLog("新增或编辑仪表台账")
    public R<Boolean> addOrEdit(@Valid @RequestBody EpInstrumentAccountDTO dto) {
        return R.success(epInstrumentAccountService.addOrEdit(dto));
    }

    @PostMapping("/reviseStatus")
    @ApiOperation(value = "修改仪表台账状态")
    @SysLog("修改仪表台账状态")
    public R<Boolean>  reviseStatus(@RequestBody EpInstrumentAccountStatusDTO dto) {
        return R.success(epInstrumentAccountService.reviseStatus(dto));
    }
}
