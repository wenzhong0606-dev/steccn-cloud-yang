package com.steccn.pms.service;

import com.steccn.platform.vo.SysOfficeVO;

import java.util.Collection;
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
     * @param workShopId 车间id
     * @return
     */
    List<SysOfficeVO> getWsLineList(Integer workShopId);

    /**
     * 获取节点的孩子节点，指定孩子类型
     *
     * @param parentOfficeId  父亲的officeId
     * @param childOfficeType 孩子的类型 传空，则默认全部类型
     * @return
     */
    List<SysOfficeVO> getChildren(Integer parentOfficeId, String childOfficeType);

    /**
     * 获取节点的孩子节点，指定孩子类型
     *
     * @param parentOfficeIdList  父亲的officeId
     * @param childOfficeType 孩子的类型 传空，则默认全部类型
     * @return
     */
    List<SysOfficeVO> getChildren(Collection<Integer> parentOfficeIdList, String childOfficeType);

}
