package com.steccn.wms.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.common.vo.R;
import com.steccn.pms.feign.RemoteMessFeignApi;
import com.steccn.wms.domain.WmsCheckOrder;
import com.steccn.wms.domain.WmsCheckOrderScanInfo;
import com.steccn.wms.dto.WmsCheckOrderDTO;
import com.steccn.wms.dto.WmsCheckOrderScanInfoDTO;
import com.steccn.wms.dto.WmsCheckOrderScanInfoListDTO;
import com.steccn.wms.service.WmsCheckOrderScanInfoService;
import com.steccn.wms.service.WmsCheckOrderService;
import com.steccn.wms.vo.WmsCheckOrderAreaVO;
import com.steccn.wms.vo.WmsCheckOrderInfoVO;
import com.steccn.wms.vo.WmsCheckOrderScanInfoVO;
import com.steccn.wms.vo.WmsCheckOrderVO;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.util.function.Tuple3;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * <p>
 * 盘点管理
 * </p>
 *
 * @author yss
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/wms/check/orde")
public class WmsCheckOrderController {

    @Autowired
    private WmsCheckOrderService wmsCheckOrderService;
    private final RemoteMessFeignApi remoteMessFeignApi;
    private final WmsCheckOrderScanInfoService wmsCheckOrderScanInfoService;
    /**
     * @explain: 获取盘点管理列表
     * @Date
     **/
    @PostMapping("/getCheckOrderList")
    @ApiOperation(value = "获取盘点管理列表")
    public R<Page<WmsCheckOrderVO>> getCheckOrderList(@RequestBody WmsCheckOrderDTO dto) {
        return R.success(wmsCheckOrderService.getCheckOrderList(dto));
    }

    /*****
     * 盘点新增或者编辑
     * @param dto
     * @return
     */
    @PostMapping("/addOrUpdateCheckOrder")
    @ApiOperation(value = "新增或者编辑盘点管理")
    public R<String> addOrUpdateCheckOrder(@RequestBody WmsCheckOrder dto) {
        return  R.success(wmsCheckOrderService.addOrUpdateCheckOrder(dto));
    }

    /****
     * 盘点删除
     * @param dto
     * @return
     */
    @DeleteMapping("/delCheckOrder")
    @ApiOperation(value = "删除盘点管理")
    public R<Boolean> delCheckOrder( WmsCheckOrder dto) {
        return R.success(wmsCheckOrderService.delCheckOrder(dto));
    }

    /****
     * 盘点管理详情
     * @param id
     * @return
     */
    @GetMapping("/getByIdCheckOrderDetail")
    @ApiOperation(value = "盘点管理详情")
    public R<Map> getByIdCheckOrderDetail(Long id,Integer current, Integer size) {
        return R.success(wmsCheckOrderService.getByIdCheckOrderDetail(id,current,size));
    }

    /*****
     * 盘点差异调整
     * @param
     * @return
     */
    @GetMapping("/varianceAdjustmentCheckOrder")
    @ApiOperation(value = "盘点差异调整")
    public R<String> varianceAdjustmentCheckOrder(Long id) {
        return R.success(wmsCheckOrderService.varianceAdjustmentCheckOrder(id));
    }

    /*****
     * 获取盘点单分类列表
     * @return
     */
    @GetMapping("/getTypeTreeList")
    @ApiOperation(value = "获取盘点单分类列表")
    public  R  getTypeTreeList( ) {
        return  R.success(remoteMessFeignApi.tree());
    }

    /*****
     * 手机端 获取盘点单列表
     * @param
     * @return
     */
    @GetMapping("/api/getApiCheckOrderList")
    @ApiOperation(value = "手机端 获取盘点单列表")
    public  R<Page<WmsCheckOrderVO>> getApiCheckOrderList(Integer current,Integer size,Integer status, String beginTime,
                                                          String endTime,Integer post) {
        if(Objects.isNull(current)) return R.failure("当前页不可为空");
        if(Objects.isNull(size)) return R.failure("每页显示条数不可为空");
        return R.success(wmsCheckOrderService.getApiCheckOrderList(current,size,status,beginTime,endTime,post));
    }

    /*****
     * 手机端 获取盘点单明细列表
     * @param
     * @return
     */
    @GetMapping("/api/getApiCheckOrderDetailList")
    @ApiOperation(value = "手机端 获取盘点单明细列表")
    public   R<Page<WmsCheckOrderInfoVO>>  getApiCheckOrderDetailList(Long checkOrderId, Integer current,
                                                                      Integer size,Integer status) {
        if(Objects.isNull(checkOrderId)) return R.failure("盘点单主键不可为空");
        return  wmsCheckOrderService.getApiCheckOrderDetailList(checkOrderId,current,size,status);
    }

    /*****
     * 手机端 调整盘点单操作正常或者差异调整明细
     * @param
     * @return
     */
    @PostMapping("/api/adjNormalOrDifferenceCheckOrderItem")
    @ApiOperation(value = "手机端 调整盘点单操作正常或者差异调整明细")
    public  R<Boolean> adjNormalOrDifferenceCheckOrderItem(@RequestBody  Map<String, Object> paramMap) {

        return   wmsCheckOrderService.adjNormalOrDifferenceCheckOrderItem(paramMap);
    }


    @PostMapping("/add/scan/check/info")
    @ApiOperation("增加扫描盘点明细")
    public R<Boolean> addScanCheckInfo(@RequestBody WmsCheckOrderScanInfoDTO dto){
        return R.success(wmsCheckOrderScanInfoService.addScanCheckInfo(dto));
    }


    @PostMapping("/edit/scan/check/info")
    @ApiOperation("编辑扫描盘点明细")
    public R<Boolean> editScanCheckInfo(@RequestBody WmsCheckOrderScanInfoDTO dto){
        return R.success(wmsCheckOrderScanInfoService.editScanCheckInfo(dto));
    }

    @PostMapping("/list/scan/check/info")
    @ApiOperation("扫描盘点明细列表")
    public R<List<WmsCheckOrderScanInfoVO>> listScanCheckInfo(@RequestBody WmsCheckOrderScanInfoListDTO dto){
        return R.success(wmsCheckOrderScanInfoService.listScanCheckInfo(dto));
    }

    @GetMapping("/area/count/list")
    @ApiOperation("盘点区域盘点数列表")
    public R<List<WmsCheckOrderAreaVO>> areaCountList(Long checkOrderId){
        return R.success(wmsCheckOrderService.areaCountList(checkOrderId));
    }

    @PostMapping("/affirm/check")
    @ApiOperation("确认盘点")
    public R<Boolean>  affirmCheck(@RequestBody WmsCheckOrderDTO dto){
        return R.success(wmsCheckOrderService.affirmCheck(dto));
    }



}
