package com.steccn.dp.controller;

import com.steccn.common.vo.R;
import com.steccn.dp.dto.DpPmsOperateDTO;
import com.steccn.dp.service.DpPmsOperateService;
import com.steccn.dp.vo.DpPmsOperateInStorageVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author yequan
 * @desc 生产驾驶舱运营看板
 * @date 2023/11/29
 */
@Api("备件信息")
@RestController
@RequiredArgsConstructor
@RequestMapping("/dp/pms/operate")
public class DpPmsOperateController {

    private final DpPmsOperateService dpPmsOperateService;

    @PostMapping("/in/storage")
    @ApiOperation(value = "总入库数，入库合格率")
    public R<DpPmsOperateInStorageVO> inStorage(@RequestBody DpPmsOperateDTO dto) {
        return R.success(dpPmsOperateService.inStorage(dto));
    }
}
