package com.steccn.pms.feign;

import com.steccn.pms.dto.*;
import com.steccn.pms.vo.MessInStorageVoucherVO;
import feign.Headers;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author yequan
 * @desc mess系统服务接口
 */
@FeignClient(name = "remoteMessFeignApi",url = "61.177.139.229:38080",decode404 = true)
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

    /**
     * 获取箱子位置
     * @param caseNoSet 箱子位置
     * @return
     */
    @PostMapping(value = "/web/eqp/eqpSpareInventory/stockByCaseNo")
    @Headers(value = {"content-type=application/json"})
    @ResponseBody
    Map<String, CaseSiteDTO> caseSite(@RequestBody Set<String> caseNoSet);

}
