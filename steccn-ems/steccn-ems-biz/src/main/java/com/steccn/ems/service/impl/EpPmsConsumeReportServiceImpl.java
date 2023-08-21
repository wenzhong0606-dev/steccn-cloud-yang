package com.steccn.ems.service.impl;

import cn.hutool.core.lang.Assert;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.common.collect.Sets;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.common.constant.FeignConstant;
import com.steccn.common.util.BigDecimalUtil;
import com.steccn.ems.domain.EpEnergyType;
import com.steccn.ems.domain.EpPartition;
import com.steccn.ems.service.EpEnergyTypeService;
import com.steccn.ems.service.EpMeterReadService;
import com.steccn.ems.service.EpPartitionService;
import com.steccn.ems.service.EpPmsConsumeReportService;
import com.steccn.ems.vo.EpMeterReadAllDataVO;
import com.steccn.ems.vo.EpPmsConsumeVO;
import com.steccn.platform.feign.RemoteDictFeignApi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
        return consumeUse(officeId, date, 4);
    }

    private EpPmsConsumeVO consumeUse(Integer officeId, LocalDate date, Integer type) {
        //获取单位
        EpEnergyType epEnergyType = epEnergyTypeService.getById(type);
        Assert.notNull(epEnergyType, "能源信息为空");
        Map<Long, String> measureUnitMap = FeignOptional.of(() -> remoteDictFeignApi.listByIdCh(Sets.newHashSet(epEnergyType.getMeasureUnit())), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        EpPartition nodeInfo = epPartitionService.getOne(new LambdaQueryWrapper<EpPartition>().eq(EpPartition::getOfficeId, officeId).eq(EpPartition::getType, epEnergyType.getId()), Boolean.FALSE);
        Set<LocalDate> dateSet = Sets.newHashSet(date.minusDays(1), date);
        List<EpMeterReadAllDataVO> classDateAllData = epMeterReadService.classDateAllData(dateSet, epEnergyType.getId(), Arrays.asList(nodeInfo.getId()));
        List<BigDecimal> collect = classDateAllData.stream().map(EpMeterReadAllDataVO::getConsume).collect(Collectors.toList());
        return new EpPmsConsumeVO(BigDecimalUtil.sum(collect), measureUnitMap.get(epEnergyType.getMeasureUnit()));
    }
}
