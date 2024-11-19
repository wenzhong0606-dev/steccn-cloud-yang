package com.steccn.pms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.common.constant.FeignConstant;
import com.steccn.common.constant.MpConstant;
import com.steccn.platform.feign.RemoteDictFeignApi;
import com.steccn.platform.feign.RemoteSpareFeignApi;
import com.steccn.platform.vo.EqpSpareVO;
import com.steccn.pms.domain.*;
import com.steccn.pms.dto.PmsMonthlyProductionPlanDTO;
import com.steccn.pms.dto.PmsProduceOrderCraftDTO;
import com.steccn.pms.dto.PmsProduceOrderDTO;
import com.steccn.pms.dto.PmsWorkOrderBomDTO;
import com.steccn.pms.service.*;
import com.steccn.pms.mapper.PmsMonthlyProductionPlanMapper;
import com.steccn.pms.vo.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
* @author 34872
* @description 针对表【pms_monthly_production_plan】的数据库操作Service实现
* @createDate 2024-08-26 10:51:10
*/
@Service
@RequiredArgsConstructor
@Slf4j
public class PmsMonthlyProductionPlanServiceImpl extends ServiceImpl<PmsMonthlyProductionPlanMapper, PmsMonthlyProductionPlan>
    implements PmsMonthlyProductionPlanService{
    private final RemoteSpareFeignApi remoteSpareFeignApi;

    private final PmsMonthlyProductionPlanCraftService pmsMonthlyProductionPlanCraftService;

    private final PmsMonthlyProductionPlanBomService pmsMonthlyProductionPlanBomService;

    private final RemoteDictFeignApi remoteDictFeignApi;

    private final PmProcessService processService;

    private final PmsProduceOrderService pmsProduceOrderService;

    @Resource
    private PmsMonthlyProductionPlanNewService pmsMonthlyProductionPlanNewService;

    @Override
    public Boolean add(PmsMonthlyProductionPlanDTO dto) {
        PmsMonthlyProductionPlan pmsMonthlyProductionPlan = BeanUtil.copyProperties(dto, PmsMonthlyProductionPlan.class);
        pmsMonthlyProductionPlan.setState(0);
        save(pmsMonthlyProductionPlan);
        if (CollUtil.isNotEmpty(dto.getStationList())) {
            pmsMonthlyProductionPlanCraftService.saveCrafts(dto.getStationList(), pmsMonthlyProductionPlan.getId());
        }
        if (CollUtil.isNotEmpty(dto.getMaterialList())) {
            pmsMonthlyProductionPlanBomService.saveBom(dto.getMaterialList(), pmsMonthlyProductionPlan.getId());
        }
        //关联转月度计划New
        try {
            if (MpConstant.YES.equals(dto.getState())) {
                pmsMonthlyProductionPlanNewService.release(pmsMonthlyProductionPlan.getId(), dto.getMonthly(), dto.getClientId(), dto.getMaterialId());
                //从月度计划过来的直接下达
                dto.setId(pmsMonthlyProductionPlan.getId());
                this.order(dto);
            }
        } catch (RuntimeException e) {
            log.error("关联转月度计划New Error -> {}", e.getMessage());
        }
        return Boolean.TRUE;
    }

    @Override
    public Boolean edit(PmsMonthlyProductionPlanDTO dto) {
        PmsMonthlyProductionPlan pmsMonthlyProductionPlan = BeanUtil.copyProperties(dto, PmsMonthlyProductionPlan.class);
        // 获取工序信息
        if (CollUtil.isNotEmpty(dto.getStationList())) {
            Set<Long> ids = Sets.newHashSet();
            dto.getStationList().forEach(pmsMonthlyProductionPlanCraftDTO -> {
                PmsMonthlyProductionPlanCraft craft = new PmsMonthlyProductionPlanCraft();
                BeanUtil.copyProperties(pmsMonthlyProductionPlanCraftDTO, craft);
                craft.setMonthlyProductionPlanId(dto.getId());
                if (Objects.nonNull(craft.getId())) {
                    //修改工序信息
                    ids.add(craft.getId());
                    pmsMonthlyProductionPlanCraftService.updateById(craft);
                } else {
                    //新增工序信息
                    craft.setMonthlyProductionPlanId(dto.getId());
                    pmsMonthlyProductionPlanCraftService.save(craft);

                }
            });
            //删除工序信息
            pmsMonthlyProductionPlanCraftService.deleteCrafts(ids, dto.getId());
        }
        //获取物料信息
        List<PmsMonthlyProductionPlanBom> bomList = Lists.newArrayList();
        if (CollUtil.isNotEmpty(dto.getMaterialList())) {
            Set<Long> idSet = Sets.newHashSet();
            dto.getMaterialList().forEach(material -> {
                PmsMonthlyProductionPlanBom pmsMonthlyProductionPlanBom = new PmsMonthlyProductionPlanBom();
                BeanUtil.copyProperties(material, pmsMonthlyProductionPlanBom);
                pmsMonthlyProductionPlanBom.setMonthlyProductionPlanId(dto.getId());
                if (Objects.nonNull(material.getId())) {
                    idSet.add(material.getId());
                    pmsMonthlyProductionPlanBomService.updateById(pmsMonthlyProductionPlanBom);
                } else {
                    bomList.add(pmsMonthlyProductionPlanBom);
                }
            });
            //删除物料信息
            pmsMonthlyProductionPlanBomService.deleteBom(idSet, dto.getId());
            pmsMonthlyProductionPlanBomService.saveBatch(bomList);
        } else {
            pmsMonthlyProductionPlanBomService.remove(new LambdaQueryWrapper<PmsMonthlyProductionPlanBom>().eq(PmsMonthlyProductionPlanBom::getMonthlyProductionPlanId, dto.getId()));
        }
        return updateById(pmsMonthlyProductionPlan);
    }

    @Override
    public Boolean editPlanQuantity(Long id, BigDecimal newQuantity) {
        Assert.notNull(id, "主键不能为空");
        Assert.notNull(newQuantity, "更新后的计划数量不能为空");
        PmsMonthlyProductionPlan plan = new PmsMonthlyProductionPlan();
        plan.setId(id);
        plan.setPlannedQuantity(newQuantity);
        return updateById(plan);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean order(PmsMonthlyProductionPlanDTO dto) {
        PmsMonthlyProductionPlan plan = getById(dto.getId());
        Assert.isTrue(0 == plan.getState() || 2 == plan.getState(),"当前计划状态不可下达");
        plan.setState(1);
        return  updateById(plan);
    }

    @Override
    public Boolean stop(Long id) {
        PmsMonthlyProductionPlan plan = getById(id);
        Assert.isTrue(1 == plan.getState() ,"当前计划状态不可下达");
        plan.setState(2);
        return  updateById(plan);
    }

    @Override
    public Boolean complete(Long id) {
        PmsMonthlyProductionPlan plan = getById(id);
        plan.setState(3);
        return  updateById(plan);
    }

    @Override
    public Page<PmsMonthlyProductionPlanVO> pageList(PmsMonthlyProductionPlanDTO dto) {
        // 获取原始的分页数据
        Page<PmsMonthlyProductionPlan> originalPage = this.page(new Page<>(dto.getCurrent(), dto.getSize()),
                new LambdaQueryWrapper<PmsMonthlyProductionPlan>()
                        .eq(Objects.nonNull(dto.getMonthly()),PmsMonthlyProductionPlan::getMonthly, dto.getMonthly())
                        .eq(Objects.nonNull(dto.getState()),PmsMonthlyProductionPlan::getState,dto.getState())
        );

        // 处理数据
        List<Long> materialIds = originalPage.getRecords().stream()
                .map(PmsMonthlyProductionPlan::getMaterialId)
                .collect(Collectors.toList());

        Map<Long, EqpSpareVO> materialMap = FeignOptional.of(() -> remoteSpareFeignApi.listById(materialIds), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();

        Page page = new Page<>(dto.getCurrent(), dto.getSize());
        //4.拷贝page信息
        BeanUtil.copyProperties(originalPage, page);
        //5.拷贝返回的信息
        List<PmsMonthlyProductionPlanVO> res = BeanUtil.copyToList(originalPage.getRecords(), PmsMonthlyProductionPlanVO.class);

        if(CollUtil.isNotEmpty(res)) {
            Set<Long> ids = Sets.newHashSet();
            res.forEach( e-> {
                ids.add(e.getUnitId());
                ids.add(e.getMinimumPackingQuantityUnit());
            });
            Map<Long, String> dictMap = FeignOptional.of(() -> remoteDictFeignApi.listByIdCh(ids), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            res.forEach(originalRecord -> {
                EqpSpareVO eqpSpareVO = materialMap.get(originalRecord.getMaterialId());
                originalRecord.setUnitName(dictMap.get(originalRecord.getUnitId()));
                originalRecord.setMinimumPackingQuantityUnitName(dictMap.get(originalRecord.getMinimumPackingQuantityUnit()));
                if (eqpSpareVO != null) {
                    originalRecord.setMaterialCode(eqpSpareVO.getCode());
                    originalRecord.setMaterialName(eqpSpareVO.getName());
                    originalRecord.setSpecification(eqpSpareVO.getSpecifications());
                    originalRecord.setThickQuantity(eqpSpareVO.getThickQuantity());
                    originalRecord.setThickUnitName(eqpSpareVO.getThickUnitName());
                }
            });
        }
        return page.setRecords(res);
    }

    @Override
    public PmsMonthlyProductionPlanVO info(Long id) {
        PmsMonthlyProductionPlan plan = getById(id);
        EqpSpareVO eqpSpareVO = FeignOptional.of(() -> remoteSpareFeignApi.getById(plan.getMaterialId()), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        PmsMonthlyProductionPlanVO pmsMonthlyProductionPlanVO = BeanUtil.copyProperties(plan, PmsMonthlyProductionPlanVO.class);
        pmsMonthlyProductionPlanVO.setMaterialName(eqpSpareVO.getName());
        pmsMonthlyProductionPlanVO.setMaterialCode(eqpSpareVO.getCode());
        pmsMonthlyProductionPlanVO.setSpecification(eqpSpareVO.getSpecifications());
        pmsMonthlyProductionPlanVO.setThickQuantity(eqpSpareVO.getThickQuantity());
        pmsMonthlyProductionPlanVO.setThickUnitName(eqpSpareVO.getThickUnitName());
        Set<Long> unitIds = Sets.newHashSet();
        unitIds.add(pmsMonthlyProductionPlanVO.getUnitId());
        unitIds.add(pmsMonthlyProductionPlanVO.getMinimumPackingQuantityUnit());
        Map<Long, String> dictUnitMap = FeignOptional.of(()->remoteDictFeignApi.listByIdCh(unitIds),FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        pmsMonthlyProductionPlanVO.setUnitName(dictUnitMap.get(pmsMonthlyProductionPlanVO.getUnitId()));
        pmsMonthlyProductionPlanVO.setMinimumPackingQuantityUnitName(dictUnitMap.get(pmsMonthlyProductionPlanVO.getMinimumPackingQuantityUnit()));
        //获取工艺信息
        List<PmsMonthlyProductionPlanCraft> list = pmsMonthlyProductionPlanCraftService.list(new LambdaQueryWrapper<PmsMonthlyProductionPlanCraft>()
                .eq(PmsMonthlyProductionPlanCraft::getMonthlyProductionPlanId, plan.getId()));
        List<PmsMonthlyProductionPlanCraftVO> pmsMonthlyProductionPlanCraftVOS = BeanUtil.copyToList(list, PmsMonthlyProductionPlanCraftVO.class);
        //获取工艺信息
        if (CollUtil.isNotEmpty(pmsMonthlyProductionPlanCraftVOS)) {
            Set<Long> crafts = Sets.newHashSet();
            //处理单位数据
            Set<Long> dictIds = Sets.newHashSet();
            pmsMonthlyProductionPlanCraftVOS.forEach(
                    craft -> {
                        crafts.add(craft.getId());
                        dictIds.add(craft.getTypeId());
                        dictIds.add(craft.getUnitId());
                    }

            );
            //获取字典数据
            Map<Long, String> dictMap = FeignOptional.of(() -> remoteDictFeignApi.listByIdCh(dictIds), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            pmsMonthlyProductionPlanCraftVOS.forEach(
                    craft -> {
                        craft.setUnit(dictMap.get(craft.getUnitId()));
                        craft.setType(dictMap.get(craft.getTypeId()));
                    }
            );
        }
        pmsMonthlyProductionPlanVO.setStationList(pmsMonthlyProductionPlanCraftVOS);
        List<PmsMonthlyProductionPlanBom> bomList = pmsMonthlyProductionPlanBomService.list(new LambdaQueryWrapper<PmsMonthlyProductionPlanBom>()
                .eq(PmsMonthlyProductionPlanBom::getMonthlyProductionPlanId, plan.getId()));
        List<PmsMonthlyProductionPlanBomVO> pmsMonthlyProductionPlanBomVOS = BeanUtil.copyToList(bomList, PmsMonthlyProductionPlanBomVO.class);
        if (CollUtil.isNotEmpty(pmsMonthlyProductionPlanBomVOS)) {
            Set<Long> ids = Sets.newHashSet();
            List<Long> materialIds = Lists.newArrayList();
            List<Long> processIds = Lists.newArrayList();
            pmsMonthlyProductionPlanBomVOS.forEach( material->{
                ids.add(material.getUnitId());
                materialIds.add(material.getModuleId());
                processIds.add(material.getProcessId());
            });
            //3.1.获取字典数据
            Map<Long, String> dictMap = FeignOptional.of(()->remoteDictFeignApi.listByIdCh(ids),FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            //3.2 获取物料数据
            Map<Long, EqpSpareVO> spareVOMap = FeignOptional.of(()->remoteSpareFeignApi.listById(materialIds),FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            EqpSpareVO spareVO = new EqpSpareVO();
            PmProcessVO pmProcessVO = new PmProcessVO();
            //3.3 获取工序数据
            Map<Long, PmProcessVO> processVOMap = processService.listById(processIds);
            pmsMonthlyProductionPlanBomVOS.forEach(material->{
                //处理物料数据
                EqpSpareVO spare = spareVOMap.getOrDefault(material.getModuleId(), spareVO);
                material.setName(spare.getName());
                material.setCode(spare.getCode());
                //处理工序数据
                PmProcessVO processVO = processVOMap.getOrDefault(material.getProcessId(), pmProcessVO);
                material.setProcessName(processVO.getName());
                material.setUnit(dictMap.get(material.getUnitId()));
            });
        }
        pmsMonthlyProductionPlanVO.setMaterialList(pmsMonthlyProductionPlanBomVOS);
        return pmsMonthlyProductionPlanVO;
    }

    @Override
    public Boolean createOrder(PmsMonthlyProductionPlanDTO dto) {
        Assert.notNull(dto.getId(),"id不能为空");
        PmsMonthlyProductionPlan plan = getById(dto.getId());
        plan.setTransferredQuantity(Objects.nonNull(plan.getTransferredQuantity())?plan.getTransferredQuantity().add(dto.getQuantity()): dto.getQuantity());
        updateById(plan);
        PmsProduceOrderDTO pmsProduceOrderDTO = new PmsProduceOrderDTO();
        pmsProduceOrderDTO.setState(0);
        pmsProduceOrderDTO.setTypeId(dto.getTypeId());
        pmsProduceOrderDTO.setPlanStartTime(dto.getPlanStartTime());
        pmsProduceOrderDTO.setPlanEndTime(dto.getPlanEndTime());
        pmsProduceOrderDTO.setQuantity(dto.getQuantity());
        pmsProduceOrderDTO.setCustomerId(dto.getClientId());
        pmsProduceOrderDTO.setMaterialId(dto.getMaterialId());
        pmsProduceOrderDTO.setMaterialName(dto.getMaterialName());
        pmsProduceOrderDTO.setMaterialCode(dto.getMaterialCode());
        pmsProduceOrderDTO.setUnitId(plan.getUnitId());
        pmsProduceOrderDTO.setProductionLineId(dto.getProductionLineId());
        pmsProduceOrderDTO.setMinPackageQuantity(plan.getMinimumPackingQuantity());
        pmsProduceOrderDTO.setMinPackageUnitId(plan.getMinimumPackingQuantityUnit());
        pmsProduceOrderDTO.setCraftId(dto.getCraftId());
        pmsProduceOrderDTO.setPlanId(dto.getId());
        pmsProduceOrderDTO.setMaterialList(dto.getBomList());
        pmsProduceOrderDTO.setProcessList(dto.getProcessList());
        return  pmsProduceOrderService.add(pmsProduceOrderDTO);
    }

}




