package com.steccn.pms.controller;

import com.steccn.common.vo.R;
import com.steccn.pms.dto.CompleteStorageScanDTO;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author yequan
 * @desc 完工入库控制器
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/pms/complete/storage")
@Slf4j
public class CompleteStorageController {

    @GetMapping("/scan")
    @ApiOperation("摄像头扫描箱号")
    public R<CompleteStorageScanDTO> scan(@ApiParam("箱号") @RequestParam("caseNumber") String caseNumber) {
//        是否已生成入库单，如果生成则抛异常
        return null;
    }

    @PostMapping("/generate")
    @ApiOperation("生成入库单")
    public R<String> generate(@RequestBody List<CompleteStorageScanDTO> list) {
        return R.success();
    }
}
