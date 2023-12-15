package com.steccn.dp.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Maps;
import com.steccn.common.dto.TupleDTO;
import com.steccn.common.util.BigDecimalUtil;
import com.steccn.common.util.LocalDateUtil;
import com.steccn.common.util.ShardTimeUtil;
import com.steccn.dp.domain.AdsPmsPullForce;
import com.steccn.dp.dto.DpPullForceDTO;
import com.steccn.dp.service.AdsPmsPullForceService;
import com.steccn.dp.mapper.AdsPmsPullForceMapper;
import com.steccn.qms.constant.QtManagerConstant;
import com.steccn.qms.util.MysqlDateKeyUtil;
import com.steccn.qms.util.XAxisUtil;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

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
        QtManagerConstant.QtGranularityEnum granularity = QtManagerConstant.QtGranularityEnum.getByCode(dto.getGranularity());
        Assert.notNull(granularity, "粒度不存在");
        List<AdsPmsPullForce> dataList = this.list(new QueryWrapper<AdsPmsPullForce>()
                .select("pull_force_date, sum(quantity) as quantity").lambda()
                .in(CollUtil.isNotEmpty(dto.getWorkshopIdSet()), AdsPmsPullForce::getWorkshopId, dto.getWorkshopIdSet())
                .ge(Objects.nonNull(dto.getStart()), AdsPmsPullForce::getPullForceDate, dto.getStart())
                .le(Objects.nonNull(dto.getEnd()), AdsPmsPullForce::getPullForceDate, dto.getEnd())
                .groupBy(AdsPmsPullForce::getPullForceDate));
        if (CollUtil.isNotEmpty(dataList)) {
            List<LocalDate> dateList = ShardTimeUtil.cutDate(granularity.getType(), dto.getStart(), dto.getEnd());
            Assert.notEmpty(dataList, "日期区间有误");
            for (int i = 1; i < dateList.size(); i++) {
                LocalDate left = dateList.get(i - 1);
                LocalDate right = dateList.get(i);
                String convert = MysqlDateKeyUtil.convert(granularity, left);
                List<BigDecimal> valueList = dataList.stream().filter(xx -> LocalDateUtil.isRange(xx.getPullForceDate(), left, right, Boolean.TRUE, Boolean.FALSE)).map(AdsPmsPullForce::getQuantity).collect(Collectors.toList());
                resMap.put(convert, BigDecimalUtil.sum(valueList));
            }
        }
        return resMap;
    }
}




