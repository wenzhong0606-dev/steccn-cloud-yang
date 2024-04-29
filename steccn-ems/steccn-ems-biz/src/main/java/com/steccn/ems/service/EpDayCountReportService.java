package com.steccn.ems.service;

import com.steccn.ems.domain.EpDayCountReport;
import com.baomidou.mybatisplus.extension.service.IService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

/**
 * @author yequan
 * @description 针对表【ep_day_count_report】的数据库操作Service
 * @createDate 2024-04-28 16:38:50
 */
public interface EpDayCountReportService extends IService<EpDayCountReport> {

    /**
     * 获取能耗日统计报表消耗的值
     *
     * @param energyType     能源类型
     * @param departmentName 部门名称
     * @param start          开始日期
     * @param end            结束日期
     * @return
     */
    Map<LocalDate, BigDecimal> consumeValue(String energyType, String departmentName, LocalDate start, LocalDate end);
}
