package com.steccn.common.util;

import com.steccn.common.vo.QtTaskClassCalculate;

import java.time.LocalDateTime;

/**
 * 计算班次日期工具类
 *
 * @author AndrewLuo
 * @date 2022/12/05
 */
public class CalculateDateUtil {

    /**
     * 计算班次日期
     * classType 0 为早班 1为晚班
     * @param time
     * @return
     */
    public static QtTaskClassCalculate calculateClassTime(LocalDateTime time) {
        QtTaskClassCalculate result = new QtTaskClassCalculate();
        //早班开始时间
        LocalDateTime zaoStartTime = time;
        zaoStartTime = zaoStartTime.withHour(8).withMinute(0).withSecond(0);
        //早班结束时间
        LocalDateTime zaoEndTime = time;
        zaoEndTime = zaoEndTime.withHour(20).withMinute(0).withSecond(0);

        //在早班开始之前
        if (time.isBefore(zaoStartTime)) {
            result.setClassType(1);
            result.setClassDate(zaoStartTime.minusDays(1).withHour(0).withMinute(1));
            return result;
        }
        //在早班的范围内[8:00,20:00)
        if ((time.isAfter(zaoStartTime) || time.isEqual(zaoStartTime)) && time.isBefore(zaoEndTime)) {
            result.setClassType(0);
            result.setClassDate(zaoStartTime.withHour(0).withMinute(1));
            return result;
        }
        //在早班之后[20:00,8:00)
        if (time.isAfter(zaoEndTime) || time.isEqual(zaoEndTime)) {
            result.setClassType(1);
            result.setClassDate(zaoStartTime.withHour(0).withMinute(1));
            return result;
        }
        return result;
    }

    /**
     * 计算是否跨班次
     * true 表示没有跨班次
     * false 表示跨班次
     * @param startTime
     * @param endTime
     * @return
     */
    public static Boolean isSameClassType(LocalDateTime startTime,LocalDateTime endTime){
        QtTaskClassCalculate defectStart = calculateClassTime(startTime);
        //如果是20:00点则表示取不到
        if(endTime.getHour()==20 && endTime.getMinute()==0 && endTime.getSecond()==0){
            endTime = endTime.minusSeconds(1);
        }
        //如果是8:00点则表示取不到
        if(endTime.getHour()==8 && endTime.getMinute()==0 && endTime.getSecond()==0){
            endTime = endTime.minusSeconds(1);
        }
        QtTaskClassCalculate defectEnd = calculateClassTime(endTime);
        //判断是否跨班次和跨时间维度
        if(defectStart.getClassType().equals(defectEnd.getClassType()) && defectStart.getClassDate().toLocalDate().equals(defectEnd.getClassDate().toLocalDate())){
            return true;
        }else {
            return false;
        }
    }

}
