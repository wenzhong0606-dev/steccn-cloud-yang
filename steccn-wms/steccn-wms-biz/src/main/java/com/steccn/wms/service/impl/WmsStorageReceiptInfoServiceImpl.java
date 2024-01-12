package com.steccn.wms.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.common.constant.FeignConstant;
import com.steccn.platform.feign.RemoteDictFeignApi;
import com.steccn.platform.vo.SysDictVO;
import com.steccn.pms.constant.PmManagerConstant;
import com.steccn.pms.dto.RawMaterialReportDTO;
import com.steccn.wms.domain.WmsStorageReceiptInfo;
import com.steccn.wms.dto.WmsStorageReceiptInfoDTO;
import com.steccn.wms.service.WmsStorageReceiptInfoService;
import com.steccn.wms.mapper.WmsStorageReceiptInfoMapper;
import com.steccn.wms.vo.WmsDeliveryReceiptInfoVO;
import lombok.RequiredArgsConstructor;
import org.apache.commons.compress.utils.Lists;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author W10
 * @description 针对表【wms_storage_receipt_info】的数据库操作Service实现
 * @createDate 2022-12-09 11:18:09
 */
@Service
@RequiredArgsConstructor
public class WmsStorageReceiptInfoServiceImpl extends ServiceImpl<WmsStorageReceiptInfoMapper, WmsStorageReceiptInfo>
        implements WmsStorageReceiptInfoService {

    private final  WmsStorageReceiptInfoMapper wmsStorageReceiptInfoMapper;
    private final RemoteDictFeignApi remoteDictFeignApi;
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
}




