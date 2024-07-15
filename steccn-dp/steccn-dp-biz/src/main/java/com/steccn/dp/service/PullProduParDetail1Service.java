package com.steccn.dp.service;

import com.steccn.dp.domain.PullProduParDetail1;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.ems.vo.EpDeepConsumeContrastVO;

import java.time.LocalDate;

/**
* @author Admin
* @description 针对表【pull_produ_par_detail1】的数据库操作Service
* @createDate 2024-06-26 15:57:02
*/
public interface PullProduParDetail1Service extends IService<PullProduParDetail1> {

    /**
     * 获取速度趋势
     * @param date 日期
     * @param productLine 产线，对应 1-8
     * @return
     */
    EpDeepConsumeContrastVO getSpeedTrend(LocalDate date,Integer productLine);
}
