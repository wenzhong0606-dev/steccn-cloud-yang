package com.steccn.ems.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.lang.Assert;
import com.alibaba.nacos.common.utils.CollectionUtils;
import com.alibaba.nacos.common.utils.StringUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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
import com.steccn.common.util.BigDecimalUtil;
import com.steccn.common.util.LocalDateUtil;
import com.steccn.common.util.MapUtil;
import com.steccn.ems.constant.EpManagerConstant;
import com.steccn.ems.domain.*;
import com.steccn.ems.dto.EpEnergyParamDTO;
import com.steccn.ems.dto.EpEnergyTreeDTO;
import com.steccn.ems.dto.EpMeterReadAddDTO;
import com.steccn.ems.dto.EpMeterReadUpdateDTO;
import com.steccn.ems.mapper.EpMeterOperationRecordMapper;
import com.steccn.ems.mapper.EpMeterReadMapper;
import com.steccn.ems.service.*;
import com.steccn.ems.vo.*;
import com.steccn.pms.feign.RemoteReportWorkFeignApi;
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
    private EpComputeProduceDetailService epComputeProduceDetailService;
    @Autowired
    private RemoteReportWorkFeignApi remoteReportWorkFeignApi;

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
            meterReadListVO.setMeterName("null".equals(keys[2])? null : keys[2]);
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
                readValueListVO.setReadDate(localDate);
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
                    if (Objects.equals(meterReadListVO.getEntityTable(),2)) {
                        for (int i = 0; i < meterReadListVO.getReadValueList().size(); i++) {
                            int finalI = i;
//                            BigDecimal reduce = meterReadListVO.getMeterReadListVOList().stream().filter(meterReadList -> Objects.nonNull(meterReadList.getReadValueList().get(finalI).getReadValue())).map(meterReadList -> meterReadList.getReadValueList().get(finalI).getReadValue()).reduce(BigDecimal.ZERO, BigDecimal::add);
//                            meterReadListVO.getReadValueList().get(i).setReadValue(reduce);
                            List<List<ReadValueListVO>> readValueAllList = new ArrayList<>();
                            reduceRecursion(meterReadListVO.getMeterReadListVOList(),readValueAllList);
                            BigDecimal sumReduce = readValueAllList.stream().filter(v -> Objects.nonNull(v.get(finalI).getReadValue()) && !v.get(finalI).getEntityTable().equals(2)).map(v -> v.get(finalI).getReadValue()).reduce(BigDecimal.ZERO, BigDecimal::add);
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
        eqMeterReadVO.setLabelList(localDateList);
        eqMeterReadVO.setMeterReadListVOList(meterReadListVOList1);
        return eqMeterReadVO;
    }

    private void reduceRecursion(List<MeterReadListVO> meterReadListVOList, List<List<ReadValueListVO>> readValueAllList) {
        for (MeterReadListVO meterReadListVO : meterReadListVOList) {
            reduceRecursion(meterReadListVO.getMeterReadListVOList(),readValueAllList);
            if (!Objects.equals(meterReadListVO.getEntityTable(),2)) {
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
                if (optional.get().getAdminFlag().equals(0)){
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
    public List<EpMeterReadAllDataVO> classDateAllData(Set<LocalDate> dateList, Integer energyType, List<Long> partitionIdList) {
        SteccnTableAssertUtil.notEmpty(partitionIdList, "组织机构不能为空");
        Assert.notNull(energyType, "能源类型不能为空");
        List<EpMeterReadAllDataVO> gpDataList = Lists.newArrayList();

        //根据选中的组织机构查询出树
        EpEnergyParamDTO energyParamDTO = new EpEnergyParamDTO();
        energyParamDTO.setPartitionId(partitionIdList);
        energyParamDTO.setType(energyType);
        long l = System.currentTimeMillis();
        EpEnergyTreeDTO childTree = epPartitionService.getChildTree(energyParamDTO);
        log.info("epPartitionService.getChildTree -> 执行时间{}");
        SteccnTableAssertUtil.notEmpty(childTree.getPartitionIds(), "组织机构中没有实体仪表");
        //查询全量数据表
        List<EpInstrumentAccount> instrumentAccountList = epInstrumentAccountService.list(new LambdaQueryWrapper<EpInstrumentAccount>()
                .eq(EpInstrumentAccount::getMeterType, energyType)
                .in(EpInstrumentAccount::getPartitionNode, childTree.getPartitionIds())
                .select(EpInstrumentAccount::getId, EpInstrumentAccount::getPartitionNode));
        SteccnTableAssertUtil.notEmpty(instrumentAccountList, "组织机构暂未关联仪表");
        //获取仪表id集合
        Map<Integer, Long> meterPartitionMap = Maps.newHashMap();
        instrumentAccountList.forEach(ele -> {
            meterPartitionMap.put(ele.getId(), ele.getPartitionNode());
        });

        if (CollUtil.isNotEmpty(dateList) && CollUtil.isNotEmpty(meterPartitionMap)) {
            //获取集合最小的日期
            List<LocalDate> sortedDateList = CollectionUtil.sort(dateList, Comparator.comparing(x -> x));
            LocalDate minLocalDate = sortedDateList.get(0).minusDays(1);
            sortedDateList.add(minLocalDate);
            //根据条件获取数据
            List<EpMeterRead> dataList = list(new LambdaQueryWrapper<EpMeterRead>()
                    .in(EpMeterRead::getMeterId, meterPartitionMap.keySet())
                    .in(EpMeterRead::getReadDate, sortedDateList));
            if (CollUtil.isNotEmpty(dataList)) {
                //排序
                dataList.sort((o1, o2) -> {
                    if(Objects.nonNull(o1.getStart()) && Objects.nonNull(o2.getStart())){
                        return o1.getStart().compareTo(o2.getStart());
                    }
                    if(Objects.isNull(o1.getStart())){
                        return 1;
                    }
                    return -1;
                });

                //存放仪表的班次日期的抄表数据
                Map<Integer, Map<LocalDate, List<EpMeterRead>>> map = new LinkedHashMap<>();
                //将数据按照班次日期分组
                dataList.forEach(ele -> {
                    // 2层map的插入，k1和k2相同的元素，存放到集合中
                    MapUtil.biCompute(map, ele.getMeterId(), ele.getReadDate(), ele);
                });
                //遍历仪表
                List<EpMeterReadAllDataVO> resList = calDiffValue(meterPartitionMap, minLocalDate, map);

                //对班次和产品厚度进行分组累计
                Map<Integer, Map<LocalDate, List<EpMeterReadAllDataVO>>> calMap = Maps.newHashMap();
                Map<LocalDate, List<EpMeterReadAllDataVO>> classDateDataMap = resList.stream().collect(Collectors.groupingBy(EpMeterReadAllDataVO::getReadDate));
                for (Map.Entry<LocalDate, List<EpMeterReadAllDataVO>> classDateEntry : classDateDataMap.entrySet()) {
                    Map<Integer, List<EpMeterReadAllDataVO>> meterDataMap = classDateEntry.getValue().stream().collect(Collectors.groupingBy(EpMeterReadAllDataVO::getMeterId));
                    for (Map.Entry<Integer, List<EpMeterReadAllDataVO>> meterEntry : meterDataMap.entrySet()) {
                        Map<String, List<EpMeterReadAllDataVO>> thickDataMap = meterEntry.getValue().stream().collect(Collectors.groupingBy(v -> v.getClassId() + "_" + v.getThick()));
                        for (Map.Entry<String, List<EpMeterReadAllDataVO>> thickEntry : thickDataMap.entrySet()) {
                            EpMeterReadAllDataVO epMeterReadAllDataVO = thickEntry.getValue().get(0);
                            EpMeterReadAllDataVO ele = new EpMeterReadAllDataVO();
                            BigDecimal reduce = thickEntry.getValue().stream().filter(v -> Objects.nonNull(v.getConsume())).map(v -> v.getConsume()).reduce(BigDecimal.ZERO, BigDecimal::add);
                            BeanUtils.copyProperties(epMeterReadAllDataVO, ele);
                            ele.setConsume(reduce);
                            gpDataList.add(ele);

                            List<Long> reportWorkIdList = Lists.newArrayList();
                            thickEntry.getValue().forEach(te -> {
                                if (CollUtil.isNotEmpty(te.getReportWorkIdList())) {
                                    reportWorkIdList.addAll(te.getReportWorkIdList());
                                }
                            });
                            ele.setReportWorkIdList(reportWorkIdList);

                            MapUtil.biCompute(calMap, ele.getMeterId(), ele.getReadDate(), ele);
                        }
                    }
                }
                //构造整个虚拟节点
                List<LocalDate> originalDateList = CollectionUtil.sort(dateList, Comparator.comparing(x -> x));
                Set<String> existDataSet = Sets.newHashSet();
                for (LocalDate classDate : originalDateList) {
                    for (EpPartitionVO ep : childTree.getTree()) {
                        cycle(calMap, classDate, ep, gpDataList, existDataSet);
                    }
                }
            }
        }

        Set<Long> partitionIdSet = gpDataList.stream().map(EpMeterReadAllDataVO::getOfficeId).collect(Collectors.toSet());
        Map<Long, EpPartition> epPartitionMap = epPartitionService.listById(partitionIdSet);

        //将全量数据表的厚度为空的都设置成-1
        gpDataList.forEach(ele -> {
            if(Objects.isNull(ele.getThick())){
                ele.setThick(new BigDecimal("-1"));
            }
            //设置组织机构id
            EpPartition epPartition = epPartitionMap.get(ele.getOfficeId());
            if(Objects.nonNull(epPartition)){
                ele.setSysOfficeId(epPartition.getOfficeId());
            }
            ele.setEnergyType(energyType);
        });
        return gpDataList;
    }

    @Override
    public List<EpMeterReadAllDataVO> classDateAllDataUnit(Set<LocalDate> dateList, Integer energyType, List<Long> partitionIdList) {
        SteccnTableAssertUtil.notEmpty(partitionIdList, "组织机构不能为空");
        Assert.notNull(energyType, "能源类型不能为空");
        List<EpMeterReadAllDataVO> gpDataList = Lists.newArrayList();

        //根据选中的组织机构查询出树
        EpEnergyParamDTO energyParamDTO = new EpEnergyParamDTO();
        energyParamDTO.setPartitionId(partitionIdList);
        energyParamDTO.setType(energyType);
        EpEnergyTreeDTO childTree = epPartitionService.getChildTree(energyParamDTO);
        SteccnTableAssertUtil.notEmpty(childTree.getPartitionIds(), "组织机构中没有实体仪表");

        //查询全量数据表
        List<EpInstrumentAccount> instrumentAccountList = epInstrumentAccountService.list(new LambdaQueryWrapper<EpInstrumentAccount>()
                .eq(EpInstrumentAccount::getMeterType, energyType)
                .in(EpInstrumentAccount::getPartitionNode, childTree.getPartitionIds())
                .select(EpInstrumentAccount::getId, EpInstrumentAccount::getPartitionNode));
        SteccnTableAssertUtil.notEmpty(instrumentAccountList, "组织机构暂未关联仪表");
        //获取仪表id集合
        Map<Integer, Long> meterPartitionMap = Maps.newHashMap();
        instrumentAccountList.forEach(ele -> {
            meterPartitionMap.put(ele.getId(), ele.getPartitionNode());
        });

        if (CollUtil.isNotEmpty(dateList) && CollUtil.isNotEmpty(meterPartitionMap)) {
            //获取集合最小的日期
            List<LocalDate> sortedDateList = CollectionUtil.sort(dateList, Comparator.comparing(x -> x));
            LocalDate minLocalDate = sortedDateList.get(0).minusDays(1);
            sortedDateList.add(0 , minLocalDate);

            //获取所有的产量信息
            List<EpProduceDetailVO> produceDetailList = epComputeProduceDetailService.getProduceDetailList(minLocalDate, sortedDateList.get(sortedDateList.size() - 1));

            Set<Integer> officeIdSet = produceDetailList.stream().map(v -> v.getOfficeId()).collect(Collectors.toSet());


            //根据组织机构id和能源类型查询分区节点
            Map<Integer, EpInstrumentAccount> epPartitionMap = epPartitionService.listByOfficeId(energyType, officeIdSet);
            EpInstrumentAccount defaultEpInstrumentAccount = new EpInstrumentAccount();

            //根据条件获取数据
            List<EpMeterRead> dataList = list(new LambdaQueryWrapper<EpMeterRead>()
                    .in(EpMeterRead::getMeterId, meterPartitionMap.keySet())
                    .in(EpMeterRead::getReadDate, sortedDateList)
                    .orderByAsc(Lists.newArrayList(EpMeterRead::getReadDate, EpMeterRead::getStart)));
            if (CollUtil.isNotEmpty(dataList)) {

                //获取报工集合
                Set<Long> reportWorkIdSet = Sets.newHashSet();
                dataList.forEach(ep ->{
                    if(Objects.nonNull(ep.getReportWorkId())){
                        reportWorkIdSet.add(ep.getReportWorkId());
                    }
                });
                Map<Long, Long> reportMap = FeignOptional.of(() -> remoteReportWorkFeignApi.reportWorkClassGroup(reportWorkIdSet), FeignConstant.STECCN_PMS_SERVICE_NAME).get();

                //存放仪表的班次日期的抄表数据
                Map<Integer, Map<LocalDate, List<EpMeterRead>>> map = new LinkedHashMap<>();
                //将数据按照仪表id和班次日期分组
                dataList.forEach(ele -> {
                    // 2层map的插入，k1和k2相同的元素，存放到集合中
                    MapUtil.biCompute(map, ele.getMeterId(), ele.getReadDate(), ele);
                });
                List<EpMeterReadAllDataVO> resList =  calDiffValue(meterPartitionMap, minLocalDate, map);

                //对班次和产品厚度进行分组累计
                Map<Integer, Map<LocalDate, List<EpMeterReadAllDataVO>>> calMap = Maps.newHashMap();
                Map<LocalDate, List<EpMeterReadAllDataVO>> classDateDataMap = resList.stream().collect(Collectors.groupingBy(EpMeterReadAllDataVO::getReadDate));

                for (Map.Entry<LocalDate, List<EpMeterReadAllDataVO>> classDateEntry : classDateDataMap.entrySet()) {
                    Map<Integer, List<EpMeterReadAllDataVO>> meterDataMap = classDateEntry.getValue().stream().collect(Collectors.groupingBy(EpMeterReadAllDataVO::getMeterId));
                    for (Map.Entry<Integer, List<EpMeterReadAllDataVO>> meterEntry : meterDataMap.entrySet()) {
                        Map<String, List<EpMeterReadAllDataVO>> thickDataMap = meterEntry.getValue().stream().collect(Collectors.groupingBy(v -> v.getClassId() + "_" + v.getThick()));
                        for (Map.Entry<String, List<EpMeterReadAllDataVO>> thickEntry : thickDataMap.entrySet()) {
                            EpMeterReadAllDataVO epMeterReadAllDataVO = thickEntry.getValue().get(0);
                            EpMeterReadAllDataVO ele = new EpMeterReadAllDataVO();
                            BigDecimal reduce = thickEntry.getValue().stream().filter(v -> Objects.nonNull(v.getConsume())).map(v -> v.getConsume()).reduce(BigDecimal.ZERO, BigDecimal::add);
                            BeanUtils.copyProperties(epMeterReadAllDataVO, ele);
                            //产量
                            List<EpProduceDetailVO> productNumObjectList = filterProductNumList(produceDetailList, epPartitionMap, defaultEpInstrumentAccount, classDateEntry, meterEntry, epMeterReadAllDataVO);
                            if (CollUtil.isNotEmpty(productNumObjectList)) {
                                BigDecimal yield = productNumObjectList.stream().filter(v -> Objects.nonNull(v.getYield())).map(v -> v.getYield()).reduce(BigDecimal.ZERO, BigDecimal::add);
                                ele.setProductNum(yield);
                            }

                            List<Long> reportWorkIdList = Lists.newArrayList();
                            thickEntry.getValue().forEach(te -> {
                                if (CollUtil.isNotEmpty(te.getReportWorkIdList())) {
                                    reportWorkIdList.addAll(te.getReportWorkIdList());
                                }
                            });
                            ele.setReportWorkIdList(reportWorkIdList);
                            Long classGroupId = calClassGroupId(ele,reportMap);
                            ele.setClassGroupId(classGroupId);
                            ele.setConsume(reduce);
                            gpDataList.add(ele);
                            MapUtil.biCompute(calMap, ele.getMeterId(), ele.getReadDate(), ele);
                        }
                    }
                }
                //构造整个虚拟节点
                List<LocalDate> originalDateList = CollectionUtil.sort(dateList, Comparator.comparing(x -> x));
                Set<String> existDataSet = Sets.newHashSet();
                for (LocalDate classDate : originalDateList) {
                    for (EpPartitionVO ep : childTree.getTree()) {
                        cycle(calMap, classDate, ep, gpDataList, existDataSet);
                    }
                }
            }
        }

        //将全量数据表的厚度为空的都设置成-1
        gpDataList.forEach(ele -> {
            if(Objects.isNull(ele.getThick())){
                ele.setThick(new BigDecimal("-1"));
            }
            ele.setEnergyType(energyType);
        });

        return gpDataList;
    }

    /**
     * 过滤产量
     * @param produceDetailList
     * @param epPartitionMap
     * @param defaultEpInstrumentAccount
     * @param classDateEntry
     * @param meterEntry
     * @param epMeterReadAllDataVO
     * @return
     */
    private List<EpProduceDetailVO> filterProductNumList(List<EpProduceDetailVO> produceDetailList, Map<Integer, EpInstrumentAccount> epPartitionMap, EpInstrumentAccount defaultEpInstrumentAccount, Map.Entry<LocalDate, List<EpMeterReadAllDataVO>> classDateEntry, Map.Entry<Integer, List<EpMeterReadAllDataVO>> meterEntry, EpMeterReadAllDataVO epMeterReadAllDataVO) {
        List<EpProduceDetailVO> productNumObjectList = produceDetailList.stream().filter(v -> {
            boolean flag1, flag2, flag3;
            //校验厚度
            if (Objects.nonNull(v.getThickQuantity()) && Objects.nonNull(epMeterReadAllDataVO.getThick())) {
                flag1 = v.getThickQuantity().equals(epMeterReadAllDataVO.getThick());
            } else if (Objects.isNull(v.getThickQuantity()) && Objects.isNull(epMeterReadAllDataVO.getThick())) {
                flag1 = Boolean.TRUE;
            } else {
                flag1 = Boolean.FALSE;
            }
            //校验班次日期
            if (Objects.nonNull(v.getClassDate()) && Objects.nonNull(classDateEntry.getKey())) {
                flag2 = v.getClassDate().equals(classDateEntry.getKey());
            } else if (Objects.isNull(v.getClassDate()) && Objects.isNull(classDateEntry.getKey())) {
                flag2 = Boolean.TRUE;
            } else {
                flag2 = Boolean.FALSE;
            }
            //校验班次id
            if (Objects.nonNull(v.getClassId()) && Objects.nonNull(epMeterReadAllDataVO.getClassId())) {
                flag3 = v.getClassId().equals(epMeterReadAllDataVO.getClassId());
            } else if (Objects.isNull(v.getClassId()) && Objects.isNull(epMeterReadAllDataVO.getClassId())) {
                flag3 = Boolean.TRUE;
            } else {
                flag3 = Boolean.FALSE;
            }

            return flag1 && flag2 && flag3 && meterEntry.getKey().equals(epPartitionMap.getOrDefault(v.getOfficeId(), defaultEpInstrumentAccount).getId());
        }).collect(Collectors.toList());
        return productNumObjectList;
    }

    @Override
    public List<EpMeterReadAllDataVO> kcalDataUnit(Set<LocalDate> dateSet, List<Long> partitionIdList) {
        SteccnTableAssertUtil.notEmpty(partitionIdList, "组织机构不能为空");
        //将组织机构分成3和4类型
        EpEnergyTreeDTO childTree1 = energyTree(partitionIdList, 3);
        EpEnergyTreeDTO childTree2 = energyTree(partitionIdList, 4);
        Set<Long> childTree = Sets.newHashSet();
        List<EpPartitionVO> childrenList = Lists.newArrayList();
        if (CollUtil.isNotEmpty(childTree1.getPartitionIds())) {
            childTree.addAll(childTree1.getPartitionIds());
            childrenList.addAll(childTree1.getTree());
        }
        if (CollUtil.isNotEmpty(childTree2.getPartitionIds())) {
            childTree.addAll(childTree2.getPartitionIds());
            childrenList.addAll(childTree1.getTree());
        }
        //查询全量数据表
        List<EpInstrumentAccount> instrumentAccountList = epInstrumentAccountService.list(new LambdaQueryWrapper<EpInstrumentAccount>()
                .in(EpInstrumentAccount::getMeterType, Arrays.asList(3, 4))
                .in(CollUtil.isNotEmpty(childTree), EpInstrumentAccount::getPartitionNode, childTree)
                .select(EpInstrumentAccount::getId, EpInstrumentAccount::getPartitionNode, EpInstrumentAccount::getMeterType));
        SteccnTableAssertUtil.notEmpty(instrumentAccountList, "组织机构暂未关联仪表");
        //获取仪表id集合
        Map<Integer, Long> meterPartitionMap = Maps.newHashMap();
        instrumentAccountList.forEach(ele -> meterPartitionMap.put(ele.getId(), ele.getPartitionNode()));

        //对班次和产品厚度进行分组累计
        Map<Integer, Map<LocalDate, List<EpMeterReadAllDataVO>>> calMap = Maps.newHashMap();

        List<EpMeterReadAllDataVO> gpDataList = Lists.newArrayList();
        if (CollUtil.isNotEmpty(dateSet)) {
            //定义数据总集合
            List<EpMeterReadAllDataVO> kcalDataUnitList = Lists.newArrayList();
            //获取天然气和燃料油得全量数据表
            List<EpMeterReadAllDataVO> gasList = classDateAllData(dateSet, 3, partitionIdList);
            List<EpMeterReadAllDataVO> oilList = classDateAllData(dateSet, 4, partitionIdList);
            kcalDataUnitList.addAll(gasList);
            kcalDataUnitList.addAll(oilList);
            //获取集合最小的日期
            List<LocalDate> sortedDateList = CollectionUtil.sort(dateSet, Comparator.comparing(x -> x));
            LocalDate minLocalDate = sortedDateList.get(0).minusDays(1);
            sortedDateList.add(0, minLocalDate);
            //获取所有的产量信息
            List<EpProduceDetailVO> produceNumList = epComputeProduceDetailService.getProduceDetailList(minLocalDate, sortedDateList.get(sortedDateList.size() - 1));

            //根据组织机构id和能源类型查询分区节点
            //根据条件获取数据
            List<EpMeterRead> dataList = list(new LambdaQueryWrapper<EpMeterRead>()
                    .in(EpMeterRead::getMeterId, meterPartitionMap.keySet())
                    .in(EpMeterRead::getReadDate, sortedDateList)
                    .orderByAsc(Lists.newArrayList(EpMeterRead::getReadDate, EpMeterRead::getStart)));
            //获取报工集合
            Set<Long> reportWorkIdSet = Sets.newHashSet();
            dataList.forEach(ep -> {
                if (Objects.nonNull(ep.getReportWorkId())) {
                    reportWorkIdSet.add(ep.getReportWorkId());
                }
            });
            Map<Long, Long> reportMap = FeignOptional.of(() -> remoteReportWorkFeignApi.reportWorkClassGroup(reportWorkIdSet), FeignConstant.STECCN_PMS_SERVICE_NAME).get();

            //根据班次日期
            Map<LocalDate, List<EpMeterReadAllDataVO>> classDateMap = kcalDataUnitList.stream().collect(Collectors.groupingBy(EpMeterReadAllDataVO::getReadDate));
            for (Map.Entry<LocalDate, List<EpMeterReadAllDataVO>> classDateEntry : classDateMap.entrySet()) {
                //根据组织机构分组-会把同一个组织机构对应的数据分到同一组
                Map<Integer, List<EpMeterReadAllDataVO>> meterMap = classDateEntry.getValue().stream().collect(Collectors.groupingBy(EpMeterReadAllDataVO::getSysOfficeId));
                for (Map.Entry<Integer, List<EpMeterReadAllDataVO>> officeEntry : meterMap.entrySet()) {
                    Map<String, List<EpMeterReadAllDataVO>> thickMap = officeEntry.getValue().stream().collect(Collectors.groupingBy(v -> v.getClassId() + "_" + v.getThick()));
                    for (Map.Entry<String, List<EpMeterReadAllDataVO>> thickEntry : thickMap.entrySet()) {
                        EpMeterReadAllDataVO epMeterReadAllDataVO = thickEntry.getValue().get(0);
                        EpMeterReadAllDataVO ele = new EpMeterReadAllDataVO();

                        //天然气
                        BigDecimal gasBd = thickEntry.getValue().stream().filter(v -> 3 == v.getEnergyType() && Objects.nonNull(v.getConsume())).map(v -> v.getConsume()).reduce(BigDecimal.ZERO, BigDecimal::add);
                        //燃料油
                        BigDecimal oilBd = thickEntry.getValue().stream().filter(v -> 4 == v.getEnergyType() && Objects.nonNull(v.getConsume())).map(v -> v.getConsume()).reduce(BigDecimal.ZERO, BigDecimal::add);
                        BigDecimal gasMu = BigDecimalUtil.multiply(gasBd, BigDecimal.valueOf(7788));
                        BigDecimal oilMu = BigDecimalUtil.multiply(oilBd, BigDecimal.valueOf(9300000));

                        BeanUtils.copyProperties(epMeterReadAllDataVO, ele);
                        //产量
                        List<EpProduceDetailVO> productNumObjectList = filterProductNumList(produceNumList, classDateEntry.getKey(), officeEntry.getKey(), epMeterReadAllDataVO);
                        if (CollUtil.isNotEmpty(productNumObjectList)) {
                            BigDecimal yield = productNumObjectList.stream().filter(v -> Objects.nonNull(v.getYield())).map(v -> v.getYield()).reduce(BigDecimal.ZERO, BigDecimal::add);
                            ele.setProductNum(BigDecimalUtil.multiply(yield, BigDecimal.valueOf(1000)));
                        }
                        List<Long> reportWorkIdList = Lists.newArrayList();
                        thickEntry.getValue().forEach(te -> {
                            if (CollUtil.isNotEmpty(te.getReportWorkIdList())) {
                                reportWorkIdList.addAll(te.getReportWorkIdList());
                            }
                        });
                        ele.setReportWorkIdList(reportWorkIdList);
                        Long classGroupId = calClassGroupId(ele, reportMap);
                        ele.setClassGroupId(classGroupId);
                        ele.setConsume(BigDecimalUtil.add(gasMu, oilMu));
                        gpDataList.add(ele);
                        MapUtil.biCompute(calMap, ele.getMeterId(), ele.getReadDate(), ele);
                    }
                }
                //构造整个虚拟节点
                List<LocalDate> originalDateList = CollectionUtil.sort(dateSet, Comparator.comparing(x -> x));
                Set<String> existDataSet = Sets.newHashSet();
                for (LocalDate classDate : originalDateList) {
                    for (EpPartitionVO ep : childrenList) {
                        cycle(calMap, classDate, ep, gpDataList, existDataSet);
                    }
                }
            }
        }
        return gpDataList;
    }

    /**
     * 过滤产量
     * @param produceDetailList
     * @param officeId
     * @param epMeterReadAllDataVO
     * @return
     */
    private List<EpProduceDetailVO> filterProductNumList(List<EpProduceDetailVO> produceDetailList, LocalDate classDate, Integer officeId, EpMeterReadAllDataVO epMeterReadAllDataVO) {
        List<EpProduceDetailVO> productNumObjectList = produceDetailList.stream().filter(v -> {
            boolean flag1, flag2, flag3;
            //校验厚度
            if (Objects.nonNull(v.getThickQuantity()) && Objects.nonNull(epMeterReadAllDataVO.getThick())) {
                flag1 = v.getThickQuantity().equals(epMeterReadAllDataVO.getThick());
            } else if (Objects.isNull(v.getThickQuantity()) && Objects.isNull(epMeterReadAllDataVO.getThick())) {
                flag1 = Boolean.TRUE;
            } else {
                flag1 = Boolean.FALSE;
            }
            //校验班次日期
            if (Objects.nonNull(v.getClassDate()) && Objects.nonNull(classDate)) {
                flag2 = v.getClassDate().equals(classDate);
            } else if (Objects.isNull(v.getClassDate()) && Objects.isNull(classDate)) {
                flag2 = Boolean.TRUE;
            } else {
                flag2 = Boolean.FALSE;
            }
            //校验班次id
            if (Objects.nonNull(v.getClassId()) && Objects.nonNull(epMeterReadAllDataVO.getClassId())) {
                flag3 = v.getClassId().equals(epMeterReadAllDataVO.getClassId());
            } else if (Objects.isNull(v.getClassId()) && Objects.isNull(epMeterReadAllDataVO.getClassId())) {
                flag3 = Boolean.TRUE;
            } else {
                flag3 = Boolean.FALSE;
            }

            return flag1 && flag2 && flag3 && Objects.nonNull(officeId) && officeId.equals(epMeterReadAllDataVO.getSysOfficeId());
        }).collect(Collectors.toList());
        return productNumObjectList;
    }


    private EpEnergyTreeDTO energyTree(List<Long> partitionIdList, int i) {
        //根据选中的组织机构查询出树
        EpEnergyParamDTO energyParamDTO = new EpEnergyParamDTO();
        energyParamDTO.setPartitionId(partitionIdList);
        energyParamDTO.setType(i);
        EpEnergyTreeDTO childTree = epPartitionService.getChildTree(energyParamDTO);
        return childTree;
    }

    /**
     * 计算
     * @param dataVO 元素vo
     * @param reportMap 报工map
     * @return 班组id
     */
    private Long calClassGroupId(EpMeterReadAllDataVO dataVO, Map<Long, Long> reportMap) {
        //获取报工id集合
        List<Long> reportWorkIdList = dataVO.getReportWorkIdList();
        Map<Long, List<Integer>> dataMap = Maps.newHashMap();
        reportWorkIdList.forEach(ele -> {
            Long orDefault = reportMap.get(ele);
            if (Objects.nonNull(orDefault)) {
                MapUtil.compute(dataMap, orDefault, 1);
            }
        });
        Integer maxSum = -1;
        Long classGroupId = null;
        for (Map.Entry<Long, List<Integer>> entry : dataMap.entrySet()) {
            int sum1 = entry.getValue().stream().filter(v -> Objects.nonNull(v)).mapToInt(v -> v).sum();
            if (maxSum < sum1) {
                maxSum = sum1;
                classGroupId = entry.getKey();
            }
        }
        return classGroupId;
    }

    /**
     * 计算差值
     * @param meterPartitionMap 分区节点的map
     * @param minLocalDate 最小日期值
     * @param map 元素map
     * @return resList
     */
    private  List<EpMeterReadAllDataVO> calDiffValue(Map<Integer, Long> meterPartitionMap, LocalDate minLocalDate, Map<Integer, Map<LocalDate, List<EpMeterRead>>> map) {
        List<EpMeterReadAllDataVO> resList = Lists.newArrayList();
        //遍历仪表
        for (Map.Entry<Integer, Map<LocalDate, List<EpMeterRead>>> entry : map.entrySet()) {
            //记录每个仪表的数据
            BigDecimal preValue = null;
            Map<LocalDate, List<EpMeterRead>> childEntry = entry.getValue();
            for (Map.Entry<LocalDate, List<EpMeterRead>> entry1 : childEntry.entrySet()) {
                //获取到班次日期的数据
                List<EpMeterRead> curEpMeterReadDataList = entry1.getValue();
                if (CollUtil.isNotEmpty(curEpMeterReadDataList)) {
                    for (EpMeterRead epMeterRead : curEpMeterReadDataList) {
                        //对班次和产品厚度进行分组
                        EpMeterReadAllDataVO vo = new EpMeterReadAllDataVO();
                        BeanUtils.copyProperties(epMeterRead, vo);
                        vo.setOfficeId(meterPartitionMap.get(vo.getMeterId()));
                        vo.setConsume(BigDecimalUtil.subtract(epMeterRead.getAdminValue(), preValue));
                        if(Objects.nonNull(epMeterRead.getReportWorkId())){
                            vo.setReportWorkIdList(Lists.newArrayList(epMeterRead.getReportWorkId()));
                        }
                        preValue = epMeterRead.getAdminValue();
                        //最小日期仅仅是为了计算差值，不需要返回
                        if (!entry1.equals(minLocalDate)) {
                            resList.add(vo);
                        }
                    }
                }
            }
        }
        return resList;
    }


    /**
     * 递归计算节点
     *
     * @param map       数据集合
     * @param classDate 班次日期
     * @param ep        能耗信息
     * @param resList   返回集合
     * @return
     */
    private BigDecimal[] cycle(Map<Integer, Map<LocalDate, List<EpMeterReadAllDataVO>>> map, LocalDate classDate, EpPartitionVO ep, List<EpMeterReadAllDataVO> resList, Set<String> existDataSet) {
        Integer meterId = ep.getMeterId();
        Integer entityTable = ep.getEntityTable();
        BigDecimal sum = null;
        BigDecimal yield = null;
        //如果该节点没有挂仪表，则直接返回null，如果挂了仪表则根据类型返回
        if (Objects.nonNull(meterId) && Objects.nonNull(entityTable)) {
            //是虚拟节点
            if (EpManagerConstant.InstrumentType.VIRTUAL.getCode().equals(entityTable) && CollUtil.isNotEmpty(ep.getChildren())) {
                boolean flag = Boolean.FALSE;
                //如果孩子节点不为空
                // 递归所有的孩子节点，算一个和，便是自己的数值
                for (EpPartitionVO child : ep.getChildren()) {
                    BigDecimal[] cycle = cycle(map, classDate, child, resList, existDataSet);
                    sum = BigDecimalUtil.add(sum, cycle[0]);
                    yield = BigDecimalUtil.add(yield, cycle[1]);
                }
                //获取孩子节点挂的仪表类型
                Set<Integer> entityTableCollect = ep.getChildren().stream().map(v -> v.getEntityTable()).collect(Collectors.toSet());
                //如果孩子节点的仪表只有一个
                //N以下各级子节点关联的实体仪表只有实体远传仪表或只有实体非远传仪表
                //统计能耗考虑班次、产品厚度（类型I）/ 仅统计每日能耗（类型II）
                if (!entityTableCollect.contains(EpManagerConstant.InstrumentType.NON_REMOTE.getCode())) {
                    flag = Boolean.TRUE;
                }
                if (flag) {
                    //孩子节点只有实体远传
                    //获取所有的孩子节点挂的仪表
                    Set<Integer> allMeterIdSet = ep.getChildren().stream().map(v -> v.getMeterId()).collect(Collectors.toSet());
                    List<EpMeterReadAllDataVO> allVirtualChildData = Lists.newArrayList();
                    //获取所有的孩子节点关联的仪表id集合
                    for (Integer meter : allMeterIdSet) {
                        List<EpMeterReadAllDataVO> epMeterReads = map.getOrDefault(meter, Maps.newHashMap()).get(classDate);
                        if (CollUtil.isNotEmpty(epMeterReads)) {
                            allVirtualChildData.addAll(epMeterReads);
                        }
                    }
                    Map<String, List<EpMeterReadAllDataVO>> thickDataMap = allVirtualChildData.stream().collect(Collectors.groupingBy(v -> v.getClassId() + "_" + v.getThick()));
                    for (Map.Entry<String, List<EpMeterReadAllDataVO>> thickEntry : thickDataMap.entrySet()) {
                        EpMeterReadAllDataVO ele = new EpMeterReadAllDataVO();
                        BigDecimal reduce = thickEntry.getValue().stream().filter(v -> Objects.nonNull(v.getConsume())).map(v -> v.getConsume()).reduce(BigDecimal.ZERO, BigDecimal::add);
                        BigDecimal produceNum = thickEntry.getValue().stream().filter(v -> Objects.nonNull(v.getProductNum())).map(v -> v.getProductNum()).reduce(BigDecimal.ZERO, BigDecimal::add);
                        BeanUtils.copyProperties(ep, ele);
                        ele.setProductNum(produceNum);
                        ele.setConsume(reduce);
                        String[] s = thickEntry.getKey().split("_");
                        if (!Objects.equals(s[0], "null")) {
                            ele.setClassId(Integer.parseInt(s[0]));
                        }
                        if (!Objects.equals(s[1], "null")) {
                            ele.setThick(new BigDecimal(s[1]));
                        }
                        ele.setReadDate(classDate);
                        ele.setOfficeId(ep.getId());
                        ele.setOfficeName(ep.getOfficeName());
                        if (!existDataSet.contains(classDate + "_" + ele.getMeterId() + "+" + ele.getClassId() + "_" + ele.getThick())) {
                            resList.add(ele);
                            existDataSet.add(classDate + "_" + ele.getMeterId() + "+" + ele.getClassId() + "_" + ele.getThick());
                            MapUtil.biCompute(map, ele.getMeterId(), classDate, ele);
                        }
                    }
                } else {
                    //其他，孩子节点既有实体远传又有实体非远传
                    EpMeterReadAllDataVO epMeterReadAllDataVO = new EpMeterReadAllDataVO();
                    BeanUtils.copyProperties(ep, epMeterReadAllDataVO);
                    epMeterReadAllDataVO.setOfficeId(ep.getId());
                    epMeterReadAllDataVO.setOfficeName(ep.getOfficeName());
                    epMeterReadAllDataVO.setReadDate(classDate);
                    epMeterReadAllDataVO.setConsume(sum);
                    epMeterReadAllDataVO.setProductNum(yield);
                    if (!existDataSet.contains(classDate + "_" + epMeterReadAllDataVO.getMeterId() + "_" + epMeterReadAllDataVO.getClassId() + "_" + epMeterReadAllDataVO.getThick())) {
                        resList.add(epMeterReadAllDataVO);
                        existDataSet.add(classDate + "_" + epMeterReadAllDataVO.getMeterId() + "_" + epMeterReadAllDataVO.getClassId() + "_" + epMeterReadAllDataVO.getThick());
                        MapUtil.biCompute(map, epMeterReadAllDataVO.getMeterId(), classDate, epMeterReadAllDataVO);
                    }
                }
            } else if (EpManagerConstant.InstrumentType.REMOTE.getCode().equals(entityTable)) {
                //实体远传节点
                List<EpMeterReadAllDataVO> epMeterReads = map.getOrDefault(meterId, Maps.newHashMap()).getOrDefault(classDate, Lists.newArrayList());
                sum = epMeterReads.stream().filter(v -> Objects.nonNull(v.getConsume())).map(v -> v.getConsume()).reduce(BigDecimal.ZERO, BigDecimal::add);
                yield = epMeterReads.stream().filter(v -> Objects.nonNull(v.getProductNum())).map(v -> v.getProductNum()).reduce(BigDecimal.ZERO, BigDecimal::add);
                if (CollUtil.isNotEmpty(ep.getChildren())) {
                    for (EpPartitionVO child : ep.getChildren()) {
                        BigDecimal[] cycle = cycle(map, classDate, child, resList, existDataSet);
                        sum = BigDecimalUtil.add(sum, cycle[0]);
                        yield = BigDecimalUtil.add(yield, cycle[1]);
                    }
                }

            } else if (EpManagerConstant.InstrumentType.NON_REMOTE.getCode().equals(entityTable)) {
                //实体非远传节点
                List<EpMeterReadAllDataVO> epMeterReads = map.getOrDefault(meterId, Maps.newHashMap()).getOrDefault(classDate, Lists.newArrayList());
                if (CollUtil.isNotEmpty(epMeterReads)) {
                    EpMeterReadAllDataVO epMeterRead = epMeterReads.get(0);
                    sum = epMeterRead.getConsume();
                    yield = epMeterRead.getProductNum();
                }
                if (CollUtil.isNotEmpty(ep.getChildren())) {
                    for (EpPartitionVO child : ep.getChildren()) {
                        BigDecimal[] cycle = cycle(map, classDate, child, resList, existDataSet);
                        sum = BigDecimalUtil.add(sum, cycle[0]);
                        yield = BigDecimalUtil.add(yield, cycle[1]);
                    }
                }
            }
        }
        return new BigDecimal[]{sum, yield};
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
//                            BigDecimal reduce = meterReadListVO.getMeterReadListVOList().stream()
//                                    .filter(meterReadList -> Objects.nonNull(meterReadList.getReadValueList().get(finalI).getReadValue()))
//                                    .map(meterReadList -> meterReadList.getReadValueList().get(finalI).getReadValue()).reduce(BigDecimal.ZERO, BigDecimal::add);
//                            meterReadListVO.getReadValueList().get(i).setReadValue(reduce);
                            List<List<ReadValueListVO>> readValueAllList = new ArrayList<>();
                            reduceRecursion(meterReadListVO.getMeterReadListVOList(),readValueAllList);
                            BigDecimal sumReduce = readValueAllList.stream().filter(v -> Objects.nonNull(v.get(finalI).getReadValue())).map(v -> v.get(finalI).getReadValue()).reduce(BigDecimal.ZERO, BigDecimal::add);
                            meterReadListVO.getReadValueList().get(i).setReadValue(sumReduce);
                        }
                    }
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
                    BeanUtils.copyProperties(meterReadMapperVO,readMapperCopyVo);

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




