package com.steccn.ems.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.common.collect.Lists;
import com.steccn.common.constant.MpConstant;
import com.steccn.common.util.BigDecimalUtil;
import com.steccn.ems.constant.EpManagerConstant;
import com.steccn.ems.domain.EpEnergyMoney;
import com.steccn.ems.domain.EpInstrumentAccount;
import com.steccn.ems.domain.EpMeterRead;
import com.steccn.ems.domain.EpPartition;
import com.steccn.ems.dto.EpSectionPriceDTO;
import com.steccn.ems.service.*;
import com.steccn.ems.vo.EpEnergyChartPieDisVO;
import com.steccn.ems.vo.EpEnergyMoneyVO;
import com.steccn.qms.constant.QtManagerConstant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
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

    private final EpPartitionService epPartitionService;


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
    public List<EpEnergyMoneyVO> generateSectionData(EpSectionPriceDTO dto){
        List<EpEnergyMoneyVO> results = Lists.newArrayList();
        //先拿到节点名字
        EpPartition epPartition = epPartitionService.getById(dto.getPartitionId());
        String partitionName = epPartition.getOfficeName();
        //电表类型为1
        if(!epPartition.getType().equals(MpConstant.YES)){
            return results;
        }

        //1、通过节点查到表的数据
        EpInstrumentAccount instrumentAccount = epInstrumentAccountService.getOne(new LambdaQueryWrapper<EpInstrumentAccount>()
                .eq(EpInstrumentAccount::getPartitionNode, dto.getPartitionId()),false);
        if(instrumentAccount == null){
            return results;
        }
        //如果是非远传仪表则也返回空
        if(instrumentAccount.getEntityTable().equals(EpManagerConstant.InstrumentType.NON_REMOTE.getCode())){
            return results;
        }
        //仪表id List
        List<Long> partitionIdList = Lists.newArrayList();
        //如果是远传仪表则只有一条数据
        if(instrumentAccount.getEntityTable().equals(EpManagerConstant.InstrumentType.REMOTE.getCode())){
            partitionIdList.add(instrumentAccount.getPartitionNode());
        }else {
            //否则是虚拟仪表，需要找到下级节点所有远传仪表的数据
            List<EpPartition> partitionList = epPartitionService.list(new LambdaQueryWrapper<EpPartition>().eq(EpPartition::getParentId, dto.getPartitionId()));
            if(CollUtil.isNotEmpty(partitionList)){
                partitionList.forEach(tmp->{
                    partitionIdList.add(tmp.getId());
                });
            }else {
                //没有下级节点则为空
                return results;
            }
        }

        //2、查全量数据表
        LambdaQueryWrapper<EpEnergyMoney> moneyCondition = new LambdaQueryWrapper<EpEnergyMoney>()
                .in(EpEnergyMoney::getPartitionId,partitionIdList)
                .between(EpEnergyMoney::getClassDate,dto.getDateStart(),dto.getDateEnd());
        List<EpEnergyMoney> epEnergyMonies = epEnergyMoneyService.list(moneyCondition);

        //3、算值
        //算每条数据的值
        if(CollUtil.isNotEmpty(epEnergyMonies)){
            epEnergyMonies.forEach(tmp->{
                EpEnergyMoneyVO vo = new EpEnergyMoneyVO();
                BeanUtils.copyProperties(tmp,vo);
                vo.setPartitionName(partitionName);
                vo.setTypeName(EpManagerConstant.InstrumentType.getInstrumentType(tmp.getType()).getName());
                //电耗
                vo.setConsumptionNum(BigDecimalUtil.subtract(tmp.getEndValue(),tmp.getStartValue()));
                vo.setConsumption(new DecimalFormat("0.0").format(vo.getConsumptionNum()) + "kWh");
                //单价
                vo.setPriceStr(new DecimalFormat("0.0").format(vo.getPrice()) + "元/kWh");
                //电费
                vo.setFeeNum(BigDecimalUtil.multiply(vo.getPrice(),vo.getConsumptionNum()));
                vo.setFee(new DecimalFormat("0.0").format(vo.getFeeNum()) + "元");
                results.add(vo);
            });
        }else {
            return results;
        }

        return results;
    }
}
