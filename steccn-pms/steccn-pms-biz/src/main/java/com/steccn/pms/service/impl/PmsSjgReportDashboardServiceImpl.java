package com.steccn.pms.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.google.common.collect.Sets;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.authserver.util.SteccnTableAssertUtil;
import com.steccn.common.constant.FeignConstant;
import com.steccn.platform.constant.SysOfficeConstant;
import com.steccn.platform.dto.SysOfficeParentDTO;
import com.steccn.platform.feign.RemoteOfficeFeign;
import com.steccn.platform.feign.RemoteStationWarehouseFeignApi;
import com.steccn.platform.vo.SysOfficeVO;
import com.steccn.platform.vo.SysStationWarehouseVO;
import com.steccn.pms.service.PmsSjgReportDashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author yequan
 * @desc 深加工车间当日看板服务
 * @date 2023/9/12
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class PmsSjgReportDashboardServiceImpl implements PmsSjgReportDashboardService {

    private final RemoteOfficeFeign remoteOfficeFeign;
    private final RemoteStationWarehouseFeignApi stationWarehouseFeignApi;

    @Override
    public BigDecimal slip(Integer workShopId, LocalDate date) {
        Set<Integer> officeIdSet = Sets.newHashSet(workShopId);
        //根据车间获取产线
        SysOfficeParentDTO lineParentDto = new SysOfficeParentDTO();
        lineParentDto.setId(workShopId);
        lineParentDto.setOfficeType(SysOfficeConstant.PRODUCT_LINE);
        List<SysOfficeVO> lineList = FeignOptional.of(() -> remoteOfficeFeign.listByParentId(lineParentDto), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        if (CollUtil.isNotEmpty(lineList)) {
            Set<Integer> lineIdSet = lineList.stream().map(SysOfficeVO::getId).collect(Collectors.toSet());
            officeIdSet.addAll(lineIdSet);
            //根据产线获取工位
            SysOfficeParentDTO stationParentDto = new SysOfficeParentDTO();
            stationParentDto.setParentIds(lineIdSet);
            stationParentDto.setOfficeType(SysOfficeConstant.STATION);
            List<SysOfficeVO> stationList = FeignOptional.of(() -> remoteOfficeFeign.listByParentIdAndType(stationParentDto), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            Set<Integer> stationIdSet = stationList.stream().map(SysOfficeVO::getId).collect(Collectors.toSet());
            officeIdSet.addAll(stationIdSet);
        }
        Map<Integer, List<SysStationWarehouseVO>> lineWarehouseMap = FeignOptional.of(() -> stationWarehouseFeignApi.listByStationIds(officeIdSet), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        SteccnTableAssertUtil.notEmpty(lineWarehouseMap, "车间暂未绑定线边仓");
        //获取线边仓货架
        Set<Integer> shelfSet = Sets.newHashSet();
        lineWarehouseMap.forEach((k, v) -> {
            v.forEach(ele -> {
                shelfSet.add(ele.getShelfId());
            });
        });

        //根据线边仓去调拨单中获取今日的原片数量之和


        return null;
    }
}
