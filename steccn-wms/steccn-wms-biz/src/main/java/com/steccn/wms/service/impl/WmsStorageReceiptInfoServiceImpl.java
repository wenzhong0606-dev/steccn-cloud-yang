package com.steccn.wms.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.wms.domain.WmsStorageReceiptInfo;
import com.steccn.wms.service.WmsStorageReceiptInfoService;
import com.steccn.wms.mapper.WmsStorageReceiptInfoMapper;
import com.steccn.wms.vo.WmsDeliveryReceiptInfoVO;
import org.apache.commons.compress.utils.Lists;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author W10
 * @description 针对表【wms_storage_receipt_info】的数据库操作Service实现
 * @createDate 2022-12-09 11:18:09
 */
@Service
public class WmsStorageReceiptInfoServiceImpl extends ServiceImpl<WmsStorageReceiptInfoMapper, WmsStorageReceiptInfo>
        implements WmsStorageReceiptInfoService {
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
}




