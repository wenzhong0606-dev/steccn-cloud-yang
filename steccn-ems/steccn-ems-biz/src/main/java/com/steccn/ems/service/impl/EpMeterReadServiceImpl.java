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
import com.steccn.authserver.exception.BusinessException;
import com.steccn.authserver.util.SecurityUtils;
import com.steccn.authserver.util.SteccnTableAssertUtil;
import com.steccn.authserver.util.SteccnUser;
import com.steccn.common.util.BigDecimalUtil;
import com.steccn.common.util.LocalDateUtil;
import com.steccn.common.util.MapUtil;
import com.steccn.ems.constant.EpManagerConstant;
import com.steccn.ems.domain.EpInstrumentAccount;
import com.steccn.ems.domain.EpMeterOperationRecord;
import com.steccn.ems.domain.EpMeterRead;
import com.steccn.ems.domain.EpPartition;
import com.steccn.ems.dto.EpEnergyParamDTO;
import com.steccn.ems.dto.EpEnergyTreeDTO;
import com.steccn.ems.dto.EpMeterReadAddDTO;
import com.steccn.ems.dto.EpMeterReadUpdateDTO;
import com.steccn.ems.mapper.EpMeterOperationRecordMapper;
import com.steccn.ems.mapper.EpMeterReadMapper;
import com.steccn.ems.mapper.EpPartitionMapper;
import com.steccn.ems.service.EpInstrumentAccountService;
import com.steccn.ems.service.EpMeterOperationRecordService;
import com.steccn.ems.service.EpMeterReadService;
import com.steccn.ems.service.EpPartitionService;
import com.steccn.ems.vo.*;
import com.steccn.platform.feign.RemoteDictFeignApi;
import com.steccn.platform.feign.RemoteSpareFeignApi;
import com.steccn.pms.feign.RemotePmsFeignApi;
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
    private  EpMeterOperationRecordMapper epMeterOperationRecordMapper;
    @Autowired
    private  EpPartitionMapper epPartitionMapper;
    @Autowired
    private  EpPartitionService epPartitionService;
    @Autowired
    private  EpInstrumentAccountService epInstrumentAccountService;
    @Autowired
    private  EpMeterOperationRecordService epMeterOperationRecordService;
    @Autowired
    private  RemoteDictFeignApi remoteDictFeignApi;
    @Autowired
    private  RemoteSpareFeignApi remoteSpareFeignApi;
    @Autowired
    private  RemotePmsFeignApi remotePmsFeignApi;

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
                endDay = currentLocalDate.plusDays(1);
            }
            localDateList = LocalDateUtil.getLocalDateByStartEndDate(firstDayOfMonth, endDay);
        } else {
            endDay = selectMonth.with(TemporalAdjusters.lastDayOfMonth()).plusDays(1);
            localDateList = LocalDateUtil.getLocalDateByStartEndDate(firstDayOfMonth,endDay);
        }

        List<MeterReadMapperVO> meterReadMapperVOList = this.baseMapper.selectAllByReadMonth(firstDayOfMonth, endDay, meterType);
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
            meterReadListVO.setMeterName(keys[2]);
            meterReadListVO.setPartitionNodeName(keys[4]);

            List<ReadValueListVO> readValueListVOList = new ArrayList<>();
            for (LocalDate localDate : localDateList) {
                List<MeterReadMapperVO> filterList = entry.getValue().stream().filter(meterReadMapperVO -> Objects.equals(meterReadMapperVO.getReadDate(), localDate)).collect(Collectors.toList());
                ReadValueListVO readValueListVO = new ReadValueListVO();
                if (CollectionUtils.isNotEmpty(filterList)) {
                    BigDecimal total = filterList.stream().map(MeterReadMapperVO::getAdminValue).reduce(BigDecimal.ZERO, BigDecimal::add);
                    readValueListVO.setReadValue(total);
                }
                readValueListVO.setEntityTable(readValueListVO.getEntityTable());
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
                    if (meterReadListVO.getEntityTable().equals(2)) {
                        for (int i = 0; i < meterReadListVO.getReadValueList().size(); i++) {
                            int finalI = i;
                            BigDecimal reduce = meterReadListVO.getMeterReadListVOList().stream().map(meterReadList -> meterReadList.getReadValueList().get(finalI).getReadValue()).reduce(BigDecimal.ZERO, BigDecimal::add);
                            meterReadListVO.getReadValueList().get(i).setReadValue(reduce);
                        }
                    }
                }
        ).collect(Collectors.toList());

        EpMeterReadVO eqMeterReadVO = new EpMeterReadVO();
        localDateList.remove(0);
        localDateList.remove(localDateList.size()-1);
        eqMeterReadVO.setLabelList(localDateList);
        eqMeterReadVO.setMeterReadListVOList(meterReadListVOList1);
        return eqMeterReadVO;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean add(EpMeterReadAddDTO dto) {
        EpMeterRead epMeterRead = new EpMeterRead();
        BeanUtils.copyProperties(dto, epMeterRead);
        epMeterRead.setReadValue(dto.getReadValue());
        if (dto.getAdminFlag().equals(1)) {
            epMeterRead.setReadValue(null);
            epMeterRead.setAdminTime(LocalDateTime.now());
            epMeterRead.setAdminValue(dto.getReadValue());
        } else {
            epMeterRead.setReadTime(LocalDateTime.now());
        }
        save(epMeterRead);
        SteccnUser user = SecurityUtils.getUser();
        EpMeterOperationRecord epMeterOperationRecord = new EpMeterOperationRecord();
        epMeterOperationRecord.setReadId(epMeterRead.getId());
        epMeterOperationRecord.setOpNode(0);
        epMeterOperationRecord.setOpBy(user.getUserId());
        epMeterOperationRecord.setOpName(user.getName());
        epMeterOperationRecord.setOpValueOld(dto.getReadValue());
        epMeterOperationRecord.setOpTime(LocalDateTime.now());

        epMeterOperationRecordMapper.insert(epMeterOperationRecord);

        return true;
    }

    @Override
    public EpMeterReadDetailsVO details(String readDate, Integer meterId) {
        EpMeterReadDetailsVO epMeterReadDetailsVO = this.baseMapper.selectByReadDateAndMeterId(readDate, meterId);
        if (Objects.nonNull(epMeterReadDetailsVO) && epMeterReadDetailsVO.getStatus().equals(0)) {
            throw new BusinessException("仪表不存在或停用");
        }
        if (Objects.nonNull(epMeterReadDetailsVO)) {
            List<EpPartition> list = epPartitionService.list();
            Optional<EpPartition> optional = list.stream().filter(epPartition -> epPartition.getId().equals(epMeterReadDetailsVO.getPartitionId())).findAny();
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
        return epMeterReadDetailsVO;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean updateBatch(List<EpMeterReadUpdateDTO> dtoList) {
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
                epMeterOperationRecord.setOpNode(2);
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
        Assert.notEmpty(partitionIdList, "组织机构不能为空");
        Assert.notNull(energyType, "能源类型不能为空");

        //根据选中的组织机构查询出树
        EpEnergyParamDTO energyParamDTO = new EpEnergyParamDTO();
        energyParamDTO.setPartitionId(partitionIdList);
        energyParamDTO.setType(energyType);
        EpEnergyTreeDTO childTree = epPartitionService.getChildTree(energyParamDTO);
        Assert.notEmpty(childTree.getPartitionIds(), "组织机构中没有实体仪表");

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

        List<EpMeterReadAllDataVO> resList = Lists.newArrayList();
        if (CollUtil.isNotEmpty(dateList) && CollUtil.isNotEmpty(meterPartitionMap)) {
            //获取集合最小的日期
            List<LocalDate> sortedDateList = CollectionUtil.sort(dateList, Comparator.comparing(x -> x));
            LocalDate minLocalDate = sortedDateList.get(0).minusDays(1);
            sortedDateList.add(minLocalDate);
            //根据条件获取数据
            List<EpMeterRead> dataList = list(new LambdaQueryWrapper<EpMeterRead>()
                    .in(EpMeterRead::getMeterId, meterPartitionMap.keySet())
                    .in(EpMeterRead::getReadDate, sortedDateList)
                    .orderByAsc(Lists.newArrayList(EpMeterRead::getReadDate, EpMeterRead::getStart, EpMeterRead::getEnd)));
            if (CollUtil.isNotEmpty(dataList)) {
                //存放仪表的班次日期的抄表数据
                Map<Integer, Map<LocalDate, List<EpMeterRead>>> map = new LinkedHashMap<>();
                //将数据按照班次日期分组
                dataList.forEach(ele -> {
                    // 2层map的插入，k1和k2相同的元素，存放到集合中
                    MapUtil.biCompute(map, ele.getMeterId(), ele.getReadDate(), ele);
                });
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
                                preValue = vo.getConsume();
                                //最小日期仅仅是为了计算差值，不需要返回
                                if (!entry1.equals(minLocalDate)) {
                                    resList.add(vo);
                                }
                            }
                        }
                    }
                }

                //对班次和产品厚度进行分组累计
                List<EpMeterReadAllDataVO> gpDataList = Lists.newArrayList();
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
                            BeanUtils.copyProperties(epMeterReadAllDataVO,ele);
                            ele.setConsume(reduce);
                            gpDataList.add(ele);
                            MapUtil.biCompute(calMap, ele.getMeterId(), ele.getReadDate(), ele);
                        }
                    }
                }
                //构造整个虚拟节点
                for (LocalDate classDate : sortedDateList) {
                    for (EpPartitionVO ep : childTree.getTree()) {
                        cycle(calMap, classDate, ep , gpDataList);
                    }
                }
            }
        }
        return resList;
    }

    /**
     * 递归计算节点
     * @param map 数据集合
     * @param classDate 班次日期
     * @param ep 能耗信息
     * @param resList 返回集合
     * @return
     */
    private BigDecimal cycle(Map<Integer, Map<LocalDate, List<EpMeterReadAllDataVO>>> map, LocalDate classDate, EpPartitionVO ep, List<EpMeterReadAllDataVO> resList) {
        Integer meterId = ep.getMeterId();
        Integer entityTable = ep.getEntityTable();
        BigDecimal sum = null;
        if (Objects.nonNull(meterId) && Objects.nonNull(entityTable)) {
            //孩子不为空，就可以往下走，自己可能没有挂仪表，也可能挂了仪表
            if (EpManagerConstant.InstrumentType.VIRTUAL.getCode().equals(entityTable)) {
                boolean flag = Boolean.FALSE;
                //是虚拟节点
                if (CollUtil.isNotEmpty(ep.getChildren())) {
                    for (EpPartitionVO child : ep.getChildren()) {
                        BigDecimal cycle = cycle(map, classDate, child, resList);
                        sum = BigDecimalUtil.add(sum, cycle);
                    }
                    Set<Integer> entityTableCollect = ep.getChildren().stream().map(v -> v.getEntityTable()).collect(Collectors.toSet());
                    if(entityTableCollect.size() == 1 && EpManagerConstant.InstrumentType.REMOTE.getCode().equals(entityTableCollect.stream().findFirst().get())){
                        flag  =Boolean.TRUE;
                    }
                }
                //N以下各级子节点关联的实体仪表只有实体远传仪表或只有实体非远传仪表
                //统计能耗考虑班次、产品厚度（类型I）/ 仅统计每日能耗（类型II）
                if(flag){
                    //只有实体远传
                    Set<Integer> allMeterIdSet = ep.getChildren().stream().map(v -> v.getMeterId()).collect(Collectors.toSet());
                    List<EpMeterReadAllDataVO> allVirtualChildData = Lists.newArrayList();

                    for (Integer meter : allMeterIdSet) {
                        List<EpMeterReadAllDataVO> epMeterReads = map.getOrDefault(meter, Maps.newHashMap()).get(classDate);
                        if(CollUtil.isNotEmpty(epMeterReads)){
                            allVirtualChildData.addAll(epMeterReads);
                        }
                    }
                    Map<String, List<EpMeterReadAllDataVO>> thickDataMap = allVirtualChildData.stream().collect(Collectors.groupingBy(v -> v.getClassId() + "_" + v.getThick()));
                    for (Map.Entry<String, List<EpMeterReadAllDataVO>> thickEntry : thickDataMap.entrySet()) {
                        EpMeterReadAllDataVO ele = new EpMeterReadAllDataVO();
                        BigDecimal reduce = thickEntry.getValue().stream().filter(v -> Objects.nonNull(v.getConsume())).map(v -> v.getConsume()).reduce(BigDecimal.ZERO, BigDecimal::add);
                        ele.setConsume(reduce);
                        BeanUtils.copyProperties(ep,ele);
                        String[] s = thickEntry.getKey().split("_");
                        ele.setClassId(Integer.parseInt(s[0]));
                        ele.setThick(new BigDecimal(s[1]));
                        resList.add(ele);
                    }
                }else {
                   //其他
                    EpMeterReadAllDataVO epMeterReadAllDataVO = new EpMeterReadAllDataVO();
                    BeanUtils.copyProperties(ep,epMeterReadAllDataVO);
                    epMeterReadAllDataVO.setReadDate(classDate);
                    epMeterReadAllDataVO.setConsume(sum);
                    resList.add(epMeterReadAllDataVO);
                }
            } else if (EpManagerConstant.InstrumentType.REMOTE.getCode().equals(entityTable)) {
                //实体远传节点
                List<EpMeterReadAllDataVO> epMeterReads = map.getOrDefault(meterId, Maps.newHashMap()).getOrDefault(classDate,Lists.newArrayList());
                sum = epMeterReads.stream().filter(v -> Objects.nonNull(v.getConsume())).map(v -> v.getConsume()).reduce(BigDecimal.ZERO, BigDecimal::add);
            } else {
                //实体非远传节点
                List<EpMeterReadAllDataVO> epMeterReads = map.getOrDefault(meterId, Maps.newHashMap()).getOrDefault(classDate,Lists.newArrayList());
                if (CollUtil.isNotEmpty(epMeterReads)) {
                    EpMeterReadAllDataVO epMeterRead = epMeterReads.get(0);
                    sum = epMeterRead.getConsume();
                }
            }
        }
        return sum;
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
                    if (Objects.equals(meterReadListVO.getEntityTable(),2)) {
                        for (int i = 0; i < meterReadListVO.getReadValueList().size(); i++) {
                            int finalI = i;
                            BigDecimal reduce = meterReadListVO.getMeterReadListVOList().stream()
                                    .filter(meterReadList -> Objects.nonNull(meterReadList.getReadValueList().get(finalI).getReadValue()))
                                    .map(meterReadList -> meterReadList.getReadValueList().get(finalI).getReadValue()).reduce(BigDecimal.ZERO, BigDecimal::add);
                            meterReadListVO.getReadValueList().get(i).setReadValue(reduce);
                        }
                    }
                }
        ).collect(Collectors.toList());
    }
}




