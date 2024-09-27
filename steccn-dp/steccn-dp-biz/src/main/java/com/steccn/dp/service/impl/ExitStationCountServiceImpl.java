package com.steccn.dp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.steccn.dp.domain.ExitStationCount;
import com.steccn.dp.service.ExitStationCountService;
import com.steccn.dp.mapper.ExitStationCountMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

/**
* @author 34872
* @description 针对表【exit_station_count(出站量统计)】的数据库操作Service实现
* @createDate 2024-09-26 16:46:47
*/
@Service
public class ExitStationCountServiceImpl extends ServiceImpl<ExitStationCountMapper, ExitStationCount>
    implements ExitStationCountService{

    @Override
    public Map<Long, BigDecimal> output(Set<Long> orderIds) {
        Map<Long, BigDecimal> map = new HashMap<>();
        List<ExitStationCount> list = list(new LambdaQueryWrapper<ExitStationCount>().in(ExitStationCount::getProduceOrderId, orderIds));
        list.forEach(e->{
            map.put(e.getProduceOrderId(),e.getValueCount());
        });
        return map;
    }
}




