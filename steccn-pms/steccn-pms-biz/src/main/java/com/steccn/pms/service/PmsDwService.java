package com.steccn.pms.service;

import java.time.LocalDate;

/**
 * @desc 数仓接口
 * @author yequan
 */
public interface PmsDwService {

    /**
     * 指定日期-进站归档统计
     * @param date 指定日期
     * @return
     */
    Boolean inStation(LocalDate date);

    /**
     * 指定日期-出站归档统计
     * @param date 指定日期
     * @return
     */
    Boolean outStation(LocalDate date);
}
