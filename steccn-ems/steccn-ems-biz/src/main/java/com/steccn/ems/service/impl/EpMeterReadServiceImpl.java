package com.steccn.ems.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.lang.Assert;
import com.alibaba.nacos.common.utils.CollectionUtils;
import com.alibaba.nacos.common.utils.StringUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.steccn.authserver.exception.BusinessException;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.authserver.util.SecurityUtils;
import com.steccn.authserver.util.SteccnTableAssertUtil;
import com.steccn.authserver.util.SteccnUser;
import com.steccn.common.constant.FeignConstant;
import com.steccn.common.constant.MpConstant;
import com.steccn.common.dto.*;
import com.steccn.common.util.*;
import com.steccn.ems.constant.EpManagerConstant;
import com.steccn.ems.domain.*;
import com.steccn.ems.dto.*;
import com.steccn.ems.mapper.EpMeterOperationRecordMapper;
import com.steccn.ems.mapper.EpMeterReadMapper;
import com.steccn.ems.service.*;
import com.steccn.ems.util.UnitConsumeUtil;
import com.steccn.ems.vo.*;
import com.steccn.platform.constant.SysOfficeConstant;
import com.steccn.platform.dto.SysOfficeTypeKeyDTO;
import com.steccn.platform.feign.RemoteDictFeignApi;
import com.steccn.platform.feign.RemoteOfficeFeign;
import com.steccn.platform.feign.RemoteSysConfigurationFeignApi;
import com.steccn.platform.vo.SysDictVO;
import com.steccn.platform.vo.SysOfficeVO;
import com.steccn.pms.constant.PmManagerConstant;
import com.steccn.pms.dto.DwProduceOrderDTO;
import com.steccn.pms.dto.EpMeterReadRowDTO;
import com.steccn.pms.feign.RemotePmsFeignApi;
import com.steccn.pms.feign.RemoteReportWorkFeignApi;
import com.steccn.qms.constant.QtManagerConstant;
import com.steccn.qms.dto.QtEmsPullAmountDTO;
import com.steccn.qms.feign.RemoteCheckTaskDetailFeignApi;
import com.steccn.qms.util.MysqlDateKeyUtil;
import com.steccn.wms.dto.BatchChargeAgainstDTO;
import com.steccn.wms.dto.WmsDeliveryReceiptDTO;
import com.steccn.wms.dto.WmsDeliveryReceiptInfoDTO;
import com.steccn.wms.dto.WmsStockInventoryDTO;
import com.steccn.wms.feign.RemoteWmsInventoryFeignApi;
import io.netty.util.internal.shaded.org.jctools.queues.MessagePassingQueue;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.jni.Local;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author yequan
 * @description 针对表【ep_meter_read(抄表)】的数据库操作Service实现
 * @createDate 2022-11-30 15:05:32
 */
