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
     * @return 条数
     */
    Integer meterRead(LocalDate date);

    /**
     * 电费归档
     * @param date 指定日期
     * @return 是否成功
     */
    Integer electricMoney(LocalDate date);

    /**
     * 抄表读数
     * @param date 上一个班次日期
     * @return 条数
     */
    Integer read(LocalDate date);

    /**
     * 余热发电
     * @param date 班次日期
     * @return
     */
    Boolean heatPower(LocalDate date);


    /**
     * 能源任务补偿代码
     * @param batchSize 补偿批量条数
     * @param maxExecCount 补偿次数最大上线
     */
    void compensate(int batchSize, int maxExecCount);

    /**
     * 统计任务完成情况
     */
    void countExec();
}
