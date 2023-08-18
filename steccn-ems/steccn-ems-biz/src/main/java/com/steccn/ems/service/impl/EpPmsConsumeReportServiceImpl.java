package com.steccn.ems.service.impl;

import cn.hutool.core.lang.Assert;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.common.collect.Sets;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.common.constant.FeignConstant;
import com.steccn.common.util.BigDecimalUtil;
import com.steccn.ems.domain.EpEnergyType;
import com.steccn.ems.domain.EpInstrumentAccount;
import com.steccn.ems.domain.EpPartition;
import com.steccn.ems.service.*;
import com.steccn.ems.vo.EpMeterReadAllDataVO;
import com.steccn.ems.vo.EpPmsConsumeVO;
import com.steccn.platform.feign.RemoteDictFeignApi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.Lists;
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
    private final EpInstrumentAccountService accountService;
    private final EpMeterReadService epMeterReadService;

    @Override
    public List<EpPmsConsumeVO> consumeUse(Integer officeId, LocalDate date) {
        //获取单位
        List<EpEnergyType> energyTypeList = epEnergyTypeService.listByIds(Arrays.asList(1, 2, 3, 4));
        Assert.notEmpty(energyTypeList, "能源信息为空");
        //根据能源排序
        Collections.sort(energyTypeList, Comparator.comparing(EpEnergyType::getId));
        //根据能源的单位id获取单位的中文
        Set<Long> idList = energyTypeList.stream().map(ele -> ele.getMeasureUnit()).collect(Collectors.toSet());
        Map<Long, String> measureUnitMap = FeignOptional.of(() -> remoteDictFeignApi.listByIdCh(idList), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        //定义返回
        List<EpPmsConsumeVO> resList = Lists.newArrayList();

        for (EpEnergyType ele : energyTypeList) {
            EpPartition nodeInfo = epPartitionService.getOne(new LambdaQueryWrapper<EpPartition>().eq(EpPartition::getOfficeId, officeId).eq(EpPartition::getType, ele.getId()), Boolean.FALSE);
            Set<LocalDate> dateSet = Sets.newHashSet(date.minusDays(1), date);
            List<EpMeterReadAllDataVO> classDateAllData = epMeterReadService.classDateAllData(dateSet, ele.getId(), Arrays.asList(nodeInfo.getId()));
            List<BigDecimal> collect = classDateAllData.stream().map(EpMeterReadAllDataVO::getConsume).collect(Collectors.toList());
            resList.add(new EpPmsConsumeVO(BigDecimalUtil.sum(collect), measureUnitMap.get(ele.getMeasureUnit())));
        }
        return resList;
    }
}
