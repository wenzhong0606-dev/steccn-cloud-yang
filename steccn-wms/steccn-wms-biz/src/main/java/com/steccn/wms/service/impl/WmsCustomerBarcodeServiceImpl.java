package com.steccn.wms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.steccn.common.constant.MpConstant;
import com.steccn.wms.domain.WmsCustomerBarcode;
import com.steccn.wms.dto.WmsCustomerBarcodeDTO;
import com.steccn.wms.service.WmsCustomerBarcodeService;
import com.steccn.wms.mapper.WmsCustomerBarcodeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
* @author 34872
* @description 针对表【wms_customer_barcode(客户条码绑定)】的数据库操作Service实现
* @createDate 2023-10-20 13:59:53
*/
@Service
@RequiredArgsConstructor
public class WmsCustomerBarcodeServiceImpl extends ServiceImpl<WmsCustomerBarcodeMapper, WmsCustomerBarcode>
    implements WmsCustomerBarcodeService {

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
}




