package com.steccn.data.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.steccn.common.util.SplitListUtil;
import com.steccn.data.domain.EpMeterCollect;
import com.steccn.data.vo.DwEpMeterCollectVO;
import com.steccn.data.dto.DwMeterReadDTO;
import com.steccn.data.service.EpMeterCollectService;
import com.steccn.data.mapper.EpMeterCollectMapper;
import org.apache.commons.compress.utils.Lists;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author yequan
 * @description 针对表【ep_meter_collect(能源绩效-远程仪表采样表)】的数据库操作Service实现
 * @createDate 2022-12-16 13:23:55
 */
@Service
public class EpMeterCollectServiceImpl extends ServiceImpl<EpMeterCollectMapper, EpMeterCollect>
        implements EpMeterCollectService {

    private final ConcurrentHashMap<String, LocalDateTime> lastRecordTimeMap = new ConcurrentHashMap<>(1024);

    @Override
    public List<DwEpMeterCollectVO> meterRead(List<DwMeterReadDTO> dwList) {
        Set<DwEpMeterCollectVO> dataCollect = Sets.newHashSet();
        if (CollUtil.isNotEmpty(dwList)) {
            List<List<DwMeterReadDTO>> splitList = SplitListUtil.split(dwList, 5);
            for (List<DwMeterReadDTO> split : splitList) {
                //批量查询
                List<EpMeterCollect> dbList = this.baseMapper.batchSearch(split);
                if (CollUtil.isNotEmpty(dbList)) {
                    dataCollect.addAll(BeanUtil.copyToList(dbList, DwEpMeterCollectVO.class));
                }
            }
        }
        return new ArrayList<>(dataCollect);
    }

    @Override
    public Map<String, LocalDateTime> lastReadTime(Set<String> distantCodeSet) {
        Map<String, LocalDateTime> resMap = Maps.newHashMap();
        if (CollUtil.isNotEmpty(distantCodeSet)) {
            distantCodeSet.forEach(ele -> {
                LocalDateTime lastRecordTime = lastRecordTimeMap.get(ele);
                resMap.put(ele, lastRecordTime);
            });
        }
        return resMap;
    }

    @Override
    public void addLastRecordTime(List<EpMeterCollect> batchList) {
        if (CollUtil.isNotEmpty(batchList)) {
            batchList.forEach(ele -> {
                lastRecordTimeMap.put(ele.getCode(), ele.getRecordTime());
            });
        }
    }

    @Override
    public Boolean refreshLastReadTime() {
        List<EpMeterCollect> dbList = list(new QueryWrapper<EpMeterCollect>().select("code, max(record_time) as recordTime")
                .lambda().ge(EpMeterCollect::getRecordTime, LocalDateTime.of(LocalDate.now(), LocalTime.MIN))
                .groupBy(EpMeterCollect::getCode));
        //如果集合不为空
        if (CollUtil.isNotEmpty(dbList)) {
            dbList.forEach(ele -> {
                lastRecordTimeMap.put(ele.getCode(), ele.getRecordTime());
            });
        }
        return Boolean.TRUE;
    }
}