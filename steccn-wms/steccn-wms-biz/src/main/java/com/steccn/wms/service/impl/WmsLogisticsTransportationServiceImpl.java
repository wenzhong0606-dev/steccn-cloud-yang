package com.steccn.wms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Maps;
import com.steccn.wms.domain.WmsLogisticsTransportation;
import com.steccn.wms.dto.WmsLogisticsOrderDTO;
import com.steccn.wms.service.WmsLogisticsTransportationService;
import com.steccn.wms.mapper.WmsLogisticsTransportationMapper;
import com.steccn.wms.vo.WmsLogisticsTransportationVO;
import io.micrometer.core.instrument.util.StringEscapeUtils;
import io.micrometer.core.instrument.util.StringUtils;
import org.apache.commons.compress.utils.Lists;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author W10
 * @description 针对表【wms_logistics_transportation(运输方式)】的数据库操作Service实现
 * @createDate 2022-11-30 09:45:17
 */
@Service
public class WmsLogisticsTransportationServiceImpl extends ServiceImpl<WmsLogisticsTransportationMapper, WmsLogisticsTransportation>
        implements WmsLogisticsTransportationService {

    @Override
    public List<WmsLogisticsTransportationVO> listByLogisticsId(Long id) {
        List<WmsLogisticsTransportationVO> list = Lists.newArrayList();
        if (Objects.nonNull(id)) {
            List<WmsLogisticsTransportation> logisticsTransportations = list(new LambdaQueryWrapper<WmsLogisticsTransportation>().in(WmsLogisticsTransportation::getLogisticsId, id));
            if (CollUtil.isNotEmpty(logisticsTransportations)) {
                logisticsTransportations.forEach(way -> {
                    WmsLogisticsTransportationVO wmsLogisticsTransportationVO = new WmsLogisticsTransportationVO();
                    BeanUtil.copyProperties(way, wmsLogisticsTransportationVO);
                    list.add(wmsLogisticsTransportationVO);
                });
            }
        }
        return list;
    }

    @Override
    public Boolean edit(WmsLogisticsOrderDTO dto) {
        remove(new LambdaQueryWrapper<WmsLogisticsTransportation>()
                .eq(WmsLogisticsTransportation::getLogisticsId, dto.getId()));
        List<WmsLogisticsTransportation> wayList = BeanUtil.copyToList(dto.getWayList(), WmsLogisticsTransportation.class);
        wayList.forEach( way-> way.setLogisticsId(dto.getId()));
        return saveOrUpdateBatch(wayList);
    }

    @Override
    public Map<Long, List<WmsLogisticsTransportationVO>> mapBySetIds(Set<Long> logisticsIds) {
        Map<Long, List<WmsLogisticsTransportationVO>> map = Maps.newHashMap();
        if (CollUtil.isNotEmpty(logisticsIds)){
            LambdaQueryWrapper<WmsLogisticsTransportation> in = new LambdaQueryWrapper<WmsLogisticsTransportation>().in(WmsLogisticsTransportation::getLogisticsId, logisticsIds);
            List<WmsLogisticsTransportation> list = list(in);
            if (CollUtil.isNotEmpty(list)){
                Map<Long, List<WmsLogisticsTransportation>> collect = list.stream().collect(Collectors.groupingBy(WmsLogisticsTransportation::getLogisticsId));
                collect.forEach((logistics,wayList) ->{
                    List<WmsLogisticsTransportationVO> wayVoList = BeanUtil.copyToList(wayList, WmsLogisticsTransportationVO.class);
                    map.put(logistics,wayVoList);
                });
            }
        }
        return map;
    }
}




