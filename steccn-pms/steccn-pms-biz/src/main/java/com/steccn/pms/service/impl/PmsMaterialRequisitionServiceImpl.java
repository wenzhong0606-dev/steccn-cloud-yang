package com.steccn.pms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.common.constant.MpConstant;
import com.steccn.common.util.BigDecimalUtil;
import com.steccn.common.vo.R;
import com.steccn.platform.feign.RemoteDictFeignApi;
import com.steccn.platform.feign.RemoteOfficeFeign;
import com.steccn.platform.feign.RemoteSpareFeignApi;
import com.steccn.platform.feign.RemoteStationWarehouseFeignApi;
import com.steccn.platform.util.LogicalPageUtil;
import com.steccn.platform.vo.EqpSpareVO;
import com.steccn.platform.vo.SysDictVO;
import com.steccn.platform.vo.SysOfficeVO;
import com.steccn.platform.vo.SysStationWarehouseVO;
import com.steccn.pms.constant.PmManagerConstant;
import com.steccn.pms.domain.*;
import com.steccn.pms.dto.*;
import com.steccn.pms.feign.RemoteMessFeignApi;
import com.steccn.pms.service.*;
import com.steccn.pms.mapper.PmsMaterialRequisitionMapper;
import com.steccn.pms.util.PmsCodeUtil;
import com.steccn.pms.vo.MaterialRequisitionInfoVO;
import com.steccn.pms.vo.MaterialRequisitionPlanVO;
import com.steccn.pms.vo.MaterialRequisitionVO;
import com.steccn.pms.vo.PmsProduceOrderVO;
import io.swagger.models.auth.In;
import lombok.RequiredArgsConstructor;
import org.apache.commons.compress.utils.Lists;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author yequan
 * @description 针对表【pms_material_requisition(生产领料单)】的数据库操作Service实现
 * @createDate 2022-11-02 10:10:08
 */
