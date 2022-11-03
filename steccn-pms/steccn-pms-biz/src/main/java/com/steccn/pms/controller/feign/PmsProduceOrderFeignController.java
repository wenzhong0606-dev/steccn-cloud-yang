package com.steccn.pms.controller.feign;

import com.steccn.common.vo.R;
import com.steccn.pms.dto.PmsProduceOrderDTO;
import com.steccn.pms.service.PmsProduceOrderService;
import com.steccn.pms.vo.PmsProduceOrderVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Set;

/**
 * 生产订单feign
 *
 * @author Mongknife
 * @date 2022/11/3
 */

@RestController
@Api("生产订单feign")
@RequestMapping("/feign/pms/produce/order")
@RequiredArgsConstructor
@Slf4j
public class PmsProduceOrderFeignController {

    private final PmsProduceOrderService pmsProduceOrderService;

    @PostMapping("/line/ids")
    @ApiOperation("根据产线查询当前正在执行订单信息")
    public R<Map<Integer, PmsProduceOrderDTO>> listByLineIds(@RequestBody Set<Integer> ids) {
        return R.success(pmsProduceOrderService.listByLineIds(ids));
    }

    @ApiOperation("根据产线id查询订单当前正在执行订单")
    @GetMapping("/line")
    public R<PmsProduceOrderVO> listByProduceLineId(@ApiParam("产线id") @RequestParam("productLineId") Integer productLineId) {
        return R.success(pmsProduceOrderService.listByProduceLineId(productLineId));
    }

}
