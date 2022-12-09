package com.steccn.wms.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Maps;
import com.steccn.wms.domain.WmsTransferSlipInfo;
import com.steccn.wms.service.WmsTransferSlipInfoService;
import com.steccn.wms.mapper.WmsTransferSlipInfoMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * @author W10
 * @description 针对表【wms_transfer_slip_info(调拨单明细)】的数据库操作Service实现
 * @createDate 2022-11-30 09:45:17
 */
@Service
public class WmsTransferSlipInfoServiceImpl extends ServiceImpl<WmsTransferSlipInfoMapper, WmsTransferSlipInfo>
        implements WmsTransferSlipInfoService {

    @Override
    public Map<Long, BigDecimal> originalQuantity(List<Long> idList) {
        Map<Long, BigDecimal> map = Maps.newHashMap();
        if (CollUtil.isNotEmpty(idList)) {
            List<WmsTransferSlipInfo> list = list(new LambdaQueryWrapper<WmsTransferSlipInfo>().in(WmsTransferSlipInfo::getId, idList));
            list.forEach(info -> {
                map.put(info.getId(), info.getQuantity());
            });
        }
        return map;
    }

    @Override
    public Map<Long, String> originalCaseNo(List<Long> idList) {
        Map<Long, String> map = Maps.newHashMap();
        if (CollUtil.isNotEmpty(idList)) {
            List<WmsTransferSlipInfo> list = list(new LambdaQueryWrapper<WmsTransferSlipInfo>().in(WmsTransferSlipInfo::getId, idList));
            list.forEach(info -> {
                map.put(info.getId(), info.getCaseNo());
            });
        }
        return map;
    }
}




