package com.steccn.wms.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.lang.Assert;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Maps;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.common.constant.FeignConstant;
import com.steccn.common.util.BigDecimalUtil;
import com.steccn.common.util.GetValueUtil;
import com.steccn.common.util.LocalDateUtil;
import com.steccn.common.util.MapUtil;
import com.steccn.platform.feign.RemoteDictFeignApi;
import com.steccn.platform.vo.SysDictVO;
import com.steccn.pms.dto.RawMaterialReportDTO;
import com.steccn.wms.domain.WmsDeliveryReceiptInfo;
import com.steccn.wms.domain.WmsStorageReceiptInfo;
import com.steccn.wms.mapper.WmsDeliveryReceiptInfoMapper;
import com.steccn.wms.service.WmsDeliveryReceiptInfoService;
import com.steccn.wms.service.WmsStorageReceiptInfoService;
import com.steccn.wms.mapper.WmsStorageReceiptInfoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author W10
 * @description 针对表【wms_storage_receipt_info】的数据库操作Service实现
 * @createDate 2022-12-09 11:18:09
 */
@Service
@RequiredArgsConstructor
public class WmsStorageReceiptInfoServiceImpl extends ServiceImpl<WmsStorageReceiptInfoMapper, WmsStorageReceiptInfo>
        implements WmsStorageReceiptInfoService {

    private final WmsStorageReceiptInfoMapper wmsStorageReceiptInfoMapper;
    private final RemoteDictFeignApi remoteDictFeignApi;
    private final WmsDeliveryReceiptInfoMapper wmsDeliveryReceiptInfoMapper;

    @Override
    public Map<Long, WmsStorageReceiptInfo> infoMap(List<Long> updateIds) {
        Map<Long, WmsStorageReceiptInfo> map = Maps.newHashMap();
        if (CollUtil.isNotEmpty(updateIds)) {
            List<WmsStorageReceiptInfo> list = list(new LambdaQueryWrapper<WmsStorageReceiptInfo>().in(WmsStorageReceiptInfo::getId, updateIds));
            list.forEach(wmsStorageReceiptInfo -> {
                map.put(wmsStorageReceiptInfo.getId(), wmsStorageReceiptInfo);
            });
        }
        return map;
    }

    @Override
    public WmsStorageReceiptInfo getByCaseNo(String caseNo) {
       return wmsStorageReceiptInfoMapper.getByCaseNo(caseNo);
    }

    @Override
    public Map<String, BigDecimal> purchaseQuantity(RawMaterialReportDTO dto) {
        Map<String,BigDecimal>  resMap = Maps.newHashMap();
        //获取采购类型
        SysDictVO sysDictVO = FeignOptional.of(() -> remoteDictFeignApi.getByCode("1", "warehousing_type"), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        List<WmsStorageReceiptInfo> infoList = this.baseMapper.purchaseQuantity(sysDictVO.getId(),dto.getStartTime(),dto.getEndTime(),dto.getMaterialName());
        if (CollUtil.isNotEmpty(infoList)){
            infoList.forEach( info->{
                resMap.put(info.getMaterialName(),info.getQuantity());
            });
        }
        return resMap;
    }

    @Override
    public Map<LocalDate, BigDecimal> produceConsumeQuantity(Long materialId, LocalDate start, LocalDate end) {
        Map<LocalDate, BigDecimal> resMap = Maps.newHashMap();
        SysDictVO sysDictVO1 = FeignOptional.of(() -> remoteDictFeignApi.getByCode("4", "warehousing_type"), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        Assert.notNull(sysDictVO1, "[生产退料]字典不存在");
        SysDictVO sysDictVO2 = FeignOptional.of(() -> remoteDictFeignApi.getByCode("0", "outbound_type"), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        Assert.notNull(sysDictVO2, "[生产领料]字典不存在");
        //获取物料确认日期在[start,end]中的消耗数量
        LocalDateTime startLdt = LocalDateUtil.getMorning(start);
        LocalDateTime endLdt = LocalDateUtil.getMorning(end).plusDays(1);
        List<WmsStorageReceiptInfo> list1 = wmsStorageReceiptInfoMapper.produceReturnQuantity(sysDictVO1.getId(), materialId, startLdt, endLdt);
        List<WmsStorageReceiptInfo> list2 = wmsDeliveryReceiptInfoMapper.produceConsumeQuantity(sysDictVO2.getId(), materialId, startLdt, endLdt);
        if (CollUtil.isNotEmpty(list1) || CollUtil.isNotEmpty(list2)) {
            while (LocalDateUtil.le(start, end)) {
                LocalDate finalStart = start;
                List<BigDecimal> collect1 = list1.stream().filter(ele -> finalStart.isEqual(ele.getValidTime())).map(WmsStorageReceiptInfo::getQuantity).collect(Collectors.toList());
                List<BigDecimal> collect2 = list2.stream().filter(ele -> finalStart.isEqual(ele.getValidTime())).map(WmsStorageReceiptInfo::getQuantity).collect(Collectors.toList());
                BigDecimal value1 = BigDecimalUtil.sum(collect1);
                BigDecimal value2 = BigDecimalUtil.sum(collect2);
                BigDecimal subtract = BigDecimalUtil.subtract(value2, value1);
                if (Objects.nonNull(subtract)) {
                    resMap.put(start, subtract);
                }
                start = start.plusDays(1);
            }
        }
        return resMap;
    }
}




