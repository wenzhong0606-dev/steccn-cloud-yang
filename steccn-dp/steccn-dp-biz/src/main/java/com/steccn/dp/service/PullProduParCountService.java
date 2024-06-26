package com.steccn.dp.service;

import com.steccn.dp.domain.PullProduParCount;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.dp.vo.DpTheoryPullForceVO;

import java.time.LocalDate;
import java.util.List;

/**
* @author Admin
* @description 针对表【pull_produ_par_count】的数据库操作Service
* @createDate 2024-06-24 16:15:14
*/
public interface PullProduParCountService extends IService<PullProduParCount> {
    /**
     * 获取理论拉引量的值
     * @param date
     * @return
     */
    List<DpTheoryPullForceVO> getPullForce(LocalDate date);
}
