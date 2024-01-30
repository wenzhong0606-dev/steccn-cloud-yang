package com.steccn.pms.service;

import com.steccn.common.dto.TupleDTO;
import com.steccn.common.vo.R;
import com.steccn.platform.vo.SysOfficeVO;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

    /**
     * 查询一个节点指定类型的孩子节点
     *
     * @param parentIdList    父节点节点集合
     * @param childOfficeType 孩子类型
     * @return
     */
    Map<Integer, List<Integer>> childType(List<Integer> parentIdList, String childOfficeType);

}
