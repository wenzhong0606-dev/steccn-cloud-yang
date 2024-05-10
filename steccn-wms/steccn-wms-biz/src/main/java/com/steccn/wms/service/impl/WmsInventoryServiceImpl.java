package com.steccn.wms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.common.constant.FeignConstant;
import com.steccn.common.constant.SysDictConstant;
import com.steccn.common.dto.TupleDTO;
import com.steccn.common.util.BigDecimalUtil;
import com.steccn.platform.dto.SysOfficeParentDTO;
import com.steccn.platform.feign.RemoteDictFeignApi;
import com.steccn.platform.feign.RemoteOfficeFeign;
import com.steccn.platform.feign.RemoteSpareFeignApi;
import com.steccn.platform.feign.RemoteSpareUnitConvertFeignApi;
import com.steccn.platform.util.LogicalPageUtil;
import com.steccn.platform.vo.EqpSpareVO;
import com.steccn.platform.vo.SysDictVO;
import com.steccn.platform.vo.SysOfficeVO;
import com.steccn.platform.vo.SysSpareUnitConversionVO;
import com.steccn.pms.constant.PmManagerConstant;
import com.steccn.pms.dto.CaseSiteDTO;
import com.steccn.pms.util.UnitConvertUtil;
import com.steccn.pms.vo.WmsInventoryVO;
import com.steccn.wms.constant.WmsManagerConstant;
import com.steccn.wms.domain.WmsInventory;
import com.steccn.wms.domain.WmsInventoryRecord;
import com.steccn.wms.dto.*;
import com.steccn.wms.service.WmsInventoryRecordService;
import com.steccn.wms.service.WmsCustomerBarcodeService;
import com.steccn.wms.service.WmsInventoryService;
import com.steccn.wms.mapper.WmsInventoryMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.Lists;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * @author W10
 * @description 针对表【wms_inventory(仓储库存)】的数据库操作Service实现
 * @createDate 2022-12-09 11:18:09
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WmsInventoryServiceImpl extends ServiceImpl<WmsInventoryMapper, WmsInventory> implements WmsInventoryService {

    private final RemoteDictFeignApi remoteDictFeignApi;

    private final WmsInventoryMapper wmsInventoryMapper;

    private final RemoteOfficeFeign remoteOfficeFeign;

    private final RemoteSpareFeignApi remoteSpareFeignApi;

    private final WmsCustomerBarcodeService customerBarcodeService;

    private final WmsInventoryRecordService wmsInventoryRecordService;
    
    private final RemoteSpareUnitConvertFeignApi remoteSpareUnitConvertFeignApi;

    @Override
    public Page<WmsInventoryVO> pageList(WmsInventoryPageDTO dto) {
        Page<WmsInventoryVO> page = new Page<>();
        LambdaQueryWrapper<WmsInventory> like = new LambdaQueryWrapper<WmsInventory>()
                .eq(Objects.nonNull(dto.getInventoryId()), WmsInventory::getId, dto.getInventoryId())
                .eq(Objects.nonNull(dto.getWarehouseId()), WmsInventory::getWarehouseId, dto.getWarehouseId())
                .like(Objects.nonNull(dto.getMaterialCode()), WmsInventory::getMaterialCode, dto.getMaterialCode())
                .like(Objects.nonNull(dto.getMaterialName()), WmsInventory::getMaterialName, dto.getMaterialName())
                .like(Objects.nonNull(dto.getCaseNo()), WmsInventory::getCaseNo, dto.getCaseNo())
                .like(Objects.nonNull(dto.getShelfName()), WmsInventory::getShelfName, dto.getShelfName())
                .like(Objects.nonNull(dto.getBatch()), WmsInventory::getBatch, dto.getBatch())
                .and((dto.getCodeOrName() != null && !dto.getCodeOrName().equals("")), wrapper -> wrapper.like(WmsInventory::getMaterialName, dto.getCodeOrName())
                        .or().like(WmsInventory::getMaterialCode, dto.getCodeOrName()))
                .and(wrapper -> wrapper.notIn(WmsInventory::getAvailableQuantity, BigDecimal.ZERO)
                        .or().notIn(WmsInventory::getStockQuantity, BigDecimal.ZERO).or()
                        .notIn(WmsInventory::getFreezeQuantity, BigDecimal.ZERO).or()
                        .notIn(WmsInventory::getLockQuantity, BigDecimal.ZERO).or()
                        .notIn(WmsInventory::getDetectionQuantity, BigDecimal.ZERO).or()
                        .notIn(WmsInventory::getTotalQuantity, BigDecimal.ZERO))
                .orderByDesc(WmsInventory::getUpdateTime)
                .orderByDesc(WmsInventory::getId);
        Page<WmsInventory> resPage = page(new Page<>(dto.getCurrent(), dto.getSize()), like);
        BeanUtil.copyProperties(resPage, page);
        List<WmsInventoryVO> res = BeanUtil.copyToList(resPage.getRecords(), WmsInventoryVO.class);
        Set<Long> dict = Sets.newHashSet();
        List<Long> materialIds = Lists.newArrayList();
        Set<String> caseNos =Sets.newHashSet();
        res.forEach(wmsInventoryVO -> {
            dict.add(wmsInventoryVO.getUnit());
            materialIds.add(wmsInventoryVO.getMaterialId());
            caseNos.add(wmsInventoryVO.getCaseNo());
        });
        //获取箱号绑定的客户条码
        Map<String, String> customerBarcodeMap = customerBarcodeService.customerBarcodeMap(caseNos);
        Map<Long, EqpSpareVO> materialMap = FeignOptional.of(() -> remoteSpareFeignApi.listById(materialIds), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        Map<Long, String> map = FeignOptional.of(() -> remoteDictFeignApi.listByIdCh(dict), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        res.forEach(wmsInventoryVO -> {
            wmsInventoryVO.setUnitName(map.get(wmsInventoryVO.getUnit()));
            wmsInventoryVO.setEqpSpareVO(materialMap.get(wmsInventoryVO.getMaterialId()));
            wmsInventoryVO.setCustomerBarcode(customerBarcodeMap.get(wmsInventoryVO.getCaseNo()));
        });
        page.setRecords(res);
        return page;
    }

    @Override
    public Boolean updateLockQuantity(List<WmsInventoryDTO> infoList, Integer state) {
        Assert.isTrue(CollUtil.isNotEmpty(infoList), "数据异常");
        //自定义sql 修改锁定库存信息
        Boolean aBoolean = Boolean.TRUE;
        if (WmsManagerConstant.InventoryEnum.ZERO.getCode().equals(state)) {
            //增加锁定库存，减少可用库存
            List<Long> ids = Lists.newArrayList();
            infoList.forEach(info -> ids.add(info.getId()));
            Map<Long, List<WmsInventory>> collect = list(new LambdaQueryWrapper<WmsInventory>().in(WmsInventory::getId, ids)).stream().collect(Collectors.groupingBy(WmsInventory::getId));
            if (CollUtil.isNotEmpty(collect)) {
                List<WmsInventory> inventoryList = Lists.newArrayList();
                infoList.forEach(info -> {
                    WmsInventory inventory = collect.get(info.getId()).get(0);
                    inventory.setLockQuantity(inventory.getLockQuantity().add(info.getQuantity()));
                    inventory.setAvailableQuantity(inventory.getAvailableQuantity().subtract(info.getQuantity()));
                    inventoryList.add(inventory);
                });
                updateBatchById(inventoryList);
            }
//            aBoolean = this.baseMapper.augmentLockQuantity(infoList);
        } else if (WmsManagerConstant.InventoryEnum.ONE.getCode().equals(state)) {
            aBoolean = this.baseMapper.subtractLockQuantity(infoList);
        } else if (WmsManagerConstant.InventoryEnum.TWO.getCode().equals(state)) {
            aBoolean = this.baseMapper.subtractFirstAndThenAddLockQuantity(infoList);
        } else if (WmsManagerConstant.InventoryEnum.THREE.getCode().equals(state)) {
            aBoolean = this.baseMapper.updateLockAndTotalQuantity(infoList);
        }
        return aBoolean;
    }


    @Override
    public Boolean addInfo(List<WmsInventoryDTO> infoList) {
        if (CollUtil.isNotEmpty(infoList)) {
            List<String> list = Lists.newArrayList();
            Set<Integer> officeIds = Sets.newHashSet();
            infoList.forEach(info -> {
                //根据箱号获取当前的物料是否存在库存信息
                list.add(info.getCaseNo());
                officeIds.add(info.getWarehouseId());
                officeIds.add(info.getShelfId());
            });
            Map<String, WmsInventory> inventoryByCaseNo = getInventoryByCaseNo(list);
            List<WmsInventory> addWmsInventory = Lists.newArrayList();
            Map<Integer, SysOfficeVO> officeMap = FeignOptional.of(() -> remoteOfficeFeign.listById(officeIds), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            infoList.forEach(info -> {
                WmsInventory orDefault = inventoryByCaseNo.get(info.getCaseNo());
                SysOfficeVO shelfVO = officeMap.get(info.getShelfId());
                SysOfficeVO warehouseVO = officeMap.get(info.getWarehouseId());
                info.setWarehouseName(warehouseVO.getOfficeName());
                info.setShelfCode(shelfVO.getOfficeCode());
                info.setShelfName(shelfVO.getOfficeName());
                if (Objects.isNull(orDefault)) {
                    WmsInventory inventory = new WmsInventory();
                    BeanUtil.copyProperties(info, inventory);
                    inventory.setStockQuantity(info.getQuantity());
                    addWmsInventory.add(inventory);
                } else {
                    orDefault.setStockQuantity(orDefault.getStockQuantity().add(info.getQuantity()));
                    addWmsInventory.add(orDefault);
                }
            });
            saveOrUpdateBatch(addWmsInventory);
        }
        return true;
    }

    @Override
    public Boolean add(List<WmsInventoryDTO> infoList) {
        if (CollUtil.isNotEmpty(infoList)) {
            Set<Integer> officeIds = Sets.newHashSet();
            infoList.forEach(info -> {
                //根据箱号获取当前的物料是否存在库存信息
                officeIds.add(info.getWarehouseId());
                officeIds.add(info.getShelfId());
            });
            List<WmsInventory> addWmsInventory = Lists.newArrayList();
            Map<Integer, SysOfficeVO> officeMap = FeignOptional.of(() -> remoteOfficeFeign.listById(officeIds), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            infoList.forEach(info -> {
                SysOfficeVO shelfVO = officeMap.get(info.getShelfId());
                SysOfficeVO warehouseVO = officeMap.get(info.getWarehouseId());
                info.setId(null);
                info.setWarehouseName(warehouseVO.getOfficeName());
                info.setShelfCode(shelfVO.getOfficeCode());
                info.setShelfName(shelfVO.getOfficeName());
                WmsInventory inventory = new WmsInventory();
                BeanUtil.copyProperties(info, inventory);
                inventory.setStockQuantity(info.getQuantity());
                addWmsInventory.add(inventory);
            });
            saveBatch(addWmsInventory);
        }
        return true;
    }

    @Override
    public Boolean restore(List<WmsInventoryDTO> infoList) {
        if (CollUtil.isNotEmpty(infoList)) {
            List<String> list = Lists.newArrayList();
            infoList.forEach(info -> {
                //根据箱号获取当前的物料的库存信息
                list.add(info.getOriginalCaseNo());
            });
            Map<String, WmsInventory> inventoryByCaseNo = getInventoryByCaseNo(list);
            WmsInventory wmsInventory = new WmsInventory();
            List<WmsInventory> addWmsInventory = Lists.newArrayList();
            infoList.forEach(info -> {
                WmsInventory orDefault = inventoryByCaseNo.getOrDefault(info.getOriginalCaseNo(), wmsInventory);
                orDefault.setStockQuantity(orDefault.getStockQuantity().subtract(info.getOriginalQuantity()));
                addWmsInventory.add(orDefault);
            });
            updateBatchById(addWmsInventory);
        }
        return true;
    }

    @Override
    public Boolean updateInventory(List<WmsInventoryDTO> inventoryDTOList) {
        if (CollUtil.isNotEmpty(inventoryDTOList)) {
            List<String> caseNoList = Lists.newArrayList();
            inventoryDTOList.forEach(wmsInventoryDTO -> {
                caseNoList.add(wmsInventoryDTO.getCaseNo());
            });
            Map<String, WmsInventory> inventoryByCaseNo = getInventoryByCaseNo(caseNoList);
            WmsInventory wmsInventory = new WmsInventory();
            List<WmsInventory> addWmsInventory = Lists.newArrayList();
            inventoryDTOList.forEach(info -> {
                WmsInventory orDefault = inventoryByCaseNo.getOrDefault(info.getCaseNo(), wmsInventory);
                orDefault.setStockQuantity(orDefault.getStockQuantity().subtract(info.getOriginalQuantity()).add(info.getQuantity()));
                addWmsInventory.add(orDefault);
            });
            updateBatchById(addWmsInventory);
        }
        return true;
    }

    @Override
    public Boolean affirm(List<WmsInventoryDTO> inventoryDTOList) {
        if (CollUtil.isNotEmpty(inventoryDTOList)) {
            List<String> caseNoList = Lists.newArrayList();
            inventoryDTOList.forEach(wmsInventoryDTO -> {
                caseNoList.add(wmsInventoryDTO.getCaseNo());
            });
            Map<String, WmsInventory> inventoryByCaseNo = getInventoryByCaseNo(caseNoList);
            WmsInventory wmsInventory = new WmsInventory();
            List<WmsInventory> addWmsInventory = Lists.newArrayList();
            inventoryDTOList.forEach(info -> {
                WmsInventory orDefault = inventoryByCaseNo.getOrDefault(info.getCaseNo(), wmsInventory);

                orDefault.setStockQuantity(orDefault.getStockQuantity().subtract(info.getQuantity()));
                orDefault.setAvailableQuantity(orDefault.getAvailableQuantity().add(info.getQuantity()));
                orDefault.setTotalQuantity(orDefault.getTotalQuantity().add(info.getQuantity()));
                addWmsInventory.add(orDefault);

            });
            updateBatchById(addWmsInventory);

        }
        return true;
    }

    @Override
    public Boolean addLockQuantity(List<WmsInventoryDTO> addList) {
        if (CollUtil.isNotEmpty(addList)) {
            List<Long> ids = Lists.newArrayList();
            addList.forEach(wmsInventoryDTO -> ids.add(wmsInventoryDTO.getId()));
            Map<Long, WmsInventory> inventoryMap = getInventoryId(ids);
            List<WmsInventory> addWmsInventory = Lists.newArrayList();
            addList.forEach(info -> {
                WmsInventory wmsInventory = inventoryMap.get(info.getId());
                wmsInventory.setLockQuantity(wmsInventory.getLockQuantity().add(info.getQuantity()));
                wmsInventory.setAvailableQuantity(wmsInventory.getAvailableQuantity().subtract(info.getQuantity()));
                addWmsInventory.add(wmsInventory);
            });
            updateBatchById(addWmsInventory);
        }
        return true;
    }

    @Override
    public Boolean restoreLockQuantity(List<WmsInventoryDTO> deleteList) {
        if (CollUtil.isNotEmpty(deleteList)) {
            List<Long> ids = Lists.newArrayList();
            deleteList.forEach(wmsInventoryDTO -> ids.add(wmsInventoryDTO.getId()));
            Map<Long, WmsInventory> inventoryMap = getInventoryId(ids);
            List<WmsInventory> addWmsInventory = Lists.newArrayList();
            deleteList.forEach(info -> {
                WmsInventory wmsInventory = inventoryMap.get(info.getId());
                wmsInventory.setLockQuantity(wmsInventory.getLockQuantity().subtract(info.getQuantity()));
                wmsInventory.setAvailableQuantity(wmsInventory.getAvailableQuantity().add(info.getQuantity()));
                addWmsInventory.add(wmsInventory);
            });
            updateBatchById(addWmsInventory);
        }
        return true;
    }

    @Override
    public Boolean editLockQuantity(List<WmsInventoryDTO> updateList) {
        if (CollUtil.isNotEmpty(updateList)) {
            List<Long> ids = Lists.newArrayList();
            updateList.forEach(wmsInventoryDTO -> ids.add(wmsInventoryDTO.getId()));
            Map<Long, WmsInventory> inventoryMap = getInventoryId(ids);
            List<WmsInventory> addWmsInventory = Lists.newArrayList();
            updateList.forEach(info -> {
                WmsInventory wmsInventory = inventoryMap.get(info.getId());
                wmsInventory.setLockQuantity(wmsInventory.getLockQuantity().subtract(info.getOriginalQuantity()).add(info.getQuantity()));
                wmsInventory.setAvailableQuantity(wmsInventory.getAvailableQuantity().add(info.getOriginalQuantity()).subtract(info.getQuantity()));
                addWmsInventory.add(wmsInventory);
            });
            updateBatchById(addWmsInventory);
        }
        return true;
    }

    @Override
    public Boolean affirmLockQuantity(List<WmsInventoryDTO> inventoryDTOList) {
        if (CollUtil.isNotEmpty(inventoryDTOList)) {
            List<Long> ids = Lists.newArrayList();
            inventoryDTOList.forEach(wmsInventoryDTO -> ids.add(wmsInventoryDTO.getId()));
            Map<Long, WmsInventory> inventoryMap = getInventoryId(ids);
            List<WmsInventory> addWmsInventory = Lists.newArrayList();
            inventoryDTOList.forEach(info -> {
                WmsInventory orDefault = inventoryMap.get(info.getId());
                orDefault.setLockQuantity(orDefault.getLockQuantity().subtract(info.getQuantity()));
                orDefault.setTotalQuantity(orDefault.getTotalQuantity().subtract(info.getQuantity()));
                addWmsInventory.add(orDefault);
            });
            updateBatchById(addWmsInventory);
        }
        return true;
    }

    @Override
    public Boolean freeze(WmsInventoryDTO dto) {
        WmsInventory inventory = getById(dto.getId());
        Assert.notNull(inventory, "库存信息不存在");
        Assert.isTrue(inventory.getAvailableQuantity().subtract(dto.getQuantity()).compareTo(BigDecimal.ZERO) > 0, "可用库存不足，不可冻结");
        inventory.setFreezeQuantity(inventory.getFreezeQuantity().add(dto.getQuantity()));
        inventory.setAvailableQuantity(inventory.getAvailableQuantity().subtract(dto.getQuantity()));
        return updateById(inventory);
    }

    @Override
    public Boolean unfreeze(WmsInventoryDTO dto) {
        WmsInventory inventory = getById(dto.getId());
        Assert.notNull(inventory, "库存信息不存在");
        BigDecimal subtract = inventory.getFreezeQuantity().subtract(dto.getQuantity());
        Assert.isTrue(subtract.compareTo(BigDecimal.ZERO) >= 0, "解冻数可以小于冻结数，但不能大于冻结数");
        inventory.setFreezeQuantity(inventory.getFreezeQuantity().subtract(dto.getQuantity()));
        inventory.setAvailableQuantity(inventory.getAvailableQuantity().add(dto.getQuantity()));
        return updateById(inventory);
    }

    @Override
    public Map<String, CaseSiteDTO> stockByCaseNo(Set<String> caseNos) {
        //定义返回集合
        Map<String, CaseSiteDTO> map = Maps.newHashMap();
        if (CollUtil.isNotEmpty(caseNos)) {
            List<WmsInventory> list = list(new LambdaQueryWrapper<WmsInventory>().in(WmsInventory::getCaseNo, caseNos));
            //处理数据
            if (CollUtil.isNotEmpty(list)) {
                list.forEach(inventory -> {
                    CaseSiteDTO caseSiteDTO = new CaseSiteDTO();
                    BeanUtil.copyProperties(inventory, caseSiteDTO);
                    map.put(caseSiteDTO.getCaseNo(), caseSiteDTO);
                });
            }
        }
        return map;
    }

    @Override
    public Map<Long, BigDecimal> listMaterialInventory(List<MaterialStorageDTO> list) {
        Map<Long, BigDecimal> map = Maps.newHashMap();
        //自定义sql 获取库存信息
        if (CollUtil.isNotEmpty(list)) {
            List<WmsInventory> inventoryDTOList = wmsInventoryMapper.listMaterialInventory(list);
            if (CollUtil.isNotEmpty(inventoryDTOList)) {
                Map<Long, List<WmsInventory>> collect = inventoryDTOList.stream().collect(Collectors.groupingBy(WmsInventory::getMaterialId));
                for (Map.Entry<Long, List<WmsInventory>> longListEntry : collect.entrySet()) {
                    BigDecimal quantity = BigDecimal.ZERO;
                    for (WmsInventory wmsInventory : longListEntry.getValue()) {
                        quantity = quantity.add(wmsInventory.getAvailableQuantity());
                    }
                    map.put(longListEntry.getKey(), quantity);
                }
            }
        }
        return map;
    }

    @Override
    public Map<Long, MaterialInventoryDTO> materialInventory(Set<Long> materialIds) {
        Map<Long, MaterialInventoryDTO> map = Maps.newHashMap();
        //批量查询物料在库存中的存量
        if (CollUtil.isNotEmpty(materialIds)) {
            List<WmsInventory> list = list(new LambdaQueryWrapper<WmsInventory>().in(WmsInventory::getMaterialId, materialIds));
            if (CollUtil.isNotEmpty(list)) {
                list.forEach(inventory -> {
                    MaterialInventoryDTO inventoryDTO = new MaterialInventoryDTO();
                    BeanUtil.copyProperties(inventory, inventoryDTO);
                    map.put(inventory.getMaterialId(), inventoryDTO);
                });
            }
        }
        return map;
    }

    @Override
    public Map<Long, List<WmsInventory>> inventoryMap(Set<Long> materialIds) {
        Map<Long, List<WmsInventory>> map = new HashMap<>();
        if (CollUtil.isNotEmpty(materialIds)) {
            List<WmsInventory> list = list(new LambdaQueryWrapper<WmsInventory>().in(WmsInventory::getMaterialId, materialIds));
            if (CollUtil.isNotEmpty(list)) {
                map = list.stream().collect(Collectors.groupingBy(WmsInventory::getMaterialId));
            }
        }
        return map;
    }

    @Override
    public List<WmsStockInventoryDTO> materialConsume(List<MaterialStorageInfoDTO> list) {
//        //定义集合
//        //批量查询物料在库存中的存量
        LinkedList<WmsStockInventoryDTO> materialStorageVOList = new LinkedList<>();
        if (CollUtil.isNotEmpty(list)) {
            List<Long> materialIds = Lists.newArrayList();
            list.forEach(messMaterialStorageDTO -> materialIds.add(messMaterialStorageDTO.getMaterialId()));
            List<WmsInventory> materialInventoryList = list(new LambdaQueryWrapper<WmsInventory>().in(WmsInventory::getMaterialId, materialIds));
            //获取需要的库存数量，去除多余数量条数
            if (CollUtil.isNotEmpty(materialInventoryList)) {
                List<WmsStockInventoryDTO> materialList = Lists.newArrayList();
                materialInventoryList.forEach(materialInventory -> {
                    WmsStockInventoryDTO wmsStockInventoryDTO = new WmsStockInventoryDTO();
                    BeanUtil.copyProperties(materialInventory, wmsStockInventoryDTO);
                    wmsStockInventoryDTO.setInventoryId(materialInventory.getId());
                    wmsStockInventoryDTO.setBatchNo(materialInventory.getBatch());
                    wmsStockInventoryDTO.setCreateDate(materialInventory.getCreateTime());
                    materialList.add(wmsStockInventoryDTO);
                });
                Map<Long, List<WmsStockInventoryDTO>> materialMap = materialList.stream().collect(Collectors.groupingBy(WmsStockInventoryDTO::getMaterialId));
                List<WmsStockInventoryDTO> inventoryList = Lists.newArrayList();
                list.forEach(material -> {
                    List<WmsStockInventoryDTO> wmsInventoryList = materialMap.getOrDefault(material.getMaterialId(), inventoryList);
                    if (CollUtil.isNotEmpty(wmsInventoryList)) {
                        BigDecimal sum = BigDecimal.ZERO;
                        wmsInventoryList.sort(Comparator.comparing(WmsStockInventoryDTO::getCreateDate));
                        //过滤库存小于等于O的数据
                        List<WmsStockInventoryDTO> collect = wmsInventoryList.stream().filter(wmsInventory -> wmsInventory.getAvailableQuantity().compareTo(BigDecimal.ZERO) > 0).collect(Collectors.toList());
                        if (CollUtil.isNotEmpty(collect)) {
                            for (WmsStockInventoryDTO wmsInventory : collect) {
                                sum = wmsInventory.getAvailableQuantity().add(sum);
                                wmsInventory.setQuantity(wmsInventory.getAvailableQuantity());
                                materialStorageVOList.add(wmsInventory);
                                if (material.getQuantity().compareTo(sum) < 1) {
                                    WmsStockInventoryDTO last = materialStorageVOList.getLast();
                                    //计算实际使用数量
                                    last.setQuantity(last.getAvailableQuantity().subtract(sum.subtract(material.getQuantity())));
                                    break;
                                }
                            }
                        } else {
                            WmsStockInventoryDTO materialStorageVO = new WmsStockInventoryDTO();
                            materialStorageVO.setMaterialId(material.getMaterialId());
                            materialStorageVO.setAvailableQuantity(BigDecimal.ZERO);
                            materialStorageVO.setCreateDate(null);
                            materialStorageVOList.add(materialStorageVO);
                        }
                    } else {
                        WmsStockInventoryDTO materialStorageVO = new WmsStockInventoryDTO();
                        materialStorageVO.setMaterialId(material.getMaterialId());
                        materialStorageVO.setAvailableQuantity(BigDecimal.ZERO);
                        materialStorageVO.setCreateDate(null);
                        materialStorageVOList.add(materialStorageVO);
                    }
                });
            } else {
                if (CollUtil.isNotEmpty(materialIds)) {
                    materialIds.forEach(materialId -> {
                        WmsStockInventoryDTO materialStorageVO = new WmsStockInventoryDTO();
                        materialStorageVO.setMaterialId(materialId);
                        materialStorageVO.setAvailableQuantity(BigDecimal.ZERO);
                        materialStorageVO.setCreateDate(null);
                        materialStorageVOList.add(materialStorageVO);
                    });
                }
            }
        }
        return materialStorageVOList;
    }

    @Override
    public Boolean checkCaseNo(WmsCheckCaseNoDTO wmsCheckCaseNoDTO) {
        if (Objects.nonNull(wmsCheckCaseNoDTO)) {
            List<WmsInventory> list = list(new LambdaQueryWrapper<WmsInventory>().eq(WmsInventory::getCaseNo, wmsCheckCaseNoDTO.getCaseNo()));
            if (CollUtil.isNotEmpty(list)) {
                Boolean aBoolean = Boolean.FALSE;
                for (WmsInventory wmsInventory : list) {
                    if (wmsInventory.getShelfId().equals(wmsCheckCaseNoDTO.getShelfId()) && wmsInventory.getBatch().equals(wmsCheckCaseNoDTO.getBatch()) && wmsInventory.getMaterialId().equals(wmsCheckCaseNoDTO.getMaterialId())) {
                        aBoolean = Boolean.TRUE;
                    }
                }
                return aBoolean;
            }
        }
        return true;
    }

    @Override
    public Map<Integer, List<WmsStockInventoryDTO>> materialByShelf(Set<Integer> set) {
        Map<Integer, List<WmsStockInventoryDTO>> res = Maps.newHashMap();
        if (CollUtil.isNotEmpty(set)) {
            List<WmsInventory> list = list(new LambdaQueryWrapper<WmsInventory>().in(WmsInventory::getShelfId, set));
            if (CollUtil.isNotEmpty(list)) {
                List<WmsStockInventoryDTO> wmsStockInventoryDTOS = BeanUtil.copyToList(list, WmsStockInventoryDTO.class);
                res = wmsStockInventoryDTOS.stream().collect(Collectors.groupingBy(WmsStockInventoryDTO::getShelfId));
            }
        }
        return res;
    }

    @Override
    public Map<Integer, List<WmsStockInventoryDTO>> materialByWarehouse(Set<Integer> set) {

        Map<Integer, List<WmsStockInventoryDTO>> res = Maps.newHashMap();
        if (CollUtil.isNotEmpty(set)) {
            List<WmsInventory> list = list(new LambdaQueryWrapper<WmsInventory>().in(WmsInventory::getWarehouseId, set).gt(WmsInventory::getAvailableQuantity, BigDecimal.ZERO));
            if (CollUtil.isNotEmpty(list)) {
                List<WmsStockInventoryDTO> wmsStockInventoryDTOS = Lists.newArrayList();
                list.forEach(wmsInventory -> {
                    WmsStockInventoryDTO wmsStockInventoryDTO = new WmsStockInventoryDTO();
                    BeanUtil.copyProperties(wmsInventory, wmsStockInventoryDTO);
                    wmsStockInventoryDTO.setBatchNo(wmsInventory.getBatch());
                    wmsStockInventoryDTO.setInventoryId(wmsInventory.getId());
                    wmsStockInventoryDTO.setCreateDate(wmsInventory.getCreateTime());
                    wmsStockInventoryDTOS.add(wmsStockInventoryDTO);
                });
                res = wmsStockInventoryDTOS.stream().collect(Collectors.groupingBy(WmsStockInventoryDTO::getWarehouseId));
            }
        }
        return res;
    }

    @Override
    public Boolean deduct(List<WmsInventory> wmsInventoryList) {
        return updateBatchById(wmsInventoryList);
    }

    @Override
    public Boolean deleteData() {
        log.info("开始时间："+ LocalDateTime.now());
        Set<String> names = new HashSet<>();
        names.add("备品备件库");
        names.add("包装材料库");
        names.add("辅助材料库-光伏");
        //获取仓库id
        Map<String, SysOfficeVO> map = FeignOptional.of(() -> remoteOfficeFeign.listByName(names), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        Set<Long> ids = new HashSet<>();
        map.values().forEach( office ->ids.add(Long.valueOf(office.getId())));
        List<WmsInventory> list = list(new LambdaQueryWrapper<WmsInventory>().in(WmsInventory::getWarehouseId, ids));
        list.forEach( wmsInventory -> {
            wmsInventory.setDelFlag(1);
            wmsInventory.setCreateBy(System.currentTimeMillis());
        });
        List<Long> collect = list.stream().map(WmsInventory::getId).collect(Collectors.toList());
        boolean b = removeByIds(collect);
        log.info("删除库存信息："+list.size() + b);
        log.info("结束时间："+ LocalDateTime.now());
        return true;
    }

    @Override
    public Page<WmsInventoryVO> materialList(WmsInventoryPageDTO dto) {
        //获取查询的物料列表
        List<WmsInventory> list = list(new LambdaQueryWrapper<WmsInventory>()
                .like(Objects.nonNull(dto.getMaterialName()),WmsInventory::getMaterialName, dto.getMaterialName())
                .select(WmsInventory::getId, WmsInventory::getMaterialId, WmsInventory::getMaterialName, WmsInventory::getMaterialCode, WmsInventory::getWarehouseId,
                        WmsInventory::getWarehouseName,WmsInventory::getTotalQuantity,WmsInventory::getAvailableQuantity,WmsInventory::getStockQuantity,WmsInventory::getFreezeQuantity,WmsInventory::getLockQuantity));
        List<WmsInventoryVO> inventoryList = summarizationList(list);
        //逻辑分页
        Page<WmsInventoryVO> page  = LogicalPageUtil.page(inventoryList, dto.getCurrent(), dto.getSize());
        return page;
    }

    @Override
    public Map<String, WmsInventory> inventoryMapByCaseNos(Set<String> caseNos) {
        Map<String, WmsInventory> map = new HashMap<>();
        if (CollUtil.isNotEmpty(caseNos)) {
            List<WmsInventory> list = list(new LambdaQueryWrapper<WmsInventory>().in(WmsInventory::getCaseNo, caseNos)
                    .and(wrapper -> wrapper.notIn(WmsInventory::getAvailableQuantity, BigDecimal.ZERO)));
            if (CollUtil.isNotEmpty(list)) {
                list.forEach( inventory-> map.put(inventory.getCaseNo(),inventory));
            }
        }
        return map;
    }

    /**
     * 获取实时库存中仓库下呆滞库存的物料之合
     * @param dto
     * @return
     */
    @Override
    public TupleDTO<BigDecimal,BigDecimal> inactionStock(WmsReportWarehouseDTO dto) {
        TupleDTO<BigDecimal,BigDecimal>  tupleDTO =  new TupleDTO<>();
        BigDecimal quantity = null;
        AtomicReference<BigDecimal> totalM = new AtomicReference<>(BigDecimal.ZERO);
        //获取仓库下的呆滞货架
        if (Objects.nonNull(dto.getWarehouseIds())){
            SysOfficeParentDTO sysOfficeParentDTO = new SysOfficeParentDTO();
            sysOfficeParentDTO.setOfficeType(WmsManagerConstant.CODE_FIVE);
            Set<Integer> warehouseIds = dto.getWarehouseIds().stream()
                    .map(Long::intValue)
                    .collect(Collectors.toSet());
            sysOfficeParentDTO.setParentIds(warehouseIds);
            List<SysOfficeVO> officeVOList = FeignOptional.of(() -> remoteOfficeFeign.listByParentIdAndType(sysOfficeParentDTO), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            if (CollUtil.isNotEmpty(officeVOList)) {
                //获取呆滞品货架
                Set<Integer> shelfIds = officeVOList.stream()
                        .filter(vo -> WmsManagerConstant.INACTION_STOCK.equals(vo.getOfficeName())).map(SysOfficeVO::getId).collect(Collectors.toSet());
                List<WmsInventory> list = list(new LambdaQueryWrapper<WmsInventory>().in(WmsInventory::getShelfId, shelfIds));
                if (CollUtil.isNotEmpty(list)) {
                    List<BigDecimal> collect = list.stream().map(WmsInventory::getTotalQuantity).collect(Collectors.toList());
                    Set<Long> materialIdSet = list.stream().map(WmsInventory::getMaterialId).collect(Collectors.toSet());
                    //获取转换单位
                    SysDictVO unitPiece = FeignOptional.of(() -> remoteDictFeignApi.getByCode(PmManagerConstant.MEASURE_UNIT_PIECE, SysDictConstant.TYPE_MEASURE_UNIT), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
                    SysDictVO unitSm = FeignOptional.of(() -> remoteDictFeignApi.getByCode(PmManagerConstant.MEASURE_UNIT_SM, SysDictConstant.TYPE_MEASURE_UNIT), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
                    //单位转换
                    Map<Long, List<SysSpareUnitConversionVO>> materialConvertMap = FeignOptional.of(() -> remoteSpareUnitConvertFeignApi.convert(materialIdSet), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
                    quantity = BigDecimalUtil.sum(collect);
                    list.forEach(inventory -> {
                        totalM.set(BigDecimalUtil.add(totalM.get(), UnitConvertUtil.convertMaterial(materialConvertMap.get(inventory.getMaterialId()), unitPiece.getId(), unitSm.getId(), inventory.getTotalQuantity(), 4)));
                    });
                }
            }
        }
        tupleDTO.setField1(quantity);
        tupleDTO.setField2(totalM.get());
        return tupleDTO;
    }

    @Override
    public BigDecimal inventoryM(WmsReportWarehouseDTO wmsReportWarehouseDTO) {
        AtomicReference<BigDecimal> totalM = new AtomicReference<>(BigDecimal.ZERO);
        List<WmsInventory> list = list(new LambdaQueryWrapper<WmsInventory>().in(WmsInventory::getWarehouseId, wmsReportWarehouseDTO.getWarehouseIds()));
        if (CollUtil.isNotEmpty(list)){
            Set<Long> materialIdSet = list.stream().map(WmsInventory::getMaterialId).collect(Collectors.toSet());
            //获取转换单位
            SysDictVO unitPiece = FeignOptional.of(() -> remoteDictFeignApi.getByCode(PmManagerConstant.MEASURE_UNIT_PIECE, SysDictConstant.TYPE_MEASURE_UNIT), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            SysDictVO unitSm = FeignOptional.of(() -> remoteDictFeignApi.getByCode(PmManagerConstant.MEASURE_UNIT_SM, SysDictConstant.TYPE_MEASURE_UNIT), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            //单位转换
            Map<Long, List<SysSpareUnitConversionVO>> materialConvertMap = FeignOptional.of(() -> remoteSpareUnitConvertFeignApi.convert(materialIdSet), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            list.forEach(inventory -> {
                totalM.set(BigDecimalUtil.add(totalM.get(), UnitConvertUtil.convertMaterial(materialConvertMap.get(inventory.getMaterialId()), unitPiece.getId(), unitSm.getId(), inventory.getTotalQuantity(), 4)));
            });
        }
        return totalM.get();
    }

    @Override
    public Page<WmsInventoryVO> pageListAvailableZero(WmsInventoryPageDTO dto) {
        Page<WmsInventoryVO> page = new Page<>();
        LambdaQueryWrapper<WmsInventory> like = new LambdaQueryWrapper<WmsInventory>()
                .eq(Objects.nonNull(dto.getInventoryId()), WmsInventory::getId, dto.getInventoryId())
                .eq(Objects.nonNull(dto.getWarehouseId()), WmsInventory::getWarehouseId, dto.getWarehouseId())
                .like(Objects.nonNull(dto.getMaterialCode()), WmsInventory::getMaterialCode, dto.getMaterialCode())
                .like(Objects.nonNull(dto.getMaterialName()), WmsInventory::getMaterialName, dto.getMaterialName())
                .like(Objects.nonNull(dto.getCaseNo()), WmsInventory::getCaseNo, dto.getCaseNo())
                .like(Objects.nonNull(dto.getShelfName()), WmsInventory::getShelfName, dto.getShelfName())
                .like(Objects.nonNull(dto.getBatch()), WmsInventory::getBatch, dto.getBatch())
                .and((dto.getCodeOrName() != null && !dto.getCodeOrName().equals("")), wrapper -> wrapper.like(WmsInventory::getMaterialName, dto.getCodeOrName())
                        .or().like(WmsInventory::getMaterialCode, dto.getCodeOrName()))
                .and(wrapper -> wrapper.notIn(WmsInventory::getAvailableQuantity, BigDecimal.ZERO))
                .orderByDesc(WmsInventory::getUpdateTime)
                .orderByDesc(WmsInventory::getId);
        Page<WmsInventory> resPage = page(new Page<>(dto.getCurrent(), dto.getSize()), like);
        BeanUtil.copyProperties(resPage, page);
        List<WmsInventoryVO> res = BeanUtil.copyToList(resPage.getRecords(), WmsInventoryVO.class);
        Set<Long> dict = Sets.newHashSet();
        List<Long> materialIds = Lists.newArrayList();
        Set<String> caseNos =Sets.newHashSet();
        res.forEach(wmsInventoryVO -> {
            dict.add(wmsInventoryVO.getUnit());
            materialIds.add(wmsInventoryVO.getMaterialId());
            caseNos.add(wmsInventoryVO.getCaseNo());
        });
        //获取箱号绑定的客户条码
        Map<String, String> customerBarcodeMap = customerBarcodeService.customerBarcodeMap(caseNos);
        Map<Long, EqpSpareVO> materialMap = FeignOptional.of(() -> remoteSpareFeignApi.listById(materialIds), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        Map<Long, String> map = FeignOptional.of(() -> remoteDictFeignApi.listByIdCh(dict), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        res.forEach(wmsInventoryVO -> {
            wmsInventoryVO.setUnitName(map.get(wmsInventoryVO.getUnit()));
            wmsInventoryVO.setEqpSpareVO(materialMap.get(wmsInventoryVO.getMaterialId()));
            wmsInventoryVO.setCustomerBarcode(customerBarcodeMap.get(wmsInventoryVO.getCaseNo()));
        });
        page.setRecords(res);
        return page;
    }

    @Override
    public BigDecimal inventory(WmsReportWarehouseDTO wmsReportWarehouseDTO) {
        return this.baseMapper.inventory(wmsReportWarehouseDTO.getWarehouseIds());
    }

    @Override
    public Page<WmsInventoryVO> warehouseList(WmsInventoryPageDTO dto) {
        //获取查询的物料列表
        List<WmsInventory> list = list(new LambdaQueryWrapper<WmsInventory>()
                .like(Objects.nonNull(dto.getMaterialName()),WmsInventory::getMaterialName, dto.getMaterialName())
                .eq(Objects.nonNull(dto.getWarehouseId()),WmsInventory::getWarehouseId, dto.getWarehouseId()));
        List<WmsInventoryVO> inventoryList = summarizationList(list);
        //逻辑分页
        return LogicalPageUtil.page(inventoryList, dto.getCurrent(), dto.getSize());
    }
//    @Override
//    public Map<String, WmsInventory> inventoryMapByCaseNos(Set<String> caseNos) {
//        Map<String, WmsInventory> map = new HashMap<>();
//        if (CollUtil.isNotEmpty(caseNos)) {
//            List<WmsInventory> list = list(new LambdaQueryWrapper<WmsInventory>().in(WmsInventory::getCaseNo, caseNos));
//            if (CollUtil.isNotEmpty(list)) {
//                list.forEach( inventory-> map.put(inventory.getCaseNo(),inventory));
//            }
//        }
//        return map;
//    }


    @Override
    public Page<WmsInventoryVO> shelfList(WmsInventoryPageDTO dto) {
        //获取查询的物料列表
        List<WmsInventory> list = list(new LambdaQueryWrapper<WmsInventory>()
                .like(Objects.nonNull(dto.getMaterialName()),WmsInventory::getMaterialName, dto.getMaterialName())
                .eq(Objects.nonNull(dto.getWarehouseId()),WmsInventory::getWarehouseId, dto.getWarehouseId())
                .like(Objects.nonNull(dto.getShelfName()),WmsInventory::getShelfName, dto.getShelfName()));
        List<WmsInventoryVO> inventoryList = summarizationList(list);
        //逻辑分页
        return LogicalPageUtil.page(inventoryList, dto.getCurrent(), dto.getSize());
    }


    private List<WmsInventoryVO> summarizationList(List<WmsInventory> list) {
        List<WmsInventoryVO> inventoryList = Lists.newArrayList();
        if (CollUtil.isNotEmpty(list)) {
            //按物料编码、仓库分组，总数、可用数、锁定数、在途数、冻结数分别求和
            Map<String, List<WmsInventory>> inventoryMap = list.stream().collect(Collectors.groupingBy(WmsInventory::getMaterialCode));
            inventoryMap.forEach((code, materialList) -> {
                WmsInventoryVO wmsInventoryVO = new WmsInventoryVO();
                AtomicReference<BigDecimal> totalQuantity = new AtomicReference<>(BigDecimal.ZERO);
                AtomicReference<BigDecimal> availableQuantity = new AtomicReference<>(BigDecimal.ZERO);
                AtomicReference<BigDecimal> stockQuantity = new AtomicReference<>(BigDecimal.ZERO);
                AtomicReference<BigDecimal> freezeQuantity = new AtomicReference<>(BigDecimal.ZERO);
                AtomicReference<BigDecimal> lockQuantity = new AtomicReference<>(BigDecimal.ZERO);
                materialList.forEach(inventory -> {
                    BeanUtil.copyProperties(inventory, wmsInventoryVO);
                    totalQuantity.set(BigDecimalUtil.add(totalQuantity.get(), inventory.getTotalQuantity()));
                    availableQuantity.set(BigDecimalUtil.add(availableQuantity.get(), inventory.getAvailableQuantity()));
                    stockQuantity.set(BigDecimalUtil.add(stockQuantity.get(), inventory.getStockQuantity()));
                    freezeQuantity.set(BigDecimalUtil.add(freezeQuantity.get(), inventory.getFreezeQuantity()));
                    lockQuantity.set(BigDecimalUtil.add(lockQuantity.get(), inventory.getLockQuantity()));
                });
                wmsInventoryVO.setTotalQuantity(totalQuantity.get());
                wmsInventoryVO.setAvailableQuantity(availableQuantity.get());
                wmsInventoryVO.setStockQuantity(stockQuantity.get());
                wmsInventoryVO.setFreezeQuantity(stockQuantity.get());
                wmsInventoryVO.setLockQuantity(stockQuantity.get());
                inventoryList.add(wmsInventoryVO);
            });
            //获取单位物料名称
            Set<Long> dict = Sets.newHashSet();
            List<Long> materialIds = Lists.newArrayList();
            inventoryList.forEach(wmsInventoryVO -> {
                dict.add(wmsInventoryVO.getUnit());
                materialIds.add(wmsInventoryVO.getMaterialId());
            });
            Map<Long, EqpSpareVO> materialMap = FeignOptional.of(() -> remoteSpareFeignApi.listById(materialIds), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            Map<Long, String> map = FeignOptional.of(() -> remoteDictFeignApi.listByIdCh(dict), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            inventoryList.forEach(wmsInventoryVO -> {
                wmsInventoryVO.setUnitName(map.get(wmsInventoryVO.getUnit()));
                wmsInventoryVO.setSpecifications(materialMap.get(wmsInventoryVO.getMaterialId()).getSpecifications());
            });
        }
        return inventoryList;
    }


    @Override
    public Map<String, WmsInventory> getInventoryByCaseNo(List<String> caseNo) {
        Map<String, WmsInventory> map = Maps.newHashMap();
        if (CollUtil.isNotEmpty(caseNo)) {
            List<WmsInventory> list = list(new LambdaQueryWrapper<WmsInventory>().in(WmsInventory::getCaseNo, caseNo));
            list.forEach(wmsInventory -> {
                map.put(wmsInventory.getCaseNo(), wmsInventory);
            });
        }
        return map;
    }

    @Override
    public Map<Long, WmsInventory> inventoryIdMap(Set<Long> inventoryIds) {
        Map<Long,WmsInventory> map = Maps.newHashMap();
        if (CollUtil.isNotEmpty(inventoryIds)){
            List<WmsInventory> list = list(new LambdaQueryWrapper<WmsInventory>().in(WmsInventory::getId, inventoryIds));
            list.forEach(wmsInventory -> map.put(wmsInventory.getId(),wmsInventory));
        }
        return map;
    }

    @Override
    public Boolean addInfo(List<WmsInventoryDTO> infoList, String code, Integer warehouseId,Long typeId) {
        if (CollUtil.isNotEmpty(infoList)) {
            //获取库存变动类型
            SysDictVO sysDictVO = FeignOptional.of(() -> remoteDictFeignApi.get(typeId), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            String type = WmsManagerConstant.variableTypeEnum.getTypeByValue(sysDictVO.getName());
            if (Objects.nonNull(type)) {
                SysDictVO incomingType = FeignOptional.of(() -> remoteDictFeignApi.getByCode(type, WmsManagerConstant.VARIATION_TYPE), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
                List<String> list = Lists.newArrayList();
                Set<Integer> officeIds = Sets.newHashSet();
                infoList.forEach(info -> {
                    //根据箱号获取当前的物料是否存在库存信息
                    list.add(info.getCaseNo());
                    officeIds.add(info.getWarehouseId());
                    officeIds.add(info.getShelfId());
                });
                Map<String, WmsInventory> inventoryByCaseNo = getInventoryByCaseNo(list);
                List<WmsInventory> addWmsInventory = Lists.newArrayList();
                Map<Integer, SysOfficeVO> officeMap = FeignOptional.of(() -> remoteOfficeFeign.listById(officeIds), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
                List<WmsInventoryRecord> wmsInventoryRecordList = Lists.newArrayList();
                infoList.forEach(info -> {
                    //库存变动记录
                    WmsInventoryRecord wmsInventoryRecord = new WmsInventoryRecord();
                    wmsInventoryRecord.setMaterialId(info.getMaterialId());
                    wmsInventoryRecord.setMaterialName(info.getMaterialName());
                    wmsInventoryRecord.setType(incomingType.getId());
                    wmsInventoryRecord.setCode(code);
                    wmsInventoryRecord.setWarehouseId(warehouseId);
                    wmsInventoryRecord.setShelfId(info.getShelfId());
                    //变动数量
                    wmsInventoryRecord.setVariableQuantity(info.getQuantity());
                    WmsInventory orDefault = inventoryByCaseNo.get(info.getCaseNo());
                    SysOfficeVO shelfVO = officeMap.get(info.getShelfId());
                    SysOfficeVO warehouseVO = officeMap.get(info.getWarehouseId());
                    info.setWarehouseName(warehouseVO.getOfficeName());
                    info.setShelfCode(shelfVO.getOfficeCode());
                    info.setShelfName(shelfVO.getOfficeName());
                    if (Objects.isNull(orDefault)) {
                        //变动前
                        wmsInventoryRecord.setFrontAvailableQuantity(BigDecimal.ZERO);
                        wmsInventoryRecord.setFrontStockQuantity(BigDecimal.ZERO);
                        wmsInventoryRecord.setFrontLockQuantity(BigDecimal.ZERO);
                        wmsInventoryRecord.setFrontTotalQuantity(BigDecimal.ZERO);
                        //变动后
                        wmsInventoryRecord.setBackAvailableQuantity(BigDecimal.ZERO);
                        wmsInventoryRecord.setBackStockQuantity(info.getQuantity());
                        wmsInventoryRecord.setBackTotalQuantity(BigDecimal.ZERO);
                        wmsInventoryRecord.setBackLockQuantity(BigDecimal.ZERO);
                        WmsInventory inventory = new WmsInventory();
                        BeanUtil.copyProperties(info, inventory);
                        inventory.setStockQuantity(info.getQuantity());
                        addWmsInventory.add(inventory);
                    } else {
                        //变动前
                        wmsInventoryRecord.setFrontAvailableQuantity(orDefault.getAvailableQuantity());
                        wmsInventoryRecord.setFrontStockQuantity(orDefault.getStockQuantity());
                        wmsInventoryRecord.setFrontLockQuantity(orDefault.getLockQuantity());
                        wmsInventoryRecord.setFrontTotalQuantity(orDefault.getTotalQuantity());
                        //变动后
                        wmsInventoryRecord.setBackAvailableQuantity(orDefault.getAvailableQuantity());
                        wmsInventoryRecord.setBackStockQuantity(orDefault.getStockQuantity().add(info.getQuantity()));
                        wmsInventoryRecord.setBackTotalQuantity(orDefault.getTotalQuantity());
                        wmsInventoryRecord.setBackLockQuantity(orDefault.getLockQuantity());
                        orDefault.setStockQuantity(orDefault.getStockQuantity().add(info.getQuantity()));
                        addWmsInventory.add(orDefault);
                    }
                    wmsInventoryRecordList.add(wmsInventoryRecord);
                });
                //保存库存变化记录
                wmsInventoryRecordService.saveBatch(wmsInventoryRecordList);
                saveOrUpdateBatch(addWmsInventory);
            }
        }
        return true;
    }

    private Map<Long, WmsInventory> getInventoryId(List<Long> ids) {
        Map<Long, WmsInventory> map = Maps.newHashMap();
        if (CollUtil.isNotEmpty(ids)) {
            List<WmsInventory> list = list(new LambdaQueryWrapper<WmsInventory>().in(WmsInventory::getId, ids));
            list.forEach(wmsInventory -> {
                map.put(wmsInventory.getId(), wmsInventory);
            });
        }
        return map;
    }
}




