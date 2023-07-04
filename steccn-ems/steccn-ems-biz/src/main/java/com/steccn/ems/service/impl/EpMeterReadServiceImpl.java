package com.steccn.ems.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.RandomUtil;
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
import com.steccn.common.dto.TupleDTO;
import com.steccn.common.dto.ValidList;
import com.steccn.common.util.BigDecimalUtil;
import com.steccn.common.util.LocalDateUtil;
import com.steccn.common.util.MapUtil;
import com.steccn.ems.constant.EpManagerConstant;
import com.steccn.ems.domain.*;
import com.steccn.ems.dto.*;
import com.steccn.ems.mapper.EpMeterOperationRecordMapper;
import com.steccn.ems.mapper.EpMeterReadMapper;
import com.steccn.ems.service.*;
import com.steccn.ems.vo.*;
import com.steccn.platform.constant.SysOfficeConstant;
import com.steccn.platform.dto.SysOfficeTypeKeyDTO;
import com.steccn.platform.feign.RemoteOfficeFeign;
import com.steccn.platform.vo.SysOfficeVO;
import com.steccn.pms.constant.PmManagerConstant;
import com.steccn.pms.dto.DwProduceOrderDTO;
import com.steccn.pms.dto.EpMeterReadRowDTO;
import com.steccn.pms.feign.RemotePmsFeignApi;
import com.steccn.pms.feign.RemoteReportWorkFeignApi;
import com.steccn.qms.dto.QtEmsPullAmountDTO;
import com.steccn.qms.feign.RemoteCheckTaskDetailFeignApi;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
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
            for (LocalDate localDate : localDateList) {
                List<MeterReadMapperVO> filterList = entry.getValue().stream().filter(meterReadMapperVO -> Objects.equals(meterReadMapperVO.getReadDate(), localDate)).collect(Collectors.toList());
                ReadValueListVO readValueListVO = new ReadValueListVO();
                if (CollectionUtils.isNotEmpty(filterList)) {
                    if (meterReadListVO.getEntityTable().equals(EpManagerConstant.InstrumentType.REMOTE.getCode())) {
                        List<MeterReadMapperVO> collect = filterList.stream().filter(meterReadMapperVO -> Objects.nonNull(meterReadMapperVO.getEndTime())).sorted(Comparator.comparing(MeterReadMapperVO::getEndTime).reversed()).collect(Collectors.toList());
                        if (CollectionUtils.isNotEmpty(collect)) {
                            readValueListVO.setReadValue(collect.get(0).getAdminValue());
                        }
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
                if (!localDate.isEqual(firstDayOfMonth) && !localDate.isEqual(localDateList.get(localDateList.size()-1))) {
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

    /**
     * 排序
     * @param eleList
     */
    private void eleSort(List<MeterReadListVO> eleList) {
        Collections.sort(eleList, Comparator.nullsLast(Comparator.comparing(MeterReadListVO::getSort)));
        if(CollUtil.isNotEmpty(eleList)){
            for (MeterReadListVO ele : eleList) {
                if(CollUtil.isNotEmpty(ele.getMeterReadListVOList())){
                    eleSort(ele.getMeterReadListVOList());
                }
            }
        }
    }

    /**
     * 设置序号
     * @param eleList
     * @param sortMap
     */
    private void setSort(List<MeterReadListVO> eleList, Map<Long,Integer> sortMap){
        if(CollUtil.isNotEmpty(eleList) && CollUtil.isNotEmpty(sortMap)){
            for (MeterReadListVO ele : eleList) {
                Long partitionNode = ele.getPartitionNode();
                ele.setSort(sortMap.getOrDefault(partitionNode,Integer.MAX_VALUE));
                if(CollUtil.isNotEmpty(ele.getMeterReadListVOList())){
                    setSort(ele.getMeterReadListVOList(),sortMap);
                }
            }
        }
    }


    private void reduceRecursion(List<MeterReadListVO> meterReadListVOList, List<List<ReadValueListVO>> readValueAllList) {
        for (MeterReadListVO meterReadListVO : meterReadListVOList) {
            if(Objects.equals(meterReadListVO.getEntityTable(), 2)){
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
        SteccnUser user = SecurityUtils.getUser();
        epMeterOperationRecord.setReadId(epMeterRead.getId());
        epMeterOperationRecord.setOpBy(user.getUserId());
        epMeterOperationRecord.setOpName(user.getName());
        epMeterOperationRecord.setOpValueOld(dto.getReadValue());
        epMeterOperationRecord.setOpTime(LocalDateTime.now());

        epMeterOperationRecordMapper.insert(epMeterOperationRecord);

        return true;
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

        if (Objects.isNull(epMeterReadDetailsVO) && now.isEqual(date)) {
            EpEnergyType epEnergyType = epEnergyTypeService.getById(instrumentAccount.getMeterType());
            epMeterReadDetailsVO = new EpMeterReadDetailsVO();
            epMeterReadDetailsVO.setPartitionId(instrumentAccount.getPartitionNode());
            epMeterReadDetailsVO.setMeterType(instrumentAccount.getMeterType());
            epMeterReadDetailsVO.setEntityTable(instrumentAccount.getEntityTable());
            epMeterReadDetailsVO.setMeterId(instrumentAccount.getId().longValue());
            epMeterReadDetailsVO.setMeterName(instrumentAccount.getMeterName());
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
        return true;

    }

    @Override
    public List<EpMeterReadAllDataVO> classDateAllData(Set<LocalDate> dateSet, Integer energyType, List<Long> partitionIdList) {
        SteccnTableAssertUtil.notEmpty(partitionIdList, "组织机构不能为空");
        Assert.notNull(energyType, "能源类型不能为空");
        Assert.notEmpty(dateSet, "日期筛选不可以为空");
        //获取节点关联仪表
        Map<Long, EpInstrumentAccountDTO> nodeLinkMeterMap = epInstrumentAccountService.queryNodeLinkMeter(energyType);
        SteccnTableAssertUtil.notEmpty(nodeLinkMeterMap, "该能源下无仪表");
        //获取每个节点的office信息
        Map<Long, EpPartition> epPartitionMap = epPartitionService.listById(nodeLinkMeterMap.keySet());
        List<EpEnergyConsumeDTO> rowList = consumeTable(dateSet, energyType, nodeLinkMeterMap);
        //最后将全量数据表的map转换为有用的list集合 对外输出-根据需要的节点过滤
        return row2List(energyType, Sets.newHashSet(partitionIdList), nodeLinkMeterMap, epPartitionMap, rowList , Maps.newHashMap());
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
        Map<String, BigDecimal> handReadDataMap = handRead(handMeterIdSet, dateSet);

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
                                consumeMap.put(k, remoteDataMap.get(account.getId() + "_" + date + "_" + classes + "_" + thick));
                            } else if (account.getEntityTable() == 1 && noThickAndClass(classes, thick)) {
                                //表读数来自人工抄表，每天抄一次）：仅统计每日能耗（类型II）
                                consumeMap.put(k, handReadDataMap.get(account.getId() + "_" + date));
                            } else if (account.getEntityTable() == 2) {
                                //虚拟仪表
                                if (Integer.valueOf(0).equals(v.getType())) {
                                    //统计能耗考虑班次、产品厚度（类型I）
                                    BigDecimal value = BigDecimal.ZERO;
                                    for (Long child : v.getChildren()) {
                                        value = BigDecimalUtil.add(value, consumeMap.get(child));
                                    }
                                    consumeMap.put(k, value);
                                } else if (Integer.valueOf(1).equals(v.getType()) && noThickAndClass(classes, thick)) {
                                    //都是实体非远传仪表
                                    BigDecimal value = BigDecimal.ZERO;
                                    for (Long child : v.getChildren()) {
                                        value = BigDecimalUtil.add(value, consumeMap.get(child));
                                    }
                                    consumeMap.put(k, value);
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
                                    consumeMap.put(k, value);
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
                    EpInstrumentAccountDTO accountDTO = nodeLinkMeterMap.get(key);
                    resVo.setMeterId(accountDTO.getId());
                    resVo.setConsume(value);
                    resVo.setOfficeId(key);
                    EpPartition epPartition = epPartitionMap.getOrDefault(key, defaultNode);
                    resVo.setSysOfficeId(epPartition.getOfficeId());
                    resVo.setOfficeName(epPartition.getOfficeName());
                    resVo.setProductNum(yieldMap.get(key));
                    if(CollUtil.isNotEmpty(classGroupMap)){
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
     * @param meterIdSet 远传仪表id集合
     * @param dateSet    日期集合
     * @return
     */
    private Map<String, BigDecimal> handRead(Set<Integer> meterIdSet, Set<LocalDate> dateSet) {
        Map<String, BigDecimal> resMap = Maps.newHashMap();

        //获取当前日期最小日期的前一天，用于求差值
        List<LocalDate> sortedDateList = CollectionUtil.sort(dateSet, Comparator.comparing(x -> x));
        LocalDate minLocalDate = sortedDateList.get(0).minusDays(1);
        sortedDateList.add(minLocalDate);

        //查询全量数据表-手动抄表部分【需要查询前一天，涉及差值求当前值】
        if (CollUtil.isNotEmpty(meterIdSet) && CollUtil.isNotEmpty(dateSet)) {
            List<EpMeterRead> dataList = list(new LambdaQueryWrapper<EpMeterRead>()
                    .in(EpMeterRead::getMeterId, meterIdSet)
                    .in(EpMeterRead::getReadDate, sortedDateList));
            if (CollUtil.isNotEmpty(dataList)) {
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
                    preValue = BigDecimal.ZERO;
                    for (Map.Entry<LocalDate, List<EpMeterRead>> dateEntry : meterEntry.getValue().entrySet()) {
                        //手动抄表 一天一条,去取最后一条
                        BigDecimal curValue = dateEntry.getValue().get(dateEntry.getValue().size() - 1).getAdminValue();
                        if (!dateEntry.getKey().isEqual(minLocalDate)) {
                            resMap.put(meterEntry.getKey() + "_" + dateEntry.getKey(), BigDecimalUtil.subtract(curValue, preValue));
                        }
                        preValue = curValue;
                    }
                }
            }
        }
        return resMap;
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
        if(CollUtil.isNotEmpty(dateSet) && CollUtil.isNotEmpty(meterIdSet)){
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
        //获取生产明细样例表
        List<EpMeterReadDistantDTO> orderList = orderDetail(dateSet, nodeLinkMeterMap.values());

        //获取所有节点的具体信息
        Map<Long, EpPartition> partitionMap = epPartitionService.listById(nodeLinkMeterMap.keySet());
        EpPartition defaultPartition = new EpPartition();
        //获取深加工产线和基片产线
        Set<Integer> jpLineSet = getLine(PmManagerConstant.UROPATAGIA);
        Set<Integer> sjgLineSet = getLine(PmManagerConstant.DEEP_PROCESSING);
        //获取深加工产线和基片产线的每一行对应的产量
        List<QtEmsPullAmountDTO> jpDataList = Lists.newArrayList();
        List<EpMeterReadRowDTO> sjgDataList = Lists.newArrayList();
        //获取产量-区分深加工和基片车间
        Map<Long, EpMeterReadRowDTO> orderMap = Maps.newHashMap();
        //因为同一时间 一条产线上 生产订单只会存在一个
        List<EpMeterReadRowDTO> orderRowList = Lists.newArrayList();
        //查询所有节点的产线父节点
        Set<Integer> nodeOfficeIdSet = partitionMap.values().stream().map(EpPartition::getOfficeId).collect(Collectors.toSet());
        Map<Integer, Integer> parentLineMap = FeignOptional.of(() -> remoteOfficeFeign.parentLine(nodeOfficeIdSet), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();

        orderList.forEach(ele -> {
            EpMeterReadRowDTO rowDTO = new EpMeterReadRowDTO();
            rowDTO.setUuid(IdWorker.getId());
            BeanUtils.copyProperties(ele, rowDTO);
            Long nodeId = meterNodeMap.get(ele.getMeterId());
            EpPartition epPartition = partitionMap.getOrDefault(nodeId, defaultPartition);
            //获取当前节点产线父节点
            Integer lineId = parentLineMap.get(epPartition.getOfficeId());
            rowDTO.setProductLineId(lineId);
            if (sjgLineSet.contains(lineId)) {
                //深加工产线节点
                sjgDataList.add(rowDTO);
            } else if (jpLineSet.contains(lineId)) {
                //基片产线节点
                rowDTO.setProductLineId(epPartition.getOfficeId());
                jpDataList.add(convertToJpDto(rowDTO));
            }
            orderMap.put(rowDTO.getUuid(), rowDTO);
            orderRowList.add(rowDTO);
        });
        //获取基片和深加工的产量
        List<QtEmsPullAmountDTO> jpNumList = FeignOptional.of(() -> remoteCheckTaskDetailFeignApi.getEmsPullAmount(new ValidList<>(jpDataList)), FeignConstant.STECCN_QMS_SERVICE_NAME).get();
        jpNumList.forEach(ele -> {
            EpMeterReadRowDTO jpRowDTO = orderMap.getOrDefault(ele.getUuid(), new EpMeterReadRowDTO());
            jpRowDTO.setYield(ele.getPullAmount());
            //test11
//            jpRowDTO.setYield(BigDecimalUtil.setScale(RandomUtil.randomBigDecimal(new BigDecimal("10"), new BigDecimal("100")), 2));
        });
        List<EpMeterReadRowDTO> sjgNumList = FeignOptional.of(() -> remoteReportWorkFeignApi.sjgOrderRw(sjgDataList), FeignConstant.STECCN_PMS_SERVICE_NAME).get();
        sjgNumList.forEach(ele -> {
            EpMeterReadRowDTO sjgRowDTO = orderMap.getOrDefault(ele.getUuid(), new EpMeterReadRowDTO());
            sjgRowDTO.setYield(ele.getYield());
            //test11
//            sjgRowDTO.setYield(BigDecimalUtil.setScale(RandomUtil.randomBigDecimal(new BigDecimal("10"), new BigDecimal("100")), 2));
        });

        //查询虚拟仪表的孩子是什么仪表，返回map key => 虚拟仪表节点，0全部是远传 1是机械+远传或者全是机械  null 是没有孩子仪表
        Map<Long, EpChildTypeDTO> childTypeMap = epPartitionService.childTypeMap(energyType);
        childTypeMap.forEach((k, v) -> v.setAccount(nodeLinkMeterMap.get(k)));

        //考虑性能优化
        Map<Long, List<EpPartitionVO>> allChildren = epPartitionService.getAllChildren(nodeLinkMeterMap.keySet());

        //获取所有产线的父节点-不包含父节点
        Map<Long, Set<Long>> lineParentMap = getLineParentMap(partitionMap.values(), jpLineSet, sjgLineSet);
        EpPartition root = root(partitionMap.values());
        //根节点为空，则直接返回
        if(Objects.isNull(root)){
            return Lists.newArrayList();
        }
        //获取父节点的产线孩子节点-不含有根节点
        Map<Long, Set<Long>> lineChildrenMap = getLineChildrenMap(lineParentMap);
        lineChildrenMap.remove(root.getId());

        //保存所有的unitMap
        Map<String, Map<Long, BigDecimal>> unitMapCol = Maps.newHashMap();
        //构建产量的全量数据表
        for (EpEnergyConsumeDTO consumeDTO : consumeTable) {
            //构建单耗map
            Map<Long, BigDecimal> unitMap = Maps.newLinkedHashMap();
            consumeDTO.setYieldMap(unitMap);
            unitMapCol.put(consumeDTO.getReadDate() + "_" + consumeDTO.getClassId() + "_" + consumeDTO.getThick(), unitMap);
            //遍历应该考虑从下向上遍历--参考能耗全量数据表
            childTypeMap.forEach((k, v) -> {
                EpInstrumentAccountDTO account = v.getAccount();
                //保证当前节点
                if (Objects.nonNull(account) && Objects.nonNull(account.getEntityTable())) {
                    EpPartition epPartition = partitionMap.getOrDefault(account.getPartitionNode(), defaultPartition);
                    if (!noThickAndClass(consumeDTO.getClassId(), consumeDTO.getThick()) && (sjgLineSet.contains(epPartition.getOfficeId()) || jpLineSet.contains(epPartition.getOfficeId()))) {
                        //若空格所在列的节点是产线节点L或L的下级节点，则查询条件为“产线 = L”
                        //过滤出相同的班次日期，相同班次，相同厚度的订单
                        List<BigDecimal> yieldSumList = orderRowList.stream().filter(ele -> ele.getReadDate().equals(consumeDTO.getReadDate()) &&
                                Objects.equals(ele.getClassId(), consumeDTO.getClassId()) &&
                                Objects.equals(ele.getThick(), consumeDTO.getThick())).filter(ele -> epPartition.getOfficeId().equals(ele.getProductLineId())).map(ele -> ele.getYield()).filter(ele -> Objects.nonNull(ele)).collect(Collectors.toList());
                        BigDecimal yieldSum = BigDecimalUtil.sum(yieldSumList);
                        unitMap.put(epPartition.getId(), yieldSum);
                        //获取当前节点的所有孩子节点
                        Set<Long> childrenIdSet = allChildren.getOrDefault(epPartition.getId(), Lists.newArrayList()).stream().map(EpPartitionVO::getId).collect(Collectors.toSet());
                        childrenIdSet.forEach(ele -> unitMap.put(ele, BigDecimalUtil.add(unitMap.get(ele), yieldSum)));
                    } else if (noThickAndClass(consumeDTO.getClassId(), consumeDTO.getThick()) && lineChildrenMap.containsKey(account.getPartitionNode())) {
                        //若空格所在列的节点是产线节点的上级节点，则查询条件为“产线 = 该上级节点以下所有产线节点”
                        Set<Long> lineIdSet = lineChildrenMap.get(account.getPartitionNode());
                        Set<Integer> officeIdSet = Sets.newHashSet();
                        lineIdSet.forEach(ele -> {
                            officeIdSet.add(partitionMap.get(ele).getOfficeId());
                        });
                        List<BigDecimal> workShopYieldSumList = orderRowList.stream().filter(ele -> officeIdSet.contains(ele.getProductLineId())).map(ele -> ele.getYield()).filter(ele -> Objects.nonNull(ele)).collect(Collectors.toList());
                        BigDecimal workShopYieldSum = BigDecimalUtil.sum(workShopYieldSumList);
                        unitMap.put(epPartition.getId(), workShopYieldSum);
                    } else if (noThickAndClass(consumeDTO.getClassId(), consumeDTO.getThick()) && !unitMap.containsKey(epPartition.getId())) {
                        //对于班次、产品厚度均为“无”的行，值为“-”的单元格K填入日产量t：
                        BigDecimal tempSum = null;
                        for (Map.Entry<String, Map<Long, BigDecimal>> entry : unitMapCol.entrySet()) {
                            tempSum = BigDecimalUtil.add(tempSum, entry.getValue().get(epPartition.getId()));
                        }
                        unitMap.put(epPartition.getId(), tempSum);
                    }
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
    private QtEmsPullAmountDTO convertToJpDto(EpMeterReadRowDTO rowDTO) {
        QtEmsPullAmountDTO dto = new QtEmsPullAmountDTO();
        BeanUtils.copyProperties(rowDTO, dto);
        dto.setClassDate(rowDTO.getReadDate());
        dto.setClassId(rowDTO.getClassId());
        dto.setStartTime(rowDTO.getStart());
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
        Map<Long, EpInstrumentAccountDTO> gasOilNodeLinkMeterMap = epInstrumentAccountService.queryNodeLinkMeter(gasOilEnergyType);
        List<EpEnergyConsumeDTO> unitTable = unitTable(dateSet, gasOilEnergyType, gasOilNodeLinkMeterMap);

        Map<Long, EpPartition> gasOilPartitionMap = epPartitionService.listById(gasOilNodeLinkMeterMap.keySet());
        Map<Long, Integer> gasOilPoMap = Maps.newHashMap();
        gasOilPartitionMap.forEach((k, v) -> {
            gasOilPoMap.put(k, v.getOfficeId());
        });

        //任意单元格K的值= (天然气能耗 * 7800 + 燃料油能耗 * 1000 * 9300) / (拉引量 * 1000)
        for (EpEnergyConsumeDTO ele : unitTable) {
            Map<Long, BigDecimal> consumeMap = ele.getConsumeMap();
            Map<Long, BigDecimal> yieldMap = ele.getYieldMap();
            //注意单耗和能耗都是依赖ep_partition中的主键，需要转换成officeId来计算
            EpEnergyConsumeDTO oilConsumeDTO = oilRowMap.getOrDefault(ele.getReadDate() + "_" + ele.getClassId() + "_" + ele.getThick(), defaultConsumeDTO);
            Map<Long, BigDecimal> oilConsumeMap = oilConsumeDTO.getConsumeMap();
            EpEnergyConsumeDTO gasConsumeDTO = gasRowMap.getOrDefault(ele.getReadDate() + "_" + ele.getClassId() + "_" + ele.getThick(), defaultConsumeDTO);
            Map<Long, BigDecimal> gasConsumeMap = gasConsumeDTO.getConsumeMap();

            for (Map.Entry<Long, BigDecimal> entry : consumeMap.entrySet()) {
                BigDecimal yield = yieldMap.get(entry.getKey());
                BigDecimal yieldMu = BigDecimalUtil.multiply(yield, new BigDecimal("1000"));
                yieldMap.put(entry.getKey(), yieldMu);
                Integer gasOilOfficeId = gasOilPoMap.get(entry.getKey());
                Long gasId = gasOpMap.get(gasOilOfficeId);
                Long oilId = oilOpMap.get(gasOilOfficeId);
                BigDecimal gasValue = gasConsumeMap.get(gasId);
                BigDecimal oilValue = oilConsumeMap.get(oilId);
                BigDecimal gasMu = BigDecimalUtil.multiply(gasValue, new BigDecimal("7800"));
                BigDecimal oilMu = BigDecimalUtil.multiply(oilValue, new BigDecimal("9300"));
                entry.setValue(BigDecimalUtil.add(gasMu, oilMu));
            }
        }
        return row2List(gasOilEnergyType, Sets.newHashSet(partitionIdList), gasOilNodeLinkMeterMap, gasOilPartitionMap, unitTable , Maps.newHashMap());
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




