package com.steccn.ems.controller;

import com.steccn.common.vo.R;
import com.steccn.dp.dto.DpPmsOperateDTO;
import com.steccn.ems.service.PmsOperateEnergyService;
import com.steccn.ems.vo.PmsOperateEnergyVO;
import com.steccn.qms.constant.QtManagerConstant;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * @author yequan
 * @desc 运营驾驶舱
 * @date 2023/12/1
 */
@Api("运营驾驶舱")
@RestController
@RequiredArgsConstructor
@RequestMapping("/ems/operate")
public class PmsOperateEnergyController {

    private final PmsOperateEnergyService pmsOperateEnergyService;

    @PostMapping("/gas")
    @ApiOperation(value = "总天然气使用量")
    public R<PmsOperateEnergyVO> gas(@Valid @RequestBody DpPmsOperateDTO dto) {
        //只能是年度，月度
        if (dto.getType() == 0) {
            dto.setGranularity(QtManagerConstant.QtGranularityEnum.DAY.getCode());
        } else {
            dto.setGranularity(QtManagerConstant.QtGranularityEnum.MONTH.getCode());
        }
        return R.success(pmsOperateEnergyService.gas(dto));
    }

    @PostMapping("/oil")
    @ApiOperation(value = "总重油使用量")
    public R<PmsOperateEnergyVO> oil(@Valid @RequestBody DpPmsOperateDTO dto) {
        //只能是年度，月度
        if (dto.getType() == 0) {
            dto.setGranularity(QtManagerConstant.QtGranularityEnum.DAY.getCode());
        } else {
            dto.setGranularity(QtManagerConstant.QtGranularityEnum.MONTH.getCode());
        }
        return R.success(pmsOperateEnergyService.oil(dto));
    }

    @PostMapping("/water")
    @ApiOperation(value = "总用水量")
    public R<PmsOperateEnergyVO> water(@Valid @RequestBody DpPmsOperateDTO dto) {
        //只能是年度，月度
        if (dto.getType() == 0) {
            dto.setGranularity(QtManagerConstant.QtGranularityEnum.DAY.getCode());
        } else {
            dto.setGranularity(QtManagerConstant.QtGranularityEnum.MONTH.getCode());
        }
        return R.success(pmsOperateEnergyService.water(dto));
    }

    @PostMapping("/electric")
    @ApiOperation(value = "总用电量")
    public R<PmsOperateEnergyVO> electric(@Valid @RequestBody DpPmsOperateDTO dto) {
        //只能是年度，月度
        if (dto.getType() == 0) {
            dto.setGranularity(QtManagerConstant.QtGranularityEnum.DAY.getCode());
        } else {
            dto.setGranularity(QtManagerConstant.QtGranularityEnum.MONTH.getCode());
        }
        return R.success(pmsOperateEnergyService.electric(dto));
    }

    @PostMapping("/unit/consume")
    @ApiOperation(value = "单耗")
    public R<PmsOperateEnergyVO> unitConsume(@Valid @RequestBody DpPmsOperateDTO dto) {
        //只能是年度，月度
//        if (dto.getType() == 0) {
//            dto.setGranularity(QtManagerConstant.QtGranularityEnum.DAY.getCode());
//        } else {
//            dto.setGranularity(QtManagerConstant.QtGranularityEnum.MONTH.getCode());
//        }
        dto.setGranularity(dto.getType());
        PmsOperateEnergyVO pmsOperateEnergyVO = pmsOperateEnergyService.unitConsume(dto);
        return R.success(pmsOperateEnergyVO);
    }
}
