package com.steccn.pms.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.common.collect.Sets;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.common.constant.FeignConstant;
import com.steccn.common.constant.SysDictConstant;
import com.steccn.common.util.BigDecimalUtil;
import com.steccn.platform.dto.SysOfficeParentDTO;
import com.steccn.platform.feign.RemoteDictFeignApi;
import com.steccn.platform.feign.RemoteOfficeFeign;
import com.steccn.platform.feign.RemoteSpareUnitConvertFeignApi;
import com.steccn.platform.vo.SysDictVO;
import com.steccn.platform.vo.SysOfficeVO;
import com.steccn.platform.vo.SysSpareUnitConversionVO;
import com.steccn.pms.constant.PmManagerConstant;
import com.steccn.pms.domain.PmsPullForce;
import com.steccn.pms.service.PmsReportDashboardService;
import com.steccn.pms.service.PmsReportWorkService;
import com.steccn.pms.util.UnitConvertUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author yequan
 * @desc 生产看板服务实现
 * @date 2023/8/28
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PmsReportDashboardServiceImpl implements PmsReportDashboardService {

    private final PmsPullForceServiceImpl pmsPullForceService;
    private final RemoteOfficeFeign remoteOfficeFeign;
    private final PmsReportWorkService pmsReportWorkService;
    private final RemoteSpareUnitConvertFeignApi spareUnitConvertFeignApi;
    private final RemoteDictFeignApi remoteDictFeignApi;

    @Override
    public BigDecimal pullForceToday(Integer workShopId, LocalDate date) {
        BigDecimal resVal = null;
        List<PmsPullForce> dbList = pmsPullForceService.list(new LambdaQueryWrapper<PmsPullForce>()
                .eq(PmsPullForce::getPullForceDate, date)
                .eq(PmsPullForce::getWorkshopId, workShopId)
                .select(PmsPullForce::getQuantity));
        if (CollUtil.isNotEmpty(dbList)) {
            List<BigDecimal> collect = dbList.stream().map(PmsPullForce::getQuantity).collect(Collectors.toList());
            resVal = BigDecimalUtil.sum(collect);
        }
        return resVal;
    }

    @Override
    public List<BigDecimal> produceNumToday(Integer workShopId, LocalDate date) {

        Map<Integer, SysOfficeVO> sysOfficeMap = FeignOptional.of(() -> remoteOfficeFeign.listById(Sets.newHashSet(workShopId)), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        Assert.notEmpty(sysOfficeMap, "车间不能为空");
        SysOfficeVO wsOffice = sysOfficeMap.get(workShopId);
        int type = wsOffice.getOfficeName().indexOf("基片") != -1 ? 0 : 1;
        //根据车间区分深加工还是基片
        //①基片产量：所有报工加工数之和
        //②深加工产量:磨边工位报工加工数之和

        //根据车间获取产线集合
        SysOfficeParentDTO sysOfficeParent = new SysOfficeParentDTO();
        sysOfficeParent.setId(wsOffice.getId());
        List<SysOfficeVO> lineOfficeList = FeignOptional.of(() -> remoteOfficeFeign.listByParentId(sysOfficeParent), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        Assert.notEmpty(sysOfficeMap, "车间下暂无产线");
        List<Integer> lineIdList = lineOfficeList.stream().map(SysOfficeVO::getId).collect(Collectors.toList());
        Map<Long, Map<Integer, BigDecimal>> todayPm = pmsReportWorkService.getTodayPm(lineIdList, date, type);
        //获取物料转换单位
        Map<Long, List<SysSpareUnitConversionVO>> convertMap = FeignOptional.of(() -> spareUnitConvertFeignApi.convert(todayPm.keySet()), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        //获取/T/片/㎡
        SysDictVO sysDict1 = FeignOptional.of(() -> remoteDictFeignApi.getByCode(PmManagerConstant.MEASURE_UNIT_PIECE, SysDictConstant.TYPE_MEASURE_UNIT), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        SysDictVO sysDict2 = FeignOptional.of(() -> remoteDictFeignApi.getByCode(PmManagerConstant.MEASURE_UNIT_TON, SysDictConstant.TYPE_MEASURE_UNIT), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        SysDictVO sysDict3 = FeignOptional.of(() -> remoteDictFeignApi.getByCode(PmManagerConstant.MEASURE_UNIT_SM, SysDictConstant.TYPE_MEASURE_UNIT), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        Assert.notNull(sysDict1, "换算单位1不存在");
        Assert.notNull(sysDict2, "换算单位2不存在");
        Assert.notNull(sysDict3, "换算单位3不存在");
        BigDecimal sum1 = null;
        BigDecimal sum2 = null;
        BigDecimal sum3 = null;
        //先换算后求和
        for (Map.Entry<Long, Map<Integer, BigDecimal>> entry : todayPm.entrySet()) {
            BigDecimal value = BigDecimalUtil.sum(entry.getValue().values());
            BigDecimal convert1 = UnitConvertUtil.convert(convertMap, entry.getKey(), sysDict1.getId(), sysDict2.getId(), value);
            BigDecimal convert2 = UnitConvertUtil.convert(convertMap, entry.getKey(), sysDict1.getId(), sysDict3.getId(), value);
            sum1 = BigDecimalUtil.add(sum1, value);
            sum2 = BigDecimalUtil.add(sum2, convert1);
            sum3 = BigDecimalUtil.add(sum3, convert2);
        }
        return Arrays.asList(sum1, sum2, sum3);
    }
}
