package com.steccn.wms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Maps;
import com.steccn.authserver.exception.BusinessException;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.authserver.util.SecurityUtils;
import com.steccn.common.constant.FeignConstant;
import com.steccn.common.constant.MpConstant;
import com.steccn.platform.dto.SalesLogisticsDTO;
import com.steccn.platform.feign.RemoteSalesFeignApi;
import com.steccn.platform.vo.SalesLogisticsVO;
import com.steccn.pms.dto.PmsProducerOrderInfoDTO;
import com.steccn.pms.feign.RemotePmsFeignApi;
import com.steccn.sales.dto.KsLogisticsOrderDTO;
import com.steccn.sales.feign.RemoteSaleFeignApi;
import com.steccn.sales.vo.ResUserVO;
import com.steccn.wms.constant.WmsManagerConstant;
import com.steccn.wms.domain.WmsCustomerBarcode;
import com.steccn.wms.domain.WmsDeliveryReceipt;
import com.steccn.wms.domain.WmsDeliveryReceiptInfo;
import com.steccn.wms.domain.WmsInventory;
import com.steccn.wms.dto.WmsCustomerBarcodeDTO;
import com.steccn.wms.mapper.WmsDeliveryReceiptInfoMapper;
import com.steccn.wms.mapper.WmsDeliveryReceiptMapper;
import com.steccn.wms.mapper.WmsInventoryMapper;
import com.steccn.wms.service.WmsCustomerBarcodeService;
import com.steccn.wms.mapper.WmsCustomerBarcodeMapper;
import com.steccn.wms.service.WmsDeliveryReceiptService;
import com.steccn.wms.vo.WmsCustomerBarcodeVO;
import com.steccn.wms.vo.WmsDeliveryCustomerBarCodeVO;
import com.steccn.wms.vo.WmsDeliverySellFlagVO;
import lombok.RequiredArgsConstructor;
import org.apache.commons.compress.utils.Lists;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
* @author 34872
* @description 针对表【wms_customer_barcode(客户条码绑定)】的数据库操作Service实现
* @createDate 2023-10-20 13:59:53
*/
@Service
@RequiredArgsConstructor
public class WmsCustomerBarcodeServiceImpl extends ServiceImpl<WmsCustomerBarcodeMapper, WmsCustomerBarcode>
    implements WmsCustomerBarcodeService {

    private final RemotePmsFeignApi remotePmsFeignApi;
    private final WmsDeliveryReceiptMapper wmsDeliveryReceiptMapper;
    private final WmsDeliveryReceiptInfoMapper wmsDeliveryReceiptInfoMapper;
    private final WmsInventoryMapper wmsInventoryMapper;
    private final RemoteSalesFeignApi remoteSalesFeignApi;
    private final RemoteSaleFeignApi remoteSaleFeignApi;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean add(WmsCustomerBarcodeDTO dto) {
        WmsCustomerBarcode customerBarcode = getOne(new LambdaQueryWrapper<WmsCustomerBarcode>().eq(WmsCustomerBarcode::getCaseNo, dto.getCaseNo()));
        if (Objects.nonNull(customerBarcode)){
            BeanUtil.copyProperties(dto,customerBarcode);
           return updateById(customerBarcode);
        }else {
           return save(BeanUtil.copyProperties(dto,WmsCustomerBarcode.class));
        }
    }

    @Override
    public String getCustomerBarcodeByCaseNO(String caseNo) {
        WmsCustomerBarcode customerBarcode = getOne(new LambdaQueryWrapper<WmsCustomerBarcode>().eq(WmsCustomerBarcode::getCaseNo, caseNo));
        if (Objects.nonNull(customerBarcode)){
            return customerBarcode.getCustomerBarcode();
        }else {
            return null;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean batchUpdate(List<WmsCustomerBarcodeDTO> list) {
        if (CollUtil.isNotEmpty(list)){
            List<WmsCustomerBarcode> wmsCustomerBarcodes = Lists.newArrayList();
            Set<String> collect = list.stream().map(WmsCustomerBarcodeDTO::getCaseNo).collect(Collectors.toSet());
            Map<String, List<WmsCustomerBarcode>> caseMap = list(new LambdaQueryWrapper<WmsCustomerBarcode>()
                    .in(WmsCustomerBarcode::getCaseNo, collect))
                    .stream()
                    .collect(Collectors.groupingBy(WmsCustomerBarcode::getCaseNo));
            list.forEach( wmsCustomerBarcodeDTO -> {
                List<WmsCustomerBarcode> barcodes = caseMap.get(wmsCustomerBarcodeDTO.getCaseNo());
                if (CollUtil.isNotEmpty(barcodes)){
                    WmsCustomerBarcode wmsCustomerBarcode = barcodes.get(WmsManagerConstant.InventoryEnum.ZERO.getCode());
                    wmsCustomerBarcode.setCustomerBarcode(wmsCustomerBarcodeDTO.getCustomerBarcode());
                    wmsCustomerBarcode.setUpdateBy(wmsCustomerBarcodeDTO.getUpdateBy());
                    wmsCustomerBarcode.setUpdateName(wmsCustomerBarcodeDTO.getUpdateName());
                    wmsCustomerBarcodes.add(wmsCustomerBarcode);
                }else {
                    WmsCustomerBarcode wmsCustomerBarcode = new WmsCustomerBarcode();
                    BeanUtil.copyProperties(wmsCustomerBarcodeDTO,wmsCustomerBarcode);
                    wmsCustomerBarcode.setCreateBy(wmsCustomerBarcodeDTO.getUpdateBy());
                    wmsCustomerBarcode.setCreateName(wmsCustomerBarcodeDTO.getUpdateName());
                    wmsCustomerBarcodes.add(wmsCustomerBarcode);

                }
            });
            saveOrUpdateBatch(wmsCustomerBarcodes);
        }
        return Boolean.TRUE;
    }

    @Override
    public Map<String, String> customerBarcodeMap(Set<String> caseNos) {
        Map<String, String> resMap = Maps.newHashMap();
        if (CollUtil.isNotEmpty(caseNos)) {
            List<WmsCustomerBarcode> list = list(new LambdaQueryWrapper<WmsCustomerBarcode>()
                    .in(WmsCustomerBarcode::getCaseNo, caseNos));
            if (CollUtil.isNotEmpty(list)) {
                list.forEach(wmsCustomerBarcode -> resMap.put(wmsCustomerBarcode.getCaseNo(), wmsCustomerBarcode.getCustomerBarcode()));
            }
        }
        return resMap;
    }

    @Override
    public List<String> listByCustomerBarcode(String customerBarcode) {
        List<WmsCustomerBarcode> list = list(new LambdaQueryWrapper<WmsCustomerBarcode>()
                .like(WmsCustomerBarcode::getCustomerBarcode, customerBarcode));
        List<String> resList = Lists.newArrayList();
        if (CollUtil.isNotEmpty(list)){
            resList = list.stream().map(WmsCustomerBarcode::getCaseNo).collect(Collectors.toList());
        }
        return resList;
    }

    @Override
    public WmsCustomerBarcodeVO CustomerBarcodeByCaseNO(String customerBarcode) {
        WmsCustomerBarcodeVO wmsCustomerBarcodeVO = new WmsCustomerBarcodeVO();
        WmsCustomerBarcode barcode = getOne(new LambdaQueryWrapper<WmsCustomerBarcode>().eq(WmsCustomerBarcode::getCustomerBarcode, customerBarcode).last(MpConstant.STR_LIMIT_1));
        if (Objects.nonNull(barcode)){
            //根据箱号获取生产订单号以及批次号以及物料名称
            PmsProducerOrderInfoDTO dto = FeignOptional.of(() -> remotePmsFeignApi.getNumberByCaseNo(barcode.getCaseNo()), FeignConstant.STECCN_PMS_SERVICE_NAME).get();
            BeanUtil.copyProperties(dto,wmsCustomerBarcodeVO);
            wmsCustomerBarcodeVO.setCaseNo(barcode.getCaseNo());
        }
        return wmsCustomerBarcodeVO;
    }

    @Override
    public List<WmsDeliveryCustomerBarCodeVO>  getDeliveryCustomerBarCode(Set<String> logisticsCodes) {
        Assert.notEmpty(logisticsCodes,"数据不能为空");
        List<WmsDeliveryCustomerBarCodeVO> results = com.google.common.collect.Lists.newArrayList();
        //获取出库单
        List<WmsDeliveryReceipt> wmsDeliveryReceipts = wmsDeliveryReceiptMapper.selectList(new LambdaQueryWrapper<WmsDeliveryReceipt>()
                .select(WmsDeliveryReceipt::getId,WmsDeliveryReceipt::getCode,WmsDeliveryReceipt::getSalesOrder)
                .in(WmsDeliveryReceipt::getSalesOrder, logisticsCodes));
        //获取出库单详情
        List<WmsDeliveryReceiptInfo> wmsDeliveryReceiptInfos = Lists.newArrayList();
        if(CollUtil.isNotEmpty(wmsDeliveryReceipts)){
            List<Long> receiptId = wmsDeliveryReceipts.stream().map(WmsDeliveryReceipt::getId).collect(Collectors.toList());
            wmsDeliveryReceiptInfos = wmsDeliveryReceiptInfoMapper.selectList(new LambdaQueryWrapper<WmsDeliveryReceiptInfo>()
                    .select(WmsDeliveryReceiptInfo::getInventoryId,WmsDeliveryReceiptInfo::getDeliveryReceiptId)
                    .in(WmsDeliveryReceiptInfo::getDeliveryReceiptId,receiptId));
        }
        //获取库存详情
        List<WmsInventory> wmsInventories = Lists.newArrayList();
        if(CollUtil.isNotEmpty(wmsDeliveryReceiptInfos)){
            List<Long> inventoryId = wmsDeliveryReceiptInfos.stream().map(WmsDeliveryReceiptInfo::getInventoryId).collect(Collectors.toList());
            if(CollUtil.isNotEmpty(inventoryId)){
                wmsInventories = wmsInventoryMapper.selectList(new LambdaQueryWrapper<WmsInventory>()
                        .select(WmsInventory::getId,WmsInventory::getCaseNo)
                        .in(WmsInventory::getId,inventoryId));
            }
        }
        //获取客户条码
        List<WmsCustomerBarcode> wmsCustomerBarcodes = Lists.newArrayList();
        if(CollUtil.isNotEmpty(wmsInventories)){
            List<String> caseNos = wmsInventories.stream().map(WmsInventory::getCaseNo).collect(Collectors.toList());
            if(CollUtil.isNotEmpty(caseNos)){
                wmsCustomerBarcodes = list(new LambdaQueryWrapper<WmsCustomerBarcode>()
                        .select(WmsCustomerBarcode::getCustomerBarcode,WmsCustomerBarcode::getCaseNo)
                        .in(WmsCustomerBarcode::getCaseNo,caseNos));
            }
        }
        //开始组数据
        for(String tmp:logisticsCodes){
            WmsDeliveryCustomerBarCodeVO result = new WmsDeliveryCustomerBarCodeVO();
            //出库单
            WmsDeliveryReceipt wmsDeliveryReceiptTmp = wmsDeliveryReceipts.stream().filter(v -> v.getSalesOrder().equals(tmp)).findFirst().orElse(null);
            result.setLogisticsCode(tmp);
            if(Objects.nonNull(wmsDeliveryReceiptTmp)){
                result.setDeliveryCode(wmsDeliveryReceiptTmp.getCode());
                //出库单详情
                if(CollUtil.isNotEmpty(wmsDeliveryReceiptInfos)){
                    List<WmsDeliveryReceiptInfo> wmsDeliveryReceiptInfoTmp = wmsDeliveryReceiptInfos.stream().filter(v -> v.getDeliveryReceiptId().equals(wmsDeliveryReceiptTmp.getId())).collect(Collectors.toList());
                    //获取库存详情
                    if(CollUtil.isNotEmpty(wmsDeliveryReceiptInfoTmp)&&CollUtil.isNotEmpty(wmsInventories)){
                        List<Long> inventoryIds = wmsDeliveryReceiptInfoTmp.stream().map(WmsDeliveryReceiptInfo::getInventoryId).collect(Collectors.toList());
                        if(CollUtil.isNotEmpty(inventoryIds)){
                            List<WmsInventory> inventoriesTmp = wmsInventories.stream().filter(v -> inventoryIds.contains(v.getId())).collect(Collectors.toList());
                            //获取客户条码
                            List<String> caseNoTmp = inventoriesTmp.stream().map(WmsInventory::getCaseNo).collect(Collectors.toList());
                            if(CollUtil.isNotEmpty(caseNoTmp)){
                                List<WmsCustomerBarcode> wmsCustomerBarcodesTmp = wmsCustomerBarcodes.stream().filter(v -> caseNoTmp.contains(v.getCaseNo())).collect(Collectors.toList());
                                if(CollUtil.isNotEmpty(wmsCustomerBarcodesTmp)){
                                    List<String> barCodeStr = wmsCustomerBarcodesTmp.stream().map(WmsCustomerBarcode::getCustomerBarcode).distinct().collect(Collectors.toList());
                                    result.setCustomerBarCode(barCodeStr);
                                }
                            }
                        }
                    }
                }

            }
            results.add(result);
        }
        return results;
    }

    @Override
    public WmsDeliverySellFlagVO customerBarcode(String customerBarcode) {
        WmsDeliverySellFlagVO wmsDeliverySellFlagVO = new WmsDeliverySellFlagVO();
        //根据客户条码获取物流单号
         String salesOrder =   wmsDeliveryReceiptMapper.getSalesOrderByCustomerBarcode(customerBarcode);
         if (Objects.nonNull(salesOrder)){
             //调用物流单明细查询接口
             SalesLogisticsDTO dto = new SalesLogisticsDTO();
             dto.setServer_code(WmsManagerConstant.SERVER_CODE1);
             List<String> codes = Lists.newArrayList();
             codes.add(salesOrder);
             dto.setCode(codes);
             String data = remoteSalesFeignApi.queryLogistics(dto);
             JSONObject jsonObject = JSONObject.parseObject(data);
             try {
                 List<SalesLogisticsVO> logisticsVOS = jsonObject.getJSONObject("result").getJSONArray("mdMappings").toJavaList(SalesLogisticsVO.class);
                 if (CollUtil.isNotEmpty(logisticsVOS)){
                     wmsDeliverySellFlagVO.setNumber(WmsManagerConstant.YesOrNo.YES.getCode());
                     SalesLogisticsVO salesLogisticsVO = logisticsVOS.get(0);
                     wmsDeliverySellFlagVO.setSalesOrder(salesLogisticsVO.getName());
                     wmsDeliverySellFlagVO.setClient(salesLogisticsVO.getCustomer_name());
                     wmsDeliverySellFlagVO.setSendQuantity(new BigDecimal(salesLogisticsVO.getSale_qty()));
                     wmsDeliverySellFlagVO.setPriceQuantity(salesLogisticsVO.getPrice_qty());
                     wmsDeliverySellFlagVO.setProduct(salesLogisticsVO.getProduct_name());
                     wmsDeliverySellFlagVO.setSendDate(salesLogisticsVO.getSend_date());
                     //根据物料名称获取单片面积
//                     wmsDeliverySellFlagVO.setCarCode(salesLogisticsVO.get);
                     String state = salesLogisticsVO.getState();
//                     if (state.equals("已离厂")) {
//                         wmsDeliverySellFlagVO.setLeaveTheFactoryStateFlag(WmsManagerConstant.YesOrNo.YES.getCode());
//                     }
                     if (state.equals("已预约")) {
                         wmsDeliverySellFlagVO.setMakeAnAppointmentFlag(WmsManagerConstant.YesOrNo.YES.getCode());
                     }
                     if (state.equals("已反馈")) {
                         wmsDeliverySellFlagVO.setFeedbackFlag(WmsManagerConstant.YesOrNo.YES.getCode());
                     }
                     KsLogisticsOrderDTO ksLogisticsOrderDTO = new KsLogisticsOrderDTO();
                     ksLogisticsOrderDTO.setName(salesLogisticsVO.getName());
                     Integer integer = FeignOptional.of(() -> remoteSaleFeignApi.salesOrderCompanyId(ksLogisticsOrderDTO), FeignConstant.STECCN_SALES_NAME).get();
                     if (Objects.nonNull(SecurityUtils.getUser())) {
                         ResUserVO resUserVO = FeignOptional.of(() -> remoteSaleFeignApi.salesUserInfo(SecurityUtils.getUser().getName()), FeignConstant.STECCN_SALES_NAME).get();
                         if (Objects.nonNull(resUserVO) && Objects.equals(resUserVO.getLogisticsCompanyId(), integer)) {
                             wmsDeliverySellFlagVO.setCompanyFlag(1);
                         }
                     }
                 }
             } catch (Exception e) {
                 throw new BusinessException("物流单不存在");
             }
         }
        return wmsDeliverySellFlagVO;
    }
}




