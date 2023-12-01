package com.steccn.dp.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Maps;
import com.steccn.common.dto.TupleDTO;
import com.steccn.dp.domain.AdsPmsPullForce;
import com.steccn.dp.dto.DpPullForceDTO;
import com.steccn.dp.service.AdsPmsPullForceService;
import com.steccn.dp.mapper.AdsPmsPullForceMapper;
import com.steccn.qms.constant.QtManagerConstant;
import com.steccn.qms.util.XAxisUtil;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * @author yequan
 * @description 针对表【ads_pms_pull_force(每日拉引量统计)】的数据库操作Service实现
 * @createDate 2023-11-29 16:09:13
 */
@Service
public class AdsPmsPullForceServiceImpl extends ServiceImpl<AdsPmsPullForceMapper, AdsPmsPullForce>
        implements AdsPmsPullForceService {

    @Override
    public Map<String, BigDecimal> pullForceSum(DpPullForceDTO dto) {
        Map<String, BigDecimal> resMap = Maps.newHashMap();
        List<TupleDTO<String, BigDecimal>> dbList;

        QtManagerConstant.QtGranularityEnum granularityEnum = QtManagerConstant.QtGranularityEnum.getByCode(dto.getGranularity());
        if (QtManagerConstant.QtGranularityEnum.DAY.equals(granularityEnum)) {
            dbList = baseMapper.pullForceDay(dto.getStart(), dto.getEnd(), dto.getWorkshopIdSet());
        } else {
            dbList = baseMapper.pullForceMonth(dto.getStart(), dto.getEnd(), dto.getWorkshopIdSet());
        }
        if (CollUtil.isNotEmpty(dbList)) {
            dbList.forEach(ele -> resMap.put(ele.getField1(), ele.getField2()));
        }
        return resMap;
    }
}




