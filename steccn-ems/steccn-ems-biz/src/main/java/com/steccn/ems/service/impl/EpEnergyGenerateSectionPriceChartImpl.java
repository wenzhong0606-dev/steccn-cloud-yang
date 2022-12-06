package com.steccn.ems.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.common.collect.Lists;
import com.steccn.common.constant.MpConstant;
import com.steccn.ems.domain.EpEnergyMoney;
import com.steccn.ems.domain.EpInstrumentAccount;
import com.steccn.ems.domain.EpMeterRead;
import com.steccn.ems.dto.EpSectionPriceDTO;
import com.steccn.ems.service.EpEnergyGenerateSectionPriceChart;
import com.steccn.ems.service.EpEnergyMoneyService;
import com.steccn.ems.service.EpInstrumentAccountService;
import com.steccn.ems.service.EpMeterReadService;
import com.steccn.ems.vo.EpEnergyChartPieDisVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 生成峰谷平图表
 *
 * @author AndrewLuo
 * @date 2022/12/06
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EpEnergyGenerateSectionPriceChartImpl implements EpEnergyGenerateSectionPriceChart {
    private final EpEnergyMoneyService epEnergyMoneyService;

    private final EpInstrumentAccountService epInstrumentAccountService;

    private final EpMeterReadService epMeterReadService;

    /**
     * 生成峰谷平
     * @param dto
     * @return
     */
    @Override
    public EpEnergyChartPieDisVO generateDistribution(EpSectionPriceDTO dto) {

        return null;
    }

    /**
     * 生成峰谷平全量数据表
     * @param dto
     * @return
     */
    public List<EpEnergyMoney> generateSectionData(EpSectionPriceDTO dto){
        List<EpEnergyMoney> results = Lists.newArrayList();
        //1、通过节点查到表数据
        EpInstrumentAccount instrumentAccount = epInstrumentAccountService.getOne(new LambdaQueryWrapper<EpInstrumentAccount>().eq(EpInstrumentAccount::getPartitionNode, dto.getPartitionId()),false);
        if(instrumentAccount == null){
            return results;
        }
        //2、查全量数据表
        LambdaQueryWrapper<EpEnergyMoney> moneyCondition = new LambdaQueryWrapper<EpEnergyMoney>()
                .eq(EpEnergyMoney::getPartitionId,dto.getPartitionId())
                .between(EpEnergyMoney::getClassDate,dto.getDateStart(),dto.getDateEnd());
        List<EpEnergyMoney> epEnergyMonies = epEnergyMoneyService.list(moneyCondition);
        //3、查看是否有抄表数据
        LambdaQueryWrapper<EpMeterRead> readCondition = new LambdaQueryWrapper<EpMeterRead>()
                .eq(EpMeterRead::getAdminFlag, MpConstant.YES)
                .between(EpMeterRead::getReadDate,dto.getDateStart(),dto.getDateEnd());
        List<EpMeterRead> epMeterReads = epMeterReadService.list(readCondition);
        if(CollUtil.isNotEmpty(epMeterReads)){
            //根据班次日期分组
            Map<LocalDate,List<EpEnergyMoney>> tmpMap = epEnergyMonies.stream().collect(Collectors.groupingBy(EpEnergyMoney::getClassDate));
            for(EpMeterRead read:epMeterReads){
                //需要把调整数据算入
                //当前调整的读数
            }
        }
        return results;
    }
}
