package com.steccn.pms.service;

import com.steccn.platform.vo.SysOfficeVO;

import java.util.List;

/**
 * @author yequan
 * @description office公共接口，用于基础服务
 * @createDate 2024-01-08 15:46:08
 */
public interface PmsOfficeManager {

    /**
     * 获取车间下面的产线
     *
     * @param workShopId
     * @return
     */
    List<SysOfficeVO> getWsLineList(Integer workShopId);
}
