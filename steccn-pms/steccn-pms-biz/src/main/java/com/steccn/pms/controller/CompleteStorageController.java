package com.steccn.pms.controller;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.authserver.exception.BusinessException;
import com.steccn.common.annotation.SysLog;
import com.steccn.common.dto.ValidList;
import com.steccn.common.vo.R;
import com.steccn.pms.dto.*;
import com.steccn.pms.service.PmsCompleteStorageService;
import com.steccn.pms.vo.CompleteStorageListVO;
import com.steccn.pms.vo.CompleteStoragePageVO;
import com.steccn.pms.vo.PmsCompleteStorageVO;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
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
    public R<CompleteStorageScanDTO> scan(@ApiParam("货架id") @RequestParam("warehouseId") Integer warehouseId,@ApiParam("箱号") @RequestParam("caseNo") String caseNo) {
        return R.success(pmsCompleteStorageService.scan(warehouseId,caseNo));
    }

    @PostMapping("/generate")
    @ApiOperation("生成入库单")
    @SysLog("生成入库单")
    public R<String> generate(@Valid @RequestBody ValidList<CompleteStorageScanDTO> validList) {
        if (CollUtil.isEmpty(validList)) {
            throw new BusinessException("请选择箱子");
        }
        return R.success(pmsCompleteStorageService.generate(validList.getList()));
    }

    @PostMapping("/connect/line")
    @ApiOperation("连线入库")
    @SysLog("连线入库")
    public R<String> connectLine(@Valid @RequestBody ConnectLineStorageDTO dto) {
        return R.success(pmsCompleteStorageService.connectLine(dto));
    }

    @PostMapping("/page")
    @ApiOperation("入库单列表")
    public R<Page<CompleteStoragePageVO>> pageList(@Valid @RequestBody CompleteStoragePageDTO dto) {
        return R.success(pmsCompleteStorageService.pageList(dto));
    }

    @GetMapping("/{id}")
    @ApiOperation("入库单明细")
    public R<CompleteStorageListVO> info(@PathVariable("id") Long id) {
        return R.success(pmsCompleteStorageService.info(id));
    }

    @PostMapping("/order/page")
    @ApiOperation("获取订单的完工入库分页列表")
    public R<Page<PmsCompleteStorageVO>> completeStoragePageList(@RequestBody PmsCompleteStorageDTO dto) {
        return R.success(pmsCompleteStorageService.completeStoragePageList(dto));
    }

    @PostMapping("/complete")
    @ApiOperation("根据完成生产订单入库单编码修改状态")
    @SysLog("complete")
    public R<Boolean> completeStorageReviseStatue(@RequestBody PmsCompleteStorageDTO dto) {
        return pmsCompleteStorageService.completeStorageReviseStatue(dto.getCode(),dto.getConfirmName());
    }
}
