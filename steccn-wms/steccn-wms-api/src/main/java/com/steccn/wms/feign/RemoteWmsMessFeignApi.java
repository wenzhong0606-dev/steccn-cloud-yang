package com.steccn.wms.feign;


import com.steccn.wms.dto.WmsTransferSlipInfoDTO;
import feign.Headers;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;


/**
 * @author mengle
 * @desc mess系统服务接口
 */
@FeignClient(name = "remoteWmsMessFeignApi", url = "${mes.url}", decode404 = true)
public interface RemoteWmsMessFeignApi {

    /**
     * 调拨单新增变更库存信息
     *
     * @param list 明细信息集合
     * @return
     */
    @PostMapping(value = "/web/eqp/eqpSpareInventory/transfer/slip/add")
    @Headers(value = {"content-type=application/json"})
    @ResponseBody
    Boolean transferSlipAdd(@RequestBody List<WmsTransferSlipInfoDTO> list);

    /**
     * 调拨单明细编辑变更库存信息
     *
     * @param list 明细信息集合
     * @return
     */
    @PostMapping(value = "/web/eqp/eqpSpareInventory/transfer/slip/edit")
    @Headers(value = {"content-type=application/json"})
    @ResponseBody
    Boolean transferSlipEdit(@RequestBody List<WmsTransferSlipInfoDTO> list);


    /**
     * 调拨单明细删除变更库存信息
     *
     * @param list 明细信息集合
     * @return
     */
    @PostMapping(value = "/web/eqp/eqpSpareInventory/transfer/slip/delete")
    @Headers(value = {"content-type=application/json"})
    @ResponseBody
    Boolean transferSlipDelete(@RequestBody List<WmsTransferSlipInfoDTO> list);

    /**
     * 调拨单明细确认变更库存信息
     *
     * @param list 明细信息集合
     * @return
     */
    @PostMapping(value = "/web/eqp/eqpSpareInventory/transfer/slip/affirm")
    @Headers(value = {"content-type=application/json"})
    @ResponseBody
    Boolean transferSlipAffirm(@RequestBody List<WmsTransferSlipInfoDTO> list);




}
