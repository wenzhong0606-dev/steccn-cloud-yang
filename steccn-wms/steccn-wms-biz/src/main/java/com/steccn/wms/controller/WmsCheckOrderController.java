package com.steccn.wms.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.common.vo.R;
import com.steccn.wms.domain.WmsCheckOrder;
import com.steccn.wms.dto.WmsCheckOrderDTO;
import com.steccn.wms.service.WmsCheckOrderService;
import com.steccn.wms.vo.WmsCheckOrderVO;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 盘点管理
 * </p>
 *
 * @author yss
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/wms/check/orde")
public class WmsCheckOrderController {

    @Autowired
    private WmsCheckOrderService wmsCheckOrderService;

    /**
     * @explain: 获取盘点管理列表
     * @Date
     **/
    @PostMapping("/getCheckOrderList")
    @ApiOperation(value = "获取盘点管理列表")
    public R<Page<WmsCheckOrderVO>> getCheckOrderList(@RequestBody WmsCheckOrderDTO dto) {

        return R.success(wmsCheckOrderService.getCheckOrderList(dto));
    }

    @PostMapping("/addOrUpdateCheckOrder")
    @ApiOperation(value = "新增或者编辑盘点管理")
    public R<Boolean> addOrUpdateCheckOrder(@RequestBody WmsCheckOrder dto) {
        return R.success(wmsCheckOrderService.addOrUpdateCheckOrder(dto));
    }
    @DeleteMapping("/delCheckOrder")
    @ApiOperation(value = "删除盘点管理")
    public R<Boolean> delCheckOrder( WmsCheckOrder dto) {
        return R.success(wmsCheckOrderService.delCheckOrder(dto));
    }

}
