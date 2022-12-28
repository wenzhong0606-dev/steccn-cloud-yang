package com.steccn.ems.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.LocalDateTimeUtil;
import com.alibaba.excel.util.MapUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Lists;
import com.steccn.authserver.util.SteccnTableAssertUtil;
import com.steccn.common.constant.MpConstant;
import com.steccn.common.util.BigDecimalUtil;
import com.steccn.common.util.CollectorsUtil;
import com.steccn.common.util.LocalDateUtil;
import com.steccn.ems.constant.EpManagerConstant;
import com.steccn.ems.domain.EpEnergyMoney;
import com.steccn.ems.domain.EpInstrumentAccount;
import com.steccn.ems.domain.EpMeterRead;
import com.steccn.ems.domain.EpPartition;
import com.steccn.ems.dto.*;
import com.steccn.ems.service.*;
import com.steccn.ems.util.DownloadUtil;
import com.steccn.ems.vo.*;
import com.steccn.platform.util.LogicalPageUtil;
import com.steccn.pms.vo.ProducePassRateVO;
import com.steccn.qms.constant.QtManagerConstant;
import com.steccn.qms.dto.QtChartSubDetailDTO;
import io.swagger.models.auth.In;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.Period;
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

    /**
     * 生成峰谷平-领导驾驶舱
     * @param dto
     * @return
     */
    @Override
    public EpEnergyChartPieDisVO generateDistributionLeader(EpSectionPriceLeaderDTO dto) {
        EpSectionPriceDTO tmpDto = new EpSectionPriceDTO();
        tmpDto.setDateStart(dto.getStartDate().minusDays(6));
        tmpDto.setDateEnd(dto.getStartDate());
        //查到电耗根节点
        EpPartition epPartition = epPartitionService.getOne(new LambdaQueryWrapper<EpPartition>()
                .eq(EpPartition::getType, EpManagerConstant.EnergyPartitioningTree.POWER_CONSUMPTION_PARTITION.getEnergyType())
                .isNull(EpPartition::getParentId),false);
        if(epPartition == null){
            tmpDto.setPartitionId(Long.parseLong(MpConstant.YES.toString()));
        }else {
            tmpDto.setPartitionId(epPartition.getId());
        }
        return generateDistribution(tmpDto);
    }

    @Override
    public Page<EpEnergyMoneyVO> getDistributionDetails(EpSectionPriceDTO dto) {
        //先生成全量数据表
        List<EpEnergyMoneyVO> results = generateSectionData(dto);
        //排序
        results = results.stream().sorted(Comparator.comparing(EpEnergyMoneyVO::getClassDate).thenComparing(EpEnergyMoneyVO::getType)).collect(Collectors.toList());
        //根据filters过滤
        if(CollUtil.isNotEmpty(dto.getFilters())){
            results = results.stream().filter(t -> dto.getFilters().contains(t.getTypeName())).collect(Collectors.toList());
        }
        Page<EpEnergyMoneyVO> page = LogicalPageUtil.page(results, dto.getCurrent(), dto.getSize());
        return page;
    }

    @SneakyThrows
    @Override
    public void downloadDistribution(EpSectionPriceDTO dto, HttpServletResponse response) {
        dto.setCurrent(1);
        dto.setSize(99999);
        Page<EpEnergyMoneyVO> page = getDistributionDetails(dto);
        List<EpEnergyMoneyVO> data = page.getRecords();
        Map<String, Object> map = MapUtils.newHashMap();
        map.put("dateStart",LocalDateUtil.localDateFormat(dto.getDateStart(),DatePattern.NORM_DATE_PATTERN));
        map.put("dateEnd",LocalDateUtil.localDateFormat(dto.getDateEnd(),DatePattern.NORM_DATE_PATTERN));
        String excelName = "峰谷平分布(电)_"
                + LocalDateUtil.localDateFormat(dto.getDateStart(),DatePattern.NORM_DATE_PATTERN) + "_"
                + LocalDateUtil.localDateFormat(dto.getDateEnd(),DatePattern.NORM_DATE_PATTERN) + ".xlsx";
        DownloadUtil.downloadTemplate(response,EpManagerConstant.EP_DOWNLOAD_MODEL_NAME_FGPDB,excelName,map,data,dto.getBase64());
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
        if(Objects.isNull(epPartition)){
            return results;
        }
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
                partitionIdList.addAll(childTree.getPartitionIds());
            } else {
                //没有下级节点则为空
                return results;
            }
        }

        if(CollUtil.isEmpty(partitionIdList)){
            return results;
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
                vo.setTypeName(EpManagerConstant.ElectricType.getNameByCode(tmp.getType()));
                //电耗
                vo.setConsumptionNum(BigDecimalUtil.subtract(tmp.getEndValue(),tmp.getStartValue()));
                vo.setConsumption(new DecimalFormat("0.00").format(vo.getConsumptionNum()) + "kWh");
                //单价
                vo.setPriceStr(new DecimalFormat("0.00").format(vo.getPrice()) + "元/kWh");
                //电费
                vo.setFeeNum(BigDecimalUtil.multiply(vo.getPrice(),vo.getConsumptionNum()));
                vo.setFee(new DecimalFormat("0.00").format(vo.getFeeNum()) + "元");
                //班次日期
                vo.setClassDateStr(LocalDateUtil.localDateFormat(vo.getClassDate(),DatePattern.NORM_DATE_PATTERN));
                //开始 结束时间str
                vo.setStartStr(LocalDateTimeUtil.format(vo.getStart(),DatePattern.NORM_DATETIME_PATTERN));
                vo.setEndStr(LocalDateTimeUtil.format(vo.getEnd(),DatePattern.NORM_DATETIME_PATTERN));
                results.add(vo);
            }
        }else {
            return results;
        }
        //因为使用的同一种价格方案所以查到的数据必然拥有相同的开始时间和结束时间,所以合并节点的值
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

    @Override
    public EpEnergyChartPieHisVO generateHistory(EpSectionPriceHisDTO dto) {
        EpEnergyChartPieHisVO VOS = new EpEnergyChartPieHisVO();
        //计算时间范围
        dto = calculateTime(dto);
        //获取全量数据表
        //获取dto1
        EpSectionPriceDTO dto1 = new EpSectionPriceDTO();
        dto1.setPartitionId(dto.getPartitionId());
        dto1.setDateStart(dto.getDateStart1());
        dto1.setDateEnd(dto.getDateEnd1());
        dto1.setCurrent(1);
        dto1.setSize(99999);
        List<EpEnergyMoneyVO> epEnergyMoneyVO1 = generateSectionData(dto1);
        //生成dto1
        if(CollUtil.isEmpty(epEnergyMoneyVO1)){
            EqEnergyChartPieVO<BigDecimal> resultPieVO = new EqEnergyChartPieVO<>();
            List<EpEnergyChartPieValueVO<BigDecimal>> pies = Lists.newArrayList();
            resultPieVO.setData(pies);
            VOS.setEnergy1(resultPieVO);
            VOS.setPrice1(resultPieVO);
        }else {
            //电耗加总
            Map<Integer, BigDecimal> consumption = new LinkedHashMap<>();
            consumption = epEnergyMoneyVO1.stream().collect(Collectors.groupingBy(EpEnergyMoneyVO::getType, CollectorsUtil.summingBigDecimal(EpEnergyMoneyVO::getConsumptionNum)));
            VOS.setEnergy1(generatePie(consumption));
            //电费加总
            Map<Integer, BigDecimal> money = new LinkedHashMap<>();
            money = epEnergyMoneyVO1.stream().collect(Collectors.groupingBy(EpEnergyMoneyVO::getType, CollectorsUtil.summingBigDecimal(EpEnergyMoneyVO::getFeeNum)));
            VOS.setPrice1(generatePie(money));
        }
        //获取dto2
        EpSectionPriceDTO dto2 = new EpSectionPriceDTO();
        dto2.setPartitionId(dto.getPartitionId());
        dto2.setDateStart(dto.getDateStart2());
        dto2.setDateEnd(dto.getDateEnd2());
        dto2.setCurrent(1);
        dto2.setSize(99999);
        List<EpEnergyMoneyVO> epEnergyMoneyVO2 = generateSectionData(dto2);
        //生成dto2
        if(CollUtil.isEmpty(epEnergyMoneyVO2)){
            EqEnergyChartPieVO<BigDecimal> resultPieVO = new EqEnergyChartPieVO<>();
            List<EpEnergyChartPieValueVO<BigDecimal>> pies = Lists.newArrayList();
            resultPieVO.setData(pies);
            VOS.setEnergy2(resultPieVO);
            VOS.setPrice2(resultPieVO);
        }else {
            //电耗加总
            Map<Integer, BigDecimal> consumption = new LinkedHashMap<>();
            consumption = epEnergyMoneyVO2.stream().collect(Collectors.groupingBy(EpEnergyMoneyVO::getType, CollectorsUtil.summingBigDecimal(EpEnergyMoneyVO::getConsumptionNum)));
            VOS.setEnergy2(generatePie(consumption));
            //电费加总
            Map<Integer, BigDecimal> money = new LinkedHashMap<>();
            money = epEnergyMoneyVO2.stream().collect(Collectors.groupingBy(EpEnergyMoneyVO::getType, CollectorsUtil.summingBigDecimal(EpEnergyMoneyVO::getFeeNum)));
            VOS.setPrice2(generatePie(money));
        }
        return VOS;
    }

    /**
     * 生成峰谷平历史数据
     * @param dto
     * @return
     */
    @Override
    public Page<EpEnergyMoneyVO> getHistoryDetails(EpSectionPriceHisDTO dto) {
        //计算时间范围
        dto = calculateTime(dto);
        //获取全量数据表
        //获取dto1
        EpSectionPriceDTO dto1 = new EpSectionPriceDTO();
        dto1.setPartitionId(dto.getPartitionId());
        dto1.setDateStart(dto.getDateStart1());
        dto1.setDateEnd(dto.getDateEnd1());
        List<EpEnergyMoneyVO> epEnergyMoneyVO1 = generateSectionData(dto1);
        //获取dto2
        EpSectionPriceDTO dto2 = new EpSectionPriceDTO();
        dto2.setPartitionId(dto.getPartitionId());
        dto2.setDateStart(dto.getDateStart2());
        dto2.setDateEnd(dto.getDateEnd2());
        List<EpEnergyMoneyVO> epEnergyMoneyVO2 = generateSectionData(dto2);
        epEnergyMoneyVO1.addAll(epEnergyMoneyVO2);
        //根据峰 谷 平过滤
        if(CollUtil.isNotEmpty(dto.getFilters())){
            List<String> filters = dto.getFilters();
            epEnergyMoneyVO1 = epEnergyMoneyVO1.stream().filter(t -> filters.contains(t.getTypeName())).collect(Collectors.toList());
        }
        //防止重复的时间分片,需要对相同班次日期的同一个开始时间去重
        epEnergyMoneyVO1 = epEnergyMoneyVO1.stream().collect(Collectors.collectingAndThen(Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(tmp -> tmp.getClassDateStr() + ";" + tmp.getStartStr()))), ArrayList::new));
        //排序
        epEnergyMoneyVO1 = epEnergyMoneyVO1.stream().sorted(Comparator.comparing(EpEnergyMoneyVO::getClassDate).thenComparing(EpEnergyMoneyVO::getType)).collect(Collectors.toList());
        //分页
        Page<EpEnergyMoneyVO> page = LogicalPageUtil.page(epEnergyMoneyVO1, dto.getCurrent(), dto.getSize());
        return page;
    }

    @SneakyThrows
    @Override
    public void downloadHistory(EpSectionPriceHisDTO dto, HttpServletResponse response) {
        //默认全部数据
        dto.setCurrent(1);
        dto.setSize(99999);
        Page<EpEnergyMoneyVO> page = getHistoryDetails(dto);
        List<EpEnergyMoneyVO> data = page.getRecords();
        Map<String, Object> map = MapUtils.newHashMap();
        map.put("dateStart1",LocalDateUtil.localDateFormat(dto.getDateStart1(),DatePattern.NORM_DATE_PATTERN));
        map.put("dateEnd1",LocalDateUtil.localDateFormat(dto.getDateEnd1(),DatePattern.NORM_DATE_PATTERN));
        map.put("dateStart2",LocalDateUtil.localDateFormat(dto.getDateStart2(),DatePattern.NORM_DATE_PATTERN));
        map.put("dateEnd2",LocalDateUtil.localDateFormat(dto.getDateEnd2(),DatePattern.NORM_DATE_PATTERN));
        String excelName = "峰谷平历史对比(电)_"
                + LocalDateUtil.localDateFormat(dto.getDateStart1(),DatePattern.NORM_DATE_PATTERN) + "_"
                + LocalDateUtil.localDateFormat(dto.getDateEnd1(),DatePattern.NORM_DATE_PATTERN) + "-"
                + LocalDateUtil.localDateFormat(dto.getDateStart2(),DatePattern.NORM_DATE_PATTERN) + "_"
                + LocalDateUtil.localDateFormat(dto.getDateEnd2(),DatePattern.NORM_DATE_PATTERN);
        DownloadUtil.downloadTemplate(response,EpManagerConstant.EP_DOWNLOAD_MODEL_NAME_FGPLSDB,excelName,map,data,dto.getBase64());
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
//        BigDecimal sum = dataMap.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        for(Map.Entry<Integer,BigDecimal> entry:dataMap.entrySet()){
            EpEnergyChartPieValueVO<BigDecimal> pie = new EpEnergyChartPieValueVO<>();
            pie.setName(EpManagerConstant.ElectricType.getNameByCode(entry.getKey()) + "时段");
            pie.setValue(entry.getValue().setScale(2, RoundingMode.HALF_UP));
            pies.add(pie);
        }
        resultPieVO.setData(pies);
        return resultPieVO;
    }

    private EpSectionPriceHisDTO calculateTime(EpSectionPriceHisDTO dto){
        //选择的天数必须和“日期范围”选择框的天数相同：
        Period period1 = Period.between(dto.getDateStart1(),dto.getDateEnd1());
        Period period2 = Period.between(dto.getDateStart2(),dto.getDateEnd2());
        int duration = period2.getDays() - period1.getDays();
        //1）天数比后者少时，在结束日期之后补全
        if(duration < 0) {
            dto.setDateEnd2(dto.getDateEnd2().plusDays(Math.abs(duration)));
        }
        //2）天数比后者多时，从开始日期往后截取和后者相同的天数
        if(duration > 0){
            dto.setDateEnd2(dto.getDateStart2().plusDays(period1.getDays()));
        }
        return dto;
    }
}
