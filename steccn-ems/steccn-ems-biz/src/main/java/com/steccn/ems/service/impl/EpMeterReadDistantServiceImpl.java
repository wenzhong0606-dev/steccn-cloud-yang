package com.steccn.ems.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.steccn.common.util.BigDecimalUtil;
import com.steccn.ems.domain.EpMeterReadDistant;
import com.steccn.ems.service.EpMeterReadDistantService;
import com.steccn.ems.mapper.EpMeterReadDistantMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * @author yequan
 * @description 针对表【ep_meter_read_distant(远传仪表读数数据)】的数据库操作Service实现
 * @createDate 2023-05-15 14:16:43
 */
@Service
public class EpMeterReadDistantServiceImpl extends ServiceImpl<EpMeterReadDistantMapper, EpMeterReadDistant>
        implements EpMeterReadDistantService {

    @Override
    public List<BigDecimal> selectAllThick(LocalDate start, LocalDate end) {
        List<BigDecimal> resList = Lists.newArrayList(BigDecimalUtil.MINUS_ONE);
        List<EpMeterReadDistant> dbList = list(new QueryWrapper<EpMeterReadDistant>()
                .select("DISTINCT thick").lambda()
                .between(EpMeterReadDistant::getReadDate, start, end)
                .isNotNull(EpMeterReadDistant::getThick));
        if (CollUtil.isNotEmpty(dbList)) {
            for (EpMeterReadDistant model : dbList) {
                resList.add(model.getThick());
            }
        }
        return resList;
    }
}




