package com.steccn.wms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Maps;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.common.constant.FeignConstant;
import com.steccn.common.constant.MpConstant;
import com.steccn.pms.dto.PmsProducerOrderInfoDTO;
import com.steccn.pms.feign.RemotePmsFeignApi;
import com.steccn.wms.constant.WmsManagerConstant;
import com.steccn.wms.domain.WmsCustomerBarcode;
import com.steccn.wms.dto.WmsCustomerBarcodeDTO;
import com.steccn.wms.service.WmsCustomerBarcodeService;
import com.steccn.wms.mapper.WmsCustomerBarcodeMapper;
import com.steccn.wms.vo.WmsCustomerBarcodeVO;
import lombok.RequiredArgsConstructor;
import org.apache.commons.compress.utils.Lists;
import org.springframework.stereotype.Service;

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

    @Override
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
                    wmsCustomerBarcodes.add(wmsCustomerBarcode);
                }else {
                    WmsCustomerBarcode wmsCustomerBarcode = new WmsCustomerBarcode();
                    BeanUtil.copyProperties(wmsCustomerBarcodeDTO,wmsCustomerBarcode);
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
        WmsCustomerBarcode barcode = getOne(new LambdaQueryWrapper<WmsCustomerBarcode>().eq(WmsCustomerBarcode::getCustomerBarcode, customerBarcode));
        if (Objects.nonNull(barcode.getCaseNo())){
            //根据箱号获取生产订单号以及批次号以及物料名称
            PmsProducerOrderInfoDTO dto = FeignOptional.of(() -> remotePmsFeignApi.getNumberByCaseNo(barcode.getCaseNo()), FeignConstant.STECCN_PMS_SERVICE_NAME).get();
            BeanUtil.copyProperties(dto,wmsCustomerBarcodeVO);
            wmsCustomerBarcodeVO.setCaseNo(barcode.getCaseNo());
        }
        return wmsCustomerBarcodeVO;
    }
}




