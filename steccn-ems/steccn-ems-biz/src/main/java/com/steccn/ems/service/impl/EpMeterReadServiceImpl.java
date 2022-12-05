package com.steccn.ems.service.impl;

import cn.hutool.core.date.DatePattern;
import com.alibaba.nacos.common.utils.CollectionUtils;
import com.alibaba.nacos.common.utils.StringUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.steccn.authserver.exception.BusinessException;
import com.steccn.authserver.util.SecurityUtils;
import com.steccn.authserver.util.SteccnUser;
import com.steccn.common.util.LocalDateUtil;
import com.steccn.ems.domain.EpMeterOperationRecord;
import com.steccn.ems.domain.EpMeterRead;
import com.steccn.ems.domain.EpPartition;
import com.steccn.ems.dto.EpMeterReadAddDTO;
import com.steccn.ems.dto.EpMeterReadUpdateDTO;
import com.steccn.ems.mapper.EpMeterOperationRecordMapper;
import com.steccn.ems.mapper.EpMeterReadMapper;
import com.steccn.ems.mapper.EpPartitionMapper;
import com.steccn.ems.service.EpMeterOperationRecordService;
import com.steccn.ems.service.EpMeterReadService;
import com.steccn.ems.service.EpPartitionService;
import com.steccn.ems.vo.*;
import com.steccn.platform.feign.RemoteDictFeignApi;
import com.steccn.platform.vo.SysDictVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
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
@RequiredArgsConstructor
public class EpMeterReadServiceImpl extends ServiceImpl<EpMeterReadMapper, EpMeterRead>
        implements EpMeterReadService {

    private final EpMeterOperationRecordMapper epMeterOperationRecordMapper;

    private final EpPartitionMapper epPartitionMapper;

    private final EpPartitionService epPartitionService;

    private final EpMeterOperationRecordService epMeterOperationRecordService;

    private final RemoteDictFeignApi remoteDictFeignApi;

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
            localDateList = LocalDateUtil.getCurrentMonthDateAll(selectMonth);
        }

        List<MeterReadMapperVO> meterReadMapperVOList = this.baseMapper.selectAllByReadMonth(firstDayOfMonth, endDay, meterType);
        Map<String, List<MeterReadMapperVO>> groupBy = meterReadMapperVOList
                .stream()
                .collect(Collectors.groupingBy(meterReadMapperVO -> meterReadMapperVO.getParentId()
                        + "," + meterReadMapperVO.getMeterId() + "," + meterReadMapperVO.getMeterName() + ","
                        + meterReadMapperVO.getPartitionId() + "," + meterReadMapperVO.getOfficeName() + "," + meterReadMapperVO.getEntityTable()));

        List<MeterReadListVO> meterReadListVOList = new ArrayList<>();
        for (Map.Entry<String, List<MeterReadMapperVO>> entry : groupBy.entrySet()) {
            MeterReadListVO meterReadListVO = new MeterReadListVO();
            String[] keys = entry.getKey().split(",");
            if (StringUtils.isNotBlank(keys[0]) && !"null".equals(keys[0])) {
                meterReadListVO.setParentId(Long.parseLong(keys[0]));
            }
            if (StringUtils.isNotBlank(keys[1]) && !"null".equals(keys[1])) {
                meterReadListVO.setMeterId(Integer.parseInt(keys[1]));
            }
            if (StringUtils.isNotBlank(keys[3]) && !"null".equals(keys[3])) {
                meterReadListVO.setPartitionNode(Long.parseLong(keys[3]));
            }
            if (StringUtils.isNotBlank(keys[5]) && !"null".equals(keys[5])) {
                meterReadListVO.setEntityTable(Integer.parseInt(keys[5]));
            }
            meterReadListVO.setMeterName(keys[2]);
            meterReadListVO.setPartitionNodeName(keys[4]);

            List<ReadValueListVO> readValueListVOList = new ArrayList<>();
            for (LocalDate localDate : localDateList) {
                List<MeterReadMapperVO> filterList = entry.getValue().stream().filter(meterReadMapperVO -> Objects.equals(meterReadMapperVO.getReadDate(), localDate)).collect(Collectors.toList());
                ReadValueListVO readValueListVO = new ReadValueListVO();
                if (CollectionUtils.isNotEmpty(filterList)) {
                    BigDecimal total = filterList.stream().map(MeterReadMapperVO::getAdminValue).reduce(BigDecimal.ZERO, BigDecimal::add);
                    readValueListVO.setEntityTable(filterList.get(0).getEntityTable());
                    readValueListVO.setReadValue(total);
                }
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
        eqMeterReadVO.setLabelList(localDateList);
        eqMeterReadVO.setMeterReadListVOList(meterReadListVOList1);
        return eqMeterReadVO;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean add(EpMeterReadAddDTO dto) {
        EpMeterRead epMeterRead = new EpMeterRead();
        BeanUtils.copyProperties(dto, epMeterRead);
        epMeterRead.setAdminValue(dto.getReadValue());
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
        epMeterOperationRecord.setOpNode(dto.getPartitionId());
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
        SysDictVO sysDictVO = remoteDictFeignApi.getByCode("0", "sys_shift").getData();

        List<EpMeterOperationRecord> epMeterOperationRecordList = new ArrayList<>();
        EpMeterRead meterRead = new EpMeterRead();
        for (EpMeterRead epMeterRead : list) {
            Optional<EpMeterReadUpdateDTO> optional = dtoList.stream().filter(meterReadUpdateDTO -> meterReadUpdateDTO.getMeterId().equals(epMeterRead.getMeterId())
                    && meterReadUpdateDTO.getReadDate().isEqual(epMeterRead.getReadDate())).findAny();
            if (optional.isPresent()) {
                //实体远传表白班的数据不修改
                if (optional.get().getEntityTable().equals(0) && epMeterRead.getClassId().equals(sysDictVO.getId())) {
                    BeanUtils.copyProperties(epMeterRead, meterRead);
                    continue;
                }

                EpMeterOperationRecord epMeterOperationRecord = new EpMeterOperationRecord();
                epMeterOperationRecord.setReadId(epMeterRead.getId());
                epMeterOperationRecord.setOpNode(optional.get().getPartitionId());
                epMeterOperationRecord.setOpBy(user.getUserId());
                epMeterOperationRecord.setOpName(user.getName());
                epMeterOperationRecord.setOpValueNew(optional.get().getReadValue());
                epMeterOperationRecord.setOpTime(LocalDateTime.now());
                epMeterOperationRecord.setOpValueOld(epMeterRead.getAdminValue());
                epMeterOperationRecordList.add(epMeterOperationRecord);

                if (optional.get().getEntityTable().equals(0)) {
                    Optional<EpMeterRead> readOptional = list.stream().filter(read -> read.getReadDate().isEqual(epMeterRead.getReadDate()) && read.getClassId().equals(sysDictVO.getId())).findAny();
                    if (readOptional.isPresent()) {
                        BigDecimal readValue = readOptional.get().getReadValue();
                        BigDecimal updateReadValue = optional.get().getReadValue();
                        epMeterRead.setAdminValue(updateReadValue.subtract(readValue));
                    }
                } else {
                    epMeterRead.setAdminValue(optional.get().getReadValue());
                    if (optional.get().getAdminFlag().equals(0)) {
                        epMeterRead.setReadValue(optional.get().getReadValue());
                    }
                }
            }

            epMeterOperationRecordService.saveBatch(epMeterOperationRecordList);
            updateBatchById(list);
        }
        return true;

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
                    if (meterReadListVO.getEntityTable().equals(2)) {
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




