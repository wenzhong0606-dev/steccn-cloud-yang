package com.steccn.dp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.dp.domain.AdsPmsPullForce;
import com.steccn.dp.dto.DpPullForceDTO;
import java.math.BigDecimal;
import java.util.Map;

/**
* @author yequan
* @description 针对表【ads_pms_pull_force(每日拉引量统计)】的数据库操作Service
* @createDate 2023-11-29 16:09:13
*/
public interface AdsPmsPullForceService extends IService<AdsPmsPullForce> {

    /**
     * 拉引量汇总
     * @param dto dto
     * @return
     */
    Map<String, BigDecimal> pullForceSum(DpPullForceDTO dto);
}
