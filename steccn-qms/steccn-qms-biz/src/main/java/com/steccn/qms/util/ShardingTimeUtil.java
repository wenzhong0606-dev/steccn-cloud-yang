package com.steccn.qms.util;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * 时间分片工具类
 */
public class ShardingTimeUtil {
    public static void main(String[] args) {
        System.out.println(cutDate("H", "2022-4-29 00:00:00", "2022-10-17 00:00:00"));
    }

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
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date dBegin = sdf.parse(start);
            Date dEnd = sdf.parse(end);
            return findDates(dateType, dBegin, dEnd);
        } catch (Exception e) {
            System.out.println(e);
        }
        return null;
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
                if (dateType.equals("H")) {
                    listDate.add(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(calBegin.getTime()));
                } else {
                    listDate.add(new SimpleDateFormat("yyyy-MM-dd").format(calBegin.getTime()));
                }

            } else {
                if (dateType.equals("H")) {
                    listDate.add(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(calEnd.getTime()));
                } else {
                    listDate.add(new SimpleDateFormat("yyyy-MM-dd").format(calEnd.getTime()));
                }
            }
        }
        return listDate;
    }
}
