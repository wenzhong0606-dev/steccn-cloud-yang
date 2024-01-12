package com.steccn.wms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.steccn.authserver.exception.BusinessException;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.authserver.util.SecurityUtils;
import com.steccn.common.constant.FeignConstant;
import com.steccn.common.constant.LockConstant;
import com.steccn.common.constant.MpConstant;
import com.steccn.common.dto.QtSeriesDataDTO;
import com.steccn.common.util.BigDecimalUtil;
import com.steccn.common.util.LocalDateUtil;
import com.steccn.common.vo.EpChartGraphVO;
import com.steccn.mes.dto.ErpCommonSaveDTO;
import com.steccn.mes.dto.ErpTransferSaveDetailDTO;
import com.steccn.mes.feign.RemoteErpFeignApi;
import com.steccn.oa.constant.OaConstant;
import com.steccn.platform.feign.RemoteDictFeignApi;
import com.steccn.platform.feign.RemoteOfficeFeign;
import com.steccn.platform.feign.RemoteSpareFeignApi;
import com.steccn.platform.util.LogicalPageUtil;
import com.steccn.platform.vo.EqpSpareVO;
import com.steccn.platform.vo.SysDictVO;
import com.steccn.platform.vo.SysOfficeVO;
import com.steccn.pms.feign.RemoteMessFeignApi;
import com.steccn.pms.vo.ClassificationVO;
import com.steccn.wms.constant.WmsManagerConstant;
import com.steccn.wms.domain.*;
import com.steccn.wms.dto.*;
import com.steccn.wms.service.WmsInventoryRecordService;
import com.steccn.wms.service.WmsInventoryService;
import com.steccn.wms.service.WmsTransferSlipInfoService;
import com.steccn.wms.service.WmsTransferSlipService;
import com.steccn.wms.mapper.WmsTransferSlipMapper;
import com.steccn.wms.util.WmsCodeUtil;
import com.steccn.wms.vo.WmsReportWarehouseVO;
import com.steccn.wms.vo.WmsStorageReceiptInfoVO;
import com.steccn.wms.vo.WmsTransferSlipInfoVO;
import com.steccn.wms.vo.WmsTransferSlipVO;
import lombok.RequiredArgsConstructor;
import org.apache.commons.compress.utils.Lists;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author W10
 * @description 针对表【wms_transfer_slip(调拨单)】的数据库操作Service实现
 * @createDate 2022-11-30 09:45:17
 */
@Service
@RequiredArgsConstructor
public class WmsTransferSlipServiceImpl extends ServiceImpl<WmsTransferSlipMapper, WmsTransferSlip> implements WmsTransferSlipService {

    private final RedissonClient redissonClient;

    private final WmsTransferSlipInfoService wmsTransferSlipInfoService;

    private final RemoteOfficeFeign remoteOfficeFeign;

    private final RemoteDictFeignApi remoteDictFeignApi;

    private final RemoteErpFeignApi remoteErpFeignApi;

    private final WmsInventoryService wmsInventoryService;

    private final WmsInventoryRecordService wmsInventoryRecordService;

    private final RemoteMessFeignApi remoteMessFeignApi;

    private final RemoteSpareFeignApi remoteSpareFeignApi;


