package com.steccn.pms.controller;

import com.steccn.common.vo.R;
import com.steccn.dp.vo.DpPmsOperateCompleteStorageVO;
import com.steccn.pms.dto.PmsOperateDriverDTO;
import com.steccn.pms.service.PmsOperateDriverService;
import com.steccn.pms.vo.PmsOperateDriverCompleteStorageVO;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * @author 叶泉
 * @desc 运营驾驶舱
 * @date 2024/01/11
 */

@RestController
@RequestMapping("/pms/operate")
@RequiredArgsConstructor
@Slf4j
public class PmsOperateDriverController {

    private final PmsOperateDriverService pmsOperateDriverService;

    @PostMapping("/in/storage")
    @ApiOperation(value = "总入库数，入库合格率")
    public R<PmsOperateDriverCompleteStorageVO> inStorage(@Valid @RequestBody PmsOperateDriverDTO dto) {
        return R.success(pmsOperateDriverService.inStorage(dto));
    }

    @PostMapping("/complete")
    @ApiOperation(value = "总完工数，完工合格率")
    public R<PmsOperateDriverCompleteStorageVO> complete(@Valid @RequestBody PmsOperateDriverDTO dto) {
        return R.success(pmsOperateDriverService.complete(dto));
    }
}
