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
}
