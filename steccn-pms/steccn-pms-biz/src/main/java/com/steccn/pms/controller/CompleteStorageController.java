package com.steccn.pms.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.common.vo.R;
import com.steccn.pms.dto.CompleteStoragePageDTO;
import com.steccn.pms.dto.CompleteStorageScanDTO;
import com.steccn.pms.service.PmsCompleteStorageService;
import com.steccn.pms.vo.CompleteStoragePageVO;
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

    private final PmsCompleteStorageService pmsCompleteStorageService;

    @GetMapping("/scan")
    @ApiOperation("摄像头扫描箱号")
    public R<CompleteStorageScanDTO> scan(@ApiParam("箱号") @RequestParam("caseNo") String caseNo) {
        return R.success(pmsCompleteStorageService.scan(caseNo));
    }

    @PostMapping("/generate")
    @ApiOperation("生成入库单")
    public R<String> generate(@RequestBody List<CompleteStorageScanDTO> list) {
        return R.success(pmsCompleteStorageService.generate(list));
    }

    @PostMapping("/page")
    @ApiOperation("入库单列表")
    public R<Page<CompleteStoragePageVO>> page(@RequestBody CompleteStoragePageDTO dto) {
        return R.success();
    }

    @GetMapping("/{id}")
    @ApiOperation("入库单明细")
    public R<CompleteStoragePageVO> info(@PathVariable("id") Long id) {
        return R.success();
    }
}
