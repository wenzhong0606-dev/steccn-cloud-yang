package com.steccn.ems.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DatePattern;
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
import java.util.concurrent.atomic.AtomicReference;
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
    @Autowired
    private EpMeterReadDistantService readDistantService;
    @Autowired
    private RemoteOfficeFeign remoteOfficeFeign;
    @Autowired
    private RemoteCheckTaskDetailFeignApi remoteCheckTaskDetailFeignApi;
    @Autowired
    private RemotePmsFeignApi remotePmsFeignApi;

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
    public List<EpMeterReadAllDataVO> classDateAllData(Set<LocalDate> dateSet, Integer energyType, List<Long> partitionIdList) {
        SteccnTableAssertUtil.notEmpty(partitionIdList, "组织机构不能为空");
        Assert.notNull(energyType, "能源类型不能为空");
        Assert.notEmpty(dateSet, "日期筛选不可以为空");
        //获取生产明细样例表
        List<DwProduceOrderDTO> orderDetailList = Lists.newArrayList();
        dateSet.forEach(ele -> orderDetailList.addAll(FeignOptional.of(() -> remotePmsFeignApi.currentProduceOrder(new TupleDTO<>(ele, ele)), FeignConstant.STECCN_PMS_SERVICE_NAME).get()));
        //获取能耗分区所有的节点
        EpEnergyTreeDTO tree = tree(energyType, partitionIdList);
        //查询该能源树下每个节点关联的仪表
        Map<Long, EpInstrumentAccountDTO> nodeLinkMeterMap = epInstrumentAccountService.queryNodeLinkMeter(energyType);
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
        long l = System.currentTimeMillis();
        Map<Long, EpChildTypeDTO> childTypeMap = epPartitionService.childTypeMap(energyType);
        childTypeMap.forEach((k, v) -> v.setAccount(nodeLinkMeterMap.get(k)));
        log.info("总的花费时间 {}", (System.currentTimeMillis() - l));

        //获取远传仪表的数据 key => 仪表id + "_" + 班次日期 +"_" + 班次id + "_" + 厚度 value => 读数
        Map<String, BigDecimal> remoteDataMap = remoteRead(remoteMeterIdSet, dateSet);
        //获取手动抄表的数据 key => 仪表id + "_" + 班次日期  value => 读数
        Map<String, BigDecimal> handReadDataMap = handRead(handMeterIdSet, dateSet);

        //构建能耗分区树二维表格【班次日期，班次，产品厚度，xxx节点，yyy节点....】
        List<BigDecimal> thickList = orderDetailList.stream().map(DwProduceOrderDTO::getThick).distinct().sorted().collect(Collectors.toList());
        //如果厚度集合不包含无厚度，则加入无厚度
        if (!thickList.contains(new BigDecimal("-1"))) {
            thickList.add(new BigDecimal("-1"));
        }
        List<EpEnergyConsumeDTO> rowList = Lists.newArrayList();
        dateSet.forEach(a -> {
            Lists.newArrayList(0, 1, -1).forEach(b -> {
                thickList.forEach(c -> {
                    Map<Long, BigDecimal> consumeMap = Maps.newHashMap();
                    childTypeMap.forEach((k, v) -> {
                        EpInstrumentAccountDTO account = v.getAccount();
                        if(Objects.nonNull(account) && Objects.nonNull(account.getEntityTable())){
                            if (account.getEntityTable() == 0) {
                                //远传仪表
                                consumeMap.put(k, remoteDataMap.get(account.getId() + "_" + a + "_" + b + "_" + c));
                            } else if (account.getEntityTable() == 1 && b == -1) {
                                //表读数来自人工抄表，每天抄一次）：仅统计每日能耗（类型II）
                                consumeMap.put(k, handReadDataMap.get(account.getId() + "_" + a));
                            }else if(v.getAccount().getEntityTable() == 2){
                                //虚拟仪表

                            }
                        }
                    });
                    EpEnergyConsumeDTO row = new EpEnergyConsumeDTO();
                    row.setReadDate(a);
                    row.setClassId(b);
                    row.setThick(c);
                    row.setConsumeMap(consumeMap);
                    rowList.add(row);
                });
            });
        });
        //最后将全量数据表的map转换为有用的list集合 对外输出-根据需要的节点过滤

        return Lists.newArrayList();
    }

    /**
     * 获取手动抄表的数据
     * @param meterIdSet 远传仪表id集合
     * @param dateSet 日期集合
     * @return
     */
    private Map<String, BigDecimal> handRead(Set<Integer> meterIdSet, Set<LocalDate> dateSet) {
        Map<String, BigDecimal> resMap = Maps.newHashMap();

        //获取当前日期最小日期的前一天，用于求差值
        List<LocalDate> sortedDateList = CollectionUtil.sort(dateSet, Comparator.comparing(x -> x));
        LocalDate minLocalDate = sortedDateList.get(0).minusDays(1);
        sortedDateList.add(minLocalDate);

        //查询全量数据表-手动抄表部分【需要查询前一天，涉及差值求当前值】
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
        return resMap;
    }

    /**
     * 获取能源类型对应的能耗分区树
     * @param energyType 能源类型
     * @param partitionIdList 分区节点的集合
     * @return
     */
    private EpEnergyTreeDTO tree(Integer energyType, List<Long> partitionIdList){
        EpEnergyParamDTO energyParamDTO = new EpEnergyParamDTO();
        energyParamDTO.setPartitionId(partitionIdList);
        energyParamDTO.setType(energyType);
        return epPartitionService.getChildTree(energyParamDTO);
    }


    /**
     * 抄表的读数
     * @param meterIdSet 仪表集合
     * @param dateSet 日期集合
     * @return list
     */
    public Map<String,BigDecimal> remoteRead(Set<Integer> meterIdSet, Set<LocalDate> dateSet) {
        Map<String,BigDecimal> resMap = Maps.newHashMap();
        List<EpMeterReadDistant> dbList = readDistantService.list(new LambdaQueryWrapper<EpMeterReadDistant>()
                .in(EpMeterReadDistant::getMeterId, meterIdSet)
                .in(EpMeterReadDistant::getReadDate, dateSet));
        if (CollUtil.isNotEmpty(dbList)) {
            Map<String, List<EpMeterReadDistant>> groupData = dbList.stream().collect(Collectors.groupingBy(v -> v.getMeterId() + "_" + v.getReadDate() + "_"+ v.getClassId() + "_" + v.getThick()));
            groupData.forEach((k, v) -> {
                BigDecimal reduce = v.stream().map(EpMeterReadDistant::getReadValue).filter(e -> Objects.nonNull(e)).reduce(BigDecimal.ZERO, BigDecimal::add);
                resMap.put(k, reduce);
            });
        }
        return resMap;
    }

    /**
     * 老的-全量数据表的逻辑
     * @param dateList 日期集合
     * @param energyType 能源类型
     * @param partitionIdList 所需节点集合
     * @return
     */
    public List<EpMeterReadAllDataVO> classDateAllData1(Set<LocalDate> dateList, Integer energyType, List<Long> partitionIdList) {
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
                .select(EpInstrumentAccount::getId, EpInstrumentAccount::getPartitionNode, EpInstrumentAccount::getEntityTable));
        SteccnTableAssertUtil.notEmpty(instrumentAccountList, "组织机构暂未关联仪表");
        //获取仪表id集合
        Map<Integer, EpInstrumentAccount> meterPartitionMap = Maps.newHashMap();
        instrumentAccountList.forEach(ele -> {
            meterPartitionMap.put(ele.getId(), ele);
        });

        if (CollUtil.isNotEmpty(dateList) && CollUtil.isNotEmpty(meterPartitionMap)) {
            //获取集合最小的日期
            List<LocalDate> sortedDateList = CollectionUtil.sort(dateList, Comparator.comparing(x -> x));
            LocalDate minLocalDate = sortedDateList.get(0).minusDays(1);
            sortedDateList.add(minLocalDate);
            //根据条件获取数据

            //查询全量数据表-手动抄表部分【需要查询前一天，涉及差值求当前值】
            List<EpMeterRead> dataList = list(new LambdaQueryWrapper<EpMeterRead>()
                    .in(EpMeterRead::getMeterId, meterPartitionMap.keySet())
                    .in(EpMeterRead::getReadDate, sortedDateList));
            //查询全量数据表-自动抄表部分【只需要查询当天】
            dataList.addAll(autoRead(meterPartitionMap.keySet(), dateList));
            if (CollUtil.isNotEmpty(dataList)) {
                //将dataList的数据全部转化为EpMeterReadRowDTO
                List<EpMeterReadRowDTO> newDataList = Lists.newArrayList();

                dataList.forEach(ele ->{
                    EpMeterReadRowDTO rowDTO = new EpMeterReadRowDTO();
                    BeanUtils.copyProperties(ele, rowDTO);
                    rowDTO.setProduceOrderId(ele.getReportWorkId());
                    newDataList.add(rowDTO);
                });
                //排序
                newDataList.sort((o1, o2) -> {
                    if(Objects.nonNull(o1.getStart()) && Objects.nonNull(o2.getStart())){
                        return o1.getStart().compareTo(o2.getStart());
                    }
                    if(Objects.isNull(o1.getStart())){
                        return 1;
                    }
                    return -1;
                });

                //存放仪表的班次日期的抄表数据
                Map<Integer, Map<LocalDate, List<EpMeterReadRowDTO>>> map = new LinkedHashMap<>();
                //将数据按照班次日期分组
                newDataList.forEach(ele -> {
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

        List<EpMeterReadAllDataVO> resList = Lists.newArrayList();
        Set<Long> targetIdSet = Sets.newHashSet(energyParamDTO.getPartitionId());

        //将全量数据表的厚度为空的都设置成-1
        gpDataList.forEach(ele -> {
            if(targetIdSet.contains(ele.getOfficeId())){
                if(Objects.isNull(ele.getThick())){
                    ele.setThick(new BigDecimal("-1"));
                }
                //设置组织机构id
                EpPartition epPartition = epPartitionMap.get(ele.getOfficeId());
                if(Objects.nonNull(epPartition)){
                    ele.setSysOfficeId(epPartition.getOfficeId());
                }
                ele.setEnergyType(energyType);
                resList.add(ele);
            }
        });
        return resList;
    }

    /**
     * 抄表的读数
     * @param meterIdSet 仪表集合
     * @param dateSet 日期集合
     * @return list
     */
    public List<EpMeterRead> autoRead(Set<Integer> meterIdSet, Set<LocalDate> dateSet) {
        List<EpMeterRead> resList = Lists.newArrayList();
        List<EpMeterReadDistant> dbList = readDistantService.list(new LambdaQueryWrapper<EpMeterReadDistant>()
                .in(EpMeterReadDistant::getMeterId, meterIdSet)
                .in(EpMeterReadDistant::getReadDate, dateSet));
        if (CollUtil.isNotEmpty(dbList)) {
            Map<String, List<EpMeterReadDistant>> collect = dbList.stream().collect(Collectors.groupingBy(v -> v.getMeterId() + "_" + v.getClassId() + "_" + v.getThick() +"_" + v.getProduceOrderId()));
            collect.forEach((k, v) -> {
                EpMeterReadDistant ele = v.get(0);
                EpMeterRead model = new EpMeterRead();
                BeanUtils.copyProperties(ele, model);
                BigDecimal reduce = v.stream().map(EpMeterReadDistant::getReadValue).filter(e -> Objects.nonNull(e)).reduce(BigDecimal.ZERO, BigDecimal::add);
                model.setAdminFlag(MpConstant.NO);
                model.setAdminValue(reduce);
                model.setReadValue(reduce);
                model.setReportWorkId(ele.getProduceOrderId());
                resList.add(model);
            });
        }
        return resList;
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
                .select(EpInstrumentAccount::getId, EpInstrumentAccount::getPartitionNode, EpInstrumentAccount::getEntityTable));
        SteccnTableAssertUtil.notEmpty(instrumentAccountList, "组织机构暂未关联仪表");
        //获取仪表id集合
        Map<Integer, EpInstrumentAccount> meterPartitionMap = Maps.newHashMap();
        instrumentAccountList.forEach(ele -> {
            meterPartitionMap.put(ele.getId(), ele);
        });

        if (CollUtil.isNotEmpty(dateList) && CollUtil.isNotEmpty(meterPartitionMap)) {
            //查询全量节点映射关系
            Map<Long, EpPartition> partitionMap = epPartitionService.listById(childTree.getPartitionIds());

            //获取集合最小的日期
            List<LocalDate> sortedDateList = CollectionUtil.sort(dateList, Comparator.comparing(x -> x));
            LocalDate minLocalDate = sortedDateList.get(0).minusDays(1);
            sortedDateList.add(0 , minLocalDate);
            //查询全量数据表-手动抄表部分【需要多查询一天，用于差值求当前值】
            List<EpMeterRead> dataList = list(new LambdaQueryWrapper<EpMeterRead>()
                    .in(EpMeterRead::getMeterId, meterPartitionMap.keySet())
                    .in(EpMeterRead::getReadDate, sortedDateList)
                    .orderByAsc(Lists.newArrayList(EpMeterRead::getReadDate, EpMeterRead::getStart)));

            //查询全量数据表-自动抄表部分【只需要查询当天】
            dataList.addAll(autoRead(meterPartitionMap.keySet(),dateList));

            if (CollUtil.isNotEmpty(dataList)) {
                //将dataList的数据全部转化为EpMeterReadRowDTO
                List<EpMeterReadRowDTO> newDataList = Lists.newArrayList();
                //获取基片产线的每一行对应的拉引量
                Set<Integer> jpLineSet = getLine(PmManagerConstant.UROPATAGIA);
                List<QtEmsPullAmountDTO> jpDataList = Lists.newArrayList();

                //获取深加工产线的每一行对应的产量
                Set<Integer> sjgLineSet = getLine(PmManagerConstant.DEEP_PROCESSING);
                //过滤出所有的深加工节点
                EpInstrumentAccount defaultAccount = new EpInstrumentAccount();
                EpPartition defaultPartition = new EpPartition();
                List<EpMeterReadRowDTO> sjgDataList = Lists.newArrayList();

                dataList.forEach(ele -> {
                    EpMeterReadRowDTO rowDTO = new EpMeterReadRowDTO();
                    rowDTO.setUuid(IdWorker.getId());
                    BeanUtils.copyProperties(ele, rowDTO);
                    rowDTO.setProduceOrderId(ele.getReportWorkId());
                    EpInstrumentAccount account = meterPartitionMap.getOrDefault(ele.getMeterId(), defaultAccount);
                    EpPartition epPartition = partitionMap.getOrDefault(account.getPartitionNode(), defaultPartition);
                    if (sjgLineSet.contains(epPartition.getOfficeId())) {
                        //深加工产线节点
                        sjgDataList.add(rowDTO);
                    } else if (jpLineSet.contains(epPartition.getOfficeId())) {
                        //基片产线节点
                        rowDTO.setProductLineId(epPartition.getOfficeId());
                        jpDataList.add(convertToJpDto(rowDTO));
                    }
                    newDataList.add(rowDTO);
                });

                //计算【生产明细表】中基片产线的每一行对应的拉引量（从窑炉拉引出的玻璃液的重量），填入右侧追加的“基片拉引量”一列，单位为吨，小数点后最多2位
                //拉引量 = (平均单片掰边重量 + 平均单片重量) * （拉引时速 /玻璃片长度) * 生产时长 / 1000000
                //平均单片掰边重量/平均单片重量/拉引时速：将满足下列条件的巡检任务录入的“单片掰边重量”/“单片重量”/“拉引速度”（需提前配置好“单片掰边重量”/“单片重量”/“拉引速度”检验项）分别取平均值，单位分别为kg、kg、m/h
                //检验完成时间在当前行的班次时段内（例如2021-09-21白班 8:00:00 ~ 20:00:00）
                //检验任务的产线为当前行的产线 检验任务的物料为当前行的生产订单中的物料
                //玻璃片长度：当前行的生产订单中的物料，取其“玻璃片长度”属性的值，单位为mm
                //生产时长：当前行的本班次生产结束时间 - 当前行的本班次生产开始时间，单位为h
                List<QtEmsPullAmountDTO> qmsNumList = FeignOptional.of(() -> remoteCheckTaskDetailFeignApi.getEmsPullAmount(new ValidList<>(jpDataList)), FeignConstant.STECCN_QMS_SERVICE_NAME).get();
                Map<Long,QtEmsPullAmountDTO> jpUuidMap = Maps.newHashMap();
                qmsNumList.forEach(ele -> jpUuidMap.put(ele.getUuid(),ele));

                //计算【生产明细表】中深加工产线的每一行对应的深加工产量（即成品玻璃的产量），填入右侧追加的“深加工产量”一列，单位为㎡
                //深加工产量 = 将满足下列条件的报工记录上报的片数折算成面积，单位为㎡
                //报工记录关联的生产订单为当前行的生产订单
                //报工记录的时段在当前行的班次时段内
                List<EpMeterReadRowDTO> epMeterReadRowDTOList = FeignOptional.of(() -> remoteReportWorkFeignApi.sjgOrderRw(sjgDataList), FeignConstant.STECCN_PMS_SERVICE_NAME).get();
                Map<Long,EpMeterReadRowDTO> sjgUuidMap = Maps.newHashMap();
                epMeterReadRowDTOList.forEach(ele -> sjgUuidMap.put(ele.getUuid(),ele));
                //获取报工集合
                Set<Long> reportWorkIdSet = epMeterReadRowDTOList.stream().flatMap(v -> v.getReportWorkIdSet().stream()).collect(Collectors.toSet());
                //获取每一个报工对应的班组
                Map<Long, Long> reportMap = FeignOptional.of(() -> remoteReportWorkFeignApi.reportWorkClassGroup(reportWorkIdSet), FeignConstant.STECCN_PMS_SERVICE_NAME).get();

                //存放仪表的班次日期的抄表数据
                Map<Integer, Map<LocalDate, List<EpMeterReadRowDTO>>> map = new LinkedHashMap<>();
                //将数据按照仪表id和班次日期分组
                newDataList.forEach(ele -> {
                    if (sjgUuidMap.containsKey(ele.getUuid())) {
                        //如果ele的uuid在epMeterReadRowDTOList集合中，则替换掉
                        EpMeterReadRowDTO sjgNum = sjgUuidMap.get(ele.getUuid());
                        ele.setYield(sjgNum.getYield());
                        ele.setReportWorkIdSet(sjgNum.getReportWorkIdSet());
                    } else if (jpUuidMap.containsKey(ele.getUuid())) {
                        QtEmsPullAmountDTO jpNum = jpUuidMap.get(ele.getUuid());
                        ele.setYield(jpNum.getPullAmount());
                    }
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
                            BigDecimal productNum = thickEntry.getValue().stream().filter(v -> Objects.nonNull(v.getProductNum())).map(v -> v.getProductNum()).reduce(BigDecimal.ZERO, BigDecimal::add);
                            ele.setProductNum(productNum);
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
     * 将EpMeterReadRowDTO 转换为QtEmsPullAmountDTO
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
    private  List<EpMeterReadAllDataVO> calDiffValue(Map<Integer, EpInstrumentAccount> meterPartitionMap, LocalDate minLocalDate, Map<Integer, Map<LocalDate, List<EpMeterReadRowDTO>>> map) {
        EpInstrumentAccount defaultAccount = new EpInstrumentAccount();
        List<EpMeterReadAllDataVO> resList = Lists.newArrayList();
        //遍历仪表
        for (Map.Entry<Integer, Map<LocalDate, List<EpMeterReadRowDTO>>> entry : map.entrySet()) {
            //记录每个仪表的数据
            BigDecimal preValue = null;
            Map<LocalDate, List<EpMeterReadRowDTO>> childEntry = entry.getValue();
            for (Map.Entry<LocalDate, List<EpMeterReadRowDTO>> entry1 : childEntry.entrySet()) {
                //获取到班次日期的数据
                List<EpMeterReadRowDTO> curEpMeterReadDataList = entry1.getValue();
                if (CollUtil.isNotEmpty(curEpMeterReadDataList)) {
                    for (EpMeterReadRowDTO epMeterRead : curEpMeterReadDataList) {
                        //对班次和产品厚度进行分组
                        EpMeterReadAllDataVO vo = new EpMeterReadAllDataVO();
                        BeanUtils.copyProperties(epMeterRead, vo);
                        EpInstrumentAccount account = meterPartitionMap.getOrDefault(vo.getMeterId(),defaultAccount);
                        vo.setOfficeId(account.getPartitionNode());
                        if (account.getEntityTable() == 0) {
                           vo.setConsume(epMeterRead.getAdminValue());
                        }else{
                            vo.setConsume(BigDecimalUtil.subtract(epMeterRead.getAdminValue(), preValue));
                        }
                        //设置产量
                        vo.setProductNum(epMeterRead.getYield());
                        if(CollUtil.isNotEmpty(epMeterRead.getReportWorkIdSet())){
                            vo.setReportWorkIdList(Lists.newArrayList(epMeterRead.getReportWorkIdSet()));
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




