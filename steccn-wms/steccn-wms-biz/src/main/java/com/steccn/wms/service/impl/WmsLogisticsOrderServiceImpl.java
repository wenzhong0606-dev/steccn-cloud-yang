package com.steccn.wms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Sets;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.common.constant.LockConstant;
import com.steccn.common.constant.MpConstant;
import com.steccn.platform.feign.RemoteDictFeignApi;
import com.steccn.platform.feign.RemoteOfficeFeign;
import com.steccn.pms.vo.WmsInventoryVO;
import com.steccn.wms.constant.WmsManagerConstant;
import com.steccn.wms.domain.*;
import com.steccn.wms.dto.*;
import com.steccn.wms.service.WmsLogisticsDetailService;
import com.steccn.wms.service.WmsLogisticsInfoService;
import com.steccn.wms.service.WmsLogisticsOrderService;
import com.steccn.wms.mapper.WmsLogisticsOrderMapper;
import com.steccn.wms.service.WmsLogisticsTransportationService;
import com.steccn.wms.util.WmsCodeUtil;
import com.steccn.wms.vo.*;
import lombok.RequiredArgsConstructor;
import org.apache.commons.compress.utils.Lists;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

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
                .eq(Objects.nonNull(dto.getType()), WmsLogisticsOrder::getType, dto.getType())
                .eq(Objects.nonNull(dto.getStatus()), WmsLogisticsOrder::getStatus, dto.getStatus())
                .eq(Objects.nonNull(dto.getLogisticsCompany()), WmsLogisticsOrder::getLogisticsCompany, dto.getLogisticsCompany())
                .between(Objects.nonNull(dto.getPredictStartTime()) && Objects.nonNull(dto.getPredictEndTime()),
                        WmsLogisticsOrder::getPredictTime, dto.getPredictStartTime(), dto.getPredictEndTime())
                .between(Objects.nonNull(dto.getPracticalStartDate()) && Objects.nonNull(dto.getPracticalEndDate()),
                        WmsLogisticsOrder::getPracticalTime, dto.getPracticalStartTime(), dto.getPracticalEndTime());
        //3.处理数据
        Page<WmsLogisticsOrder> page = page(new Page<>(dto.getCurrent(), dto.getSize()), eq);
        Page<WmsLogisticsOrderVO> resPage = new Page<>();
        BeanUtil.copyProperties(page, resPage);
        List<WmsLogisticsOrderVO> list = BeanUtil.copyToList(page.getRecords(), WmsLogisticsOrderVO.class);
        Set<Integer> officeIdSet = Sets.newHashSet();
        Set<Long> dictIdSet = Sets.newHashSet();
        List<Long> orderIds = Lists.newArrayList();
        list.forEach(order -> {
            orderIds.add(order.getId());
            dictIdSet.add(order.getType());
            officeIdSet.add(order.getLogisticsCompany());
        });
        // 获取物流收发货机构
        Map<Long, List<WmsLogisticsInfoVO>> map = wmsLogisticsInfoService.mapByLogisticsId(orderIds);
        List<WmsLogisticsInfoVO> infoList = Lists.newArrayList();
        Map<Integer, String> officeMap = FeignOptional.of(remoteOfficeFeign.listByIdCh(officeIdSet)).get();
        Map<Long, String> dictMap = FeignOptional.of(remoteDictFeignApi.listByIdCh(dictIdSet)).get();
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
            order.setLogisticsCompanyName(officeMap.get(order.getLogisticsCompany()));

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
        list.forEach(wmsLogisticsInfo -> officeIds.add(wmsLogisticsInfo.getOfficeId()));
        Map<Integer, String> officeMap = FeignOptional.of(remoteOfficeFeign.listByIdCh(officeIds)).get();
        Map<Long, String> dictMap = FeignOptional.of(remoteDictFeignApi.listByIdCh(dictIds)).get();
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




