package com.steccn.qms.util;

import cn.hutool.core.lang.Assert;
import com.steccn.common.util.JointUtil;
import com.steccn.common.util.ShardTimeUtil;
import com.steccn.qms.constant.QtManagerConstant;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * @author yequan
 * @desc mysql的日期工具类
 * @date 2023/12/13
 */
public class MysqlDateKeyUtil {

    public static final DateTimeFormatter HOUR_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH");
    public static final DateTimeFormatter DAY_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    public static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");


    /**
     * X轴显示时间段标签格式
     * 1）天：YYYY-MM-DD
     * 3）周：YYYYXX周 eg.202350,202304
     * 4）月：YYYY-MM
     * 5）季度：YYYY-X eg.2023-4
     * 6）年：YYYY
     *
     * @param granularityEnum 粒度
     * @param ldt             时间
     * @return
     */
    public static String convert(QtManagerConstant.QtGranularityEnum granularityEnum, LocalDateTime ldt) {
        String mysqlKey;
        if (QtManagerConstant.QtGranularityEnum.HOUR.getCode().equals(granularityEnum.getCode())) {
            mysqlKey = XAxisUtil.convert(granularityEnum, ldt);
        } else if (QtManagerConstant.QtGranularityEnum.CLASS.getCode().equals(granularityEnum.getCode())) {
            mysqlKey = XAxisUtil.convert(granularityEnum, ldt);
        } else if (QtManagerConstant.QtGranularityEnum.DAY.getCode().equals(granularityEnum.getCode())) {
            mysqlKey = XAxisUtil.convert(granularityEnum, ldt);
        } else if (QtManagerConstant.QtGranularityEnum.WEEK.getCode().equals(granularityEnum.getCode())) {
            mysqlKey = ShardTimeUtil.date2Week(ldt.toLocalDate());
        } else if (QtManagerConstant.QtGranularityEnum.MONTH.getCode().equals(granularityEnum.getCode())) {
            mysqlKey = XAxisUtil.convert(granularityEnum, ldt);
        } else if (QtManagerConstant.QtGranularityEnum.QUARTER.getCode().equals(granularityEnum.getCode())) {
            mysqlKey = JointUtil.joint("-", ldt.getYear(), quater(ldt.getMonthValue()));
        } else if (QtManagerConstant.QtGranularityEnum.YEAR.getCode().equals(granularityEnum.getCode())) {
            mysqlKey = String.valueOf(ldt.getYear());
        } else {
            //无粒度
            mysqlKey = "NONE_" + XAxisUtil.convert(granularityEnum, ldt);
        }
        return mysqlKey;
    }

    /**
     * 日期转换
     *
     * @param granularityEnum 粒度
     * @param ld              日期
     * @return
     */
    public static String convert(QtManagerConstant.QtGranularityEnum granularityEnum, LocalDate ld) {
        Assert.isTrue(!QtManagerConstant.QtGranularityEnum.HOUR.getCode().equals(granularityEnum.getCode()), "日期粒度不可以为小时");
        return convert(granularityEnum, LocalDateTime.of(ld, LocalTime.MIN));
    }

    /**
     * 季度计算
     *
     * @param month
     * @return
     */
    private static String quater(int month) {
        String quater;
        if (month >= 1 && month <= 3) {
            quater = "1";
        } else if (month >= 4 && month <= 6) {
            quater = "2";
        } else if (month >= 7 && month <= 9) {
            quater = "3";
        } else {
            quater = "4";
        }
        return quater;
    }
}
