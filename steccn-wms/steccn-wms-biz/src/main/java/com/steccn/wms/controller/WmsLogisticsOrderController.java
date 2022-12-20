package com.steccn.wms.controller;

import cn.hutool.core.lang.Assert;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.common.annotation.SysLog;
import com.steccn.common.vo.R;
import com.steccn.pms.dto.PmsProduceOrderDTO;
import com.steccn.pms.vo.WmsInventoryVO;
import com.steccn.wms.domain.WmsLogisticsOrder;
import com.steccn.wms.dto.WmsInventoryPageDTO;
import com.steccn.wms.dto.WmsLogisticsDetailDTO;
import com.steccn.wms.dto.WmsLogisticsOrderDTO;
import com.steccn.wms.dto.WmsLogisticsOrderPageDTO;
import com.steccn.wms.service.WmsLogisticsDetailService;
import com.steccn.wms.service.WmsLogisticsOrderService;
import com.steccn.wms.vo.WmsLogisticsDetailVO;
import com.steccn.wms.vo.WmsLogisticsInfoVO;
import com.steccn.wms.vo.WmsLogisticsOrderVO;
import com.steccn.wms.vo.WmsStorageReceiptVO;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 物流单
 *
 * @author Mongknife
 * @date 2022/12/14
 */
@RestController
@RequestMapping("/wms/logistics")
@RequiredArgsConstructor
@Slf4j
public class WmsLogisticsOrderController {

    private final WmsLogisticsOrderService wmsLogisticsOrderService;

    private final WmsLogisticsDetailService wmsLogisticsDetailService;

    @ApiOperation("物流单分页列表")
    @PostMapping("/list")
    public R<Page<WmsLogisticsOrderVO>> pageList(@RequestBody WmsLogisticsOrderPageDTO dto) {
        return R.success(wmsLogisticsOrderService.pageList(dto));
    }

    @ApiOperation("物流单新增")
    @PostMapping("/add")
    public R<Boolean> add(@RequestBody WmsLogisticsOrderDTO wmsLogisticsOrderDTO) {
        return R.success(wmsLogisticsOrderService.add(wmsLogisticsOrderDTO));
    }

    @ApiOperation("物流单详情")
    @GetMapping()
    public R<WmsLogisticsOrderVO> get(@ApiParam("物流单id") @RequestParam("id") Long id) {
        return R.success(wmsLogisticsOrderService.get(id));
    }

    @ApiOperation("物流单编辑")
    @PutMapping
    @SysLog("物流单编辑")
    public R<Boolean> edit(@RequestBody WmsLogisticsOrderDTO dto) {
        return R.success(wmsLogisticsOrderService.edit(dto));
    }

    @ApiOperation("物流单运输")
    @PutMapping("/transport")
    @SysLog("物流单运输")
    public R<Boolean> transport(@RequestBody WmsLogisticsOrderDTO dto) {
        Assert.notNull(dto.getId(), "物流单id不能为空");
        return R.success(wmsLogisticsOrderService.transport(dto.getId()));
    }

    @PostMapping("/receipt/info")
    @ApiOperation("物流单详情")
    public R<Page<WmsLogisticsDetailVO>> receiptInfo(@RequestBody WmsLogisticsDetailDTO dto) {
        Assert.notNull(dto.getLogisticsId(), "物流单id不能为空");
        return R.success(wmsLogisticsDetailService.receiptInfo(dto));
    }


    @ApiOperation("物流单签收")
    @PostMapping("/receipt")
    @SysLog("物流单签收")
    public R<Boolean> receipt(@RequestBody WmsLogisticsOrderDTO dto) {
        Assert.notNull(dto.getId(), "物流单id不能为空");
        return R.success(wmsLogisticsOrderService.receipt(dto));
    }

    @ApiOperation("物流单删除")
    @SysLog("物流单删除")
    @DeleteMapping
    public R<Boolean> delete(@RequestBody WmsLogisticsOrderDTO dto) {
        Assert.notNull(dto.getId(), "物流单id不能为空");
        return R.success(wmsLogisticsOrderService.delete(dto.getId()));
    }


}
