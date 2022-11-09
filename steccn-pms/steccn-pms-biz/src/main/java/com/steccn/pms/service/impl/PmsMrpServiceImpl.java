package com.steccn.pms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.Assert;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.xiaoymin.knife4j.core.util.StrUtil;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.common.vo.R;
import com.steccn.platform.dto.SparePageDTO;
import com.steccn.platform.feign.RemoteDictFeignApi;
import com.steccn.platform.feign.RemoteSpareFeignApi;
import com.steccn.platform.vo.EqpSpareVO;
import com.steccn.platform.vo.SysDictVO;
import com.steccn.pms.constant.PmManagerConstant;
import com.steccn.pms.domain.PmsMrp;
import com.steccn.pms.domain.PmsMrpInfo;
import com.steccn.pms.domain.PmsProducePlan;
import com.steccn.pms.domain.PmsWorkOrderBatch;
import com.steccn.pms.dto.MesMaterialInventoryDTO;
import com.steccn.pms.dto.PmsMrpPageDTO;
import com.steccn.pms.feign.RemoteMessFeignApi;
import com.steccn.pms.service.PmsMrpInfoService;
import com.steccn.pms.service.PmsMrpService;
import com.steccn.pms.mapper.PmsMrpMapper;
import com.steccn.pms.vo.PmsMrpVO;
import com.steccn.pms.vo.PmsWorkOrderBatchVO;
import lombok.RequiredArgsConstructor;
import org.apache.commons.compress.utils.Lists;
import org.apache.commons.compress.utils.Sets;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author yequan
 * @description 针对表【pms_mrp(物料需求计划)】的数据库操作Service实现
 * @createDate 2022-11-02 10:10:08
 */
