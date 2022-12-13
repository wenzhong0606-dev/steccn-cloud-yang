package com.steccn.wms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.lang.Assert;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.platform.feign.RemoteDictFeignApi;
import com.steccn.platform.feign.RemoteOfficeFeign;
import com.steccn.platform.vo.SysOfficeVO;
import com.steccn.pms.vo.WmsInventoryVO;
import com.steccn.wms.constant.WmsManagerConstant;
import com.steccn.wms.domain.WmsInventory;
import com.steccn.wms.dto.*;
import com.steccn.wms.service.WmsInventoryService;
import com.steccn.wms.mapper.WmsInventoryMapper;
import lombok.RequiredArgsConstructor;
import org.apache.commons.compress.utils.Lists;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author W10
 * @description 针对表【wms_inventory(仓储库存)】的数据库操作Service实现
 * @createDate 2022-12-09 11:18:09
 */
@Service
@RequiredArgsConstructor
public class WmsInventoryServiceImpl extends ServiceImpl<WmsInventoryMapper, WmsInventory>
        implements WmsInventoryService {

    private final RemoteDictFeignApi remoteDictFeignApi;

    private final WmsInventoryMapper wmsInventoryMapper;

    private final RemoteOfficeFeign remoteOfficeFeign;

    @Override
    public Page<WmsInventoryVO> pageList(WmsInventoryPageDTO dto) {
        Page<WmsInventoryVO> page = new Page<>();
        LambdaQueryWrapper<WmsInventory> like = new LambdaQueryWrapper<WmsInventory>()
                .eq(Objects.nonNull(dto.getWarehouseId()), WmsInventory::getWarehouseId, dto.getWarehouseId())
                .like(Objects.nonNull(dto.getMaterialCode()), WmsInventory::getMaterialCode, dto.getMaterialCode())
                .like(Objects.nonNull(dto.getMaterialName()), WmsInventory::getMaterialName, dto.getMaterialName())
                .like(Objects.nonNull(dto.getCaseNo()), WmsInventory::getCaseNo, dto.getCaseNo())
                .like(Objects.nonNull(dto.getBatch()), WmsInventory::getBatch, dto.getBatch());
        Page<WmsInventory> resPage = page(new Page<>(dto.getCurrent(), dto.getSize()), like);
        BeanUtil.copyProperties(resPage, page);
        List<WmsInventoryVO> res = BeanUtil.copyToList(resPage.getRecords(), WmsInventoryVO.class);
        Set<Long> dict = Sets.newHashSet();
        res.forEach(wmsInventoryVO -> dict.add(wmsInventoryVO.getUnit()));
        Map<Long, String> map = FeignOptional.of(remoteDictFeignApi.listByIdCh(dict)).get();
        res.forEach(wmsInventoryVO -> wmsInventoryVO.setUnitName(map.get(wmsInventoryVO.getUnit())));
        page.setRecords(res);
        return page;
    }

    @Override
    public Boolean updateLockQuantity(List<WmsInventoryDTO> infoList, Integer state) {
        Assert.isTrue(CollUtil.isNotEmpty(infoList), "数据异常");
        //自定义sql 修改锁定库存信息
        Boolean aBoolean = null;
        if (WmsManagerConstant.InventoryEnum.ZERO.getCode().equals(state)) {
            aBoolean = this.baseMapper.augmentLockQuantity(infoList);
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
            Map<Integer, SysOfficeVO> officeVOMap = FeignOptional.of(remoteOfficeFeign.listById(officeIds)).get();
            infoList.forEach(info -> {
                WmsInventory orDefault = inventoryByCaseNo.get(info.getCaseNo());
                SysOfficeVO shelfVO = officeVOMap.get(info.getShelfId());
                SysOfficeVO warehouseVO = officeVOMap.get(info.getWarehouseId());
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
    public Boolean restore(List<WmsInventoryDTO> infoList) {
        if (CollUtil.isNotEmpty(infoList)) {
            List<String> list = Lists.newArrayList();
            infoList.forEach(info -> {
                //根据箱号获取当前的物料的库存信息
                list.add(info.getCaseNo());
            });
            Map<String, WmsInventory> inventoryByCaseNo = getInventoryByCaseNo(list);
            WmsInventory wmsInventory = new WmsInventory();
            List<WmsInventory> addWmsInventory = Lists.newArrayList();
            infoList.forEach(info -> {
                WmsInventory orDefault = inventoryByCaseNo.getOrDefault(info.getCaseNo(), wmsInventory);
                orDefault.setStockQuantity(orDefault.getStockQuantity().subtract(info.getQuantity()));
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
        inventory.setFreezeQuantity(inventory.getFreezeQuantity().add(dto.getQuantity()));
        inventory.setAvailableQuantity(inventory.getAvailableQuantity().subtract(dto.getQuantity()));
        return updateById(inventory);
    }

    @Override
    public Boolean unfreeze(WmsInventoryDTO dto) {
        WmsInventory inventory = getById(dto.getId());
        Assert.notNull(inventory, "库存信息不存在");
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
                list.forEach(
                        inventory -> {
                            CaseSiteDTO caseSiteDTO = new CaseSiteDTO();
                            BeanUtil.copyProperties(inventory, caseSiteDTO);
                            map.put(caseSiteDTO.getCaseNo(), caseSiteDTO);
                        }
                );
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
                        }
                );
            }
        }
        return map;
    }


    private Map<String, WmsInventory> getInventoryByCaseNo(List<String> caseNo) {
        Map<String, WmsInventory> map = Maps.newHashMap();
        if (CollUtil.isNotEmpty(caseNo)) {
            List<WmsInventory> list = list(new LambdaQueryWrapper<WmsInventory>().in(WmsInventory::getCaseNo, caseNo));
            list.forEach(wmsInventory -> {
                        map.put(wmsInventory.getCaseNo(), wmsInventory);
                    }
            );
        }
        return map;
    }

    private Map<Long, WmsInventory> getInventoryId(List<Long> ids) {
        Map<Long, WmsInventory> map = Maps.newHashMap();
        if (CollUtil.isNotEmpty(ids)) {
            List<WmsInventory> list = list(new LambdaQueryWrapper<WmsInventory>().in(WmsInventory::getId, ids));
            list.forEach(wmsInventory -> {
                        map.put(wmsInventory.getId(), wmsInventory);
                    }
            );
        }
        return map;
    }
}




