package com.steccn.pms.feign;

import com.steccn.pms.dto.MessInStorageVoucherDTO;
import com.steccn.pms.dto.MessOutStorageVoucherDTO;
import com.steccn.pms.vo.MessInStorageVoucherVO;
import com.steccn.pms.dto.MessMaterialConsumeDTO;
import com.steccn.pms.dto.MessMaterialStorageDTO;
import feign.Headers;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * @author yequan
 * @desc mess系统服务接口
 */
@FeignClient(name = "remoteMessFeignApi",url = "10.1.103.230:8081",decode404 = true)
public interface RemoteMessFeignApi {

    /**
     * 获取物料库存信息
     * @param list 物料集合
     * @return
     */
    @PostMapping(value = "/web/eqp/eqpSpareInventory/storeList")
    @Headers(value = {"content-type=application/json"})
    @ResponseBody
    List<MessMaterialConsumeDTO> materialConsume(@RequestBody List<MessMaterialStorageDTO> list);


    /**
     * 生成入库单
     * @param messDto 物料集合
     * @return
     */
    @PostMapping(value = "/web/eqp/eqpSpareInventory/generateStockIn")
    @Headers(value = {"content-type=application/json"})
    @ResponseBody
    MessInStorageVoucherVO generateInStorageVoucher(@RequestBody MessInStorageVoucherDTO messDto);

    /**
     * 生成出库单
     * @param messDto 物料集合
     * @return
     */
    @PostMapping(value = "/web/eqp/eqpSpareInventory/generateStockOut")
    @Headers(value = {"content-type=application/json"})
    @ResponseBody
    MessInStorageVoucherVO generateOutStorageVoucher(@RequestBody MessOutStorageVoucherDTO messDto);

}
