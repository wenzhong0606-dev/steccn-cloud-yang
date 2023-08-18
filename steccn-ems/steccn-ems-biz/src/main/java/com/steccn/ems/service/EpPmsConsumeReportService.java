package com.steccn.ems.service;

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
     * 获取今日用电量，今日用水量，今日天然气使用量，今日重油使用量
     * @param officeId 单位id
     * @param date 日期
     * @return list
     */
    List<EpPmsConsumeVO> consumeUse(Integer officeId, LocalDate date);
}
