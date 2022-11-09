package com.steccn.pms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.common.util.LocalDateUtil;
import com.steccn.platform.feign.RemoteDictFeignApi;
import com.steccn.platform.vo.SysDictVO;
import com.steccn.pms.constant.PmManagerConstant;
import com.steccn.pms.domain.*;
import com.steccn.pms.dto.*;
import com.steccn.pms.feign.RemoteMessFeignApi;
import com.steccn.pms.mapper.PmsProducePlanMapper;
import com.steccn.pms.service.PmsMrpInfoService;
import com.steccn.pms.mapper.PmsMrpInfoMapper;
import com.steccn.pms.service.PmsProducePlanService;
import com.steccn.pms.vo.PmsMrpInfoVO;
import com.steccn.pms.vo.PmsWorkOrderBatchVO;
import lombok.RequiredArgsConstructor;
import org.apache.commons.compress.utils.Lists;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
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
    private final PmsProducePlanService pmsProducePlanService;

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
                    pmsMrpInfo.setStatus(PmManagerConstant.OrderStateEnum.ZERO.getCode());
                    // 库存小于0，则创建计划订单数据
                    if (availableQuantity.compareTo(BigDecimal.ZERO) < 0) {
                        pmsMrpInfos.add(create(PmManagerConstant.MRP_PLANNED_ORDER, pmsMrpInfo.getAvailableQuantity().negate(),
                                BigDecimal.ZERO, pmsMrpInfo.getMaterialId(), pmsMrpInfo.getMrpDate(),
                                pmsMrpInfo.getPlanId(), PmManagerConstant.OrderStateEnum.ZERO.getCode()));
//                        pmsMrpInfo.setAvailableQuantity(BigDecimal.ZERO);
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


    @Override
    public void turnProduceOrder(PmsProduceOrderDTO model) {
        //获取Mrp的计划订单数据
        PmsMrpInfo planInfo = getById(model.getMrpId());
        Assert.notNull(planInfo, "计划订单不存在");
        //创建生产计划数据
        PmsMrpInfo produceInfo = create(PmManagerConstant.MRP_MANUFACTURE_REQUIRMENT, model.getQuantity(),
                planInfo.getAvailableQuantity(), model.getMaterialId(), planInfo.getMrpDate()
                , model.getMrpId(), PmManagerConstant.OrderStateEnum.ZERO.getCode());
        //判断当前的计划订单需求数量是否大于生产订单
        if (planInfo.getQuantity().compareTo(produceInfo.getQuantity()) > 0) {
            planInfo.setQuantity(planInfo.getQuantity().subtract(planInfo.getQuantity()));
        }
        if (planInfo.getQuantity().compareTo(produceInfo.getQuantity()) < 0) {
//            produceInfo.setAvailableQuantity(produceInfo.getQuantity().subtract(planInfo.getQuantity()));
            //对当日之后的计划订单数据重新进行mrp计算
            //获取当前日期的下一天
            LocalDate date = produceInfo.getMrpDate().plusDays(1);
            //获取当前月的最后一天
            LocalDate with = produceInfo.getMrpDate().with(TemporalAdjusters.lastDayOfMonth());
            //获取当日以后的所有mrp数据(计划订单，独立需求，生产订单)
            List<PmsMrpInfo> list = list(new LambdaQueryWrapper<PmsMrpInfo>()
                    .eq(PmsMrpInfo::getMaterialId, model.getMaterialId())
                    .between(PmsMrpInfo::getMrpDate, date, with)
                    .orderByAsc(PmsMrpInfo::getMrpDate));
            BigDecimal availableQuantity = produceInfo.getQuantity().subtract(planInfo.getQuantity());
            Set<String> codeSet = Sets.newHashSet();
            codeSet.add(PmManagerConstant.MRP_PLANNED_ORDER);
            codeSet.add(PmManagerConstant.MRP_MANUFACTURE_REQUIRMENT);
            codeSet.add(PmManagerConstant.MRP_PURCHASE_REQUEST);
            Set<Long> ids = Sets.newHashSet();
            Map<String, SysDictVO> typeMap = FeignOptional.of(remoteDictFeignApi.listByCodes(codeSet)).get();
            ids.add(typeMap.get(PmManagerConstant.MRP_PLANNED_ORDER).getId());
            ids.add(typeMap.get(PmManagerConstant.MRP_MANUFACTURE_REQUIRMENT).getId());
            ids.add(typeMap.get(PmManagerConstant.MRP_PURCHASE_REQUEST).getId());
            for (PmsMrpInfo pmsMrpInfo : list) {
                availableQuantity = availableQuantity.subtract(pmsMrpInfo.getQuantity());
                pmsMrpInfo.setStatus(PmManagerConstant.OrderStateEnum.ZERO.getCode());
                // 库存小于0，则创建计划订单数据
                if (availableQuantity.compareTo(BigDecimal.ZERO) < 0) {
                    //获取当天的计划订单以及生产订单以及采购申请的数量 todo:采购申请的数据暂无
                    List<PmsMrpInfo> supplyInfo = list(new LambdaQueryWrapper<PmsMrpInfo>().eq(PmsMrpInfo::getMaterialId, pmsMrpInfo.getMaterialId())
                            .eq(PmsMrpInfo::getMrpDate, pmsMrpInfo.getMrpDate())
                            .in(PmsMrpInfo::getMrpTypeId, ids));
                    for (PmsMrpInfo mrpInfo : supplyInfo) {
//                        mrpInfo
                    }
//                    pmsMrpInfos.add(create(PmManagerConstant.MRP_PLANNED_ORDER, pmsMrpInfo.getAvailableQuantity().negate(),
//                            BigDecimal.ZERO, pmsMrpInfo.getMaterialId(), pmsMrpInfo.getMrpDate(),
//                            pmsMrpInfo.getPlanId(), PmManagerConstant.OrderStateEnum.ZERO.getCode()));
                    pmsMrpInfo.setAvailableQuantity(BigDecimal.ZERO);
                    availableQuantity = BigDecimal.ZERO;
                }
            }
        }
        if (planInfo.getQuantity().compareTo(produceInfo.getQuantity()) == 0) {
            removeById(planInfo.getId());
        }

    }


    /**
     * Mrp计算
     *
     * @param dto
     * @return
     */
    Boolean mrpCalculate(PmsProducePlanOrderDTO dto) {
        //1.获取物料计划数据
        List<PmsProducePlan> list = pmsProducePlanService.list(new LambdaQueryWrapper<PmsProducePlan>()
                .in(PmsProducePlan::getMaterialId, dto.getMaterialIds())
                .between(PmsProducePlan::getMaterialId, dto.getFirstDay(), dto.getLastDay()));
        Map<Long, List<PmsProducePlan>> collect = list.stream().collect(Collectors.groupingBy(PmsProducePlan::getMaterialId));
        Set<String> codes = Sets.newHashSet();
        codes.add(PmManagerConstant.MRP_INDEPENDENT_DEMAND);
        codes.add(PmManagerConstant.MRP_PLANNED_ORDER);
        Map<String, SysDictVO> dictMap = FeignOptional.of(remoteDictFeignApi.listByCodes(codes)).get();
        Set<Long> ids = Sets.newHashSet();
        Map<Long, MesMaterialInventoryDTO> inventoryDTOMap = remoteMessFeignApi.materialInventory(Sets.newHashSet(dto.getMaterialIds()));
        //获取独立需求类型id
        Long mrpDemandId = dictMap.get(PmManagerConstant.MRP_INDEPENDENT_DEMAND).getId();
        //获取计划订单类型id
        Long planOrderId = dictMap.get(PmManagerConstant.MRP_PLANNED_ORDER).getId();
        dictMap.forEach((key, value) -> ids.add(value.getId()));
        collect.forEach((key, value) -> {
            List<PmsMrpInfo> infoList = Lists.newArrayList();
            //1.1 清除物料的独立需求计划以及计划订单的mrp数据
            remove(new LambdaQueryWrapper<PmsMrpInfo>().eq(PmsMrpInfo::getMaterialId, key).in(PmsMrpInfo::getMrpTypeId, ids));
            //1.2 获取物料已有的mrp计划
            List<PmsMrpInfo> mrpInfoList = list(new LambdaQueryWrapper<PmsMrpInfo>().eq(PmsMrpInfo::getMaterialId, key));
            //1.3 获取物料的库存可用信息
            BigDecimal availableQuantity = inventoryDTOMap.get(key).getAvailableQuantity();
            //1.4 对物料计划数据生成 mrp需求数据
            for (PmsProducePlan pmsProducePlan : value) {
                infoList.add(create(PmManagerConstant.MRP_INDEPENDENT_DEMAND, pmsProducePlan.getQuantity(), BigDecimal.ZERO, pmsProducePlan.getMaterialId(),
                        pmsProducePlan.getPlanDate(), pmsProducePlan.getId(), PmManagerConstant.OrderStateEnum.ZERO.getCode()));
            }
            //1.5 合并物料的mrp计划数据进行mrp计算
            infoList.addAll(mrpInfoList);
            List<PmsMrpInfo> sortedList = infoList.stream().sorted(Comparator.comparing(PmsMrpInfo::getMrpDate)).collect(Collectors.toList());
            Map<LocalDate, List<PmsMrpInfo>> dateMap = sortedList.stream().collect(Collectors.groupingBy(PmsMrpInfo::getMrpDate));
            //1.6 进行mrp计算并生成计划订单
            for (Map.Entry<LocalDate, List<PmsMrpInfo>> entry : dateMap.entrySet()) {
                BigDecimal demandQuantity = BigDecimal.ZERO;
                BigDecimal supplyQuantity = BigDecimal.ZERO;
                for (PmsMrpInfo pmsMrpInfo : entry.getValue()) {
                    if (pmsMrpInfo.getMrpTypeId().equals(mrpDemandId)) {
                        demandQuantity = pmsMrpInfo.getQuantity();
                    } else {
                        supplyQuantity = supplyQuantity.add(pmsMrpInfo.getQuantity());
                    }
                }
                //如果供给小于需求，则考虑库存
                if (supplyQuantity.compareTo(demandQuantity) < 0) {
                    //如果库存小于需求减去供给,则创建计划订单
                    if (availableQuantity.compareTo(demandQuantity.subtract(supplyQuantity)) < 0) {
                        // 计划订单数量 库存+供给-需求
                        BigDecimal negate = availableQuantity.add(supplyQuantity).subtract(demandQuantity).negate();
                        infoList.add(create(PmManagerConstant.MRP_PLANNED_ORDER, negate, BigDecimal.ZERO,
                                key, entry.getKey(), null, PmManagerConstant.OrderStateEnum.ZERO.getCode()));
                    }
                } else {
                    availableQuantity = availableQuantity.add(supplyQuantity.subtract(demandQuantity));
                }
            }
        });
        return true;
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