@Service
@RequiredArgsConstructor
public class PmsMaterialRequisitionServiceImpl extends ServiceImpl<PmsMaterialRequisitionMapper, PmsMaterialRequisition>
        implements PmsMaterialRequisitionService {

    private final PmsMaterialRequisitionInfoService pmsMaterialRequisitionInfoService;
    private final RemoteOfficeFeign remoteOfficeFeign;
    private final RemoteDictFeignApi remoteDictFeignApi;
    private final RemoteSpareFeignApi remoteSpareFeignApi;
    private final PmsProduceOrderService produceOrderService;
    private final PmsWorkOrderBomService pmsWorkOrderBomService;
    private final RemoteMessFeignApi remoteMessFeignApi;
    private final PmsCompleteDeliveryInfoService pmsCompleteDeliveryInfoService;
    private final RemoteStationWarehouseFeignApi remoteStationWarehouseFeignApi;

    @Override
    public Page<MaterialRequisitionVO> pageList(MaterialRequisitionPageDTO dto) {
        //1.处理时间
        //计划开始时间-开始
        if (Objects.nonNull(dto.getStartTime())) {
            dto.setStartCreateTime(LocalDateTime.of(dto.getStartTime(), LocalTime.MIN));
        }
        //计划开始时间-结束
        if (Objects.nonNull(dto.getEndTime())) {
            dto.setEndCreateTime(LocalDateTime.of(dto.getEndTime(), LocalTime.MAX));
        }
        //2.自定义sql获取数据
        Page<MaterialRequisitionVO> page = this.baseMapper.pageList(new Page(dto.getCurrent(), dto.getSize()), dto);
        //3.处理数据
        if (CollUtil.isNotEmpty(page.getRecords())) {
            //3.1 处理部门数据
            Set<Integer> officeIdSet = Sets.newHashSet();
            //3.2 处理字典数据
            Set<Long> dictIdSet = Sets.newHashSet();
            Set<Long> ids = Sets.newHashSet();
            page.getRecords().forEach(materialRequisitionVO -> {
                dictIdSet.add(materialRequisitionVO.getStatus());
                dictIdSet.add(materialRequisitionVO.getTypeId());
                officeIdSet.add(materialRequisitionVO.getDepartmentId());
                officeIdSet.add(materialRequisitionVO.getWarehouseId());
                ids.add(materialRequisitionVO.getId());
            });
            //3.3 获取字典以及部门数据
            Map<Long, String> dictMap = FeignOptional.of(remoteDictFeignApi.listByIdCh(dictIdSet)).get();
            Map<Integer, String> officeMap = FeignOptional.of(remoteOfficeFeign.listByIdCh(officeIdSet)).get();
            page.getRecords().forEach(materialRequisitionVO -> {
                materialRequisitionVO.setStatusName(dictMap.get(materialRequisitionVO.getStatus()));
                materialRequisitionVO.setTypeName(dictMap.get(materialRequisitionVO.getTypeId()));
                materialRequisitionVO.setDepartmentName(officeMap.get(materialRequisitionVO.getDepartmentId()));
                materialRequisitionVO.setWarehouseName(officeMap.get(materialRequisitionVO.getWarehouseId()));
            });
            //3.4获取总数量
            Map<Long, BigDecimal> quantityMap = pmsMaterialRequisitionInfoService.getTotalQuantity(ids);
            BigDecimal zero = BigDecimal.ZERO;
            page.getRecords().forEach(materialRequisitionVO -> {
                BigDecimal quantity = quantityMap.getOrDefault(materialRequisitionVO.getId(), zero);
                materialRequisitionVO.setQuantity(quantity);
            });
        }
        return page;
    }

    @Override
    public Boolean add(MaterialRequisitionDTO materialRequisitionDTO) {
        //处理数据
        PmsMaterialRequisition model = new PmsMaterialRequisition();
        BeanUtil.copyProperties(materialRequisitionDTO, model);
        model.setCode(PmsCodeUtil.getProduceOrderCode(PmsCodeUtil.PREFIX_PMS_MATERIAL_REQUISITION, PmsCodeUtil.NUM_BIT_4, currentMaxCode()));
        //获取创建状态在字典中的id
        Long status = FeignOptional.of(remoteDictFeignApi.getByCode(PmManagerConstant.MRBS_CREATED,
                PmManagerConstant.MATERIAL_REQUISITION_STATUS)).get().getId();
        model.setStatus(status);
        save(model);
        if (CollUtil.isNotEmpty(materialRequisitionDTO.getList())) {
            List<PmsMaterialRequisitionInfo> list = Lists.newArrayList();
            materialRequisitionDTO.getList().forEach(materialRequisitionInfoDTO -> {
                PmsMaterialRequisitionInfo materialRequisitionInfo = new PmsMaterialRequisitionInfo();
                BeanUtil.copyProperties(materialRequisitionInfoDTO, materialRequisitionInfo);
                materialRequisitionInfo.setMaterialRequisitionId(model.getId());
                list.add(materialRequisitionInfo);
            });
            pmsMaterialRequisitionInfoService.saveBatch(list);
        }
        return Boolean.TRUE;
    }

    @Override
    public Boolean edit(MaterialRequisitionDTO materialRequisitionDTO) {
        //1.校验领料单状态
        PmsMaterialRequisition materialRequisition = getById(materialRequisitionDTO.getId());
        Assert.notNull(materialRequisition, "领料单不存在");
        List<Long> idSet = Lists.newArrayList();
        idSet.add(materialRequisition.getStatus());
        Map<Long, SysDictVO> dictVOMap = FeignOptional.of(remoteDictFeignApi.listById(idSet)).get();
        Assert.notNull(dictVOMap, "订单状态异常");
        String code = dictVOMap.get(materialRequisition.getStatus()).getCode();
        Assert.isTrue(code.equals(PmManagerConstant.MRBS_CREATED), "领料单不是可编辑状态");
        //2.处理数据
        PmsMaterialRequisition model = new PmsMaterialRequisition();
        BeanUtil.copyProperties(materialRequisitionDTO, model);
        Boolean aBoolean = updateById(model);
        //3.编辑数据
        if (aBoolean) {
            if (CollUtil.isNotEmpty(materialRequisitionDTO.getList())) {
                aBoolean = pmsMaterialRequisitionInfoService.editList(materialRequisitionDTO);
            }
        }
        return aBoolean;
    }

    @Override
    public Boolean delete(Long id) {
        boolean b = removeById(id);
        if (b) {
            pmsMaterialRequisitionInfoService.remove(new LambdaQueryWrapper<PmsMaterialRequisitionInfo>()
                    .eq(PmsMaterialRequisitionInfo::getMaterialRequisitionId, id));
        }
        return b;
    }

    @Override
    public MaterialRequisitionVO get(Long id) {
        //1.根据id 获取领料单信息
        PmsMaterialRequisition pmsMaterialRequisition = getById(id);
        Assert.notNull(pmsMaterialRequisition, "领料单信息不存在");
        //2.获取领料单详情信息
        List<PmsMaterialRequisitionInfo> list = pmsMaterialRequisitionInfoService.list(new LambdaQueryWrapper<PmsMaterialRequisitionInfo>()
                .eq(PmsMaterialRequisitionInfo::getMaterialRequisitionId, id));
        //3.处理数据
        MaterialRequisitionVO materialRequisitionVO = BeanUtil.copyProperties(pmsMaterialRequisition, MaterialRequisitionVO.class);
        //3.1 处理部门数据
        Set<Integer> officeIdSet = Sets.newHashSet();
        //3.2 处理字典数据
        Set<Long> dictIdSet = Sets.newHashSet();
        //3.3 处理物料数据
        List<Long> materialIdSet = Lists.newArrayList();
        dictIdSet.add(materialRequisitionVO.getStatus());
        dictIdSet.add(materialRequisitionVO.getTypeId());
        officeIdSet.add(materialRequisitionVO.getDepartmentId());
        officeIdSet.add(materialRequisitionVO.getWarehouseId());
        List<MaterialRequisitionInfoVO> infoList = Lists.newArrayList();
        if (CollUtil.isNotEmpty(list)) {
            list.forEach(info -> {
                MaterialRequisitionInfoVO model = new MaterialRequisitionInfoVO();
                BeanUtil.copyProperties(info, model);
                dictIdSet.add(model.getUnitId());
                officeIdSet.add(model.getShelfId());
                materialIdSet.add(model.getMaterialId());
                if (CollUtil.isNotEmpty(model.getOrganization())) {
                    model.getOrganization().forEach(officeId -> {
                                officeIdSet.add(officeId);
                            }
                    );
                }
                infoList.add(model);
            });
        }
        //3.3 获取字典、物料以及部门数据
        Map<Long, String> dictMap = FeignOptional.of(remoteDictFeignApi.listByIdCh(dictIdSet)).get();
        Map<Integer, String> officeMap = FeignOptional.of(remoteOfficeFeign.listByIdCh(officeIdSet)).get();
        Map<Long, EqpSpareVO> materialMap = FeignOptional.of(remoteSpareFeignApi.listById(materialIdSet)).get();
        EqpSpareVO eqpSpareVO = new EqpSpareVO();
        if (CollUtil.isNotEmpty(infoList)) {
            infoList.forEach(
                    infoVO -> {
                        EqpSpareVO orDefault = materialMap.getOrDefault(infoVO.getMaterialId(), eqpSpareVO);
                        infoVO.setMaterialName(orDefault.getName());
                        infoVO.setShelfName(officeMap.get(infoVO.getShelfId()));
                        infoVO.setUnitName(dictMap.get(infoVO.getUnitId()));
                        //3.3.1处理机构数据
                        List<String> organizationList = Lists.newArrayList();
                        infoVO.getOrganization().forEach(
                                integer -> {
                                    organizationList.add(officeMap.get(integer));
                                }
                        );
                        infoVO.setOrganizationName(organizationList);
                    }
            );
        }
        materialRequisitionVO.setStatusName(dictMap.get(materialRequisitionVO.getStatus()));
        materialRequisitionVO.setTypeName(dictMap.get(materialRequisitionVO.getTypeId()));
        materialRequisitionVO.setDepartmentName(officeMap.get(materialRequisitionVO.getDepartmentId()));
        materialRequisitionVO.setWarehouseName(officeMap.get(materialRequisitionVO.getWarehouseId()));
        materialRequisitionVO.setList(infoList);
        return materialRequisitionVO;
    }

    @Override
    public Boolean approval(MaterialRequisitionApprovalDTO dto) {
        //1.根据id获取领料单
        PmsMaterialRequisition pmsMaterialRequisition = getById(dto.getId());
        //2.校验领料单是否存在
        Assert.notNull(pmsMaterialRequisition, "该领料单不存在");
        //3.根据审批状态修改审批单
        if (dto.getApprovalFlag().equals(PmsCodeUtil.DEFAULT_PADDING)) {
            pmsMaterialRequisition.setStatus(FeignOptional.of(remoteDictFeignApi.getByCode(PmManagerConstant.MRBS_APPROVAL_PASS,
                    PmManagerConstant.MATERIAL_REQUISITION_STATUS)).get().getId());
        } else {
            pmsMaterialRequisition.setStatus(FeignOptional.of(remoteDictFeignApi.getByCode(PmManagerConstant.MRBS_APPROVAL_FAIL,
                    PmManagerConstant.MATERIAL_REQUISITION_STATUS)).get().getId());
        }
        return updateById(pmsMaterialRequisition);
    }

//    @Override
//    public Page<MaterialRequisitionPlanVO> planPageList(MaterialRequisitionPlanDTO dto) {
//        //1.根据工位id获取上级的产线数据
//        Map<Long, SysOfficeVO> officeMap = FeignOptional.of(remoteOfficeFeign.getParentOfficeMap(dto.getStation())).get();
//        //2.获取产线id
//        List<Integer> lineIds = Lists.newArrayList();
//        officeMap.values().stream().collect(Collectors.toList()).forEach(officeVO -> {
//            lineIds.add(officeVO.getId());
//        });
//        //3.根据产线获取产线下的订单的信息
//        List<PmsProduceOrderVO> list = produceOrderService.executionOfOrder(Sets.newHashSet(lineIds));
//        //4.获取投料数量
//
//        return null;
//    }

    @Override
    public Page<MaterialRequisitionPlanVO> planPageList(MaterialRequisitionPlanDTO dto) {
        List<MaterialRequisitionPlanVO> res = Lists.newArrayList();
        if (CollUtil.isNotEmpty(dto.getStation())) {
            //1.根据工位id获取上级的产线数据
            Map<Integer, SysOfficeVO> officeMap = FeignOptional.of(remoteOfficeFeign.parentInfo(dto.getStation())).get();
            SysOfficeVO sysOfficeVO = new SysOfficeVO();
            //2.获取产线id
            List<Integer> lineIds = Lists.newArrayList();
            officeMap.values().forEach(officeVO -> {
                lineIds.add(officeVO.getId());
            });
            //3.根据产线获取产线下的订单的信息
            List<PmsProduceOrderVO> list = produceOrderService.executionOfOrder(Sets.newHashSet(lineIds));
            List<MaterialRequisitionPlanVO> resBomList = Lists.newArrayList();
            if (CollUtil.isNotEmpty(list)) {
                List<Long> orderIds = Lists.newArrayList();
                list.forEach(pmsProduceOrderVO -> orderIds.add(pmsProduceOrderVO.getId()));
                //根据订单id 获取订单信息
                Map<Long, PmsProduceOrder> orderMap = produceOrderService.orderList(orderIds);
                PmsProduceOrder order = new PmsProduceOrder();
                //3.1 根据订单id获取订单的物料组件信息
                List<PmsWorkOrderBom> bomList = pmsWorkOrderBomService.list(new LambdaQueryWrapper<PmsWorkOrderBom>()
                        .in(PmsWorkOrderBom::getWorkProduceId, orderIds));
                //3.2 获取bom 的工序下的工位绑定的线边仓货架编码
                Set<Long> bomIds = Sets.newHashSet();
                bomList.forEach(bom -> bomIds.add(bom.getId()));
                List<BomStationDTO> stationList = pmsWorkOrderBomService.listStation(bomIds);
                //处理 工位信息
                List<MessMaterialStorageDTO> inventoryDTOList = Lists.newArrayList();
                Map<Long, List<BomStationDTO>> map = stationList.stream().collect(Collectors.groupingBy(BomStationDTO::getMaterialId));
                List<MaterialRequisitionPlanVO> splitBomList = Lists.newArrayList();
                //处理工位与物料
                Map<PmsWorkOrderBom, List<BomStationDTO>> bomMap = Maps.newHashMap();
                map.forEach((materialId, bomStationDTOList) -> {
                    for (PmsWorkOrderBom pmsWorkOrderBom : bomList) {
                        if (materialId.equals(pmsWorkOrderBom.getModuleId())) {
                            bomMap.put(pmsWorkOrderBom, bomStationDTOList);
                        }
                    }
                });
                //拆分数据
                //获取物料的投料信息
                List<FeedQuantityDTO> feedList = Lists.newArrayList();
                bomMap.forEach((bom, stations) -> {
                    FeedQuantityDTO feedQuantityDTO = new FeedQuantityDTO();
                    feedQuantityDTO.setProduceOrderId(bom.getWorkProduceId());
                    feedQuantityDTO.setMaterialId(bom.getModuleId());
                    feedQuantityDTO.setProcessId(bom.getProcessId());
                    feedList.add(feedQuantityDTO);
                });
                Map<FeedQuantityDTO, BigDecimal> feedQuantityMap = pmsCompleteDeliveryInfoService.listQuantity(feedList);
                BigDecimal quantity = BigDecimal.ZERO;
                Set<Long> dictList = Sets.newHashSet();
                Set<Integer> officeId = Sets.newHashSet();
                List<Long> materialIds = Lists.newArrayList();
                Set<Integer> stationIds = Sets.newHashSet();
                bomMap.forEach((bom, stations) -> {
                    int size = stations.size();
                    Set<Integer> stationIdSet = Sets.newHashSet();
                    BigDecimal bigDecimalSize = BigDecimal.valueOf(size);
                    FeedQuantityDTO feedQuantityDTO = new FeedQuantityDTO();
                    feedQuantityDTO.setProduceOrderId(bom.getWorkProduceId());
                    feedQuantityDTO.setMaterialId(bom.getModuleId());
                    feedQuantityDTO.setProcessId(bom.getProcessId());
                    BigDecimal orDefault = feedQuantityMap.getOrDefault(feedQuantityDTO, quantity);
                    PmsProduceOrder orderInfo = orderMap.getOrDefault(bom.getWorkProduceId(), order);
                    for (BomStationDTO station : stations) {
                        SysOfficeVO sysOffice = officeMap.getOrDefault(station.getExecuteStationId(), sysOfficeVO);
                        MaterialRequisitionPlanVO materialRequisitionPlanVO = new MaterialRequisitionPlanVO();
                        materialRequisitionPlanVO.setMaterialId(bom.getModuleId());
                        //四舍五入，保留三位小数的除法
                        BigDecimal divide = BigDecimalUtil.divide(bom.getQuantity(), bigDecimalSize);
                        materialRequisitionPlanVO.setRequireQuantity(divide);
                        materialRequisitionPlanVO.setStationName(station.getExecuteStationName());
                        materialRequisitionPlanVO.setStation(station.getExecuteStationId());
                        materialRequisitionPlanVO.setStationCode(station.getExecuteStationCode());
                        materialRequisitionPlanVO.setOnPassageQuantity(BigDecimal.ZERO);
                        materialRequisitionPlanVO.setProduceOrderId(bom.getWorkProduceId());
                        materialRequisitionPlanVO.setProcessId(bom.getProcessId());
                        materialRequisitionPlanVO.setProductionLine(sysOffice.getId());
                        materialRequisitionPlanVO.setProductionLineName(sysOffice.getOfficeName());
                        materialRequisitionPlanVO.setWorkshop(sysOffice.getParentId());
                        materialRequisitionPlanVO.setWorkshopName(sysOffice.getParentName());
                        materialRequisitionPlanVO.setUnitId(bom.getUnitId());
                        //需求时间
                        LocalDateTime planStartTime = orderInfo.getPlanStartTime();
                        LocalDate date = planStartTime.toLocalDate();
                        materialRequisitionPlanVO.setRequireDate(date);
                        dictList.add(bom.getUnitId());
                        officeId.add(station.getExecuteStationId());
                        materialIds.add(bom.getModuleId());
//                    materialRequisitionPlanVO.setRequireDate();
                        //需求数量（Bom 需求数量 -已投入数量）/ 工位个数
                        BigDecimal requiredQuantity = bom.getQuantity().subtract(orDefault);
                        BigDecimal requireQuantity = BigDecimalUtil.divide(requiredQuantity, bigDecimalSize);
                        materialRequisitionPlanVO.setRequireQuantity(requireQuantity);
                        splitBomList.add(materialRequisitionPlanVO);
                        stationIds.add(materialRequisitionPlanVO.getStation());
                        stationIdSet.add(materialRequisitionPlanVO.getStation());
                    }
                    //处理物料线边库存数据
                    MessMaterialStorageDTO storageDTO = new MessMaterialStorageDTO();
                    storageDTO.setMaterialId(bom.getModuleId());
                    storageDTO.setStationId(stationIdSet);
                    inventoryDTOList.add(storageDTO);
                });
                //获取单位数据以及车间产线数据
                Map<Long, String> dictMap = FeignOptional.of(remoteDictFeignApi.listByIdCh(dictList)).get();
                //获取工位绑定的仓库
                Map<Integer, List<SysStationWarehouseVO>> warehouseMap = FeignOptional.of(remoteStationWarehouseFeignApi.listByStationIds(stationIds)).get();
                List<SysStationWarehouseVO> warehouseVOList = Lists.newArrayList();
                // 获取物料绑定的线边仓库存数据
                inventoryDTOList.forEach(inventoryDTO -> {
                    List<String> warehouseCodes = Lists.newArrayList();
                    inventoryDTO.getStationId().forEach(station -> {
                        List<SysStationWarehouseVO> warehouseList = warehouseMap.getOrDefault(station, warehouseVOList);
                        warehouseList.forEach(warehouse -> warehouseCodes.add(warehouse.getShelfCode()));
                    });
                    inventoryDTO.setShelfCodes(warehouseCodes);
                });
                List<MessMaterialStorageDTO> collect = inventoryDTOList.stream().filter(inventoryDTO -> inventoryDTO.getShelfCodes().size() > 0).collect(Collectors.toList());
                Map<Long, BigDecimal> inventoryMap = remoteMessFeignApi.listMaterialInventory(collect);
                // 获取物料的数据
                EqpSpareVO eqpSpareVO = new EqpSpareVO();
                Map<Long, EqpSpareVO> spareMap = FeignOptional.of(remoteSpareFeignApi.listById(materialIds)).get();
                BigDecimal zero = BigDecimal.ZERO;
                splitBomList.forEach(bom -> {
                    EqpSpareVO spareVO = spareMap.getOrDefault(bom.getMaterialId(), eqpSpareVO);
                    bom.setUnitName(dictMap.get(bom.getUnitId()));
                    bom.setMaterialName(spareVO.getName());
                    bom.setMaterialCode(spareVO.getCode());
                    BigDecimal orDefault = inventoryMap.getOrDefault(bom.getMaterialId(), zero);
                    //线边数量
                    bom.setLineEdgeQuantity(orDefault);
                    //批次数量
                    bom.setBatchDeliveryQuantity(BigDecimal.ZERO);
                    if (bom.getRequireQuantity().subtract(orDefault).compareTo(BigDecimal.ZERO) > 0) {
                        resBomList.add(bom);
                    }
                });
            }
            List<Integer> stationList = dto.getStation();
            //过滤数据 只返回在当前工位上的物料信息
            res = resBomList.stream().filter(plan -> stationList.contains(plan.getStation())).collect(Collectors.toList());
        }
        return LogicalPageUtil.page(res, dto.getCurrent(), dto.getSize());
    }

    @Override
    public R<String> create(List<MaterialRequisitionInfoPlanDTO> list) {
        R<String> r = new R<>();
        MaterialRequisitionDTO materialRequisitionDTO = new MaterialRequisitionDTO();
        List<MaterialRequisitionInfoDTO> infoList = Lists.newArrayList();
        list.forEach(info -> {
            MaterialRequisitionInfoDTO materialRequisitionInfoDTO = new MaterialRequisitionInfoDTO();
            BeanUtil.copyProperties(info, materialRequisitionInfoDTO);
            materialRequisitionInfoDTO.setReceiveQuantity(info.getQuantity());
            infoList.add(materialRequisitionInfoDTO);
        });
        materialRequisitionDTO.setList(infoList);
        //获取部门
        SysOfficeVO sysOfficeVO = FeignOptional.of(remoteOfficeFeign.parent(list.get(0).getWorkshopId())).get();
        materialRequisitionDTO.setDepartmentId(sysOfficeVO.getId());
        //获取类型
        SysDictVO dictVO = FeignOptional.of(remoteDictFeignApi.getByCode(PmManagerConstant.PRODUCE_PICK, PmManagerConstant.MATERIAL_REQUISITION)).get();
        materialRequisitionDTO.setTypeId(dictVO.getId());
        String order = createOrder(materialRequisitionDTO);
        r.setCode(1);
        r.setData(order);
        return r;
    }

    private String createOrder(MaterialRequisitionDTO materialRequisitionDTO) {
        //处理数据
        PmsMaterialRequisition model = new PmsMaterialRequisition();
        BeanUtil.copyProperties(materialRequisitionDTO, model);
        model.setCode(PmsCodeUtil.getProduceOrderCode(PmsCodeUtil.PREFIX_PMS_MATERIAL_REQUISITION, PmsCodeUtil.NUM_BIT_4, currentMaxCode()));
        //获取创建状态在字典中的id
        Long status = FeignOptional.of(remoteDictFeignApi.getByCode(PmManagerConstant.MRBS_CREATED,
                PmManagerConstant.MATERIAL_REQUISITION_STATUS)).get().getId();
        model.setStatus(status);
        save(model);
        if (CollUtil.isNotEmpty(materialRequisitionDTO.getList())) {
            List<PmsMaterialRequisitionInfo> list = Lists.newArrayList();
            materialRequisitionDTO.getList().forEach(materialRequisitionInfoDTO -> {
                PmsMaterialRequisitionInfo materialRequisitionInfo = new PmsMaterialRequisitionInfo();
                BeanUtil.copyProperties(materialRequisitionInfoDTO, materialRequisitionInfo);
                materialRequisitionInfo.setMaterialRequisitionId(model.getId());
                list.add(materialRequisitionInfo);
            });
            pmsMaterialRequisitionInfoService.saveBatch(list);
        }
        return model.getCode();
    }


    /**
     * 当前最大的code值
     *
     * @return 数值类型，可能为null
     */
    private Long currentMaxCode() {
        //1.定义返回值
        Long currentMax = 0L;
        //2.获取系统最大code
        LocalDateTime max = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);
        LocalDateTime min = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        PmsMaterialRequisition maxCodeModel = getOne(new LambdaQueryWrapper<PmsMaterialRequisition>()
                .between(PmsMaterialRequisition::getCreateTime, min, max)
                .orderByDesc(PmsMaterialRequisition::getCode)
                .last(MpConstant.STR_LIMIT_1));
        //3.如果最大code不为空
        if (Objects.nonNull(maxCodeModel)) {
            try {
                //4.解析数值部分
                String substringVal = maxCodeModel.getCode().substring(PmsCodeUtil.PREFIX_PMS_MATERIAL_REQUISITION.length() + PmsCodeUtil.NUM_BIT_8 + PmsCodeUtil.NUM_BIT_2);
                //赋值
                long l = Long.parseLong(substringVal);
                currentMax = l;
            } catch (Exception e) {
                log.error(e.getLocalizedMessage());
            }
        }
        return currentMax;
    }
}




