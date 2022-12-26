package com.steccn.wms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Sets;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.authserver.util.SecurityUtils;
import com.steccn.common.constant.LockConstant;
import com.steccn.common.constant.MpConstant;
import com.steccn.common.vo.R;
import com.steccn.platform.feign.RemoteDictFeignApi;
import com.steccn.platform.feign.RemoteOfficeFeign;
import com.steccn.platform.vo.SysDictVO;
import com.steccn.wms.constant.WmsManagerConstant;
import com.steccn.wms.domain.WmsDeliveryReceipt;
import com.steccn.wms.domain.WmsDeliveryReceiptInfo;
import com.steccn.wms.domain.WmsInventory;
import com.steccn.wms.dto.WmsDeliveryReceiptDTO;
import com.steccn.wms.dto.WmsDeliveryReceiptInfoDTO;
import com.steccn.wms.dto.WmsDeliveryReceiptPageDTO;
import com.steccn.wms.dto.WmsInventoryDTO;
import com.steccn.wms.mapper.WmsDeliveryReceiptMapper;
import com.steccn.wms.service.WmsDeliveryReceiptInfoService;
import com.steccn.wms.service.WmsDeliveryReceiptService;
import com.steccn.wms.service.WmsInventoryService;
import com.steccn.wms.util.WmsCodeUtil;
import com.steccn.wms.vo.WmsDeliveryReceiptInfoVO;
import com.steccn.wms.vo.WmsDeliveryReceiptVO;
import com.steccn.wms.vo.WmsScanInventoryVO;
import lombok.RequiredArgsConstructor;
import org.apache.commons.compress.utils.Lists;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author W10
 * @description 针对表【wms_delivery_receipt(出库单)】的数据库操作Service实现
 * @createDate 2022-12-09 11:18:09
 */
