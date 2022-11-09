package com.steccn.pms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.platform.feign.RemoteDictFeignApi;
import com.steccn.platform.vo.SysDictVO;
import com.steccn.pms.constant.PmManagerConstant;
import com.steccn.pms.domain.PmsMrp;
import com.steccn.pms.domain.PmsMrpInfo;
import com.steccn.pms.domain.PmsProducePlan;
import com.steccn.pms.domain.PmsWorkOrderBatch;
import com.steccn.pms.dto.*;
import com.steccn.pms.feign.RemoteMessFeignApi;
import com.steccn.pms.service.PmsMrpInfoService;
import com.steccn.pms.mapper.PmsMrpInfoMapper;
import com.steccn.pms.vo.PmsMrpInfoVO;
import com.steccn.pms.vo.PmsWorkOrderBatchVO;
import lombok.RequiredArgsConstructor;
import org.apache.commons.compress.utils.Lists;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

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

//    @Override
//    public Boolean add(PmsProducePlanDTO dto) {
//        //1.处理数据
//        Set<Long> materialIds = Sets.newHashSet();
//        materialIds.add(dto.getMaterialId());
//        Map<Long, MesMaterialInventoryDTO> materialInventoryMap = remoteMessFeignApi.materialInventory(materialIds);
//        MesMaterialInventoryDTO inventoryDTO = materialInventoryMap.get(dto.getMaterialId());
//        BigDecimal availableQuantity = inventoryDTO.getAvailableQuantity();
//        List<PmsMrpInfo> list = Lists.newArrayList();
//        //根据物料id 获取plan 数据
//        if (CollUtil.isNotEmpty(dto.getProducePlanDetailsDTOList())) {
//            SysDictVO sysDictVO = FeignOptional.of(remoteDictFeignApi.getByCode(PmManagerConstant.MRP_INDEPENDENT_DEMAND, PmManagerConstant.MRP_TYPE)).get();
//            //清除已有的独立需求，重新生成独立需求计划
//            remove(new LambdaQueryWrapper<PmsMrpInfo>()
//                    .eq(PmsMrpInfo::getMaterialId, dto.getMaterialId())
//                    .eq(PmsMrpInfo::getMrpTypeId, sysDictVO.getId()));
//            List<PmsProducePlanDetailsDTO> producePlanDetailsDTOList = dto.getProducePlanDetailsDTOList();
//            for (PmsProducePlanDetailsDTO pmsProducePlanDetailsDTO : producePlanDetailsDTOList) {
//                //1.1新增独立需求计划 todo 暂不计算安全库存
//                availableQuantity = availableQuantity.subtract(pmsProducePlanDetailsDTO.getQuantity());
//                list.add(create(PmManagerConstant.MRP_INDEPENDENT_DEMAND, pmsProducePlanDetailsDTO.getQuantity(),
//                        BigDecimal.ZERO, dto.getMaterialId(), pmsProducePlanDetailsDTO.getPlanDate()));
//            }
//        }
//        //2.保存数据
//        return saveBatch(list);
//    }

    @Override
    public Boolean mrpAdd(PmsProducePlanOrderDTO dto) {
        //自定义sql查询mrp计算的数据
        List<PmsMrpInfo> pmsMrpInfoList = this.baseMapper.list(dto);
        List<PmsMrpInfo> pmsMrpInfos = Lists.newArrayList();
        //处理数据
        if (CollUtil.isNotEmpty(pmsMrpInfoList)) {
            Map<Long, List<PmsMrpInfo>> collect = pmsMrpInfoList.stream().collect(Collectors.groupingBy(PmsMrpInfo::getMaterialId));
            //根据物料id 获取库存信息
            Map<Long, MesMaterialInventoryDTO> materialInventory = remoteMessFeignApi.materialInventory(Sets.newHashSet(dto.getMaterialIds()));
            MesMaterialInventoryDTO inventoryDTO = new MesMaterialInventoryDTO();
            collect.forEach((key, list) -> {
                list.sort(Comparator.comparing(PmsMrpInfo::getMrpDate));
                //修改mrp数据的库存信息
                BigDecimal availableQuantity = materialInventory.getOrDefault(key, inventoryDTO).getAvailableQuantity();
                for (PmsMrpInfo pmsMrpInfo : list) {
                    availableQuantity = availableQuantity.subtract(pmsMrpInfo.getQuantity());
                    pmsMrpInfo.setAvailableQuantity(availableQuantity);
                    pmsMrpInfo.setStatus(PmManagerConstant.OrderStateEnum.ZERO.getCode());
                    // 库存小于0，则创建计划订单数据
                    if (pmsMrpInfo.getAvailableQuantity().compareTo(BigDecimal.ZERO) < 0) {
                        pmsMrpInfos.add(create(PmManagerConstant.MRP_PLANNED_ORDER, pmsMrpInfo.getAvailableQuantity().negate(),
                                BigDecimal.ZERO, pmsMrpInfo.getMaterialId(), pmsMrpInfo.getMrpDate(),
                                pmsMrpInfo.getPlanId(), PmManagerConstant.OrderStateEnum.ZERO.getCode()));
                        pmsMrpInfo.setAvailableQuantity(BigDecimal.ZERO);
                        availableQuantity = BigDecimal.ZERO;
                    }
                }
            });
            collect.forEach((key, list) -> {
                pmsMrpInfos.addAll(list);
            });
        }
        return saveOrUpdateBatch(pmsMrpInfos);
    }

    @Override
    public Boolean saveMrp(List<PmsProducePlan> producePlanList) {
        List<PmsMrpInfo> list = Lists.newArrayList();
        if (CollUtil.isNotEmpty(producePlanList)) {
            List<Long> planIds = Lists.newArrayList();
            producePlanList.forEach(pmsProducePlan -> {
                //新增独立需求计划 todo 暂不计算安全库存
                list.add(create(PmManagerConstant.MRP_INDEPENDENT_DEMAND, pmsProducePlan.getQuantity(),
                        BigDecimal.ZERO, pmsProducePlan.getMaterialId(), pmsProducePlan.getPlanDate(), pmsProducePlan.getId(), PmManagerConstant.OrderStateEnum.ONE.getCode()));
                planIds.add(pmsProducePlan.getId());
            });
            //清除已生成的独立需求计划
            remove(new LambdaQueryWrapper<PmsMrpInfo>().in(PmsMrpInfo::getPlanId, planIds));
        }
        //处理数据
        return saveBatch(list);
    }

    @Override
    public Page<PmsMrpInfoVO> infoPage(PmsMrpInfoPageDTO dto) {
        //1.获取库存信息以及安全库存信息
        Set<Long> materialIds = Sets.newHashSet();
        materialIds.add(dto.getId());
        Map<Long, MesMaterialInventoryDTO> materialInventory = remoteMessFeignApi.materialInventory(materialIds);
        MesMaterialInventoryDTO orDefault = materialInventory.getOrDefault(dto.getId(), new MesMaterialInventoryDTO());
        //2.创建安全库存信息
        PmsMrpInfo pmsMrpInfo = create(PmManagerConstant.MRP_INVENTORY, null, orDefault.getAvailableQuantity(), dto.getId(),
                LocalDate.now(), null, PmManagerConstant.OrderStateEnum.ZERO.getCode());
        PmsMrpInfoVO infoVO = BeanUtil.copyProperties(pmsMrpInfo, PmsMrpInfoVO.class);
        //3.处理安全库存以及分页逻辑问题
        if (PmManagerConstant.OrderStateEnum.ONE.getCode().equals(dto.getCurrent())) {
            //需考虑安全库存动态配置 todo 获取安全库存 没有则不显示
            dto.setSize(dto.getSize() - PmManagerConstant.OrderStateEnum.ONE.getCode());
        }
        //4.自定义查询条件获取分页数据
        LambdaQueryWrapper<PmsMrpInfo> eq = new LambdaQueryWrapper<PmsMrpInfo>().eq(Objects.nonNull(dto.getId()), PmsMrpInfo::getMaterialId, dto.getId())
                .eq(PmsMrpInfo::getStatus, PmManagerConstant.OrderStateEnum.ZERO.getCode())
                .orderByAsc(PmsMrpInfo::getMrpDate);
        //5.定义分页对象
        Page page = new Page<>(dto.getCurrent(), dto.getSize());
        //6.查询分页数据
        IPage resPage = page(page, eq);
        //7.拷贝page信息
        BeanUtil.copyProperties(resPage, page);
        //8.拷贝返回的信息
        List<PmsMrpInfoVO> res = BeanUtil.copyToList(resPage.getRecords(), PmsMrpInfoVO.class);
        //9.处理数据
        Set<Long> typeIds = Sets.newHashSet();
        typeIds.add(infoVO.getMrpTypeId());
        res.forEach(pmsMrpInfoVO -> {
            typeIds.add(pmsMrpInfoVO.getMrpTypeId());
        });
        //获取字典数据
        Map<Long, String> dictMap = FeignOptional.of(remoteDictFeignApi.listByIdCh(typeIds)).get();
        res.forEach(pmsMrpInfoVO -> {
            pmsMrpInfoVO.setMrpTypeName(dictMap.get(pmsMrpInfoVO.getMrpTypeId()));
        });
        infoVO.setMrpTypeName(dictMap.get(infoVO.getMrpTypeId()));
        res.add(PmManagerConstant.OrderStateEnum.ZERO.getCode(), infoVO);
        page.setSize(res.size());
        page.setTotal(page.getTotal() + PmManagerConstant.OrderStateEnum.ONE.getCode());
        return page.setRecords(res);
    }

    /**
     * 创建需求计划数据
     *
     * @param type
     * @param quantity
     * @param availableQuantity
     * @param materialId
     * @param mrpDate
     * @param planId
     * @param status
     * @return
     */
    PmsMrpInfo create(String type, BigDecimal quantity, BigDecimal availableQuantity, Long materialId, LocalDate mrpDate, Long planId, Integer status) {
        SysDictVO safetyType = FeignOptional.of(remoteDictFeignApi.getByCode(type, PmManagerConstant.MRP_TYPE)).get();
        PmsMrpInfo planOrder = new PmsMrpInfo();
        planOrder.setMrpTypeId(safetyType.getId());
        planOrder.setQuantity(quantity);
        planOrder.setMaterialId(materialId);
        planOrder.setAvailableQuantity(availableQuantity);
        planOrder.setMrpDate(mrpDate);
        planOrder.setPlanId(planId);
        planOrder.setStatus(status);
        return planOrder;
    }
}




