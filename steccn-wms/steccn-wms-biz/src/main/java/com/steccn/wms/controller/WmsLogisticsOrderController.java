package com.steccn.wms.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.common.vo.R;
import com.steccn.pms.vo.WmsInventoryVO;
import com.steccn.wms.domain.WmsLogisticsOrder;
import com.steccn.wms.dto.WmsInventoryPageDTO;
import com.steccn.wms.dto.WmsLogisticsOrderDTO;
import com.steccn.wms.dto.WmsLogisticsOrderPageDTO;
import com.steccn.wms.service.WmsLogisticsOrderService;
import com.steccn.wms.vo.WmsLogisticsInfoVO;
import com.steccn.wms.vo.WmsLogisticsOrderVO;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    @ApiOperation("物流单分页列表")
    @PostMapping("/list")
    public R<Page<WmsLogisticsOrderVO>> pageList(@RequestBody WmsLogisticsOrderPageDTO dto) {
        return R.success(wmsLogisticsOrderService.pageList(dto));
    }

    @ApiOperation("物流单新增")
    @PostMapping("/add")
    public R<Boolean> add(@RequestBody WmsLogisticsOrderDTO wmsLogisticsOrderDTO){
        return R.success(wmsLogisticsOrderService.add(wmsLogisticsOrderDTO));
    }
}
