package com.steccn.pms.service.impl;

import cn.hutool.core.lang.Assert;
import com.google.common.collect.Sets;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.common.constant.FeignConstant;
import com.steccn.common.constant.SysDictConstant;
import com.steccn.common.util.BigDecimalUtil;
import com.steccn.platform.feign.RemoteDictFeignApi;
import com.steccn.platform.feign.RemoteOfficeFeign;
import com.steccn.platform.feign.RemoteSpareFeignApi;
import com.steccn.platform.vo.SysDictVO;
import com.steccn.platform.vo.SysOfficeVO;
import com.steccn.platform.vo.SysSpareUnitConversionVO;
import com.steccn.pms.constant.PmManagerConstant;
import com.steccn.pms.service.PmsReportDashboardService;
import com.steccn.pms.service.PmsReportWorkService;
import com.steccn.pms.service.PmsSjgReportDashboardService;
import com.steccn.pms.util.UnitConvertUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author yequan
 * @desc 深加工车间当日看板服务
 * @date 2023/9/12
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class PmsSjgReportDashboardServiceImpl implements PmsSjgReportDashboardService {

    private final PmsReportWorkService pmsReportWorkService;
    private final RemoteOfficeFeign remoteOfficeFeign;
    private final RemoteDictFeignApi remoteDictFeignApi;
    private final RemoteSpareFeignApi remoteSpareFeignApi;
    private final PmsReportDashboardService pmsReportDashboardService;

    @Override
    public List<BigDecimal> produceNumToday(Integer workShopId, LocalDate date) {
        return pmsReportDashboardService.produceNumToday(workShopId, date);
    }

    @Override
    public BigDecimal produceNumRate(Integer workShopId, LocalDate date) {
        return pmsReportDashboardService.produceNumRate(workShopId, date);
    }

    @Override
    public List<BigDecimal> inStorageToday(Integer workShopId, LocalDate date) {
        return pmsReportDashboardService.inStorageToday(workShopId, date);
    }

    @Override
    public BigDecimal inStorageRate(Integer workShopId, LocalDate date) {
        return pmsReportDashboardService.inStorageRate(workShopId, date);
    }
}
