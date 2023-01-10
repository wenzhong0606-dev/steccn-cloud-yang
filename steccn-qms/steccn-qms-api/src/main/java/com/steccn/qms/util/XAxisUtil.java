package com.steccn.qms.util;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.LocalDateTimeUtil;
import com.steccn.qms.constant.QtManagerConstant;
import lombok.extern.slf4j.Slf4j;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;

/**
 * @author yequan
 * @desc x轴的转换
 */
@Slf4j
public class XAxisUtil {

    /**
     * X轴显示时间段标签格式
     * 1）天：YYYY-MM-DD
     * 3）周：第X周 MM-DD（该周第一天的日期）
     * 4）月：YYYY-MM
     * 5）季度：YYYY-X季度
     * 6）年：YYYY
     *
     * @param granularityEnum 粒度
     * @param ldt             时间
     * @return
     */
    public static String convert(QtManagerConstant.QtGranularityEnum granularityEnum, LocalDateTime ldt) {
        String xAxis;
        if(QtManagerConstant.QtGranularityEnum.HOUR.getCode().equals(granularityEnum.getCode())){
            xAxis = LocalDateTimeUtil.format(ldt, "MM-dd HH:mm");
        } else if (QtManagerConstant.QtGranularityEnum.DAY.getCode().equals(granularityEnum.getCode())) {
            xAxis = LocalDateTimeUtil.format(ldt, DatePattern.NORM_DATE_PATTERN);
        } else if (QtManagerConstant.QtGranularityEnum.WEEK.getCode().equals(granularityEnum.getCode())) {
            LocalDateTime monday = ldt.with(DayOfWeek.MONDAY);
            LocalDateTime sunday = ldt.with(DayOfWeek.SUNDAY);
            xAxis = LocalDateTimeUtil.format(monday, "MM.dd") + "-" + LocalDateTimeUtil.format(sunday, "MM.dd");
        } else if (QtManagerConstant.QtGranularityEnum.MONTH.getCode().equals(granularityEnum.getCode())) {
            xAxis = LocalDateTimeUtil.format(ldt, "yyyy-MM");
        } else if (QtManagerConstant.QtGranularityEnum.QUARTER.getCode().equals(granularityEnum.getCode())) {
            xAxis = LocalDateTimeUtil.format(ldt, "yyyy") + "-" + quater(ldt.getMonth().getValue()) + "季度";
        } else {
            xAxis = LocalDateTimeUtil.format(ldt, "yyyy");
        }
        return xAxis;
    }

    /**
     * 季度计算
     *
     * @param month
     * @return
     */
    private static int quater(int month) {
        int quater = 4;
        if (month >= 1 && month <= 3) {
            quater = 1;
        } else if (month >= 4 && month <= 6) {
            quater = 2;
        } else if (month >= 7 && month <= 9) {
            quater = 3;
        }
        return quater;
    }
}