    /**
     * 调拨单分页内容
     *
     * @param dto
     * @return
     */
    @Override
    public Page<WmsTransferSlipVO> pageList(WmsTransferSlipPageDTO dto) {
        //1.处理时间格式
        //计划开始时间-开始
        if (Objects.nonNull(dto.getCreateStartDate())) {
            dto.setCreateStartTime(LocalDateTime.of(dto.getCreateStartDate(), LocalTime.MIN));
        }
        //计划开始时间-结束
        if (Objects.nonNull(dto.getCreateEndDate())) {
            dto.setCreateEndTime(LocalDateTime.of(dto.getCreateEndDate(), LocalTime.MAX));
        }
        //计划结束时间-开始
        if (Objects.nonNull(dto.getAffirmStartDate())) {
            dto.setAffirmStartTime(LocalDateTime.of(dto.getAffirmStartDate(), LocalTime.MIN));
        }
        //计划结束时间-结束
        if (Objects.nonNull(dto.getAffirmEndDate())) {
            dto.setAffirmEndTime(LocalDateTime.of(dto.getAffirmEndDate(), LocalTime.MAX));
        }
        //2.自定义sql查询订单信息列表
        Page<WmsTransferSlipVO> page = this.baseMapper.pageList(new Page(dto.getCurrent(), dto.getSize()), dto);
        //3.处理数据
        Set<Integer> idSet = Sets.newHashSet();
        Set<Long> typeSet = Sets.newHashSet();
        page.getRecords().forEach(wmsTransferSlipVO -> {
            idSet.add(wmsTransferSlipVO.getSourceWarehouse());
            idSet.add(wmsTransferSlipVO.getTargetWarehouse());
            typeSet.add(wmsTransferSlipVO.getType());
        });
        Map<Integer, String> officeMap = FeignOptional.of(() -> remoteOfficeFeign.listByIdCh(idSet), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        Map<Long, String> dictMap = FeignOptional.of(() -> remoteDictFeignApi.listByIdCh(typeSet), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        page.getRecords().forEach(wmsTransferSlipVO -> {
            wmsTransferSlipVO.setTypeName(dictMap.get(wmsTransferSlipVO.getType()));
            wmsTransferSlipVO.setSourceWarehouseName(officeMap.get(wmsTransferSlipVO.getSourceWarehouse()));
            wmsTransferSlipVO.setTargetWarehouseName(officeMap.get(wmsTransferSlipVO.getTargetWarehouse()));
        });
        return page;
    }

    /**
     * 调拨单新增
     *
     * @param dto
     * @return
     */
    @Override
    @Transactional(readOnly = false)
    public Boolean add(WmsTransferSlipDTO dto) {
        RLock lock = redissonClient.getLock(LockConstant.TABLE_WMS_TRANSFER_SLIP);
        lock.lock();
        try {
            //1.校验数据是否存在
            Assert.isTrue(Objects.nonNull(dto), "调拨单信息为空");
            //2.定义数据模型
            WmsTransferSlip model = new WmsTransferSlip();
            BeanUtil.copyProperties(dto, model);
            //3.给当前调拨单生成编码
            model.setCode(WmsCodeUtil.getProduceOrderCode(WmsCodeUtil.PREFIX_WMS_TRANSFER_SLIP, WmsCodeUtil.NUM_BIT_4, currentMaxCode()));
            model.setStatus(WmsManagerConstant.OrderStateEnum.ZERO.getCode());
            //4.保存调拨单信息
            save(model);
            //5.保存调拨单明细信息
            if (CollUtil.isNotEmpty(dto.getList())) {
                List<WmsTransferSlipInfo> infoList = Lists.newArrayList();
                List<WmsInventoryDTO> sourceList = Lists.newArrayList();
                List<WmsInventoryDTO> targetList = Lists.newArrayList();
                //获取箱号规则
                Long caseNo = getCaseNo(model.getCode(), model.getId());

                List<WmsTransferSlipInfoDTO> list = dto.getList();
                for (WmsTransferSlipInfoDTO info : list) {
                    WmsTransferSlipInfo wmsTransferSlipInfo = new WmsTransferSlipInfo();
                    BeanUtil.copyProperties(info, wmsTransferSlipInfo);
                    wmsTransferSlipInfo.setTransferSlipId(model.getId());

                    WmsInventoryDTO source = new WmsInventoryDTO();
                    BeanUtil.copyProperties(info, source);
                    source.setId(info.getSourceInventoryId());
                    source.setWarehouseId(model.getSourceWarehouse());
                    source.setShelfId(info.getSourceShelf());
                    sourceList.add(source);
                    WmsInventoryDTO target = new WmsInventoryDTO();
                    BeanUtil.copyProperties(info, target);
                    //生成箱号
                    caseNo = ++caseNo;
                    target.setCaseNo(model.getCode() + WmsCodeUtil.SHORT_LINE + caseNo);
                    target.setShelfId(info.getTargetShelf());
                    target.setWarehouseId(model.getTargetWarehouse());
                    targetList.add(target);
                    wmsTransferSlipInfo.setTargetCaseNo(target.getCaseNo());
                    infoList.add(wmsTransferSlipInfo);
                }
                wmsTransferSlipInfoService.saveBatch(infoList);
                //增加库存中原始占用库存数量,减少可用库存
                wmsInventoryService.addLockQuantity(sourceList);
                //增加目标货架在途库存数量
                wmsInventoryService.add(targetList);
            }
        } finally {
            lock.unlock();
        }
        return true;
    }

    /**
     * 调拨单编辑
     *
     * @param dto
     * @return
     */
    @Override
    @Transactional(readOnly = false)
    public Boolean edit(WmsTransferSlipDTO dto) {
        RLock lock = redissonClient.getLock(LockConstant.TABLE_WMS_TRANSFER_SLIP);
        lock.lock();
        try {
            //1.校验数据是否存在
            Assert.isTrue(Objects.nonNull(dto), "调拨单信息为空");
            //2.定义数据模型
            WmsTransferSlip model = new WmsTransferSlip();
            BeanUtil.copyProperties(dto, model);
            //3.修改
            updateById(model);
            //3.1 修改详情数据
            if (Objects.nonNull(dto.getList())) {
                Set<Long> ids = Sets.newHashSet();
                List<WmsTransferSlipInfo> list = Lists.newArrayList();
                List<WmsTransferSlipInfoDTO> addList = Lists.newArrayList();
                List<WmsTransferSlipInfoDTO> updateList = Lists.newArrayList();
                //获取箱号规则
                Long caseNo = getCaseNo(model.getCode(), model.getId());
                List<WmsTransferSlipInfoDTO> dtoList = dto.getList();
                for (WmsTransferSlipInfoDTO info : dtoList) {
                    WmsTransferSlipInfo wmsTransferSlipInfo = new WmsTransferSlipInfo();
                    BeanUtil.copyProperties(info, wmsTransferSlipInfo);
                    ids.add(info.getId());
                    if (Objects.nonNull(info.getId())) {
                        updateList.add(info);
                    } else {
                        wmsTransferSlipInfo.setTransferSlipId(model.getId());
                        addList.add(info);
                    }
                }
                LambdaQueryWrapper<WmsTransferSlipInfo> wrapper = new LambdaQueryWrapper<WmsTransferSlipInfo>().eq(WmsTransferSlipInfo::getTransferSlipId, dto.getId()).notIn(CollUtil.isNotEmpty(ids), WmsTransferSlipInfo::getId, ids);
                //获取需要删除的数据
                List<WmsTransferSlipInfo> deleteList = wmsTransferSlipInfoService.list(wrapper);
                if (CollUtil.isNotEmpty(deleteList)) {
                    List<WmsInventoryDTO> sourceList = Lists.newArrayList();
                    List<WmsInventoryDTO> targetList = Lists.newArrayList();
                    deleteList.forEach(wmsTransferSlipInfo -> {
                        WmsInventoryDTO source = new WmsInventoryDTO();
                        BeanUtil.copyProperties(wmsTransferSlipInfo, source);
                        source.setId(wmsTransferSlipInfo.getSourceInventoryId());
                        source.setWarehouseId(model.getSourceWarehouse());
                        source.setShelfId(wmsTransferSlipInfo.getSourceShelf());
                        WmsInventoryDTO target = new WmsInventoryDTO();
                        BeanUtil.copyProperties(wmsTransferSlipInfo, target);
                        target.setShelfId(wmsTransferSlipInfo.getTargetShelf());
                        target.setWarehouseId(model.getTargetWarehouse());
                        target.setOriginalCaseNo(wmsTransferSlipInfo.getCaseNo());
                        target.setOriginalQuantity(wmsTransferSlipInfo.getQuantity());
                        sourceList.add(source);
                        targetList.add(target);
                    });
                    //去除源库存信息（增加可用库存，减少占用库存）
                    wmsInventoryService.restoreLockQuantity(sourceList);
                    //去除目标库存信息(减少在途库存)
                    wmsInventoryService.restore(targetList);
                    //删除数据
                    wmsTransferSlipInfoService.remove(wrapper);
                }
                if (CollUtil.isNotEmpty(addList)) {
                    List<WmsInventoryDTO> sourceList = Lists.newArrayList();
                    List<WmsInventoryDTO> targetList = Lists.newArrayList();
                    for (WmsTransferSlipInfoDTO info : addList) {
                        WmsInventoryDTO source = new WmsInventoryDTO();
                        BeanUtil.copyProperties(info, source);
                        source.setId(info.getSourceInventoryId());
                        source.setShelfId(info.getSourceShelf());
                        source.setWarehouseId(model.getSourceWarehouse());
                        sourceList.add(source);
                        WmsInventoryDTO target = new WmsInventoryDTO();
                        BeanUtil.copyProperties(info, target);
                        //生成箱号
                        caseNo = ++caseNo;
                        target.setCaseNo(model.getCode() + WmsCodeUtil.SHORT_LINE + caseNo);
                        target.setShelfId(info.getTargetShelf());
                        target.setWarehouseId(model.getTargetWarehouse());
                        targetList.add(target);
                        //明细信息
                        WmsTransferSlipInfo wmsTransferSlipInfo = new WmsTransferSlipInfo();
                        BeanUtil.copyProperties(info, wmsTransferSlipInfo);
                        wmsTransferSlipInfo.setTransferSlipId(model.getId());
                        wmsTransferSlipInfo.setTargetCaseNo(target.getCaseNo());
                        list.add(wmsTransferSlipInfo);
                    }
                    //增加库存中原始占用库存数量,减少可用库存
                    wmsInventoryService.addLockQuantity(sourceList);
                    //增加目标货架在途库存数量
                    wmsInventoryService.addInfo(sourceList);
                }
                if (CollUtil.isNotEmpty(updateList)) {
                    //获取原始数量
                    List<WmsInventoryDTO> sourceList = Lists.newArrayList();
                    List<WmsInventoryDTO> targetList = Lists.newArrayList();
                    List<Long> idList = Lists.newArrayList();
                    updateList.forEach(info -> idList.add(info.getId()));
                    Map<Long, BigDecimal> originalQuantityMap = wmsTransferSlipInfoService.originalQuantity(idList);
                    Map<Long, String> originalCaseNoMap = wmsTransferSlipInfoService.originalCaseNo(idList);
                    updateList.forEach(info -> {
                        WmsInventoryDTO source = new WmsInventoryDTO();
                        BeanUtil.copyProperties(info, source);
                        source.setId(info.getSourceInventoryId());
                        source.setOriginalQuantity(originalQuantityMap.get(info.getId()));
                        sourceList.add(source);
                        WmsInventoryDTO target = new WmsInventoryDTO();
                        BeanUtil.copyProperties(info, target);
                        target.setOriginalCaseNo(originalCaseNoMap.get(info.getId()));
                        target.setOriginalQuantity(originalQuantityMap.get(info.getId()));
                        target.setShelfId(info.getTargetShelf());
                        target.setWarehouseId(model.getTargetWarehouse());
                        targetList.add(target);
                        //明细信息
                        WmsTransferSlipInfo wmsTransferSlipInfo = new WmsTransferSlipInfo();
                        BeanUtil.copyProperties(info, wmsTransferSlipInfo);
                        list.add(wmsTransferSlipInfo);
                    });
                    wmsInventoryService.editLockQuantity(sourceList);
                    wmsInventoryService.updateInventory(sourceList);
                }
                //修改数据
                wmsTransferSlipInfoService.saveOrUpdateBatch(list);
            }
        } finally {
            lock.unlock();
        }
        return true;
    }

    @Override
    public Boolean affirm(Long id) {
        //1.查询订单
        WmsTransferSlip order = getOne(new LambdaQueryWrapper<WmsTransferSlip>().eq(WmsTransferSlip::getId, id));
        //2.校验数据
        Assert.notNull(order, "该调拨单不存在");
        //3.校验状态
        Assert.isTrue(WmsManagerConstant.OrderStateEnum.ZERO.getCode().equals(order.getStatus()), "当前状态不可确认");
        order.setStatus(WmsManagerConstant.OrderStateEnum.ONE.getCode());
        order.setAffirmBy(SecurityUtils.getUser().getUserId());
        order.setAffirmName(SecurityUtils.getUser().getName());
        order.setAffirmTime(LocalDateTime.now());
        //todo 获取订单详情信息 调用mes库存接口 减少库存中原始货架可用库存数量，减少原始货架占用库存数量，增加目标货架可用库存数量，减少目标货架在途库存数量
        //获取明细信息
        List<WmsTransferSlipInfo> list = wmsTransferSlipInfoService.list(new LambdaQueryWrapper<WmsTransferSlipInfo>().eq(WmsTransferSlipInfo::getTransferSlipId, id));
        if (CollUtil.isNotEmpty(list)) {
            List<WmsInventoryRecord> wmsInventoryRecords = Lists.newArrayList();
            List<WmsInventoryDTO> sourceList = Lists.newArrayList();
            List<WmsInventoryDTO> targetList = Lists.newArrayList();
            //获取库存变化类型
            SysDictVO incomingType = FeignOptional.of(() -> remoteDictFeignApi.getByCode(WmsManagerConstant.variableTypeEnum.TYPE_9.getType(), WmsManagerConstant.VARIATION_TYPE), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            SysDictVO outboundType = FeignOptional.of(() -> remoteDictFeignApi.getByCode(WmsManagerConstant.variableTypeEnum.TYPE_17.getType(), WmsManagerConstant.VARIATION_TYPE), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            List<String> caseNoList = Lists.newArrayList();
            list.forEach(info -> {
                WmsInventoryDTO source = new WmsInventoryDTO();
                BeanUtil.copyProperties(info, source);
                source.setId(info.getSourceInventoryId());
                source.setWarehouseId(order.getSourceWarehouse());
                source.setShelfId(info.getSourceShelf());
                WmsInventoryDTO target = new WmsInventoryDTO();
                BeanUtil.copyProperties(info, target);
                target.setShelfId(info.getTargetShelf());
                target.setCaseNo(info.getTargetCaseNo());
                target.setWarehouseId(order.getTargetWarehouse());
                sourceList.add(source);
                targetList.add(target);
                caseNoList.add(info.getCaseNo());
                caseNoList.add(info.getTargetCaseNo());
            });
            WmsInventory wmsInventory = new WmsInventory();
            //根据箱号获取库存记录
            Map<String, WmsInventory> inventoryByCaseNo = wmsInventoryService.getInventoryByCaseNo(caseNoList);
            //调拨出库类型
            sourceList.forEach(info->{
                WmsInventory orDefault = inventoryByCaseNo.getOrDefault(info.getCaseNo(), wmsInventory);
                //生成库存记录
                WmsInventoryRecord wmsInventoryRecord = new WmsInventoryRecord();
                wmsInventoryRecord.setCode(order.getCode());
                wmsInventoryRecord.setType(outboundType.getId());
                wmsInventoryRecord.setWarehouseId(info.getWarehouseId());
                wmsInventoryRecord.setShelfId(info.getShelfId());
                wmsInventoryRecord.setMaterialId(info.getMaterialId());
                wmsInventoryRecord.setMaterialName(info.getMaterialName());
                //变动数量
                wmsInventoryRecord.setVariableQuantity(info.getQuantity());
                //变动前
                wmsInventoryRecord.setFrontAvailableQuantity(orDefault.getAvailableQuantity());
                wmsInventoryRecord.setFrontStockQuantity(orDefault.getStockQuantity());
                wmsInventoryRecord.setFrontLockQuantity(orDefault.getLockQuantity());
                wmsInventoryRecord.setFrontTotalQuantity(orDefault.getTotalQuantity());
                //变动后
                wmsInventoryRecord.setBackAvailableQuantity(orDefault.getAvailableQuantity());
                wmsInventoryRecord.setBackStockQuantity(orDefault.getStockQuantity());
                wmsInventoryRecord.setBackTotalQuantity(orDefault.getTotalQuantity().subtract(info.getQuantity()));
                wmsInventoryRecord.setBackLockQuantity(orDefault.getLockQuantity().subtract(info.getQuantity()));
                wmsInventoryRecords.add(wmsInventoryRecord);
            });
            //调拨入库类型
            targetList.forEach(info->{
                WmsInventory orDefault = inventoryByCaseNo.getOrDefault(info.getCaseNo(), wmsInventory);
                //生成库存记录
                WmsInventoryRecord wmsInventoryRecord = new WmsInventoryRecord();
                wmsInventoryRecord.setType(incomingType.getId());
                wmsInventoryRecord.setCode(order.getCode());
                wmsInventoryRecord.setWarehouseId(info.getWarehouseId());
                wmsInventoryRecord.setShelfId(info.getShelfId());
                wmsInventoryRecord.setMaterialId(info.getMaterialId());
                wmsInventoryRecord.setMaterialName(info.getMaterialName());
                //变动数量
                wmsInventoryRecord.setVariableQuantity(info.getQuantity());
                //变动前
                wmsInventoryRecord.setFrontAvailableQuantity(orDefault.getAvailableQuantity());
                wmsInventoryRecord.setFrontStockQuantity(orDefault.getStockQuantity());
                wmsInventoryRecord.setFrontLockQuantity(orDefault.getLockQuantity());
                wmsInventoryRecord.setFrontTotalQuantity(orDefault.getTotalQuantity());
                //变动后
                wmsInventoryRecord.setBackAvailableQuantity(orDefault.getAvailableQuantity().add(info.getQuantity()));
                wmsInventoryRecord.setBackStockQuantity(orDefault.getStockQuantity().subtract(info.getQuantity()));
                wmsInventoryRecord.setBackTotalQuantity(orDefault.getTotalQuantity().add(info.getQuantity()));
                wmsInventoryRecord.setBackLockQuantity(orDefault.getLockQuantity());
                wmsInventoryRecords.add(wmsInventoryRecord);
            });
            //保存记录
            List<WmsInventoryRecord> recordList = BeanUtil.copyToList(wmsInventoryRecords, WmsInventoryRecord.class);
            wmsInventoryRecordService.saveBatch(recordList);
            //在库存确认变更前 生成库存流水记录 同一物料调拨生成两条记录
            wmsInventoryService.affirmLockQuantity(sourceList);
            wmsInventoryService.affirm(targetList);
        }
        return updateById(order);
    }

    @Override
    public Boolean delete(Long id) {
        //1.查询订单
        WmsTransferSlip order = getOne(new LambdaQueryWrapper<WmsTransferSlip>().eq(WmsTransferSlip::getId, id));
        //2.校验数据
        Assert.notNull(order, "该调拨单不存在");
        //3.校验状态
        Assert.isTrue(WmsManagerConstant.OrderStateEnum.ZERO.getCode().equals(order.getStatus()), "当前状态不可删除");
        order.setStatus(WmsManagerConstant.OrderStateEnum.FOUR.getCode());
        //todo 获取订单详情信息 调用mes库存接口 减少原始货架占用库存数量，减少目标货架在途库存数量
        //获取明细信息
        List<WmsTransferSlipInfo> list = wmsTransferSlipInfoService.list(new LambdaQueryWrapper<WmsTransferSlipInfo>().eq(WmsTransferSlipInfo::getTransferSlipId, id));
        if (CollUtil.isNotEmpty(list)) {
            List<WmsInventoryDTO> sourceList = Lists.newArrayList();
            List<WmsInventoryDTO> targetList = Lists.newArrayList();
            list.forEach(wmsTransferSlipInfo -> {
                WmsInventoryDTO source = new WmsInventoryDTO();
                BeanUtil.copyProperties(wmsTransferSlipInfo, source);
                source.setId(wmsTransferSlipInfo.getSourceInventoryId());
                source.setWarehouseId(order.getSourceWarehouse());
                source.setShelfId(wmsTransferSlipInfo.getSourceShelf());
                WmsInventoryDTO target = new WmsInventoryDTO();
                BeanUtil.copyProperties(wmsTransferSlipInfo, target);
                target.setShelfId(wmsTransferSlipInfo.getTargetShelf());
                target.setWarehouseId(order.getTargetWarehouse());
                target.setOriginalQuantity(wmsTransferSlipInfo.getQuantity());
                target.setOriginalCaseNo(wmsTransferSlipInfo.getCaseNo());
                sourceList.add(source);
                targetList.add(target);
            });
            //去除源库存信息（增加可用库存，减少占用库存）
            wmsInventoryService.restoreLockQuantity(sourceList);
            //去除目标库存信息(减少在途库存)
            wmsInventoryService.restore(targetList);
        }
        return updateById(order);
    }

    @Override
    public WmsTransferSlipVO get(Long id) {
        //1.查询订单
        WmsTransferSlip order = getOne(new LambdaQueryWrapper<WmsTransferSlip>().eq(WmsTransferSlip::getId, id));
        //2.校验数据
        Assert.notNull(order, "该调拨单不存在");
        //3.定义返回数据模型
        WmsTransferSlipVO wmsTransferSlipVO = new WmsTransferSlipVO();
        BeanUtil.copyProperties(order, wmsTransferSlipVO);
        //3.处理数据
        Set<Integer> idSet = Sets.newHashSet();
        Set<Long> typeSet = Sets.newHashSet();
        idSet.add(wmsTransferSlipVO.getSourceWarehouse());
        idSet.add(wmsTransferSlipVO.getTargetWarehouse());
        typeSet.add(wmsTransferSlipVO.getType());
        Map<Integer, String> officeMap = FeignOptional.of(() -> remoteOfficeFeign.listByIdCh(idSet), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        Map<Long, String> dictMap = FeignOptional.of(() -> remoteDictFeignApi.listByIdCh(typeSet), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        wmsTransferSlipVO.setTypeName(dictMap.get(wmsTransferSlipVO.getType()));
        wmsTransferSlipVO.setSourceWarehouseName(officeMap.get(wmsTransferSlipVO.getSourceWarehouse()));
        wmsTransferSlipVO.setTargetWarehouseName(officeMap.get(wmsTransferSlipVO.getTargetWarehouse()));
        //4.获取详情数据
        List<WmsTransferSlipInfo> list = wmsTransferSlipInfoService.list(new LambdaQueryWrapper<WmsTransferSlipInfo>().eq(WmsTransferSlipInfo::getTransferSlipId, order.getId()));
        //5.处理详情数据
        if (CollUtil.isNotEmpty(list)) {
            Set<Long> dictSet = Sets.newHashSet();
            List<WmsTransferSlipInfoVO> infoVOList = BeanUtil.copyToList(list, WmsTransferSlipInfoVO.class);
            infoVOList.forEach(info -> dictSet.add(info.getUnit()));
            wmsTransferSlipVO.setList(infoVOList);
            Map<Long, String> dict = FeignOptional.of(() -> remoteDictFeignApi.listByIdCh(dictSet), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            infoVOList.forEach(info -> info.setUnitName(dict.get(info.getUnit())));
        }
        return wmsTransferSlipVO;
    }

    @Override
    public Boolean linkErp(Long id) {
        //获取详情
        WmsTransferSlipVO wmsTransferSlipVO = get(id);
        if (!wmsTransferSlipVO.getStatus().equals(OaConstant.NUM_1)) {
            throw new BusinessException("只有已确认状态才可以同步");
        }
        String sourceCode = FeignOptional.of(() -> remoteOfficeFeign.getByIdSysOfficeInfo(wmsTransferSlipVO.getSourceWarehouse().longValue()), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        String targetCode = FeignOptional.of(() -> remoteOfficeFeign.getByIdSysOfficeInfo(wmsTransferSlipVO.getTargetWarehouse().longValue()), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        //组erp数据
        ErpCommonSaveDTO<ErpTransferSaveDetailDTO> erpDTO = new ErpCommonSaveDTO();
        erpDTO.setFmesbillid(wmsTransferSlipVO.getId().toString());
        erpDTO.setFmesbillno(wmsTransferSlipVO.getCode());
        erpDTO.setFdate(wmsTransferSlipVO.getCreateTime().toLocalDate());
        //组erp详情
        List<ErpTransferSaveDetailDTO> erpDetails = Lists.newArrayList();
        if (CollUtil.isNotEmpty(wmsTransferSlipVO.getList())) {
            //获取所有仓库信息
            for (WmsTransferSlipInfoVO tmp : wmsTransferSlipVO.getList()) {
                ErpTransferSaveDetailDTO erpDetail = new ErpTransferSaveDetailDTO();
                erpDetail.setFmesentryid(tmp.getId().toString());
                erpDetail.setFmatno(tmp.getMaterialCode());
                erpDetail.setFqty(tmp.getQuantity());
                erpDetail.setFsrcstockno(sourceCode);
                erpDetail.setFdeststockno(targetCode);
                erpDetail.setFlot(tmp.getBatch());
                erpDetails.add(erpDetail);
            }
        }
        erpDTO.setFentry(erpDetails);
        //调用EPR
        String listR = remoteErpFeignApi.saveTransfer(erpDTO);
        JSONObject jsonObject = JSONObject.parseObject(listR);
        if (jsonObject.getBoolean(WmsManagerConstant.JSON_SUCCESS)) {
            WmsTransferSlip wmsTransferSlip = new WmsTransferSlip();
            wmsTransferSlip.setId(id);
            wmsTransferSlip.setStatus(OaConstant.NUM_2);
            updateById(wmsTransferSlip);
        } else {
            String error = "同步失败-" + jsonObject.getString(WmsManagerConstant.JSON_MESSAGE);
            log.error(error);
            throw new BusinessException(error);
        }
        return true;
    }

    @Override
    public BigDecimal slipLineWarehouse(LocalDate date, Set<Integer> shelfIdSet) {
        LocalDateTime start = LocalDateUtil.getMorning(date);
        List<WmsTransferSlip> dbList = list(new LambdaQueryWrapper<WmsTransferSlip>().eq(WmsTransferSlip::getStatus, 1).ge(WmsTransferSlip::getAffirmTime, start).lt(WmsTransferSlip::getAffirmTime, start.plusDays(1)));
        if (CollUtil.isNotEmpty(dbList)) {
            Set<Long> idSet = dbList.stream().map(WmsTransferSlip::getId).collect(Collectors.toSet());
            List<WmsTransferSlipInfo> sourceList = wmsTransferSlipInfoService.list(new LambdaQueryWrapper<WmsTransferSlipInfo>().in(WmsTransferSlipInfo::getTransferSlipId, idSet).in(WmsTransferSlipInfo::getTargetShelf, shelfIdSet).like(WmsTransferSlipInfo::getMaterialName, "原片"));
            if (CollUtil.isNotEmpty(sourceList)) {
                List<BigDecimal> valueList = sourceList.stream().map(WmsTransferSlipInfo::getQuantity).collect(Collectors.toList());
                return BigDecimalUtil.sum(valueList);
            }
        }
        return null;
    }

    @Override
    public Page<WmsReportWarehouseVO> transferSlip(WmsReportWarehouseDTO dto) {
        //处理时间
        if (Objects.nonNull(dto.getDate())) {
            dto.setStartTime(LocalDateTime.of(dto.getDate(), LocalTime.MIN));
            dto.setEndTime(LocalDateTime.of(dto.getDate(), LocalTime.MAX));
        }
        List<WmsReportWarehouseVO> resList = Lists.newArrayList();
        LambdaQueryWrapper<WmsTransferSlip> wrapper = new LambdaQueryWrapper<WmsTransferSlip>()
                .eq(Objects.nonNull(dto.getStatus()), WmsTransferSlip::getStatus, dto.getStatus())
                .eq(Objects.nonNull(dto.getTypeId()), WmsTransferSlip::getType, dto.getTypeId())
                .between(Objects.nonNull(dto.getStartTime()) && Objects.nonNull(dto.getEndTime()), WmsTransferSlip::getCreateTime, dto.getStartTime(), dto.getEndTime())
                .notIn(WmsTransferSlip::getStatus, WmsManagerConstant.OrderStateEnum.FIVE.getCode());
        if (CollUtil.isNotEmpty(dto.getWarehouseIds())) {
            wrapper.and(w -> {
                w.in(WmsTransferSlip::getSourceWarehouse, dto.getWarehouseIds()).or()
                        .in(WmsTransferSlip::getTargetWarehouse, dto.getWarehouseIds());
            });
        }
        List<WmsTransferSlip> list = list(wrapper);
        if (CollUtil.isNotEmpty(list)){
            Map<Long,WmsTransferSlip> map = Maps.newHashMap();
            List<Long> typeIds = Lists.newArrayList();
            Set<Integer> officeIds = Sets.newHashSet();
            list.forEach( receipt->{
                map.put(receipt.getId(),receipt);
                typeIds.add(receipt.getType());
                officeIds.add(Math.toIntExact(receipt.getTargetWarehouse()));
                officeIds.add(Math.toIntExact(receipt.getSourceWarehouse()));
            });
            Set<Long> ids = map.keySet();
            Map<Long, SysDictVO> dictMap = FeignOptional.of(() -> remoteDictFeignApi.listById(typeIds), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            Map<Integer, String> officeMap = FeignOptional.of(() -> remoteOfficeFeign.listByIdCh(officeIds), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            List<WmsTransferSlipInfo> infos = wmsTransferSlipInfoService.list(new LambdaQueryWrapper<WmsTransferSlipInfo>().in(WmsTransferSlipInfo::getTransferSlipId,ids));
            infos.forEach(info->{
                WmsReportWarehouseVO wmsReportWarehouseVO = new WmsReportWarehouseVO();
                BeanUtil.copyProperties(info,wmsReportWarehouseVO);
                WmsTransferSlip wmsTransferSlip = map.get(info.getTransferSlipId());
                Long typeId = wmsTransferSlip.getType();
                wmsReportWarehouseVO.setType(typeId);
                wmsReportWarehouseVO.setWarehouse(Long.valueOf(wmsTransferSlip.getTargetWarehouse()));
                wmsReportWarehouseVO.setWarehouseName(officeMap.get(Math.toIntExact(wmsTransferSlip.getSourceWarehouse())));
                wmsReportWarehouseVO.setCode(wmsTransferSlip.getCode());
                wmsReportWarehouseVO.setTypeName(dictMap.get(typeId).getName());
                wmsReportWarehouseVO.setStatus(wmsTransferSlip.getStatus());
                wmsReportWarehouseVO.setAffirmName(wmsTransferSlip.getAffirmName());
                wmsReportWarehouseVO.setAffirmTime(wmsTransferSlip.getAffirmTime());
                wmsReportWarehouseVO.setCreateTime(wmsTransferSlip.getCreateTime());
                wmsReportWarehouseVO.setCreateName(wmsTransferSlip.getCreateName());
                resList.add(wmsReportWarehouseVO);
            });
        }
        return LogicalPageUtil.page(resList, dto.getCurrent(), dto.getSize());
    }

    @Override
    public EpChartGraphVO<List<String>, BigDecimal> transferSlipClassification(WmsReportWarehouseDTO dto) {
        EpChartGraphVO<List<String>, BigDecimal>  chartVO =  new EpChartGraphVO<>();
        List<QtSeriesDataDTO> seriesDataVO = Lists.newArrayList();
        //获取调拨单数据
        LambdaQueryWrapper<WmsTransferSlip> wrapper = new LambdaQueryWrapper<WmsTransferSlip>()
                .eq(Objects.nonNull(dto.getStatus()), WmsTransferSlip::getStatus, dto.getStatus())
                .eq(Objects.nonNull(dto.getTypeId()), WmsTransferSlip::getType, dto.getTypeId())
                .between(Objects.nonNull(dto.getStartTime()) && Objects.nonNull(dto.getEndTime()), WmsTransferSlip::getCreateTime, dto.getStartTime(), dto.getEndTime())
                .notIn(WmsTransferSlip::getStatus, WmsManagerConstant.OrderStateEnum.FIVE.getCode());
        if (CollUtil.isNotEmpty(dto.getWarehouseIds())) {
            wrapper.and(w -> {
                w.in(WmsTransferSlip::getSourceWarehouse, dto.getWarehouseIds()).or()
                        .in(WmsTransferSlip::getTargetWarehouse, dto.getWarehouseIds());
            });
        }
        List<WmsTransferSlip> list = list(wrapper);
        if (CollUtil.isNotEmpty(list)) {
            Set<Long> ids = list.stream().map(WmsTransferSlip::getId).collect(Collectors.toSet());
            List<WmsTransferSlipInfo> infos = wmsTransferSlipInfoService.list(new LambdaQueryWrapper<WmsTransferSlipInfo>().in(WmsTransferSlipInfo::getTransferSlipId,ids));
            List<WmsStorageReceiptInfoVO> wmsStorageReceiptInfoVOS = BeanUtil.copyToList(infos, WmsStorageReceiptInfoVO.class);
            List<Long> materialIds = infos.stream().map(WmsTransferSlipInfo::getMaterialId).collect(Collectors.toList());
            Set<Long> typeIds = Sets.newHashSet();
            List<String> types = Lists.newArrayList();
            Map<Long, EqpSpareVO> materialMap = FeignOptional.of(() -> remoteSpareFeignApi.listById(materialIds), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            materialMap.forEach((key, value) -> typeIds.add(value.getType()));
            //获取物料分类
            Map<Long, ClassificationVO> classificationMap = remoteMessFeignApi.spareClassification(typeIds);
            if (CollUtil.isNotEmpty(classificationMap)){
                classificationMap.forEach((key,value)-> types.add(value.getName()));
                wmsStorageReceiptInfoVOS.forEach(infoVO-> {
                    if (Objects.nonNull(materialMap.get(infoVO.getMaterialId()).getType())) {
                        infoVO.setClassificationName(classificationMap.get( materialMap.get(infoVO.getMaterialId()).getType()).getName());
                    }
                });
                Map<String, List<WmsStorageReceiptInfoVO>> receiptMap = wmsStorageReceiptInfoVOS.stream()
                        .filter(vo -> vo.getClassificationName() != null)
                        .collect(Collectors.groupingBy(WmsStorageReceiptInfoVO::getClassificationName));
                QtSeriesDataDTO<String,BigDecimal> qtSeriesDataDTO = new QtSeriesDataDTO<>();
                List<BigDecimal> quantity = Lists.newArrayList();
                types.forEach(type->{
                    quantity.add( receiptMap.get(type).stream().map(WmsStorageReceiptInfoVO::getQuantity).reduce(BigDecimal.ZERO, BigDecimalUtil::add));
                });
                qtSeriesDataDTO.setData(quantity);
                qtSeriesDataDTO.setName(WmsManagerConstant.MATERIAL);
                qtSeriesDataDTO.setType(WmsManagerConstant.BAR);
                seriesDataVO.add(qtSeriesDataDTO);
            }
            chartVO.setXData(types);
            chartVO.setSeriesDataVO(seriesDataVO);
        }
        return chartVO;
    }

    @Override
    public BigDecimal totalQuantity(WmsReportWarehouseDTO dto) {
        BigDecimal quantity  = null;
        //获取出库单数据
        List<WmsTransferSlipInfo> infos = this.baseMapper.totalQuantity(dto.getWarehouseIds(), dto.getStartTime(), dto.getEndTime());
        if (CollUtil.isNotEmpty(infos)){
            quantity = infos.stream().map(WmsTransferSlipInfo::getQuantity).reduce(BigDecimal.ZERO,BigDecimalUtil::add);
        }
        return quantity;
    }

    @Override
    public Map<String, BigDecimal> materialDepot(LocalDateTime startTime, LocalDateTime endTime, List<String> materialName) {
        Map<String,BigDecimal>  resMap = Maps.newHashMap();
        //获取主要材料库
        Map<String, SysOfficeVO> warehouseMap = FeignOptional.of(() -> remoteOfficeFeign.listByName(Sets.newHashSet(WmsManagerConstant.MATERIAL_DEPOT)), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
//       List<WmsTransferSlipInfo> infos =  this.baseMapper.materialDepot(startTime,endTime,materialName,warehouseMap.get(WmsManagerConstant.MATERIAL_DEPOT),materialName);
        return null;
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
        WmsTransferSlip maxCodeModel = getOne(new LambdaQueryWrapper<WmsTransferSlip>().between(WmsTransferSlip::getCreateTime, min, max).orderByDesc(WmsTransferSlip::getCode).last(MpConstant.STR_LIMIT_1));
        //3.如果最大code不为空
        if (Objects.nonNull(maxCodeModel)) {
            try {
                //4.解析数值部分
                String substringVal = maxCodeModel.getCode().substring(WmsCodeUtil.PREFIX_WMS_TRANSFER_SLIP.length() + WmsCodeUtil.NUM_BIT_12);
                //赋值
                long l = Long.parseLong(substringVal);
                currentMax = l;
            } catch (Exception e) {
                log.error(e.getLocalizedMessage());
            }
        }
        return currentMax;
    }

    private Long getCaseNo(String code, Long id) {
        // 定义返回值
        Long currentMax = 0L;
        //获取规则箱号
        WmsTransferSlipInfo maxCodeModel = wmsTransferSlipInfoService.getOne(new LambdaQueryWrapper<WmsTransferSlipInfo>().like(WmsTransferSlipInfo::getCaseNo, code).eq(WmsTransferSlipInfo::getTransferSlipId, id).orderByDesc(WmsTransferSlipInfo::getCaseNo).last(MpConstant.STR_LIMIT_1));
        if (Objects.nonNull(maxCodeModel)) {
            try {
                //4.解析数值部分
                String substringVal = maxCodeModel.getCaseNo().substring(WmsCodeUtil.PREFIX_WMS_TRANSFER_SLIP.length() + WmsCodeUtil.NUM_BIT_15);
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




