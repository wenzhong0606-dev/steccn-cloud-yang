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
import com.steccn.common.constant.LockConstant;
import com.steccn.common.constant.MpConstant;
import com.steccn.platform.feign.RemoteDictFeignApi;
import com.steccn.platform.feign.RemoteOfficeFeign;
import com.steccn.platform.feign.RemoteSpareFeignApi;
import com.steccn.platform.vo.EqpSpareVO;
import com.steccn.wms.constant.WmsManagerConstant;
import com.steccn.wms.domain.WmsDeliveryReceipt;
import com.steccn.wms.domain.WmsDeliveryReceiptInfo;
import com.steccn.wms.domain.WmsTransferSlip;
import com.steccn.wms.domain.WmsTransferSlipInfo;
import com.steccn.wms.dto.WmsDeliveryReceiptDTO;
import com.steccn.wms.dto.WmsDeliveryReceiptPageDTO;
import com.steccn.wms.service.WmsDeliveryReceiptInfoService;
import com.steccn.wms.mapper.WmsDeliveryReceiptInfoMapper;
import com.steccn.wms.util.WmsCodeUtil;
import com.steccn.wms.vo.WmsDeliveryReceiptInfoVO;
import com.steccn.wms.vo.WmsDeliveryReceiptVO;
import lombok.RequiredArgsConstructor;
import org.apache.commons.compress.utils.Lists;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

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
 * @description 针对表【wms_delivery_receipt_info(出库单明细)】的数据库操作Service实现
 * @createDate 2022-12-09 11:18:09
 */
@Service
@RequiredArgsConstructor
public class WmsDeliveryReceiptInfoServiceImpl extends ServiceImpl<WmsDeliveryReceiptInfoMapper, WmsDeliveryReceiptInfo>
        implements WmsDeliveryReceiptInfoService {

    private final RemoteDictFeignApi remoteDictFeignApi;
    private final RemoteSpareFeignApi remoteSpareFeignApi;

    @Override
    public Map<Long, BigDecimal> originalQuantity(List<Long> updateIds) {
        Map<Long, BigDecimal> map = Maps.newHashMap();
        if (CollUtil.isNotEmpty(updateIds)) {
            List<WmsDeliveryReceiptInfo> list = list(new LambdaQueryWrapper<WmsDeliveryReceiptInfo>().in(WmsDeliveryReceiptInfo::getId, updateIds));
            list.forEach(info -> {
                map.put(info.getInventoryId(), info.getQuantity());
            });
        }
        return map;
    }

    @Override
    public List<WmsDeliveryReceiptInfoVO> infoList(Long id) {
        //自定义sql 获取出库单明细信息
        List<WmsDeliveryReceiptInfoVO> list = Lists.newArrayList();
        //处理数据
        if (Objects.nonNull(id)) {
            list = this.baseMapper.infoList(id);
            Set<Long> dictIds = Sets.newHashSet();
            list.forEach(info -> dictIds.add(info.getUnit()));
            Map<Long, String> dictMap = FeignOptional.of(()->remoteDictFeignApi.listByIdCh(dictIds), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            list.forEach(info -> info.setUnitName(dictMap.get(info.getUnit())));
        }
        return list;
    }

    @Override
    public Page<WmsDeliveryReceiptInfoVO> infoPageList(WmsDeliveryReceiptPageDTO dto) {
        Page<WmsDeliveryReceiptInfoVO> page = this.baseMapper.pageList(new Page(dto.getCurrent(), dto.getSize()), dto);
        if (CollUtil.isNotEmpty(page.getRecords())){
            Set<Long> dictIds = Sets.newHashSet();
            List<Long> materialIds = Lists.newArrayList();
            for (WmsDeliveryReceiptInfoVO record : page.getRecords()) {
                dictIds.add(record.getUnit());
                materialIds.add(record.getMaterialId());
            }
            Map<Long, String> dictMap = FeignOptional.of(()->remoteDictFeignApi.listByIdCh(dictIds), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            Map<Long, EqpSpareVO> materialMap = FeignOptional.of(() -> remoteSpareFeignApi.listById(materialIds), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            page.getRecords().forEach(
                    info -> {
                        info.setUnitName(dictMap.get(info.getUnit()));
                        info.setSpecification(materialMap.get(info.getMaterialId()).getSpecifications());
                    }
            );
        }
        return page;
    }
}




