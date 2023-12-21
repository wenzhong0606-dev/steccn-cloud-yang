package com.steccn.pms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.authserver.util.SecurityUtils;
import com.steccn.common.constant.FeignConstant;
import com.steccn.common.dto.TupleDTO;
import com.steccn.pms.domain.PmRawMaterialCount;
import com.steccn.pms.dto.PmsRawMaterialCountDTO;
import com.steccn.pms.service.PmRawMaterialCountService;
import com.steccn.pms.mapper.PmRawMaterialCountMapper;
import com.steccn.pms.vo.PmRawMaterialVO;
import com.steccn.wms.dto.WmsDeliveryReceiptDTO;
import com.steccn.wms.dto.WmsDeliveryReceiptInfoDTO;
import com.steccn.wms.feign.RemoteWmsInventoryFeignApi;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

/**
* @author Admin
* @description 针对表【pm_raw_material_count(原料配料统计表)】的数据库操作Service实现
* @createDate 2023-12-19 16:18:55
*/
@Service
@RequiredArgsConstructor
public class PmRawMaterialCountServiceImpl extends ServiceImpl<PmRawMaterialCountMapper, PmRawMaterialCount>
    implements PmRawMaterialCountService{
    private final RemoteWmsInventoryFeignApi remoteWmsInventoryFeignApi;

    @Override
    public List<PmRawMaterialVO> getListByDate(TupleDTO<LocalDate,LocalDate> dto) {
        List<PmRawMaterialCount> list = list(new LambdaQueryWrapper<PmRawMaterialCount>().between(PmRawMaterialCount::getClassDate,dto.getField1(),dto.getField2()));
        List<PmRawMaterialVO> results = Lists.newArrayList();
        if(CollUtil.isNotEmpty(list)){
            list.forEach(tmp->{
                PmRawMaterialVO vo = new PmRawMaterialVO();
                BeanUtil.copyProperties(tmp,vo);
                results.add(vo);
            });
        }
        return results;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean generateDeliveryAndSave(PmsRawMaterialCountDTO dto) {
        //先查是否有改天的数据
        PmRawMaterialCount one = getOne(new LambdaQueryWrapper<PmRawMaterialCount>()
                .eq(PmRawMaterialCount::getClassDate, dto.getDate())
                .last("LIMIT 1"));
        //不为空则需要冲销
        if(Objects.nonNull(one)){
            FeignOptional.of(() -> remoteWmsInventoryFeignApi.chargeAgainstDelivery(new TupleDTO<>(one.getDeliveryId(), SecurityUtils.getUser().getName())), FeignConstant.STECCN_WMS_SERVICE_NAME).get();
        }
        //创建出库单
        WmsDeliveryReceiptDTO deliveryDTO = new WmsDeliveryReceiptDTO();
        deliveryDTO.setWarehouseId(113);
        deliveryDTO.setWarehouseName("主要材料库");
        deliveryDTO.setType(1582296769061031936L);
        //获取库存详情
        List<WmsDeliveryReceiptInfoDTO> deliveryInfoDTO = Lists.newArrayList();

        Long deliveryId = FeignOptional.of(() -> remoteWmsInventoryFeignApi.createDeliveryId(deliveryDTO), FeignConstant.STECCN_WMS_SERVICE_NAME).get();
        //保存数据
        //1、先删除
        remove(new LambdaQueryWrapper<PmRawMaterialCount>().eq(PmRawMaterialCount::getClassDate,dto.getDate()));
        //2、后保存
        List<PmRawMaterialCount> models = Lists.newArrayList();
        dto.getData().forEach(tmp->{
            PmRawMaterialCount model = new PmRawMaterialCount();
            model.setDeliveryId(deliveryId);
            model.setTagId(tmp.getField1());
            model.setValue(tmp.getField2());
            models.add(model);
        });
        return true;
    }
}




