package com.steccn.dp.controller;

import com.steccn.common.dto.TupleDTO;
import com.steccn.common.vo.R;
import com.steccn.dp.service.DpColdEndCheckpointValueService;
import com.steccn.dp.vo.DpColdEndCheckpointValueVO;
import com.steccn.dp.vo.DpWeightVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 冷端数采接口
 *
 * @author AndrewLuo
 * @date 2024/03/01
 */
@Api("备件信息")
@RestController
@RequiredArgsConstructor
@RequestMapping("/dp/coldEnd")
public class DpColdEndController {

    private final DpColdEndCheckpointValueService dpColdEndCheckpointValueService;

    @GetMapping("/list")
    @ApiOperation(value = "冷端数采数据")
    public R<List<DpColdEndCheckpointValueVO>> count() {
        return R.success(dpColdEndCheckpointValueService.getColdEndValue());
    }
}
