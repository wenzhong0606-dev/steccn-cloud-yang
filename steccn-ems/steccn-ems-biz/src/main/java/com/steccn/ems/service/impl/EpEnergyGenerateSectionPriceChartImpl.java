package com.steccn.ems.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Lists;
import com.steccn.authserver.util.SteccnTableAssertUtil;
import com.steccn.common.constant.MpConstant;
import com.steccn.common.util.BigDecimalUtil;
import com.steccn.common.util.CollectorsUtil;
import com.steccn.ems.constant.EpManagerConstant;
import com.steccn.ems.domain.EpEnergyMoney;
import com.steccn.ems.domain.EpInstrumentAccount;
import com.steccn.ems.domain.EpMeterRead;
import com.steccn.ems.domain.EpPartition;
import com.steccn.ems.dto.EpEnergyParamDTO;
import com.steccn.ems.dto.EpEnergyTreeDTO;
import com.steccn.ems.dto.EpSectionPriceDTO;
import com.steccn.ems.service.*;
import com.steccn.ems.vo.EpEnergyChartPieDisVO;
import com.steccn.ems.vo.EpEnergyChartPieValueVO;
import com.steccn.ems.vo.EpEnergyMoneyVO;
import com.steccn.ems.vo.EqEnergyChartPieVO;
import com.steccn.platform.util.LogicalPageUtil;
import com.steccn.pms.vo.ProducePassRateVO;
import com.steccn.qms.constant.QtManagerConstant;
import com.steccn.qms.dto.QtChartSubDetailDTO;
import io.swagger.models.auth.In;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.*;
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
        EpEnergyChartPieDisVO result = new EpEnergyChartPieDisVO();
        //先生成全量数据表
        List<EpEnergyMoneyVO> data = generateSectionData(dto);
        SteccnTableAssertUtil.notEmpty(data, "数据为空，暂无报表");
        //电耗加总
        Map<Integer, BigDecimal> consumption = new LinkedHashMap<>();
        consumption = data.stream().collect(Collectors.groupingBy(EpEnergyMoneyVO::getType, CollectorsUtil.summingBigDecimal(EpEnergyMoneyVO::getConsumptionNum)));
        result.setEnergy(generatePie(consumption));
        //电费加总
        Map<Integer, BigDecimal> money = new LinkedHashMap<>();
        money = data.stream().collect(Collectors.groupingBy(EpEnergyMoneyVO::getType, CollectorsUtil.summingBigDecimal(EpEnergyMoneyVO::getFeeNum)));
        result.setPrice(generatePie(money));
        return result;
    }

    @Override
    public Page<EpEnergyMoneyVO> getDetails(EpSectionPriceDTO dto) {
        //先生成全量数据表
        List<EpEnergyMoneyVO> results = generateSectionData(dto);
        //排序
        results = results.stream().sorted(Comparator.comparing(EpEnergyMoneyVO::getClassDate).thenComparing(EpEnergyMoneyVO::getType)).collect(Collectors.toList());
        Page<EpEnergyMoneyVO> page = LogicalPageUtil.page(results, dto.getCurrent(), dto.getSize());
        return page;
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
            //调用树状方法查到所有树状节点
            EpEnergyParamDTO epEnergyParam = new EpEnergyParamDTO();
            List<Long> partitionIds = new ArrayList<>();
            partitionIds.add(dto.getPartitionId());
            epEnergyParam.setPartitionId(partitionIds);
            epEnergyParam.setType(EpManagerConstant.EnergyPartitioningTree.POWER_CONSUMPTION_PARTITION.getEnergyType());
            EpEnergyTreeDTO childTree = epPartitionService.getChildTree(epEnergyParam);
            if(CollUtil.isNotEmpty(childTree.getPartitionIds())){
                partitionIds.addAll(childTree.getPartitionIds());
            } else {
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
        //算每条数据的电耗值
        if(CollUtil.isNotEmpty(epEnergyMonies)){
            for(EpEnergyMoney tmp:epEnergyMonies){
                EpEnergyMoneyVO vo = new EpEnergyMoneyVO();
                BeanUtils.copyProperties(tmp,vo);
                vo.setPartitionName(partitionName);
                vo.setTypeName(EpManagerConstant.InstrumentType.getInstrumentType(tmp.getType()).getName());
                //电耗
                vo.setConsumptionNum(BigDecimalUtil.subtract(tmp.getEndValue(),tmp.getStartValue()));
                vo.setConsumption(new DecimalFormat("0.00").format(vo.getConsumptionNum()) + "kWh");
                //单价
                vo.setPriceStr(new DecimalFormat("0.00").format(vo.getPrice()) + "元/kWh");
                //电费
                vo.setFeeNum(BigDecimalUtil.multiply(vo.getPrice(),vo.getConsumptionNum()));
                vo.setFee(new DecimalFormat("0.00").format(vo.getFeeNum()) + "元");
                results.add(vo);
            }
        }else {
            return results;
        }
        //因为使用的同一种加个方案所以查到的数据必然拥有相同的开始时间和结束时间,所以合并节点的值
        results = results.stream()
                //startTime如果有重复的,那么从EpEnergyMoneyVO对象中筛选出一个,这里选择o1,
                //并把id重复,需要将nums和sums与o1进行合并的o2,赋值给o1,最后返回o1
                .collect(Collectors.toMap(EpEnergyMoneyVO::getStart,a -> a,(o1,o2)->{
                    //电耗加总
                    o1.setConsumptionNum(BigDecimalUtil.add(o1.getConsumptionNum(),o2.getConsumptionNum()));
                    o1.setConsumption(new DecimalFormat("0.00").format(o1.getConsumptionNum()) + "kWh");
                    //电费加总
                    o1.setFeeNum(BigDecimalUtil.add(o1.getFeeNum(),o2.getFeeNum()));
                    o1.setConsumption(new DecimalFormat("0.00").format(o1.getConsumptionNum()) + "kWh");
                    return o1;
                })).values().stream().collect(Collectors.toList());
        return results;
    }

    /**
     * 峰平谷专用生成饼状图
     * @param dataMap
     * @return
     */
    private EqEnergyChartPieVO generatePie(Map<Integer, BigDecimal> dataMap){
        EqEnergyChartPieVO<BigDecimal> resultPieVO = new EqEnergyChartPieVO<>();
        List<EpEnergyChartPieValueVO<BigDecimal>> pies = Lists.newArrayList();
        /**
         * 遍历节点
         */
        BigDecimal sum = dataMap.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        for(Map.Entry<Integer,BigDecimal> entry:dataMap.entrySet()){
            EpEnergyChartPieValueVO<BigDecimal> pie = new EpEnergyChartPieValueVO<>();
            pie.setName(EpManagerConstant.ElectricType.getNameByCode(entry.getKey()));
            pie.setValue(BigDecimalUtil.divide(entry.getValue(),sum));
            pies.add(pie);
        }
        resultPieVO.setData(pies);
        return resultPieVO;
    }
}
