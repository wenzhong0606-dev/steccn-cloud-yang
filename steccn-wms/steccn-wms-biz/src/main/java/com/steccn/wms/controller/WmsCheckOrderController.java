package com.steccn.wms.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.common.vo.R;
import com.steccn.wms.domain.WmsCheckOrder;
import com.steccn.wms.dto.WmsCheckOrderDTO;
import com.steccn.wms.service.WmsCheckOrderService;
import com.steccn.wms.vo.WmsCheckOrderVO;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
    public R<Boolean> addOrUpdateCheckOrder(@RequestBody WmsCheckOrder dto) {
        return R.success(wmsCheckOrderService.addOrUpdateCheckOrder(dto));
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
    public R<WmsCheckOrderVO> getByIdCheckOrderDetail(Long id) {
        return R.success(wmsCheckOrderService.getByIdCheckOrderDetail(id));
    }

    /*****
     * 盘点差异调整
     * @param dto
     * @return
     */
    @GetMapping("/varianceAdjustmentCheckOrder")
    @ApiOperation(value = "盘点差异调整")
    public R<Boolean> varianceAdjustmentCheckOrder(Long id) {
        return R.success(wmsCheckOrderService.varianceAdjustmentCheckOrder(id));
    }
    /*****
     * 手机端 获取盘点单列表
     * @param dto
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

}
