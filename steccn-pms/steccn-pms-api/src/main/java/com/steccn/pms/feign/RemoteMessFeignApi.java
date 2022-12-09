package com.steccn.pms.feign;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.pms.dto.*;
import com.steccn.pms.vo.MessInStorageVoucherVO;
import com.steccn.pms.vo.WmsStockVO;
import feign.Headers;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author yequan
 * @desc mess系统服务接口
 */
@FeignClient(name = "remoteMessFeignApi", url = "${mes.url}", decode404 = true)
public interface RemoteMessFeignApi {

    /**
     * 获取物料库存信息
     *
     * @param list 物料集合
     * @return
     */
    @PostMapping(value = "/web/eqp/eqpSpareInventory/storeList")
    @Headers(value = {"content-type=application/json"})
    @ResponseBody
    List<MessMaterialConsumeDTO> materialConsume(@RequestBody List<MessMaterialStorageDTO> list);


    /**
     * 获取物料可用库存
     *
     * @param materialIds
     * @return
     */
    @PostMapping(value = "/web/eqp/eqpSpareInventory/materialInventory")
    @Headers(value = {"content-type=application/json"})
    @ResponseBody
    Map<Long, MesMaterialInventoryDTO> materialInventory(@RequestBody Set<Long> materialIds);

    /**
     * 生成入库单
     *
     * @param messDto 物料集合
     * @return
     */
    @PostMapping(value = "/web/eqp/eqpSpareInventory/generateStockIn")
    @Headers(value = {"content-type=application/json"})
    @ResponseBody
    MessInStorageVoucherVO generateInStorageVoucher(@RequestBody MessInStorageVoucherDTO messDto);

    /**
     * 生成出库单
     *
     * @param messDto 物料集合
     * @return
     */
    @PostMapping(value = "/web/eqp/eqpSpareInventory/generateStockOut")
    @Headers(value = {"content-type=application/json"})
    @ResponseBody
    MessInStorageVoucherVO generateOutStorageVoucher(@RequestBody MessOutStorageVoucherDTO messDto);

    /**
     * 获取箱子位置
     *
     * @param caseNoSet 箱子位置
     * @return
     */
    @PostMapping(value = "/web/eqp/eqpSpareInventory/stockByCaseNo")
    @Headers(value = {"content-type=application/json"})
    @ResponseBody
    Map<String, CaseSiteDTO> caseSite(@RequestBody Set<String> caseNoSet);

    /**
     * 获取物料在货架中的库存数量
     *
     * @param list
     * @return
     */
    @PostMapping(value = "/web/eqp/eqpSpareInventory/list/materialInventory")
    @Headers(value = {"content-type=application/json"})
    @ResponseBody
    Map<Long, BigDecimal> listMaterialInventory(@RequestBody List<MessMaterialStorageDTO> list);


    /**
     * 根据货架获取库存列表
     *
     * @param materialIds
     * @return
     */
    @PostMapping(value = "/web/eqp/eqpSpareInventory/shelfInventory")
    @Headers(value = {"content-type=application/json"})
    @ResponseBody
    Map<String, List<WmsStockVO>> shelfInventory(@RequestBody Set<String> shelfs);

    /**
     * 根据物料获取库存列表
     *
     * @param materialIds
     * @return
     */
    @PostMapping(value = "/web/eqp/eqpSpareInventory/material/inventory")
    @Headers(value = {"content-type=application/json"})
    @ResponseBody
    Map<Long, List<WmsStockVO>> inventory(@RequestBody Set<Long> materials);

    /**
     * 根据物料获取库存列表
     *
     * @param materialIds
     * @return
     */
    @GetMapping(value = "/web/mesClassification/tree")
    @Headers(value = {"content-type=application/json"})
    @ResponseBody
    Object tree();

}