@Service
@Slf4j
public class EpMeterReadServiceImpl extends ServiceImpl<EpMeterReadMapper, EpMeterRead>
        implements EpMeterReadService {

    @Autowired
    private EpMeterOperationRecordMapper epMeterOperationRecordMapper;
    @Autowired
    private EpPartitionService epPartitionService;
    @Autowired
    private EpInstrumentAccountService epInstrumentAccountService;
    @Autowired
    private EpMeterOperationRecordService epMeterOperationRecordService;
    @Autowired
    private EpEnergyTypeService epEnergyTypeService;
    @Autowired
    private RemoteReportWorkFeignApi remoteReportWorkFeignApi;
    @Autowired
    private EpMeterReadDistantService readDistantService;
    @Autowired
    private RemoteOfficeFeign remoteOfficeFeign;
    @Autowired
    private RemoteCheckTaskDetailFeignApi remoteCheckTaskDetailFeignApi;
    @Autowired
    private RemoteWmsInventoryFeignApi remoteWmsInventoryFeignApi;
    @Autowired
    private RemoteDictFeignApi remoteDictFeignApi;
    @Autowired
    private RemotePmsFeignApi remotePmsFeignApi;
    @Autowired
    private RemoteSysConfigurationFeignApi remoteSysConfigurationFeignApi;
    @Autowired
    private RemoteReportWorkFeignApi reportWorkFeignApi;


    @Override
    public EpMeterReadVO list(String readMonth, Integer meterType) {
        //当前时间
        LocalDateTime now = LocalDateTime.now();
        //当前日期
        LocalDate currentLocalDate = now.toLocalDate();
        //当前时间
        LocalTime currentTime = now.toLocalTime();
        LocalTime localTime = LocalTime.of(9, 0, 0);
        LocalDate selectMonth = LocalDate.parse(readMonth + "-01", DatePattern.NORM_DATE_FORMATTER);
        //查询的第一天
        LocalDate firstDayOfMonth = selectMonth.with(TemporalAdjusters.firstDayOfMonth()).minusDays(1);
        //查询的最后一天
        LocalDate endDay = currentLocalDate;
        //获取需要查询的日期
        List<LocalDate> localDateList;
        //如果查询的月份是当月
        if (selectMonth.getMonth().equals(currentLocalDate.getMonth())) {
            if (currentTime.isBefore(localTime)) {
                endDay = currentLocalDate.minusDays(1);
            }
            localDateList = LocalDateUtil.getLocalDateByStartEndDate(firstDayOfMonth, endDay);
        } else {
            endDay = selectMonth.with(TemporalAdjusters.lastDayOfMonth()).plusDays(1);
            localDateList = LocalDateUtil.getLocalDateByStartEndDate(firstDayOfMonth, endDay);
        }

        //如果是天然气类型，获取每个仪表最近第一天的读数
        Map<Integer, BigDecimal> lastValueMap = new HashMap<>();
        if (meterType == 3) {
            List<MeterReadMapperVO> allMeterList = this.baseMapper.selectAllByMeterType(meterType);
            if (CollUtil.isNotEmpty(allMeterList)) {
                Set<Integer> meterIdSet = allMeterList.stream().map(MeterReadMapperVO::getMeterId).collect(Collectors.toSet());
                //获取所有仪表firstDayOfMonth之前的读数和
                List<EpMeterRead> lastValueList = this.baseMapper.selectLastValue(meterIdSet, firstDayOfMonth);
                if (CollUtil.isNotEmpty(lastValueList)) {
                    lastValueList.forEach(ele -> lastValueMap.put(ele.getMeterId(), ele.getAdminValue()));
                }
            }
        }

        //获取抄表值
        List<MeterReadMapperVO> meterReadMapperVOList = getMeterReadMapperVOList(meterType, firstDayOfMonth, endDay);
        Map<String, List<MeterReadMapperVO>> groupBy = meterReadMapperVOList
                .stream()
                .collect(Collectors.groupingBy(meterReadMapperVO -> meterReadMapperVO.getParentId()
                        + "," + meterReadMapperVO.getMeterId() + "," + meterReadMapperVO.getMeterName() + ","
                        + meterReadMapperVO.getPartitionId() + "," + meterReadMapperVO.getOfficeName() + ","
                        + meterReadMapperVO.getEntityTable()));

        List<MeterReadListVO> meterReadListVOList = new ArrayList<>();
        for (Map.Entry<String, List<MeterReadMapperVO>> entry : groupBy.entrySet()) {
            MeterReadListVO meterReadListVO = new MeterReadListVO();
            String[] keys = entry.getKey().split(",");
            if (StringUtils.isNotBlank(keys[0]) && !"null".equals(keys[0])) {
                meterReadListVO.setParentId(Long.parseLong(keys[0]));
            }
            if (StringUtils.isNotBlank(keys[1]) && !"null".equals(keys[1])) {
                meterReadListVO.setMeterId(Integer.parseInt(keys[1]));
                meterReadListVO.setIsBindingMeter(1);
            } else {
                meterReadListVO.setIsBindingMeter(0);
            }
            if (StringUtils.isNotBlank(keys[3]) && !"null".equals(keys[3])) {
                meterReadListVO.setPartitionNode(Long.parseLong(keys[3]));
            }
            if (StringUtils.isNotBlank(keys[5]) && !"null".equals(keys[5])) {
                meterReadListVO.setEntityTable(Integer.parseInt(keys[5]));
            }
            meterReadListVO.setMeterStatus(entry.getValue().get(0).getStatus());
            meterReadListVO.setMeterName("null".equals(keys[2]) ? null : keys[2]);
            meterReadListVO.setPartitionNodeName(keys[4]);

            List<ReadValueListVO> readValueListVOList = new ArrayList<>();
            //根据日期去遍历
            BigDecimal preValue = lastValueMap.get(meterReadListVO.getMeterId());
            for (LocalDate localDate : localDateList) {
                List<MeterReadMapperVO> filterList = entry.getValue().stream().filter(meterReadMapperVO -> Objects.equals(meterReadMapperVO.getReadDate(), localDate)).collect(Collectors.toList());
                ReadValueListVO readValueListVO = new ReadValueListVO();
                if (CollectionUtils.isNotEmpty(filterList)) {
                    if (meterReadListVO.getEntityTable().equals(EpManagerConstant.InstrumentType.REMOTE.getCode())) {
                        List<MeterReadMapperVO> collect = filterList.stream().filter(meterReadMapperVO -> Objects.nonNull(meterReadMapperVO.getEndTime())).sorted(Comparator.comparing(MeterReadMapperVO::getEndTime).reversed()).collect(Collectors.toList());
                        if (CollectionUtils.isNotEmpty(collect)) {
                            readValueListVO.setReadValue(collect.get(0).getAdminValue());
                        }
                    } else if (meterType == 3) {
                        readValueListVO.setReadValue(BigDecimalUtil.add(filterList.get(0).getAdminValue(), preValue));
                        preValue = readValueListVO.getReadValue();
                    } else {
                        readValueListVO.setReadValue(filterList.get(0).getAdminValue());
                    }
                }
                readValueListVO.setEntityTable(meterReadListVO.getEntityTable());
                readValueListVO.setReadDate(LocalDateTimeUtil.format(localDate, "MM-dd"));
                readValueListVO.setMeterId(meterReadListVO.getMeterId());
                readValueListVOList.add(readValueListVO);
            }
            meterReadListVO.setReadValueList(readValueListVOList);
            meterReadListVOList.add(meterReadListVO);
        }
        List<MeterReadListVO> meterReadListVOList1 = meterReadListVOList.stream().filter(meterReadMapperVO -> Objects.isNull(meterReadMapperVO.getParentId())).peek(
                meterReadListVO -> {

                    meterReadListVO.setMeterReadListVOList(recursion(meterReadListVOList, meterReadListVO.getPartitionNode()));
                    //如果是虚拟仪表
                    if (Objects.equals(meterReadListVO.getEntityTable(), 2)) {
                        for (int i = 0; i < meterReadListVO.getReadValueList().size(); i++) {
                            int finalI = i;
                            List<List<ReadValueListVO>> readValueAllList = new ArrayList<>();
                            reduceRecursion(meterReadListVO.getMeterReadListVOList(), readValueAllList);
                            //如果孩子为空，则它也为空
                            List<BigDecimal> sumReduceList = readValueAllList.stream().filter(v -> Objects.nonNull(v.get(finalI).getReadValue()) && !v.get(finalI).getEntityTable().equals(2)).map(v -> v.get(finalI).getReadValue()).collect(Collectors.toList());
                            BigDecimal sumReduce = BigDecimalUtil.sum(sumReduceList);
                            meterReadListVO.getReadValueList().get(i).setReadValue(sumReduce);
                        }
                    }
                }
        ).collect(Collectors.toList());

        EpMeterReadVO eqMeterReadVO = new EpMeterReadVO();
        localDateList.remove(0);
        if (!selectMonth.getMonth().equals(currentLocalDate.getMonth())) {
            localDateList.remove(localDateList.size() - 1);
        }

        //日期格式固定为MM-dd
        List<String> collect = localDateList.stream().map(ele -> LocalDateTimeUtil.format(ele, "MM-dd")).collect(Collectors.toList());
        eqMeterReadVO.setLabelList(collect);
        eqMeterReadVO.setMeterReadListVOList(meterReadListVOList1);

        //根据partitionId去查询sort，然后根据sort排序
        //查询所有的节点sort
        Map<Long, Integer> sortMap = Maps.newHashMap();
        List<EpPartition> sortDbList = epPartitionService.list(new LambdaQueryWrapper<EpPartition>().eq(EpPartition::getType, meterType));
        if (CollUtil.isNotEmpty(sortDbList)) {
            sortDbList.forEach(ele -> sortMap.put(ele.getId(), ele.getSort()));
        }
        setSort(eqMeterReadVO.getMeterReadListVOList(), sortMap);
        eleSort(eqMeterReadVO.getMeterReadListVOList());

        return eqMeterReadVO;
    }

    @Override
    public EpMeterReadVO kvDay(String readMonth, Integer meterType) {
        //当前时间
        LocalDateTime now = LocalDateTime.now();
        //当前日期
        LocalDate currentLocalDate = now.toLocalDate();
        //当前时间
        LocalTime currentTime = now.toLocalTime();
        LocalTime localTime = LocalTime.of(9, 0, 0);
        LocalDate selectMonth = LocalDate.parse(readMonth + "-01", DatePattern.NORM_DATE_FORMATTER);
        //查询的第一天
        LocalDate firstDayOfMonth = selectMonth.with(TemporalAdjusters.firstDayOfMonth()).minusDays(1);

        //查询的最后一天
        LocalDate endDay = currentLocalDate;
        //获取需要查询的日期
        List<LocalDate> localDateList;
        //如果查询的月份是当月
        if (selectMonth.getMonth().equals(currentLocalDate.getMonth())) {
            if (currentTime.isBefore(localTime)) {
                endDay = currentLocalDate.minusDays(1);
            }
            localDateList = LocalDateUtil.getLocalDateByStartEndDate(firstDayOfMonth, endDay);
        } else {
            endDay = selectMonth.with(TemporalAdjusters.lastDayOfMonth()).plusDays(1);
            localDateList = LocalDateUtil.getLocalDateByStartEndDate(firstDayOfMonth, endDay);
        }

        //获取抄表值
        List<MeterReadMapperVO> meterReadMapperVOList = getMeterReadMapperVOList(meterType, firstDayOfMonth, endDay);
        Map<String, List<MeterReadMapperVO>> groupBy = meterReadMapperVOList
                .stream()
                .collect(Collectors.groupingBy(meterReadMapperVO -> meterReadMapperVO.getParentId()
                        + "," + meterReadMapperVO.getMeterId() + "," + meterReadMapperVO.getMeterName() + ","
                        + meterReadMapperVO.getPartitionId() + "," + meterReadMapperVO.getOfficeName() + ","
                        + meterReadMapperVO.getEntityTable()));

        List<MeterReadListVO> meterReadListVOList = new ArrayList<>();
        for (Map.Entry<String, List<MeterReadMapperVO>> entry : groupBy.entrySet()) {
            MeterReadListVO meterReadListVO = new MeterReadListVO();
            String[] keys = entry.getKey().split(",");
            if (StringUtils.isNotBlank(keys[0]) && !"null".equals(keys[0])) {
                meterReadListVO.setParentId(Long.parseLong(keys[0]));
            }
            if (StringUtils.isNotBlank(keys[1]) && !"null".equals(keys[1])) {
                meterReadListVO.setMeterId(Integer.parseInt(keys[1]));
                meterReadListVO.setIsBindingMeter(1);
            } else {
                meterReadListVO.setIsBindingMeter(0);
            }
            if (StringUtils.isNotBlank(keys[3]) && !"null".equals(keys[3])) {
                meterReadListVO.setPartitionNode(Long.parseLong(keys[3]));
            }
            if (StringUtils.isNotBlank(keys[5]) && !"null".equals(keys[5])) {
                meterReadListVO.setEntityTable(Integer.parseInt(keys[5]));
            }
            meterReadListVO.setMeterStatus(entry.getValue().get(0).getStatus());
            meterReadListVO.setMeterName("null".equals(keys[2]) ? null : keys[2]);
            meterReadListVO.setPartitionNodeName(keys[4]);

            List<ReadValueListVO> readValueListVOList = new ArrayList<>();
            BigDecimal preReadValue = null;
            BigDecimal currentValue = null;
            BigDecimal sum = null;
            for (LocalDate localDate : localDateList) {
                List<MeterReadMapperVO> filterList = entry.getValue().stream().filter(meterReadMapperVO -> Objects.equals(meterReadMapperVO.getReadDate(), localDate)).collect(Collectors.toList());
                ReadValueListVO readValueListVO = new ReadValueListVO();
                if (CollectionUtils.isNotEmpty(filterList)) {
                    //如果是远传仪表，仅仅获取第一天
                    if (meterReadListVO.getEntityTable().equals(EpManagerConstant.InstrumentType.REMOTE.getCode())) {
                        Optional<MeterReadMapperVO> maxRemoteValueOp = filterList.stream().filter(meterReadMapperVO -> Objects.nonNull(meterReadMapperVO.getEndTime())).sorted(Comparator.comparing(MeterReadMapperVO::getEndTime).reversed()).findFirst();
                        if (maxRemoteValueOp.isPresent()) {
                            currentValue = maxRemoteValueOp.get().getAdminValue();
                        }
                    } else {
                        //普通仪表，只有一天的数据
                        currentValue = filterList.get(0).getAdminValue();
                    }
                }
                BigDecimal subtract = BigDecimalUtil.subtract(currentValue, preReadValue);
                if (Objects.nonNull(subtract) && BigDecimalUtil.ge(subtract, BigDecimal.ZERO)) {
                    readValueListVO.setReadValue(subtract);
                    sum = BigDecimalUtil.add(sum, subtract);
                }
                preReadValue = currentValue;
                //多获取一天的数据 仅仅用于做差值
                if (!localDate.isEqual(firstDayOfMonth) && !localDate.isEqual(localDateList.get(localDateList.size() - 1))) {
                    readValueListVO.setEntityTable(meterReadListVO.getEntityTable());
                    readValueListVO.setReadDate(LocalDateTimeUtil.format(localDate, "MM-dd"));
                    readValueListVO.setMeterId(meterReadListVO.getMeterId());
                    readValueListVOList.add(readValueListVO);
                }
            }
            meterReadListVO.setSum(sum);
            meterReadListVO.setReadValueList(readValueListVOList);
            meterReadListVOList.add(meterReadListVO);
        }
        List<MeterReadListVO> meterReadListVOList1 = meterReadListVOList.stream().filter(meterReadMapperVO -> Objects.isNull(meterReadMapperVO.getParentId())).peek(
                meterReadListVO -> {

                    meterReadListVO.setMeterReadListVOList(recursion(meterReadListVOList, meterReadListVO.getPartitionNode()));
                    //如果是虚拟仪表
                    if (Objects.equals(meterReadListVO.getEntityTable(), 2)) {
                        for (int i = 0; i < meterReadListVO.getReadValueList().size(); i++) {
                            int finalI = i;
                            List<List<ReadValueListVO>> readValueAllList = new ArrayList<>();
                            reduceRecursion(meterReadListVO.getMeterReadListVOList(), readValueAllList);
                            //求和，如果孩子都是null 则和为null
                            List<BigDecimal> sumReduceList = readValueAllList.stream().filter(v -> Objects.nonNull(v.get(finalI).getReadValue()) && !v.get(finalI).getEntityTable().equals(2)).map(v -> v.get(finalI).getReadValue()).collect(Collectors.toList());
                            BigDecimal sumReduce = BigDecimalUtil.sum(sumReduceList);
                            meterReadListVO.getReadValueList().get(i).setReadValue(sumReduce);
                        }
                    }
                    //求和
                    List<BigDecimal> sumList = meterReadListVO.getReadValueList().stream().filter(v -> Objects.nonNull(v.getReadValue())).map(ReadValueListVO::getReadValue).collect(Collectors.toList());
                    meterReadListVO.setSum(BigDecimalUtil.sum(sumList));
                }
        ).collect(Collectors.toList());

        EpMeterReadVO eqMeterReadVO = new EpMeterReadVO();
        localDateList.remove(0);
        localDateList.remove(localDateList.size() - 1);
        //时间格式化为MM-dd
        List<String> collect = localDateList.stream().map(ele -> LocalDateTimeUtil.format(ele, "MM-dd")).collect(Collectors.toList());
        eqMeterReadVO.setLabelList(collect);
        eqMeterReadVO.setMeterReadListVOList(meterReadListVOList1);
        //根据partitionId去查询sort，然后根据sort排序
        //查询所有的节点sort
        Map<Long, Integer> sortMap = Maps.newHashMap();
        List<EpPartition> sortDbList = epPartitionService.list(new LambdaQueryWrapper<EpPartition>().eq(EpPartition::getType, meterType));
        if (CollUtil.isNotEmpty(sortDbList)) {
            sortDbList.forEach(ele -> sortMap.put(ele.getId(), ele.getSort()));
        }
        setSort(eqMeterReadVO.getMeterReadListVOList(), sortMap);
        eleSort(eqMeterReadVO.getMeterReadListVOList());
        return eqMeterReadVO;
    }

    @Override
    public CommonReportDTO energyConsume(Integer energyType, Integer workshopId, LocalDate start, LocalDate end, Integer granularityInt) {
        //确定能源存在
        EpEnergyType epEnergyType = epEnergyTypeService.getById(energyType);
        Assert.notNull(epEnergyType, "能源类型不存在");
        //日期区间划分
        QtManagerConstant.QtGranularityEnum granularity = QtManagerConstant.QtGranularityEnum.getByCode(granularityInt);
        Assert.notNull(granularity, "粒度不存在");
        //根据组织架构id查询能源树上id
        Map<Integer, EpInstrumentAccount> accountMap = epPartitionService.listByOfficeId(energyType, Sets.newHashSet(workshopId));
        Assert.notEmpty(accountMap, "能源数据不存在此组织架构节点");
        CommonReportDTO resVo = new CommonReportDTO();
        resVo.setName(epEnergyType.getName());
        SysDictVO sysDictVO = FeignOptional.of(() -> remoteDictFeignApi.get(epEnergyType.getMeasureUnit()), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        resVo.setUnit(GetValueUtil.get(sysDictVO, (x) -> x.getName()));
        List<LocalDate> dayTimeList = ShardTimeUtil.cutDate(QtManagerConstant.QtGranularityEnum.DAY.getType(), start, end);
        List<Long> partitionIdList = accountMap.values().stream().map(EpInstrumentAccount::getPartitionNode).collect(Collectors.toList());
        List<EpMeterReadAllDataVO> consumeList = classDateAllData(Sets.newHashSet(dayTimeList), energyType, partitionIdList);
        //考虑时间分片
        List<LocalDate> timeList = ShardTimeUtil.cutDate(granularity.getType(), start, end);
        Map<String,BigDecimal> valueMap = Maps.newHashMap();
        for (int i = 1; i < timeList.size(); i++) {
            LocalDate areaStart = timeList.get(i - 1);
            LocalDate areaEnd = timeList.get(i);
            String convert = MysqlDateKeyUtil.convert(granularity, areaStart);
            List<BigDecimal> valueList = consumeList.stream().filter(ele -> LocalDateUtil.isRange(ele.getReadDate(), areaStart, areaEnd)).map(EpMeterReadAllDataVO::getConsume).collect(Collectors.toList());
            valueMap.put(convert, BigDecimalUtil.sum(valueList));
        }
        resVo.setValueMap(valueMap);
        return resVo;
    }

    @Override
    public Map<String,BigDecimal> energyConsumeOffice(Integer energyType, List<Integer> officeIdList, LocalDate start, LocalDate end, Integer granularityInt) {
        //确定能源存在
        EpEnergyType epEnergyType = epEnergyTypeService.getById(energyType);
        Assert.notNull(epEnergyType, "能源类型不存在");
        //日期区间划分
        QtManagerConstant.QtGranularityEnum granularity = QtManagerConstant.QtGranularityEnum.getByCode(granularityInt);
        Assert.notNull(granularity, "粒度不存在");
        //根据组织架构id查询能源树上id
        Map<Integer, EpInstrumentAccount> accountMap = epPartitionService.listByOfficeId(energyType, Sets.newHashSet(officeIdList));
        Assert.notEmpty(accountMap, "能源数据不存在此组织架构节点");
        Map<String,BigDecimal> resMap = Maps.newHashMap();
        List<LocalDate> dayTimeList = ShardTimeUtil.cutDate(QtManagerConstant.QtGranularityEnum.DAY.getType(), start, end);
        List<Long> partitionIdList = accountMap.values().stream().map(EpInstrumentAccount::getPartitionNode).collect(Collectors.toList());
        List<EpMeterReadAllDataVO> consumeList = classDateAllData(Sets.newHashSet(dayTimeList), energyType, partitionIdList);
        //考虑时间分片
        List<LocalDate> timeList = ShardTimeUtil.cutDate(granularity.getType(), start, end);
        for (Integer officeId : officeIdList) {
            for (int i = 1; i < timeList.size(); i++) {
                LocalDate areaStart = timeList.get(i - 1);
                LocalDate areaEnd = timeList.get(i);
                String convert = MysqlDateKeyUtil.convert(granularity, areaStart);
                List<BigDecimal> valueList = consumeList.stream()
                        .filter(ele -> LocalDateUtil.isRange(ele.getReadDate(), areaStart, areaEnd, Boolean.TRUE, Boolean.TRUE))
                        .filter(ele -> officeId.equals(ele.getSysOfficeId()))
                        .map(EpMeterReadAllDataVO::getConsume).collect(Collectors.toList());
                resMap.put(officeId + "_" + convert, BigDecimalUtil.sum(valueList));
            }
        }
        return resMap;
    }

    @Override
    public Map<String, BigDecimal> unitConsumeOffice(Integer energyType, List<Integer> officeIdList, LocalDate start, LocalDate end, Integer granularityInt) {
        //确定能源存在
        EpEnergyType epEnergyType = epEnergyTypeService.getById(energyType);
        Assert.notNull(epEnergyType, "能源类型不存在");
        //日期区间划分
        QtManagerConstant.QtGranularityEnum granularity = QtManagerConstant.QtGranularityEnum.getByCode(granularityInt);
        Assert.notNull(granularity, "粒度不存在");
        //根据组织架构id查询能源树上id
        Map<Integer, EpInstrumentAccount> accountMap = epPartitionService.listByOfficeId(energyType, Sets.newHashSet(officeIdList));
        Assert.notEmpty(accountMap, "能源数据不存在此组织架构节点");
        Map<String, BigDecimal> resMap = Maps.newHashMap();
        List<LocalDate> dayTimeList = ShardTimeUtil.cutDate(QtManagerConstant.QtGranularityEnum.DAY.getType(), start, end);
        List<Long> partitionIdList = accountMap.values().stream().map(EpInstrumentAccount::getPartitionNode).collect(Collectors.toList());
        List<EpMeterReadAllDataVO> consumeList;
        //时间区间内所有班次集合
        List<LocalDate> dayList = ShardTimeUtil.cutDate(QtManagerConstant.QtGranularityEnum.DAY.getType(), start, end);
        //获取全量数据表
        Set<LocalDate> dateSet = Sets.newHashSet(dayList);
        if (energyType == 5) {
            //燃料油+气单耗
            consumeList = kcalDataUnit(dateSet, partitionIdList);
        } else if (energyType == 1) {
            //电单耗
            consumeList = classDateAllDataUnit(dateSet, energyType, partitionIdList);
        } else {
            //其他能源的单耗
            consumeList = classDateAllDataUnitOther(dateSet, energyType, partitionIdList);
        }
        //考虑时间分片
        List<LocalDate> timeList = ShardTimeUtil.cutDate(granularity.getType(), start, end);
        for (Integer officeId : officeIdList) {
            for (int i = 1; i < timeList.size(); i++) {
                LocalDate areaStart = timeList.get(i - 1);
                LocalDate areaEnd = timeList.get(i);
                String convert = MysqlDateKeyUtil.convert(granularity, areaStart);
                List<BigDecimal> valueList1 = consumeList.stream()
                        .filter(ele -> LocalDateUtil.isRange(ele.getReadDate(), areaStart, areaEnd, Boolean.TRUE, Boolean.TRUE))
                        .filter(ele -> officeId.equals(ele.getSysOfficeId()))
                        .map(EpMeterReadAllDataVO::getConsume).collect(Collectors.toList());
                List<BigDecimal> valueList2 = consumeList.stream()
                        .filter(ele -> LocalDateUtil.isRange(ele.getReadDate(), areaStart, areaEnd, Boolean.TRUE, Boolean.TRUE))
                        .filter(ele -> officeId.equals(ele.getSysOfficeId()))
                        .map(EpMeterReadAllDataVO::getProductNum).collect(Collectors.toList());

                BigDecimal value1 = BigDecimalUtil.sum(valueList1);
                BigDecimal value2 = BigDecimalUtil.sum(valueList2);
                resMap.put(officeId + "_" + convert, BigDecimalUtil.divide(value1, value2));
            }
        }
        return resMap;
    }

    /**
     * 排序
     *
     * @param eleList
     */
    private void eleSort(List<MeterReadListVO> eleList) {
        Collections.sort(eleList, Comparator.nullsLast(Comparator.comparing(MeterReadListVO::getSort)));
        if (CollUtil.isNotEmpty(eleList)) {
            for (MeterReadListVO ele : eleList) {
                if (CollUtil.isNotEmpty(ele.getMeterReadListVOList())) {
                    eleSort(ele.getMeterReadListVOList());
                }
            }
        }
    }

    /**
     * 设置序号
     *
     * @param eleList
     * @param sortMap
     */
    private void setSort(List<MeterReadListVO> eleList, Map<Long, Integer> sortMap) {
        if (CollUtil.isNotEmpty(eleList) && CollUtil.isNotEmpty(sortMap)) {
            for (MeterReadListVO ele : eleList) {
                Long partitionNode = ele.getPartitionNode();
                ele.setSort(sortMap.getOrDefault(partitionNode, Integer.MAX_VALUE));
                if (CollUtil.isNotEmpty(ele.getMeterReadListVOList())) {
                    setSort(ele.getMeterReadListVOList(), sortMap);
                }
            }
        }
    }


    private void reduceRecursion(List<MeterReadListVO> meterReadListVOList, List<List<ReadValueListVO>> readValueAllList) {
        for (MeterReadListVO meterReadListVO : meterReadListVOList) {
            if (Objects.equals(meterReadListVO.getEntityTable(), 2)) {
                reduceRecursion(meterReadListVO.getMeterReadListVOList(), readValueAllList);
            } else if (!Objects.equals(meterReadListVO.getEntityTable(), 2)) {
                readValueAllList.add(meterReadListVO.getReadValueList());
            }
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean add(EpMeterReadAddDTO dto) {
        if (Objects.isNull(dto.getReadValue())) {
            throw new BusinessException("抄表值不能为空");
        }
        EpMeterRead epMeterRead = new EpMeterRead();
        BeanUtils.copyProperties(dto, epMeterRead);
        EpMeterOperationRecord epMeterOperationRecord = new EpMeterOperationRecord();

        if (dto.getAdminFlag().equals(1)) {
            epMeterRead.setAdminTime(LocalDateTime.now());
            epMeterOperationRecord.setOpNode(1);
        } else {
            epMeterRead.setReadTime(LocalDateTime.now());
            epMeterRead.setReadValue(dto.getReadValue());
            epMeterOperationRecord.setOpNode(0);
        }
        epMeterRead.setAdminValue(dto.getReadValue());

        save(epMeterRead);
        Boolean aBoolean = createDeliveryReceipt(dto);
        log.info("创建出库单->"+ aBoolean);
        SteccnUser user = SecurityUtils.getUser();
        epMeterOperationRecord.setReadId(epMeterRead.getId());
        epMeterOperationRecord.setOpBy(user.getUserId());
        epMeterOperationRecord.setOpName(user.getName());
        epMeterOperationRecord.setOpValueOld(dto.getReadValue());
        epMeterOperationRecord.setOpTime(LocalDateTime.now());
        epMeterOperationRecordMapper.insert(epMeterOperationRecord);

        return true;
    }

    public Boolean createDeliveryReceipt(EpMeterReadAddDTO dto){
        //仅当仪表类型为燃料油计量表时：任一班次日期D的读数成功录入后，触发自动出库
        //查询仪表获取仪表类型
        EpInstrumentAccountVO epInstrumentAccountVO = epInstrumentAccountService.getEpInstrumentAccountVO(dto.getMeterId());
        if (Objects.nonNull(epInstrumentAccountVO) && epInstrumentAccountVO.getMeterType().equals(4)) {
            //仪表类型为燃料油计量表，自动出库
            Set<String> names = Sets.newHashSet();
            //获取仓库信息
            names.add(EpManagerConstant.FUEL_POWER_WAREHOUSE);
            Map<String, SysOfficeVO> officeMap = FeignOptional.of(() -> remoteOfficeFeign.listByName(names), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            //获取出库单类型
            SysDictVO sysDictVO = FeignOptional.of(() -> remoteDictFeignApi.getByCode("0", EpManagerConstant.OUTBOUND_TYPE), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            WmsDeliveryReceiptDTO wmsDeliveryReceiptDTO = new WmsDeliveryReceiptDTO();
            wmsDeliveryReceiptDTO.setCreateBy(SecurityUtils.getUser().getUserId());
            wmsDeliveryReceiptDTO.setCreateName(SecurityUtils.getUser().getName());
            wmsDeliveryReceiptDTO.setType(sysDictVO.getId());
            Integer warehouseId = officeMap.get(EpManagerConstant.FUEL_POWER_WAREHOUSE).getId();
            wmsDeliveryReceiptDTO.setWarehouseId(warehouseId);
            wmsDeliveryReceiptDTO.setWarehouseName(EpManagerConstant.FUEL_POWER_WAREHOUSE);
            wmsDeliveryReceiptDTO.setNote("能源抄表自动出库");
            List<WmsDeliveryReceiptInfoDTO> list = Lists.newArrayList();
            //获取燃料仓库中的库存，优先消耗入库时间早的库存
            Set<Integer> warehouseIds = Sets.newHashSet();
            warehouseIds.add(warehouseId);
            Map<Integer, List<WmsStockInventoryDTO>> inventortyMap = FeignOptional.of(() -> remoteWmsInventoryFeignApi.materialByWarehouse(warehouseIds), FeignConstant.STECCN_WMS_SERVICE_NAME).get();
            List<WmsStockInventoryDTO> inventoryDTOList = inventortyMap.get(warehouseId);
            //获取库存消耗量（当前读数减去前一天读数）
            if (CollUtil.isNotEmpty(inventoryDTOList)) {
                //计算消耗量并转换单位为吨
                BigDecimal readValue = previousDayMeterReading(dto.getReadDate(), dto.getMeterId());
                BigDecimal consumption = BigDecimalUtil.subtract(dto.getReadValue(), readValue);
                BigDecimal divide = BigDecimalUtil.divide(consumption, new BigDecimal("1000"));
                inventoryDTOList = inventoryDTOList.stream()
                        .sorted(Comparator.comparing(WmsStockInventoryDTO::getCreateDate, Comparator.nullsLast(Comparator.naturalOrder())))
                        .collect(Collectors.toList());
                //过滤可用库存小于等于O的数据
                List<WmsStockInventoryDTO> collect = inventoryDTOList.stream().filter(wmsInventory -> wmsInventory.getAvailableQuantity().compareTo(BigDecimal.ZERO) > 0).collect(Collectors.toList());
                if (CollUtil.isNotEmpty(collect)) {
                    for (WmsStockInventoryDTO wmsInventory : collect) {
                        WmsDeliveryReceiptInfoDTO wmsDeliveryReceiptInfoDTO = new WmsDeliveryReceiptInfoDTO();
                        BeanUtil.copyProperties(wmsInventory, wmsDeliveryReceiptInfoDTO);
                        if (wmsInventory.getAvailableQuantity().compareTo(divide) >= 0){
                            wmsDeliveryReceiptInfoDTO.setQuantity(divide);
                            wmsDeliveryReceiptInfoDTO.setAssociatedNo(String.valueOf(dto.getMeterId()));
                            list.add(wmsDeliveryReceiptInfoDTO);
                            break;
                        }else {
                            divide = BigDecimalUtil.subtract(divide,wmsInventory.getAvailableQuantity());
                            wmsDeliveryReceiptInfoDTO.setQuantity(wmsInventory.getAvailableQuantity());
                            wmsDeliveryReceiptInfoDTO.setAssociatedNo(String.valueOf(dto.getMeterId()));
                            wmsInventory.setAvailableQuantity(BigDecimal.ZERO);
                            list.add(wmsDeliveryReceiptInfoDTO);
                        }
                    }
                } else {
                    log.info("燃料油抄表扣除库存数不足消耗");
                }
            }
            wmsDeliveryReceiptDTO.setList(list);
            //自动确认标识
            wmsDeliveryReceiptDTO.setAffirmFlag("1");
            Boolean aBoolean = FeignOptional.of(() -> remoteWmsInventoryFeignApi.createDelivery(wmsDeliveryReceiptDTO), FeignConstant.STECCN_WMS_SERVICE_NAME).get();
            log.info("燃料油抄表自动创建出库单->" + aBoolean);
        }
        return Boolean.TRUE;
    }

    /**
     * 获取前一天抄表数据
     *
     * @return
     */
    public BigDecimal previousDayMeterReading(LocalDate date, Integer meterId) {
        LocalDate recentDay = date.minusDays(1);
        //查找前一天的抄表数据
        EpMeterRead read = getOne(new LambdaQueryWrapper<EpMeterRead>().eq(EpMeterRead::getMeterId, meterId).eq(EpMeterRead::getReadDate, recentDay).orderByDesc(EpMeterRead::getReadDate).last(MpConstant.STR_LIMIT_1));
        //创建一个计数器，用来限制查找天数（7天）
        int count = 0;
        while (Objects.isNull(read) && count < 7) {
            recentDay = recentDay.minusDays(1);
            read = getOne(new LambdaQueryWrapper<EpMeterRead>().eq(EpMeterRead::getMeterId, meterId).eq(EpMeterRead::getReadDate, recentDay).orderByDesc(EpMeterRead::getReadDate).last(MpConstant.STR_LIMIT_1));
            count++;
        }
        if (Objects.isNull(read)) {
            //如果7天内都没有抄表数，则返回为0
            return BigDecimal.ZERO;
        }
        return read.getReadValue();
    }

    @Override
    public EpMeterReadDetailsVO details(String readDate, Integer meterId) {
        EpInstrumentAccount instrumentAccount = epInstrumentAccountService.getById(meterId);
        if (Objects.isNull(instrumentAccount) || instrumentAccount.getStatus().equals(0) || !instrumentAccount.getEntityTable().equals(1)) {
            throw new BusinessException("仪表不存在或停用");
        }
        LocalDate now = LocalDate.now();
        LocalTime nowTime = LocalTime.now();
        if (nowTime.isBefore(LocalTime.of(9, 0))) {
            now = now.minusDays(1);
        }
        LocalDate date = LocalDate.parse(readDate, DatePattern.NORM_DATE_FORMATTER);
        EpMeterReadDetailsVO epMeterReadDetailsVO = this.baseMapper.selectByReadDateAndMeterId(readDate, meterId);

        if (Objects.isNull(epMeterReadDetailsVO) && LocalDateUtil.le(date, now)) {
            EpEnergyType epEnergyType = epEnergyTypeService.getById(instrumentAccount.getMeterType());
            epMeterReadDetailsVO = new EpMeterReadDetailsVO();
            epMeterReadDetailsVO.setPartitionId(instrumentAccount.getPartitionNode());
            epMeterReadDetailsVO.setMeterType(instrumentAccount.getMeterType());
            epMeterReadDetailsVO.setEntityTable(instrumentAccount.getEntityTable());
            epMeterReadDetailsVO.setMeterId(instrumentAccount.getId().longValue());
            epMeterReadDetailsVO.setMeterName(instrumentAccount.getMeterName());
            epMeterReadDetailsVO.setStatus(instrumentAccount.getStatus());
            epMeterReadDetailsVO.setMeterTypeName(epEnergyType.getMeasureMeterName());
            setPartitionNode(epMeterReadDetailsVO);
        } else {
            setPartitionNode(epMeterReadDetailsVO);
        }
        return epMeterReadDetailsVO;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean updateBatch(List<EpMeterReadUpdateDTO> dtoList) {
        Optional<EpMeterReadUpdateDTO> dtoOptional = dtoList.stream().filter(epMeterReadUpdateDTO -> EpManagerConstant.InstrumentType.REMOTE.getCode().equals(epMeterReadUpdateDTO.getEntityTable())
                || EpManagerConstant.InstrumentType.VIRTUAL.getCode().equals(epMeterReadUpdateDTO.getEntityTable())).findAny();
        if (dtoOptional.isPresent()) {
            throw new BusinessException("只能修改实体非远传仪表的值");
        }
        Set<LocalDate> localDateSet = dtoList.stream().map(EpMeterReadUpdateDTO::getReadDate).collect(Collectors.toSet());
        Set<Integer> meterIdSet = dtoList.stream().map(EpMeterReadUpdateDTO::getMeterId).collect(Collectors.toSet());
        List<EpMeterRead> list = list(new LambdaQueryWrapper<EpMeterRead>().in(EpMeterRead::getMeterId, meterIdSet).in(EpMeterRead::getReadDate, localDateSet));
        SteccnUser user = SecurityUtils.getUser();

        //获取需要修改的数据
        List<EpMeterOperationRecord> epMeterOperationRecordList = new ArrayList<>();
        for (EpMeterRead epMeterRead : list) {
            Optional<EpMeterReadUpdateDTO> optional = dtoList.stream().filter(meterReadUpdateDTO -> meterReadUpdateDTO.getMeterId().equals(epMeterRead.getMeterId())
                    && meterReadUpdateDTO.getReadDate().isEqual(epMeterRead.getReadDate())).findAny();
            if (optional.isPresent()) {

                EpMeterOperationRecord epMeterOperationRecord = new EpMeterOperationRecord();
                epMeterOperationRecord.setReadId(epMeterRead.getId());
                if (optional.get().getAdminFlag().equals(0)) {
                    epMeterOperationRecord.setOpNode(0);
                } else {
                    epMeterOperationRecord.setOpNode(2);
                }
                epMeterOperationRecord.setOpBy(user.getUserId());
                epMeterOperationRecord.setOpName(user.getName());
                epMeterOperationRecord.setOpValueNew(optional.get().getReadValue());
                epMeterOperationRecord.setOpTime(LocalDateTime.now());
                epMeterOperationRecord.setOpValueOld(epMeterRead.getAdminValue());
                epMeterOperationRecordList.add(epMeterOperationRecord);

                epMeterRead.setAdminTime(LocalDateTime.now());
                epMeterRead.setAdminValue(optional.get().getReadValue());
                if (optional.get().getAdminFlag().equals(0)) {
                    epMeterRead.setReadValue(optional.get().getReadValue());
                    epMeterRead.setReadTime(LocalDateTime.now());
                }
            }
        }

        //获取新增数据
        for (EpMeterReadUpdateDTO epMeterReadUpdateDTO : dtoList) {
            List<EpMeterRead> epMeterReadList = list.stream().filter(epMeterRead -> epMeterRead.getMeterId().equals(epMeterReadUpdateDTO.getMeterId())
                    && epMeterRead.getReadDate().isEqual(epMeterReadUpdateDTO.getReadDate())).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(epMeterReadList)) {
                EpMeterRead epMeterRead = new EpMeterRead();
                epMeterRead.setMeterId(epMeterReadUpdateDTO.getMeterId());
                epMeterRead.setAdminValue(epMeterReadUpdateDTO.getReadValue());
                epMeterRead.setAdminTime(LocalDateTime.now());
                epMeterRead.setAdminFlag(epMeterReadUpdateDTO.getAdminFlag());
                epMeterRead.setReadDate(epMeterReadUpdateDTO.getReadDate());
                epMeterRead.setReadTime(LocalDateTime.now());
                save(epMeterRead);

                EpMeterOperationRecord epMeterOperationRecord = new EpMeterOperationRecord();
                epMeterOperationRecord.setReadId(epMeterRead.getId());
                epMeterOperationRecord.setOpNode(1);
                epMeterOperationRecord.setOpBy(user.getUserId());
                epMeterOperationRecord.setOpName(user.getName());
                epMeterOperationRecord.setOpValueNew(epMeterReadUpdateDTO.getReadValue());
                epMeterOperationRecord.setOpTime(LocalDateTime.now());
                epMeterOperationRecordList.add(epMeterOperationRecord);
            }
        }
        epMeterOperationRecordService.saveBatch(epMeterOperationRecordList);
        updateBatchById(list);
        if (CollUtil.isNotEmpty(dtoList)){
            Set<Integer> meterIds = dtoList.stream().map(EpMeterReadUpdateDTO::getMeterId).collect(Collectors.toSet());
            List<EpInstrumentAccount> instrumentAccounts = epInstrumentAccountService.list(new LambdaQueryWrapper<EpInstrumentAccount>().in(EpInstrumentAccount::getId,meterIds).eq(EpInstrumentAccount::getMeterType, 4));
            if (CollUtil.isNotEmpty(instrumentAccounts)){
                Map<Integer,EpMeterReadUpdateDTO> map = Maps.newHashMap();
                for (EpMeterReadUpdateDTO epMeterReadUpdateDTO : dtoList) {
                    map.put(epMeterReadUpdateDTO.getMeterId(),epMeterReadUpdateDTO);
                }
                List<BatchChargeAgainstDTO> againstDTOList = Lists.newArrayList();
                instrumentAccounts.forEach(epInstrumentAccount -> {
                    BatchChargeAgainstDTO batchChargeAgainstDTO = new BatchChargeAgainstDTO();
                    batchChargeAgainstDTO.setCode(epInstrumentAccount.getId().toString());
                    batchChargeAgainstDTO.setUserName(SecurityUtils.getUser().getName());
                    batchChargeAgainstDTO.setStartTime(LocalDateUtil.getStartTimeOfDayStr(map.get(epInstrumentAccount.getId()).getReadDate()));
                    batchChargeAgainstDTO.setEndTime(LocalDateUtil.getEndTimeOfDayStr(map.get(epInstrumentAccount.getId()).getReadDate()));
                    againstDTOList.add(batchChargeAgainstDTO);
                });
                //仓储批量冲销
                Boolean aBoolean = FeignOptional.of(() -> remoteWmsInventoryFeignApi.chargeAgainstAssociated(againstDTOList), FeignConstant.STECCN_WMS_SERVICE_NAME).get();
                log.info("仓储冲销:"+aBoolean);
                //重新生成入库单
                for (EpMeterReadUpdateDTO epMeterReadUpdateDTO : dtoList) {
                    EpMeterReadAddDTO epMeterReadAddDTO = BeanUtil.copyProperties(epMeterReadUpdateDTO, EpMeterReadAddDTO.class);
                    createDeliveryReceipt(epMeterReadAddDTO);
                }
            }
        }
        return true;

    }

    @Override
    public List<EpMeterReadAllDataVO> classDateAllData(Set<LocalDate> dateSet, Integer energyType, List<Long> partitionIdList) {
        SteccnTableAssertUtil.notEmpty(partitionIdList, "组织机构不能为空");
        //判断节点是否属于这棵树
        long count = epPartitionService.count(new LambdaQueryWrapper<EpPartition>().eq(EpPartition::getType, energyType).in(EpPartition::getId, partitionIdList));
        Assert.isTrue(count != 0, "此能源类型节点为空");
        Assert.notNull(energyType, "能源类型不能为空");
        Assert.notEmpty(dateSet, "日期筛选不可以为空");
        //获取节点关联仪表
        //优化点 - 获取节点关联的仪表，从根节点向下查询
        //根据partitionId查询所有关联的节点
        List<EpPartition> epPartitions = epPartitionService.getLinkedChildren(energyType, partitionIdList);
        Assert.notEmpty(epPartitions, "暂无符合条件的组织节点");
        //获取每个节点的office信息
        Map<Long, EpPartition> epPartitionMap = Maps.newHashMap();
        epPartitions.forEach(ele -> epPartitionMap.put(ele.getId(), ele));
        Map<Long, EpInstrumentAccountDTO> nodeLinkMeterMap = epInstrumentAccountService.queryNodeLinkMeterFilter(energyType, epPartitionMap.keySet());
        SteccnTableAssertUtil.notEmpty(nodeLinkMeterMap, "该能源下无仪表");
        List<EpEnergyConsumeDTO> rowList = consumeTable(dateSet, energyType, nodeLinkMeterMap);
        //最后将全量数据表的map转换为有用的list集合 对外输出-根据需要的节点过滤
        return row2List(energyType, Sets.newHashSet(partitionIdList), nodeLinkMeterMap, epPartitionMap, rowList, Maps.newHashMap());
    }


    @Override
    public List<EpMeterReadAllDataVO> classDateAllDataShare(Set<LocalDate> dateSet, Integer energyType, List<Long> partitionIdList) {
        List<EpMeterReadAllDataVO> resList = Lists.newArrayList();
        //分摊逻辑-只涉及基片制造部&深加工一~八线
        //获取指定的组织结构名称和主键
        Map<Integer, List<TripleDTO<Long, Integer, String>>> defaultNodeMap = epPartitionService.dayReportCountDefaultNode();
        List<TripleDTO<Long, Integer, String>> partitionList = defaultNodeMap.get(EpManagerConstant.ENERGY_ELECTRIC);
        List<Long> sharePartitionList = partitionList.stream().map(TripleDTO::getField1).collect(Collectors.toList());
        //包含厚度的数据结果集
        List<EpMeterReadAllDataVO> classDateAllData = classDateAllData(dateSet, energyType, sharePartitionList);
        //将classDateAllData根据日期和节点分组--最终返回规范
        Map<String, List<EpMeterReadAllDataVO>> map = classDateAllData.stream().collect(Collectors.groupingBy(v -> v.getReadDate() + "_" + v.getSysOfficeId()));
        map.forEach((k, v) -> {
            EpMeterReadAllDataVO ele = v.get(0);
            List<BigDecimal> valueList = v.stream().map(aa -> aa.getConsume()).collect(Collectors.toList());
            ele.setThick(null);
            ele.setClassId(null);
            ele.setClassGroupId(null);
            ele.setConsume(BigDecimalUtil.sum(valueList));
            resList.add(ele);
        });
        classDateAllData = resList;

        //获取配置map
        Map<String, List<Integer>> confMap = FeignOptional.of(() -> remoteSysConfigurationFeignApi.confListInteger(new TripleDTO<>(EpManagerConstant.CONF_EMS_DAY_COUNT_ELEC, EpManagerConstant.ELECConfContentEnum.allContentConf(), 2)), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        //基片制造部
        List<Integer> jpzzbList = confMap.getOrDefault(EpManagerConstant.ELECConfContentEnum.JPZZB_LIST.toString(), Lists.newArrayList());
        //深加工四条产线【一二三四】
        List<Integer> sjgLine14List = confMap.getOrDefault(EpManagerConstant.ELECConfContentEnum.SJG_LINE14_LIST.toString(), Lists.newArrayList());
        //深加工四条产线【五六七八】
        List<Integer> sjgLine58List = confMap.getOrDefault(EpManagerConstant.ELECConfContentEnum.SJG_LINE58_LIST.toString(), Lists.newArrayList());
        //公用电【深加工二车间（东线）->公用电,深加工二车间（西线）->公用电】
        List<Integer> gydList = confMap.getOrDefault(EpManagerConstant.ELECConfContentEnum.GYD_LIST.toString(), Lists.newArrayList());
        //风冷螺杆式空压机6 + 风冷螺杆式空压机7 + 风冷螺杆式空压机8 + 风冷螺杆式空压机9 + 风冷螺杆式空压机10 + 微热吸附式干燥器3
        List<Integer> fl610List = confMap.getOrDefault(EpManagerConstant.ELECConfContentEnum.FL610_LIST.toString(), Lists.newArrayList());
        //风冷螺杆式空压机1 + 风冷螺杆式空压机2 + 风冷螺杆式空压机3 + 风冷螺杆式空压机4 + 风冷螺杆式空压机5 + 微热吸附式干燥器1 + 微热吸附式干燥器2
        List<Integer> fl15List = confMap.getOrDefault(EpManagerConstant.ELECConfContentEnum.FL15_LIST.toString(), Lists.newArrayList());
        //大数据控制中心
        List<Integer> dsjList = confMap.getOrDefault(EpManagerConstant.ELECConfContentEnum.DSJ_LIST.toString(), Lists.newArrayList());
        //深加工8条产线
        List<Integer> sjgLineList = Lists.newArrayList();
        sjgLine14List.forEach(ele -> sjgLineList.add(ele));
        sjgLine58List.forEach(ele -> sjgLineList.add(ele));

        //获取
        LocalDate start = dateSet.stream().min((x, y) -> x.compareTo(y)).get();
        LocalDate end = dateSet.stream().max((x, y) -> x.compareTo(y)).get();
        List<LocalDate> timeList = dateSet.stream().sorted((x, y) -> x.compareTo(y)).collect(Collectors.toList());
        //基片制造部=b1+b2+b3
        List<EpMeterReadAllDataVO> jpList = classDateAllData.stream().filter(v -> jpzzbList.contains(v.getSysOfficeId())).collect(Collectors.toList());
        Map<LocalDate, List<BigDecimal>> jpResMap = Maps.newHashMap();
        for (LocalDate ele : timeList) {
            List<BigDecimal> timeDataList = jpList.stream().filter(v -> v.getReadDate().isEqual(ele)).map(EpMeterReadAllDataVO::getConsume).collect(Collectors.toList());
            BigDecimal reduce = BigDecimalUtil.sum(timeDataList);
            jpResMap.put(ele, Lists.newArrayList(reduce));
        }

        //深加工一~八产线
        List<TripleDTO<Long, Integer, String>> linePartitionList = partitionList.stream().filter(v -> sjgLine14List.contains(v.getField2()) || sjgLine58List.contains(v.getField2())).collect(Collectors.toList());
        Set<Integer> lineOfficeIdSet = Sets.newHashSet();
        Map<String, List<BigDecimal>> lineResMap = Maps.newHashMap();

        List<EpDayReportCountRowVO> lineRowList = Lists.newArrayList();
        for (TripleDTO<Long, Integer, String> line : linePartitionList) {
            //获取产线id集合
            lineOfficeIdSet.add(line.getField2());
            List<EpMeterReadAllDataVO> lineList = classDateAllData.stream().filter(v -> line.getField1().equals(v.getOfficeId())).collect(Collectors.toList());
            //构建基片制造部
            List<BigDecimal> lineValueList = Lists.newArrayList();
            for (LocalDate ele : timeList) {
                List<BigDecimal> timeDataList = lineList.stream().filter(v -> v.getReadDate().isEqual(ele)).map(EpMeterReadAllDataVO::getConsume).collect(Collectors.toList());
                BigDecimal timeValue = BigDecimalUtil.sum(timeDataList);
                //a1.1 a2.1 a3.1 a4.1 a5.1 a6.1 a7.1 a8.1
                lineResMap.put(mapKey(line.getField2(), ele), Lists.newArrayList(timeValue));
            }
        }
        //获取几条产线的产量及对应比例，用于分摊公用电
        Map<String, BigDecimal> productNumMap = FeignOptional.of(() -> reportWorkFeignApi.classDateProductNum(new TripleDTO<>(lineOfficeIdSet, start, end)), FeignConstant.STECCN_DATA_PLATFORM_SERVICE_NAME).get();
        TupleDTO<Map<String, BigDecimal>, Map<String, BigDecimal>> tupleDTO = getProductNumRateMap(sjgLine14List, sjgLine58List, timeList, productNumMap);
        Map<String, BigDecimal> productNumLineRateMap = tupleDTO.getField1();
        Map<String, BigDecimal> productNumRateMap = tupleDTO.getField2();

        //获取公用电
        gydRate(classDateAllData, gydList, Lists.newArrayList(sjgLine14List, sjgLine58List), timeList, productNumLineRateMap, lineResMap);

        //获取风冷螺杆式空压机6 + 风冷螺杆式空压机7 + 风冷螺杆式空压机8 + 风冷螺杆式空压机9 + 风冷螺杆式空压机10 + 微热吸附式干燥器3
        //a1.3 a2.3 a3.3 a4.3 a5.3 a6.3 a7.3 a8.3
        fl610(classDateAllData, fl610List, sjgLineList, timeList, productNumRateMap, lineResMap);

        //获取风冷螺杆式空压机1 + 风冷螺杆式空压机2 + 风冷螺杆式空压机3 + 风冷螺杆式空压机4 + 风冷螺杆式空压机5 + 微热吸附式干燥器1 + 微热吸附式干燥器2
        //获取b2
        fl15(classDateAllData, fl15List, timeList, jpResMap);

        //获取总能耗N 按b1、a1.1 + a1.2 + a1.3、a2.1 + a2.2 + a2.3、... 、a8.1 + a8.2 + a8.3按照比例分摊
        //N分摊为b3、a1.4、a2.4、... 、a8.4共9个值
        Map<String, BigDecimal> nRateMap = getNRateMap(sjgLineList, timeList, jpResMap, lineResMap);
        dsj(classDateAllData, dsjList, sjgLineList, timeList, nRateMap, jpResMap, lineResMap);
        classDateAllData = classDateAllData.stream().filter(ele -> jpzzbList.contains(ele.getSysOfficeId()) || sjgLine14List.contains(ele.getSysOfficeId()) || sjgLine58List.contains(ele.getSysOfficeId())).collect(Collectors.toList());
        List<EpMeterReadAllDataVO> jpzzCDAD = classDateAllData.stream().filter(ele -> jpzzbList.contains(ele.getSysOfficeId())).collect(Collectors.toList());
        List<EpMeterReadAllDataVO> lineCDAD = classDateAllData.stream().filter(ele -> sjgLine14List.contains(ele.getSysOfficeId()) || sjgLine58List.contains(ele.getSysOfficeId())).collect(Collectors.toList());
        //重新计算基片行和产线行
        ftJpRow(jpzzCDAD, jpResMap);
        ftLineRow(lineCDAD, lineResMap);
        return classDateAllData;
    }


    /**
     * 产线行分摊-数据+合计调整
     *
     * @param classDateAllData 数据集合
     * @param lineResMap  产线数据集合
     */
    private void ftLineRow(List<EpMeterReadAllDataVO> classDateAllData, Map<String, List<BigDecimal>> lineResMap) {
        for (EpMeterReadAllDataVO ele : classDateAllData) {
            List<BigDecimal> valueList = lineResMap.get(mapKey(ele.getSysOfficeId(), ele.getReadDate()));
            BigDecimal sum = BigDecimalUtil.sum(valueList);
            BigDecimal format = BigDecimalUtil.setScale(sum, 0);
            ele.setConsume(format);
        }
    }

    /**
     * 重新计算分摊的行-数据+合计调整
     *
     * @param classDateAllData
     * @param jpResMap
     */
    private void ftJpRow(List<EpMeterReadAllDataVO> classDateAllData, Map<LocalDate, List<BigDecimal>> jpResMap) {
        for (EpMeterReadAllDataVO ele : classDateAllData) {
            List<BigDecimal> valueList = jpResMap.get(ele.getReadDate());
            BigDecimal sum = BigDecimalUtil.sum(valueList);
            BigDecimal format = BigDecimalUtil.setScale(sum, 0);
            ele.setConsume(format);
        }
    }


    /**
     * 基于N按b1、a1.1 + a1.2 + a1.3、a2.1 + a2.2 + a2.3、... 、a8.1 + a8.2 + a8.3共9部分的比例分摊，则N分摊为b3、a1.4、a2.4、... 、a8.4共9个值
     *
     * @param classDateAllData 全量数据表
     * @param dsjList          大数据节点
     * @param sjgLineList      深加工产线节点
     * @param timeList         时间集合
     * @param nRateMap         分摊比例map
     * @param jpResMap         基片map
     * @param lineResMap       深加工产线map
     */
    private void dsj(List<EpMeterReadAllDataVO> classDateAllData, List<Integer> dsjList, List<Integer> sjgLineList, List<LocalDate> timeList, Map<String, BigDecimal> nRateMap, Map<LocalDate, List<BigDecimal>> jpResMap, Map<String, List<BigDecimal>> lineResMap) {
        List<EpMeterReadAllDataVO> dsjDataList = classDateAllData.stream().filter(v -> dsjList.contains(v.getSysOfficeId())).collect(Collectors.toList());
        for (LocalDate ele : timeList) {
            List<BigDecimal> timeDataList = dsjDataList.stream().filter(v -> v.getReadDate().isEqual(ele)).map(EpMeterReadAllDataVO::getConsume).collect(Collectors.toList());
            BigDecimal dsjValue = BigDecimalUtil.sum(timeDataList);
            //基片分摊
            BigDecimal jpValue = BigDecimalUtil.multiply(dsjValue, nRateMap.get(mapKey(-1, ele)));
            jpResMap.get(ele).add(jpValue);
            //产线分摊value
            for (Integer line : sjgLineList) {
                BigDecimal lineValue = BigDecimalUtil.multiply(dsjValue, nRateMap.get(mapKey(line, ele)));
                lineResMap.get(mapKey(line, ele)).add(lineValue);
            }
        }
    }

    /**
     * N按b1、a1.1 + a1.2 + a1.3、a2.1 + a2.2 + a2.3、... 、a8.1 + a8.2 + a8.3共9部分的比例分摊，
     * 则N分摊为b3、a1.4、a2.4、... 、a8.4共9个值
     *
     * @param sjgLineList 深加工产线集合
     * @param timeList    班次日期集合
     * @param jpResMap    基片制造部数据
     * @param lineResMap  产线数据
     * @return 分摊比例map
     */
    private Map<String, BigDecimal> getNRateMap(List<Integer> sjgLineList, List<LocalDate> timeList, Map<LocalDate, List<BigDecimal>> jpResMap, Map<String, List<BigDecimal>> lineResMap) {
        Map<String, BigDecimal> resMap = Maps.newHashMap();
        for (LocalDate classDate : timeList) {
            BigDecimal b1 = jpResMap.get(classDate).get(0);
            BigDecimal sum = b1;
            Map<Integer, BigDecimal> lineDataMap = Maps.newHashMap();

            for (Integer line : sjgLineList) {
                List<BigDecimal> lineDataList = lineResMap.get(mapKey(line, classDate));
                //ax.1 + ax.2 + ax.3
                BigDecimal lineDaySum = BigDecimalUtil.sum(lineDataList);
                sum = BigDecimalUtil.add(sum, lineDaySum);
                lineDataMap.put(line, lineDaySum);
            }
            //基片制造部比例
            resMap.put(mapKey(-1, classDate), BigDecimalUtil.divide(b1, sum));
            //深加工产线比例
            for (Map.Entry<Integer, BigDecimal> entry : lineDataMap.entrySet()) {
                resMap.put(mapKey(entry.getKey(), classDate), BigDecimalUtil.divide(entry.getValue(), sum));
            }
        }
        return resMap;
    }

    /**
     * 获取b2
     *
     * @param classDateAllData 全量数据表
     * @param fl15             风冷螺杆式空压机1 + 风冷螺杆式空压机2 + 风冷螺杆式空压机3 + 风冷螺杆式空压机4 + 风冷螺杆式空压机5 + 微热吸附式干燥器1 + 微热吸附式干燥器2
     * @param timeList         时间集合
     * @param jpResMap         基片的数据集合
     */
    private void fl15(List<EpMeterReadAllDataVO> classDateAllData, List<Integer> fl15, List<LocalDate> timeList, Map<LocalDate, List<BigDecimal>> jpResMap) {
        List<EpMeterReadAllDataVO> fl610DataList = classDateAllData.stream().filter(v -> fl15.contains(v.getSysOfficeId())).collect(Collectors.toList());
        for (LocalDate ele : timeList) {
            List<BigDecimal> timeDataList = fl610DataList.stream().filter(v -> v.getReadDate().isEqual(ele)).map(EpMeterReadAllDataVO::getConsume).collect(Collectors.toList());
            BigDecimal b2 = BigDecimalUtil.sum(timeDataList);
            jpResMap.get(ele).add(b2);
        }
    }

    /**
     * 查询的6个总能耗值加总记为M 分摊到产线
     *
     * @param classDateAllData  全量数据表
     * @param fl610             风冷螺杆式空压机6 + 风冷螺杆式空压机7 + 风冷螺杆式空压机8 + 风冷螺杆式空压机9 + 风冷螺杆式空压机10 + 微热吸附式干燥器3
     * @param sjgLineList       深加工产线集合
     * @param timeList          时间集合
     * @param productNumRateMap 产量比例集合
     * @param lineResMap        产线数据集合
     */
    private void fl610(List<EpMeterReadAllDataVO> classDateAllData, List<Integer> fl610, List<Integer> sjgLineList, List<LocalDate> timeList, Map<String, BigDecimal> productNumRateMap, Map<String, List<BigDecimal>> lineResMap) {
        List<EpMeterReadAllDataVO> fl610DataList = classDateAllData.stream().filter(v -> fl610.contains(v.getSysOfficeId())).collect(Collectors.toList());
        for (LocalDate ele : timeList) {
            List<BigDecimal> timeDataList = fl610DataList.stream().filter(v -> v.getReadDate().isEqual(ele)).map(EpMeterReadAllDataVO::getConsume).collect(Collectors.toList());
            BigDecimal reduce = BigDecimalUtil.sum(timeDataList);
            //a1.3 a2.3 a3.3 a4.3 a5.3 a6.3 a7.3 a8.3
            for (Integer sjgLine : sjgLineList) {
                String mapKey = mapKey(sjgLine, ele);
                BigDecimal rateValue = BigDecimalUtil.multiply(reduce, productNumRateMap.get(mapKey));
                lineResMap.get(mapKey).add(rateValue);
            }
        }
    }

    /**
     * 公用电 对应2个集合
     *
     * @param classDateAllData  全量数据表
     * @param gydList           公用电集合
     * @param lineList          产线集合【1-4 ，5-8】
     * @param timeList          时间集合
     * @param productNumRateMap 产量集合
     * @param lineResMap        产线返回集合
     */
    private void gydRate(List<EpMeterReadAllDataVO> classDateAllData, List<Integer> gydList, List<List<Integer>> lineList, List<LocalDate> timeList, Map<String, BigDecimal> productNumRateMap, Map<String, List<BigDecimal>> lineResMap) {
        for (int i = 0; i < gydList.size(); i++) {
            int finalI = i;
            List<EpMeterReadAllDataVO> gydDataList = classDateAllData.stream().filter(v -> gydList.get(finalI).equals(v.getSysOfficeId())).collect(Collectors.toList());
            //a1.2 a2.2 a3.2 a4.2
            //a5.2 a6.2 a7.2 a8.2
            for (LocalDate ele : timeList) {
                List<BigDecimal> timeDataList = gydDataList.stream().filter(v -> v.getReadDate().isEqual(ele)).map(EpMeterReadAllDataVO::getConsume).collect(Collectors.toList());
                BigDecimal reduce = BigDecimalUtil.sum(timeDataList);
                for (Integer sjgLine : lineList.get(i)) {
                    String mapKey = mapKey(sjgLine, ele);
                    BigDecimal rateValue = BigDecimalUtil.multiply(reduce, productNumRateMap.get(mapKey));
                    lineResMap.get(mapKey).add(rateValue);
                }
            }
        }
    }

    /**
     * 获取产量比例的map
     *
     * @param sjgLine14List 深加工四条产线【一二三四】
     * @param sjgLine58List 深加工四条产线【五六七八】
     * @param timeList      班次日期集合
     * @param productNumMap 产量集合
     * @return
     */
    private TupleDTO<Map<String, BigDecimal>, Map<String, BigDecimal>> getProductNumRateMap(List<Integer> sjgLine14List, List<Integer> sjgLine58List, List<LocalDate> timeList, Map<String, BigDecimal> productNumMap) {
        Map<String, BigDecimal> productNumLineRateMap = Maps.newHashMap();
        Map<String, BigDecimal> productNumRateMap = Maps.newHashMap();

        //深加工8条产线
        List<Integer> sjgLineList = Lists.newArrayList();
        sjgLine14List.forEach(ele -> sjgLineList.add(ele));
        sjgLine58List.forEach(ele -> sjgLineList.add(ele));

        //获取几条产线的每日产量对比
        for (LocalDate classDate : timeList) {
            //深加工四条产线【一二三四】
            BigDecimal sum14 = BigDecimal.ZERO;
            for (Integer line : sjgLine14List) {
                sum14 = BigDecimalUtil.add(sum14, productNumMap.getOrDefault(mapKey(line, classDate), BigDecimal.ZERO));
            }
            for (Integer line : sjgLine14List) {
                String mapKey = mapKey(line, classDate);
                BigDecimal num = productNumMap.getOrDefault(mapKey, BigDecimal.ZERO);
                productNumLineRateMap.put(mapKey, BigDecimalUtil.divide(num, sum14));
            }
            //深加工四条产线【五六七八】
            BigDecimal sum58 = BigDecimal.ZERO;
            for (Integer line : sjgLine58List) {
                sum58 = BigDecimalUtil.add(sum58, productNumMap.getOrDefault(mapKey(line, classDate), BigDecimal.ZERO));
            }
            for (Integer line : sjgLine58List) {
                String mapKey = mapKey(line, classDate);
                BigDecimal num = productNumMap.getOrDefault(mapKey, BigDecimal.ZERO);
                productNumLineRateMap.put(mapKey, BigDecimalUtil.divide(num, sum58));
            }
            //深加工八条产线【一二三四五六七八】
            BigDecimal sum = BigDecimal.ZERO;
            for (Integer line : sjgLineList) {
                sum = BigDecimalUtil.add(sum, productNumMap.getOrDefault(mapKey(line, classDate), BigDecimal.ZERO));
            }
            for (Integer line : sjgLineList) {
                String mapKey = mapKey(line, classDate);
                BigDecimal num = productNumMap.getOrDefault(mapKey, BigDecimal.ZERO);
                productNumRateMap.put(mapKey, BigDecimalUtil.divide(num, sum));
            }
        }
        return new TupleDTO<>(productNumLineRateMap, productNumRateMap);
    }

    /**
     * 产线和日期拼接成key
     *
     * @param line
     * @param date
     * @return
     */
    private String mapKey(Integer line, LocalDate date) {
        StringBuilder sb = new StringBuilder();
        sb.append(line).append("_").append(date);
        return sb.toString();
    }


    @Override
    public List<EpMeterReadAllDataVO> unitConsumeFacade(Set<LocalDate> dateSet, Integer energyType, List<Long> partitionIdList) {
        if (energyType == 5) {
            return kcalDataUnit(dateSet, partitionIdList);
        } else if (energyType == 1) {
            return classDateAllDataUnit(dateSet, energyType, partitionIdList);
        }
        return classDateAllDataUnitOther(dateSet, energyType, partitionIdList);
    }

    /**
     * 获取能耗的二维表格
     *
     * @param dateSet          日期集合
     * @param energyType       能源类型
     * @param nodeLinkMeterMap 节点关联仪表map
     * @return
     */
    public List<EpEnergyConsumeDTO> consumeTable(Set<LocalDate> dateSet, Integer energyType, Map<Long, EpInstrumentAccountDTO> nodeLinkMeterMap) {
        //获取生产明细样例表
        List<DwProduceOrderDTO> orderDetailList = Lists.newArrayList();
        //根据时间获取订单
        List<EpMeterReadDistantDTO> orderList = orderDetail(dateSet, nodeLinkMeterMap.values());
        if (CollUtil.isNotEmpty(orderList)) {
            orderList.forEach(ele -> {
                DwProduceOrderDTO dwProduceOrderDTO = new DwProduceOrderDTO();
                BeanUtils.copyProperties(ele, dwProduceOrderDTO);
                dwProduceOrderDTO.setClassDate(ele.getReadDate());
                orderDetailList.add(dwProduceOrderDTO);
            });
        }
        EpInstrumentAccountDTO defaultAccount = new EpInstrumentAccountDTO();
        //查询这个班次日期 所有节点的抄表能耗【上一次抄表与当前次抄表的差值】和远传仪表耗能,用于填充二维表格的数据
        Set<Integer> remoteMeterIdSet = Sets.newHashSet();
        Set<Integer> handMeterIdSet = Sets.newHashSet();
        for (EpInstrumentAccountDTO account : nodeLinkMeterMap.values()) {
            if (account.getEntityTable() == 0) {
                remoteMeterIdSet.add(account.getId());
            } else if (account.getEntityTable() == 1) {
                handMeterIdSet.add(account.getId());
            }
        }
        //查询虚拟仪表的孩子是什么仪表，返回map key => 虚拟仪表节点，0全部是远传 1是机械+远传或者全是机械  null 是没有孩子仪表
        Map<Long, EpChildTypeDTO> childTypeMap = epPartitionService.childTypeMap(energyType);
        childTypeMap.forEach((k, v) -> v.setAccount(nodeLinkMeterMap.get(k)));

        //获取远传仪表的数据 key => 仪表id + "_" + 班次日期 +"_" + 班次id + "_" + 厚度 value => 读数
        Map<String, BigDecimal> remoteDataMap = remoteRead(remoteMeterIdSet, dateSet);
        //获取手动抄表的数据 key => 仪表id + "_" + 班次日期  value => 读数
        Map<String, BigDecimal> handReadDataMap = handRead(energyType, handMeterIdSet, dateSet);

        //构建能耗分区树二维表格【班次日期，班次，产品厚度，xxx节点，yyy节点....】
        List<BigDecimal> thickList = orderDetailList.stream().filter(v -> Objects.nonNull(v.getThick())).map(DwProduceOrderDTO::getThick).distinct().sorted().collect(Collectors.toList());
        //如果厚度集合不包含无厚度，则加入无厚度
        BigDecimal noThick = BigDecimalUtil.MINUS_ONE;
        if (!thickList.contains(noThick)) {
            thickList.add(noThick);
        }
        List<EpEnergyConsumeDTO> rowList = Lists.newArrayList();
        //存放map的集合
        Map<String, Map<Long, BigDecimal>> consumeCol = Maps.newHashMap();
        dateSet.forEach(date -> {
            Lists.newArrayList(0, 1, -1).forEach(classes -> {
                thickList.forEach(thick -> {
                    Map<Long, BigDecimal> consumeMap = Maps.newLinkedHashMap();
                    //将所有的map都汇总起来
                    consumeCol.put(date + "_" + classes + "_" + thick, consumeMap);
                    childTypeMap.forEach((k, v) -> {
                        EpInstrumentAccountDTO account = v.getAccount();
                        if (Objects.nonNull(account) && Objects.nonNull(account.getEntityTable())) {
                            if (account.getEntityTable() == 0) {
                                //远传仪表
                                MapUtil.putIfNotNull(consumeMap, k, remoteDataMap.get(account.getId() + "_" + date + "_" + classes + "_" + thick));
                            } else if (account.getEntityTable() == 1 && noThickAndClass(classes, thick)) {
                                //表读数来自人工抄表，每天抄一次）：仅统计每日能耗（类型II）
                                MapUtil.putIfNotNull(consumeMap, k, handReadDataMap.get(account.getId() + "_" + date));
                            } else if (account.getEntityTable() == 2) {
                                //虚拟仪表
                                if (Integer.valueOf(0).equals(v.getType())) {
                                    //统计能耗考虑班次、产品厚度（类型I）
                                    BigDecimal value = BigDecimal.ZERO;
                                    for (Long child : v.getChildren()) {
                                        value = BigDecimalUtil.add(value, consumeMap.get(child));
                                    }
                                    MapUtil.putIfNotNull(consumeMap, k, value);
                                } else if (Integer.valueOf(1).equals(v.getType()) && noThickAndClass(classes, thick)) {
                                    //都是实体非远传仪表
                                    BigDecimal value = BigDecimal.ZERO;
                                    for (Long child : v.getChildren()) {
                                        value = BigDecimalUtil.add(value, consumeMap.get(child));
                                    }
                                    MapUtil.putIfNotNull(consumeMap, k, value);
                                } else if (Integer.valueOf(2).equals(v.getType()) && noThickAndClass(classes, thick)) {
                                    //既有实体远传仪表，又有虚拟仪表，还有非实体远传仪表
                                    BigDecimal value = BigDecimal.ZERO;
                                    for (Long child : v.getChildren()) {
                                        EpInstrumentAccountDTO accountDTO = nodeLinkMeterMap.getOrDefault(child, defaultAccount);
                                        //如果孩子是实体非远传仪表
                                        if (Integer.valueOf(1).equals(accountDTO.getEntityTable())) {
                                            value = BigDecimalUtil.add(value, consumeMap.get(child));
                                        } else {
                                            //获取当前班次日期，所有的实体远传仪表的读数之和
                                            //孩子是虚拟仪表
                                            for (Map.Entry<String, Map<Long, BigDecimal>> entry : consumeCol.entrySet()) {
                                                if (entry.getKey().indexOf(date + "_") != -1) {
                                                    value = BigDecimalUtil.add(value, entry.getValue().get(child));
                                                }
                                            }
                                        }
                                    }
                                    MapUtil.putIfNotNull(consumeMap, k, value);
                                }
                            }
                        }
                    });
                    rowList.add(new EpEnergyConsumeDTO(date, classes, thick, consumeMap));
                });
            });
        });
        return rowList;
    }

    /**
     * 将2维表格转换为list集合，返回全量数据表
     *
     * @param energyType       能源类型
     * @param partitionIdList  过滤参数
     * @param nodeLinkMeterMap 节点绑定仪表
     * @param epPartitionMap   能源map
     * @param rowList          二维表格数据
     * @param classGroupMap    班组map
     * @return
     */
    private List<EpMeterReadAllDataVO> row2List(Integer energyType, Set<Long> partitionIdList, Map<Long, EpInstrumentAccountDTO> nodeLinkMeterMap, Map<Long, EpPartition> epPartitionMap, List<EpEnergyConsumeDTO> rowList, Map<String, Long> classGroupMap) {
        EpPartition defaultNode = new EpPartition();
        List<EpMeterReadAllDataVO> resList = Lists.newArrayList();
        for (EpEnergyConsumeDTO row : rowList) {
            Map<Long, BigDecimal> yieldMap = Objects.isNull(row.getYieldMap()) ? Maps.newHashMap() : row.getYieldMap();
            row.getConsumeMap().forEach((key, value) -> {
                EpMeterReadAllDataVO resVo = new EpMeterReadAllDataVO();
                resVo.setReadDate(row.getReadDate());
                resVo.setClassId(row.getClassId());
                resVo.setThick(row.getThick());
                resVo.setEnergyType(energyType);
                if (partitionIdList.contains(key) && Objects.nonNull(value) && BigDecimalUtil.gt(value, BigDecimal.ZERO)) {
                    EpInstrumentAccountDTO accountDTO = nodeLinkMeterMap.getOrDefault(key,new EpInstrumentAccountDTO());
                    resVo.setMeterId(accountDTO.getId());
                    resVo.setConsume(value);
                    resVo.setOfficeId(key);
                    EpPartition epPartition = epPartitionMap.getOrDefault(key, defaultNode);
                    resVo.setSysOfficeId(epPartition.getOfficeId());
                    resVo.setOfficeName(epPartition.getOfficeName());
                    resVo.setProductNum(yieldMap.get(key));
                    if (CollUtil.isNotEmpty(classGroupMap)) {
                        resVo.setClassGroupId(classGroupMap.get(row.getReadDate() + "_" + row.getClassId() + "_" + epPartition.getOfficeId()));
                    }
                    resList.add(resVo);
                }
            });
        }
        return resList;
    }

    /**
     * 获取手动抄表的数据
     *
     * @param energyType 能源类型
     * @param meterIdSet 远传仪表id集合
     * @param dateSet    日期集合
     * @return
     */
    private Map<String, BigDecimal> handRead(Integer energyType, Set<Integer> meterIdSet, Set<LocalDate> dateSet) {
        Map<String, BigDecimal> resMap = Maps.newHashMap();

        //获取当前日期最小日期的前一天，用于求差值
        //获取当前日期前的最大值 作为参考值
        List<LocalDate> sortedDateList = CollectionUtil.sort(dateSet, Comparator.comparing(x -> x));
        LocalDate minLocalDate = sortedDateList.get(0).minusDays(1);
        sortedDateList.add(minLocalDate);

        //查询全量数据表-手动抄表部分【需要查询前一天，涉及差值求当前值】
        if (CollUtil.isNotEmpty(meterIdSet) && CollUtil.isNotEmpty(dateSet)) {
            List<EpMeterRead> dataList = list(new LambdaQueryWrapper<EpMeterRead>()
                    .in(EpMeterRead::getMeterId, meterIdSet)
                    .in(EpMeterRead::getReadDate, sortedDateList));
            if (CollUtil.isNotEmpty(dataList)) {
                Map<Integer, BigDecimal> lastValueMap = Maps.newHashMap();
                LocalDate minLocalDatePrev = minLocalDate.minusDays(10);
                List<EpMeterRead> lastValueList = this.baseMapper.selectLastValueMax(meterIdSet, minLocalDate, minLocalDatePrev);
                if (CollUtil.isNotEmpty(lastValueList)) {
                    lastValueList.forEach(ele -> lastValueMap.put(ele.getMeterId(), ele.getAdminValue()));
                }
                //手抄仪表，根据班次日期+仪表id排序
                List<EpMeterRead> sortDataList = dataList.stream().sorted(Comparator.comparing(EpMeterRead::getReadDate).thenComparing(EpMeterRead::getMeterId)).collect(Collectors.toList());
                //求手抄仪表的差值
                //存放仪表的班次日期的抄表数据
                Map<Integer, Map<LocalDate, List<EpMeterRead>>> groupMap = new LinkedHashMap<>();
                //将数据按照班次日期分组
                sortDataList.forEach(ele -> {
                    // 2层map的插入，k1和k2相同的元素，存放到集合中
                    MapUtil.biCompute(groupMap, ele.getMeterId(), ele.getReadDate(), ele);
                });

                BigDecimal preValue;
                for (Map.Entry<Integer, Map<LocalDate, List<EpMeterRead>>> meterEntry : groupMap.entrySet()) {
                    preValue = lastValueMap.get(meterEntry.getKey());
                    for (Map.Entry<LocalDate, List<EpMeterRead>> dateEntry : meterEntry.getValue().entrySet()) {
                        //手动抄表 一天一条,去取最后一条
                        BigDecimal curValue = dateEntry.getValue().get(dateEntry.getValue().size() - 1).getAdminValue();
                        //判断最大是否已经超过了最大读数，最大值是N个9,此处可能会存在些许误差
                        preValue = possibleBigDecimal(preValue, curValue);
                        if (energyType == 3 && !dateEntry.getKey().isEqual(minLocalDate)) {
                            resMap.put(meterEntry.getKey() + "_" + dateEntry.getKey(), curValue);
                        } else if (energyType == 4 && !dateEntry.getKey().isEqual(minLocalDate)) {
                            BigDecimal subtract = BigDecimalUtil.subtract(curValue, preValue);
                            resMap.put(meterEntry.getKey() + "_" + dateEntry.getKey(), BigDecimalUtil.divide(subtract, new BigDecimal("1000"), UnitLengthUtil.UNIT_TON_LENGTH));
                        } else {
                            resMap.put(meterEntry.getKey() + "_" + dateEntry.getKey(), BigDecimalUtil.subtract(curValue, preValue));
                        }
                        if (Objects.nonNull(curValue)) {
                            preValue = curValue;
                        }
                    }
                }
            }
        }
        return resMap;
    }

    /**
     * 可能表溢出的值
     * @param preValue
     * @param curValue
     * @return
     */
    private BigDecimal possibleBigDecimal(BigDecimal preValue, BigDecimal curValue) {
        if (Objects.nonNull(preValue) && Objects.nonNull(curValue)) {
            int p1 = curValue.precision() - curValue.scale();
            int p2 = preValue.precision() - preValue.scale();
            if (BigDecimalUtil.gt(preValue, curValue) && p1 > p2) {
                //对前一个值进行调整，负向减值
                int n = preValue.scale();
                int m = preValue.precision() - n;
                String overflowStr = "";
                for (int cnt = 0; cnt < m; cnt++) {
                    overflowStr += "9";
                }
                if (n > 0) {
                    overflowStr += ".";
                }
                for (int cnt = 0; cnt < n; cnt++) {
                    overflowStr += "9";
                }
                preValue = BigDecimalUtil.subtract(preValue, new BigDecimal(overflowStr));
            }
        }
        return preValue;
    }

    /**
     * 抄表的读数
     *
     * @param meterIdSet 仪表集合
     * @param dateSet    日期集合
     * @return list
     */
    public Map<String, BigDecimal> remoteRead(Set<Integer> meterIdSet, Set<LocalDate> dateSet) {
        Map<String, BigDecimal> resMap = Maps.newHashMap();
        if (CollUtil.isNotEmpty(meterIdSet) && CollUtil.isNotEmpty(dateSet)) {
            List<EpMeterReadDistant> dbList = readDistantService.list(new LambdaQueryWrapper<EpMeterReadDistant>()
                    .in(EpMeterReadDistant::getMeterId, meterIdSet)
                    .in(EpMeterReadDistant::getReadDate, dateSet));
            if (CollUtil.isNotEmpty(dbList)) {
                dbList.forEach(ele -> {
                    if (Objects.isNull(ele.getClassId())) {
                        ele.setClassId(-1);
                    }
                    if (Objects.isNull(ele.getThick())) {
                        ele.setThick(BigDecimalUtil.MINUS_ONE);
                    }
                });

                Map<String, List<EpMeterReadDistant>> groupData = dbList.stream().collect(Collectors.groupingBy(v -> v.getMeterId() + "_" + v.getReadDate() + "_" + v.getClassId() + "_" + v.getThick()));
                groupData.forEach((k, v) -> {
                    List<BigDecimal> reduceList = v.stream().map(EpMeterReadDistant::getReadValue).filter(e -> Objects.nonNull(e)).collect(Collectors.toList());
                    BigDecimal reduce = BigDecimalUtil.sum(reduceList);
                    resMap.put(k, reduce);
                });
            }
        }
        return resMap;
    }

    /**
     * 获取远传仪表编码集合
     *
     * @param dateSet    日期集合
     * @param meterIdSet 仪表id集合
     * @return list
     */
    public List<EpMeterReadDistantDTO> orderDetail(Set<LocalDate> dateSet, Collection<EpInstrumentAccountDTO> meterIdSet) {
        List<EpMeterReadDistantDTO> resList = Lists.newArrayList();
        if (CollUtil.isNotEmpty(dateSet) && CollUtil.isNotEmpty(meterIdSet)) {
            List<EpMeterReadDistant> dbList = readDistantService.list(new LambdaQueryWrapper<EpMeterReadDistant>()
                    .in(EpMeterReadDistant::getMeterId, meterIdSet.stream().map(EpInstrumentAccountDTO::getId).collect(Collectors.toSet()))
                    .in(EpMeterReadDistant::getReadDate, dateSet));
            if (CollUtil.isNotEmpty(dbList)) {
                Map<String, List<EpMeterReadDistant>> collect = dbList.stream().collect(Collectors.groupingBy(v -> v.getMeterId() + "_" + v.getClassId() + "_" + v.getThick() + "_" + v.getProduceOrderId()));
                collect.forEach((k, v) -> {
                    EpMeterReadDistant ele = v.get(0);
                    EpMeterReadDistantDTO model = new EpMeterReadDistantDTO();
                    BeanUtils.copyProperties(ele, model);
                    List<BigDecimal> reduceList = v.stream().map(EpMeterReadDistant::getReadValue).filter(e -> Objects.nonNull(e)).collect(Collectors.toList());
                    BigDecimal reduce = BigDecimalUtil.sum(reduceList);
                    model.setReadValue(reduce);
                    model.setProduceOrderId(ele.getProduceOrderId());
                    resList.add(model);
                });
            }
        }
        return resList;
    }

    @Override
    public List<EpMeterReadAllDataVO> classDateAllDataUnit(Set<LocalDate> dateSet, Integer energyType, List<Long> partitionIdList) {
        SteccnTableAssertUtil.notEmpty(partitionIdList, "组织机构不能为空");
        Assert.notNull(energyType, "能源类型不能为空");
        Assert.notEmpty(dateSet, "日期筛选不可以为空");
        //获取节点关联仪表
        Map<Long, EpInstrumentAccountDTO> nodeLinkMeterMap = epInstrumentAccountService.queryNodeLinkMeter(energyType);
        SteccnTableAssertUtil.notEmpty(nodeLinkMeterMap, "该能源下无仪表");
        //获取每个节点的office信息
        Map<Long, EpPartition> partitionMap = epPartitionService.listById(nodeLinkMeterMap.keySet());
        List<EpEnergyConsumeDTO> consumeTable = unitTable(dateSet, energyType, nodeLinkMeterMap);


        //结合排班表结合报工时填报的班组，在【单耗全量数据表】中加入班组维度，右侧增加三列，每格代表本行的班次是本列的车间的哪个班组（本列的车间所有工位在本行的班次报工时填报的班组中，取填报次数最多的一个班组）
        //获取所有的工位节点

        Map<String, Long> classGroup = getClassGroup(dateSet, partitionIdList, partitionMap);
        return row2List(energyType, Sets.newHashSet(partitionIdList), nodeLinkMeterMap, partitionMap, consumeTable, classGroup);
    }

    @Override
    public List<EpMeterReadAllDataVO> classDateAllDataUnitOther(Set<LocalDate> dateSet, Integer energyType, List<Long> partitionIdList) {
        SteccnTableAssertUtil.notEmpty(partitionIdList, "组织机构不能为空");
        Assert.notNull(energyType, "能源类型不能为空");
        Assert.notEmpty(dateSet, "日期筛选不可以为空");
        //获取节点关联仪表
        Map<Long, EpInstrumentAccountDTO> nodeLinkMeterMap = epInstrumentAccountService.queryNodeLinkMeter(energyType);
        SteccnTableAssertUtil.notEmpty(nodeLinkMeterMap, "该能源下无仪表");
        //获取每个节点的office信息
        Map<Long, EpPartition> partitionMap = epPartitionService.listById(nodeLinkMeterMap.keySet());
        List<EpEnergyConsumeDTO> consumeTable = unitTableOther(dateSet, energyType,nodeLinkMeterMap, partitionMap.values());


        //结合排班表结合报工时填报的班组，在【单耗全量数据表】中加入班组维度，右侧增加三列，每格代表本行的班次是本列的车间的哪个班组（本列的车间所有工位在本行的班次报工时填报的班组中，取填报次数最多的一个班组）
        //获取所有的工位节点
        return row2List(energyType, Sets.newHashSet(partitionIdList), nodeLinkMeterMap, partitionMap, consumeTable, Maps.newHashMap());
    }

    private List<EpEnergyConsumeDTO> unitTableOther(Set<LocalDate> dateSet, Integer energyType, Map<Long, EpInstrumentAccountDTO> nodeLinkMeterMap, Collection<EpPartition> epPartitionList) {
        Map<Integer, Long> meterNodeMap = Maps.newHashMap();
        nodeLinkMeterMap.forEach((k, v) -> meterNodeMap.put(v.getId(), k));
        //查询能耗的全量数据表
        List<EpEnergyConsumeDTO> consumeTable = consumeTable(dateSet, energyType, nodeLinkMeterMap);
        Map<Integer, Long> epIdMap = Maps.newHashMap();
        epPartitionList.forEach(ele ->{
            epIdMap.put(ele.getOfficeId(),ele.getId());
        });
        //构建出当前日期下节点的所有产量，需要考虑递归形势
        LocalDate min = dateSet.stream().min((x, y) -> x.compareTo(y)).get();
        LocalDate max = dateSet.stream().max((x, y) -> x.compareTo(y)).get();
        List<Integer> officeIdList = epPartitionList.stream().map(EpPartition::getOfficeId).collect(Collectors.toList());
        Map<String, BigDecimal> pm = FeignOptional.of(() -> remotePmsFeignApi.orderDetailOtherEnergy(new TripleDTO<>(min, max, officeIdList)), FeignConstant.STECCN_PMS_SERVICE_NAME).get();
        //获取到所有的产量
        consumeTable.forEach(ele ->{
            if (noThickAndClass(ele.getClassId(), ele.getThick())) {
                Map<Long, BigDecimal> yieldMap = Maps.newHashMap();
                ele.setYieldMap(yieldMap);
                for (Integer officeId : officeIdList) {
                    BigDecimal yield = pm.get(JointUtil.joint(JointUtil.SEPARATOR_UNDERLINE, officeId, ele.getReadDate()));
                    Long partitionId = epIdMap.get(officeId);
                    if (Objects.nonNull(yield) && Objects.nonNull(partitionId)) {
                        yieldMap.put(partitionId, yield);
                    }
                }
            }
        });
        return consumeTable;
    }

    /**
     * 获取班组  key：班次日期_班次【0,1】_车间id  value：班组id
     *
     * @param dateSet         日期set集合
     * @param partitionIdList 节点
     * @param partitionMap    节点的map
     */
    private Map<String, Long> getClassGroup(Set<LocalDate> dateSet, List<Long> partitionIdList, Map<Long, EpPartition> partitionMap) {
        List<SysOfficeVO> stationList = FeignOptional.of(() -> remoteOfficeFeign.flatData(SysOfficeConstant.STATION), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        //获取所有的车间
        List<SysOfficeVO> workshopList = FeignOptional.of(() -> remoteOfficeFeign.flatData(SysOfficeConstant.WORKSHOP), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        Set<Integer> stationIdSet = stationList.stream().map(SysOfficeVO::getId).collect(Collectors.toSet());
        Set<Integer> workshopIdSet = workshopList.stream().map(SysOfficeVO::getId).collect(Collectors.toSet());
        //获取所有工位节点的车间父节点
        Map<Integer, Integer> parentWorkshop = FeignOptional.of(() -> remoteOfficeFeign.parentType(new TupleDTO<>(SysOfficeConstant.WORKSHOP, stationIdSet)), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        Set<Integer> wsSet = Sets.newHashSet();
        partitionIdList.forEach(ele -> {
            EpPartition epPartition = partitionMap.get(ele);
            if (Objects.nonNull(epPartition) && workshopIdSet.contains(epPartition.getOfficeId())) {
                wsSet.add(epPartition.getOfficeId());
            }
        });
        Map<Integer, Set<Integer>> shopStationMap = Maps.newHashMap();
        //查询车间的工位
        parentWorkshop.forEach((k, v) -> {
            if (wsSet.contains(v)) {
                MapUtil.computeSet(shopStationMap, v, k);
            }
        });
        //根据工位+时间获取车间班次报工最多的班组
        Map<String, Long> workShopClassMap = FeignOptional.of(() -> remoteReportWorkFeignApi.workshopClassGroupMap(new TupleDTO<>(dateSet, shopStationMap)), FeignConstant.STECCN_PMS_SERVICE_NAME).get();
        return workShopClassMap;
    }

    /**
     * 单耗的全量数据表
     *
     * @param dateSet          日期集合
     * @param energyType       能源类型
     * @param nodeLinkMeterMap 关联仪表map
     * @return
     */
    private List<EpEnergyConsumeDTO> unitTable(Set<LocalDate> dateSet, Integer energyType, Map<Long, EpInstrumentAccountDTO> nodeLinkMeterMap) {
        Map<Integer, Long> meterNodeMap = Maps.newHashMap();
        nodeLinkMeterMap.forEach((k, v) -> meterNodeMap.put(v.getId(), k));

        //查询能耗的全量数据表
        List<EpEnergyConsumeDTO> consumeTable = consumeTable(dateSet, energyType, nodeLinkMeterMap);
        Set<BigDecimal> thickSet = consumeTable.stream().map(EpEnergyConsumeDTO::getThick).collect(Collectors.toSet());
        //获取所有节点的具体信息
        Map<Long, EpPartition> partitionMap = epPartitionService.listById(nodeLinkMeterMap.keySet());
        //查询所有节点的产线父节点
        Set<Integer> nodeOfficeIdSet = partitionMap.values().stream().map(EpPartition::getOfficeId).collect(Collectors.toSet());
        //日期排序
        LocalDate min = dateSet.stream().min((x, y) -> x.compareTo(y)).get();
        LocalDate max = dateSet.stream().max((x, y) -> x.compareTo(y)).get();
        //获取所有产线的产量
        Map<String, BigDecimal> numMap = FeignOptional.of(() -> remotePmsFeignApi.orderDetailElectricEnergy(new Tuple4DTO<>(min, max, Lists.newArrayList(nodeOfficeIdSet), Lists.newArrayList(thickSet))), FeignConstant.STECCN_PMS_SERVICE_NAME).get();
        for (EpEnergyConsumeDTO consume : consumeTable) {
            consume.setYieldMap(Maps.newHashMap());
            Map<Long, BigDecimal> consumeMap = consume.getConsumeMap();
            Map<Long, BigDecimal> yieldMap = consume.getYieldMap();
            consumeMap.forEach((k,v) ->{
                EpPartition epPartition = partitionMap.getOrDefault(k,new EpPartition());
                String joint = JointUtil.joint(JointUtil.SEPARATOR_UNDERLINE, epPartition.getOfficeId(), consume.getReadDate(), consume.getClassId(), consume.getThick());
                BigDecimal bigDecimal = numMap.get(joint);
                if(Objects.nonNull(bigDecimal)){
                    yieldMap.put(k, bigDecimal);
                }
            });
        }
        return consumeTable;
    }

    /**
     * 获取节点的产线孩子
     *
     * @param lineParentMap
     * @return
     */
    private Map<Long, Set<Long>> getLineChildrenMap(Map<Long, Set<Long>> lineParentMap) {
        Map<Long, Set<Long>> resMap = Maps.newHashMap();
        //遍历所有的产线父节点
        for (Map.Entry<Long, Set<Long>> entry : lineParentMap.entrySet()) {
            for (Long parent : entry.getValue()) {
                MapUtil.computeSet(resMap, parent, entry.getKey());
            }
        }
        return resMap;
    }

    /**
     * 获取根节点
     *
     * @param nodeList 所有节点
     * @return
     */
    private EpPartition root(Collection<EpPartition> nodeList) {
        Optional<EpPartition> first = nodeList.stream().filter(v -> Objects.isNull(v.getParentId())).findFirst();
        return first.isPresent() ? first.get() : null;
    }


    /**
     * 获取产线节点的父节点
     *
     * @param nodeList   所有的树上节点
     * @param jpLineSet  基片产线节点
     * @param sjgLineSet 深加工产线节点
     * @return map
     */
    private Map<Long, Set<Long>> getLineParentMap(Collection<EpPartition> nodeList, Set<Integer> jpLineSet, Set<Integer> sjgLineSet) {
        Set<Integer> lineOfficeIdSet = Sets.newHashSet();
        Map<Long, Set<Long>> resMap = Maps.newHashMap();
        lineOfficeIdSet.addAll(jpLineSet);
        lineOfficeIdSet.addAll(sjgLineSet);
        //获取产线节点的集合
        List<EpPartition> lineSet = nodeList.stream().filter(v -> lineOfficeIdSet.contains(v.getOfficeId())).collect(Collectors.toList());

        for (EpPartition line : lineSet) {
            resMap.put(line.getId(), parentLine(Sets.newHashSet(line.getParentId()), nodeList));
        }
        return resMap;
    }

    /**
     * 产线父节点
     *
     * @param parentIdSet 当前节点id集合
     * @param nodeList
     * @return
     */
    public Set<Long> parentLine(Set<Long> parentIdSet, Collection<EpPartition> nodeList) {
        Set<Long> resSet = Sets.newHashSet();
        if (CollUtil.isNotEmpty(parentIdSet)) {
            List<EpPartition> parentList = nodeList.stream().filter(v -> parentIdSet.contains(v.getId())).collect(Collectors.toList());
            Set<Long> currentIdSet = parentList.stream().map(EpPartition::getId).collect(Collectors.toSet());
            resSet.addAll(currentIdSet);
            resSet.addAll(parentLine(parentList.stream().map(EpPartition::getParentId).collect(Collectors.toSet()), nodeList));
        }
        return resSet;
    }

    /**
     * 没有厚度和班次
     *
     * @param classId 班次id
     * @param thick   厚度id
     * @return
     */
    private boolean noThickAndClass(Integer classId, BigDecimal thick) {
        BigDecimal noThick = BigDecimalUtil.MINUS_ONE;
        return classId == -1 && noThick.equals(thick);
    }

    /**
     * 将EpMeterReadRowDTO 转换为QtEmsPullAmountDTO
     *
     * @param rowDTO 对象
     * @return
     */
    private QtEmsPullAmountDTO convertToJpDto(EpMeterReadRowDTO rowDTO, Integer lineId) {
        QtEmsPullAmountDTO dto = new QtEmsPullAmountDTO();
        BeanUtils.copyProperties(rowDTO, dto);
        dto.setClassDate(rowDTO.getReadDate());
        dto.setClassId(rowDTO.getClassId());
        dto.setStartTime(rowDTO.getStart());
        dto.setProductLineId(lineId);
        dto.setEndTime(rowDTO.getEnd());
        return dto;
    }

    /**
     * 获取key过滤后的产线
     *
     * @param key 关键字
     * @return
     */
    private Set<Integer> getLine(String key) {
        SysOfficeTypeKeyDTO sysOfficeTypeKeyDTO = new SysOfficeTypeKeyDTO();
        sysOfficeTypeKeyDTO.setType(Lists.newArrayList(SysOfficeConstant.PRODUCT_LINE));
        sysOfficeTypeKeyDTO.setKey(key);
        List<SysOfficeVO> sysOfficeVOList = FeignOptional.of(() -> remoteOfficeFeign.getListByTypeAndKey(sysOfficeTypeKeyDTO), FeignConstant.STECCN_PMS_SERVICE_NAME).get();
        Set<Integer> resSet = sysOfficeVOList.stream().map(SysOfficeVO::getId).collect(Collectors.toSet());
        return resSet;
    }

    @Override
    public List<EpMeterReadAllDataVO> kcalDataUnit(Set<LocalDate> dateSet, List<Long> partitionIdList) {
        //若能源类型为“燃料(气+油)”，需要天然气【能耗全量数据表】、燃料油【能耗全量数据表】两张表，结合【产量全量数据表】做如下计算，即可得到【单耗全量数据表-终版】
        //第一步获取天然气【能耗全量数据表】
        Integer gasEnergyType = 3;
        //获取节点关联仪表
        Map<Long, EpInstrumentAccountDTO> gasNodeLinkMeterMap = epInstrumentAccountService.queryNodeLinkMeter(gasEnergyType);
        List<EpEnergyConsumeDTO> gasRowList = consumeTable(dateSet, gasEnergyType, gasNodeLinkMeterMap);
        Map<String, EpEnergyConsumeDTO> gasRowMap = Maps.newHashMap();
        gasRowList.forEach(ele -> gasRowMap.put(ele.getReadDate() + "_" + ele.getClassId() + "_" + ele.getThick(), ele));

        Map<Long, EpPartition> gasPartitionMap = epPartitionService.listById(gasNodeLinkMeterMap.keySet());
        Map<Long, Integer> gasPoMap = Maps.newHashMap();
        Map<Integer, Long> gasOpMap = Maps.newHashMap();
        gasPartitionMap.forEach((k, v) -> {
            gasPoMap.put(k, v.getOfficeId());
            gasOpMap.put(v.getOfficeId(), k);
        });

        //第二步获取燃料油【能耗全量数据表】
        Integer oilEnergyType = 4;
        //获取节点关联仪表
        Map<Long, EpInstrumentAccountDTO> oilNodeLinkMeterMap = epInstrumentAccountService.queryNodeLinkMeter(oilEnergyType);
        List<EpEnergyConsumeDTO> oilRowList = consumeTable(dateSet, oilEnergyType, oilNodeLinkMeterMap);
        Map<String, EpEnergyConsumeDTO> oilRowMap = Maps.newHashMap();
        oilRowList.forEach(ele -> oilRowMap.put(ele.getReadDate() + "_" + ele.getClassId() + "_" + ele.getThick(), ele));

        Map<Long, EpPartition> oilPartitionMap = epPartitionService.listById(oilNodeLinkMeterMap.keySet());
        Map<Long, Integer> oilPoMap = Maps.newHashMap();
        Map<Integer, Long> oilOpMap = Maps.newHashMap();
        oilPartitionMap.forEach((k, v) -> {
            oilPoMap.put(k, v.getOfficeId());
            oilOpMap.put(v.getOfficeId(), k);
        });
        EpEnergyConsumeDTO defaultConsumeDTO = new EpEnergyConsumeDTO();

        //第三步获取【产量全量数据表】
        Integer gasOilEnergyType = 5;
        Map<Long, EpPartition> gasOilPartitionMap = epPartitionService.listById(Sets.newHashSet(partitionIdList));
        Map<Long, Integer> gasOilPoMap = Maps.newHashMap();
        Map<Integer, Long> gasOilOpMap = Maps.newHashMap();
        List<Integer> officeIdList = Lists.newArrayList();
        gasOilPartitionMap.forEach((k, v) -> {
            gasOilPoMap.put(k, v.getOfficeId());
            gasOilOpMap.put(v.getOfficeId(), k);
            officeIdList.add(v.getOfficeId());
        });
        //考虑产量
        LocalDate min = dateSet.stream().min((x, y) -> x.compareTo(y)).get();
        LocalDate max = dateSet.stream().max((x, y) -> x.compareTo(y)).get();
        Map<String, BigDecimal> officeYieldMap = FeignOptional.of(() -> remotePmsFeignApi.orderDetailOtherEnergyPf(new TripleDTO<>(min, max, officeIdList)), FeignConstant.STECCN_PMS_SERVICE_NAME).get();

        //任意单元格K的值= (天然气能耗 * 7800 + 燃料油能耗 * 1000 * 9300) / (拉引量 * 1000)
        for (EpEnergyConsumeDTO ele : oilRowList) {
            Map<Long, BigDecimal> consumeMap = ele.getConsumeMap();
            Map<Long, BigDecimal> convertConsumeMap = Maps.newHashMap();
            Map<Long, BigDecimal> convertYieldMap = Maps.newHashMap();
            //注意单耗和能耗都是依赖ep_partition中的主键，需要转换成officeId来计算
            EpEnergyConsumeDTO oilConsumeDTO = oilRowMap.getOrDefault(ele.getReadDate() + "_" + ele.getClassId() + "_" + ele.getThick(), defaultConsumeDTO);
            Map<Long, BigDecimal> oilConsumeMap = oilConsumeDTO.getConsumeMap();
            EpEnergyConsumeDTO gasConsumeDTO = gasRowMap.getOrDefault(ele.getReadDate() + "_" + ele.getClassId() + "_" + ele.getThick(), defaultConsumeDTO);
            Map<Long, BigDecimal> gasConsumeMap = gasConsumeDTO.getConsumeMap();

            for (Map.Entry<Long, BigDecimal> entry : consumeMap.entrySet()) {
                Integer gasOilOfficeId = oilPoMap.get(entry.getKey());
                BigDecimal yield = officeYieldMap.get(JointUtil.joint(JointUtil.SEPARATOR_UNDERLINE, gasOilOfficeId, ele.getReadDate()));
                BigDecimal yieldMu = BigDecimalUtil.multiply(yield, new BigDecimal("1000"));
                Long gasId = gasOpMap.get(gasOilOfficeId);
                Long oilId = oilOpMap.get(gasOilOfficeId);
                BigDecimal gasValue = gasConsumeMap.get(gasId);
                BigDecimal oilValue = oilConsumeMap.get(oilId);
                BigDecimal gasMu = BigDecimalUtil.multiply(gasValue, new BigDecimal("7800"));
                BigDecimal oilMu = BigDecimalUtil.multiply(oilValue, new BigDecimal("9300000"));
                entry.setValue(BigDecimalUtil.add(gasMu, oilMu));
                Long convertPartitionId = gasOilOpMap.get(gasOilOfficeId);
                convertConsumeMap.put(convertPartitionId, entry.getValue());
                convertYieldMap.put(convertPartitionId, yieldMu);
            }
            ele.setConsumeMap(convertConsumeMap);
            ele.setYieldMap(convertYieldMap);
        }
        return row2List(gasOilEnergyType, Sets.newHashSet(partitionIdList), oilNodeLinkMeterMap, gasOilPartitionMap, oilRowList, Maps.newHashMap());
    }

    private EpPartition partitionRecursion(List<EpPartition> list, EpPartition partition, StringBuilder sb) {
        Optional<EpPartition> partitionOptional = list.stream().filter(epPartition -> epPartition.getId().equals(partition.getParentId())).findAny();
        if (partitionOptional.isPresent() && Objects.nonNull(partitionOptional.get().getParentId())) {
            sb.insert(0, partitionOptional.get().getOfficeName() + "/");
            return partitionRecursion(list, partitionOptional.get(), sb);
        }
        return null;
    }

    private List<MeterReadListVO> recursion(List<MeterReadListVO> meterReadListVOList, Long partitionNode) {
        return meterReadListVOList.stream().filter(meterReadMapper -> Objects.equals(meterReadMapper.getParentId(), partitionNode)).peek(
                meterReadListVO -> {
                    meterReadListVO.setMeterReadListVOList(recursion(meterReadListVOList, meterReadListVO.getPartitionNode()));
                    //如果是虚拟仪表
                    if (Objects.equals(meterReadListVO.getEntityTable(), 2)) {
                        for (int i = 0; i < meterReadListVO.getReadValueList().size(); i++) {
                            int finalI = i;
                            List<List<ReadValueListVO>> readValueAllList = new ArrayList<>();
                            reduceRecursion(meterReadListVO.getMeterReadListVOList(), readValueAllList);
                            List<BigDecimal> bdList = readValueAllList.stream().filter(v -> Objects.nonNull(v.get(finalI).getReadValue())).map(v -> v.get(finalI).getReadValue()).collect(Collectors.toList());
                            meterReadListVO.getReadValueList().get(i).setReadValue(BigDecimalUtil.sum(bdList));
                        }
                    }
                    //求和
                    List<BigDecimal> bdList = meterReadListVO.getReadValueList().stream().filter(v -> Objects.nonNull(v.getReadValue())).map(ReadValueListVO::getReadValue).collect(Collectors.toList());
                    meterReadListVO.setSum(BigDecimalUtil.sum(bdList));
                }
        ).collect(Collectors.toList());
    }

    private void setPartitionNode(EpMeterReadDetailsVO epMeterReadDetailsVO) {
        if (Objects.nonNull(epMeterReadDetailsVO)) {
            List<EpPartition> list = epPartitionService.list();
            Optional<EpPartition> optional = null;
            if (CollUtil.isNotEmpty(list) && Objects.nonNull(epMeterReadDetailsVO)) {
                optional = list.stream().filter(epPartition -> epPartition.getId().equals(epMeterReadDetailsVO.getPartitionId())).findAny();
            }
            if (optional.isPresent() && Objects.isNull(optional.get().getParentId())) {
                epMeterReadDetailsVO.setPartitionNode(optional.get().getOfficeName());
            } else {
                StringBuilder sb = new StringBuilder();
                Optional<EpPartition> partitionOptional = list.stream().filter(epPartition -> epPartition.getId().equals(epMeterReadDetailsVO.getPartitionId())).findAny();
                if (partitionOptional.isPresent()) {
                    sb.insert(0, partitionOptional.get().getOfficeName());
                    partitionRecursion(list, partitionOptional.get(), sb);
                    epMeterReadDetailsVO.setPartitionNode(sb.toString());
                }
            }
        }
    }

    private List<MeterReadMapperVO> getMeterReadMapperVOList(Integer meterType, LocalDate firstDayOfMonth, LocalDate endDay) {
        List<MeterReadMapperVO> readMapperVOList = new ArrayList<>();
        List<MeterReadMapperVO> meterReadMapperVOList = this.baseMapper.selectAllByMeterType(meterType);

        Set<Integer> meterIdSet = meterReadMapperVOList.stream().map(MeterReadMapperVO::getMeterId).collect(Collectors.toSet());
        if (CollectionUtils.isEmpty(meterIdSet)) {
            return meterReadMapperVOList;
        }
        List<EpMeterRead> epMeterReadList = list(new LambdaQueryWrapper<EpMeterRead>().in(EpMeterRead::getMeterId, meterIdSet).ge(EpMeterRead::getReadDate, firstDayOfMonth).le(EpMeterRead::getReadDate, endDay));
        for (MeterReadMapperVO meterReadMapperVO : meterReadMapperVOList) {
            List<EpMeterRead> meterReadList = epMeterReadList.stream().filter(epMeterRead -> Objects.equals(meterReadMapperVO.getMeterId(), epMeterRead.getMeterId())).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(meterReadList)) {
                for (EpMeterRead epMeterRead : meterReadList) {
                    MeterReadMapperVO readMapperCopyVo = new MeterReadMapperVO();
                    BeanUtils.copyProperties(meterReadMapperVO, readMapperCopyVo);

                    readMapperCopyVo.setReadDate(epMeterRead.getReadDate());
                    readMapperCopyVo.setEndTime(epMeterRead.getEnd());
                    readMapperCopyVo.setClassId(epMeterRead.getClassId());
                    readMapperCopyVo.setAdminValue(epMeterRead.getAdminValue());
                    readMapperCopyVo.setCorrectValue(epMeterRead.getCorrectValue());
                    readMapperCopyVo.setReadValue(epMeterRead.getReadValue());
                    readMapperVOList.add(readMapperCopyVo);
                }
            } else {
                readMapperVOList.add(meterReadMapperVO);
            }

        }
        return readMapperVOList;
    }
}




