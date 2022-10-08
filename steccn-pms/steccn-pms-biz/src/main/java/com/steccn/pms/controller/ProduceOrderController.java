package com.steccn.pms.controller;

import com.steccn.common.vo.R;
import com.steccn.pms.dto.PmsProduceOrderDTO;
import com.steccn.pms.service.PmsProduceOrderService;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 生产订单相关内容
 *
 * @author Mongknife
 * @date 2022/10/8
 */
@RestController
@RequestMapping("/pms/produce/order")
@RequiredArgsConstructor
@Slf4j
public class ProduceOrderController {

    private final PmsProduceOrderService pmsProduceOrderService;

    @ApiOperation("生产订单新增")
    @PostMapping()
    public R<Boolean> add(@RequestBody PmsProduceOrderDTO dto) {
        return R.success(pmsProduceOrderService.add(dto));
    }

}
