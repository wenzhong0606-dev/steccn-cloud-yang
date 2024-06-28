package com.steccn.dp.service.impl;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.steccn.authserver.util.SteccnTableAssertUtil;
import com.steccn.common.util.BigDecimalUtil;
import com.steccn.dp.domain.PullProduParDetail1;
import com.steccn.dp.domain.PullProduParDetail2;
import com.steccn.dp.service.PullProduParDetail2Service;
import com.steccn.dp.mapper.PullProduParDetail2Mapper;
import com.steccn.ems.vo.EpDeepConsumeContrastVO;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
* @author Admin
* @description 针对表【pull_produ_par_detail2】的数据库操作Service实现
* @createDate 2024-06-26 15:57:07
*/
@Service
@DS("ch")
public class PullProduParDetail2ServiceImpl extends ServiceImpl<PullProduParDetail2Mapper, PullProduParDetail2>
    implements PullProduParDetail2Service{

    @Override
    public EpDeepConsumeContrastVO getThickTrend(LocalDate date, Integer productLine) {
        List<PullProduParDetail2> model = list(new QueryWrapper<PullProduParDetail2>()
                .select("avg(id_value) AS idValue, toHour(dt) AS partitionDatehour")
                .groupBy("partitionDatehour")
                .orderBy(true,true,"partitionDatehour")
                .lambda()
                .eq(PullProduParDetail2::getProductLine,productLine)
                .ge(PullProduParDetail2::getDt, date.atTime(8, 0, 0))
                .le(PullProduParDetail2::getDt, date.plusDays(1).atTime(8, 0, 0)));
        SteccnTableAssertUtil.notEmpty(model,"数据为空");
        EpDeepConsumeContrastVO vo = new EpDeepConsumeContrastVO();
        List<String> xAxis = Lists.newArrayList();
        List<BigDecimal> yData = Lists.newArrayList();
        model.forEach(tmp->{
            yData.add(BigDecimalUtil.multiply(tmp.getIdValue(),new BigDecimal(1000),2));
        });
        vo.setYData(yData);
        vo.setXAxis(xAxis);
        return vo;
    }
}