@Service
@RequiredArgsConstructor
public class WmsDeliveryReceiptServiceImpl extends ServiceImpl<WmsDeliveryReceiptMapper, WmsDeliveryReceipt>
        implements WmsDeliveryReceiptService {


    private final RemoteOfficeFeign remoteOfficeFeign;

    private final RemoteDictFeignApi remoteDictFeignApi;

    private final RedissonClient redissonClient;

    private final WmsInventoryService wmsInventoryService;

    private final WmsDeliveryReceiptInfoService wmsDeliveryReceiptInfoService;

    @Override
    public Page<WmsDeliveryReceiptVO> pageList(WmsDeliveryReceiptPageDTO dto) {
        //1.处理筛选条件时间格式
        if (Objects.nonNull(dto.getAffirmStartDate())) {
            dto.setAffirmStartTime(LocalDateTime.of(dto.getAffirmStartDate(), LocalTime.MIN));
        }
        if (Objects.nonNull(dto.getAffirmEndDate())) {
            dto.setAffirmEndTime(LocalDateTime.of(dto.getAffirmEndDate(), LocalTime.MAX));
        }
        if (Objects.nonNull(dto.getCreateStartDate())) {
            dto.setCreateStartTime(LocalDateTime.of(dto.getCreateStartDate(), LocalTime.MIN));
        }
        if (Objects.nonNull(dto.getCreateEndDate())) {
            dto.setCreateEndTime(LocalDateTime.of(dto.getCreateEndDate(), LocalTime.MAX));
        }
        //2.自定义sql查询订单信息列表
        Page<WmsDeliveryReceiptVO> page = this.baseMapper.pageList(new Page(dto.getCurrent(), dto.getSize()), dto);
        //3.处理数据
        List<WmsDeliveryReceiptVO> records = page.getRecords();
        if (CollUtil.isNotEmpty(records)) {
            Set<Long> typeSet = Sets.newHashSet();
            Set<Integer> officeSet = Sets.newHashSet();
            records.forEach(receipt -> {
                typeSet.add(receipt.getType());
                officeSet.add(receipt.getWarehouseId());
            });
            Map<Integer, String> officeMap = FeignOptional.of(remoteOfficeFeign.listByIdCh(officeSet)).get();
            Map<Long, String> dictMap = FeignOptional.of(remoteDictFeignApi.listByIdCh(typeSet)).get();
            records.forEach(receipt -> {
                receipt.setTypeName(dictMap.get(receipt.getType()));
                receipt.setWarehouseName(officeMap.get(receipt.getWarehouseId()));
            });
        }
        //返回数据
        return page;
    }

    @Override
    public Boolean add(WmsDeliveryReceiptDTO dto) {
        RLock lock = redissonClient.getLock(LockConstant.TABLE_WMS_DELIVERY_RECEIPT);
        lock.lock();
        try {
            //1.校验数据是否存在
            Assert.isTrue(Objects.nonNull(dto), "出库凭证信息为空");
            //2.定义数据模型
            WmsDeliveryReceipt model = new WmsDeliveryReceipt();
            BeanUtil.copyProperties(dto, model);
            //3.给当前出库单生成编码
            model.setCode(WmsCodeUtil.getProduceOrderCode(WmsCodeUtil.PREFIX_WMS_DELIVERY_RECEIPT, WmsCodeUtil.NUM_BIT_4, currentMaxCode()));
            model.setStatus(WmsManagerConstant.OrderStateEnum.ZERO.getCode());
            //4.保存出库单信息
            save(model);
            //5.保存出库单明细信息
            if (CollUtil.isNotEmpty(dto.getList())) {
                List<WmsInventoryDTO> infoList = Lists.newArrayList();
                List<WmsDeliveryReceiptInfo> receiptInfoList = Lists.newArrayList();
                dto.getList().forEach(info -> {
                    WmsInventoryDTO wmsInventoryDTO = new WmsInventoryDTO();
                    BeanUtil.copyProperties(info, wmsInventoryDTO);
                    wmsInventoryDTO.setId(info.getInventoryId());
                    infoList.add(wmsInventoryDTO);
                    WmsDeliveryReceiptInfo wmsDeliveryReceiptInfo = new WmsDeliveryReceiptInfo();
                    BeanUtil.copyProperties(info, wmsDeliveryReceiptInfo);
                    wmsDeliveryReceiptInfo.setDeliveryReceiptId(model.getId());
                    receiptInfoList.add(wmsDeliveryReceiptInfo);
                });
                //调整锁定库存信息
                wmsInventoryService.updateLockQuantity(infoList, WmsManagerConstant.InventoryEnum.ZERO.getCode());
                //保存明细信息
                wmsDeliveryReceiptInfoService.saveBatch(receiptInfoList);
            }
        } finally {
            lock.unlock();
        }
        return true;
    }

    @Override
    public Boolean edit(WmsDeliveryReceiptDTO dto) {
        RLock lock = redissonClient.getLock(LockConstant.TABLE_WMS_DELIVERY_RECEIPT);
        lock.lock();
        try {
            //1.校验数据是否存在
            Assert.isTrue(Objects.nonNull(dto), "出库凭证信息为空");
            //2.定义数据模型
            WmsDeliveryReceipt model = new WmsDeliveryReceipt();
            BeanUtil.copyProperties(dto, model);
            //3.修改
            updateById(model);
            //3.1 修改详情数据
            if (Objects.nonNull(dto.getList())) {
                List<WmsDeliveryReceiptInfo> wmsDeliveryReceiptInfoList = Lists.newArrayList();
                List<WmsInventoryDTO> addList = Lists.newArrayList();
                List<WmsInventoryDTO> updateList = Lists.newArrayList();
                List<Long> ids = Lists.newArrayList();
                List<Long> updateIds = Lists.newArrayList();
                dto.getList().forEach(info -> {
                    WmsDeliveryReceiptInfo wmsDeliveryReceiptInfo = new WmsDeliveryReceiptInfo();
                    BeanUtil.copyProperties(info, wmsDeliveryReceiptInfo);
                    WmsInventoryDTO wmsInventoryDTO = new WmsInventoryDTO();
                    BeanUtil.copyProperties(info, wmsInventoryDTO);
                    wmsInventoryDTO.setId(info.getInventoryId());
                    if (Objects.isNull(info.getId())) {
                        wmsDeliveryReceiptInfo.setDeliveryReceiptId(model.getId());
                        addList.add(wmsInventoryDTO);
                    } else {
                        ids.add(info.getId());
                        updateIds.add(info.getId());
                        updateList.add(wmsInventoryDTO);
                    }
                    wmsDeliveryReceiptInfoList.add(wmsDeliveryReceiptInfo);
                });
                LambdaQueryWrapper<WmsDeliveryReceiptInfo> wrapper = new LambdaQueryWrapper<WmsDeliveryReceiptInfo>()
                        .eq(WmsDeliveryReceiptInfo::getDeliveryReceiptId, model.getId())
                        .notIn(CollUtil.isNotEmpty(ids), WmsDeliveryReceiptInfo::getId, ids);
                //获取需要删除的数据
                List<WmsDeliveryReceiptInfo> list = wmsDeliveryReceiptInfoService.list(wrapper);
                if (CollUtil.isNotEmpty(updateList)) {
                    //获取原始数量
                    Map<Long, BigDecimal> originalQuantityMap = wmsDeliveryReceiptInfoService.originalQuantity(updateIds);
                    updateList.forEach(inventory -> {
                        inventory.setOriginalQuantity(originalQuantityMap.get(inventory.getId()));
                    });
                    wmsInventoryService.editLockQuantity(updateList);
                }
                if (CollUtil.isNotEmpty(list)) {
                    List<WmsInventoryDTO> deleteList = Lists.newArrayList();
                    list.forEach(info -> {
                        WmsInventoryDTO wmsInventoryDTO = new WmsInventoryDTO();
                        wmsInventoryDTO.setId(info.getInventoryId());
                        wmsInventoryDTO.setQuantity(info.getQuantity());
                        deleteList.add(wmsInventoryDTO);
                    });
                    wmsInventoryService.restoreLockQuantity(deleteList);
                    wmsDeliveryReceiptInfoService.remove(wrapper);
                }
                if (CollUtil.isNotEmpty(addList)) {
                    wmsInventoryService.addLockQuantity(addList);
                }

                wmsDeliveryReceiptInfoService.saveOrUpdateBatch(wmsDeliveryReceiptInfoList);
            }
        } finally {
            lock.unlock();
        }
        return true;
    }

    @Override
    public Boolean affirm(Long id) {
        //1.查询出库单
        WmsDeliveryReceipt receipt = getById(id);
        //2.判断单状态
        Assert.isTrue(WmsManagerConstant.OrderStateEnum.ZERO.getCode().equals(receipt.getStatus())
                , "当前状态不可确认");
        //3.确认
        //3.1获取该单据的信息
        List<WmsDeliveryReceiptInfo> list = wmsDeliveryReceiptInfoService.list(new LambdaQueryWrapper<WmsDeliveryReceiptInfo>()
                .eq(WmsDeliveryReceiptInfo::getDeliveryReceiptId, id));
        if (CollUtil.isNotEmpty(list)) {
            //解锁改该单冻结的库存，并调整库存数量修改数量
            List<WmsInventoryDTO> inventoryDTOList = Lists.newArrayList();
            list.forEach(info -> {
                WmsInventoryDTO wmsInventoryDTO = new WmsInventoryDTO();
                wmsInventoryDTO.setId(info.getInventoryId());
                wmsInventoryDTO.setQuantity(info.getQuantity());
                inventoryDTOList.add(wmsInventoryDTO);
            });
            wmsInventoryService.affirmLockQuantity(inventoryDTOList);
        }
        //4.修改确认单状态
        receipt.setAffirmName(SecurityUtils.getUser().getName());
        receipt.setAffirmTime(LocalDateTime.now());
        receipt.setStatus(WmsManagerConstant.OrderStateEnum.ONE.getCode());
        return updateById(receipt);
    }

    @Override
    public Boolean delete(Long id) {
        WmsDeliveryReceipt order = getById(id);
        Assert.notNull(order, "该单不存在");
        Assert.isTrue(WmsManagerConstant.OrderStateEnum.ZERO.getCode().equals(order.getStatus())
                , "当前状态不可删除");
        //修改状态为已删除
        order.setStatus(WmsManagerConstant.OrderStateEnum.FOUR.getCode());
        //获取订单的明细
        List<WmsDeliveryReceiptInfo> list = wmsDeliveryReceiptInfoService.list(new LambdaQueryWrapper<WmsDeliveryReceiptInfo>()
                .eq(WmsDeliveryReceiptInfo::getDeliveryReceiptId, id));
        if (CollUtil.isNotEmpty(list)) {
            //还原库存
            List<Long> ids = Lists.newArrayList();
            list.forEach(info -> ids.add(info.getId()));
            List<WmsInventoryDTO> deleteList = Lists.newArrayList();
            list.forEach(info -> {
                WmsInventoryDTO wmsInventoryDTO = new WmsInventoryDTO();
                wmsInventoryDTO.setQuantity(info.getQuantity());
                wmsInventoryDTO.setId(info.getInventoryId());
                deleteList.add(wmsInventoryDTO);
            });
            wmsInventoryService.restoreLockQuantity(deleteList);
        }
        return updateById(order);
    }

    @Override
    public WmsDeliveryReceiptVO get(Long id) {
        WmsDeliveryReceipt order = getById(id);
        Assert.notNull(order, "该出库单不存在");
        //获取订单的明细
        List<WmsDeliveryReceiptInfoVO> infoVOList = wmsDeliveryReceiptInfoService.infoList(id);
        //处理数据
        WmsDeliveryReceiptVO wmsDeliveryReceiptVO = new WmsDeliveryReceiptVO();
        BeanUtil.copyProperties(order, wmsDeliveryReceiptVO);
        Set<Long> typeSet = Sets.newHashSet();
        Set<Integer> officeSet = Sets.newHashSet();
        typeSet.add(wmsDeliveryReceiptVO.getType());
        officeSet.add(wmsDeliveryReceiptVO.getWarehouseId());
        infoVOList.forEach(info -> typeSet.add(info.getUnit()));
        Map<Integer, String> officeMap = FeignOptional.of(remoteOfficeFeign.listByIdCh(officeSet)).get();
        Map<Long, String> dictMap = FeignOptional.of(remoteDictFeignApi.listByIdCh(typeSet)).get();
        infoVOList.forEach(info -> info.setUnitName(dictMap.get(info.getUnit())));
        wmsDeliveryReceiptVO.setWarehouseName(officeMap.get(wmsDeliveryReceiptVO.getWarehouseId()));
        wmsDeliveryReceiptVO.setTypeName(dictMap.get(wmsDeliveryReceiptVO.getType()));
        wmsDeliveryReceiptVO.setList(infoVOList);
        return wmsDeliveryReceiptVO;
    }

    @Override
    public Boolean generateDelivery(WmsDeliveryReceiptDTO dto) {
        //1.校验库存信息是否变化
        if (CollUtil.isNotEmpty(dto.getList())) {
            if (judgeInventory(dto.getList())) {
                //库存发生变化，不生成单据
                return Boolean.FALSE;
            } else {
                //保存出库单
                return add(dto);
            }
        }
        return Boolean.TRUE;
    }



    /**
     * 判断物料列表中的物料库存信息是否变化
     * changeFlag = true 发生变化 反之没变
     *
     * @param materialList
     * @return
     */
    private Boolean judgeInventory(List<WmsDeliveryReceiptInfoDTO> materialList) {
        //定义变更标识
        Boolean changeFlag = false;
        if (CollUtil.isNotEmpty(materialList)) {
            //批量查询物料的库存信息
            List<Long> inventoryId = Lists.newArrayList();
            materialList.forEach(materialDTO -> {
                inventoryId.add(materialDTO.getInventoryId());
            });
            List<WmsInventory> list = wmsInventoryService.list(new LambdaQueryWrapper<WmsInventory>().in(WmsInventory::getId,inventoryId));

            for (WmsInventory wmsInventory : list) {
                for (WmsDeliveryReceiptInfoDTO wmsDeliveryReceiptInfoDTO : materialList) {
                    if (wmsInventory.getId().equals(wmsDeliveryReceiptInfoDTO.getInventoryId())) {
                        if (wmsDeliveryReceiptInfoDTO.getAvailableQuantity().compareTo(wmsInventory.getAvailableQuantity()) != 0) {
                            changeFlag = true;
                        }
                    }
                }
            }
        }
        return changeFlag;
    }

    @Override
    public R<WmsScanInventoryVO> getCaseNoByScanInventoryInfo(String caseNo) {
        WmsScanInventoryVO wmsScanInventoryVO = new WmsScanInventoryVO();
        //根据箱号获取对应库存信息
        LambdaQueryWrapper<WmsInventory> inventoryLambdaQueryWrapper = new LambdaQueryWrapper<>();
        inventoryLambdaQueryWrapper.eq(WmsInventory::getCaseNo,caseNo);
        WmsInventory wmsInventory = wmsInventoryService.getOne(inventoryLambdaQueryWrapper,false);
        if(Objects.isNull(wmsInventory) ) return R.failure("未查询到此箱号信息，请联系管理员");
        //检索当前箱号是否已出库
        LambdaQueryWrapper<WmsDeliveryReceiptInfo> receiptInfoLambdaQueryWrapper = new LambdaQueryWrapper<>();
        receiptInfoLambdaQueryWrapper.eq(WmsDeliveryReceiptInfo::getInventoryId,wmsInventory.getId());
        WmsDeliveryReceiptInfo wmsDeliveryReceiptInfo = wmsDeliveryReceiptInfoService.getOne(receiptInfoLambdaQueryWrapper,false);
        if(Objects.nonNull(wmsDeliveryReceiptInfo)){
            WmsDeliveryReceipt wmsDeliveryReceipt = this.baseMapper.selectById(wmsDeliveryReceiptInfo.getDeliveryReceiptId());
            BeanUtils.copyProperties(wmsInventory,wmsScanInventoryVO);
            wmsScanInventoryVO.setStatus(wmsDeliveryReceipt.getStatus());
            wmsScanInventoryVO.setInventoryId(wmsInventory.getId());
            List<Long> units = new ArrayList<>();
            units.add(wmsInventory.getUnit());
            R<Map<Long, SysDictVO>> sysDicts = remoteDictFeignApi.listById(units);
            if(sysDicts.getData().size()>0) wmsScanInventoryVO.setUnitName(sysDicts.getData().get(wmsInventory.getUnit()).getName());
            wmsScanInventoryVO.setDeliveryReceiptCode(wmsDeliveryReceipt.getCode());
            wmsScanInventoryVO.setDeliveryReceiptTime(wmsDeliveryReceipt.getAffirmTime());
            return R.success(wmsScanInventoryVO);
        }
        if(wmsInventory.getAvailableQuantity().compareTo(BigDecimal.ZERO) <= 0 ) return R.failure("可用数量不足，请联系管理员");
        BeanUtils.copyProperties(wmsInventory,wmsScanInventoryVO);
        wmsScanInventoryVO.setInventoryId(wmsInventory.getId());
        return R.success(wmsScanInventoryVO);
    }

    @Override
    public R<String> scanDeliveryReceipt(Map<String, Object> paramMap) {
        String result ="";
        //类型
        Long type = paramMap.containsKey("type") ? (paramMap.get("type") == null ? null :
                Long.parseLong(paramMap.get("type").toString())) : null;
        if(Objects.isNull(type)) return R.failure("请选择类型");
        List<Map<String, Object>> productList = paramMap.containsKey("inventoryList") ? (List<Map<String,
                Object>>) paramMap.get("inventoryList") : null;
        Map<Long, List<Map<String, Object>>> warehouseMap =
                productList.stream().collect(Collectors.groupingBy(e -> Long.valueOf(e.get("warehouseId").toString())));
        for (Map.Entry<Long, List<Map<String, Object>>> moduleEntry : warehouseMap.entrySet()) {
            List<Map<String, Object>> inventoryList = moduleEntry.getValue();
            //保存出库信息
            WmsDeliveryReceipt wmsDeliveryReceipt = new WmsDeliveryReceipt();
            wmsDeliveryReceipt.setCode(WmsCodeUtil.getProduceOrderCode(WmsCodeUtil.PREFIX_WMS_DELIVERY_RECEIPT, WmsCodeUtil.NUM_BIT_4, currentMaxCode()));
            wmsDeliveryReceipt.setType(type);
            wmsDeliveryReceipt.setStatus(WmsManagerConstant.OrderStateEnum.ONE.getCode());
            wmsDeliveryReceipt.setWarehouseId(moduleEntry.getKey());
            wmsDeliveryReceipt.setAffirmTime(LocalDateTime.now());
            wmsDeliveryReceipt.setAffirmName(SecurityUtils.getUser().getName());
            this.baseMapper.insert(wmsDeliveryReceipt);
            for (Map<String, Object> item : inventoryList) {
                //保存出库明细
                Long inventoryId = Long.parseLong(item.get("inventoryId").toString());//库存主键
                BigDecimal availableQuantity = new BigDecimal(item.get("availableQuantity").toString());//数量
                WmsDeliveryReceiptInfo wmsDeliveryReceiptInfo = new WmsDeliveryReceiptInfo();
                wmsDeliveryReceiptInfo.setDeliveryReceiptId(wmsDeliveryReceipt.getId());
                wmsDeliveryReceiptInfo.setMaterialId(Long.parseLong(item.get("materialId").toString()));
                wmsDeliveryReceiptInfo.setInventoryId(inventoryId);
                wmsDeliveryReceiptInfo.setQuantity(availableQuantity);
                wmsDeliveryReceiptInfoService.save(wmsDeliveryReceiptInfo);
                //更新库存
                WmsInventory wmsInventory = wmsInventoryService.getById(inventoryId);
                wmsInventory.setTotalQuantity(wmsInventory.getTotalQuantity().subtract(availableQuantity));
                wmsInventory.setAvailableQuantity(wmsInventory.getAvailableQuantity().subtract(availableQuantity));
                wmsInventoryService.updateById(wmsInventory);
            }
            result = result+wmsDeliveryReceipt.getCode()+",";
        }
        if(StringUtils.isNotBlank(result)) result = result.substring(0,result.length() -1);
        return R.success(result,null);
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
        WmsDeliveryReceipt maxCodeModel = getOne(new LambdaQueryWrapper<WmsDeliveryReceipt>()
                .between(WmsDeliveryReceipt::getCreateTime, min, max)
                .orderByDesc(WmsDeliveryReceipt::getCode)
                .last(MpConstant.STR_LIMIT_1));
        //3.如果最大code不为空
        if (Objects.nonNull(maxCodeModel)) {
            try {
                //4.解析数值部分
                String substringVal = maxCodeModel.getCode().substring(WmsCodeUtil.PREFIX_WMS_DELIVERY_RECEIPT.length() + WmsCodeUtil.NUM_BIT_12);
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




