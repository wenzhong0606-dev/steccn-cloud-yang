package com.steccn.dp.service.impl;

import cn.hutool.core.lang.Assert;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.steccn.authserver.util.SteccnTableAssertUtil;
import com.steccn.common.util.BigDecimalUtil;
import com.steccn.dp.domain.PullProduParCount;
import com.steccn.dp.domain.PullProduParDetail1;
import com.steccn.dp.service.PullProduParDetail1Service;
import com.steccn.dp.mapper.PullProduParDetail1Mapper;
import com.steccn.ems.vo.EpDeepConsumeContrastVO;
import org.apache.http.util.Asserts;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
* @author Admin
* @description 针对表【pull_produ_par_detail1】的数据库操作Service实现
* @createDate 2024-06-26 15:57:02
*/
@Service
@DS("ch")
public class PullProduParDetail1ServiceImpl extends ServiceImpl<PullProduParDetail1Mapper, PullProduParDetail1>
    implements PullProduParDetail1Service{

    @Override
    public EpDeepConsumeContrastVO getSpeedTrend(LocalDate date, Integer productLine) {
        List<PullProduParDetail1> model = list(new QueryWrapper<PullProduParDetail1>()
                .select("avg(id_value) AS idValue, toHour(dt) AS partitionDatehour")
                .lambda()
                .eq(PullProduParDetail1::getProductLine,productLine)
                .ge(PullProduParDetail1::getDt, date.atTime(8, 0, 0))
                .le(PullProduParDetail1::getDt, date.plusDays(1).atTime(8, 0, 0))
                .groupBy(PullProduParDetail1::getPartitionDatehour)
                .orderBy(true,true,PullProduParDetail1::getPartitionDatehour));
        SteccnTableAssertUtil.notEmpty(model,"数据为空");
        EpDeepConsumeContrastVO vo = new EpDeepConsumeContrastVO();
        List<String> xAxis = Lists.newArrayList();
        List<BigDecimal> yData = Lists.newArrayList();
        model.forEach(tmp->{
            yData.add(BigDecimalUtil.setScale(tmp.getIdValue(),2));
        });
        vo.setYData(yData);
        vo.setXAxis(xAxis);
        return vo;
    }
}




