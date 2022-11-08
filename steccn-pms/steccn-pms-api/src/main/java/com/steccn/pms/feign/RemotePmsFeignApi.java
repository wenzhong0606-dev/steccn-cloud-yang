package com.steccn.pms.feign;

import com.steccn.common.constant.FeignConstant;
import com.steccn.common.vo.R;
import com.steccn.pms.dto.PmsProduceOrderDTO;
import com.steccn.pms.vo.PmsProduceOrderVO;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;
import java.util.Set;

/**
 * 生产管理feign
 *
 * @author Mongknife
 * @date 2022/11/3
 */
@FeignClient(contextId = "RemotePmsFeignApi", value = FeignConstant.STECCN_PMS_SERVICE_NAME)
public interface RemotePmsFeignApi {


    /**
     * 根据产线id批量获取当前产线下正在执行中的订单信息
     *
     * @param ids
     * @return
     */
    @PostMapping("/feign/pms/produce/order/line/ids")
    @ApiOperation("根据产线查询当前正在执行订单信息")
    R<Map<Integer, PmsProduceOrderDTO>> listByLineIds(@RequestBody Set<Integer> ids);


    /**
     * 根据产线id查询订单当前正在执行订单
     *
     * @param productLineId
     * @return
     */
    @ApiOperation("根据产线id查询订单当前正在执行订单")
    @GetMapping("/feign/pms/produce/order/line")
    R<PmsProduceOrderVO> listByProduceLineId(@ApiParam("产线id") @RequestParam("productLineId") Integer productLineId);

    /**
     * 根据生产订单号查询正在执行中任务的产线,批次,车间信息
     * @param code
     * @return
     */
    @ApiOperation("根据生产订单号查询正在执行中任务的产线,批次,车间信息")
    @GetMapping("/feign/pms/produce/order/code")
    R<PmsProduceOrderVO> code(@ApiParam("生产订单号") @RequestParam("code") String code);
}