@Service
@RequiredArgsConstructor
public class PmsMrpServiceImpl extends ServiceImpl<PmsMrpMapper, PmsMrp>
        implements PmsMrpService {
    private final RemoteMessFeignApi remoteMessFeignApi;
    private final PmsMrpInfoService pmsMrpInfoService;
    private final RemoteDictFeignApi remoteDictFeignApi;
    private final RemoteSpareFeignApi remoteSpareFeignApi;

    @Override
    public Page<PmsMrpVO> pageList(PmsMrpPageDTO dto) {
        //1.定义查询条件，获取数据
        SparePageDTO sparePageDTO = new SparePageDTO();
        BeanUtil.copyProperties(dto, sparePageDTO);
        Page<EqpSpareVO> resPage = FeignOptional.of(remoteSpareFeignApi.listPage(sparePageDTO)).get();
        //2.定义分页对象
        Page page = new Page<>(dto.getCurrent(), dto.getSize());
        //3.拷贝page信息
        BeanUtil.copyProperties(resPage, page);
        //4.拷贝返回的数据
        List<EqpSpareVO> eqpSpareVOList = BeanUtil.copyToList(page.getRecords(), EqpSpareVO.class);
        //5.处理数据
        List<PmsMrpVO> pmsMrpVOList = Lists.newArrayList();
        Set<Long> materialIds = Sets.newHashSet();
        eqpSpareVOList.forEach(eqpSpareVO -> {
            PmsMrpVO pmsMrpVO = new PmsMrpVO();
            BeanUtil.copyProperties(eqpSpareVO, pmsMrpVO);
            pmsMrpVO.setMaterialId(eqpSpareVO.getId());
            pmsMrpVO.setMaterialName(eqpSpareVO.getName());
            pmsMrpVO.setMaterialCode(eqpSpareVO.getCode());
            pmsMrpVO.setTypeId(eqpSpareVO.getProcurementTypeId());
            pmsMrpVO.setTypeName(eqpSpareVO.getProcurementTypeName());
            pmsMrpVO.setUnitId(eqpSpareVO.getUnitId());
            pmsMrpVO.setUnitName(eqpSpareVO.getUnit());
            materialIds.add(eqpSpareVO.getId());
            pmsMrpVOList.add(pmsMrpVO);
        });
        //6.根据物料id获取库存数量
        Map<Long, MesMaterialInventoryDTO> materialInventory = remoteMessFeignApi.materialInventory(materialIds);
        MesMaterialInventoryDTO inventoryDTO = new MesMaterialInventoryDTO();
        //7.根据物料id 自定义sql 获取需求计划的数量
        Set<String> typeCodes = Sets.newHashSet();
        typeCodes.add(PmManagerConstant.MRP_INDEPENDENT_DEMAND);
        typeCodes.add(PmManagerConstant.MRP_MANUFACTURE_REQUIRMENT);
        Map<String, SysDictVO> typeMap = FeignOptional.of(remoteDictFeignApi.listByCodes(typeCodes)).get();
        LambdaQueryWrapper<PmsMrpInfo> pmsMrpInfoLambdaQueryWrapper = new QueryWrapper<PmsMrpInfo>().select("material_id,sum(quantity) as quantity").lambda()
                .in(PmsMrpInfo::getMaterialId, materialIds)
                .in(PmsMrpInfo::getMrpTypeId, typeMap.get(PmManagerConstant.MRP_INDEPENDENT_DEMAND).getId(), typeMap.get(PmManagerConstant.MRP_MANUFACTURE_REQUIRMENT).getId())
                .eq(PmsMrpInfo::getStatus,PmManagerConstant.OrderStateEnum.ZERO.getCode())
                .groupBy(PmsMrpInfo::getMaterialId);
        List<PmsMrpInfo> list = pmsMrpInfoService.list(pmsMrpInfoLambdaQueryWrapper);
        Map<Long, PmsMrpInfo> pmsMrpInfoMap = list.stream().collect(Collectors.toMap(PmsMrpInfo::getMaterialId, Function.identity()));
        PmsMrpInfo pmsMrpInfo = new PmsMrpInfo();
        pmsMrpVOList.forEach(pmsMrpVO -> {
            PmsMrpInfo orDefault = pmsMrpInfoMap.getOrDefault(pmsMrpVO.getMaterialId(), pmsMrpInfo);
            MesMaterialInventoryDTO inventoryOrDefault = materialInventory.getOrDefault(pmsMrpVO.getMaterialId(), inventoryDTO);
            pmsMrpVO.setQuantity(orDefault.getQuantity());
            pmsMrpVO.setStockQuantity(inventoryOrDefault.getAvailableQuantity());
        });
        page.setTotal(pmsMrpVOList.size());
        return page.setRecords(pmsMrpVOList);
    }

//    @Override
//    public Page<PmsMrpVO> pageList(PmsMrpPageDTO dto) {
//        //1.定义查询条件
//        LambdaQueryWrapper<PmsMrp> eq = new LambdaQueryWrapper<PmsMrp>().like(StrUtil.isNotBlank(dto.getName()), PmsMrp::getMaterialName, dto.getName());
//        //2.定义分页对象
//        Page page = new Page<>(dto.getCurrent(), dto.getSize());
//        //3.查询分页数据
//        IPage resPage = page(page, eq);
//        //4.拷贝page信息
//        BeanUtil.copyProperties(resPage, page);
//        //5.拷贝返回的信息
//        List<PmsMrpVO> pmsMrpVOS = BeanUtil.copyToList(page.getRecords(), PmsMrpVO.class);
//        //3.处理数据
//        List<Long> materialIds = Lists.newArrayList();
//        Set<Long> dictIds = Sets.newHashSet();
//        Set<Long> ids = Sets.newHashSet();
//        pmsMrpVOS.forEach(pmsMrpVO -> {
//            materialIds.add(pmsMrpVO.getMaterialId());
//            materialIds.add(pmsMrpVO.getTypeId());
//            ids.add(pmsMrpVO.getId());
//        });
//        //获取物料的需求数量
//        //获取物料信息
//        Map<Long, EqpSpareVO> materialMap = FeignOptional.of(remoteSpareFeignApi.listById(materialIds)).get();
//        //获取字典数据
//        Map<Long, String> dictMap = FeignOptional.of(remoteDictFeignApi.listByIdCh(dictIds)).get();
//        EqpSpareVO eqpSpareVO = new EqpSpareVO();
//        pmsMrpVOS.forEach(pmsMrpVO -> {
//            EqpSpareVO material = materialMap.getOrDefault(pmsMrpVO.getMaterialId(), eqpSpareVO);
//            pmsMrpVO.setMaterialName(material.getName());
//            pmsMrpVO.setUnitName(material.getUnit());
////            pmsMrpVO.setTypeId(material.getTypeId());
//            pmsMrpVO.setTypeName(dictMap.get(pmsMrpVO.getTypeId()));
//        });
//        return page.setRecords(pmsMrpVOS);
//    }

//    @Override
//    public Boolean add(List<PmsMrp> list) {
//        //1.处理数据
//        List<PmsMrpInfo> pmsMrpInfos = Lists.newArrayList();
//        Set<Long> materialIds = Sets.newHashSet();
//        list.forEach(pmsMrp -> {
//            materialIds.add(pmsMrp.getMaterialId());
//        });
//        //3.获取物料的可用库存数量
//        Map<Long, MesMaterialInventoryDTO> materialInventoryMap = remoteMessFeignApi.materialInventory(materialIds);
//        Map<Long, String> materialMap = FeignOptional.of(remoteSpareFeignApi.listByIdCh(materialIds)).get();
//        MesMaterialInventoryDTO mesMaterialInventoryDTO = new MesMaterialInventoryDTO();
//        //4.获取MRP类型数据
//        SysDictVO inventoryType = FeignOptional.of(remoteDictFeignApi.getByCode(PmManagerConstant.MRP_INVENTORY, PmManagerConstant.MRP_TYPE)).get();
//        SysDictVO safetyType = FeignOptional.of(remoteDictFeignApi.getByCode(PmManagerConstant.MRP_SAFETY_INVENTORY, PmManagerConstant.MRP_TYPE)).get();
//        //5.处理数据
//        //5.1保存主数据
//        list.forEach(pmsMrp -> {
//            MesMaterialInventoryDTO inventoryDTO = materialInventoryMap.getOrDefault(pmsMrp.getMaterialId(), mesMaterialInventoryDTO);
//            pmsMrp.setMaterialName(materialMap.get(pmsMrp.getMaterialId()));
//            pmsMrp.setStockQuantity(inventoryDTO.getAvailableQuantity());
//        });
//        saveBatch(list);
//        list.forEach(pmsMrp -> {
//            PmsMrpInfo mrpInventory = new PmsMrpInfo();
//            MesMaterialInventoryDTO inventoryDTO = materialInventoryMap.getOrDefault(pmsMrp.getMaterialId(), mesMaterialInventoryDTO);
//            mrpInventory.setAvailableQuantity(inventoryDTO.getAvailableQuantity());
//            mrpInventory.setMrpDate(LocalDate.now());
//            mrpInventory.setMrpId(pmsMrp.getId());
//            mrpInventory.setMrpTypeId(inventoryType.getId());
//            pmsMrpInfos.add(mrpInventory);
//            //处理安全库存
//            PmsMrpInfo mrpSafety = new PmsMrpInfo();
//            //todo 安全库存暂定为O 需计算扣除安全库存后的可用库存数量
//            mrpSafety.setQuantity(BigDecimal.ZERO);
//            mrpSafety.setAvailableQuantity(mrpInventory.getAvailableQuantity());
//            mrpSafety.setMrpDate(LocalDate.now());
//            mrpSafety.setMrpId(pmsMrp.getId());
//            mrpSafety.setMrpTypeId(safetyType.getId());
//            pmsMrpInfos.add(mrpSafety);
//            //安全库存小于0，则创建计划订单数据
//            if (mrpSafety.getAvailableQuantity().compareTo(BigDecimal.ZERO) < 0) {
//                pmsMrpInfos.add(createPlanOrder(PmManagerConstant.MRP_PLANNED_ORDER,mrpSafety.getAvailableQuantity(), BigDecimal.ZERO, pmsMrp.getId()));
//            }
//            //创建独立需求订单
//        });
//        return pmsMrpInfoService.saveBatch(pmsMrpInfos);
//    }


//    /**
//     * 创建计划订单数据
//     *
//     * @param type
//     * @param quantity
//     * @param availableQuantity
//     * @param mrpId
//     * @return
//     */
//    PmsMrpInfo createPlanOrder(String type,BigDecimal quantity, BigDecimal availableQuantity, Long mrpId) {
//        SysDictVO safetyType = FeignOptional.of(remoteDictFeignApi.getByCode(type, PmManagerConstant.MRP_TYPE)).get();
//        PmsMrpInfo planOrder = new PmsMrpInfo();
//        planOrder.setMrpTypeId(safetyType.getId());
//        planOrder.setQuantity(quantity);
//        planOrder.setMrpId(mrpId);
//        planOrder.setAvailableQuantity(availableQuantity);
//        planOrder.setMrpDate(LocalDate.now());
//        return planOrder;
//    }
}




