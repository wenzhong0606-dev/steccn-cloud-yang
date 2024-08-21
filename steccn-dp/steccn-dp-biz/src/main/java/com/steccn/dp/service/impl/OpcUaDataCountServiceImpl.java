package com.steccn.dp.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.steccn.common.dto.TripleDTO;
import com.steccn.common.dto.TupleDTO;
import com.steccn.common.util.BigDecimalUtil;
import com.steccn.dp.domain.OpcUaDataCount;
import com.steccn.dp.service.OpcUaDataCountService;
import com.steccn.dp.mapper.OpcUaDataCountMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.Lists;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

/**
* @author Admin
* @description 针对表【opc_ua_data_count】的数据库操作Service实现
* @createDate 2024-08-21 14:36:31
*/
@Service
@Slf4j
public class OpcUaDataCountServiceImpl extends ServiceImpl<OpcUaDataCountMapper, OpcUaDataCount>
    implements OpcUaDataCountService{

    @Override
    public List<TupleDTO<String, Integer>> category(LocalDateTime time) {
        List<TupleDTO<String, Integer>> resList = Lists.newArrayList();
        List<OpcUaDataCount> dbList = baseMapper.selectList(new QueryWrapper<OpcUaDataCount>().select("id_category, sum(id_value) as idValue").lambda().eq(OpcUaDataCount::getDt, time)
                .groupBy(OpcUaDataCount::getIdCategory));
        if(CollUtil.isNotEmpty(dbList)){
            dbList.sort(Comparator.comparing(OpcUaDataCount::getIdValue).reversed());
            dbList.forEach(ele -> {
                resList.add(new TupleDTO<>(ele.getIdCategory(), ele.getIdValue().intValue()));
            });
        }
        return resList;
    }

    @Override
    public List<TripleDTO<String, String, Integer>> detail(LocalDateTime time, String categoryCode) {
        List<TripleDTO<String, String, Integer>> resList = Lists.newArrayList();
        List<OpcUaDataCount> dbList = baseMapper.detail(time,categoryCode);
        if(CollUtil.isNotEmpty(dbList)){
            dbList.sort(Comparator.comparing(OpcUaDataCount::getIdValue).reversed());
            dbList.forEach(ele -> {
                resList.add(new TripleDTO<>(ele.getId(),ele.getPath(), ele.getIdValue().intValue()));
            });
        }
        return resList;
    }
}




