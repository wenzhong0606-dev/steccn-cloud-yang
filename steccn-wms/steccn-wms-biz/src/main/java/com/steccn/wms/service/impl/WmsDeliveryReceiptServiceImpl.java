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
import com.steccn.platform.feign.RemoteDictFeignApi;
import com.steccn.platform.feign.RemoteOfficeFeign;
import com.steccn.wms.constant.WmsManagerConstant;
import com.steccn.wms.domain.*;
import com.steccn.wms.dto.*;
import com.steccn.wms.service.WmsDeliveryReceiptInfoService;
import com.steccn.wms.service.WmsDeliveryReceiptService;
import com.steccn.wms.mapper.WmsDeliveryReceiptMapper;
import com.steccn.wms.service.WmsInventoryService;
import com.steccn.wms.util.WmsCodeUtil;
import com.steccn.wms.vo.WmsDeliveryReceiptInfoVO;
import com.steccn.wms.vo.WmsDeliveryReceiptVO;
import lombok.RequiredArgsConstructor;
import org.apache.commons.compress.utils.Lists;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

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
                    BeanUtil.copyProperties(info,wmsDeliveryReceiptInfo);
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
            if (CollUtil.isNotEmpty(dto.getList())) {
                List<WmsDeliveryReceiptInfo> wmsDeliveryReceiptInfoList = Lists.newArrayList();
                List<WmsInventoryDTO> addList = Lists.newArrayList();
                List<WmsInventoryDTO> updateList = Lists.newArrayList();
                List<Long> ids = Lists.newArrayList();
                List<Long> updateIds = Lists.newArrayList();
                dto.getList().forEach(info -> {
                    WmsDeliveryReceiptInfo wmsDeliveryReceiptInfo = new WmsDeliveryReceiptInfo();
                    BeanUtil.copyProperties(info, wmsDeliveryReceiptInfo);
                    WmsInventoryDTO wmsInventoryDTO = new WmsInventoryDTO();
                    BeanUtil.copyProperties(info,wmsInventoryDTO);
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
        infoVOList.forEach(info-> typeSet.add(info.getUnit()));
        Map<Integer, String> officeMap = FeignOptional.of(remoteOfficeFeign.listByIdCh(officeSet)).get();
        Map<Long, String> dictMap = FeignOptional.of(remoteDictFeignApi.listByIdCh(typeSet)).get();
        infoVOList.forEach(info->info.setUnitName(dictMap.get(info.getUnit())));
        wmsDeliveryReceiptVO.setWarehouseName(officeMap.get(wmsDeliveryReceiptVO.getWarehouseId()));
        wmsDeliveryReceiptVO.setTypeName(dictMap.get(wmsDeliveryReceiptVO.getType()));
        wmsDeliveryReceiptVO.setList(infoVOList);
        return wmsDeliveryReceiptVO;
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




