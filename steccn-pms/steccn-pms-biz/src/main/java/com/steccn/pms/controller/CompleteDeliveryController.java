package com.steccn.pms.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.common.vo.R;
import com.steccn.pms.dto.PmsCompleteDeliveryDTO;
import com.steccn.pms.service.PmsCompleteDeliveryService;
import com.steccn.pms.service.PmsProduceOrderService;
import com.steccn.pms.vo.PmsCompleteDeliveryVO;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 完工出库控制器
 *
 * @author Mongknife
 * @date 2022/10/12
 */
@RestController
@RequestMapping("/pms/complete/delivery")
@Slf4j
public class CompleteDeliveryController {

    @Autowired
    private PmsCompleteDeliveryService pmsCompleteDeliveryService;

    @ApiOperation("获取订单投料分页列表")
    @PostMapping("/page")
    public R<Page<PmsCompleteDeliveryVO>> completeDeliveryPageList(@RequestBody PmsCompleteDeliveryDTO dto) {
        return R.success(pmsCompleteDeliveryService.completeDeliveryPageList(dto));
    }

    @ApiOperation("工艺信息-投料信息")
    @PostMapping("/feed")
    public R<Page<PmsCompleteDeliveryVO>> feedPageList(@RequestBody PmsCompleteDeliveryDTO dto) {
        return R.success(pmsCompleteDeliveryService.feedPageList(dto));
    }


}
