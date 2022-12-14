package com.steccn.ems.service;

import com.steccn.ems.vo.EpOverViewVO;

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
}
