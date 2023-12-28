package com.steccn.pms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.steccn.authserver.exception.BusinessException;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.authserver.util.SecurityUtils;
import com.steccn.common.constant.FeignConstant;
import com.steccn.common.dto.TupleDTO;
import com.steccn.common.util.BigDecimalUtil;
import com.steccn.dp.DpConstant;
import com.steccn.dp.vo.DpWeightVO;
import com.steccn.oa.constant.OaConstant;
import com.steccn.platform.dto.SysConfigurationDTO;
import com.steccn.platform.feign.RemoteSpareFeignApi;
import com.steccn.platform.feign.RemoteSysConfigurationFeignApi;
import com.steccn.platform.vo.EqpSpareVO;
import com.steccn.pms.domain.PmRawMaterialCount;
import com.steccn.pms.dto.PmsRawMaterialCountDTO;
import com.steccn.pms.service.PmRawMaterialCountService;
import com.steccn.pms.mapper.PmRawMaterialCountMapper;
import com.steccn.pms.vo.PmRawMaterialVO;
import com.steccn.wms.dto.WmsDeliveryReceiptDTO;
import com.steccn.wms.dto.WmsDeliveryReceiptInfoDTO;
import com.steccn.wms.dto.WmsStockInventoryDTO;
import com.steccn.wms.feign.RemoteWmsInventoryFeignApi;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

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
    private final RemoteSpareFeignApi remoteSpareFeignApi;
    private final RemoteSysConfigurationFeignApi remoteSysConfigurationFeignApi;

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
        //获取物料信息
        Set<String> refSet = dto.getData().stream().map(TupleDTO::getField1).collect(Collectors.toSet());
        if(refSet.size()!=10){
            throw new BusinessException("请传全量数据");
        }
        Map<String, List<SysConfigurationDTO>> dpWeight = FeignOptional.of(() -> remoteSysConfigurationFeignApi.confList(new TupleDTO<>("DP_WEIGHT", refSet)), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        Map<String, EqpSpareVO> spareMap = FeignOptional.of(() -> remoteSpareFeignApi.listByCode(dpWeight.values().stream()
                .flatMap(List::stream)  // 扁平化 List<SysConfigurationDTO> 为 Stream<SysConfigurationDTO>
                .map(SysConfigurationDTO::getOtherContent)  // 提取 SysConfigurationDTO 中的 name 值
                .filter(v -> StrUtil.isNotBlank(v))
                .distinct()
                .collect(Collectors.toList())), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();

        //创建出库单
        WmsDeliveryReceiptDTO deliveryDTO = new WmsDeliveryReceiptDTO();
        deliveryDTO.setWarehouseId(113);
        deliveryDTO.setWarehouseName("主要材料库");
        deliveryDTO.setType(1582296769061031936L);
        deliveryDTO.setAffirmFlag(OaConstant.NUM_1.toString());
        //获取库存详情
        List<WmsDeliveryReceiptInfoDTO> deliveryInfoDTOs = Lists.newArrayList();
        //获取库存数据
        List<WmsStockInventoryDTO> wmsStockInventory = FeignOptional.of(() -> remoteWmsInventoryFeignApi.materialByWarehouse(Sets.newHashSet(113)), FeignConstant.STECCN_WMS_SERVICE_NAME).get().get(113);
        //开始计算库存
        for(TupleDTO<String,BigDecimal> tmp:dto.getData()){
            //由于返给总合数据，所以只需要算一次,不需要对小料操作
            if(tmp.getField1().equals(DpConstant.DP_WEIGHT_GLASS_2_TAGID)||tmp.getField1().equals(DpConstant.DP_WEIGHT_GUISHA_2_TAGID)||tmp.getField1().equals(DpConstant.DP_WEIGHT_XIAOLIAO_TAGID)){
                continue;
            }
            BigDecimal remain = BigDecimalUtil.divide(tmp.getField2(),new BigDecimal(1000),2) ;
            String materialCode = dpWeight.get(tmp.getField1()).get(0).getOtherContent();
            List<WmsStockInventoryDTO> inventoryTmp = wmsStockInventory.stream().filter(v -> v.getMaterialCode().equals(materialCode)).sorted(Comparator.comparing(WmsStockInventoryDTO::getCreateDate)).collect(Collectors.toList());
            if(CollUtil.isEmpty(inventoryTmp)){
                throw new BusinessException("符合条件的库存为空");
            }
            for (int i = 0; i < inventoryTmp.size(); i++){
                if(remain.compareTo(BigDecimal.ZERO)!=1){
                    continue;
                }
                WmsStockInventoryDTO wmsStockInventoryDTO = inventoryTmp.get(i);
                if(wmsStockInventoryDTO.getAvailableQuantity().compareTo(BigDecimal.ZERO)==0){
                    continue;
                }
                WmsDeliveryReceiptInfoDTO deliveryReceiptInfo = new WmsDeliveryReceiptInfoDTO();
                //开始扣库存，若库存大于等于耗料数，则只扣减1次，否则扣完
                if(wmsStockInventoryDTO.getAvailableQuantity().compareTo(remain)==1||
                        wmsStockInventoryDTO.getAvailableQuantity().compareTo(remain)==0){
                    deliveryReceiptInfo.setQuantity(remain);
                }else {
                    deliveryReceiptInfo.setQuantity(wmsStockInventoryDTO.getAvailableQuantity());
                }
                remain = BigDecimalUtil.subtract(remain,deliveryReceiptInfo.getQuantity(),2);
                deliveryReceiptInfo.setInventoryId(wmsStockInventoryDTO.getInventoryId());
                deliveryReceiptInfo.setMaterialId(wmsStockInventoryDTO.getMaterialId());
                deliveryReceiptInfo.setMaterialCode(wmsStockInventoryDTO.getMaterialCode());
                deliveryReceiptInfo.setMaterialName(wmsStockInventoryDTO.getMaterialName());
                deliveryReceiptInfo.setShelfId(wmsStockInventoryDTO.getShelfId().longValue());
                deliveryReceiptInfo.setShelfName(wmsStockInventoryDTO.getShelfName());
                deliveryReceiptInfo.setBatch(wmsStockInventoryDTO.getBatchNo());
                deliveryReceiptInfo.setCaseNo(wmsStockInventoryDTO.getCaseNo());
                deliveryReceiptInfo.setUnit(wmsStockInventoryDTO.getUnit());
                deliveryInfoDTOs.add(deliveryReceiptInfo);
            }
        }
        deliveryDTO.setList(deliveryInfoDTOs);
        Long deliveryId = FeignOptional.of(() -> remoteWmsInventoryFeignApi.createDeliveryId(deliveryDTO), FeignConstant.STECCN_WMS_SERVICE_NAME).get();
        //不为空则需要冲销
        if(Objects.nonNull(one)&&one.getDeliveryId()!=null){
            FeignOptional.of(() -> remoteWmsInventoryFeignApi.chargeAgainstDelivery(new TupleDTO<>(one.getDeliveryId(), SecurityUtils.getUser().getName())), FeignConstant.STECCN_WMS_SERVICE_NAME).get();
        }
        //保存数据
        //1、先删除
        remove(new LambdaQueryWrapper<PmRawMaterialCount>().eq(PmRawMaterialCount::getClassDate,dto.getDate()));
        //2、后保存
        List<PmRawMaterialCount> models = Lists.newArrayList();
        dto.getData().forEach(tmp->{
            PmRawMaterialCount model = new PmRawMaterialCount();
            model.setClassDate(dto.getDate());
            model.setDeliveryId(deliveryId);
            model.setTagId(tmp.getField1());
            model.setValue(tmp.getField2());
            if(!model.getTagId().equals(DpConstant.DP_WEIGHT_XIAOLIAO_TAGID)){
                model.setMaterialCode(dpWeight.get(tmp.getField1()).get(0).getOtherContent());
                model.setMaterialId(spareMap.get(model.getMaterialCode()).getId());
            }else {
                model.setMaterialCode("");
            }
            models.add(model);
        });
        saveBatch(models);
        return true;
    }
}




