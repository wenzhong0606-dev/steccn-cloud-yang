package com.steccn.ems.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.steccn.common.util.LocalDateUtil;
import com.steccn.ems.domain.EpHeatPower;
import com.steccn.ems.service.EpHeatPowerService;
import com.steccn.ems.mapper.EpHeatPowerMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * @author yequan
 * @description 针对表【ep_heat_power(余热发电表)】的数据库操作Service实现
 * @createDate 2023-06-05 14:33:28
 */
@Service
public class EpHeatPowerServiceImpl extends ServiceImpl<EpHeatPowerMapper, EpHeatPower>
        implements EpHeatPowerService {

    @Override
    public List<BigDecimal> heatPower(LocalDate start, LocalDate end) {
        //定义返回集合
        List<BigDecimal> resList = Lists.newArrayList();
        //定义日期集合
        List<LocalDate> ldList = Lists.newArrayList();
        //计算日期区间内，全部班次日期的集合
        while (LocalDateUtil.le(start, end)) {
            ldList.add(start);
            start = start.plusDays(1);
        }
        //查询所有班次日期的余热发电数据
        List<EpHeatPower> dbList = list(new LambdaQueryWrapper<EpHeatPower>()
                .in(EpHeatPower::getClassDate, ldList));
        //班次日期 - 余热发电数据
        Map<LocalDate, BigDecimal> valueMap = Maps.newHashMap();
        if (CollUtil.isNotEmpty(dbList)) {
            dbList.forEach(ele -> valueMap.put(ele.getClassDate(), ele.getValue()));
        }
        ldList.forEach(ele -> resList.add(valueMap.getOrDefault(ele, BigDecimal.ZERO)));
        return resList;
    }
}




