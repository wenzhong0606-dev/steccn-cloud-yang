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
}
