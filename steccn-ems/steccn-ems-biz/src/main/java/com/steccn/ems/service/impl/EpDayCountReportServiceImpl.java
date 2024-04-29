package com.steccn.ems.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Maps;
import com.steccn.ems.domain.EpDayCountReport;
import com.steccn.ems.service.EpDayCountReportService;
import com.steccn.ems.mapper.EpDayCountReportMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author yequan
 * @description 针对表【ep_day_count_report】的数据库操作Service实现
 * @createDate 2024-04-28 16:38:50
 */
@Service
public class EpDayCountReportServiceImpl extends ServiceImpl<EpDayCountReportMapper, EpDayCountReport>
        implements EpDayCountReportService {

    @Override
    public Map<LocalDate, BigDecimal> consumeValue(String energyType, String departmentName, LocalDate start, LocalDate end) {
        Map<LocalDate, BigDecimal> resMap = Maps.newHashMap();
        if (StrUtil.isNotBlank(energyType) && StrUtil.isNotBlank(departmentName) && Objects.nonNull(start) && Objects.nonNull(end)) {
            LambdaQueryWrapper<EpDayCountReport> eq = new LambdaQueryWrapper<EpDayCountReport>().eq(EpDayCountReport::getEnergyType, energyType)
                    .eq(EpDayCountReport::getDepartmentName, departmentName)
                    .ge(EpDayCountReport::getClassDate, start)
                    .le(EpDayCountReport::getClassDate, end);
            List<EpDayCountReport> list = list(eq);
            if (CollUtil.isNotEmpty(list)) {
                list.forEach(ele -> {
                    resMap.put(ele.getClassDate(), ele.getConsumeValue());
                });
            }
        }
        return resMap;
    }
}




