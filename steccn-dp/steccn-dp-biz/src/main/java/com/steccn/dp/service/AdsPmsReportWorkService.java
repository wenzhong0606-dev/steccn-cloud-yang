package com.steccn.dp.service;

import com.steccn.dp.domain.AdsPmsReportWork;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.qms.constant.QtManagerConstant;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.Set;

/**
 * @author yequan
 * @description 针对表【ads_pms_report_work(各车间产线工序每日报工统计)】的数据库操作Service
 * @createDate 2023-11-29 16:09:13
 */
public interface AdsPmsReportWorkService extends IService<AdsPmsReportWork> {

    /**
     * 深加工完工信息
     *
     * @param granularity 粒度
     * @param start       开始日期
     * @param end         结束日期
     * @param wsIdSet     车间id集合
     * @param bit         位数
     * @return
     */
    Map<String, BigDecimal> sjgComplete(QtManagerConstant.QtGranularityEnum granularity, LocalDate start, LocalDate end, Set<Integer> wsIdSet, Integer bit);

    /**
     * 基片完工信息
     *
     * @param granularity 粒度
     * @param start       开始日期
     * @param end         结束日期
     * @param wsIdSet     车间id集合
     * @param bit         位数
     * @return
     */
    Map<String, BigDecimal> jpComplete(QtManagerConstant.QtGranularityEnum granularity, LocalDate start, LocalDate end, Set<Integer> wsIdSet, int bit);

    /**
     * 实际使用
     *
     * @param granularity 粒度
     * @param start       开始日期
     * @param end         结束日期
     * @param wsIdSet     车间id集合
     * @param bit         位数
     * @return
     */
    Map<String, BigDecimal> actualUse(QtManagerConstant.QtGranularityEnum granularity, LocalDate start, LocalDate end, Set<Integer> wsIdSet, int bit);
}
