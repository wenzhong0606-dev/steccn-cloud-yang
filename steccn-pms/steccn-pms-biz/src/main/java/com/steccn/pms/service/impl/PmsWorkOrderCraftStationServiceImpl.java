package com.steccn.pms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Maps;
import com.steccn.pms.domain.PmsWorkOrderCraftStation;
import com.steccn.pms.service.PmsWorkOrderCraftStationService;
import com.steccn.pms.mapper.PmsWorkOrderCraftStationMapper;
import com.steccn.pms.vo.PmsWorkOrderCraftStationVO;
import org.apache.commons.compress.utils.Lists;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author yequan
 * @description 针对表【pms_work_order_craft_station(生产订单-工艺信息-工位信息)】的数据库操作Service实现
 * @createDa    te 2022-09-28 15:18:51
 */
@Service
public class PmsWorkOrderCraftStationServiceImpl extends ServiceImpl<PmsWorkOrderCraftStationMapper, PmsWorkOrderCraftStation>
        implements PmsWorkOrderCraftStationService {


    @Override
    public Map<Long,List<PmsWorkOrderCraftStationVO>> stations(Set<Long> craftSet) {
        Map<Long,List<PmsWorkOrderCraftStationVO>> resMap = Maps.newHashMap();
        if (CollUtil.isNotEmpty(craftSet)) {
            List<PmsWorkOrderCraftStation> list = list(new LambdaQueryWrapper<PmsWorkOrderCraftStation>()
                    .in(PmsWorkOrderCraftStation::getProduceOrderCraftId, craftSet));
//                    .groupBy(PmsWorkOrderCraftStation::getProduceOrderCraftId));
            if (CollUtil.isNotEmpty(list)) {
                Map<Long, List<PmsWorkOrderCraftStation>> collect = list.parallelStream()
                        .collect(Collectors.groupingBy(PmsWorkOrderCraftStation::getProduceOrderCraftId));
                collect.forEach((key,value) ->{
                    List<PmsWorkOrderCraftStationVO> voList = BeanUtil.copyToList(value, PmsWorkOrderCraftStationVO.class);
                    resMap.put(key,voList);
                });
            }
        }
        return resMap;
    }
}




