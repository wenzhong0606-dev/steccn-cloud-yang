package com.steccn.platform.feign;

import com.steccn.common.vo.R;
import com.steccn.platform.dto.*;
import com.steccn.platform.vo.SalesTokenVO;
import feign.Headers;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 销售系统接口
 *
 * @author AndrewLuo
 * @date 2023/08/23
 */
@FeignClient(name = "remoteSalesFeignApi", url = "${sales.url}", decode404 = true)
public interface RemoteSalesFeignApi {
    /**
     * 根据分类id查询分类信息
     *
     * @param dto
     * @return
     */
    @PostMapping(value = "/api/get_token")
    @Headers(value = {"content-type=application/json"})
    @ResponseBody
    String getToken(@RequestBody SalesUserNameDTO dto);

    /**
     * oa回调接口
     * @param dto
     * @return
     */
    @PostMapping("/sync/oa/approve")
    @Headers(value = {"content-type=application/json"})
    @ResponseBody
    String oaCallBack(@RequestBody SrmOaWorkFlowDTO dto);

    /**
     * 查询物流单接口
     * @param dto
     * @return
     */
    @PostMapping("/query/logistics")
    @Headers(value = {"content-type=application/json"})
    @ResponseBody
    String queryLogistics(@RequestBody SalesLogisticsDTO dto);

    /**
     * 查询退货单接口
     * @param dto
     * @return
     */
    @PostMapping("/query/return_order")
    @Headers(value = {"content-type=application/json"})
    @ResponseBody
    String queryReturnOrder(@RequestBody SalesReturnOrderDTO dto);

    /**
     * 同步物流单
     * @param dto
     * @return
     */
    @PostMapping("/sync/mes/logistics")
    @Headers(value = {"content-type=application/json"})
    @ResponseBody
    String syncLogistics(@RequestBody SalesLogisticsSyncDTO dto);

    /**
     * 同步退货单
     * @param dto
     * @return
     */
    @PostMapping("/entry/stock_order")
    @Headers(value = {"content-type=application/json"})
    @ResponseBody
    String syncReturnOrder(@RequestBody SalesReturnOrderSyncDTO dto);

    /**
     * 查询合同图表
     * @param dto
     * @return
     */
    @PostMapping("/query/contract/details")
    @Headers(value = {"content-type=application/json"})
    @ResponseBody
    String contractDashBoard(@RequestBody SalesContractDTO dto);
}
