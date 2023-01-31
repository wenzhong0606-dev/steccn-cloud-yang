package com.steccn.wms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.common.constant.FeignConstant;
import com.steccn.platform.feign.RemoteOfficeFeign;
import com.steccn.wms.domain.WmsLogisticsInfo;
import com.steccn.wms.dto.WmsLogisticsInfoDTO;
import com.steccn.wms.service.WmsLogisticsInfoService;
import com.steccn.wms.mapper.WmsLogisticsInfoMapper;
import com.steccn.wms.vo.WmsLogisticsInfoVO;
import io.prometheus.client.Collector;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author W10
 * @description 针对表【wms_logistics_info(收发货信息)】的数据库操作Service实现
 * @createDate 2022-11-30 09:45:17
 */
@Service
@RequiredArgsConstructor
public class WmsLogisticsInfoServiceImpl extends ServiceImpl<WmsLogisticsInfoMapper, WmsLogisticsInfo>
        implements WmsLogisticsInfoService {

    private final RemoteOfficeFeign remoteOfficeFeign;

    @Override
    public Map<Long, List<WmsLogisticsInfoVO>> mapByLogisticsId(List<Long> orderIds) {
        Map<Long, List<WmsLogisticsInfoVO>> map = Maps.newHashMap();
        if (CollUtil.isNotEmpty(orderIds)) {
            List<WmsLogisticsInfo> list = list(new LambdaQueryWrapper<WmsLogisticsInfo>().in(WmsLogisticsInfo::getLogisticsId, orderIds));
            if (CollUtil.isNotEmpty(list)) {
                List<WmsLogisticsInfoVO> wmsLogisticsInfo = BeanUtil.copyToList(list, WmsLogisticsInfoVO.class);
                Set<Integer> officeIdSet = Sets.newHashSet();
                wmsLogisticsInfo.forEach( info -> officeIdSet.add(info.getOfficeId()));
                Map<Integer, String> officeMap = FeignOptional.of(()->remoteOfficeFeign.listByIdCh(officeIdSet), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
                wmsLogisticsInfo.forEach( info ->{
                    info.setOfficeName(officeMap.get(info.getOfficeId()));
                });
                map = wmsLogisticsInfo.stream().collect(Collectors.groupingBy(WmsLogisticsInfoVO::getLogisticsId));
            }
        }
        return map;
    }
}




