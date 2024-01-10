package com.steccn.ems.service.impl;

import cn.hutool.core.lang.Assert;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.common.collect.Sets;
import com.steccn.authserver.exception.SteccnChartException;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.common.constant.FeignConstant;
import com.steccn.common.util.BigDecimalUtil;
import com.steccn.common.util.LocalDateUtil;
import com.steccn.common.util.ShardTimeUtil;
import com.steccn.dp.dto.DpPullForceDTO;
import com.steccn.dp.feign.RemoteDpPullForceFeignApi;
import com.steccn.ems.domain.EpEnergyType;
import com.steccn.ems.domain.EpPartition;
import com.steccn.ems.service.EpEnergyTypeService;
import com.steccn.ems.service.EpMeterReadService;
import com.steccn.ems.service.EpPartitionService;
import com.steccn.ems.service.EpPmsConsumeReportService;
import com.steccn.ems.util.UnitConsumeUtil;
import com.steccn.ems.vo.EpMeterReadAllDataVO;
import com.steccn.ems.vo.EpPmsConsumeVO;
import com.steccn.ems.vo.PmElectricConsumeVO;
import com.steccn.platform.feign.RemoteDictFeignApi;
import com.steccn.qms.constant.QtManagerConstant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.jni.Local;
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
    public EpPmsConsumeVO consumeUseElectric(Integer officeId, LocalDate start, LocalDate end) {
        return consumeUse(officeId, start, end, 1);
    }

    @Override
    public EpPmsConsumeVO consumeUseWater(Integer officeId, LocalDate start, LocalDate end) {
        return consumeUse(officeId, start, end, 2);
    }

    @Override
    public EpPmsConsumeVO consumeUseGas(Integer officeId, LocalDate start, LocalDate end) {
        return consumeUse(officeId, start, end, 3);
    }

    @Override
    public EpPmsConsumeVO consumeUseOil(Integer officeId, LocalDate start, LocalDate end) {
        EpPmsConsumeVO epPmsConsumeVO = consumeUse(officeId, start, end, 4);
        epPmsConsumeVO.setSum(BigDecimalUtil.setScale(epPmsConsumeVO.getSum(), 1));
        return epPmsConsumeVO;
    }

    @Override
    public EpPmsConsumeVO consumeUseUnitConsume(Integer officeId, LocalDate start, LocalDate end) {
        EpPmsConsumeVO oil = consumeUseOil(officeId, start, end);
        EpPmsConsumeVO gas = consumeUseGas(officeId, start, end);
        DpPullForceDTO dpPullForceDTO = new DpPullForceDTO();
        dpPullForceDTO.setWorkshopIdSet(Sets.newHashSet(officeId));
        dpPullForceDTO.setGranularity(1);
        dpPullForceDTO.setStart(start);
        dpPullForceDTO.setEnd(end);
        Map<String, BigDecimal> pullForceMap = FeignOptional.of(() -> remoteDpPullForceFeignApi.pullForceSum(dpPullForceDTO), FeignConstant.STECCN_DP_SERVICE_NAME).get();
        //（天然气用量*7800+重油用量*1000*9300)/(拉引量*1000)
        BigDecimal pullForce = pullForceMap.get(LocalDateUtil.format(start, "yyyy-MM-dd"));
        BigDecimal sum = UnitConsumeUtil.unitConsume(gas.getSum(), oil.getSum(), pullForce, 1);
        EpPmsConsumeVO res = new EpPmsConsumeVO();
        res.setUnitName("kcal/kg");
        res.setSum(sum);
        return res;
    }

    private EpPmsConsumeVO consumeUse(Integer officeId, LocalDate start, LocalDate end, Integer type) {
        //获取单位
        EpEnergyType epEnergyType = epEnergyTypeService.getById(type);
        Assert.notNull(epEnergyType, "能源信息为空");
        Map<Long, String> measureUnitMap = FeignOptional.of(() -> remoteDictFeignApi.listByIdCh(Sets.newHashSet(epEnergyType.getMeasureUnit())), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        EpPartition nodeInfo = epPartitionService.getOne(new LambdaQueryWrapper<EpPartition>().eq(EpPartition::getOfficeId, officeId).eq(EpPartition::getType, epEnergyType.getId()), Boolean.FALSE);
        if (Objects.isNull(nodeInfo)) {
            throw new SteccnChartException("当前节点不存在于能源树");
        }
        List<LocalDate> cutDateList = ShardTimeUtil.cutDate(QtManagerConstant.QtGranularityEnum.DAY.getType(), start, end);
        cutDateList.remove(cutDateList.size() - 1);
        Set<LocalDate> dateSet = Sets.newHashSet(cutDateList);
        List<EpMeterReadAllDataVO> classDateAllData = epMeterReadService.classDateAllData(dateSet, epEnergyType.getId(), Arrays.asList(nodeInfo.getId()));
        List<BigDecimal> collect = classDateAllData.stream().map(EpMeterReadAllDataVO::getConsume).collect(Collectors.toList());
        return new EpPmsConsumeVO(BigDecimalUtil.sum(collect), measureUnitMap.get(epEnergyType.getMeasureUnit()));
    }
}
