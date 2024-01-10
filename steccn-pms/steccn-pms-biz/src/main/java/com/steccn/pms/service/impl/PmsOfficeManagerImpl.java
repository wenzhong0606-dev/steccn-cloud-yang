package com.steccn.pms.service.impl;

import com.google.common.collect.Sets;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.common.constant.FeignConstant;
import com.steccn.platform.constant.SysOfficeConstant;
import com.steccn.platform.dto.SysOfficeParentDTO;
import com.steccn.platform.feign.RemoteOfficeFeign;
import com.steccn.platform.vo.SysOfficeVO;
import com.steccn.pms.service.PmsOfficeManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author yequan
 * @description office公共接口实现，用于基础服务
 * @createDate 2024-01-08 15:46:08
 */
@Service
@RequiredArgsConstructor
public class PmsOfficeManagerImpl implements PmsOfficeManager {

    private final RemoteOfficeFeign remoteOfficeFeign;

    @Override
    public List<SysOfficeVO> getWsLineList(Integer workShopId) {
        //根据车间获取产线集合
        SysOfficeParentDTO sysOfficeParent = new SysOfficeParentDTO();
        sysOfficeParent.setId(workShopId);
        sysOfficeParent.setOfficeType(SysOfficeConstant.PRODUCT_LINE);
        List<SysOfficeVO> lineOfficeList = FeignOptional.of(() -> remoteOfficeFeign.listByParentId(sysOfficeParent), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        return lineOfficeList;
    }

    @Override
    public List<SysOfficeVO> getChildren(Integer parentOfficeId, String childOfficeType) {
        SysOfficeParentDTO sysOfficeParent = new SysOfficeParentDTO();
        sysOfficeParent.setId(parentOfficeId);
        sysOfficeParent.setOfficeType(childOfficeType);
        return FeignOptional.of(() -> remoteOfficeFeign.listByParentId(sysOfficeParent), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
    }

    @Override
    public List<SysOfficeVO> getChildren(List<Integer> parentOfficeIdList, String childOfficeType) {
        SysOfficeParentDTO sysOfficeParent = new SysOfficeParentDTO();
        sysOfficeParent.setParentIds(Sets.newHashSet(parentOfficeIdList));
        sysOfficeParent.setOfficeType(childOfficeType);
        return FeignOptional.of(() -> remoteOfficeFeign.listByParentIdAndType(sysOfficeParent), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
    }

}
