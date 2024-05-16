package com.steccn.wms.controller;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.authserver.util.SecurityUtils;
import com.steccn.common.annotation.SysLog;
import com.steccn.common.constant.FeignConstant;
import com.steccn.common.valid.InsertGroup;
import com.steccn.common.valid.UpdateGroup;
import com.steccn.common.vo.R;
import com.steccn.oa.constant.OaConstant;
import com.steccn.platform.feign.RemoteSysOauthClientDetailFeignApi;
import com.steccn.pms.dto.PmExcelDTO;
import com.steccn.pms.vo.ProducePlanImportVO;
import com.steccn.wms.domain.WmsStorageReceipt;
import com.steccn.wms.dto.*;
import com.steccn.wms.service.WmsStorageReceiptService;
import com.steccn.wms.service.WmsWeighbridgeInfoService;
import com.steccn.wms.vo.*;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

/**
 * 入库凭证控制器
 *
 * @author Mongknife
 * @date 2022/12/9
 */

@RestController
@RequestMapping("/wms/storage/receipt")
@RequiredArgsConstructor
@Slf4j
public class WmsStorageReceiptController {

    private final WmsStorageReceiptService wmsStorageReceiptService;

    private final RemoteSysOauthClientDetailFeignApi remoteSysOauthClientDetailFeignApi;

    private final WmsWeighbridgeInfoService wmsWeighbridgeInfoService;

    @ApiOperation("入库凭证分页列表")
    @PostMapping("/list")
    public R< Page<WmsStorageReceiptVO>> pageList(@RequestBody WmsStorageReceiptPageDTO dto) {
        return R.success(wmsStorageReceiptService.pageList(dto));
    }

    @ApiOperation("新增")
    @PostMapping()
    @SysLog("入库凭证新增")
    public R<Boolean> add(@Validated(InsertGroup.class) @RequestBody WmsStorageReceiptDTO dto) {
        return R.success(wmsStorageReceiptService.add(dto));
    }

    @ApiOperation("新增")
    @PostMapping("/produce/old/line/add")
    @SysLog("生产老线入库新增")
    public R<String> produceOldLineAdd(@RequestBody WmsStorageReceiptDTO dto){
        String code = wmsStorageReceiptService.produceOldLineAdd(dto);
        //调用确认接口
        WmsStorageReceipt wmsStorageReceipt = wmsStorageReceiptService.getOne(new LambdaQueryWrapper<WmsStorageReceipt>().eq(WmsStorageReceipt::getCode, code), false);
        Assert.notNull(wmsStorageReceipt, "入库单为空");
        wmsStorageReceiptService.affirm(wmsStorageReceipt.getId(), SecurityUtils.getUser().getName());
        return R.success(code);
    }

    @ApiOperation("采购系统新增")
    @PostMapping("/sales")
    @SysLog("入库凭证新增")
    public R<Boolean> salesAdd(HttpServletRequest request, @RequestBody WmsStorageReceiptSalesDTO dto) {
        validRequestHeader(request);
        return R.success(wmsStorageReceiptService.salesAdd(dto));
    }

    @ApiOperation("入库凭证编辑")
    @PutMapping
    @SysLog("入库凭证编辑")
    public R<Boolean> edit(@Validated(UpdateGroup.class) @RequestBody WmsStorageReceiptDTO dto) {
        return R.success(wmsStorageReceiptService.edit(dto));
    }

    @ApiOperation("入库凭证确认")
    @PutMapping("/affirm")
    @SysLog("入库凭证确认")
    public R<Boolean> affirm(@RequestBody WmsStorageReceiptDTO dto) {
        Assert.notNull(dto.getId(), "入库凭证id不能为空");
        return R.success(wmsStorageReceiptService.affirm(dto.getId(),SecurityUtils.getUser().getName()));
    }

    @ApiOperation("批量修改入库单货架")
    @PostMapping("/update/batch/shelf")
    @SysLog("批量修改入库单货架为同一货架")
    public R<Boolean> updateBatchShelf(@RequestBody WmsUpdateBatchShelfDTO dto){
        return R.success(wmsStorageReceiptService.updateBatchShelf(dto));
    }

    @ApiOperation("冲销")
    @PostMapping("/charge/against")
    public R<Boolean> chargeAgainst(@RequestBody WmsStorageReceiptDTO dto){
        return R.success(wmsStorageReceiptService.chargeAgainstById(dto.getId()));
    }

    @ApiOperation("入库凭证确认")
    @PutMapping("/affirm/code")
    @SysLog("入库凭证确认")
    public R<Boolean> affirmByCode(@RequestBody WmsStorageReceiptDTO dto) {
        WmsStorageReceipt one = wmsStorageReceiptService.getOne(new LambdaQueryWrapper<WmsStorageReceipt>().eq(WmsStorageReceipt::getCode, dto.getCode()), false);
        Assert.notNull(one, "入库单不存在");
        return R.success(wmsStorageReceiptService.affirm(one.getId(),SecurityUtils.getUser().getName()));
    }

