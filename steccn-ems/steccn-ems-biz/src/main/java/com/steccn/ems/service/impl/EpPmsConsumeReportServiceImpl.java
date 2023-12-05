package com.steccn.ems.service.impl;

import cn.hutool.core.lang.Assert;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.common.collect.Sets;
import com.steccn.authserver.exception.SteccnChartException;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.common.constant.FeignConstant;
import com.steccn.common.util.BigDecimalUtil;
import com.steccn.common.util.LocalDateUtil;
import com.steccn.dp.dto.DpPullForceDTO;
import com.steccn.dp.feign.RemoteDpPullForceFeignApi;
import com.steccn.ems.domain.EpEnergyType;
import com.steccn.ems.domain.EpPartition;
import com.steccn.ems.service.EpEnergyTypeService;
import com.steccn.ems.service.EpMeterReadService;
import com.steccn.ems.service.EpPartitionService;
import com.steccn.ems.service.EpPmsConsumeReportService;
import com.steccn.ems.vo.EpMeterReadAllDataVO;
import com.steccn.ems.vo.EpPmsConsumeVO;
import com.steccn.ems.vo.PmElectricConsumeVO;
import com.steccn.platform.feign.RemoteDictFeignApi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author yequan
 * @desc 生产看板能源服务
 * @date 2023/8/18
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class EpPmsConsumeReportServiceImpl implements EpPmsConsumeReportService {

    private final EpEnergyTypeService epEnergyTypeService;
    private final RemoteDictFeignApi remoteDictFeignApi;
    private final EpPartitionService epPartitionService;
    private final EpMeterReadService epMeterReadService;
    private final RemoteDpPullForceFeignApi remoteDpPullForceFeignApi;

    @Override
    public EpPmsConsumeVO consumeUseElectric(Integer officeId, LocalDate date) {
        return consumeUse(officeId, date, 1);
    }

    @Override
    public EpPmsConsumeVO consumeUseWater(Integer officeId, LocalDate date) {
        return consumeUse(officeId, date, 2);
    }

    @Override
    public EpPmsConsumeVO consumeUseGas(Integer officeId, LocalDate date) {
        return consumeUse(officeId, date, 3);
    }

    @Override
    public EpPmsConsumeVO consumeUseOil(Integer officeId, LocalDate date) {
        EpPmsConsumeVO epPmsConsumeVO = consumeUse(officeId, date, 4);
        epPmsConsumeVO.setSum(BigDecimalUtil.setScale(epPmsConsumeVO.getSum(), 1));
        return epPmsConsumeVO;
    }

    @Override
    public EpPmsConsumeVO consumeUseUnitConsume(Integer officeId, LocalDate date) {
        EpPmsConsumeVO oil = consumeUseOil(officeId, date);
        EpPmsConsumeVO gas = consumeUseGas(officeId, date);
        DpPullForceDTO dpPullForceDTO = new DpPullForceDTO();
        dpPullForceDTO.setWorkshopIdSet(Sets.newHashSet(officeId));
        dpPullForceDTO.setGranularity(0);
        dpPullForceDTO.setStart(date);
        dpPullForceDTO.setEnd(date);
        Map<String, BigDecimal> pullForceMap = FeignOptional.of(() -> remoteDpPullForceFeignApi.pullForceSum(dpPullForceDTO), FeignConstant.STECCN_DP_SERVICE_NAME).get();
        //（天然气用量*7800+重油用量*1000*9300)/(拉引量*1000)
        BigDecimal gasMu = BigDecimalUtil.multiply(gas.getSum(), new BigDecimal("7800"));
        BigDecimal oilMu = BigDecimalUtil.multiply(oil.getSum(), new BigDecimal("9300000"));
        BigDecimal addMu = BigDecimalUtil.add(gasMu, oilMu);
        BigDecimal pullForceMu = BigDecimalUtil.multiply(pullForceMap.get(LocalDateUtil.format(date, "yyyy-MM-dd")), new BigDecimal("1000"));
        EpPmsConsumeVO res = new EpPmsConsumeVO();
        res.setUnitName("kcal/kg");
        res.setSum(BigDecimalUtil.divide(addMu, pullForceMu, 1));
        return res;
    }

    @Override
    public List<PmElectricConsumeVO> electricRange(Integer officeId, LocalDate start, LocalDate end) {
        return null;
    }

    private EpPmsConsumeVO consumeUse(Integer officeId, LocalDate date, Integer type) {
        //获取单位
        EpEnergyType epEnergyType = epEnergyTypeService.getById(type);
        Assert.notNull(epEnergyType, "能源信息为空");
        Map<Long, String> measureUnitMap = FeignOptional.of(() -> remoteDictFeignApi.listByIdCh(Sets.newHashSet(epEnergyType.getMeasureUnit())), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        EpPartition nodeInfo = epPartitionService.getOne(new LambdaQueryWrapper<EpPartition>().eq(EpPartition::getOfficeId, officeId).eq(EpPartition::getType, epEnergyType.getId()), Boolean.FALSE);
        if (Objects.isNull(nodeInfo)) {
            throw new SteccnChartException("当前节点不存在于能源树");
        }
        Set<LocalDate> dateSet = Sets.newHashSet(date);
        List<EpMeterReadAllDataVO> classDateAllData = epMeterReadService.classDateAllData(dateSet, epEnergyType.getId(), Arrays.asList(nodeInfo.getId()));
        List<BigDecimal> collect = classDateAllData.stream().map(EpMeterReadAllDataVO::getConsume).collect(Collectors.toList());
        return new EpPmsConsumeVO(BigDecimalUtil.sum(collect), measureUnitMap.get(epEnergyType.getMeasureUnit()));
    }
}
