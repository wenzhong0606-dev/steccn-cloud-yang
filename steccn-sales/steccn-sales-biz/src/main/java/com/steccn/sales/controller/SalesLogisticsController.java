package com.steccn.sales.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.common.vo.R;
import com.steccn.sales.dto.KsLogisticsDTO;
import com.steccn.sales.service.KsLogisticsOrderService;
import com.steccn.sales.vo.KsLogisticsVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * 销售系统控制层
 *
 * @author AndrewLuo
 * @date 2023/11/29
 */
@Api("销售系统物流单控制器")
@RestController
@RequiredArgsConstructor
@RequestMapping("/sales/logistics")
public class SalesLogisticsController {
    private final KsLogisticsOrderService ksLogisticsOrderService;

    @PostMapping("/page")
    @ApiOperation("物流单")
    R<Page<KsLogisticsVO>> getLogisticsRecord(@Valid @RequestBody KsLogisticsDTO dto){
        return R.success(ksLogisticsOrderService.getLogisticsRecord(dto));
    }
}
