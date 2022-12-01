package com.steccn.ems.service.impl;

import com.alibaba.nacos.common.utils.CollectionUtils;
import com.alibaba.nacos.common.utils.StringUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.steccn.common.util.LocalDateUtil;
import com.steccn.ems.domain.EpMeterRead;
import com.steccn.ems.mapper.EpMeterReadMapper;
import com.steccn.ems.service.EpMeterReadService;
import com.steccn.ems.vo.EqMeterReadVO;
import com.steccn.ems.vo.MeterReadListVO;
import com.steccn.ems.vo.MeterReadMapperVO;
import com.steccn.ems.vo.ReadValueListVO;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author yequan
 * @description 针对表【ep_meter_read(抄表)】的数据库操作Service实现
 * @createDate 2022-11-30 15:05:32
 */
@Service
public class EpMeterReadServiceImpl extends ServiceImpl<EpMeterReadMapper, EpMeterRead>
        implements EpMeterReadService {

    @Override
    public EqMeterReadVO list(String readMonth, Integer meterType) {
        //当前时间
        LocalDateTime now = LocalDateTime.now();
        //当前日期
        LocalDate currentLocalDateTime = now.toLocalDate();
        //当前时间
        LocalTime currentTime = now.toLocalTime();
        LocalTime localTime = LocalTime.of(9, 0, 0);
        //当前月第一天
        LocalDate firstDayOfMonth = currentLocalDateTime.with(TemporalAdjusters.firstDayOfMonth());
        //查询的最后一天
        LocalDate endDay = currentLocalDateTime;
        if (currentTime.isBefore(localTime)) {
            endDay = currentLocalDateTime.plusDays(1);
        }
        List<LocalDate> localDateList = LocalDateUtil.getLocalDateByStartEndDate(firstDayOfMonth, endDay);
        List<MeterReadMapperVO> meterReadMapperVOList = this.baseMapper.selectAllByReadMonth(readMonth, meterType);
        Map<String, List<MeterReadMapperVO>> groupBy = meterReadMapperVOList
                .stream()
                .collect(Collectors.groupingBy(meterReadMapperVO -> meterReadMapperVO.getParentId()
                        + "," + meterReadMapperVO.getMeterId() + "," + meterReadMapperVO.getMeterName() + ","
                        + meterReadMapperVO.getPartitionId() + "," + meterReadMapperVO.getOfficeName() + "," + meterReadMapperVO.getEntityTable()));

        List<MeterReadListVO> meterReadListVOList = new ArrayList<>();
        for (Map.Entry<String, List<MeterReadMapperVO>> entry : groupBy.entrySet()) {
            MeterReadListVO meterReadListVO = new MeterReadListVO();
            String[] keys = entry.getKey().split(",");
            if (StringUtils.isNotBlank(keys[0])) {
                meterReadListVO.setParentId(Long.parseLong(keys[0]));
            }
            if (StringUtils.isNotBlank(keys[1])) {
                meterReadListVO.setMeterId(Long.parseLong(keys[1]));
            }
            if (StringUtils.isNotBlank(keys[3])) {
                meterReadListVO.setPartitionNode(Long.parseLong(keys[3]));
            }
            if (StringUtils.isNotBlank(keys[5])) {
                meterReadListVO.setEntityTable(Integer.parseInt(keys[5]));
            }
            meterReadListVO.setMeterName(keys[2]);
            meterReadListVO.setPartitionNodeName(keys[4]);

            List<ReadValueListVO> readValueListVOList = new ArrayList<>();
            for (LocalDate localDate : localDateList) {
                List<MeterReadMapperVO> filterList = entry.getValue().stream().filter(meterReadMapperVO -> meterReadMapperVO.getReadDate().isEqual(localDate)).collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(filterList)) {
                    BigDecimal total = filterList.stream().map(MeterReadMapperVO::getAdminValue).reduce(BigDecimal.ZERO, BigDecimal::add);
                    ReadValueListVO readValueListVO = new ReadValueListVO();
                    readValueListVO.setMeterId(filterList.get(0).getMeterId());
                    readValueListVO.setReadDate(filterList.get(0).getReadDate());
                    readValueListVO.setEntityTable(filterList.get(0).getEntityTable());
                    readValueListVO.setReadValue(total);
                    readValueListVOList.add(readValueListVO);
                }
            }
            meterReadListVO.setReadValueList(readValueListVOList);
            meterReadListVOList.add(meterReadListVO);
        }
        List<MeterReadListVO> meterReadListVOList1 = meterReadListVOList.stream().filter(meterReadMapperVO -> Objects.equals(null, meterReadMapperVO.getParentId())).peek(
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

        EqMeterReadVO eqMeterReadVO = new EqMeterReadVO();
        eqMeterReadVO.setLabelList(localDateList);
        eqMeterReadVO.setMeterReadListVOList(meterReadListVOList1);
        return eqMeterReadVO;
    }

    private List<MeterReadListVO> recursion(List<MeterReadListVO> meterReadListVOList, Long partitionNode) {
        return meterReadListVOList.stream().filter(meterReadMapper -> Objects.equals(meterReadMapper.getParentId(), partitionNode)).peek(
                meterReadListVO ->{
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
    }
}