    @ApiOperation("入库凭证删除")
    @DeleteMapping()
    @SysLog("入库凭证删除")
    public R<Boolean> delete(@RequestBody WmsStorageReceiptDTO dto) {
        Assert.notNull(dto.getId(), "入库凭证id不能为空");
        return R.success(wmsStorageReceiptService.delete(dto.getId()));
    }

    @ApiOperation("入库凭证详情")
    @GetMapping()
    public R<WmsStorageReceiptVO> get(@ApiParam("入库单id") @RequestParam("id") Long id){
        return R.success(wmsStorageReceiptService.get(id));
    }

    @ApiModelProperty("Excel导入")
    @PostMapping("/excelImportData")
    public R<Boolean> excelImportData(@Validated(InsertGroup.class) PmExcelDTO dto) throws IOException {
        return R.success(wmsStorageReceiptService.excelImportData(dto));
    }

    @ApiOperation("入库凭证同步erp")
    @GetMapping("/linkErp")
    public R<Boolean> linkErp(@ApiParam("入库单id") @RequestParam("id") Long id){
        return R.success(wmsStorageReceiptService.linkErp(id));
    }

    @ApiOperation("采购入库同步erp")
    @GetMapping("/purchase/linkErp")
    public R<Boolean> purchaseLinkErp(@ApiParam("入库单id") @RequestParam("id") Long id){
        return R.success(wmsStorageReceiptService.purchaseLinkErp(id));
    }

    @ApiOperation("入库单号重复-排查")
    @GetMapping("/in/storage/code")
    public R<String> inStorageCodeRepeat(@RequestParam("date") LocalDate current) {
        return R.success(wmsStorageReceiptService.inStorageCodeRepeat(current));
    }

    @ApiOperation("根据仓库批量删除入库单数据")
    @PostMapping("/delete/data")
    public R<Boolean> deleteData (){
        return R.success(wmsStorageReceiptService.deleteData());
    }

    @ApiOperation("采购入库获取箱号入库单标识相关内容")
    @PostMapping("/storage/flag/info")
    public R<WmsStorageFlagInfoVO> storageFlagInfo(@RequestBody WmsStorageReceiptFlagInfoDTO dto){
        return R.success(wmsStorageReceiptService.storageFlagInfo(dto));
    }


    @ApiOperation("销售退货获取物流单标识相关内容")
    @GetMapping("/sales/returns/flag")
    public R<WmsSalesReturnsFlagVO> salesReturnsFlag(@RequestParam("number")String number){
        return R.success(wmsStorageReceiptService.salesReturnsFlag(number));
    }


    @ApiOperation("销售退货")
    @PostMapping("/sales/returns")
    public R<String> salesReturns(@RequestBody WmsSalesReturnsDTO wmsSalesReturnsDTO){
        return R.success(wmsStorageReceiptService.salesReturns(wmsSalesReturnsDTO));
    }

    @ApiOperation("采购入库单明细")
    @PostMapping("/sales/page")
    public R<Page<WmsSalesDeliveryVO>> salesDeliveryPage(@RequestBody WmsDeliveryOrderDTO dto){
        return R.success(wmsStorageReceiptService.salesDeliveryPage(dto));
    }

    @ApiOperation("采购入库凭证")
    @PostMapping("/sales/add")
    public R<String> salesReceiptAdd(@RequestBody @Validated(InsertGroup.class) SalesStorageReceiptDTO dto){
        return R.success(wmsStorageReceiptService.salesReceiptAdd(dto));
    }

    @ApiOperation("地磅信息生成采购入库")
    @PostMapping("/weighbridge/generate")
    @SysLog("地磅生成采购入库")
    public R<Boolean> generateList(@RequestBody @Validated WmsWeighbridgeGerDTO dto){
        return R.success(wmsWeighbridgeInfoService.generateList(dto));
    }

    @ApiOperation("地磅信息分页查询")
    @PostMapping("/weighbridge/page")
    public R<Page<WmsWeighbridgeInfoVO>> weighbridgePage(@RequestBody WmsWeighbridgeInfoDTO dto){
        return R.success(wmsWeighbridgeInfoService.pageList(dto));
    }

    /**
     * 校验请求头信息
     *
     * @param request 请求
     */
    private void validRequestHeader(HttpServletRequest request) {
        List<String> pwdSet = FeignOptional.of(() -> remoteSysOauthClientDetailFeignApi.clientEncodePassword(), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        String clientId = request.getHeader(OaConstant.CLIENT_ID);
        Assert.isTrue(StrUtil.isNotBlank(clientId), "请求头中必须携带client-id");
        Assert.isTrue(pwdSet.contains(clientId), "非法访问，请携带正确的client-id");
    }

}
