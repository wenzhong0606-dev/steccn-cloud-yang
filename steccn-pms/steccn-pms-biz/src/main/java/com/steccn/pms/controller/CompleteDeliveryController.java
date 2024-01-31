package com.steccn.pms.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.common.dto.Tuple4DTO;
import com.steccn.common.dto.Tuple5DTO;
import com.steccn.common.dto.TupleDTO;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 完工出库控制器
 *
 * @author Mongknife
 * @date 2022/10/12
 */
@RestController
@RequestMapping("/pms/complete/delivery")
@Slf4j
@RequiredArgsConstructor
public class CompleteDeliveryController {

    private final PmsCompleteDeliveryService pmsCompleteDeliveryService;

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


    @ApiOperation("获取报工物料消耗（异丙醇/镀膜液/油墨）")
    @PostMapping("/material/consumption")
    public R<List<TupleDTO<String,Map<Integer,BigDecimal>>>> materialConsumption(@RequestBody Tuple5DTO<Integer, LocalDate,LocalDate,Integer,Integer> dto){
        return R.success(pmsCompleteDeliveryService.materialConsumption(dto));
    }
}
