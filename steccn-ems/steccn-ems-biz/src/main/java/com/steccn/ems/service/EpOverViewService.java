package com.steccn.ems.service;

import com.steccn.ems.vo.EpDeepConsumeContrastVO;
import com.steccn.ems.vo.EpOverViewVO;
import com.steccn.ems.vo.EpSubConsumeContrastVO;

import java.time.LocalDate;

/**
 * @author chenhuan
 * @date 2022/12/13 13:30
 */
public interface EpOverViewService {

    /**
     * 根据卡片类型获取相应卡片数据
     *
     * @param type
     * @param selectDate
     * @return
     */
    EpOverViewVO getOverViewByType(Integer type, LocalDate selectDate);

    /**
     * 基片单耗车间对比（近7日）
     *
     * @param selectDate
     * @return
     */
    EpSubConsumeContrastVO getSubConsumeContrast(LocalDate selectDate);

    /**
     * 深加工单耗产线对比（近7日）
     *
     * @param selectDate
     * @param type
     * @return
     */
    EpDeepConsumeContrastVO getDeepConsumeContrast(LocalDate selectDate, Integer type);
}
