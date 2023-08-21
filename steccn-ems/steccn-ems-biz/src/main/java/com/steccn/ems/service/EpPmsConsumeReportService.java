package com.steccn.ems.service;

import com.steccn.common.constant.ResultCode;
import com.steccn.ems.vo.EpPmsConsumeVO;

import java.time.LocalDate;
import java.util.List;

/**
 * @author yequan
 * @desc 生产看板能源服务
 * @date 2023/8/18
 */
public interface EpPmsConsumeReportService {

    /**
     * 获取今日用电量
     *
     * @param officeId 单位id
     * @param date     日期
     * @return
     */
    EpPmsConsumeVO consumeUseElectric(Integer officeId, LocalDate date);

    /**
     * 获取今日用水量
     *
     * @param officeId 单位id
     * @param date     日期
     * @return
     */
    EpPmsConsumeVO consumeUseWater(Integer officeId, LocalDate date);

    /**
     * 获取今日天然气使用量
     *
     * @param officeId 单位id
     * @param date     日期
     * @return
     */
    EpPmsConsumeVO consumeUseGas(Integer officeId, LocalDate date);

    /**
     * 获取今日重油使用量
     *
     * @param officeId 单位id
     * @param date     日期
     * @return
     */
    EpPmsConsumeVO consumeUseOil(Integer officeId, LocalDate date);
}
