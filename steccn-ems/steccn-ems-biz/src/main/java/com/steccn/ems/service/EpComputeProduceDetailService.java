package com.steccn.ems.service;

import com.steccn.ems.vo.EpProduceDetailVO;

import java.time.LocalDate;
import java.util.List;

/**
 * @author chenhuan
 * @date 2022/12/9 14:28
 */
public interface EpComputeProduceDetailService {

    /**
     * 获取生产明细列表
     *
     * @param startDate 开始时间
     * @param endDate 结束时间
     * @return
     */
    List<EpProduceDetailVO> getProduceDetailList(LocalDate startDate, LocalDate endDate);
}
