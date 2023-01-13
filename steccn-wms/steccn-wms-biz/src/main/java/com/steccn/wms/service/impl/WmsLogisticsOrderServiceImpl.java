package com.steccn.wms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.lang.Dict;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Sets;
import com.steccn.authserver.exception.BusinessException;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.common.constant.FeignConstant;
import com.steccn.common.constant.LockConstant;
import com.steccn.common.constant.MpConstant;
import com.steccn.common.vo.R;
import com.steccn.platform.feign.RemoteDictFeignApi;
import com.steccn.platform.feign.RemoteOfficeFeign;
import com.steccn.platform.vo.SysDictVO;
import com.steccn.pms.vo.WmsInventoryVO;
import com.steccn.wms.constant.WmsManagerConstant;
import com.steccn.wms.domain.*;
import com.steccn.wms.dto.*;
import com.steccn.wms.service.*;
import com.steccn.wms.mapper.WmsLogisticsOrderMapper;
import com.steccn.wms.util.WmsCodeUtil;
import com.steccn.wms.vo.*;
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
 * @description 针对表【wms_logistics_order(物流单)】的数据库操作Service实现
 * @createDate 2022-11-30 09:45:17
 */
@Service
@RequiredArgsConstructor
public class WmsLogisticsOrderServiceImpl extends ServiceImpl<WmsLogisticsOrderMapper, WmsLogisticsOrder>
        implements WmsLogisticsOrderService {

    private final RemoteDictFeignApi remoteDictFeignApi;

    private final RemoteOfficeFeign remoteOfficeFeign;

    private final RedissonClient redissonClient;

    private final WmsLogisticsInfoService wmsLogisticsInfoService;

    private final WmsLogisticsDetailService wmsLogisticsDetailService;

    private final WmsLogisticsTransportationService wmsLogisticsTransportationService;

    private final WmsShelfUpStrategyService wmsShelfUpStrategyService;

    private final WmsStorageReceiptService wmsStorageReceiptService;


    @Override
    public Page<WmsLogisticsOrderVO> pageList(WmsLogisticsOrderPageDTO dto) {
        //1.处理时间格式
        //实际送达时间 -开始
        if (Objects.nonNull(dto.getPracticalStartDate())) {
            dto.setPracticalStartTime(LocalDateTime.of(dto.getPracticalStartDate(), LocalTime.MIN));
        }
        //实际送达时间-结束
        if (Objects.nonNull(dto.getPracticalEndDate())) {
            dto.setPracticalEndTime(LocalDateTime.of(dto.getPracticalEndDate(), LocalTime.MAX));
        }
        //计划结束时间-开始
        if (Objects.nonNull(dto.getPredictStartDate())) {
            dto.setPredictStartTime(LocalDateTime.of(dto.getPredictStartDate(), LocalTime.MIN));
        }
        //计划结束时间-结束
        if (Objects.nonNull(dto.getPredictEndDate())) {
            dto.setPredictEndTime(LocalDateTime.of(dto.getPredictEndDate(), LocalTime.MAX));
        }//计划开始时间-开始
        //2.根据条件获取数据
        LambdaQueryWrapper<WmsLogisticsOrder> eq = new LambdaQueryWrapper<WmsLogisticsOrder>()
                 .like(Objects.nonNull(dto.getCode()), WmsLogisticsOrder::getCode, dto.getCode())
                .like(Objects.nonNull(dto.getCreateName()),WmsLogisticsOrder::getCreateName,dto.getCreateName())
                .eq(Objects.nonNull(dto.getType()), WmsLogisticsOrder::getType, dto.getType())
                .eq(Objects.nonNull(dto.getStatus()), WmsLogisticsOrder::getStatus, dto.getStatus())
                .eq(Objects.nonNull(dto.getLogisticsCompany()), WmsLogisticsOrder::getLogisticsCompany, dto.getLogisticsCompany())
                .between(Objects.nonNull(dto.getPredictStartTime()) && Objects.nonNull(dto.getPredictEndTime()),
                        WmsLogisticsOrder::getPredictTime, dto.getPredictStartTime(), dto.getPredictEndTime())
                .between(Objects.nonNull(dto.getPracticalStartDate()) && Objects.nonNull(dto.getPracticalEndDate()),
                        WmsLogisticsOrder::getPracticalTime, dto.getPracticalStartTime(), dto.getPracticalEndTime()).
                        notIn(WmsLogisticsOrder::getStatus, WmsManagerConstant.LogisticsStateEnum.THREE.getCode())
                .orderByDesc(WmsLogisticsOrder::getUpdateTime);
        //3.处理数据
        Page<WmsLogisticsOrder> page = page(new Page<>(dto.getCurrent(), dto.getSize()), eq);
        Page<WmsLogisticsOrderVO> resPage = new Page<>();
        BeanUtil.copyProperties(page, resPage);
        List<WmsLogisticsOrderVO> list = BeanUtil.copyToList(page.getRecords(), WmsLogisticsOrderVO.class);
        Set<Long> dictIdSet = Sets.newHashSet();
        List<Long> orderIds = Lists.newArrayList();
        Set<Long> logisticsIds = Sets.newHashSet();
        Set<Integer> officeIdSet = Sets.newHashSet();
        list.forEach(order -> {
            orderIds.add(order.getId());
            logisticsIds.add(order.getId());
            dictIdSet.add(order.getType());
            officeIdSet.add(order.getLogisticsCompany());
        });
        // 获取物流收发货机构
        Map<Long, List<WmsLogisticsInfoVO>> map = wmsLogisticsInfoService.mapByLogisticsId(orderIds);
        List<WmsLogisticsInfoVO> infoList = Lists.newArrayList();
        //获取运输方式

        Map<Long, List<WmsLogisticsTransportationVO>> wayMap = wmsLogisticsTransportationService.mapBySetIds(logisticsIds);

        //获取物流类型
        Map<Long, String> dictMap = FeignOptional.of(()->remoteDictFeignApi.listByIdCh(dictIdSet),FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        Map<Integer, String> officeIdMap = FeignOptional.of(()->remoteOfficeFeign.listByIdCh(officeIdSet), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();

        list.forEach(order -> {
            List<WmsLogisticsInfoVO> orDefault = map.getOrDefault(order.getId(), infoList);
            for (WmsLogisticsInfoVO wmsLogisticsInfoVO : orDefault) {
                if (WmsManagerConstant.LogisticsTypeEnum.ZERO.getCode().equals(wmsLogisticsInfoVO.getType())) {
                    order.setReceiving(wmsLogisticsInfoVO);
                } else if (WmsManagerConstant.LogisticsTypeEnum.ONE.getCode().equals(wmsLogisticsInfoVO.getType())) {
                    order.setShipments(wmsLogisticsInfoVO);
                }
            }
            order.setTypeName(dictMap.get(order.getType()));
            order.setLogisticsCompanyName(officeIdMap.get(order.getLogisticsCompany()));
            order.setWayList(wayMap.get(order.getId()));
        });

        return resPage.setRecords(list);
    }

    @Override
    public Boolean add(WmsLogisticsOrderDTO dto) {
        RLock lock = redissonClient.getLock(LockConstant.TABLE_WMS_LOGISTICS_ORDER);
        lock.lock();
        try {
            //1.校验数据是否存在
            Assert.isTrue(Objects.nonNull(dto), "物流单信息为空");
            //2.定义数据模型
            WmsLogisticsOrder model = new WmsLogisticsOrder();
            BeanUtil.copyProperties(dto, model);
            //3.给当前物流单生成编码
            model.setCode(WmsCodeUtil.getProduceOrderCode(WmsCodeUtil.PREFIX_WMS_LOGISTICS_ORDER, WmsCodeUtil.NUM_BIT_4, currentMaxCode()));
            model.setStatus(WmsManagerConstant.LogisticsStateEnum.ZERO.getCode());
            //4.保存物流单信息
            save(model);
            //5.保存收发货信息
            List<WmsLogisticsInfo> logisticsInfoList = Lists.newArrayList();
            if (Objects.nonNull(dto.getReceiving())) {
                WmsLogisticsInfoDTO receiving = dto.getReceiving();
                WmsLogisticsInfo wmsLogisticsInfo = new WmsLogisticsInfo();
                BeanUtil.copyProperties(receiving, wmsLogisticsInfo);
                wmsLogisticsInfo.setLogisticsId(model.getId());
                wmsLogisticsInfo.setType(WmsManagerConstant.LogisticsTypeEnum.ZERO.getCode());
                logisticsInfoList.add(wmsLogisticsInfo);
            }
            if (Objects.nonNull(dto.getShipments())) {
                WmsLogisticsInfoDTO shipments = dto.getShipments();
                WmsLogisticsInfo wmsLogisticsInfo = new WmsLogisticsInfo();
                BeanUtil.copyProperties(shipments, wmsLogisticsInfo);
                wmsLogisticsInfo.setType(WmsManagerConstant.LogisticsTypeEnum.ONE.getCode());
                wmsLogisticsInfo.setLogisticsId(model.getId());
                logisticsInfoList.add(wmsLogisticsInfo);
            }
            if (CollUtil.isNotEmpty(logisticsInfoList)) {
                wmsLogisticsInfoService.saveBatch(logisticsInfoList);
            }
            //6.保存明细信息
            if (CollUtil.isNotEmpty(dto.getList())) {
                List<WmsLogisticsDetail> detailList = Lists.newArrayList();
                List<WmsLogisticsDetailDTO> detailDTOList = dto.getList();
                detailDTOList.forEach(wmsLogisticsDetailDTO -> {
                    WmsLogisticsDetail wmsLogisticsDetail = new WmsLogisticsDetail();
                    BeanUtil.copyProperties(wmsLogisticsDetailDTO, wmsLogisticsDetail);
                    wmsLogisticsDetail.setLogisticsId(model.getId());
                    detailList.add(wmsLogisticsDetail);
                });
                wmsLogisticsDetailService.saveBatch(detailList);
            }
            //7.保存运输方式
            if (CollUtil.isNotEmpty(dto.getWayList())) {
                List<WmsLogisticsTransportationDTO> wayDTOList = dto.getWayList();
                List<WmsLogisticsTransportation> wayList = Lists.newArrayList();
                wayDTOList.forEach(wayDto -> {
                    WmsLogisticsTransportation wmsLogisticsTransportation = new WmsLogisticsTransportation();
                    BeanUtil.copyProperties(wayDto, wmsLogisticsTransportation);
                    wmsLogisticsTransportation.setLogisticsId(model.getId());
                    wayList.add(wmsLogisticsTransportation);
                });
                wmsLogisticsTransportationService.saveBatch(wayList);
            }
        } finally {
            lock.unlock();
        }
        return true;
    }

    @Override
    public WmsLogisticsOrderVO get(Long id) {
        WmsLogisticsOrder order = getById(id);
        Assert.notNull(order, "该物流单不存在");
        //处理数据
        WmsLogisticsOrderVO wmsLogisticsOrderVO = new WmsLogisticsOrderVO();
        BeanUtil.copyProperties(order, wmsLogisticsOrderVO);
        //获取明细数据
        List<WmsLogisticsDetailVO> detailList = wmsLogisticsDetailService.listByLogisticsId(id);
        wmsLogisticsOrderVO.setList(detailList);
        //获取收发货信息
        List<WmsLogisticsInfo> list = wmsLogisticsInfoService.list(new LambdaQueryWrapper<WmsLogisticsInfo>().in(WmsLogisticsInfo::getLogisticsId, id));
        Set<Integer> officeIds = Sets.newHashSet();
        Set<Long> dictIds = Sets.newHashSet();
        dictIds.add(wmsLogisticsOrderVO.getType());
        officeIds.add(wmsLogisticsOrderVO.getLogisticsCompany());
        list.forEach(wmsLogisticsInfo -> {
            officeIds.add(wmsLogisticsInfo.getOfficeId());
        });
        Map<Integer, String> officeMap = FeignOptional.of(()->remoteOfficeFeign.listByIdCh(officeIds),FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        Map<Long, String> dictMap = FeignOptional.of(()->remoteDictFeignApi.listByIdCh(dictIds),FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        list.forEach(wmsLogisticsInfo -> {
            wmsLogisticsInfo.setOfficeName(officeMap.get(wmsLogisticsInfo.getOfficeId()));
            WmsLogisticsInfoVO wmsLogisticsInfoVO = new WmsLogisticsInfoVO();
            BeanUtil.copyProperties(wmsLogisticsInfo, wmsLogisticsInfoVO);
            if (WmsManagerConstant.LogisticsTypeEnum.ZERO.getCode().equals(wmsLogisticsInfo.getType())) {
                wmsLogisticsOrderVO.setReceiving(wmsLogisticsInfoVO);
            } else if (WmsManagerConstant.LogisticsTypeEnum.ONE.getCode().equals(wmsLogisticsInfo.getType())) {
                wmsLogisticsOrderVO.setShipments(wmsLogisticsInfoVO);
            }
        });
        //获取运输方式数据
        wmsLogisticsOrderVO.setWayList(wmsLogisticsTransportationService.listByLogisticsId(id));
        wmsLogisticsOrderVO.setLogisticsCompanyName(officeMap.get(wmsLogisticsOrderVO.getLogisticsCompany()));
        wmsLogisticsOrderVO.setTypeName(dictMap.get(wmsLogisticsOrderVO.getType()));
        return wmsLogisticsOrderVO;
    }

    @Override
    @Transactional(readOnly=false)
    public Boolean transport(Long id) {
        WmsLogisticsOrder order = getById(id);
        Assert.notNull(order, "物流单不存在");
        Assert.isTrue(WmsManagerConstant.LogisticsStateEnum.ZERO.getCode().equals(order.getStatus()), "当前状态不可运输");
        order.setStatus(WmsManagerConstant.LogisticsStateEnum.ONE.getCode());
        return updateById(order);
    }

    @Override
    public R<String> receipt(WmsLogisticsOrderDTO dto) {
        R<String> r = new R<>();
        RLock lock = redissonClient.getLock(LockConstant.TABLE_WMS_LOGISTICS_ORDER);
        lock.lock();
        String code = "1";
        try {
            //1.校验状态
            WmsLogisticsOrder order = getById(dto.getId());
            Assert.notNull(order, "物流单不存在");
            Assert.isTrue(WmsManagerConstant.LogisticsStateEnum.ONE.getCode().equals(order.getStatus()), "当前状态不可签收");
            //2.修改物流单状态信息
            order.setPracticalTime(LocalDateTime.now());
            order.setStatus(WmsManagerConstant.LogisticsStateEnum.TWO.getCode());
            List<WmsLogisticsDetailDTO> list = dto.getList();
            Assert.isTrue(CollUtil.isNotEmpty(list), "物流单详情明细信息不可为空");
            //3.保存明细信息
            List<WmsLogisticsDetail> detailList = BeanUtil.copyToList(list, WmsLogisticsDetail.class);
            wmsLogisticsDetailService.updateBatchById(detailList);
            //4.如果为入库类型，生成入库单
            List<Long> typeIds = Lists.newArrayList();
            typeIds.add(order.getType());
            SysDictVO dict = FeignOptional.of(()->remoteDictFeignApi.listById(typeIds),FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get().get(order.getType());
            if (WmsManagerConstant.WAREHOUSING_TYPE.equals(dict.getType())) {
                List<WmsStorageReceiptInfoDTO> storageReceiptInfoList = Lists.newArrayList();
                Set<Long> materialIds = Sets.newHashSet();
                for (WmsLogisticsDetailDTO detailDTO : list) {
                    materialIds.add(detailDTO.getMaterialId());
                    if (WmsManagerConstant.YesOrNo.YES.getCode().equals(detailDTO.getUnpackFlag())) {
                        //是拆包装
                        Integer packQuantity = detailDTO.getPackQuantity();
                        //获取最后一包数量
                        BigDecimal pack = new BigDecimal(Integer.parseInt(packQuantity.toString()));
                        BigDecimal remainder = detailDTO.getAogQuantity().remainder(pack);
                        BigDecimal subtract = detailDTO.getAogQuantity().subtract(remainder);
                        BigDecimal divide = subtract.divide(pack);
                        for (int i = 0; i < packQuantity; i++) {
                            WmsStorageReceiptInfoDTO storageReceiptInfoDTO = new WmsStorageReceiptInfoDTO();
                            BeanUtil.copyProperties(detailDTO, storageReceiptInfoDTO);
                            storageReceiptInfoDTO.setId(null);
                            if (i == packQuantity - 1) {
                                divide = divide.add(remainder);
                            }
                            storageReceiptInfoDTO.setQuantity(divide);
                            storageReceiptInfoDTO.setAssociatedNo(order.getCode());
                            storageReceiptInfoDTO.setUnit(detailDTO.getUnitId());
                            if (Objects.isNull(detailDTO.getBatch())) {
                                storageReceiptInfoDTO.setBatch("0");
                            }
                            storageReceiptInfoList.add(storageReceiptInfoDTO);
                        }
                    } else {
                        //不拆包装
                        WmsStorageReceiptInfoDTO storageReceiptInfoDTO = new WmsStorageReceiptInfoDTO();
                        BeanUtil.copyProperties(detailDTO, storageReceiptInfoDTO);
                        storageReceiptInfoDTO.setId(null);
                        storageReceiptInfoDTO.setQuantity(detailDTO.getAogQuantity());
                        storageReceiptInfoDTO.setAssociatedNo(order.getCode());
                        storageReceiptInfoDTO.setUnit(detailDTO.getUnitId());
                        if (Objects.isNull(detailDTO.getBatch())) {
                            storageReceiptInfoDTO.setBatch("0");
                        }
                        storageReceiptInfoList.add(storageReceiptInfoDTO);
                    }
                }
                //获取物料的仓库货架
                Map<Long, WmsMaterialShelfUpVO> materialShelfUpMap = wmsShelfUpStrategyService.materialShelfUpInfo(materialIds);
                storageReceiptInfoList.forEach(receiptInfo -> {
                    WmsMaterialShelfUpVO orDefault = materialShelfUpMap.get(receiptInfo.getMaterialId());
                    Assert.notNull(orDefault, "物料:" + receiptInfo.getMaterialName() + "未配置上架策略");
                    receiptInfo.setShelfId(orDefault.getShelfId());
                    receiptInfo.setShelfName(orDefault.getShelfName());
                    receiptInfo.setShelfCode(orDefault.getShelfCode());
                });
                WmsStorageReceiptDTO wmsStorageReceiptDTO = new WmsStorageReceiptDTO();
                //获取入库类型
                SysDictVO dictVO = FeignOptional.of(()->remoteDictFeignApi.getByCode(WmsManagerConstant.CODE_ONE, WmsManagerConstant.WAREHOUSING_TYPE),FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
                wmsStorageReceiptDTO.setType(dictVO.getId());
                //获取仓库
                WmsLogisticsInfo wmsLogisticsInfo = wmsLogisticsInfoService.getOne(new LambdaQueryWrapper<WmsLogisticsInfo>()
                        .eq(WmsLogisticsInfo::getLogisticsId, order.getId())
                        .eq(WmsLogisticsInfo::getType, WmsManagerConstant.LogisticsTypeEnum.ZERO));
                wmsStorageReceiptDTO.setWarehouseId(wmsLogisticsInfo.getOfficeId());
                wmsStorageReceiptDTO.setWarehouseName(wmsLogisticsInfo.getOfficeName());
                wmsStorageReceiptDTO.setNote(order.getNote());
                wmsStorageReceiptDTO.setList(storageReceiptInfoList);
                try {
                    code = wmsStorageReceiptService.createOrder(wmsStorageReceiptDTO);
                    Assert.notNull(code, "生成入库单失败");
                } catch (Exception e) {
                    log.error(e.getLocalizedMessage());
                    e.printStackTrace();
                }
            }
            //修改单据状态
            updateById(order);
        } finally {
            lock.unlock();
        }
        r.setData(code);
        r.setMsg("访问成功");
        r.setCode(1);
        return r;
    }

    @Override
    public Boolean edit(WmsLogisticsOrderDTO dto) {
        RLock lock = redissonClient.getLock(LockConstant.TABLE_WMS_LOGISTICS_ORDER);
        lock.lock();
        try {
            //1.校验数据是否存在
            Assert.isTrue(Objects.nonNull(dto), "调拨单信息为空");
            //2.定义数据模型
            WmsLogisticsOrder model = new WmsLogisticsOrder();
            BeanUtil.copyProperties(dto, model);
            //3.修改
            updateById(model);
            //4.修改详情数据
            if (Objects.nonNull(dto.getList())) {
                wmsLogisticsDetailService.edit(dto);
            }
            //5.修改运输方式
            if (Objects.nonNull(dto.getWayList())) {
                wmsLogisticsTransportationService.edit(dto);
            }
            //6.保存收发货数据
            List<WmsLogisticsInfo> logisticsInfoList = Lists.newArrayList();
            if (Objects.nonNull(dto.getReceiving())) {
                WmsLogisticsInfoDTO receiving = dto.getReceiving();
                WmsLogisticsInfo wmsLogisticsInfo = new WmsLogisticsInfo();
                BeanUtil.copyProperties(receiving, wmsLogisticsInfo);
                wmsLogisticsInfo.setLogisticsId(model.getId());
                wmsLogisticsInfo.setType(WmsManagerConstant.LogisticsTypeEnum.ZERO.getCode());
                logisticsInfoList.add(wmsLogisticsInfo);
            }
            if (Objects.nonNull(dto.getShipments())) {
                WmsLogisticsInfoDTO shipments = dto.getShipments();
                WmsLogisticsInfo wmsLogisticsInfo = new WmsLogisticsInfo();
                BeanUtil.copyProperties(shipments, wmsLogisticsInfo);
                wmsLogisticsInfo.setType(WmsManagerConstant.LogisticsTypeEnum.ONE.getCode());
                wmsLogisticsInfo.setLogisticsId(model.getId());
                logisticsInfoList.add(wmsLogisticsInfo);
            }
            wmsLogisticsInfoService.saveOrUpdateBatch(logisticsInfoList);
        } finally {
            lock.unlock();
        }
        return true;
    }

    @Override
    public Boolean delete(Long id) {
        WmsLogisticsOrder order = getById(id);
        Assert.notNull(order, "订单信息不存在");
        Assert.isTrue(WmsManagerConstant.LogisticsStateEnum.ZERO.getCode().equals(order.getStatus()), "仅创建状态订单可删除");
        order.setStatus(WmsManagerConstant.LogisticsStateEnum.THREE.getCode());
        return updateById(order);
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
        WmsLogisticsOrder maxCodeModel = getOne(new LambdaQueryWrapper<WmsLogisticsOrder>()
                .between(WmsLogisticsOrder::getCreateTime, min, max)
                .orderByDesc(WmsLogisticsOrder::getCode)
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




