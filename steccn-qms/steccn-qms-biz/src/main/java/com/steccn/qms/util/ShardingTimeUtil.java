package com.steccn.qms.util;

import cn.hutool.core.date.DatePattern;
import com.steccn.qms.constant.QtManagerConstant;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.Lists;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * 时间分片工具类
 */
@Slf4j
public class ShardingTimeUtil {

    /**
     * 切割時間段
     *
     * @param dateType 交易類型 Y/Q/M/W/D/H -->每年/每季度/每月/每周/每天/每小時
     * @param start    yyyy-MM-dd HH:mm:ss
     * @param end      yyyy-MM-dd HH:mm:ss
     * @return
     */
    public static List<String> cutDate(String dateType, String start, String end) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(DatePattern.NORM_DATETIME_PATTERN);
            Date dBegin = sdf.parse(start);
            Date dEnd = sdf.parse(end);
            return findDates(dateType, dBegin, dEnd);
        } catch (Exception e) {
            log.error(e.getLocalizedMessage());
        }
        return Lists.newArrayList();
    }

    public static List<String> findDates(String dateType, Date dBegin, Date dEnd) throws Exception {
        List<String> listDate = new ArrayList<>();
        Calendar calBegin = Calendar.getInstance();
        calBegin.setTime(dBegin);
        Calendar calEnd = Calendar.getInstance();
        calEnd.setTime(dEnd);
        while (calEnd.after(calBegin)) {
            switch (dateType) {
                case "Y":
                    calBegin.add(Calendar.YEAR, 1);
                    break;
                case "Q":
                    int currentMonth = calBegin.get(Calendar.MONTH) + 1;
                    if (currentMonth >= 1 && currentMonth <= 3) {
                        calBegin.set(Calendar.MONTH, 0);
                    } else if (currentMonth >= 4 && currentMonth <= 6) {
                        calBegin.set(Calendar.MONTH, 3);
                    } else if (currentMonth >= 7 && currentMonth <= 9) {
                        calBegin.set(Calendar.MONTH, 6);
                    } else if (currentMonth >= 10 && currentMonth <= 12) {
                        calBegin.set(Calendar.MONTH, 9);
                    }
                    calBegin.set(Calendar.DATE, 1);
                    calBegin.add(Calendar.MONTH, 3);
                    break;
                case "M":
                    calBegin.set(Calendar.WEEK_OF_MONTH, 1);
                    calBegin.add(Calendar.MONTH, 1);
                    break;
                case "W":
                    calBegin.add(Calendar.WEEK_OF_MONTH, 1);
                    break;
                case "D":
                    calBegin.add(Calendar.DAY_OF_YEAR, 1);
                    break;
                case "H":
                    calBegin.add(Calendar.HOUR, 1);
                    break;
            }
            if (calEnd.after(calBegin)) {
                if (QtManagerConstant.QtGranularityEnum.HOUR.getType().equals(dateType)) {
                    listDate.add(new SimpleDateFormat(DatePattern.NORM_DATETIME_PATTERN).format(calBegin.getTime()));
                } else {
                    listDate.add(new SimpleDateFormat(DatePattern.NORM_DATE_PATTERN).format(calBegin.getTime()));
                }

            } else {
                if (QtManagerConstant.QtGranularityEnum.HOUR.getType().equals(dateType)) {
                    listDate.add(new SimpleDateFormat(DatePattern.NORM_DATETIME_PATTERN).format(calEnd.getTime()));
                } else {
                    listDate.add(new SimpleDateFormat(DatePattern.NORM_DATE_PATTERN).format(calEnd.getTime()));
                }
            }
        }
        return listDate;
    }
}
