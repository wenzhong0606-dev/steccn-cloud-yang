package com.steccn.dp.service;

import com.steccn.dp.domain.PullProduParDetail2;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.ems.vo.EpDeepConsumeContrastVO;

import java.time.LocalDate;

/**
* @author Admin
* @description 针对表【pull_produ_par_detail2】的数据库操作Service
* @createDate 2024-06-26 15:57:07
*/
public interface PullProduParDetail2Service extends IService<PullProduParDetail2> {

    /**
     * 获取板宽趋势
     * @param date 日期
     * @param productLine 产线，对应 1-8
     * @return
     */
    EpDeepConsumeContrastVO getThickTrend(LocalDate date, Integer productLine);
}
