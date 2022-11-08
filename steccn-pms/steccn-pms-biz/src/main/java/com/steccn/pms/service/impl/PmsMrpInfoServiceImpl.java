package com.steccn.pms.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Sets;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.platform.feign.RemoteDictFeignApi;
import com.steccn.platform.vo.SysDictVO;
import com.steccn.pms.constant.PmManagerConstant;
import com.steccn.pms.domain.PmsMrp;
import com.steccn.pms.domain.PmsMrpInfo;
import com.steccn.pms.domain.PmsProducePlan;
import com.steccn.pms.dto.MesMaterialInventoryDTO;
import com.steccn.pms.dto.PmsProducePlanDTO;
import com.steccn.pms.dto.PmsProducePlanDetailsDTO;
import com.steccn.pms.feign.RemoteMessFeignApi;
import com.steccn.pms.service.PmsMrpInfoService;
import com.steccn.pms.mapper.PmsMrpInfoMapper;
import lombok.RequiredArgsConstructor;
import org.apache.commons.compress.utils.Lists;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author yequan
 * @description 针对表【pms_mrp_info(物料需求计划详情)】的数据库操作Service实现
 * @createDate 2022-11-02 10:10:08
 */
@Service
@RequiredArgsConstructor
public class PmsMrpInfoServiceImpl extends ServiceImpl<PmsMrpInfoMapper, PmsMrpInfo>
        implements PmsMrpInfoService {

    private final RemoteDictFeignApi remoteDictFeignApi;
    private final RemoteMessFeignApi remoteMessFeignApi;

    @Override
    public Boolean add(PmsProducePlanDTO dto) {
        //1.处理数据
        Set<Long> materialIds = Sets.newHashSet();
        materialIds.add(dto.getMaterialId());
        Map<Long, MesMaterialInventoryDTO> materialInventoryMap = remoteMessFeignApi.materialInventory(materialIds);
        MesMaterialInventoryDTO inventoryDTO = materialInventoryMap.get(dto.getMaterialId());
        BigDecimal availableQuantity = inventoryDTO.getAvailableQuantity();
        List<PmsMrpInfo> list = Lists.newArrayList();
        if (CollUtil.isNotEmpty(dto.getProducePlanDetailsDTOList())) {
            List<PmsProducePlanDetailsDTO> producePlanDetailsDTOList = dto.getProducePlanDetailsDTOList();
            for (PmsProducePlanDetailsDTO pmsProducePlanDetailsDTO : producePlanDetailsDTOList) {
                //1.1新增独立需求计划 todo 暂不计算安全库存
                availableQuantity = availableQuantity.subtract(pmsProducePlanDetailsDTO.getQuantity());
                list.add(create(PmManagerConstant.MRP_INDEPENDENT_DEMAND, pmsProducePlanDetailsDTO.getQuantity(),
                        availableQuantity, dto.getMaterialId(), pmsProducePlanDetailsDTO.getPlanDate()));
            }
        }
        //2.保存数据
        return saveBatch(list);
    }

    @Override
    public Boolean mrpAdd(Set<Long> list) {
        //1.根据物料id 查询mrp需要计算的数据
        List<PmsMrpInfo> infoList = Lists.newArrayList();
        List<PmsMrpInfo> pmsMrpInfoList = list(new LambdaQueryWrapper<PmsMrpInfo>().in(PmsMrpInfo::getMaterialId, list));
        pmsMrpInfoList.forEach(pmsMrpInfo -> {
            // 库存小于0，则创建计划订单数据
            if (pmsMrpInfo.getAvailableQuantity().compareTo(BigDecimal.ZERO) < 0) {
                infoList.add(create(PmManagerConstant.MRP_PLANNED_ORDER, pmsMrpInfo.getAvailableQuantity(),
                        BigDecimal.ZERO, pmsMrpInfo.getMaterialId(), LocalDate.now()));
            }
        });
        return saveBatch(infoList);
    }

    /**
     * 创建需求计划数据
     *
     * @param type
     * @param quantity
     * @param availableQuantity
     * @param materialId
     * @param mrpDate
     * @return
     */
    PmsMrpInfo create(String type, BigDecimal quantity, BigDecimal availableQuantity, Long materialId, LocalDate mrpDate) {
        SysDictVO safetyType = FeignOptional.of(remoteDictFeignApi.getByCode(type, PmManagerConstant.MRP_TYPE)).get();
        PmsMrpInfo planOrder = new PmsMrpInfo();
        planOrder.setMrpTypeId(safetyType.getId());
        planOrder.setQuantity(quantity);
        planOrder.setMaterialId(materialId);
        planOrder.setAvailableQuantity(availableQuantity);
        planOrder.setMrpDate(mrpDate);
        return planOrder;
    }
}




