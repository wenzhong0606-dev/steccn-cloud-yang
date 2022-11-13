package com.steccn.qms.util;

import lombok.extern.slf4j.Slf4j;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * @desc 时间分片类
 */
@Slf4j
public class ShardTimeUtil {

    /**
     * 切割时间段
     *
     * @param dateType 交易類型 Y/Q/M/W/D/H -->每年/每季度/每月/每周/每天/每小時
     * @param start    yyyy-MM-dd HH:mm:ss
     * @param end      yyyy-MM-dd HH:mm:ss
     * @return
     */
    public static List<LocalDateTime> cutDateRange(String dateType, LocalDateTime start, LocalDateTime end) {
        List<LocalDateTime> listDate = new ArrayList<>();
        while (end.isAfter(start)) {
            listDate.add(start);
            switch (dateType) {
                case "Y":
                    start = start.plusYears(1);
                    start = LocalDateTime.of(LocalDate.of(start.getYear(), 1, 1), LocalTime.MIN);
                    break;
                case "Q":
                    int currentMonth = start.getMonthValue();
                    if (currentMonth >= 1 && currentMonth <= 3) {
                        start = LocalDateTime.of(LocalDate.of(start.getYear(), 1, 1), start.toLocalTime());
                    } else if (currentMonth >= 4 && currentMonth <= 6) {
                        start = LocalDateTime.of(LocalDate.of(start.getYear(), 4, 1), start.toLocalTime());
                    } else if (currentMonth >= 7 && currentMonth <= 9) {
                        start = LocalDateTime.of(LocalDate.of(start.getYear(), 7, 1), start.toLocalTime());
                    } else if (currentMonth >= 10 && currentMonth <= 12) {
                        start = LocalDateTime.of(LocalDate.of(start.getYear(), 10, 1), start.toLocalTime());
                    }
                    start = start.plusMonths(3);
                    break;
                case "M":
                    start = LocalDateTime.of(LocalDate.of(start.getYear(), start.getMonth(), 1), start.toLocalTime());
                    start = start.plusMonths(1);
                    break;
                case "W":
                    start = start.plusWeeks(1);
                    start = start.with(DayOfWeek.MONDAY);
                    break;
                case "D":
                    start = start.plusDays(1);
                    break;
                case "H":
                    start = start.plusHours(1);
                    break;
            }
            //永远加最后一个元素
            if (!end.isAfter(start)) {
                listDate.add(end);
            }
        }
        return listDate;
    }
}
