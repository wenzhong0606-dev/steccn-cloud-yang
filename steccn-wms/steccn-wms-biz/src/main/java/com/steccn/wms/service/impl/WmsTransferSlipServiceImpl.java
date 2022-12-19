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
import com.steccn.wms.domain.WmsInventory;
import com.steccn.wms.domain.WmsStorageReceiptInfo;
import com.steccn.wms.domain.WmsTransferSlip;
import com.steccn.wms.domain.WmsTransferSlipInfo;
import com.steccn.wms.dto.WmsInventoryDTO;
import com.steccn.wms.dto.WmsTransferSlipDTO;
import com.steccn.wms.dto.WmsTransferSlipInfoDTO;
import com.steccn.wms.dto.WmsTransferSlipPageDTO;
import com.steccn.wms.feign.RemoteWmsMessFeignApi;
import com.steccn.wms.service.WmsInventoryService;
import com.steccn.wms.service.WmsTransferSlipInfoService;
import com.steccn.wms.service.WmsTransferSlipService;
import com.steccn.wms.mapper.WmsTransferSlipMapper;
import com.steccn.wms.util.WmsCodeUtil;
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

/**
 * @author W10
 * @description 针对表【wms_transfer_slip(调拨单)】的数据库操作Service实现
 * @createDate 2022-11-30 09:45:17
 */
@Service
@RequiredArgsConstructor
public class WmsTransferSlipServiceImpl extends ServiceImpl<WmsTransferSlipMapper, WmsTransferSlip>
        implements WmsTransferSlipService {

    private final RedissonClient redissonClient;

    private final WmsTransferSlipInfoService wmsTransferSlipInfoService;

    private final RemoteOfficeFeign remoteOfficeFeign;

    private final RemoteDictFeignApi remoteDictFeignApi;

    private final RemoteWmsMessFeignApi remoteWmsMessFeignApi;

    private final WmsInventoryService wmsInventoryService;


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
        Map<Integer, String> officeMap = FeignOptional.of(remoteOfficeFeign.listByIdCh(idSet)).get();
        Map<Long, String> dictMap = FeignOptional.of(remoteDictFeignApi.listByIdCh(typeSet)).get();
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
                    infoList.add(wmsTransferSlipInfo);
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
                }
                wmsTransferSlipInfoService.saveBatch(infoList);
                //增加库存中原始占用库存数量,减少可用库存
                wmsInventoryService.addLockQuantity(sourceList);
                //增加目标货架在途库存数量
                wmsInventoryService.add(sourceList);
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
                    list.add(wmsTransferSlipInfo);
                }
                LambdaQueryWrapper<WmsTransferSlipInfo> wrapper = new LambdaQueryWrapper<WmsTransferSlipInfo>()
                        .eq(WmsTransferSlipInfo::getTransferSlipId, dto.getId())
                        .notIn(CollUtil.isNotEmpty(ids), WmsTransferSlipInfo::getId, ids);
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
        Assert.isTrue(WmsManagerConstant.OrderStateEnum.ZERO.getCode().equals(order.getStatus())
                , "当前状态不可确认");
        order.setStatus(WmsManagerConstant.OrderStateEnum.ONE.getCode());
        order.setAffirmBy(SecurityUtils.getUser().getUserId());
        order.setAffirmName(SecurityUtils.getUser().getName());
        order.setAffirmTime(LocalDateTime.now());
        //todo 获取订单详情信息 调用mes库存接口 减少库存中原始货架可用库存数量，减少原始货架占用库存数量，增加目标货架可用库存数量，减少目标货架在途库存数量
        //获取明细信息
        List<WmsTransferSlipInfo> list = wmsTransferSlipInfoService.list(new LambdaQueryWrapper<WmsTransferSlipInfo>().eq(WmsTransferSlipInfo::getTransferSlipId, id));
        if (CollUtil.isNotEmpty(list)) {
            List<WmsInventoryDTO> sourceList = Lists.newArrayList();
            List<WmsInventoryDTO> targetList = Lists.newArrayList();
            list.forEach(info -> {
                WmsInventoryDTO source = new WmsInventoryDTO();
                BeanUtil.copyProperties(info, source);
                source.setId(info.getSourceInventoryId());
                source.setWarehouseId(order.getSourceWarehouse());
                source.setShelfId(info.getSourceShelf());
                WmsInventoryDTO target = new WmsInventoryDTO();
                BeanUtil.copyProperties(info, target);
                target.setShelfId(info.getTargetShelf());
                target.setWarehouseId(order.getTargetWarehouse());
                sourceList.add(source);
                targetList.add(target);
            });
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
        Assert.isTrue(WmsManagerConstant.OrderStateEnum.ZERO.getCode().equals(order.getStatus())
                , "当前状态不可删除");
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
        Map<Integer, String> officeMap = FeignOptional.of(remoteOfficeFeign.listByIdCh(idSet)).get();
        Map<Long, String> dictMap = FeignOptional.of(remoteDictFeignApi.listByIdCh(typeSet)).get();
        wmsTransferSlipVO.setTypeName(dictMap.get(wmsTransferSlipVO.getType()));
        wmsTransferSlipVO.setSourceWarehouseName(officeMap.get(wmsTransferSlipVO.getSourceWarehouse()));
        wmsTransferSlipVO.setTargetWarehouseName(officeMap.get(wmsTransferSlipVO.getTargetWarehouse()));
        //4.获取详情数据
        List<WmsTransferSlipInfo> list = wmsTransferSlipInfoService.list(new LambdaQueryWrapper<WmsTransferSlipInfo>()
                .eq(WmsTransferSlipInfo::getTransferSlipId, order.getId()));
        //5.处理详情数据
        if (CollUtil.isNotEmpty(list)) {
            Set<Long> dictSet = Sets.newHashSet();
            List<WmsTransferSlipInfoVO> infoVOList = BeanUtil.copyToList(list, WmsTransferSlipInfoVO.class);
            infoVOList.forEach(info -> dictSet.add(info.getUnit()));
            wmsTransferSlipVO.setList(infoVOList);
            Map<Long, String> dict = FeignOptional.of(remoteDictFeignApi.listByIdCh(dictSet)).get();
            infoVOList.forEach(info -> info.setUnitName(dict.get(info.getUnit())));
        }
        return wmsTransferSlipVO;
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
        WmsTransferSlip maxCodeModel = getOne(new LambdaQueryWrapper<WmsTransferSlip>()
                .between(WmsTransferSlip::getCreateTime, min, max)
                .orderByDesc(WmsTransferSlip::getCode)
                .last(MpConstant.STR_LIMIT_1));
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
        WmsTransferSlipInfo maxCodeModel = wmsTransferSlipInfoService.getOne(new LambdaQueryWrapper<WmsTransferSlipInfo>()
                .like(WmsTransferSlipInfo::getCaseNo, code)
                .eq(WmsTransferSlipInfo::getTransferSlipId, id)
                .orderByDesc(WmsTransferSlipInfo::getCaseNo)
                .last(MpConstant.STR_LIMIT_1));
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




