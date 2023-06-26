package com.steccn.ems.service;

import java.time.LocalDate;

/**
 * @author yequan
 * @desc 数仓接口
 * @date 2022/12/12
 */
public interface EpDwService {

    /**
     * 抄表归档
     *
     * @param date 指定日期
     * @return 是否成功
     */
    Boolean meterRead(LocalDate date);

    /**
     * 电费归档
     * @param date 指定日期
     * @return 是否成功
     */
    Boolean electricMoney(LocalDate date);

    /**
     * 抄表读数
     * @param date 上一个班次日期
     * @return 是否成功
     */
    Boolean read(LocalDate date);

    /**
     * 余热发电
     * @param date 班次日期
     * @return
     */
    Boolean heatPower(LocalDate date);

    /**
     * 补偿任务
     * @return
     */
    Boolean compensateTask();

    /**
     * 统计任务完成情况
     * @return
     */
    Boolean countTaskFinish();
}
